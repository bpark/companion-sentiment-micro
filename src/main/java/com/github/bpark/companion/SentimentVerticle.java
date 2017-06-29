/*
 * Copyright 2017 Kurt Sparber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.bpark.companion;

import com.github.bpark.companion.input.AnalyzedText;
import com.github.bpark.companion.model.CalculatedSentiment;
import com.github.bpark.companion.model.SentimentAnalysis;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SentimentVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SentimentVerticle.class);

    private static final String ADDRESS = "sentiment.calculate";

    private static final String NLP_KEY = "nlp";
    private static final String SENTIMENT_KEY = "sentiment";

    private static final String TAB_SEP = "\t";
    private static final String MAPPING_FILE = "/AFINN-111.txt";

    @Override
    public void start() throws Exception {
        super.start();

        Map<String, Integer> sentimentMap = readSentimentFile();

        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String> consumer = eventBus.consumer(ADDRESS);
        Observable<Message<String>> observable = consumer.toObservable();

        observable.subscribe(message -> {

            String id = message.body();

            readMessage(id).flatMap(content -> {
                logger.info("text to analyze for sentences: {}", content);

                List<CalculatedSentiment> calculatedSentiments = content.getSentences().stream().map(sentence -> {
                    String[] words = sentence.getTokens();

                    logger.info("received words: {}", Arrays.asList(words));

                    List<Integer> result = new ArrayList<>();

                    for (String word : words) {
                        Integer sentimentValue = sentimentMap.get(word);
                        result.add(sentimentValue != null ? sentimentValue : 0);
                    }

                    double average = result.stream().mapToInt(r -> r).average().orElse(0);

                    return new CalculatedSentiment(average, result);
                }).collect(Collectors.toList());

                return Observable.just(new SentimentAnalysis(calculatedSentiments));
            }).flatMap(analyses -> saveMessage(id, analyses)).subscribe(a -> message.reply(id));;

        });

    }

    private Map<String, Integer> readSentimentFile() throws IOException {
        try (InputStream resource = SentimentVerticle.class.getResourceAsStream(MAPPING_FILE)) {
            Stream<String> lineStream = new BufferedReader(new InputStreamReader(resource, UTF_8)).lines();
            List<String> lines = lineStream.collect(Collectors.toList());
            Map<String, Integer> sentimentMap = new HashMap<>();
            lines.forEach(line -> {
                String[] split = line.split(TAB_SEP);
                sentimentMap.put(split[0], Integer.valueOf(split[1]));
            });

            return sentimentMap;
        }
    }

    private Observable<AnalyzedText> readMessage(String id) {
        return vertx.sharedData().<String, String>rxGetClusterWideMap(id)
                .flatMap(map -> map.rxGet(NLP_KEY))
                .flatMap(content -> Single.just(Json.decodeValue(content, AnalyzedText.class)))
                .toObservable();
    }

    private Observable<Void> saveMessage(String id, SentimentAnalysis sentimentAnalysis) {
        return vertx.sharedData().<String, String>rxGetClusterWideMap(id)
                .flatMap(map -> map.rxPut(SENTIMENT_KEY, Json.encode(sentimentAnalysis)))
                .toObservable();
    }

}
