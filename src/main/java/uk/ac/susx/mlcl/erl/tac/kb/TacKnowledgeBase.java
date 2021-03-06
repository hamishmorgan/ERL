/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.tac.kb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.annotation.Nullable;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import uk.ac.susx.mlcl.erl.tac.io.Tac2009KnowledgeBaseIO;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import static java.text.MessageFormat.format;

/**
 * A simple implementation of a database for storing and searching the Tac 2009 Knowledge Base.
 *
 * @author Hamish Morgan
 */
public class TacKnowledgeBase extends AbstractCollection<Entity> implements Closeable {

    private static final Log LOG = LogFactory.getLog(TacKnowledgeBase.class);
    private final DB database;
    private final HTreeMap<String, Entity> idIndex;
    private final HTreeMap<String, String> nameIndex;

    /**
     * Dependency injection constructor. Use {@link TacKnowledgeBase#open(java.io.File)} )} instead.
     *
     * @param database  The database connection object, used for closing
     * @param idIndex   Index of entity id's to entity objects
     * @param nameIndex Index of entity names to entity objects.
     */
    private TacKnowledgeBase(DB database, HTreeMap<String, Entity> idIndex, HTreeMap<String, String> nameIndex) {
        this.database = database;
        this.idIndex = idIndex;
        this.nameIndex = nameIndex;
    }

    @Nonnull
    public static TacKnowledgeBase open(File dbFile) {
        LOG.info(format("Opening database: {0}", dbFile));
        final DB db = Tac2009KnowledgeBaseIO.openDB(dbFile);
        final HTreeMap<String, Entity> idIndex = db.getHashMap("entity-id-index");
        final HTreeMap<String, String> nameIndex = db.getHashMap("entity-name-index");
        return new TacKnowledgeBase(db, idIndex, nameIndex);
    }

    private void checkState() throws IOException {
        if (database.isClosed()) {
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

    @Nullable
    public String getTextForId(String id) throws IOException {
        return getEntityById(id).getWikiText().orNull();
    }

    public void close() {
        database.commit();
        database.close();
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

    @Nonnull
    @Override
    public Iterator<Entity> iterator() {
        return idIndex.values().iterator();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Operation \"contains()\" is unfeasably"
                + " slow, and has been disabled for your own good!");
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Operation \"toArray()\" is unfeasably"
                + " slow, and has been disabled for your own good!");
    }

    @Nonnull
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
