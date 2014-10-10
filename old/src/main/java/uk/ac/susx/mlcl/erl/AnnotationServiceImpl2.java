/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import io.github.hamishmorgan.erl.AnnotationServiceImpl;
import io.github.hamishmorgan.erl.Annotations;
import io.github.hamishmorgan.erl.JsonUtil;
import io.github.hamishmorgan.erl.snlp.AnnotationToXML;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import nu.xom.xslt.XSLTransform;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.lib.xml.XMLToStringSerializer;
import uk.ac.susx.mlcl.lib.xml.XomB;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author hamish
 */
public class AnnotationServiceImpl2 extends AnnotationServiceImpl {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationServiceImpl2.class);

    private final XomB xomb;
    private final AnnotationToXML xmler;

    public AnnotationServiceImpl2(AnnotatorPool pool, AnnotationToXML xmler, XomB xomb, JsonUtil jsonUtil) {
        super(pool, jsonUtil);
        this.xmler = xmler;
        this.xomb = xomb;
    }

    @Nonnull
    public static AnnotationServiceImpl2 newInstance(Properties props)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ConfigurationException {

        AnnotatorPool pool = Annotations.createPool(props);

        AnnotationToXML.Builder builder = AnnotationToXML.builder();
        builder.configure(new PropertiesConfiguration("sussexXml.properties"));

        AnnotationToXML xmler = builder.build();

        XomB xomb = new XomB();

        JsonFactory jsonFactory = new JacksonFactory();
        JsonUtil jsonUtil = new JsonUtil(jsonFactory);

        return new AnnotationServiceImpl2(pool, xmler, xomb, jsonUtil);
    }

    public Document linkAsXml(String text)
            throws InstantiationException {
        final Annotation document = link(text);
        return xmler.toDocument(document);
    }

    public void linkAsXml(String text, OutputStream out, @Nonnull Charset charset)
            throws InstantiationException, IOException {
        final Document xml = linkAsXml(text);
        writeXml(xml, out, charset, false);
    }

    public Document linkAHtml(String text) throws InstantiationException,
            IOException, XSLException, ParsingException {
        final Document xml = linkAsXml(text);
        XSLTransform transform = new XSLTransform(new nu.xom.Builder()
                .build(new File(
                        "src/main/resources/CoreNLP-to-HTML_2.xsl")));

        Nodes nodes = transform.transform(xml);
        return xomb.document().setDocType("html")
                .setRoot((Element) nodes.get(0)).build();
    }

    public void linkAsHtml(String text, OutputStream out, @Nonnull Charset charset)
            throws InstantiationException, IOException, XSLException, ParsingException {
        final Document xml = linkAHtml(text);
        writeXml(xml, out, charset, true);
    }

    private static void writeXml(Document document,
                                 OutputStream out, @Nonnull Charset charset,
                                 boolean decSkip)
            throws IOException {
        XMLToStringSerializer sr = new XMLToStringSerializer(
                out, charset.name());
        sr.setXmlDeclarationSkipped(decSkip);
        sr.setIndent(2);
        sr.write(document);
        sr.flush();
    }


}
