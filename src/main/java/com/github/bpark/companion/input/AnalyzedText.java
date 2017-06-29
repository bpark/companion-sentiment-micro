package com.github.bpark.companion.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzedText {

    private List<Sentence> sentences;

    public List<Sentence> getSentences() {
        return sentences;
    }
}
