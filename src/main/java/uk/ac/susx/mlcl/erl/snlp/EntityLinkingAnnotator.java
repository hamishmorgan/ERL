/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.susx.mlcl.erl.kb.CachedKnowledgeBase;
import uk.ac.susx.mlcl.erl.kb.FreebaseKB;
import uk.ac.susx.mlcl.erl.kb.KnowledgeBase;

/**
 *
 * @author hamish
 */
public class EntityLinkingAnnotator implements Annotator {

    private final KnowledgeBase knowledgeBase;

    public EntityLinkingAnnotator(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = CachedKnowledgeBase.wrap(knowledgeBase);
    }

    static EntityLinkingAnnotator newInstance() throws IOException {

        KnowledgeBase kb = FreebaseKB.newInstance();
        return new EntityLinkingAnnotator(kb);

    }

    /**
     *
     * @return
     */
    public Set<Class<? extends CoreAnnotation<?>>> getRequiredAnnotations() {
        final Set<Class<? extends CoreAnnotation<?>>> requirements =
                new HashSet<Class<? extends CoreAnnotation<?>>>();
        requirements.add(TokensAnnotation.class);
        requirements.add(NamedEntityTagAnnotation.class);
        return requirements;
    }

    /**
     * Get a collection of all the annotation types that are produced by this annotator.
     * <p/>
     * @return annotations produced by the annotator
     */
    public Set<Class<? extends CoreAnnotation<?>>> getSuppliedAnnotations() {
        return Collections.<Class<? extends CoreAnnotation<?>>>singleton(
                EntityKbIdAnnotation.class);
    }

    /**
     *
     * @param annotation
     */
    public void annotate(Annotation document) {
        Preconditions.checkNotNull(document, "annotation");

        // Check requirements
//        Preconditions.checkArgument(document.containsKey(TokensAnnotation.class),
//                                    "TokensAnnotation is not present.");
//        Preconditions.checkArgument(annotation.containsKey(NamedEntityTagAnnotation.class),
//                                    "NamedEntityTagAnnotation is not present.");

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {

            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

            if (tokens.isEmpty()) {
                continue;
            }

            int start = 0;
            int end = 0;

            while (start < tokens.size()) {
                end = start;
                final String entityLabel =
                        tokens.get(end).get(NamedEntityTagAnnotation.class);
                end++;

                while (end < tokens.size() && tokens.get(end).get(NamedEntityTagAnnotation.class).equals(entityLabel)) {
                    end++;
                }

                if (!entityLabel.equals("O")) {


                    StringBuilder phrase = new StringBuilder();
                    boolean first = true;
                    for (int i = start; i < end; i++) {
                        CoreLabel token = tokens.get(i);
                        if (!first) {
                            phrase.append(" ");
                        }
                        first = false;
                        phrase.append(token.word());
                    }


                    String link = linkFor(document, phrase.toString());

                    for (int i = start; i < end; i++) {
                        CoreLabel token = tokens.get(i);
                        token.set(EntityKbIdAnnotation.class, link);
                    }

                }

                start = end;
            }


//            CoreLabel nextToken = tokens.(();


//            
//            while (tokenIt.hasNext()) {
//
//                List<CoreLabel> sameClassTokens = Lists.newArrayList();
//                sameClassTokens.add(tokenIt.next());
//
//                final String entityLabel = sameClassTokens.get(0).get(
//                        NamedEntityTagAnnotation.class);
//
//                
//                
//                
//
//            }
//
//
//            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                String link = linkFor(document, token);
//
//                token.set(EntityKbIdAnnotation.class, link);
//
//            }

        }
    }

    String linkFor(Annotation annotation, String token) {
//


        List<String> candidates;
        try {
            candidates = knowledgeBase.search(token);


            // Simply return the first match
            return candidates.isEmpty() ? "NIL" : candidates.get(0);


        } catch (IOException ex) {
            Logger.getLogger(EntityLinkingAnnotator.class.getName()).log(Level.SEVERE, null, ex);
            return "NIL";
        }


    }

    public static final class EntityKbIdAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    public static class Factory implements edu.stanford.nlp.util.Factory<Annotator>, Serializable {

        private static final long serialVersionUID = 1L;

        public Annotator create() {
            try {
                return EntityLinkingAnnotator.newInstance();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
