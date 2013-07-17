package uk.ac.susx.mlcl.erl.tac.io;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.io.Closer;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.Link;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 17/07/2013
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class LinkIO {

    static final char CSV_SEPARATOR = '\t';
    static final char CSV_QUOTE_CHAR = CSVWriter.NO_QUOTE_CHARACTER;
    static final char CSV_ESCAPE_CHAR = CSVWriter.NO_ESCAPE_CHARACTER;
    static final String CSV_LINE_END = "\n";
    static final int CSV_SKIP_LINES = 0;
    private static final Logger LOG = LoggerFactory.getLogger(LinkIO.class);

    public static LinkIO detectFormat(final File linksFile) throws IOException {
        LOG.debug("Detecting format from links file: {}", linksFile);

        final Closer closer = Closer.create();
        try {
            final Reader reader =
                    closer.register(new BufferedReader(
                            closer.register(new FileReader(linksFile))));
            return detectFormat(new FileReader(linksFile));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static LinkIO detectFormat(final Reader linksReader) throws IOException {

        final CSVReader reader = new CSVReader(linksReader,
                CSV_SEPARATOR, CSV_QUOTE_CHAR, CSV_ESCAPE_CHAR, CSV_SKIP_LINES);
        String[] values = reader.readNext();

        final LinkIO format;
        if (values.length == 3) {
            // TAC-KBP 2009 has three values per column
            format = new Tac2009LinkIO();
        } else if (values.length == 5) {
            // TAC-KBP 2010/2012 has five values per column

            final Genre genre;
            final boolean webUsed;
            // in 2012 they swapped the order of webUsed and genre
            if (values[4].equals("YES") || values[4].equals("NO")) {
                format = new Tac2012LinkIO();
            } else if (values[3].equals("YES") || values[3].equals("NO")) {
                format = new Tac2010LinkIO();
            } else {
                throw new AssertionError("Expected either columns 4 or 5 to be web");
            }
        } else {
            throw new AssertionError("Expected exactly 3 or 5 links, but found " + values.length);
        }

        LOG.debug("Detected format: {}", format);
        return format;

    }

    public abstract List<Link> readAll(File linksFile) throws ParsingException, IOException;

    public abstract List<Link> readAll(Reader linkReader) throws ParsingException, IOException;

    public abstract void writeAll(File linksFile, List<Link> links) throws IOException;

    public abstract void writeAll(Writer linkWriter, List<Link> links) throws IOException;


}
