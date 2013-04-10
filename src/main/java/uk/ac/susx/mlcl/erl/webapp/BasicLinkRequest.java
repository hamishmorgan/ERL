package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 10/04/2013
* Time: 10:56
* To change this template use File | Settings | File Templates.
*/
public class BasicLinkRequest extends GenericJson {

    @com.google.api.client.util.Key
    public String text = null;

    public BasicLinkRequest() {
    }

    public BasicLinkRequest(String text) {
        this.text = text;
    }

    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = text;
    }
}
