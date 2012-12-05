/*
 * Copyright (c) 2012, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import static com.google.common.base.Preconditions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A route which redirects all requests to a given destination location.
 *
 * @author Hamish Morgan
 */
public class Redirect extends Route {

    private static final Logger LOG = LoggerFactory.getLogger(Redirect.class);
    private final String destination;

    public Redirect(String path, String destination) {
        super(path);
        checkNotNull(destination);
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public Object handle(Request request, Response response) {
        LOG.debug("Ridirecting from \"{}\" to \"{}\".", request.pathInfo(), destination);
        response.redirect(destination);
        return null;
    }
}
