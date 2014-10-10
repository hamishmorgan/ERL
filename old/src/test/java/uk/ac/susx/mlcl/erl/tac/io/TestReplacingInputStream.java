package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 25/07/2013
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */

@RunWith(Enclosed.class)
public class TestReplacingInputStream extends AbstractTest {


    @RunWith(Parameterized.class)
    public static class FileTestExamples2 extends AbstractTest {

        private final String resourceName;
        private final String find;
        private final String replace;
        private final String expectedResourceName;

        public FileTestExamples2(String resourceName, String find, String replace, String expectedResourceName) {
            this.resourceName = resourceName;
            this.find = find;
            this.replace = replace;
            this.expectedResourceName = expectedResourceName;
        }

        @Nonnull
        @Parameterized.Parameters(name = "{index}: {0}/{1}/{2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"eng-NG-ampersand-example.xml", " & ", " &amp; ", "eng-NG-ampersand-example-fixed.xml"},
            });
        }

        @Test
        public void testRead() throws Exception {
            final InputStream is = getResource(resourceName).openStream();
            final InputStream ris = new ReplacingInputStream(is, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int b;
            while (-1 != (b = ris.read()))
                bos.write(b);

            String expected = Resources.toString(getResource(expectedResourceName), Charset.forName("UTF-8"));
            String actual = new String(bos.toByteArray());

            assertEquals(expected, actual);
        }


    }

    @RunWith(Parameterized.class)
    public static class FileTestExamples1 extends AbstractTest {

        private final String resourceName;
        private final String find;
        private final String replace;

        public FileTestExamples1(String resourceName, String find, String replace) {
            this.resourceName = resourceName;
            this.find = find;
            this.replace = replace;
        }

        @Nonnull
        @Parameterized.Parameters(name = "{index}: {0}/{1}/{2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"eng-NG-31-9999", " & ", " &amp; "},
                    {"eng-WL-11-9923", " & ", " &amp; "},
                    {"eng-NG-ampersand-example.xml", " & ", " &amp; "},
                    {"eng-NG-31-9999", " & ", ""},
                    {"eng-WL-11-9923", " & ", ""},
                    {"eng-NG-ampersand-example.xml", " & ", ""},
            });
        }

        @Test
        public void testRead() throws Exception {
            final InputStream is = getResource(resourceName).openStream();
            final InputStream ris = new ReplacingInputStream(is, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int b;
            while (-1 != (b = ris.read()))
                bos.write(b);

            String actual = new String(bos.toByteArray());
            System.out.println(actual);

            assertFalse(actual.contains(find));
        }


    }

    @RunWith(Parameterized.class)
    public static class SimpleTestExamples extends AbstractTest {

        private final String input;
        private final String find;
        private final String replace;
        private final String expected;


        public SimpleTestExamples(String input, String find, String replace, String expected) {
            this.input = input;
            this.find = find;
            this.replace = replace;
            this.expected = expected;
        }

        @Nonnull
        @Parameterized.Parameters(name = "{index}: {0}/{1}/{2} -> {3}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            "hello xyz world.",
                            "xyz",
                            "abc",
                            "hello abc world.",
                    },
                    {
                            "hello & world.",
                            "& ",
                            "&amp; ",
                            "hello &amp; world.",
                    },
            });
        }

        @Test
        public void testRead() throws Exception {
            final ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes("UTF-8"));
            final InputStream ris = new ReplacingInputStream(bis, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int b;
            while (-1 != (b = ris.read()))
                bos.write(b);

            String actual = new String(bos.toByteArray());

            assertEquals(expected, actual);

        }

        @Test
        public void testRead_byteArr_int_int() throws Exception {
            final ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes("UTF-8"));
            final InputStream ris = new ReplacingInputStream(bis, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[8];
            int j = buffer.length;
            while ((j = ris.read(buffer, 0, j)) != -1)
                bos.write(buffer, 0, j);

            String actual = new String(bos.toByteArray());

            assertEquals(expected, actual);

        }

        @Test
        public void testRead_byteArr() throws Exception {
            final ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes("UTF-8"));
            final InputStream ris = new ReplacingInputStream(bis, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[8];
            int n;
            while ((n = ris.read(buffer)) != -1)
                bos.write(buffer, 0, n);

            String actual = new String(bos.toByteArray());

            assertEquals(expected, actual);
        }

        @Test
        public void testAvailable() throws Exception {
            final ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes("UTF-8"));
            final InputStream ris = new ReplacingInputStream(bis, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            boolean eof = false;
            while (!eof) {
                int available = ris.available();
                int b = ris.read();
                if (-1 == b) {
                    assertTrue("Expected at no bytes to be reported as available, but found " + available, available == 0);
                    eof = true;
                } else {
                    assertTrue("Expected at least one byte to be reported as available, but found " + available, available > 0);
                    bos.write(b);
                }
            }

            String actual = new String(bos.toByteArray());
            assertEquals(expected, actual);
        }

        @Test(expected = IOException.class)
        public void testClose() throws Exception {
            final InputStream delegate = new FilterInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))) {
                boolean closed = false;

                @Override
                public void close() throws IOException {
                    closed = true;
                    super.close();
                }

                @Override
                public int read() throws IOException {
                    if (closed)
                        throw new IOException("Stream Closed");
                    return super.read();    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
            final InputStream ris = new ReplacingInputStream(delegate, find.getBytes("UTF-8"), replace.getBytes("UTF-8"));

            ris.close();
            ris.read();
        }

    }

}
