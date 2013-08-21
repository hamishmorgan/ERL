package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.tac.io.TacOutputIO;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class B3Exp {

    private static final Log LOG = LogFactory.getLog(B3Exp.class);
    private static final String goldStandardFilename = "gold_std.el";
    private static final String focusFilename = "gold_std.el";
    private static final String[] outputFilenames = {"all_in_one.el", "one_in_one.el", "perfect_output.el", "system_output_1.el"};
    private static final Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws URISyntaxException, IOException {

        TacOutputIO io = new TacOutputIO(charset);

        final OutputSet gold = io.readAll(Resources.getResource(B3Exp.class, goldStandardFilename));
        final OutputSet focus = io.readAll(Resources.getResource(B3Exp.class, focusFilename));

        System.out.printf("%d queries evaluated in focusMentions subset: %s%n", focus.getMentionCount(), focus.getName());
        System.out.printf("%d queries in gold standard environment: %s%n", gold.getMentionCount(), gold.getName());

        ImmutableList.Builder<OutputSet> systemOutputSets = ImmutableList.builder();
        for (String outputFilename : outputFilenames) {
            systemOutputSets.add(io.readAll(Resources.getResource(B3Exp.class, outputFilename)));
        }

        EvaluationComparaison evals = new EvaluationComparaison(gold, focus, systemOutputSets.build());
        evals.appendEvalationTable(System.out);
    }


}
