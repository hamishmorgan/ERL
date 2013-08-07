/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author hamish
 */
public class XMLToStringSerializer extends nu.xom.Serializer {

    private boolean xmlDeclarationSkipped = false;

    public XMLToStringSerializer(OutputStream out) {
        super(out);
    }

    public XMLToStringSerializer(OutputStream out, String encoding) throws UnsupportedEncodingException {
        super(out, encoding);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isXmlDeclarationSkipped() {
        return xmlDeclarationSkipped;
    }

    public void setXmlDeclarationSkipped(boolean xmlDeclarationSkipped) {
        this.xmlDeclarationSkipped = xmlDeclarationSkipped;
    }

    @Override
    protected void writeXMLDeclaration() throws IOException {
        if (!isXmlDeclarationSkipped()) {
            super.writeXMLDeclaration();
        }
    }
}
