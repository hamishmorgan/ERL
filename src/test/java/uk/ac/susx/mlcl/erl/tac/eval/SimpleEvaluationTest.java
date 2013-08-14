package uk.ac.susx.mlcl.erl.tac.eval;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.lib.Comparators;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@RunWith(Parameterized.class)
public class SimpleEvaluationTest extends AbstractTest {
    private static final Log LOG = LogFactory.getLog(SimpleEvaluationTest.class);
    private final List<String> actual;
    private final List<String> predicted;

    public SimpleEvaluationTest(List<String> actual, List<String> predicted) {
        this.actual = actual;
        this.predicted = predicted;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        ImmutableList.Builder<Object[]> data = ImmutableList.builder();


        //    Predicted
        //    class
        //           Cat	Dog	Rabbit
        //    Actual class
        //      Cat	3	2	0
        //      Dog	1	2	1
        //      Rabbit	0	1	6
        String[][] temp = {
                {"Cat", "Cat"},
                {"Cat", "Cat"},
                {"Cat", "Cat"},
                {"Cat", "Dog"},
                {"Cat", "Dog"},
                {"Dog", "Cat"},
                {"Dog", "Dog"},
                {"Dog", "Dog"},
                {"Dog", "Rabbit"},
                {"Rabbit", "Dog"},
                {"Rabbit", "Rabbit"},
                {"Rabbit", "Rabbit"},
                {"Rabbit", "Rabbit"},
                {"Rabbit", "Rabbit"},
                {"Rabbit", "Rabbit"},
                {"Rabbit", "Rabbit"}
        };
        String[] actual = new String[temp.length];
        String[] predicted = new String[temp.length];
        for (int i = 0; i < temp.length; i++) {
            actual[i] = temp[i][0];
            predicted[i] = temp[i][1];
        }
        data.add(new Object[]{ImmutableList.copyOf(actual), ImmutableList.copyOf(predicted)});


        return data.build();
    }

    @Test
    public void testSimple() {
        Comparator<String> linkComparator = Comparators.natural();
        Function<String, String> labelFormatter = Functions.identity();
        Evaluation<String> evaluation = new Evaluation<String>(actual, predicted, linkComparator, labelFormatter);

        Assert.assertNotNull("evaluation", evaluation);
        LOG.info(evaluation.getResultsTable());
        LOG.info(evaluation.getConfusionMatrix().getTableString());
        LOG.info(evaluation.getConfusionMatrix().getStatsString());
    }

    @Test
    public void test_StringLengthEquality() {
        Comparator<String> linkComparator = Comparators.reverse(Comparators.mapped(new StringLengthFunction()));
        Function<String, String> labelFormatter = Functions.identity();
        Evaluation<String> evaluation = new Evaluation<String>(actual, predicted, linkComparator, labelFormatter);

        Assert.assertNotNull("evaluation", evaluation);
        LOG.info(evaluation.getResultsTable());
        LOG.info(evaluation.getConfusionMatrix().getTableString());
        LOG.info(evaluation.getConfusionMatrix().getStatsString());
    }

    private static class StringLengthFunction implements Function<String, Integer> {
        @Nullable
        @Override
        public Integer apply(@Nullable String input) {
            return input == null ? null : input.length();
        }
    }


//    /**
//     * Evaluate whole well the prediction performed in terms of entities found vs. not found (nil).
//     * <p/>
//     * In this method we set up the full evaluation as before, then map the confusion matrix to binary matrix.
//     */
//    @Test
//    public void testEntityVsNil_Method1() throws NoSuchMethodException {
//
//        final Comparator<Link> linkComparator = Comparators.mapped(Link.GetEntityNodeId.INSTANCE);
//        final Function<Link, String> labelFormatter = Link.GetEntityNodeId.INSTANCE;
//
//        final Evaluation evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
//        Assert.assertNotNull("evaluation", evaluation);
////        System.out.println(evaluation);
//
//        final ConfusionMatrix fullMatrix = evaluation.getConfusionMatrix();
//
////        System.out.println(fullMatrix.getAccuracy());
//
//        BinaryConfusionMatrix binMatrix = fullMatrix.mapAllVersus(new Predicate<Link>() {
//            @Override
//            public boolean apply(@Nullable Link input) {
//                return !input.getEntityNodeId().startsWith("NIL");
//            }
//        }, "Entity", "Nil", Reducers.Doubles.sum());
//
//        Assert.assertNotNull("binMatrix", binMatrix);
////        System.out.println(binMatrix.getStatsString());
//        binMatrix.appendStatsFor(binMatrix.getPositiveLabel(), System.out, Locale.getDefault());
//        binMatrix.appendStatsFor(binMatrix.getNegativeLabel(), System.out, Locale.getDefault());
//
////        Assert.fail();
//    }
//
//    /**
//     * Evaluate whole well the prediction performed in terms of entities found vs. not found (nil).
//     * <p/>
//     * In this method the binary matrix is computer directly from the evaluation result by supplying the
//     * appropriate comparator and formatter.
//     */
//    @Test
//    public void testEntVsNil_Method2() {
//
//        // Note the link comparator here performs two functions. First it matches entities and nils by stripping
//        // the numeric suffix. Secondly it insures that, when displaying results, Entity is the first (positive)
//        // element, and that NIL is the second (negative) element.
//        final Comparator<Link> linkComparator =
//                Comparators.mapped(Link.GetEntityIdPrefix.INSTANCE,
//                        Comparators.mapped(ImmutableMap.of(
//                                Link.GetEntityIdPrefix.NIL_PREFIX_RESULT, 1,
//                                Link.GetEntityIdPrefix.ENTITY_PREFIX_RESULT, 0)));
//
////
////        new Comparator<Link>() {
////
////            @Override
////            public int compare(Link o1, Link o2) {
////                return Integer.compare(
////                        o1.getEntityNodeId().startsWith("NIL") ? 1 : 0,
////                        o2.getEntityNodeId().startsWith("NIL") ? 1 : 0);
////            }
////        };
//
//
//        final Function<Link, String> labelFormatter = Link.GetEntityIdPrefix.INSTANCE;
//
//        Evaluation evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
//        Assert.assertNotNull("evaluation", evaluation);
////        System.out.println(evaluation);
//
//        ConfusionMatrix mat = evaluation.getConfusionMatrix();
//        Assert.assertNotNull("mat", mat);
//        Assert.assertEquals(BinaryConfusionMatrix.class, mat.getClass());
//        System.out.println(mat);
//
//
////        Assert.fail();
//    }
//
//    /**
//     * Evaluate entity type predictions (PER,GPE,ORG,UKN) against each other.
//     */
//    @Test
//    public void testEntTypes() {
//
//        final Comparator<Link> linkComparator = Comparators.mapped(Link.GetEntityType.INSTANCE);
//        final Function<Link, String> labelFormatter = Link.GetEntityTypeString.INSTANCE;
//
//        final Evaluation<Link> evaluation = new Evaluation(actual, predicted, linkComparator, labelFormatter);
//
//        ConfusionMatrix<Link> fourWayMatrix = evaluation.getConfusionMatrix();
//        Assert.assertEquals(4, fourWayMatrix.getLabels().size());
//
//        fourWayMatrix.appendTable(System.out, Locale.getDefault());
//        fourWayMatrix.appendStats(System.out, Locale.getDefault());
//
//        // For each entity type we can also create a binary confusion matrix show the all-vs-one scores.
//        for (final Link label : fourWayMatrix.getLabels()) {
//            ConfusionMatrix twoWayMatrix = fourWayMatrix.mapAllVersus(new Predicate<Link>() {
//                @Override
//                public boolean apply(@Nullable Link input) {
//                    return input.getEntityType().equals(label.getEntityType());
//                }
//            }, label.getEntityType().name(), "Other", Reducers.Doubles.sum());
//
//            System.out.println(label.getEntityType().name());
//            twoWayMatrix.appendTable(System.out, Locale.getDefault());
//            twoWayMatrix.appendStats(System.out, Locale.getDefault());
//        }
//
//
////        Assert.fail();
//    }
//

}
