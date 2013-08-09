package uk.ac.susx.mlcl.erl.tac.io;

import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.queries.Link;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.text.MessageFormat.format;

/**
 * Base class for reading and writing entity links tabular files, following the TAC 2010 specification.
 *
 * @author Hamish Morgan
 */

public class Tac2010LinkIO extends Tac2009LinkIO {

    static boolean parseBoolean(@Nonnull final String string) {
        checkNotNull(string, "s");
        final String s = string.trim().toLowerCase();
        if (s.equals("true") || s.equals("yes") || s.equals("1"))
            return true;
        if (s.equals("false") || s.equals("no") || s.equals("0"))
            return false;
        else
            throw new NumberFormatException(format("Expected a truthy value (e.g \"true\" or \"yes\"), but found {0}", string));
    }

    @Override
    boolean parseWebSearch(String[] values) {
        return parseBoolean(values[3]);
    }

    @Override
    Genre parseGenre(String[] values) {
        // 2010 used "WL" for web data instead of "WB"
        return values[4].equals("WL") ? Genre.WB : Genre.valueOf(values[4]);
    }

    @Override
    String[] formatLink(@Nonnull final Link link) {
        return new String[]{
                link.getQueryId(),
                link.getEntityNodeId(),
                link.getEntityType().name(),
                link.isWebSearch() ? "YES" : "NO",
                formatGenre(link.getSourceGenre())
        };
    }

    String formatGenre(@Nonnull Genre genre) {
        // 2010 used "WL" for web data instead of "WB"
        return genre == Genre.WB ? "WL" : genre.name();
    }
}
