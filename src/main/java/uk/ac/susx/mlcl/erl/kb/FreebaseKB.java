/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.kb;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase2;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * <p/>
 * @author hiam20
 */
public class FreebaseKB implements KnowledgeBase {

    private static final Log LOG = LogFactory.getLog(FreebaseKB.class);

    private Freebase2 freebase;

    FreebaseKB(Freebase2 freebase) {
        this.freebase = freebase;
    }

    public static FreebaseKB newInstance() throws IOException {
        final String googleApiKey = Freebase2.loadGoogleApiKey(
                Paths.get(".googleApiKey.txt"));

        JsonHttpRequestInitializer credential =
                new GoogleKeyInitializer(googleApiKey);

        JsonFactory jsonFactory = new JacksonFactory();

        Freebase2 freebase = new Freebase2.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName("ERL/1.0")
                .setJsonHttpRequestInitializer(credential)
                .build();

        return new FreebaseKB(freebase);
    }

    /**
     *
     * @param id
     * @return
     * @throws IOException
     */
    public String text(String id) throws IOException {
        return freebase.text().get(Arrays.asList(id)).execute().getResult();
    }

    /**
     *
     * @param query
     * @return
     * @throws IOException
     */
    public List<String> search(String query) throws IOException {
        return freebase.searchGetIds(query);
    }
}