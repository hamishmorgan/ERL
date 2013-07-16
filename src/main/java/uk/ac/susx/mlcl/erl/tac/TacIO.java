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
 *
 */
public class TacIO {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final QueryParser TAC_2009_QUERY_PARSER = new QueryParser() {
        @Override
        public Query parse(Element queryElement) {
            final String id = queryElement.getAttribute("id").getValue();
            final String name = queryElement.getFirstChildElement("name").getValue();
            final String docId = queryElement.getFirstChildElement("docid").getValue();
            return new Query(id, name, docId);
        }
    };
    private static final QueryParser TAC_2012_QUERY_PARSER = new QueryParser() {
        @Override
        public Query parse(Element queryElement) {
            final String id = queryElement.getAttribute("id").getValue();
            final String name = queryElement.getFirstChildElement("name").getValue();
            final String docId = queryElement.getFirstChildElement("docid").getValue();
            final int beg = Integer.parseInt(queryElement.getFirstChildElement("beg").getValue());
            final int end = Integer.parseInt(queryElement.getFirstChildElement("end").getValue());
            return new Query(id, name, docId, beg, end);
        }
    };
    private static final LinkParser TAC2009_LINK_PARSER = new LinkParser() {
        @Override
        public Link parseLink(String[] values) {
            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            return new Link(queryId, kbId, entityType);
        }
    };
    private static final LinkParser TAC2010_LINK_PARSER = new LinkParser() {
        @Override
        public Link parseLink(String[] values) {
            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            final boolean webUsed = parseBoolean(values[3]);
            final Genre genre = Genre.valueOfAlias(values[4]);
            return new Link(queryId, kbId, entityType, webUsed, genre);
        }
    };
    private static final LinkParser TAC2012_LINK_PARSER = new LinkParser() {
        @Override
        public Link parseLink(String[] values) {
            final String queryId = values[0];
            final String kbId = values[1];
            final EntityType entityType = EntityType.valueOf(values[2]);
            final Genre genre = Genre.valueOfAlias(values[3]);
            final boolean webUsed = parseBoolean(values[4]);
            return new Link(queryId, kbId, entityType, webUsed, genre);

        }
    };

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

    public static Format detectFormatFromQueries(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries file: {}", queriesFile);
        Builder parser = new Builder();
        Document doc = parser.build(queriesFile);
        final Element child = doc.getRootElement().getFirstChildElement("query");

        final Format format;
        if (child.getFirstChildElement("beg") != null) {
            format = Format.TAC2012;
        } else {
            final String id = child.getAttribute("id").getValue();
            if (id.matches("^EL[\\d]{5,6}$"))
                format = Format.TAC2010;
            else
                format = Format.TAC2009;
        }

        LOG.debug("Detected format: {}", format);
        return format;
    }

    public static Format detectFormatFromLinks(final File linksFile) throws IOException {
        LOG.debug("Detecting format from links file: {}", linksFile);

        final CSVReader reader = new CSVReader(new FileReader(linksFile), '\t');
        String[] values = reader.readNext();

        final Format format;
        if (values.length == 3) {
            // TAC-KBP 2009 has three values per column
            format = Format.TAC2009;
        } else if (values.length == 5) {
            // TAC-KBP 2010/2012 has five values per column

            final Genre genre;
            final boolean webUsed;
            // in 2012 they swapped the order of webUsed and genre
            if (values[4].equals("YES") || values[4].equals("NO")) {
                format = Format.TAC2012;
            } else if (values[3].equals("YES") || values[3].equals("NO")) {
                format = Format.TAC2010;
            } else {
                throw new AssertionError("Expected either columns 4 or 5 to be web");
            }
        } else {
            throw new AssertionError("Expected exactly 3 or 5 links, but found " + values.length);
        }

        LOG.debug("Detected format: {}", format);
        return format;

    }


    public enum Format {
        DETECT(null, null) {
            @Override
            public List<Query> readQueries(File queriesFile) throws ParsingException, IOException {
                return detectFormatFromQueries(queriesFile).readQueries(queriesFile);
            }

            @Override
            public List<Link> readLinks(final File linksFile) throws IOException {
                return detectFormatFromLinks(linksFile).readLinks(linksFile);
            }
        },
        TAC2009(TAC_2009_QUERY_PARSER, TAC2009_LINK_PARSER) {},
        TAC2010(TAC_2009_QUERY_PARSER, TAC2010_LINK_PARSER) {},
        TAC2012(TAC_2012_QUERY_PARSER, TAC2012_LINK_PARSER) {};
        private final QueryParser queryParser;
        private final LinkParser linkParser;

        private Format(QueryParser queryParser, LinkParser linkParser) {
            this.queryParser = queryParser;
            this.linkParser = linkParser;
        }

        public List<Query> readQueries(File queriesFile) throws ParsingException, IOException {
            LOG.debug("Reading queries file: {}", queriesFile);
            Builder parser = new Builder();
            Document doc = parser.build(queriesFile);

            LOG.debug("doc={0}, baseURI={1}, docType={2}, rootElement={0}",
                    doc, doc.getBaseURI(), doc.getDocType(), doc.getRootElement());


            Elements children = doc.getRootElement().getChildElements();
            ImmutableList.Builder<Query> queries = ImmutableList.builder();
            for (int i = 0; i < children.size(); i++) {
                final Element child = children.get(i);
                final Query query = queryParser.parse(child);
                LOG.debug("Read query: {}", query);
                queries.add(query);
            }

            return queries.build();
        }

        public List<Link> readLinks(final File linksFile) throws IOException {
            LOG.debug("Reading links file: {}", linksFile);

            final CSVReader reader = new CSVReader(new FileReader(linksFile), '\t');
            String[] values;
            final ImmutableList.Builder<Link> links = ImmutableList.<Link>builder();
            while ((values = reader.readNext()) != null) {
                final Link link = linkParser.parseLink(values);
                LOG.debug("Read link: {}", link);
                links.add(link);
            }
            return links.build();
        }
    }


    private interface LinkParser {
        Link parseLink(String[] values);
    }


    private interface QueryParser {
        Query parse(Element queryElement);
    }


}
