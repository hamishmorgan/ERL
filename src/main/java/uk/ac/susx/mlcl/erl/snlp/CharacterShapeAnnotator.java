/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.snlp;

import com.google.common.base.Preconditions;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * An annotator that adds a character shape pattern layer to the pipeline. Character shape is a
 * normalization of characters based upon their type; for example numeric digits are replaced with a
 * <tt>'#'</tt> (hash) character, and upper case alphabetical letters are replaced with <tt>'A'</tt>
 * character. Each character is replaced individually to produce a new string of the same length; so
 * for example the string <tt>12 O'Clock</tt> becomes <tt>## A.Aaaaa</tt>.
 *
 * This annotator expects the Annotation to include the TokensAnnotation at the very least. If
 * LemmaAnnotation is present, and if {@link #setLemmaUsed(boolean)} is set <tt>true</tt>, the lemma
 * string will be used instead of the token.
 *
 * Instance of {@code CharacterShapeAnnotator} are <em>not</em> generally thread safe. However they
 * can be considered thread-safe if the configuration is not changed, since the configuration fields
 * are the only mutable state.
 *
 * @author Hamish Morgan
 */
@Nonnull
public class CharacterShapeAnnotator implements Annotator {

    /**
     * Common prefix to all properties accessed by this annotator.
     */
    private static final String PROPERTY_KEY_PREFIX = "charshape.";
    /*
     * The following define the replacement output characters for codepoints of various types.
     */
    private static final Character WHITESPACE = ' ';
    private static final Character CURRENCY = '$';
    private static final Character DIGIT = '#';
    private static final Character LOWERCASE_LETTER = 'a';
    private static final Character UPERCASE_LETTER = 'A';
    private static final Character PUNCTUATION = '.';
    private static final Character UNKNOWN = '?';
    /*
     * Instead of reading the shape from the token, use the lemma string.
     */
    private static final String LEMMA_USED_KEY = PROPERTY_KEY_PREFIX + "lemmaUsed";
    private static final boolean LEMMA_USED_DEFAULT_VALUE = false;
    private boolean lemmaUsed;

    /**
     * Construct a new {@code CharacterShapeAnnotator} with fields set to their default values.
     */
    public CharacterShapeAnnotator() {
        lemmaUsed = LEMMA_USED_DEFAULT_VALUE;
    }

    /**
     * Construct a new {@code CharacterShapeAnnotator} configuring it with the given properties
     * object.
     *
     * @param props configuration to load
     */
    public CharacterShapeAnnotator(Properties props) {
        this();
        configure(props);
    }

    /**
     * Configure (or re-configure) this object with the given properties.
     *
     * @param props configuration to load
     */
    public final void configure(Properties props) {
        if (props.containsKey(LEMMA_USED_KEY)) {
            lemmaUsed = Boolean.valueOf(props.getProperty(LEMMA_USED_KEY));
        }
    }

    /**
     * Get a collection of all the annotation types that are required to already have been produced
     * before this annotator is used.
     *
     * NB: If everything implemented this method it could be a relatively sane way of doing
     * dependence management, unless I'm missing something, which is more than likely.
     *
     * @return annotations that must be produced before this annotator is used.
     */
    public Set<Class<? extends CoreAnnotation>> getRequiredAnnotations() {
        if (!isLemmaUsed()) {
            return Collections.<Class<? extends CoreAnnotation>>singleton(TokensAnnotation.class);
        } else {
            final Set<Class<? extends CoreAnnotation>> requirements =
                    new HashSet<Class<? extends CoreAnnotation>>();
            requirements.add(TokensAnnotation.class);
            requirements.add(LemmaAnnotation.class);
            return requirements;
        }
    }

    /**
     * Set the lemmaUsed property. When true the shape will be derived from the lemma string instead
     * of the token string (in which case LemmaAnnotation must be present).
     *
     * @param lemmaUsed whether to use the lemma string instead of the token string.
     */
    public void setLemmaUsed(boolean lemmaUsed) {
        this.lemmaUsed = lemmaUsed;
    }

    /**
     * Get whether or not the lemma string should be used instead of the token string.
     *
     * @return true if the lemma string instead of the token string, false otherwise.
     */
    public boolean isLemmaUsed() {
        return lemmaUsed;
    }

    /**
     * Process the given {@code annotation }, adding a new layer of type
     * {@code CharacterShapeAnnotator.Annotation} that represents the character shape pattern.
     *
     * @param annotation 
     * @throws IllegalArgumentException when annotation is null or does not contain required keys
     */
    public void annotate(final edu.stanford.nlp.pipeline.Annotation annotation)
            throws IllegalArgumentException {
        Preconditions.checkNotNull(annotation, "annotation");
        Preconditions.checkArgument(annotation.containsKey(TokensAnnotation.class),
                "TokensAnnotation is not present.");
        Preconditions.checkArgument(!isLemmaUsed() || annotation.containsKey(LemmaAnnotation.class),
                "useLemma is true but LemmaAnnotation is not present.");

        for (CoreLabel token : annotation.get(TokensAnnotation.class)) {

            final String input = isLemmaUsed()
                    ? token.get(LemmaAnnotation.class)
                    : token.get(TextAnnotation.class);

            final StringBuilder output = new StringBuilder(input.length());

            int offset = 0;
            int codepoint = input.codePointAt(offset);
            while (offset < input.length()) {

                output.append(replacementForCodepoint(codepoint));
                
                offset += Character.charCount(codepoint);
                if (offset < input.length()) {
                    codepoint = input.codePointAt(offset);
                }
            }
            
            token.set(CharacterShapeAnnotator.Annotation.class, output.toString());
        }
    }

    /**
     * Get the replacement character for the given code-point.
     *
     * @param codepoint character to find a replacement for
     * @return replacement
     */
    private static char replacementForCodepoint(final int codepoint) {
        final char replacement;
        final int type = Character.getType(codepoint);
        switch (type) {
            case Character.CURRENCY_SYMBOL:
                replacement = CURRENCY;
                break;
            case Character.COMBINING_SPACING_MARK:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.SPACE_SEPARATOR:
            case Character.CONTROL: // includes line-feed
                replacement = WHITESPACE;
                break;
            case Character.DECIMAL_DIGIT_NUMBER:
            case Character.LETTER_NUMBER: // E.g Runic Arlaug
            case Character.OTHER_NUMBER: // E.g Superscript two
                replacement = DIGIT;
                break;
            case Character.LOWERCASE_LETTER:
            case Character.OTHER_LETTER:
            case Character.MODIFIER_LETTER:
                replacement = LOWERCASE_LETTER;
                break;
            case Character.UPPERCASE_LETTER:
            case Character.TITLECASE_LETTER:
                replacement = UPERCASE_LETTER;
                break;
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.ENCLOSING_MARK:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.FORMAT:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.NON_SPACING_MARK:
            case Character.OTHER_PUNCTUATION:
            case Character.OTHER_SYMBOL:
                replacement = PUNCTUATION;
                break;
            case Character.SURROGATE:
            case Character.PRIVATE_USE:
            case Character.UNASSIGNED:
                replacement = UNKNOWN;
            default:
                throw new AssertionError(MessageFormat.format(
                        "Unknown character type {0} for character '{1}' (cp: {2}).",
                        type, Character.toChars(codepoint), codepoint));
        }
        return replacement;
    }

    /**
     * Factory for {@code CharacterShapeAnnotator}, which is required by some components such as the
     * {@link edu.stanford.nlp.pipeline.AnnotatorPool}.
     */
    public static final class Factory implements edu.stanford.nlp.util.Factory<CharacterShapeAnnotator> {

        private final Properties props;

        public Factory() {
            this.props = new Properties();
        }

        public Factory(Properties props) {
            this.props = props;
        }

        public CharacterShapeAnnotator create() {
            final CharacterShapeAnnotator csa = new CharacterShapeAnnotator();
            csa.configure(props);
            return csa;
        }
    }

    /**
     * The CoreMap key for getting the character shape strings contained by an annotation.
     *
     * This key is typically set only on token annotations.
     */
    public static final class Annotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }
}
