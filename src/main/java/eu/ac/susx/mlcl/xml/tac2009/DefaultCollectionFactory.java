/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.xml.tac2009;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DefaultCollectionFactory allows the instantiation of common collection types in an implementation
 * agnostic manner. 
 * 
 * The idea is that a builder could create a complex object, using in-memory collections or the data
 * could be stored on disk (using MapDB for e.g.)
 * 
 * This is a work in progress. We are going to need to add a few things:
 * 
 *  * A way of identifying collections uniquely.
 *  * Support for re-opening, flushing and closing
 * 
 * @author hiam20
 */
@Beta
public class DefaultCollectionFactory {

    public <K, V> Map<K, V> newHashMap() {
        return Maps.newHashMap();
    }

    public <T> Set<T> newHashSet() {
        return Sets.newHashSet();
    }

    public <T> List<T> newArrayList() {
        return Lists.newArrayList();
    }
    
}
