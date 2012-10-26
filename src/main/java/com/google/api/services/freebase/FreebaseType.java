/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package com.google.api.services.freebase;

import com.google.api.client.json.GenericJson;
import javax.annotation.Nullable;

/**
 *
 * @author hamish
 */
@Nullable
public class FreebaseType extends GenericJson {

    @com.google.api.client.util.Key
    private String id;

    @com.google.api.client.util.Key
    private String name;

    public FreebaseType() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
