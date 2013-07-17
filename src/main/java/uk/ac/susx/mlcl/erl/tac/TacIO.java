package uk.ac.susx.mlcl.erl.tac;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableList;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.text.MessageFormat.format;

/**
 * Static utility library that provides functions for reading and writing tac data formats.
 */
public class TacIO {

    private static final Logger LOG = LoggerFactory.getLogger(TacIO.class);

    public static QueryIO detectFormatFromQueries(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries file: {}", queriesFile);
        Builder parser = new Builder();
        Document doc = parser.build(queriesFile);
        final Element child = doc.getRootElement().getFirstChildElement("query");

        final QueryIO format;
        if (child.getFirstChildElement("beg") != null) {
            format = new Tac2012QueryIO();
        } else {
            final String id = child.getAttribute("id").getValue();
            if (id.matches("^EL[\\d]{5,6}$"))
                format = new Tac2010QueryIO();
            else
                format = new Tac2009QueryIO();
        }

        LOG.debug("Detected format: {}", format);
        return format;
    }

    public static LinkIO detectFormatFromLinks(final File linksFile) throws IOException {
        LOG.debug("Detecting format from links file: {}", linksFile);

        final CSVReader reader = new CSVReader(new FileReader(linksFile), '\t');
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

    private static final boolean parseBoolean(final String string) {
        checkNotNull(string, "s");
        final String s = string.trim().toLowerCase();
        if (s.equals("true") || s.equals("yes") || s.equals("1"))
            return true;
        if (s.equals("false") || s.equals("no") || s.equals("0"))
            return false;
        else
            throw new NumberFormatException(format("Expected a truthy value (e.g \"true\" or \"yes\"), but found {0}", string));
    }

    interface QueryIO {

        List<Query> readAll(File queriesFile) throws ParsingException, IOException;

        void writeAll(File queriesFile, List<Query> queries);

    }

    interface LinkIO {

        List<Link> readAll(File linksFile) throws ParsingException, IOException;

        void writeAll(File linksFile, List<Link> links);

    }

    static class Tac2009QueryIO implements QueryIO {

        static final String QUERY_ID_ATTR_NAME = "id";
        static final String QUERY_NAME_ELEM_NAME = "name";
        static final String DOC_ID_ELEM_NAME = "docid";

        @Override
        public List<Query> readAll(File queriesFile) throws ParsingException, IOException {
            LOG.debug("Reading queries file: {}", queriesFile);
            Builder parser = new Builder();
            Document doc = parser.build(queriesFile);

            LOG.debug("doc={0}, baseURI={1}, docType={2}, rootElement={0}",
                    doc, doc.getBaseURI(), doc.getDocType(), doc.getRootElement());


            Elements children = doc.getRootElement().getChildElements();
            ImmutableList.Builder<Query> queries = ImmutableList.builder();
            for (int i = 0; i < children.size(); i++) {
                final Element child = children.get(i);
                final Query query = parseQuery(child);
                LOG.debug("Read query: {}", query);
                queries.add(query);
            }

            return queries.build();
        }

        Query parseQuery(Element queryElement) {
            final String id = queryElement.getAttribute(QUERY_ID_ATTR_NAME).getValue();
            final String name = queryElement.getFirstChildElement(QUERY_NAME_ELEM_NAME).getValue();
            final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
            return new Query(id, name, docId);
        }

        @Override
        public void writeAll(File file, List<Query> queries) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    static class Tac2010QueryIO extends Tac2009QueryIO {


    }

    static class Tac2012QueryIO extends Tac2009QueryIO {

        static final String BEGIN_ELEM_NAME = "beg";
        static final String END_ELEM_NAME = "end";

        @Override
        Query parseQuery(Element queryElement) {
            final String id = queryElement.getAttribute(QUERY_ID_ATTR_NAME).getValue();
            final String name = queryElement.getFirstChildElement(QUERY_NAME_ELEM_NAME).getValue();
            final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
            final int beg = Integer.parseInt(queryElement.getFirstChildElement(BEGIN_ELEM_NAME).getValue());
            final int end = Integer.parseInt(queryElement.getFirstChildElement(END_ELEM_NAME).getValue());
            return new Query(id, name, docId, beg, end);
        }
    }

    static class Tac2009LinkIO implements LinkIO {

        @Override
        public List<Link> readAll(File linksFile) throws ParsingException, IOException {
            LOG.debug("Reading links file: {}", linksFile);

            final CSVReader reader = new CSVReader(new FileReader(linksFile), '\t');
            String[] values;
            final ImmutableList.Builder<Link> links = ImmutableList.<Link>builder();
            while ((values = reader.readNext()) != null) {
                final Link link = parseLink(values);
                LOG.debug("Read link: {}", link);
                links.add(link);
            }
            return links.build();
        }

        Link parseLink(String[] values) {
            assert values.length == 3;

            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            return new Link(queryId, kbId, entityType);
        }

        @Override
        public void writeAll(File linksFile, List<Link> links) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    static class Tac2010LinkIO extends Tac2009LinkIO {
        @Override
        public Link parseLink(String[] values) {
            assert values.length == 5;

            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            final boolean webUsed = parseBoolean(values[3]);
            final Genre genre = Genre.valueOfAlias(values[4]);
            return new Link(queryId, kbId, entityType, webUsed, genre);
        }
    }

    static class Tac2012LinkIO extends Tac2009LinkIO {
        @Override
        public Link parseLink(String[] values) {
            assert values.length == 5;

            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            final Genre genre = Genre.valueOfAlias(values[3]);
            final boolean webUsed = parseBoolean(values[4]);
            return new Link(queryId, kbId, entityType, webUsed, genre);

        }
    }

}
