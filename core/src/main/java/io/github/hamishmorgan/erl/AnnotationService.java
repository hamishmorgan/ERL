package io.github.hamishmorgan.erl;

import edu.stanford.nlp.pipeline.Annotation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public interface AnnotationService {

    @Nonnull
    Annotation link(@Nonnull Annotation document);

    @Nonnull
    Annotation link(@Nonnull String text);

    @Nonnull String linkAsJson(@Nonnull String text);

    void linkAsJson(@Nonnull String text, @Nonnull Writer writer) throws IOException;
}
