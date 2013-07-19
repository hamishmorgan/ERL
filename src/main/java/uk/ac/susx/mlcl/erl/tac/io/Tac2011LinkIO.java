package uk.ac.susx.mlcl.erl.tac.io;

import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.queries.Link;

/**
 * Base class for reading and writing entity links tabular files, following the TAC 2011 specification.
 *
 * @author Hamish Morgan
 */

public class Tac2011LinkIO extends Tac2010LinkIO {

    @Override
    String[] formatLink(final Link link) {
        final String[] values = super.formatLink(link);

        // swap webSearch (3) and genre (4)
        final String tmp = values[3];
        values[3] = values[4];
        values[4] = tmp;

        return values;
    }

    @Override
    String formatGenre(Genre genre) {
        return genre.name();
    }

    @Override
    boolean parseWebSearch(String[] values) {
        return parseBoolean(values[4]);
    }

    @Override
    Genre parseGenre(String[] values) {
        return Genre.valueOf(values[3]);
    }

}
