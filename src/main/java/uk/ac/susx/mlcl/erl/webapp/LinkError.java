package uk.ac.susx.mlcl.erl.webapp;

import com.google.api.client.json.GenericJson;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 10/04/2013
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public class LinkError extends GenericJson {

    @com.google.api.client.util.Key
    private int code;
    @Nullable
    @com.google.api.client.util.Key
    private String name;
    @Nullable
    @com.google.api.client.util.Key
    private String message;

    public LinkError(int code, String name, String message) {
        this.code = code;
        this.name = name;
        this.message = message;
    }

    public LinkError() {
        this.code = 0;
        this.name = null;
        this.message = null;
    }

    public final int getCode() {
        return code;
    }

    public final void setCode(int code) {
        this.code = code;
    }

    @Nullable
    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    @Nullable
    public final String getMessage() {
        return message;
    }

    public final void setMessage(String message) {
        this.message = message;
    }
}
