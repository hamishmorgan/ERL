package uk.ac.susx.mlcl.erl.tac.io;

import nu.xom.ParsingException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

/**
 * Base interface for classes that handle reading and writing to some file format
 *
 * @author Hamish Morgan
 */
public interface BaseIO<T> {

    void writeAll(File file, List<T> items) throws IOException;

    void writeAll(Writer writer, List<T> items) throws IOException;

    List<T> readAll(File file) throws ParsingException, IOException;

    List<T> readAll(Reader reader) throws ParsingException, IOException;


}
