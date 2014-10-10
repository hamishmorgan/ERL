package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;
import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 10/04/2013
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public class SimpleLinkRequest extends GenericJson {

    @Nullable
    @com.google.api.client.util.Key
    public String text = null;

    public SimpleLinkRequest() {
    }

    public SimpleLinkRequest(String text) {
        this.text = text;
    }

    @Nullable
    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = text;
    }

}
