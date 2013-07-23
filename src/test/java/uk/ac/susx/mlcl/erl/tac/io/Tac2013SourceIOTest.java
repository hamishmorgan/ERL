package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.io.Resources;
import nu.xom.ParsingException;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.ForumDocument;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Hamish Morgan
 */
public class Tac2013SourceIOTest extends AbstractTest {

    @Test
    public void testParseUncompressed() throws IOException, ParsingException {
        final URL url = getResource("bolt-eng-DF-215.sample");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(Resources.asByteSource(url));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

    @Test
    public void testParseCompressed() throws IOException, ParsingException {
        final URL url = getResource("bolt-eng-DF-215.sample.gz");
        final Tac2013ForumIO instance = new Tac2013ForumIO();

        final List<ForumDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Resources.asByteSource(url)));
        System.out.println(doc.get(0));
        assertNotNull(doc);
        assertEquals(55, doc.size());
    }

}
