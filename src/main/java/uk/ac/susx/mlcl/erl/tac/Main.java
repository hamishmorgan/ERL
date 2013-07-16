package uk.ac.susx.mlcl.erl.tac;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableList;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

        LOG.debug("Reading queries file: {}", queriesFile);
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

    public static List<Link> readLinks(File linksFile) throws IOException {
        LOG.debug("Reading links file: {}", linksFile);

        CSVReader reader = new CSVReader(new FileReader(linksFile), '\t');
        String[] nextLine;
        ImmutableList.Builder<Link> links = ImmutableList.<Link>builder();
        while ((nextLine = reader.readNext()) != null) {
            assert nextLine.length == 3;
            final String queryId = nextLine[0];
            final String kbId = nextLine[1];
            final EntityType entityType = EntityType.valueOf(nextLine[2]);
            final Link link = new Link(queryId, kbId, entityType);
            LOG.debug("Read link: {}", link);
            links.add(link);
        }
        return links.build();
    }

//    public static void main(String[] args) {
//
//        final File dir = new File("/Volumes/LocalScratchHD/LocalHome/Projects/NamedEntityLinking/Data");
//
//        final String[] queryFileNames = {"tac_2009_kbp_entity_linking_queries.xml",
//                "tac_2010_kbp_evaluation_entity_linking_queries.xml",
//                "tac_2012_kbp_english_evaluation_entity_linking_queries.xml"};
//
//
//        for (String filename : queryFileNames) {
//            final File queriesFile = new File(dir, filename);
//
//            System.out.println(queriesFile);
//            try {
//
//                final List<Query> query = readQueries(queriesFile);
//
//            } catch (ParsingException ex) {
//                System.err.println("Cafe con Leche is malformed today. How embarrassing!");
//            } catch (IOException ex) {
//                System.err.println("Could not connect to Cafe con Leche. The site may be down.");
//            }
//
//        }
//
//    }

}
