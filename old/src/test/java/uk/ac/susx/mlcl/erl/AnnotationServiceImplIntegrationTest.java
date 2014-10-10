package uk.ac.susx.mlcl.erl;

import com.beust.jcommander.internal.Lists;
import com.google.common.io.Closer;
import edu.jhu.agiga.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.junit.*;
import uk.ac.susx.mlcl.erl.test.AbstractTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import static java.text.MessageFormat.format;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 14/03/2013
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationServiceImplIntegrationTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AnnotationServiceImplIntegrationTest.class);
    private static final String GW5_SAMPLE_PATH = "src/test/resources/uk/ac/susx/mlcl/erl/nyt_eng_201012_sample.xml.gz";
    private static AnnotationServiceImpl2 instance;

    @BeforeClass
    public static void setUpClass() throws IOException, ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, InterruptedException, InvocationTargetException, NoSuchMethodException {
        final Properties props = new Properties();
        instance = AnnotationServiceImpl2.newInstance(props);
    }

    @Test
    public void testLinkFromText() throws ClassNotFoundException, InstantiationException, ConfigurationException, IllegalAccessException, IOException {

        final String text = "The parish of Churchstoke is bisected by Offa's Dyke. Part of the parish lies in England" +
                " and part of it in Wales, but the Dyke delineates only a segment of the boundary between England and" +
                " Wales, which boundary also separates the counties of Powys and Shropshire. In this region of the" +
                " Welsh Marches, there is a significant incursion of Wales east of Offa’s Dyke, an area which" +
                " includes Corndon Hill and the Churchstoke valley. The borderland parish of Church Stoke comprises" +
                " nine areas, known as townships, one of which is Bacheldre, which lay in the former county of" +
                " Montgomeryshire. Click here to see an outline map of the parish.";

        Annotation document = instance.link(text);

        instance.printAnnotationAsJson(document);
    }


    /**
     * Read text from GW5 sample
     *
     * @throws IOException
     */
    @Test
    public void temp() throws IOException {

        final Closer closer = Closer.create();

        try {
            final AgigaPrefs prefs = new AgigaPrefs();
            final StreamingDocumentReader instance =
                    closer.register(new StreamingDocumentReader(GW5_SAMPLE_PATH, prefs));

            int documentCount = 0;
            for (final AgigaDocument doc : instance) {
                ++documentCount;

                LOG.debug(format("Reading document {2}; id={0}, type={1}",
                        doc.getDocId(), doc.getType(), documentCount));

                final StringBuilder sentBuilder = new StringBuilder();
                for (AgigaSentence sent : doc.getSents()) {

                    for (AgigaToken tok : sent.getTokens()) {
                        sentBuilder.append(tok.getWord());
                        sentBuilder.append(' ');
                    }
                    sentBuilder.append('\n');
                }
                System.out.println(sentBuilder.toString());

            }
            LOG.info("Number of docs: " + instance.getNumDocs());
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }


    /**
     * Build annotation
     *
     * @throws IOException
     */
    @Test
    public void temp2() throws IOException {

        final Closer closer = Closer.create();

        try {
            final AgigaPrefs prefs = new AgigaPrefs();
            final StreamingDocumentReader reader =
                    closer.register(new StreamingDocumentReader(GW5_SAMPLE_PATH, prefs));
            prefs.setAll(true);

            int documentCount = 0;
            for (final AgigaDocument doc : reader) {
                ++documentCount;
                LOG.debug(format("Reading document {2}; id={0}, type={1}",
                        doc.getDocId(), doc.getType(), documentCount));

                // Doesn't like creating an annotation without text, but then
                // we don't really have any choice.
                @SuppressWarnings("deprecation")
                Annotation snlpDocument = new Annotation();
                snlpDocument.set(CoreAnnotations.DocIDAnnotation.class, doc.getDocId());
                snlpDocument.set(DocTypeAnnotation.class, doc.getType());

                final List<CoreLabel> snlpDocumentTokens = Lists.newArrayList();
                final StringBuilder snlpDocumentText = new StringBuilder();

                final List<CoreMap> snlpSentences = Lists.newArrayList();
                int sentenceCount = 0;
                for (AgigaSentence tmp : doc.getSents()) {
                    if (!(tmp instanceof StanfordAgigaSentence))
                        throw new AssertionError();
                    final StanfordAgigaSentence agigaSent = (StanfordAgigaSentence) tmp;

                    final CoreMap snlpSent = new ArrayCoreMap();

                    snlpSent.set(CoreAnnotations.SentenceIndexAnnotation.class, sentenceCount);

                    final List<CoreLabel> snlpSentenceTokens = Lists.newArrayList();
                    final StringBuilder snlpSentenceText = new StringBuilder();

                    for (AgigaToken agigaToken : agigaSent.getTokens()) {
                        CoreLabel snlpToken = new CoreLabel();
                        snlpToken.setIndex(agigaToken.getTokIdx());
                        snlpToken.setValue(agigaToken.getWord());
                        snlpToken.setWord(agigaToken.getWord());
                        snlpToken.setBeginPosition(agigaToken.getCharOffBegin());
                        snlpToken.setEndPosition(agigaToken.getCharOffEnd());

                        snlpToken.setLemma(agigaToken.getLemma());
                        snlpToken.setTag(agigaToken.getPosTag());
                        snlpToken.setNER(agigaToken.getNerTag());
                        snlpToken.set(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class, agigaToken.getNormNer());

                        snlpToken.setDocID(doc.getDocId());
                        snlpToken.setSentIndex(sentenceCount);

                        snlpSentenceTokens.add(snlpToken);
                        snlpSentenceText.append(agigaToken.getWord()).append(" ");
                    }

                    snlpSent.set(CoreAnnotations.TokensAnnotation.class, snlpSentenceTokens);
                    snlpSent.set(CoreAnnotations.TextAnnotation.class, snlpSentenceText.toString());
                    snlpSentences.add(snlpSent);

                    snlpDocumentTokens.addAll(snlpSentenceTokens);
                    snlpDocumentText.append(snlpSentenceText);

                    sentenceCount++;
                }

                snlpDocument.set(CoreAnnotations.TokensAnnotation.class, snlpDocumentTokens);
                snlpDocument.set(CoreAnnotations.TextAnnotation.class, snlpDocumentText.toString());
                snlpDocument.set(CoreAnnotations.SentencesAnnotation.class, snlpSentences);

                Annotation result = instance.link(snlpDocument);
                instance.printAnnotationAsJson(result);
            }
            LOG.info("Number of docs: " + reader.getNumDocs());
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

}
