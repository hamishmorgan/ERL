package uk.ac.susx.mlcl.erl.tac.io;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.Query;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 17/07/2013
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueryIO {

    private static final Logger LOG = LoggerFactory.getLogger(QueryIO.class);

    public static QueryIO detectFormat(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries file: {}", queriesFile);
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesFile));
    }

    public static QueryIO detectFormat(Reader queriesReader) throws ParsingException, IOException {
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesReader));
    }

    static QueryIO detectFormat(Document doc) throws ParsingException, IOException {
        final Element child = doc.getRootElement().getFirstChildElement("query");

        final QueryIO format;
        if (child.getFirstChildElement("beg") != null) {
            format = new Tac2012QueryIO();
        } else {
            final String id = child.getAttribute("id").getValue();
            if (id.matches("^EL[\\d]{5,6}$"))
                format = new Tac2010QueryIO();
            else
                format = new Tac2009QueryIO();
        }

        LOG.debug("Detected format: {}", format);
        return format;
    }

    public abstract List<Query> readAll(File queriesFile) throws ParsingException, IOException;

//    public abstract List<Query> readAll(Reader queriesReader) throws ParsingException, IOException;

    public abstract void writeAll(File queriesFile, List<Query> queries) throws IOException;

}
