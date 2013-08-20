package uk.ac.susx.mlcl.erl.tac.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableList;
import uk.ac.susx.mlcl.erl.tac.eval.EvaluationComparaison;
import uk.ac.susx.mlcl.erl.tac.io.TacOutputIO;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 20/08/2013
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */
public class ELScorer implements Runnable {

    /**
     *
     */
    @Parameter(names = {"-g", "--gold-standard-file"}, required = true,
            description = "Ground truth for the test data.")
    private File goldStandardFile;
    /**
     *
     */
    @Parameter(names = {"-f", "--focus_gold_standard_file"},
            description = "same as gold_standard_file, but containing linkings only for queries over which " +
                    "query-level precision and recall should be averaged to compute F1")
    private File focusGoldStandardFile = null;
    /**
     *
     */
    @Parameter(names = {"-s", "--system-output-dir"}, required = true,
            description = "Directory with one or more system outputs for the test data following the KBP format.")
    private File systemOutputDir;
    /**
     *
     */
    @Parameter(names = "--help", help = true)
    private boolean help;
    /**
     *
     */
    @Parameter(names = "--charset")
    private Charset charset = Charset.forName("UTF-8");

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        ELScorer instance = new ELScorer();

        final JCommander jc = new JCommander();
        jc.setProgramName("elscorer");
        jc.addObject(instance);

        try {

            jc.parse(args);

            if (instance.focusGoldStandardFile == null)
                instance.focusGoldStandardFile = instance.goldStandardFile;

            if (instance.help) {
                jc.usage();
            } else {
                instance.run();
            }

        } catch (ParameterException ex) {

            System.err.println(ex.getLocalizedMessage());

            StringBuilder builder = new StringBuilder();
            jc.usage(builder);
            System.err.println(builder);

        }

    }

    /**
     *
     */
    @Override
    public void run() {

        try {

            TacOutputIO io = new TacOutputIO(charset);

            OutputSet gold = io.readAll(goldStandardFile);
            OutputSet focus = io.readAll(focusGoldStandardFile);

            ImmutableList.Builder<OutputSet> systems = ImmutableList.builder();

            for (File sysFile : systemOutputDir.listFiles(new VisibleFilenameFilter())) {
                systems.add(io.readAll(sysFile));
            }

            EvaluationComparaison eval = new EvaluationComparaison(gold, focus, systems.build());
            eval.appendEvalationTable(System.out);

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private static class VisibleFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return !name.startsWith(".") && name.endsWith(".el");
        }
    }


}
