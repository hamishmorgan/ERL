package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;
import javax.annotation.Nullable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 10/04/2013
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public class LinkRequest extends GenericJson {

    @Nullable
    @com.google.api.client.util.Key
    private List<Document> documents = null;

    @Nullable
    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    interface SentenceSequence extends ChunkSequence {
        @Nullable
        List<Sentence> getSentences();

        void setSentences(List<Sentence> chunks);

    }

    interface ChunkSequence extends TokenSequence {
        @Nullable
        List<Chunk> getChunks();

        void setChunks(List<Chunk> chunks);

    }


    interface TokenSequence {
        @Nullable
        String getText();

        void setText(String text);

        @Nullable
        List<Token> getTokens();

        void setTokens(List<Token> text);

    }


    interface Entity {
        @Nullable
        String getEntityType();

        void setEntityType(String text);

        @Nullable
        String getEntityId();

        void setEntityId(String id);

        @Nullable
        String getEntityUrl();

        void setEntityUrl(String url);
    }

    public static class Document extends GenericJson implements SentenceSequence {

        @Nullable
        @com.google.api.client.util.Key
        private List<Sentence> sentences = null;
        @Nullable
        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @Nullable
        @com.google.api.client.util.Key
        private List<Chunk> chunks = null;
        @Nullable
        @com.google.api.client.util.Key
        private String text = null;

        @Nullable
        public List<Sentence> getSentences() {
            return sentences;
        }

        public void setSentences(List<Sentence> sentences) {
            this.sentences = sentences;
        }

        @Nullable
        public List<Chunk> getChunks() {
            return chunks;
        }

        public void setChunks(List<Chunk> chunks) {
            this.chunks = chunks;
        }

        @Nullable
        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Nullable
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Sentence extends GenericJson implements ChunkSequence {

        @Nullable
        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @Nullable
        @com.google.api.client.util.Key
        private List<Chunk> chunks = null;
        @Nullable
        @com.google.api.client.util.Key
        private String text = null;

        @Nullable
        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Nullable
        public List<Chunk> getChunks() {
            return chunks;
        }

        public void setChunks(List<Chunk> chunks) {
            this.chunks = chunks;
        }

        @Nullable
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Chunk extends GenericJson implements Entity, TokenSequence {

        @Nullable
        @com.google.api.client.util.Key
        private List<Token> tokens = null;
        @Nullable
        @com.google.api.client.util.Key
        private String text = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityType = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityId = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityUrl = null;

        @Nullable
        public List<Token> getTokens() {
            return tokens;
        }

        public void setTokens(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Nullable
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Nullable
        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        @Nullable
        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        @Nullable
        public String getEntityUrl() {
            return entityUrl;
        }

        public void setEntityUrl(String entityUrl) {
            this.entityUrl = entityUrl;
        }
    }

    public static class Token extends GenericJson implements Entity {

        @Nullable
        @com.google.api.client.util.Key
        private String text = null;
        @Nullable
        @com.google.api.client.util.Key
        private String pos = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityType = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityId = null;
        @Nullable
        @com.google.api.client.util.Key
        private String entityUrl = null;

        @Nullable
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Nullable
        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        @Nullable
        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        @Nullable
        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        @Nullable
        public String getEntityUrl() {
            return entityUrl;
        }

        public void setEntityUrl(String entityUrl) {
            this.entityUrl = entityUrl;
        }
    }

}
