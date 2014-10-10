package uk.ac.susx.mlcl.erl.tac.eval;

import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;


public class SimpleClusterEvaluation {

    private static final String NIL_LINK_PREFIX = "NIL";
    @Nonnull
    private final OutputSet systemOutput;
    @Nonnull
    private final OutputSet goldStandard;
    @Nonnull
    private final Collection<String> focusMentions;

    public SimpleClusterEvaluation(
            @Nonnull final OutputSet systemOutput, @Nonnull final OutputSet goldStandard, @Nonnull final Collection<String> focus) {
        this.systemOutput = checkNotNull(systemOutput, "systemOutput");
        this.goldStandard = checkNotNull(goldStandard, "goldStandard");
        this.focusMentions = checkNotNull(focus, "focusMentions");
    }

    /**
     * Construct a new cluster evaluation where the focus mention ids are those in the gold standard output.
     *
     * @param systemOutput
     * @param goldStandard
     */
    public SimpleClusterEvaluation(@Nonnull final OutputSet systemOutput, @Nonnull final OutputSet goldStandard) {
        this(systemOutput, goldStandard, goldStandard.getMentionIds());
    }

    @Nonnull
    protected static String normaliseNil(@Nonnull final String kbId) {
        return kbId.startsWith(NIL_LINK_PREFIX) ? NIL_LINK_PREFIX : kbId;
    }

    @Nonnull
    public OutputSet getSystemOutput() {
        return systemOutput;
    }

    @Nonnull
    public OutputSet getGoldStandard() {
        return goldStandard;
    }

    @Nonnull
    public Collection<String> getFocusMentions() {
        return focusMentions;
    }

    @Nonnegative
    public final int getTrueCount() {
        int correctCount = 0;
        for (final String mentionId : focusMentions) {
            final String gold_kbid = normaliseNil(goldStandard.getKbIdForMention(mentionId));
            final String sys_kbid = normaliseNil(systemOutput.getKbIdForMention(mentionId));
            if (gold_kbid.equals(sys_kbid))
                ++correctCount;
        }
        return correctCount;
    }

    @Nonnegative
    public final int getTotalCount() {
        return focusMentions.size();
    }

    @Nonnegative
    public final int getFalseCount() {
        return getTotalCount() - getTrueCount();
    }

    @Nonnegative
    public final double getAccuracy() {
        return getTrueCount() / (double) getTotalCount();
    }

}
