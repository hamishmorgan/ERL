package uk.ac.susx.mlcl.erl;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hello world!
 * <p/>
 */
@Nonnull
public class App {

    private static final Log LOG = LogFactory.getLog(App.class);

   
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

        FreebaseKB kb = new FreebaseKB();
        kb.init();

//        System.out.println(kb.getText("en/barack_obama"));


        // /astronomy/planet\
//
//
//        kb.foo();;
//        
        
        String q = "{\"type\" : \"/music/album\",\"artist\": \"The Police\", \"name\" : \"Synchronicity\", \"id\" : null}";
        String r = kb.rawMQLQuery(q);
        System.out.println(r);
//        

//        
//
//        Freebase freebase = initFreebase();
//
//        List<String> xx = new ArrayList<String>();
//        xx.add("/en/barack_obama");
//        ContentserviceGet get = freebase.text().get(xx).execute();
//        System.out.println(get.getResult());
//
//      

//        mlr.execute();
//        System.out.println(mlr.executeUnparsed());


//        
//        
//        AccessMethod accessMethod = null;
//        
//        String tokenServerEncodedUrl = null;
//        
//        HttpExecuteInterceptor clientAuthentication = null;
//        HttpRequestInitializer requestInitializer = null;
//        
//        // Set up the HTTP transport and JSON factory
//        HttpTransport httpTransport = new NetHttpTransport();
//
//        
//        // Set up OAuth 2.0 access of protected resources
//        // using the refresh and access tokens, automatically
//        // refreshing the access token when it expires
//        
//        Credential credential = new Credential.Builder(accessMethod)
//            .setJsonFactory(jsonFactory)
//            .setTransport(httpTransport)
//            .setTokenServerEncodedUrl(tokenServerEncodedUrl)
//            .setClientAuthentication(clientAuthentication)
//            .setRequestInitializer(requestInitializer)
//            .build();
//
//        
//        Freebase freebase = new Freebase.Builder(
//                httpTransport, jsonFactory, credential).build();
//        
//        
//        Freebase.Mqlread foo = freebase.mqlread("");
//        foo.execute();
//        
//        

//        
//        
//        
//        // Set up the main Google+ class
//        Plus plus = new Plus(httpTransport, jsonFactory, credential);
//
//        // Make a request to access your profile and display it to console
//        Person profile = plus.people().get("me").execute();
//        System.out.println("ID: " + profile.getId());
//        System.out.println("Name: " + profile.getDisplayName());
//        System.out.println("Image URL: " + profile.getImage().getUrl());
//        System.out.println("Profile URL: " + profile.getUrl());

    }
}
