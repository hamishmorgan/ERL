/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import java.util.Properties;

/**
 * Marks a class as being capable of loading it's configuration from a properties object.
 *
 * @author Hamish Morgan
 */
public interface Configurable {

    /**
     * Configure (or re-configure) this object with the given properties.
     * <p/>
     *
     * @param props configuration to load
     */
    void configure(Properties props);
}
