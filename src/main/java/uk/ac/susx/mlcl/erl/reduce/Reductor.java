package uk.ac.susx.mlcl.erl.reduce;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 10:26
 * To change this template use File | Settings | File Templates.
 */
public class Reductor<A, T> {

    private Reducer<A, T> worker;

    public Reductor(Reducer<A, T> worker) {
        this.worker = worker;
    }

    public A fold(final A rval, final Iterator<T> itr) {
        A val = rval;
        while (itr.hasNext()) {
            val = worker.foldIn(val, itr.next());
        }
        return val;
    }
}