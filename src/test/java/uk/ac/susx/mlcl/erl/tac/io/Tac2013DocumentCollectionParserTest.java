package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.io.Closer;
import com.google.common.io.Resources;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 22/07/2013
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class Tac2013DocumentCollectionParserTest {


    @Test
    public void simpleTest() throws IOException {

        final URL url = Resources.getResource(this.getClass(), "bolt-eng-DF-215.sample");
        final Closer closer = Closer.create();
        try {

            final Reader reader = closer.register(new BufferedReader(
                    closer.register(new InputStreamReader(
                            closer.register(url.openStream())))));

            Tac2013DocumentHandler handler = new Tac2013DocumentHandler() {

                long lastStartOffset = -1;
                long lastEndOffset = -1;
                @Override
                public void documentStart(long offset) {
                    assertTrue("Expected positive offset but found " + offset, offset >= 0);
                    System.out.printf("start: %d%n", offset);
                    lastStartOffset = offset;
                }

                @Override
                public void documentEnd(long offset, CharSequence contents) {



                    System.out.println();
                    System.out.printf("%s%n", contents.length() < 100 ? contents
                            : contents.subSequence(0, 50) + " ... " + contents.subSequence(contents.length()-45, contents.length()));
                    System.out.println();
                    System.out.printf("end: %d%n", offset);
                    System.out.println();

                    lastEndOffset = offset;

                    assertTrue("Expected positive offset but found " + offset, offset >= 0);
                    assertEquals("Expected difference of offsets to equals content length.", offset - lastStartOffset, contents.length());
                }

                @Override
                public void error(String description) {
                    fail(description);
                }
            };

            Tac2013DocumentCollectionParser parser = new Tac2013DocumentCollectionParser(handler, reader);
            parser.parse();

        } catch (Throwable t) {
            closer.rethrow(t);
        } finally {
            closer.close();
        }

    }
}
