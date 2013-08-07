package uk.ac.susx.mlcl.erl.test;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Iterator;

/**
 * Tests if iterator is empty.
 */
public class IsEmptyIterator<E> extends TypeSafeMatcher<Iterator<? extends E>> {
    /**
     * Creates a matcher for {@link Iterable}s matching examined iterator that yield no items.
     * <p/>
     * For example:
     * <pre>assertThat(new ArrayList&lt;String&gt;().iterator(), is(emptyIterator()))</pre>
     */
    @Factory
    public static <E> Matcher<Iterator<? extends E>> emptyIterator() {
        return new IsEmptyIterator<E>();
    }

    /**
     * Creates a matcher for {@link Iterable}s matching examined iterables that yield no items.
     * <p/>
     * For example:
     * <pre>assertThat(new ArrayList&lt;String&gt;(), is(emptyIterableOf(String.class)))</pre>
     *
     * @param type the type of the iterable's content
     */
    @Factory
    public static <E> Matcher<Iterator<E>> emptyIteratorOf(Class<E> type) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        final Matcher<Iterator<E>> result = (Matcher) emptyIterator();
        return result;
    }

    @Override
    public boolean matchesSafely(Iterator<? extends E> iterator) {
        return !iterator.hasNext();
    }

    @Override
    public void describeMismatchSafely(Iterator<? extends E> iterator, Description mismatchDescription) {
        mismatchDescription.appendValueList("[", ",", "]", new Object[]{iterator});
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an empty iterator");
    }
}
