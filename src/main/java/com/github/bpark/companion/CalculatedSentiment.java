package com.github.bpark.companion;

import java.util.List;

/**
 * @author ksr
 */
public class CalculatedSentiment {

    private Double value;

    private List<Integer> wordValues;

    public CalculatedSentiment(Double value, List<Integer> wordValues) {
        this.value = value;
        this.wordValues = wordValues;
    }

    public Double getValue() {
        return value;
    }

    public List<Integer> getWordValues() {
        return wordValues;
    }
}
