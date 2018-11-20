package org.redwid.android.youtube.dl.app.model;

import com.google.gson.JsonObject;

import org.redwid.android.youtube.dl.app.utils.JsonHelper;

import androidx.annotation.NonNull;

/**
 * The Format class.
 */
public class Format implements Comparable<Format> {

    String name;
    String url;
    String vcodec;
    String acodec;
    String width;
    String height;
    String ext;
    String fileSize;
    int formatId;

    public Format(final JsonObject item) {
        name = JsonHelper.getAsString(item, "format");
        url = JsonHelper.getAsString(item, "url");
        acodec = JsonHelper.getAsString(item, "acodec");
        vcodec = JsonHelper.getAsString(item, "vcodec");
        width = JsonHelper.getAsString(item, "width");
        height = JsonHelper.getAsString(item, "height");
        ext = JsonHelper.getAsString(item, "ext");
        fileSize = JsonHelper.getAsString(item, "filesize");
        formatId = JsonHelper.getAsInt(item, "format_id");
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVcodec() {
        return vcodec;
    }

    public String getAcodec() {
        return acodec;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getExt() {
        return ext;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getFormatId() {
        return formatId;
    }

    @Override
    public int compareTo(@NonNull final Format o) {
        return formatId - o.getFormatId();
    }
}
