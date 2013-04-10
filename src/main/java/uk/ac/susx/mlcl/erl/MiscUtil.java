/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * Stuff that should be elsewhere!
 *
 * @author hiam20
 */
public class MiscUtil {

    private static final Log LOG = LogFactory.getLog(MiscUtil.class);
    private static final String APPLICATION_NAME = "ERL/1.0";
    private static final String API_KEY_FILE = ".google_api_key.txt";

    public static Freebase2 newFreebaseInstance() throws IOException {
        final String googleApiKey =
                Freebase2.loadGoogleApiKey(new File(API_KEY_FILE));

        JsonHttpRequestInitializer credential =
                new GoogleKeyInitializer(googleApiKey);


        JsonFactory jsonFactory = new JacksonFactory();

        Freebase2 freebase = new Freebase2.Builder(
                new NetHttpTransport(), jsonFactory, null)
                .setApplicationName(APPLICATION_NAME)
                .setJsonHttpRequestInitializer(credential)
                .build();

        return freebase;
    }


}
