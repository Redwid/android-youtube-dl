package org.redwid.android.youtube.dl.unpack;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import timber.log.Timber;

public class GZIPUtils {

    public static final String UTF_8 = "UTF-8";

    public static byte[] compress(final String stringToCompress) {
        final long time = System.currentTimeMillis();
        byte[] result = new byte[0];
        if (!TextUtils.isEmpty(stringToCompress)) {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final GZIPOutputStream gzipOutput = new GZIPOutputStream(baos);
                gzipOutput.write(stringToCompress.getBytes(UTF_8));
                gzipOutput.finish();
                result = baos.toByteArray();
            } catch (Exception e) {
                Timber.e(e, "ERROR in compress()");
            }
        }
        Timber.d("compress(), t: %dms", System.currentTimeMillis() - time);
        return result;
    }

    public static String uncompress(final byte[] compressed) {
        final long time = System.currentTimeMillis();
        final StringBuilder stringBuilder = new StringBuilder();
        if (compressed != null && compressed.length > 0) {

            if (isCompressed(compressed)) {
                try {
                    final GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(compressed));
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipInput, UTF_8));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    gzipInput.close();
                } catch (Exception e) {
                    Timber.e(e, "ERROR in uncompress()");
                }
            } else {
                stringBuilder.append(compressed);
            }
        }
        Timber.d("uncompress(), t: %dms", System.currentTimeMillis() - time);
        return stringBuilder.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
