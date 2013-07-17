package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import nu.xom.*;
import org.eclipse.jetty.io.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.Query;
import uk.ac.susx.mlcl.erl.xml.XomB;
import uk.ac.susx.mlcl.erl.xml.XomUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 17/07/2013
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class Tac2009QueryIO extends QueryIO {

    static final String ROOT_ELEM_NAME = "kbpentlink";
    static final String QUERY_ELEM_NAME = "query";
    static final String QUERY_ID_ATTR_NAME = "id";
    static final String QUERY_NAME_ELEM_NAME = "name";
    static final String DOC_ID_ELEM_NAME = "docid";
    private static final Logger LOG = LoggerFactory.getLogger(Tac2009QueryIO.class);

    @Override
    public List<Query> readAll(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Reading queries file: {}", queriesFile);
        Builder parser = new Builder();
        Document doc = parser.build(queriesFile);
        return readAll(doc);
    }

    @Override
    public List<Query> readAll(Reader queriesReader) throws ParsingException, IOException {
        Builder parser = new Builder();
        Document doc = parser.build(queriesReader);
        return readAll(doc);
    }

    List<Query> readAll(Document doc) throws ParsingException, IOException {
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

    Document toXmlDocument(List<Query> queries) {
        final XomB x = new XomB();
        final XomB.ElementBuilder root = x.element(ROOT_ELEM_NAME);
        for (Query query : queries) {
            root.add(formatQuery(x, query));
        }
        Document doc = x.document().setRoot(root).build();
        return doc;
    }

    @Override
    public void writeAll(File file, List<Query> queries) throws IOException {
        XomUtil.writeDocument(
                toXmlDocument(queries),
                new FileOutputStream(file),
                Charset.forName("UTF-8"));
    }

    @Override
    public void writeAll(Writer queriesWriter, List<Query> queries) throws IOException {
        XomUtil.writeDocument(
                toXmlDocument(queries),
                new WriterOutputStream(queriesWriter, "UTF-8"),
                Charset.forName("UTF-8"));
    }

    XomB.ElementBuilder formatQuery(XomB x, Query query) {
        return x.element(QUERY_ELEM_NAME)
                .addAttribute(QUERY_ID_ATTR_NAME, query.getId())
                .add(x.element(QUERY_NAME_ELEM_NAME).add(query.getName()))
                .add(x.element(DOC_ID_ELEM_NAME).add(query.getDocId()));
    }


}
