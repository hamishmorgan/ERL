package uk.ac.susx.mlcl.erl.reduce;

import javax.annotation.Nonnull;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 10:26
 * To change this template use File | Settings | File Templates.
 */
public interface Reducer<A, T> {
    @Nonnull
    public A foldIn(A accum, T next);
}