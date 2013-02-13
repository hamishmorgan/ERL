/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;
import edu.stanford.nlp.pipeline.Annotation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import nu.xom.ParsingException;
import nu.xom.xslt.XSLException;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.mlcl.erl.AnnotationService;

/**
 *
 * @author hiam20
 */
public class Main {

    public enum InputFormat {

        TEXT_PLAIN
    }

    public enum OutputFormat {

        SERIALIZE("ser", "application/java-serialized-object"),
        TEXT_PLAIN("txt", MimeTypes.TEXT_PLAIN),
        JSON("json", MimeTypes.TEXT_JSON),
        XML("xml", MimeTypes.TEXT_XML),
        HTML("html", MimeTypes.TEXT_HTML);
        private final String extension;
        private final String mimeType;

        private OutputFormat(String extension, String mimeType) {
            this.extension = checkNotNull(extension, "extension");
            this.mimeType = checkNotNull(mimeType, "mimeType");
        }

        public String getExtension() {
            return extension;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private final Configuration config;
    private final List<File> inputFiles;
    private final File outputDirectory;
    private final InputFormat inputFormat;
    private final OutputFormat outputFormat;
    private final Charset charset;
    private final boolean clobber;

    public Main(Configuration config, List<File> inputFiles, File outputDirectory, InputFormat inputFormat,
                OutputFormat outputFormat, Charset charset, boolean clobber) {
        this.config = checkNotNull(config, "config");
        this.inputFiles = checkNotNull(inputFiles, "inputFile");
        this.outputDirectory = checkNotNull(outputDirectory, "outputDirectory");
        this.inputFormat = checkNotNull(inputFormat, "inputFormat");
        this.outputFormat = checkNotNull(outputFormat, "outputFormat");
        this.charset = checkNotNull(charset, "charset");
        this.clobber = clobber;
    }

    private boolean run() throws InterruptedException, ClassNotFoundException,
            InstantiationException, ConfigurationException, IllegalAccessException,
            IOException, XSLException, ParsingException {
        LOG.info("running");

        final AnnotationService anno =
                AnnotationService.newInstance(
                ConfigurationConverter.getProperties(config));
        anno.preloadLinker(false);


        for (File inputFile : inputFiles) {

            LOG.info("Reading input file: {}", inputFile);
            final String text = Files.toString(inputFile, charset);

            OutputStream out = null;
            boolean isOutCloseable = true;
            try {
                if (outputDirectory.getPath().equals("-")) {
                    LOG.info("Writing to stdout.");
                    out = System.out;
                    isOutCloseable = false;
                } else {
                    // Sanity check and initialize the output directory 
                    if (!outputDirectory.exists()) {
                        LOG.info("Creating output directory: {}", outputDirectory);
                        if (!outputDirectory.mkdirs()) {
                            throw new IOException("Output directory does not exist and it "
                                    + "could not be created: " + outputDirectory);
                        }
                    } else if (!outputDirectory.isDirectory()) {
                        throw new IOException("Output directory is a not directory: " + outputDirectory);
                    }

                    // Create the new output file in the output directory
                    final String fileName = inputFile.getName() + "." + outputFormat.getExtension();
                    final File outputFile = new File(outputDirectory, fileName);
                    if (outputFile.exists()) {
                        if (clobber) {
                            if (!outputFile.canWrite()) {
                                throw new IOException("Existing output file is not writable: "
                                        + outputFile);
                            }
                            LOG.warn("Overwriting existing output file: {}", outputFile);
                        } else {
                            throw new IOException("Output file already exists: " + outputFile);
                        }
                    } else if (outputFile.isDirectory()) {
                        throw new IOException("Output file is a directory: " + outputFile);
                    }

                    LOG.info("Writing to file: {}", outputFile);
                    out = new BufferedOutputStream(new FileOutputStream(outputFile));
                    isOutCloseable = true;
                }

                LOG.info("Annotating to {}", outputFormat);
                switch (outputFormat) {
                    case HTML:
                        anno.linkAsHtml(text, out, charset);
                        break;
                    case XML:
                        anno.linkAsXml(text, out, charset);
                        break;
                    case JSON:
                        anno.linkAsJson(text, out, charset);
                        break;
                    case TEXT_PLAIN:
                        throw new UnsupportedOperationException(
                                "plain text output format is not yet implemented.");
//                break;
                    case SERIALIZE:
                        Annotation result = anno.link(text);
                        ObjectOutputStream oos = new ObjectOutputStream(out);
                        oos.writeObject(result);
                        oos.flush();
                        break;
                    default:
                        throw new AssertionError("Unknown output format: " + outputFormat);
                }
            } finally {
                if (out != null && isOutCloseable) {
                    Flushables.flushQuietly(out);
                    Closeables.closeQuietly(out);
                }
            }

        }
        LOG.info("all done");
        return false;
    }

    public static void main(@Nonnull String[] args) throws Exception {
        checkNotNull("args", args);

        Builder builder = new Builder();
        builder.setRawArgs(args);
        Main instance = builder.build();
        instance.run();
    }

    public static class Builder {

        private String[] rawArgs = null;
        private static final String PREFIX = "";
        private static final String CLOBBER_KEY = PREFIX + "clobber";
        private static final boolean CLOBBER_DEFAULT_VALUE = false;
        private static final String CHARSET_KEY = PREFIX + "charset";
        private static final String CHARSET_DEFAULT_VALUE = "UTF-8";
        private static final String INPUT_FORMAT_KEY = PREFIX + "inputFormat";
        private static final String INPUT_FORMAT_DEFAULT_VALUE = InputFormat.TEXT_PLAIN.name();
        private static final String OUTPUT_FORMAT_KEY = PREFIX + "outputFormat";
        private static final String OUTPUT_FORMAT_DEFAULT_VALUE = OutputFormat.JSON.name();

        public void setRawArgs(String[] rawArgs) {
            this.rawArgs = rawArgs;
        }

        public Main build() throws ConfigurationException {

            // XXX Move to resource.
            Configuration defaults = new BaseConfiguration();

            // defaults for the main cli:
            defaults.setProperty(CLOBBER_KEY, CLOBBER_DEFAULT_VALUE);
            defaults.setProperty(CHARSET_KEY, CHARSET_DEFAULT_VALUE);
            defaults.setProperty(INPUT_FORMAT_KEY, INPUT_FORMAT_DEFAULT_VALUE);
            defaults.setProperty(OUTPUT_FORMAT_KEY, OUTPUT_FORMAT_DEFAULT_VALUE);


            // defaults for other stuff
            defaults.setProperty("annotators", "tokenize");
            defaults.setProperty("tokenize.whitespace", "false");

            // tokenize.options:
            // Accepts the options of PTBTokenizer for example, things like 
            //  "americanize=false" 
            //  "strictTreebank3=true,
            //   untokenizable=allKeep".
            defaults.setProperty("tokenize.options", "untokenizable=allKeep");


            defaults.setProperty("clean.allowflawedxml", "true");

            // XXX end
            CombinedConfiguration comConf = new CombinedConfiguration(new OverrideCombiner());
            comConf.append(defaults);


            final CommandLineArgs parsedArgs = new CommandLineArgs();
            if (rawArgs != null) {

                // Store the return status of this invocation.
                boolean completedSuccessfully = true;

                final JCommander jc = new JCommander();
                jc.setProgramName("nel");
                jc.addObject(parsedArgs);

                try {
                    jc.parse(rawArgs);

                    if (parsedArgs.p_usageRequested) {
                        if (jc.getParsedCommand() == null) {
                            jc.usage();
                        } else {
                            jc.usage(jc.getParsedCommand());
                        }
                    }

                } catch (ParameterException ex) {
                    LOG.error(ex.getMessage());

                    final StringBuilder sb = new StringBuilder();
                    if (jc.getParsedCommand() == null) {
                        jc.usage(sb);
                    } else {
                        jc.usage(jc.getParsedCommand(), sb);
                    }
                    LOG.error("{}", sb);
                    completedSuccessfully = false;
                }

                if (!completedSuccessfully) {
                    System.exit(-1);
                }


            }



            if (parsedArgs.configurationResources != null) {
                for (String resource : parsedArgs.configurationResources) {
                    LOG.debug("Adding configuration resource: {}", resource);
                    comConf.addConfiguration(loadConfiguration(resource), resource);
                }
            }

            if (parsedArgs.properties != null) {
                comConf.addConfiguration(new MapConfiguration(parsedArgs.properties));
            }


            final boolean clobber = comConf.getBoolean(CLOBBER_KEY, CLOBBER_DEFAULT_VALUE);

            final OutputFormat outputFormat = OutputFormat.valueOf(
                    comConf.getString(OUTPUT_FORMAT_KEY, OUTPUT_FORMAT_DEFAULT_VALUE));

            final InputFormat inputFormat = InputFormat.valueOf(
                    comConf.getString(INPUT_FORMAT_KEY, INPUT_FORMAT_DEFAULT_VALUE));

            final Charset charset = (parsedArgs.charset != null)
                    ? parsedArgs.charset
                    : Charset.forName(comConf.getString(CHARSET_KEY, CHARSET_DEFAULT_VALUE));

            final List<File> inputFiles = Lists.newArrayList();
            inputFiles.addAll(parsedArgs.inputFileNames);

            final File outputFile = parsedArgs.outputDir;

            return new Main(comConf,
                            inputFiles, outputFile, inputFormat, outputFormat, charset,
                            clobber);
        }

        /**
         * Get the file extension from the given URL. The extension is either considered to be the
         * part after the last '.' (dot character). If not dot character exists in the file name
         * then this method returns the empty string.
         *
         * @param url
         * @return file name extension, or the empty-string if no extension exists.
         */
        private static String getFileExtension(URL url) {
            checkNotNull(url, "url");
            final int dotIndex = url.getFile().lastIndexOf('.');
            return (dotIndex >= 0)
                    ? url.getFile().substring(dotIndex + 1)
                    : "";
        }

        public static AbstractConfiguration loadConfiguration(String resource)
                throws ConfigurationException {
            final URL url = ConfigurationUtils.locate(resource);
            final String extension = getFileExtension(url).toLowerCase();

            switch (extension) {
                case "properties":
                    return new PropertiesConfiguration(url);
                case "plist":
                    return new PropertyListConfiguration(url);
                case "xml":
                    return new XMLConfiguration(url);
                case "ini":
                    return new HierarchicalINIConfiguration(url);
                case "":
                    throw new IllegalArgumentException("File extension not set in URL " + url);
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown file extension: \"%s\" in url: %s",
                                          extension, url));
            }
        }

        @Parameters(commandDescription = "")
        public static class CommandLineArgs {

            /**
             *
             */
            @Parameter(
            required = true,
            description = "FILE(s)")
            private List<File> inputFileNames = Lists.newArrayList();
            /**
             *
             */
            @Parameter(names = {"-o", "--outputDir"},
            description = "The destination directory for annotated documents. Default is the same "
            + "as the input file. If set to \"-\" the output is sent to stdout.")
            private File outputDir = null;
            /**
             *
             */
            @Parameter(names = {"-c", "--charset"},
            description = "Character encoding for reading and writing files.")
            private Charset charset = null;
            /**
             *
             */
            @Parameter(names = {"-h", "--help"}, help = true,
            description = "Display this usage screen.")
            private boolean p_usageRequested = false;
            /**
             *
             */
            @Parameter(names = {"-C", "--configurationFiles"},
            description = "Resource(s) from which to read configuration options.",
            variableArity = true)
            private List<String> configurationResources;
            /**
             *
             * Allow artibtrary configuration properties to be set at run time.
             */
            @DynamicParameter(names = "-D",
            description = "Set a property. (Overrides command line arguments "
            + "and configuration files.)")
            private Map<String, Object> properties = new HashMap<>();
        }
    }
}
