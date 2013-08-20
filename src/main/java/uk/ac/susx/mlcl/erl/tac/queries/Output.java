package uk.ac.susx.mlcl.erl.tac.queries;

import com.google.common.base.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An <tt>Output</tt> represents a single system output, as specified by the Tac2013 KBP guidelines.
 * <p/>
 * Each output consists of a <tt>mentionId</tt>, a  <tt>kbId</tt>, and an optional <tt>confidence</tt> value. The
 * <tt>mentionId</tt> is the <em>unique</em> identifier for particular mention query. The  <tt>kbId</tt> is the
 * unique identifier of a node in the knowledge base, if the mention was successfulling linked, or a NIL identifier
 * otherwise. The <tt>confidence</tt> denotes the systems sertainty that the link is correct with a score between
 * 0.0 and 1.0; where 0.0 indicate no-confidence, and 1.0 indicates total certainty.
 *
 * @author Hamish I A Morgan
 */
public final class Output {
    @Nonnull
    private final String mentionId;
    @Nonnull
    private final String kbId;
    @Nonnegative
    private final double confidence;

    public Output(@Nonnull final String mentionId, @Nonnull final String kbId, @Nonnegative final double confidence) {

        checkNotNull(mentionId, "mentionId");
        checkArgument(!mentionId.isEmpty(), "mentionId is an empty string");
        this.mentionId = mentionId;

        checkNotNull(kbId, "kbId");
        checkArgument(!kbId.isEmpty(), "kbId is an empty string");
        this.kbId = kbId;

        checkArgument(confidence >= 0.0 && confidence <= 1.0,
                "expected confidence in range 0.0 to 1.0; found " + confidence);
        this.confidence = confidence;
    }

    @Nonnull
    public final String getMentionId() {
        return mentionId;
    }

    @Nonnull
    public final String getKbId() {
        return kbId;
    }

    @Nonnegative
    public final double getConfidence() {
        return confidence;
    }

    @Override
    @Nonnull
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mentionId", mentionId)
                .add("kbId", kbId)
                .add("confidence", confidence)
                .toString();
    }
}
