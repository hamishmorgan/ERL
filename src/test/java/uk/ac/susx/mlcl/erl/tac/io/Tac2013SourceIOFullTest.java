package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import nu.xom.ParsingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;
import uk.ac.susx.mlcl.lib.IOUtils;
import uk.ac.susx.mlcl.erl.tac.source.WebDocument;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Hamish Morgan
 */
@RunWith(Parameterized.class)
public class Tac2013SourceIOFullTest extends AbstractTest {

    private static final File DATA_PATH = new File("/Volumes/LocalScratchHD/LocalHome/Data/TAC_2013_KBP Source Data (LDC2013E45)/data/English/web");
    private final File dataFile;

    public Tac2013SourceIOFullTest(File dataFile) {
        this.dataFile = dataFile;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        ImmutableList.Builder<Object[]> data = ImmutableList.builder();
        for(File in : DATA_PATH.listFiles())   {
//            if(in.getName().contains("1263.gz"))
//            if(in.getName().contains("eng-NG-31-1000.gz"))
//            if(in.getName().equals("eng-WL-11-9922.gz"))

                data.add(new Object[]{in});
        }
        return data.build();
    }

    @Test
    public void testParseWeb_BlogCompressed() throws IOException, ParsingException, SAXException {
        final Tac2013WebIO instance = new Tac2013WebIO();
        final List<WebDocument> doc = instance.readAll(IOUtils.asGzipByteSource(Files.asByteSource(dataFile)));
        assertNotNull(doc);
        assertTrue(doc.size() > 0);
    }

}
