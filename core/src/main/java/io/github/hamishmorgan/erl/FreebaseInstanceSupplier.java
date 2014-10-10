/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.hamishmorgan.erl;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase2;
import com.google.common.base.Supplier;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Stuff that should be elsewhere!
 *
 * @author hiam20
 */
public class FreebaseInstanceSupplier implements Supplier<Freebase2> {

    private static final String APPLICATION_NAME = "ERL/1.0";
    private static final String API_KEY_FILE = ".google_api_key.txt";

    @Nonnull
    public Freebase2 get() {

        final String googleApiKey;

        try {

            googleApiKey = Freebase2.loadGoogleApiKey(new File(API_KEY_FILE));

            JsonHttpRequestInitializer credential =
                    new GoogleKeyInitializer(googleApiKey);


            JsonFactory jsonFactory = new JacksonFactory();

            return new Freebase2.Builder(
                    new NetHttpTransport(), jsonFactory, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setJsonHttpRequestInitializer(credential)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
