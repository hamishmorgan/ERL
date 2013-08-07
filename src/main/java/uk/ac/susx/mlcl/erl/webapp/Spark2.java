/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author hamish
 */
public class Spark2 {

    private static final Log LOG = LogFactory.getLog(Spark2.class);

    public static void staticRoute(final String remotePath, final String localPath) {
        spark.Spark.get(new Route(remotePath) {
            @Override
            public Object handle(Request request, Response response) {

                File file = new File(localPath);
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(file));
                    out = new BufferedOutputStream(response.raw().getOutputStream());
                    ByteStreams.copy(in, out);
                    out.flush();
                } catch (SecurityException ex) {
                    LOG.error(ex + "\n" + Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Forbidden.code());
                } catch (FileNotFoundException ex) {
                    LOG.error(ex + "\n" + Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Not_Found.code());
                } catch (IOException ex) {
                    LOG.error(ex + "\n" + Throwables.getStackTraceAsString(ex));
                    response.status(HttpStatus.Internal_Server_Error.code());
                } finally {
                    try {
                        Closeables.close(in, true);
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                }
                return "";
            }
        });

    }

    public static void setPort(int port) {
        spark.Spark.setPort(port);
    }

    public static void get(Route route) {
        spark.Spark.get(route);
    }

    public static void post(Route route) {
        spark.Spark.post(route);
    }

    public static void put(Route route) {
        spark.Spark.put(route);
    }

    public static void delete(Route route) {
        spark.Spark.delete(route);
    }

    public static void head(Route route) {
        spark.Spark.head(route);
    }

    public static void trace(Route route) {
        spark.Spark.trace(route);
    }

    public static void connect(Route route) {
        spark.Spark.connect(route);
    }

    public static void options(Route route) {
        spark.Spark.options(route);

    }

    public static void before(Filter filter) {
        spark.Spark.before(filter);
    }

    public static void after(Filter filter) {
        spark.Spark.after(filter);
    }

    static void waitForConnectionStatus(
            URL url, long timeoutDuration, TimeUnit timeoutUnits, ConnectionStatus desiredStatus)
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

    static boolean canConnect(URL url) throws IOException {

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

            return true;

        } catch (ConnectException ex) {

            return false;
        }
    }

    enum ConnectionStatus {

        UNKNOWN,
        AVAILABLE,
        UNAVAILABLE;

    }
}
