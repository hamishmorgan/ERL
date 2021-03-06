/*
 * Copyright (c) 2012, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.webapp;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

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

    @Nullable
    @Override
    public Object handle(@Nonnull Request request, @Nonnull Response response) {
        LOG.debug("Redirecting from \"{}\" to \"{}\".", request.pathInfo(), destination);
        response.redirect(destination);
        return null;
    }
}
