package org.redwid.android.youtube.dl.app.utils;

import com.google.gson.JsonObject;

/**
 * The JsonHelper class.
 */
public final class JsonHelper {

    public static String getAsString(final JsonObject item, final String key) {
        return getAsString(item, key, "");
    }

    public static String getAsString(final JsonObject item, final String key, final String defaultValue) {
        if(item != null && item.has(key)) {
            try {
                return item.get(key).getAsString();
            } catch(UnsupportedOperationException ignore) {

            }
        }
        return defaultValue;
    }

    public static long getAsLong(final JsonObject item, final String key) {
        if(item != null && item.has(key)) {
            try {
            return item.get(key).getAsLong();
            } catch(UnsupportedOperationException ignore) {

            }
        }
        return 0;
    }

    public static int getAsInt(final JsonObject item, final String key) {
        if(item != null && item.has(key)) {
            try {
            return item.get(key).getAsInt();
            } catch(UnsupportedOperationException ignore) {

            }
        }
        return 0;
    }
}
