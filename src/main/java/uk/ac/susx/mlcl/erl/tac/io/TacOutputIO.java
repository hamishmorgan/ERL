package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.erl.tac.queries.Output;
import uk.ac.susx.mlcl.erl.tac.queries.OutputSet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 20/08/2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class TacOutputIO implements BaseIO<Output> {
    private static final Log LOG = LogFactory.getLog(TacOutputIO.class);
    public final Charset charset;

    public TacOutputIO(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void writeAll(File file, List<Output> items) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeAll(URL url, List<Output> items) throws IOException, URISyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeAll(Writer writer, List<Output> items) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputSet readAll(File file) throws  IOException {
        LOG.info("Reading tac output file " + file);
        return Files.readLines(file, charset, new OutputLineProcessor(file.getName()));
    }

    @Override
    public OutputSet readAll(URL url) throws  IOException {
        LOG.info("Reading tac output url " + url);
        return Resources.readLines(url, charset, new OutputLineProcessor(url.getFile()));
    }

    @Override
    public OutputSet readAll(Reader reader) throws IOException {
        return CharStreams.readLines(reader,  new OutputLineProcessor("<unnamed>"));
    }

}
