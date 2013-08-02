package uk.ac.susx.mlcl.erl.tac.io;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.queries.Link;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Base class for reading and writing to entity links tabular files.
 *
 * @author Hamish Morgan
 */
public abstract class LinkIO implements BaseIO<Link> {

    static final char CSV_SEPARATOR = '\t';
    static final char CSV_QUOTE_CHAR = CSVWriter.NO_QUOTE_CHARACTER;
    static final char CSV_ESCAPE_CHAR = CSVWriter.NO_ESCAPE_CHARACTER;
    static final String CSV_LINE_END = System.getProperty("line.separator");
    static final int CSV_SKIP_LINES = 0;
    static final Logger LOG = LoggerFactory.getLogger(LinkIO.class);
    static final Pattern TAC2009_QID_PATTERN = Pattern.compile("^EL\\d{1,4}$");
    static final Pattern TAC2010_QID_PATTERN = Pattern.compile("^EL\\d{5,6}$");
    static final Pattern TAC2011_QID_PATTERN = Pattern.compile("^EL_\\d{5}$");
    static final Pattern TAC2012_QID_PATTERN = Pattern.compile("^EL_ENG_\\d{5}$");

    public static LinkIO detectFormat(final URL linksUrl) throws IOException {
        LOG.debug("Detecting format from links url: {}", linksUrl);

        final Closer closer = Closer.create();
        try {
            return detectFormat(
                    closer.register(new BufferedReader(
                            closer.register(new InputStreamReader(
                                    closer.register(linksUrl.openStream()))))));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static LinkIO detectFormat(final File linksFile) throws IOException {
        LOG.debug("Detecting format from links file: {}", linksFile);

        final Closer closer = Closer.create();
        try {
            return detectFormat(
                    closer.register(new BufferedReader(
                            closer.register(new FileReader(linksFile)))));
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static LinkIO detectFormat(final Reader linksReader) throws IOException {

        final CSVReader reader = new CSVReader(linksReader,
                CSV_SEPARATOR, CSV_QUOTE_CHAR, CSV_ESCAPE_CHAR, CSV_SKIP_LINES);
        final String[] values = reader.readNext();

        final LinkIO format;
        if (values.length == 3) {
            // TAC-KBP 2009 has three values per column
            // but so does 2010 training
            if (TAC2009_QID_PATTERN.matcher(values[0]).matches())
                format = new Tac2009LinkIO();
            else if (TAC2010_QID_PATTERN.matcher(values[0]).matches())
                format = new Tac2010GoldLinkIO();
            else throw new AssertionError("Expected query id to be 2009/10 format, but found: " + values[0]);
        } else if (values.length == 5) {
            // TAC-KBP 2010 onwards have five values per column

            // in 2011 they swapped the order of webUsed and genre
            if (values[4].equals("YES") || values[4].equals("NO")) {
                if (TAC2011_QID_PATTERN.matcher(values[0]).matches())
                    format = new Tac2011LinkIO();
                else if (TAC2012_QID_PATTERN.matcher(values[0]).matches())
                    format = new Tac2012LinkIO();
                else {
                    LOG.warn(MessageFormat.format(
                            "Query ID did not expected formats for 2011 ({0}) or 2012 ({1}); guessing 2012 anyway.",
                            TAC2011_QID_PATTERN, TAC2012_QID_PATTERN));
                    format = new Tac2012LinkIO();
                }
            } else if (values[3].equals("YES") || values[3].equals("NO")) {
                assert TAC2010_QID_PATTERN.matcher(values[0]).matches()
                        : "Expected query id to be 2010 format, but found: " + values[0];
                format = new Tac2010LinkIO();
            } else {
                throw new AssertionError("Expected either columns 4 or 5 to be web");
            }
        } else {
            throw new AssertionError("Expected exactly 3 or 5 links, but found " + values.length + ", in values "
                    + Arrays.toString(values));
        }

        LOG.debug("Detected format: {}", format);
        return format;

    }

}
