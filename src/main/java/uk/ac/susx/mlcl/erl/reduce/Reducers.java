package uk.ac.susx.mlcl.erl.reduce;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 05/08/2013
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class Reducers {

    public static Reducer<Double, Double> sum() {
        return new Reducer<Double, Double>() {
            @Override
            public Double foldIn(Double accum, Double next) {
                return accum + next;
            }
        };
    }

}
