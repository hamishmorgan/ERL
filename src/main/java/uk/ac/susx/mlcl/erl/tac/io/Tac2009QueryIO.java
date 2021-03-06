package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import nu.xom.*;
import org.eclipse.jetty.io.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.queries.Query;
import uk.ac.susx.mlcl.lib.xml.XomB;
import uk.ac.susx.mlcl.lib.xml.XomUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;

/**
 * Class for reading and writing to Query XML files following TAC 2009 specification.
 *
 * @author Hamish Morgan
 */
public class Tac2009QueryIO extends QueryIO {

    static final String ROOT_ELEM_NAME = "kbpentlink";
    static final String QUERY_ELEM_NAME = "query";
    static final String ID_ATTR_NAME = "id";
    static final String NAME_ELEM_NAME = "name";
    static final String DOC_ID_ELEM_NAME = "docid";
    private static final Logger LOG = LoggerFactory.getLogger(Tac2009QueryIO.class);

    @Override
    public List<Query> readAll(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Reading queries from file: {}", queriesFile);
        final Builder parser = new Builder();
        final Document doc = parser.build(queriesFile);
        return readAll(doc);
    }

    @Override
    public List<Query> readAll(@Nonnull URL url) throws ParsingException, IOException {
        LOG.debug("Reading queries from url: {}", url);
        final Builder parser = new Builder();
        final Closer closer = Closer.create();
        try {
            final Document doc = parser.build(
                    closer.register(new BufferedInputStream(
                            closer.register(url.openStream()))));
            return readAll(doc);
        } catch (ParsingException e) {
            throw closer.rethrow(e, ParsingException.class);
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    @Override
    public List<Query> readAll(Reader queriesReader) throws ParsingException, IOException {
        final Builder parser = new Builder();
        final Document doc = parser.build(queriesReader);
        return readAll(doc);
    }

    List<Query> readAll(@Nonnull Document doc) {
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

    @Nonnull
    Query parseQuery(@Nonnull Element queryElement) {
        final String id = queryElement.getAttribute(ID_ATTR_NAME).getValue();
        final String name = queryElement.getFirstChildElement(NAME_ELEM_NAME).getValue();
        final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
        return new Query(id, name, docId);
    }

    Document toXmlDocument(@Nonnull List<Query> queries) {
        final XomB x = new XomB();
        final XomB.ElementBuilder root = x.element(ROOT_ELEM_NAME);
        for (Query query : queries) {
            root.add(formatQuery(x, query));
        }
        return x.document().setRoot(root).build();
    }

    @Override
    public void writeAll(File file, @Nonnull List<Query> queries) throws IOException {
        LOG.debug(MessageFormat.format("Writing queries to file: {0}", file));
        final Closer closer = Closer.create();
        try {
            XomUtil.writeDocument(
                    toXmlDocument(queries),
                    closer.register(new BufferedOutputStream(
                            closer.register(new FileOutputStream(file)))),
                    Charset.forName("UTF-8"));
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    @Override
    public void writeAll(@Nonnull URL url, @Nonnull List<Query> queries) throws IOException, URISyntaxException {
        LOG.debug(MessageFormat.format("Writing queries to url: {0}", url));
        if (url.getProtocol().equalsIgnoreCase("file")) {
            writeAll(new File(url.toURI()), queries);
        } else {
            final Closer closer = Closer.create();
            try {
                final URLConnection con = url.openConnection();
                con.setDoOutput(true);
                XomUtil.writeDocument(
                        toXmlDocument(queries),
                        closer.register(new BufferedOutputStream(
                                closer.register(con.getOutputStream()))),
                        Charset.forName("UTF-8"));
            } catch (Throwable t) {
                throw closer.rethrow(t);
            } finally {
                closer.close();
            }
        }
    }

    @Override
    public void writeAll(Writer queriesWriter, @Nonnull List<Query> queries) throws IOException {
        final OutputStream out = new WriterOutputStream(queriesWriter, "UTF-8");
        try {
            XomUtil.writeDocument(toXmlDocument(queries), out, Charset.forName("UTF-8"));
        } finally {
            out.flush();
        }
    }

    @Nonnull
    XomB.ElementBuilder formatQuery(@Nonnull XomB x, @Nonnull Query query) {
        return x.element(QUERY_ELEM_NAME)
                .addAttribute(ID_ATTR_NAME, query.getId())
                .add(x.element(NAME_ELEM_NAME).add(query.getName()))
                .add(x.element(DOC_ID_ELEM_NAME).add(query.getDocId()));
    }


}
