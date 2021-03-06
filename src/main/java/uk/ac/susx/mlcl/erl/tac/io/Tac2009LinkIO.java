package uk.ac.susx.mlcl.erl.tac.io;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.kb.EntityType;
import uk.ac.susx.mlcl.erl.tac.Genre;
import uk.ac.susx.mlcl.erl.tac.queries.Link;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Base class for reading and writing entity links tabular files, following the TAC 2009 specification.
 *
 * @author Hamish Morgan
 */
public class Tac2009LinkIO extends LinkIO {

    private static final Logger LOG = LoggerFactory.getLogger(Tac2009LinkIO.class);

    @Override
    public List<Link> readAll(Reader linkReader) throws ParsingException, IOException {
        final CSVReader reader = new CSVReader(linkReader,
                CSV_SEPARATOR, CSV_QUOTE_CHAR, CSV_ESCAPE_CHAR, CSV_SKIP_LINES);
        String[] values;
        final ImmutableList.Builder<Link> links = ImmutableList.builder();
        while ((values = reader.readNext()) != null) {
            final Link link = new Link(
                    values[0],
                    values[1],
                    EntityType.valueOf(values[2]),
                    parseWebSearch(values),
                    parseGenre(values));
            LOG.debug("Read link: {}", link);
            links.add(link);
        }
        return links.build();
    }

    @Override
    public List<Link> readAll(File linksFile) throws ParsingException, IOException {
        LOG.debug("Reading links file: {}", linksFile);

        final Closer closer = Closer.create();
        try {
            final Reader reader =
                    closer.register(new BufferedReader(
                            closer.register(new FileReader(linksFile))));
            return readAll(reader);
        } catch (ParsingException e) {
            throw closer.rethrow(e, ParsingException.class);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Override
    public List<Link> readAll(@Nonnull URL linksUrl) throws ParsingException, IOException {
        LOG.debug("Reading links url: {}", linksUrl);

        final Closer closer = Closer.create();
        try {
            final Reader reader =
                    closer.register(new BufferedReader(
                            closer.register(new InputStreamReader(
                                    closer.register(linksUrl.openStream())))));
            return readAll(reader);
        } catch (ParsingException e) {
            throw closer.rethrow(e, ParsingException.class);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Override
    public void writeAll(Writer linksWriter, @Nonnull List<Link> links) throws IOException {
        final CSVWriter writer = new CSVWriter(linksWriter,
                CSV_SEPARATOR, CSV_QUOTE_CHAR, CSV_ESCAPE_CHAR, CSV_LINE_END);
        try {
            for (Link link : links) {
                LOG.debug("Writing link: {}", link);
                writer.writeNext(formatLink(link));
            }
        } finally {
            writer.flush();
        }
    }

    @Override
    public void writeAll(File linksFile, @Nonnull List<Link> links) throws IOException {
        LOG.debug("Writing links to file: {}", linksFile);
        final Closer closer = Closer.create();
        try {
            final Writer writer =
                    closer.register(new BufferedWriter(
                            closer.register(new FileWriter(linksFile))));
            writeAll(writer, links);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Override
    public void writeAll(@Nonnull URL linksUrl, @Nonnull List<Link> links) throws IOException, URISyntaxException {
        LOG.debug("Writing links to file: {}", linksUrl);
        if (linksUrl.getProtocol().equalsIgnoreCase("file")) {
            writeAll(new File(linksUrl.toURI()), links);
        } else {
            final Closer closer = Closer.create();
            try {
                URLConnection con = linksUrl.openConnection();
                con.setDoOutput(true);
                final Writer writer =
                        closer.register(new BufferedWriter(
                                closer.register(new OutputStreamWriter(
                                        closer.register(con.getOutputStream())))));
                writeAll(writer, links);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }
    }

    boolean parseWebSearch(String[] values) {
        return true;
    }

    Genre parseGenre(String[] values) {
        return Genre.NW;
    }

    String[] formatLink(@Nonnull Link link) {
        return new String[]{
                link.getQueryId(),
                link.getEntityNodeId(),
                link.getEntityType().name()
        };
    }
}
