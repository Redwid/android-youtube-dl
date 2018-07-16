/**
 * This class takes care of managing resources for us. In our code, we
 * can't use R, since the name of the package containing R will
 * change. (This same code is used in both org.renpy.android and
 * org.renpy.pygame.) So this is the next best thing.
 */

package org.redwid.android.youtube.dl.unpack;

import android.content.Context;
import android.content.res.Resources;

import timber.log.Timber;

public class ResourceManager {

    private Context context;
    private Resources resources;

    public ResourceManager(Context context) {
        this.context = context;
        this.resources = context.getResources();
    }

    public int getIdentifier(String name, String kind) {
        Timber.i("getIdentifier(%s, %s)", name, kind);
        final int value = resources.getIdentifier(name, kind, context.getPackageName());
        Timber.i("getIdentifier(), value: %d", value);
        return value;
    }

    public String getString(String name) {
        try {
            Timber.i("getString(%s)", name);
            return resources.getString(getIdentifier(name, "string"));
        } catch (Exception e) {
            Timber.e(e, "Exception in getString(%s)", name);
            return null;
        }
    }
}
