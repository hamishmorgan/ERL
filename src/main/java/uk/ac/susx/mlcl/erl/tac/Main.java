package uk.ac.susx.mlcl.erl.tac;

import com.google.common.collect.ImmutableList;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 16/07/2013
 * Time: 12:10
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static List<Query> readQueries(File queriesFile) throws ParsingException, IOException {

        Builder parser = new Builder();
        Document doc = parser.build(queriesFile);

        LOG.debug("doc={0}, baseURI={1}, docType={2}, rootElement={0}",
                doc, doc.getBaseURI(), doc.getDocType(), doc.getRootElement());


        Elements children = doc.getRootElement().getChildElements();
        ImmutableList.Builder<Query> queries = ImmutableList.builder();
        for (int i = 0; i < children.size(); i++) {
            final Element child = children.get(i);

            String id = child.getAttribute("id").getValue();
            String name = child.getFirstChildElement("name").getValue();
            String docId = child.getFirstChildElement("docid").getValue();


            Query query;
            if (child.getFirstChildElement("beg") != null) {
                int beg = Integer.parseInt(child.getFirstChildElement("beg").getValue());
                int end = Integer.parseInt(child.getFirstChildElement("end").getValue());
                query = new Query(id, name, docId, beg, end);
            } else {
                query = new Query(id, name, docId);
            }

            LOG.debug("Read query: {}", query);
            queries.add(query);
        }

        return queries.build();

    }

    public static void main(String[] args) {

        final File dir = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");

        final String[] queryFileNames = {"tac_2009_kbp_entity_linking_queries.xml",
                "tac_2010_kbp_evaluation_entity_linking_queries.xml",
                "tac_2012_kbp_english_evaluation_entity_linking_queries.xml"};


        for (String filename : queryFileNames) {
            final File queriesFile = new File(dir, filename);

            System.out.println(queriesFile);
            try {

                final List<Query> query = readQueries(queriesFile);

            } catch (ParsingException ex) {
                System.err.println("Cafe con Leche is malformed today. How embarrassing!");
            } catch (IOException ex) {
                System.err.println("Could not connect to Cafe con Leche. The site may be down.");
            }

        }

    }

}
