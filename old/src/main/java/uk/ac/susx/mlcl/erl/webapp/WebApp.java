/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import uk.ac.susx.mlcl.erl.AnnotationServiceImpl2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * @author hiam20
 */
public class WebApp {

    private static final boolean DEBUG = true;
    private static final Logger LOG = LoggerFactory.getLogger(WebApp.class);

    public WebApp() {
    }

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put("annotators", "tokenize");
        props.put("tokenize.whitespace", "false");


        // tokenize.options:
        // Accepts the options of PTBTokenizer for example, things like
        //  "americanize=false"
        //  "strictTreebank3=true,
        //   untokenizable=allKeep".
        props.put("tokenize.options", "untokenizable=allKeep");


        props.put("clean.allowflawedxml", "true");


        WebApp webapp = new WebApp();
        webapp.init(props);


    }

    void init(Properties props)
            throws IOException, ClassNotFoundException,
            InstantiationException, ConfigurationException, IllegalAccessException,
            InterruptedException, InvocationTargetException, NoSuchMethodException {

        final AnnotationServiceImpl2 anno = AnnotationServiceImpl2.newInstance(props);

        anno.preloadLinker(false);

        // Debug logger write prints a detailed json structor of the entire
        // request object.
        Spark.before(new RequestLogger(LogLevel.DEBUG));
        Spark.after(new ResponseLogger(LogLevel.DEBUG));


        // The primary link annotation route
        Spark.post(new LinkService(anno, "/annotate/link/"));

        // Static resources such as HTML
        Spark.get(new StaticResource("/static/", "src/main/resources/", "*"));


        Spark.get(new Redirect("/", "/static/index.html"));


    }
}
