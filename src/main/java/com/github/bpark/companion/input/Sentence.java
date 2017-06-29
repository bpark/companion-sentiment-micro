package com.github.bpark.companion.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sentence {

    private String[] tokens;

    public String[] getTokens() {
        return tokens;
    }
}
