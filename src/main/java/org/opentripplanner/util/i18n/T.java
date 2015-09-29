package org.opentripplanner.util.i18n;

import org.opentripplanner.util.LocalizedString;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is used so that translation extraction extracts strings to po files.
 *
 * And that string context can be used in {@link LocalizedString}.
 *
 * Created by mabu on 4.8.2015.
 */
public final class T implements Serializable {

    public final String msgid;
    public final String msgctx;

    private T(String msgctx, String msg) {
        this.msgctx = msgctx;
        this.msgid = msg;
    }

    public static T tr(String msg) {
        return new T(null, msg);
    }

    public static T trc(String ctx, String msg) {
        return new T(ctx, msg);
    }

    /**
     * Basically same method as {@link #tr(String)} only it doesn't get extracted when translations are extracted.
     *
     * It is used in tests
     * @param msg
     * @return
     */
    public static T tt(String msg) {
        return T.tr(msg);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        T t = (T) o;
        return Objects.equals(msgid, t.msgid) && Objects.equals(msgctx, t.msgctx);
    }

    @Override public int hashCode() {
        return Objects.hash(msgid, msgctx);
    }
}
