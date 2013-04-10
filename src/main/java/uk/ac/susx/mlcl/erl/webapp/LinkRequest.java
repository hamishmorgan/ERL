package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 10/04/2013
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public class LinkRequest extends GenericJson {

    @com.google.api.client.util.Key
    private List<Document> documents = null;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    interface SentenceSequence extends ChunkSequence {
        List<Sentence> getSentences();

        void setSentences(List<Sentence> chunks);

    }

    interface ChunkSequence extends TokenSequence {
        List<Chunk> getChunks();

        void setChunks(List<Chunk> chunks);

    }


    interface TokenSequence {
        String getText();

        void setText(String text);

        List<Token> getTokens();

        void setTokens(List<Token> text);

    }


    interface Entity {
        String getEntityType();

        void setEntityType(String text);

        String getEntityId();

        void setEntityId(String id);

        String getEntityUrl();

        void setEntityUrl(String url);
    }

    public static class Document extends GenericJson implements SentenceSequence {

        @com.google.api.client.util.Key
        private List<Sentence> sentences = null;
        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @com.google.api.client.util.Key
        private List<Chunk> chunks = null;
        @com.google.api.client.util.Key
        private String text = null;

        public List<Sentence> getSentences() {
            return sentences;
        }

        public void setSentences(List<Sentence> sentences) {
            this.sentences = sentences;
        }

        public List<Chunk> getChunks() {
            return chunks;
        }

        public void setChunks(List<Chunk> chunks) {
            this.chunks = chunks;
        }

        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Sentence extends GenericJson implements ChunkSequence {

        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @com.google.api.client.util.Key
        private List<Chunk> chunks = null;
        @com.google.api.client.util.Key
        private String text = null;

        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        public List<Chunk> getChunks() {
            return chunks;
        }

        public void setChunks(List<Chunk> chunks) {
            this.chunks = chunks;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Chunk extends GenericJson implements Entity, TokenSequence {

        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @com.google.api.client.util.Key
        private String text = null;
        @com.google.api.client.util.Key
        private String entityType = null;
        @com.google.api.client.util.Key
        private String entityId = null;
        @com.google.api.client.util.Key
        private String entityUrl = null;

        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getEntityUrl() {
            return entityUrl;
        }

        public void setEntityUrl(String entityUrl) {
            this.entityUrl = entityUrl;
        }
    }

    public static class Token extends GenericJson implements Entity {

        @com.google.api.client.util.Key
        private String text = null;
        @com.google.api.client.util.Key
        private String pos = null;
        @com.google.api.client.util.Key
        private String entityType = null;
        @com.google.api.client.util.Key
        private String entityId = null;
        @com.google.api.client.util.Key
        private String entityUrl = null;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getEntityUrl() {
            return entityUrl;
        }

        public void setEntityUrl(String entityUrl) {
            this.entityUrl = entityUrl;
        }
    }

}
