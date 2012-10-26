/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package com.google.api.services.freebase;

import com.google.api.client.json.GenericJson;
import javax.annotation.Nullable;

/**
 *
 * @author hamish
 */
@Nullable
public class SearchMatch extends GenericJson {

    @com.google.api.client.util.Key(value = "mid")
    private String matchId;

    @com.google.api.client.util.Key
    private String name;

    @com.google.api.client.util.Key
    private FreebaseType notable;

    @com.google.api.client.util.Key("lang")
    private String language;

    @com.google.api.client.util.Key
    private Double score;

    public SearchMatch() {
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FreebaseType getNotable() {
        return notable;
    }

    public void setNotable(FreebaseType notable) {
        this.notable = notable;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
    
}
