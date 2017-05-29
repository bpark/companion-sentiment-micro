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

import com.github.bpark.companion.codecs.IntegerArrayCodec;
import com.github.bpark.companion.codecs.StringArrayCodec;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SentimentVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SentimentVerticle.class);

    private static final String ADDRESS = "sentiment.calculate";

    private static final String TAB_SEP = "\t";
    private static final String MAPPING_FILE = "/AFINN-111.txt";

    @Override
    public void start() throws Exception {
        super.start();

        this.vertx.eventBus().getDelegate().registerDefaultCodec(String[].class, new StringArrayCodec());
        this.vertx.eventBus().getDelegate().registerDefaultCodec(Integer[].class, new IntegerArrayCodec());

        Map<String, Integer> sentimentMap = readSentimentFile();

        EventBus eventBus = vertx.eventBus();

        MessageConsumer<String[]> consumer = eventBus.consumer(ADDRESS);
        Observable<Message<String[]>> observable = consumer.toObservable();
        observable.subscribe(message -> {
            String[] words = message.body();

            logger.info("received words: {}", Arrays.asList(words));

            List<Integer> result = new ArrayList<>();

            for (String word : words) {
                Integer sentimentValue = sentimentMap.get(word);
                result.add(sentimentValue != null ? sentimentValue : 0);
            }

            logger.info("sentiment weights: {}", result);

            message.reply(result);
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

}
