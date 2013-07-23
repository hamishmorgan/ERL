package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.base.CharMatcher;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

import static com.google.common.base.CharMatcher.*;

/**
 * Parser that separate individual documents from the TAC 2013 source data.
 * <p/>
 * The source data is in a somewhat messy format;  almost fell formed XML documents are concatenated together into
 * a single long file without any root element. Consequently they will be rejected by any standard XML parser. This
 * class resolves that problems by split the document out from each file.
 *
 * @author Hamish Morgan
 */
class Tac2013DocumentCollectionParser {

    private final Tac2013DocumentHandler handler;
    private final Reader reader;
    private StringBuilder contentBuilder = new StringBuilder();
    private int character;
    private long offset = -1;

    Tac2013DocumentCollectionParser(Tac2013DocumentHandler handler, Reader reader) {
        this.handler = handler;
        this.reader = reader;
    }

    public void parse() throws IOException {
        advance();
        while (character != -1) {
            advanceWhile(WHITESPACE);
            parseDocument();
            advanceWhile(WHITESPACE);
        }
    }

    void advance() throws IOException {
        character = reader.read();
        ++offset;
        if (!isEOF()) {
            contentBuilder.append(getCharacter());
        }
    }

    boolean isEOF() {
        return character == -1;
    }

    char getCharacter() throws EOFException {
        if (isEOF())
            throw new EOFException();
        return (char) character;
    }

    long getOffset() {
        return offset;
    }

    void parseDocument() throws IOException {
        parseDocumentOpenTag();
        readUntilDocumentEnd();
    }

    void read(CharMatcher matcher) throws IOException {
        if (character(matcher))
            advance();
        else
            handler.error(MessageFormat.format("Expected character matching {0} at offset {1}, but found '{2}' ({3})",
                    matcher, offset, getCharacter(), (int) getCharacter()));
    }

    void advanceWhile(CharMatcher matcher) throws IOException {
        while (!isEOF() && character(matcher))
            advance();
    }

    boolean advanceIf(CharMatcher matcher) throws IOException {
        if (character(matcher)) {
            advance();
            return true;
        } else {
            return false;
        }
    }

    boolean character(CharMatcher matcher) throws IOException {
        return matcher.matches(getCharacter());
    }

    private void parseDocumentOpenTag() throws IOException {
        handler.documentStart(offset);
        read(is('<'));
        read(anyOf("Dd"));
        read(anyOf("Oo"));
        read(anyOf("Cc"));

        if (character(WHITESPACE))
            advanceWhile(isNot('>'));
        read(is('>'));
    }

    private void readUntilDocumentEnd() throws IOException {

        boolean documentEndFound = false;
        while (!documentEndFound) {

            advanceWhile(isNot('<'));

            if (advanceIf(is('<'))
                    && advanceIf(is('/'))
                    && advanceIf(anyOf("Dd"))
                    && advanceIf(anyOf("Oo"))
                    && advanceIf(anyOf("Cc"))
                    && advanceIf(is('>')))
                documentEndFound = true;

        }
        handler.documentEnd(offset, contentBuilder);
        contentBuilder = new StringBuilder();
    }


}
