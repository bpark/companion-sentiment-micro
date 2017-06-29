package com.github.bpark.companion.model;

import java.util.List;

/**
 * @author ksr
 */
@SuppressWarnings("unused")
public class SentimentAnalysis {

    private List<CalculatedSentiment> sentiments;

    public SentimentAnalysis(List<CalculatedSentiment> sentiments) {
        this.sentiments = sentiments;
    }

    public List<CalculatedSentiment> getSentiments() {
        return sentiments;
    }
}
