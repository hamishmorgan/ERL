package uk.ac.susx.mlcl.erl.tac.io;

import com.google.common.io.Closer;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.tac.Query;

import java.io.BufferedInputStream;
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
//    private static final Pattern TAC2010_ID_PATTERN = Pattern.compile("^EL[\\d]{5,6}$");

    public static QueryIO detectFormat(File queriesFile) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries file: {}", queriesFile);
        Builder parser = new Builder();
        return detectFormat(parser.build(queriesFile));
    }

    public static QueryIO detectFormat(URL queriesUrl) throws ParsingException, IOException {
        LOG.debug("Detecting format from queries url: {}", queriesUrl);
        final Builder parser = new Builder();
        final Closer closer = Closer.create();
        try {
            return detectFormat(parser.build(
                    closer.register(new BufferedInputStream(
                            closer.register(queriesUrl.openStream())))));
        } catch (ParsingException e) {
            throw closer.rethrow(e, ParsingException.class);
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    public static QueryIO detectFormat(Reader queriesReader) throws ParsingException, IOException {
        final Builder parser = new Builder();
        return detectFormat(parser.build(queriesReader));
    }

    private static QueryIO detectFormat(Document doc) {
        LOG.debug("Detecting format from document: {}", doc);
        final Element child = doc.getRootElement().getFirstChildElement(Tac2009QueryIO.QUERY_ELEM_NAME);
        final String qid = child.getAttribute(Tac2009QueryIO.ID_ATTR_NAME).getValue();

        final QueryIO format;
        if (child.getFirstChildElement(Tac2012QueryIO.BEGIN_ELEM_NAME) != null) {
            assert LinkIO.TAC2012_QID_PATTERN.matcher(qid).matches();
            format = new Tac2012QueryIO();
        } else {
            if (LinkIO.TAC2010_QID_PATTERN.matcher(qid).matches()) {
                if (child.getFirstChildElement(Tac2010GoldQueryIO.ENTITY_ELEM_NAME) != null)
                    format = new Tac2010GoldQueryIO();
                else
                    format = new Tac2010QueryIO();
            } else  if (LinkIO.TAC2011_QID_PATTERN.matcher(qid).matches())
                format = new Tac2011QueryIO();
            else  if (LinkIO.TAC2009_QID_PATTERN.matcher(qid).matches())
                format = new Tac2009QueryIO();
            else throw new AssertionError("Uknown query id format: " + qid);
        }

        LOG.debug("Detected format: {}", format);
        return format;
    }

}
