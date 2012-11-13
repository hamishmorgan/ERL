/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.kb;

import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase;
import com.google.api.services.freebase.Freebase2;
import com.google.api.services.freebase.FreebaseRequest;
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

    private static final String APPLICATION_NAME = "ERL/1.0";

    private static final String API_KEY_FILE = ".google_api_key.txt";

    private Freebase2 freebase;

    FreebaseKB(Freebase2 freebase) {
	this.freebase = freebase;
    }

    public static FreebaseKB newInstance() throws IOException {
	final String googleApiKey =
		Freebase2.loadGoogleApiKey(Paths.get(API_KEY_FILE));

	JsonHttpRequestInitializer credential =
		new GoogleKeyInitializer(googleApiKey);

//	JsonHttpRequestInitializer credential =
//		new JsonHttpRequestInitializer() {
//		    public void initialize(JsonHttpRequest request) {
//			System.out.println("initializing: " + request);
//			request.put("key", googleApiKey);
//		    }
//		};

	//	JsonHttpRequestInitializer credential =
	//		new JsonHttpRequestInitializer() {
	//		    public void initialize(JsonHttpRequest request) {
	//			if (!(request instanceof FreebaseRequest))
	//			    throw new IllegalArgumentException();
	//			FreebaseRequest freebaseRequest =
	//				(FreebaseRequest) request;
	//			freebaseRequest.setPrettyPrint(true);
	//			if (request instanceof Freebase.Mqlread) {
	//			    ((Freebase.Mqlread) request).setIndent(2L);
	//			}
	//			freebaseRequest.setKey(googleApiKey);
	//		    }
	//		};
	JsonFactory jsonFactory = new JacksonFactory();

	Freebase2 freebase = new Freebase2.Builder(
		new NetHttpTransport(), jsonFactory, null)
		.setApplicationName(APPLICATION_NAME)
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
