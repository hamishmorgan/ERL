/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spark;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.net.MediaType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.webapp.LogLevel;
import uk.ac.susx.mlcl.erl.webapp.RequestLogger;
import uk.ac.susx.mlcl.erl.webapp.ResponseLogger;

/**
 *
 * @author hiam20
 */
public class SparkExamples {

    private static final Log LOG = LogFactory.getLog(SparkExamples.class);

    private static final File FAVICON_PATH = new File("src/test/resources/spark/favicon.ico");

    private static String PROTOCOL;

    private static int PORT;

    private static String HOST;

    private static URL ROOT_URL;

    private static Charset CHARSET;

    @BeforeClass
    public static void init() throws MalformedURLException {
        PROTOCOL = "http";
        PORT = 5678;
        HOST = "localhost";
        ROOT_URL = new URL(PROTOCOL, HOST, PORT, "/");
        CHARSET = Charset.forName("UTF-8");
    }

    @Before
    public void before() throws IOException, InterruptedException, TimeoutException {
        Spark.setPort(PORT);

        Spark.before(new RequestLogger(LogLevel.DEBUG));
        Spark.after(new ResponseLogger(LogLevel.DEBUG));

        Spark.trace(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                this.halt();
                return null;

            }
        });

        Spark.get(new Route("/favicon.ico") {
            @Override
            public Object handle(Request request, Response response) {

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(FAVICON_PATH));
                    out = new BufferedOutputStream(response.raw().getOutputStream());
                    response.raw().setContentType(MediaType.ICO.toString());
                    response.status(200);
                    ByteStreams.copy(in, out);
                    out.flush();
                    return "";
                } catch (FileNotFoundException ex) {
                    LOG.warn(ex);
                    response.status(404);
                    return ex.getMessage();
                } catch (IOException ex) {
                    LOG.warn(ex);
                    response.status(500);
                    return ex.getMessage();
                } finally {
                    Closeables.closeQuietly(in);
                }
            }
        });
        waitForConnectionStatus(ROOT_URL, 1, TimeUnit.DAYS, ConnectionStatus.AVAILABLE);
    }

    @After
    public void after() throws IOException, InterruptedException, TimeoutException {
        Spark.clearRoutes();
        Spark.stop();
        waitForConnectionStatus(ROOT_URL, 1, TimeUnit.DAYS, ConnectionStatus.UNAVAILABLE);
    }

    @Test
    public void helloWorldExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        final String expected = "Hello World!";
        final String path = "/hello";
        final URL url = new URL(ROOT_URL, path);

        Spark.get(new Route(path) {
            @Override
            public Object handle(Request request, Response response) {
                return expected;
            }
        });

        String actual = runHttpGetString(url);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void simplePostExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        final String path = "/hello";
        final URL url = new URL(ROOT_URL, path);

        Spark.post(new Route(path) {
            @Override
            public Object handle(Request request, Response response) {
                return "Hello World: " + request.body();
            }
        });

        Assert.assertEquals("Hello World: ",
                            runHttpPostInput(url, ""));
    }

    @Test
    public void simple401ErrorCodeExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        final URL url = new URL(ROOT_URL, "/private");

        Spark.get(new Route(url.getPath()) {
            @Override
            public Object handle(Request request, Response response) {
                response.status(401);
                return "Go Away!!!";
            }
        });

        Assert.assertTrue(getResponseCode(url) == 401);

    }

    @Test
    public void simpleParamsExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {


        Spark.get(new Route("/users/:name") {
            @Override
            public Object handle(Request request, Response response) {
                return "Selected user: " + request.params(":name");
            }
        });

        Assert.assertEquals("Selected user: hamish",
                            runHttpGetString(new URL(ROOT_URL, "/users/hamish")));
    }

    @Test
    public void simpleParamsExample2() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        Spark.get(new Route("/news/:section") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/xml");
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" + request
                        .params("section") + "</news>";
            }
        });

        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>things</news>",
                            runHttpGetString(new URL(ROOT_URL, "/news/things")));
    }

    @Test
    public void simple403ErrorCodeExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        final URL url = new URL(ROOT_URL, "/protected");
        Spark.get(new Route(url.getPath()) {
            @Override
            public Object handle(Request request, Response response) {
                halt(403, "I don't think so!!!");
                return null;
            }
        });

        Assert.assertTrue(getResponseCode(url) == 403);

    }

    @Test
    public void simpleRedirectExample() throws InterruptedException, MalformedURLException, IOException, TimeoutException {


        final String expected = "Hello World!";
        final String path = "/hello";
        final String redirectPath = "/redirect";


        Spark.get(new Route(path) {
            @Override
            public Object handle(Request request, Response response) {
                return expected;
            }
        });

        Spark.get(new Route(redirectPath) {
            @Override
            public Object handle(Request request, Response response) {
                response.redirect(path);
                return null;
            }
        });


        final String actual = runHttpGetString(new URL(ROOT_URL, redirectPath));
        Assert.assertEquals(expected, actual);

    }

    /*
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    static class Book {

        private String author;

        private String title;

        public Book(String author, String title) {
            this.author = author;
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "Book{" + "author=" + author + ", title=" + title + '}';
        }
    }

    @Test
    public void RESTfulExample() throws IOException, TimeoutException {

        final int startingId = 1000;

        {
            final Map<String, Book> books = new HashMap<String, Book>();

            final AtomicInteger nextId = new AtomicInteger(startingId);

            // Creates a new book resource, will return the ID to the created resource
            // author and title are sent as query parameters e.g. /books?author=Foo&title=Bar
            Spark.post(new Route("/books") {
                @Override
                public Object handle(Request request, Response response) {
                    String title = request.queryParams("title");
                    String author = request.queryParams("author");
                    Book book = new Book(author, title);

                    int id = nextId.getAndIncrement();
                    books.put(String.valueOf(id), book);

                    response.status(201); // 201 Created
                    LOG.info("Created book: " + book);
                    return id;
                }
            });

            // Gets the book resource for the provided id
            Spark.get(new Route("/books/:id") {
                @Override
                public Object handle(Request request, Response response) {
                    Book book = books.get(request.params(":id"));
                    if (book != null) {
                        return "Title: " + book.getTitle() + ", Author: " + book.getAuthor();
                    } else {
                        response.status(404); // 404 Not found
                        return "Book not found";
                    }
                }
            });

            // Updates the book resource for the provided id with new information
            // author and title are sent as query parameters e.g. /books/<id>?author=Foo&title=Bar
            Spark.put(new Route("/books/:id") {
                @Override
                public Object handle(Request request, Response response) {
                    String id = request.params(":id");
                    Book book = books.get(id);
                    if (book != null) {
                        String newAuthor = request.queryParams("author");
                        String newTitle = request.queryParams("title");
                        if (newAuthor != null) {
                            book.setAuthor(newAuthor);
                        }
                        if (newTitle != null) {
                            book.setTitle(newTitle);
                        }
                        return "Book with id '" + id + "' updated";
                    } else {
                        response.status(404); // 404 Not found
                        return "Book not found";
                    }
                }
            });

            // Deletes the book resource for the provided id
            Spark.delete(new Route("/books/:id") {
                @Override
                public Object handle(Request request, Response response) {
                    String id = request.params(":id");
                    Book book = books.remove(id);
                    if (book != null) {
                        return "Book with id '" + id + "' deleted";
                    } else {
                        response.status(404); // 404 Not found
                        return "Book not found";
                    }
                }
            });

            // Gets all available book resources (id's)
            Spark.get(new Route("/books") {
                @Override
                public Object handle(Request request, Response response) {
                    String ids = "";
                    for (String id : books.keySet()) {
                        ids += id + " ";
                    }
                    return ids;
                }
            });

        }

        String[][] books = {
            {"U. Lose", "I Win!"},
            {"Anne Droid", "Robots"},
            {"Luke Out", "Danger!"},
            {"Irma Dubble II", "Cloning"},
            {"Frank Furter", "Hot Dog!"}};

        final URL booksRoot = new URL(ROOT_URL, "/books");

        MessageFormat putBookFmt = new MessageFormat("author={0}&title={1}");

        for (int i = 0; i < books.length; i++) {
            StringBuffer query = new StringBuffer();
            putBookFmt.format(new String[]{
                        URLEncoder.encode(books[i][0], CHARSET.name()),
                        URLEncoder.encode(books[i][1], CHARSET.name())}, query, null);

            String id = runHttpPostInput(booksRoot, query.toString());
            Assert.assertEquals(Integer.toString(startingId + i), id);


            String x = runHttpGetString(new URL(booksRoot, "/books/" + id));

            String parts[] = x.split("(\\s*,\\s*)|(\\s*:\\s*)");
            String title = parts[1];
            String author = parts[3];


            Assert.assertEquals(books[i][0], author);
            Assert.assertEquals(books[i][1], title);
        }


        String bookIds = runHttpGetString(new URL(booksRoot, "/books"));
        Assert.assertEquals(books.length, bookIds.split("\\s+").length);


//        waitForConnectionStatus(ROOT_URL, 1, TimeUnit.DAYS, ConnectionStatus.UNAVAILABLE);
    }

    @Test
    public void filtersExample() {

        final Map<String, String> usernamePasswords = new HashMap<String, String>();

        usernamePasswords.put("foo", "bar");
        usernamePasswords.put("admin", "admin");

        Spark.before(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                String user = request.queryParams("user");
                String password = request.queryParams("password");

                String dbPassword = usernamePasswords.get(user);
                if (!(password != null && password.equals(dbPassword))) {
                    halt(401, "You are not welcome here!!!");
                }
            }
        });

        Spark.before(new Filter("/hello") {
            @Override
            public void handle(Request request, Response response) {
                response.header("Foo", "Set by second before filter");
            }
        });

        Spark.get(new Route("/hello") {
            @Override
            public Object handle(Request request, Response response) {
                return "Hello World!";
            }
        });

        Spark.after(new Filter("/hello") {
            @Override
            public void handle(Request request, Response response) {
                response.header("spark", "added by after-filter");
            }
        });
    }

    @Test
    public void FilterExampleAttributes() throws IOException, TimeoutException {
        Spark.get(new Route("/hi") {
            @Override
            public Object handle(Request request, Response response) {
                request.attribute("foo", "bar");
                return null;
            }
        });

        Spark.after(new Filter("/hi") {
            @Override
            public void handle(Request request, Response response) {
                for (String attr : request.attributes()) {
                    System.out.println("attr: " + attr);
                }
            }
        });

        Spark.after(new Filter("/hi") {
            @Override
            public void handle(Request request, Response response) {
                Object foo = request.attribute("foo");
                if ("xml".equals(request.queryParams("format"))) {
                    response.body(asXml("foo", foo));
                } else if ("json".equals(request.queryParams("format"))) {
                    response.body(asJson("foo", foo));
                } else {
                    response.body("foo");
                }

            }
        });


//        waitForConnectionStatus(ROOT_URL, 1, TimeUnit.DAYS, ConnectionStatus.UNAVAILABLE);
    }

    private static String asXml(String name, Object value) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + name + ">" + value + "</" + name
                + ">";
    }

    private static String asJson(String name, Object value) {
        return "{" + name + ": " + value + "}";
    }

    @Test
    public void testGetFavicon() throws InterruptedException, MalformedURLException, IOException, TimeoutException {

        final URL url = new URL(ROOT_URL, "/favicon.ico");

        InputStream in = new FileInputStream(FAVICON_PATH);
        byte[] expected = ByteStreams.toByteArray(in);

        byte[] actual = runHttpGetBytes(url);
        System.out.println(actual);


        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));


        for (int i = 1400; i < expected.length; i++) {
            System.out.print(expected[i] + ", ");
        }
        System.out.println();

        for (int i = 1400; i < actual.length; i++) {
            System.out.print(actual[i] + ", ");
        }
        System.out.println();


        System.out.println(new String(actual, 1406, actual.length - 1406));

        Assert.assertArrayEquals(expected, actual);

    }

    /*
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    static String runHttpGetString(URL url) throws IOException {
        InputStream in = null;
        try {
            in = url.openStream();
            return readAll(in);
        } finally {
            Closeables.closeQuietly(in);
        }

    }

    static byte[] runHttpGetBytes(URL url) throws IOException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Length", "0");
            connection.setUseCaches(false);
            connection.connect();

            InputStream in = null;
            try {
                in = connection.getInputStream();
                return ByteStreams.toByteArray(in);
            } finally {
                Closeables.closeQuietly(in);
            }

        } finally {
            connection.disconnect();
        }
    }

    static int getResponseCode(URL url) throws UnsupportedEncodingException, IOException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();

            return connection.getResponseCode();

        } finally {
            connection.disconnect();
        }
    }

    static String runHttpPostInput(URL url, String body) throws UnsupportedEncodingException, IOException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(body != null && !body.isEmpty());
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", CHARSET.name());
            connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
            connection.setUseCaches(false);
            connection.connect();

            if (!body.isEmpty()) {
                Writer out = null;
                try {
                    out = new OutputStreamWriter(new BufferedOutputStream(
                            connection.getOutputStream()));
                    out.append(body);
                    out.flush();
                } finally {
                    Closeables.closeQuietly(out);
                }
            }


            InputStream in = null;
            try {
                in = connection.getInputStream();
                return readAll(in);
            } finally {
                Closeables.closeQuietly(in);
            }

        } finally {
            connection.disconnect();
        }
    }

    static String readAll(InputStream in) throws IOException {

        if (in instanceof BufferedInputStream) {
            in = new BufferedInputStream(in);
        }

        Reader reader = new InputStreamReader(in);

        StringBuilder sb = new StringBuilder();
        CharBuffer cbuf = CharBuffer.allocate(8096);
        while (reader.read(cbuf) > 0) {
            cbuf.flip();
            sb.append(cbuf);
            cbuf.clear();
        }

        return sb.toString();


    }

    enum ConnectionStatus {

        UNKNOWN,
        AVAILABLE,
        UNAVAILABLE,
    }

    static void waitForConnectionStatus(URL url,
                                        long timeoutDuration, TimeUnit timeoutUnits,
                                        ConnectionStatus desiredStatus)
            throws IOException, TimeoutException {

        // Time in millis that this method should stop trying the connection
        final long endTimeMillis = System.currentTimeMillis()
                + TimeUnit.MILLISECONDS.convert(timeoutDuration, timeoutUnits);

        // Time to wait between retries. This value will slowly increase ater each failure,
        long retryInterval = 0;

        ConnectionStatus status = ConnectionStatus.UNKNOWN;
        while (status != desiredStatus && System.currentTimeMillis() < endTimeMillis) {

            if (retryInterval > 0) {
                LOG.trace(MessageFormat.format(
                        "Retrying connection in {0} milliseconds.", retryInterval));
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException intEx) {
                    throw new AssertionError(intEx);
                }
            }

            // Increase sleep duration by a small factor
            //      bounded at min: +1, and max: +time remaining
            retryInterval = (long) (retryInterval * Math.sqrt(2)) + 1L;
            if (retryInterval > (endTimeMillis - System.currentTimeMillis())) {
                retryInterval = (endTimeMillis - System.currentTimeMillis());
            }

            try {
                // Configure the connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setDoInput(false);
                connection.setDoOutput(false);
                connection.setRequestMethod("TRACE");
                connection.setUseCaches(false);

                // Try to connect then disconnect (this may well fail)
                connection.connect();
                connection.disconnect();

                status = ConnectionStatus.AVAILABLE;
            } catch (ConnectException ex) {
                // Failed to connect.
                status = ConnectionStatus.UNAVAILABLE;
            }


        }

        // If the timelimit expired before the desired connection status was achieved then
        // throw an exception
        if (status != desiredStatus) {
            throw new TimeoutException(MessageFormat.format(
                    "Waiting for connection status {0} timed out.", desiredStatus));
        }

    }
}
