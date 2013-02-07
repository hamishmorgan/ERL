/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.t9kb;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang.time.StopWatch;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author hiam20
 */
public class T9KnowledgeBase {

    private final DB db;
    private final HTreeMap<String, T9Entity> idIndex;
    private final HTreeMap<String, String> nameIndex;

    private T9KnowledgeBase(DB db, HTreeMap<String, T9Entity> idIndex, HTreeMap<String, String> nameIndex) {
        this.db = db;
        this.idIndex = idIndex;
        this.nameIndex = nameIndex;
    }

    public T9Entity getEntityById(String id) {
        return idIndex.get(id);
    }

    public T9Entity getEntityByName(String name) {
        return idIndex.get(getNameById(name));
    }

    public String getNameById(String name) {
        return nameIndex.get(name);
    }

    public String getTextForId(String id) {
        return getEntityById(id).getWikiText().orNull();
    }

    public void close() {
        db.commit();
        db.close();
    }

    private static DB openDB(File dbFile) {
        return DBMaker.newFileDB(dbFile)
                .asyncWriteDisable()
                .journalDisable()
                .closeOnJvmShutdown()
                .make();
    }

    public static T9KnowledgeBase open(File dbFile) {
        final DB db = openDB(dbFile);
        final HTreeMap<String, T9Entity> idIndex = db.getHashMap("entity-id-index");
        final HTreeMap<String, String> nameIndex = db.getHashMap("entity-name-index");
        return new T9KnowledgeBase(db, idIndex, nameIndex);
    }

    public static T9KnowledgeBase create(File dbFile, File dataDir) throws ParserConfigurationException, SAXException, IOException {
        final DB db = openDB(dbFile);
        final HTreeMap<String, T9Entity> idIndex = db.createHashMap("entity-id-index", null, null);
        final HTreeMap<String, String> nameIndex = db.createHashMap("entity-name-index", null, null);
        final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxFactory.newSAXParser();
        final DefaultHandler handler = new T9SaxHandler(new TacEntryHandler() {
            private int count = 0;
            private StopWatch tic = new StopWatch();

            {
                tic.start();
            }

            @Override
            public void entry(T9Entity entry) {
                idIndex.put(entry.getId(), entry);
                nameIndex.put(entry.getName(), entry.getId());
                if (count % 1000 == 0) {
                    db.commit();
                }
                if (count % 10000 == 0) {
                    System.out.printf("Processed %d entities. (%f e/s)%n", count,
                                      count / ((tic.getTime() / 1000.0)));
                }
                count++;
            }
        });
        final String[] parts = dataDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("kb_part-\\d+\\.xml");
            }
        });
        for (String part : parts) {
            System.out.println("Processing file: " + part);
            saxParser.parse(new File(dataDir, part), handler);
        }
        db.commit();
        return new T9KnowledgeBase(db, idIndex, nameIndex);
    }
}
