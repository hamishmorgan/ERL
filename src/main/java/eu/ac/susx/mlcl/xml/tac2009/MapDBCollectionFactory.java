/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.xml.tac2009;

import com.google.common.annotations.Beta;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 * Create various basic collections that back off to disk storage using MapDB.
 *
 * @author hiam20
 */
@Beta
public class MapDBCollectionFactory extends DefaultCollectionFactory {

    private final Random RAND = new Random();
    private final DB db;

    public MapDBCollectionFactory(File mapDbFile) {
        db = DBMaker.newFileDB(mapDbFile).journalDisable().make();
    }

    @Override
    public <K, V> Map<K, V> newHashMap() {
        HTreeMap<K, V> result = db.createHashMap(uniqueName(), null, null);
        return result;
    }

    @Override
    public <T> Set<T> newHashSet() {
        return db.createHashSet(uniqueName(), null);
    }

    @Override
    public <T> List<T> newArrayList() {
        return super.newArrayList();
    }

    private String uniqueName() {
        Set<String> names = db.getNameDir().keySet();
        String name;
        do {
            name = Long.toHexString(RAND.nextLong());
        } while (!names.contains(name));
        return name;
    }
}
