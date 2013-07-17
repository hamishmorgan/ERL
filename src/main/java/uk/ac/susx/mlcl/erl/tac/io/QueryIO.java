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
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Base class for reading and writing to Query XML files.
 *
 * @author Hamish Morgan
 */
public abstract class QueryIO implements BaseIO<Query> {

    private static final Logger LOG = LoggerFactory.getLogger(QueryIO.class);
    private static final Pattern TAC2010_ID_PATTERN = Pattern.compile("^EL[\\d]{5,6}$");

    public static QueryIO detectFormat(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries file: {}", queriesFile);
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesFile));
    }

    public static QueryIO detectFormat(URL queriesUrl) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries url: {}", queriesUrl);
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesUrl.openStream()));
    }

    public static QueryIO detectFormat(Reader queriesReader) throws ParsingException, IOException {
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesReader));
    }

    private static QueryIO detectFormat(Document doc) {
        final Element child = doc.getRootElement().getFirstChildElement(Tac2009QueryIO.QUERY_ELEM_NAME);

        final QueryIO format;
        if (child.getFirstChildElement(Tac2012QueryIO.BEGIN_ELEM_NAME) != null) {
            format = new Tac2012QueryIO();
        } else {
            final String id = child.getAttribute(Tac2009QueryIO.ID_ATTR_NAME).getValue();
            if (TAC2010_ID_PATTERN.matcher(id).matches())
                if (child.getFirstChildElement(Tac2010GoldQueryIO.ENTITY_ELEM_NAME) != null)
                    format = new Tac2010GoldQueryIO();
                else
                    format = new Tac2010QueryIO();
            else
                format = new Tac2009QueryIO();
        }

        LOG.debug("Detected format: {}", format);
        return format;
    }

}
