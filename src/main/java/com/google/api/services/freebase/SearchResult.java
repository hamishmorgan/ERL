/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package com.google.api.services.freebase;

import com.google.api.client.json.GenericJson;
import java.util.List;
import javax.annotation.Nullable;

/**
 *
 * @author hamish
 */
@Nullable
public class SearchResult extends GenericJson {

    @com.google.api.client.util.Key
    private String status;

    @com.google.api.client.util.Key
    private Long cursor;

    @com.google.api.client.util.Key
    private Long cost;

    @com.google.api.client.util.Key
    private Long hits;

    @com.google.api.client.util.Key(value = "result")
    private List<SearchMatch> matches;

    public SearchResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCursor() {
        return cursor;
    }

    public void setCursor(Long cursor) {
        this.cursor = cursor;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }

    public List<SearchMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatch> results) {
        this.matches = results;
    }
}
