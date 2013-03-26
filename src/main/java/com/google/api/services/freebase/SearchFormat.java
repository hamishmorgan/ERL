/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package com.google.api.services.freebase;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.NullValue;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author hamish
 */
@Nonnull
public enum SearchFormat {

    CLASSIC(ClassicResult.class),
    AC(EntityResult.class),
    @NullValue
    ENTITY(EntityResult.class),
    GUIDS(IdsResult.class),
    IDS(IdsResult.class),
    MIDS(IdsResult.class);

    private static final SearchFormat DEFAULT = ENTITY;

    private final Class<? extends AbstractResult> dataClass;

    private SearchFormat(Class<? extends AbstractResult> dataClass) {
        this.dataClass = dataClass;
    }

    public Class<? extends AbstractResult> getDataClass() {
        return dataClass;
    }

    /**
     * A JSON object that contains a single property id.
     */
    @Nullable
    public static class Id extends GenericJson {

        @com.google.api.client.util.Key
        private String id;

        public Id() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * Types are always produced as an id with a name property.
     */
    @Nullable
    public static class Type extends Id {

        @com.google.api.client.util.Key
        private String name;

        public Type() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Common properties of all types of search result set.
     */
    @Nullable
    @Nonnegative
    public static abstract class AbstractResult extends GenericJson {

        @com.google.api.client.util.Key
        private String status;

        @com.google.api.client.util.Key
        private Long cursor;

        @com.google.api.client.util.Key
        private Long cost;

        @com.google.api.client.util.Key
        private Long hits;

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
            Preconditions.checkArgument(cursor >= 0L, "cursor < 0");
            this.cursor = cursor;
        }

        public Long getCost() {
            return cost;
        }

        public void setCost(Long cost) {
            Preconditions.checkArgument(cost >= 0L, "cost < 0");
            this.cost = cost;
        }

        public Long getHits() {
            return hits;
        }

        public void setHits(Long hits) {
            Preconditions.checkArgument(hits >= 0L, "hits < 0");
            this.hits = hits;
        }
    }

    /**
     * The classic result format have an addition property "code"
     */
    @Nullable
    public abstract static class AbstractOldResult extends AbstractResult {

        @com.google.api.client.util.Key
        private String code;

        public AbstractOldResult() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * The IDS, MIDS, and GIDS result types are all effectively identical,
     * containing a result property which is a list of string identifiers.
     */
    @Nullable
    public final static class IdsResult extends AbstractOldResult {

        @com.google.api.client.util.Key("result")
        private List<String> results;

        /**
         * only in IDS format (not GIDS or MIDS)
         */
        @com.google.api.client.util.Key
        private Long mql;

        public IdsResult() {
        }

        public List<String> getResults() {
            return results;
        }

        public void setResults(List<String> results) {
            this.results = results;
        }

        public Long getMql() {
            return mql;
        }

        public void setMql(Long mql) {
            this.mql = mql;
        }
    }

    @Nullable
    public final static class EntityResult extends AbstractResult {

        @com.google.api.client.util.Key(value = "result")
        private List<Entity> results;

        public EntityResult() {
        }

        public List<Entity> getResults() {
            return results;
        }

        public void setResults(List<Entity> results) {
            this.results = results;
        }
    }

    /**
     *
     * @author hamish
     */
    @Nullable
    public static final class Entity extends GenericJson {

        @com.google.api.client.util.Key(value = "mid")
        private String matchId;

        @com.google.api.client.util.Key
        private String name;

        @com.google.api.client.util.Key
        private Type notable;

        @com.google.api.client.util.Key("lang")
        private String language;

        @com.google.api.client.util.Key
        private Double score;

        public Entity() {
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

        public Type getNotable() {
            return notable;
        }

        public void setNotable(Type notable) {
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

    @Nullable
    public final static class ClassicResult extends AbstractOldResult {

        @com.google.api.client.util.Key(value = "result")
        private List<Entity> results;

        public ClassicResult() {
        }

        public List<Entity> getResults() {
            return results;
        }

        public void setResults(List<Entity> results) {
            this.results = results;
        }
    }

    /**
     *
     * @author hamish
     */
    @Nullable
    public static final class ClassicEntity extends Id {

        @com.google.api.client.util.Key("alias")
        private List<String> aliases;

        @com.google.api.client.util.Key
        private Id article;

        @com.google.api.client.util.Key
        private Id image;

        @com.google.api.client.util.Key
        private String name;

        @com.google.api.client.util.Key("type")
        private List<Type> types;

        @com.google.api.client.util.Key("relevance:score")
        private Double score;

        public ClassicEntity() {
        }

        public List<String> getAliases() {
            return aliases;
        }

        public void setAliases(List<String> aliases) {
            this.aliases = aliases;
        }

        public Id getArticle() {
            return article;
        }

        public void setArticle(Id article) {
            this.article = article;
        }

        public Id getImage() {
            return image;
        }

        public void setImage(Id image) {
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Type> getTypes() {
            return types;
        }

        public void setTypes(List<Type> types) {
            this.types = types;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
