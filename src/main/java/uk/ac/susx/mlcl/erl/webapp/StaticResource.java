/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.webapp;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.text.MessageFormat;

/**
 * A route which serves static resource from the file directory structure. The request path is
 * mapped onto a local path, and if that path matches a file it is sent to the client.
 * <p/>
 * A number of checks are made to insure system security. In particlar a file will only be served if
 * it is not hidden, if it is a normal file, and if the file is direct descendent of the mapped
 * local path. Paths are converted to canonical form, so symbolic links are deliberately not
 * support.
 */
public class StaticResource extends Route {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResource.class);
    private static final boolean DEBUG = true;
    private final File localRoot;
    private final String remoteRoot;
    private final MimetypesFileTypeMap mimeMap;

    private StaticResource(File localRoot, String remoteRoot, MimetypesFileTypeMap mimeMap, String path) {
        super(path);
        this.localRoot = localRoot;
        this.remoteRoot = remoteRoot;
        this.mimeMap = mimeMap;
    }


    public StaticResource(String remoteRoot, String localPath, String path) throws IOException {
        super(remoteRoot + "/" + path);
        this.localRoot = new File(localPath).getCanonicalFile();
        this.remoteRoot = remoteRoot;
        mimeMap = new MimetypesFileTypeMap("src/main/resources/mime.types");
    }

    @Override
    public String handle(Request request, Response response) {
        try {
            String resourcePath = request.pathInfo().substring(remoteRoot.length());
            final File requestedFile = new File(localRoot, resourcePath).getCanonicalFile();
            // Perform basic file access checks
            if (!requestedFile.exists()) {
                final String msg = "Could not find static resource: " + request.pathInfo();
                halt(HttpStatus.Not_Found.code(), HttpStatus.Not_Found.toHtmlString(msg));
                return "";
            } else if (!requestedFile.canRead() || requestedFile.isHidden()) {
                halt(HttpStatus.Forbidden.code(), HttpStatus.Forbidden.toHtmlString(""));
                return "";
            } else if (!requestedFile.isFile()) {
                halt(HttpStatus.Bad_Request.code(), HttpStatus.Bad_Request.toHtmlString("The resquested static resource is not a file."));
                return "";
            }
            // Insure that the requested resource is a direct descendent of the
            // local path (i.e local path is an ancestoral directory.)
            if (!isParentOf(localRoot, requestedFile)) {
                halt(HttpStatus.Forbidden.code(), HttpStatus.Forbidden.toHtmlString(""));
                return "";
            }
            final String mime = mimeMap.getContentType(requestedFile.getName());
            LOG.debug("Serving " + mime + ": " + requestedFile);
            response.raw().setContentType(mime);
            response.raw().setContentLength((int) requestedFile.length());
            response.raw().setDateHeader("Last-Modified", requestedFile.lastModified());
            response.status(HttpStatus.OK.code());
            InputStream in = null;
            try {
                in = new FileInputStream(requestedFile);
                OutputStream out = response.raw().getOutputStream();
                ByteStreams.copy(in, out);
                out.flush();
            } finally {
                Closeables.close(in, true);
            }
            return "";
        } catch (Exception ex) {
            LOG.error(ex.toString() + "\n" + Throwables.getStackTraceAsString(ex));
            Throwables.propagateIfInstanceOf(ex, RuntimeException.class);
            handleException(ex, response);
            return "";
        }
    }

    boolean isParentOf(final File ancestor, final File descendence) throws IOException {
        Preconditions.checkNotNull(ancestor, "ancestor");
        Preconditions.checkNotNull(descendence, "descendence");
        final File cAncestor = ancestor.getCanonicalFile();
        File file = descendence.getCanonicalFile();
        while (file != null && !file.equals(cAncestor)) {
            file = file.getParentFile();
        }
        return file != null;
    }

    protected void handleException(Throwable ex, Response response) {
        LOG.error(ex.toString() + System.getProperty("line.separator") + Throwables.getStackTraceAsString(ex));
        MessageFormat frmt = new MessageFormat("<strong>{0}</strong><br/><pre>{1}</pre>");
        final String message = DEBUG ? frmt.format(new Object[]{ex.toString(), Throwables.getStackTraceAsString(ex)}) : "";
        response.status(HttpStatus.Internal_Server_Error.code());
        response.body(HttpStatus.Internal_Server_Error.toHtmlString(message));
        halt();
    }
}
