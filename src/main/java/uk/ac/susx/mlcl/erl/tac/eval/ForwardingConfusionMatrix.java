package uk.ac.susx.mlcl.erl.tac.eval;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* Created with IntelliJ IDEA.
* User: hamish
* Date: 08/08/2013
* Time: 11:57
* To change this template use File | Settings | File Templates.
*/
class ForwardingConfusionMatrix<T> extends ConfusionMatrix<T> {
    private final ConfusionMatrix<T> delegate;

    ForwardingConfusionMatrix(final ConfusionMatrix<T> delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    final ConfusionMatrix<T> getDelegate() {
        return delegate;
    }

    @Override
    public List<T> getLabels() {
        return delegate.getLabels();
    }

    public long getCount(T actual, T predicted) {
        return delegate.getCount(actual, predicted);
    }

    public String formatLabel(T label) {
        return delegate.formatLabel(label);
    }

}
