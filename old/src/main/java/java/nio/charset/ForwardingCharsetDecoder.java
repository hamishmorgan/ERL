package java.nio.charset;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 01/08/2013
* Time: 11:20
* To change this template use File | Settings | File Templates.
*/
public abstract class ForwardingCharsetDecoder extends CharsetDecoder {

    @Nonnull
    private final CharsetDecoder delegate;

    public ForwardingCharsetDecoder(@Nonnull CharsetDecoder delegate) {
        super(delegate.charset(), delegate.averageCharsPerByte(), delegate.maxCharsPerByte());
        this.delegate = delegate;
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        return delegate.decodeLoop(in, out);
    }

    @Override
    public boolean isAutoDetecting() {
        return delegate.isAutoDetecting();
    }

    @Override
    public Charset detectedCharset() {
        return delegate.detectedCharset();
    }

    @Override
    public boolean isCharsetDetected() {
        return delegate.isCharsetDetected();
    }

    @Override
    protected void implReset() {
        delegate.implReset();
    }

    @Override
    protected CoderResult implFlush(CharBuffer out) {
       return  delegate.implFlush(out);
    }

    @Override
    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        delegate.implOnUnmappableCharacter(newAction);
    }

    @Override
    protected void implOnMalformedInput(CodingErrorAction newAction) {
        delegate.implOnMalformedInput(newAction);
    }

    @Override
    public CodingErrorAction unmappableCharacterAction() {
        return delegate.unmappableCharacterAction();
    }

    @Override
    protected void implReplaceWith(String newReplacement) {
        delegate.implReplaceWith(newReplacement);
    }

    @Override
    public CodingErrorAction malformedInputAction() {
        return delegate.malformedInputAction();
    }


}
