/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package io.github.hamishmorgan.erl.snlp.annotators;

import java.util.Properties;

/**
 * Marks a class as being capable of loading it's configuration from a properties object.
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
