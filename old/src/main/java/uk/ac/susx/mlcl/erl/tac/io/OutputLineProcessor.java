package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import com.google.common.io.LineProcessor;
import uk.ac.susx.mlcl.erl.tac.queries.Output;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An <tt>OutputLineProcessor</tt> is used with streaming {@link com.google.common.io.Resources#readLines} method
 * to parse a a linking output file.
 * <p/>
 */
public final class OutputLineProcessor implements LineProcessor<OutputSet> {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    @Nonnull
    private final ImmutableList.Builder<Output> outputs = ImmutableList.builder();
    private final String name;

    OutputLineProcessor(final String name) {
        this.name = checkNotNull(name, "name");
    }

    /**
     * This method will be called once for each line.
     *
     * @param line the line read from the input, without delimiter
     * @return always true
     * @throws IllegalArgumentException if the line does not match the correct format
     * @throws NullPointerException     if the line is null
     */
    @Override
    public boolean processLine(@Nonnull final String line) throws IOException {
        final String[] parts = WHITESPACE_PATTERN.split(line.trim());
        if (parts.length < 2 || parts.length > 3)
            throw new IllegalArgumentException(String.format(
                    "Expected either 2 or 3 parts, but found %d in line \"%s\"", parts.length, line));
        final String mentionId = parts[0];
        final String kbId = parts[1];
        final double confidence = parts.length == 3 ? Double.parseDouble(parts[2]) : 1.0;
        outputs.add(new Output(mentionId, kbId, confidence));
        return true;
    }

    /**
     * @return result of processing all the lines.
     */
    @Override
    @CheckReturnValue
    @Nonnull
    public OutputSet getResult() {
        return OutputSet.newInstance(name, outputs.build());
    }
}
