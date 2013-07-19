/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import uk.ac.susx.mlcl.erl.tac.io.Tac2009KnowledgeBaseIO;

import java.io.File;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author hiam20
 */
public class TacKnowledgeBase extends AbstractCollection<Entity> {

    private static final Logger LOG = Logger.getLogger(TacKnowledgeBase.class.getName());
    private final DB db;
    private final HTreeMap<String, Entity> idIndex;
    private final HTreeMap<String, String> nameIndex;

    private TacKnowledgeBase(DB db, HTreeMap<String, Entity> idIndex, HTreeMap<String, String> nameIndex) {
        this.db = db;
        this.idIndex = idIndex;
        this.nameIndex = nameIndex;
    }


    public static TacKnowledgeBase open(File dbFile) {
        final DB db = Tac2009KnowledgeBaseIO.openDB(dbFile);
        final HTreeMap<String, Entity> idIndex = db.getHashMap("entity-id-index");
        final HTreeMap<String, String> nameIndex = db.getHashMap("entity-name-index");
        return new TacKnowledgeBase(db, idIndex, nameIndex);
    }

    private void checkState() throws IOException {
        if (db.isClosed()) {
            throw new IOException("The database is closed.");
        }
    }

    public Entity getEntityById(String id) throws IOException {
        checkState();
        return idIndex.get(id);
    }

    public Entity getEntityByName(String name) throws IOException {
        checkState();
        return idIndex.get(getNameById(name));
    }

    public String getNameById(String name) throws IOException {
        checkState();
        return nameIndex.get(name);
    }

    public String getTextForId(String id) throws IOException {
        return getEntityById(id).getWikiText().orNull();
    }

    public void close() {
        db.commit();
        db.close();
    }

    @Override
    public int size() {
        return idIndex.size();
    }

    @Override
    public boolean isEmpty() {
        assert idIndex.size() == nameIndex.size();
//        assert idIndex.isEmpty() == nameIndex.isEmpty();
        // XXX: Work around to MapDB bug.
        return idIndex.size() == 0;
    }

    @Override
    public Iterator<Entity> iterator() {
        return idIndex.values().iterator();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Operation \"contains()\" is unfeasably"
                + " slow, and has been disabled for your own good!");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Operation \"toArray()\" is unfeasably"
                + " slow, and has been disabled for your own good!");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Operation \"toArray(T[] a)\" is unfeasably"
                + " slow, and has been disabled for your own good!");
    }

    @Override
    public boolean add(Entity e) {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Operation \"containsAll(Collection<?> c)\""
                + "  is unfeasably slow, and has been disabled for your own good!");
    }

    @Override
    public boolean addAll(Collection<? extends Entity> c) {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Collection is immutable!");
    }

}
