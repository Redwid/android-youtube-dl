package org.redwid.android.youtube.dl.unpack;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.work.Worker;
import timber.log.Timber;

/**
 * The UnpackWorker class.
 */
public class UnpackWorker extends Worker {

    public static final String PRIVATE = "private";
    public static final String YOUTUBE_DL = "youtube_dl";

    @NonNull
    @Override
    public WorkerResult doWork() {
        final Context applicationContext = getApplicationContext();
        final WorkerResult workerResult = unpackData(PRIVATE, getAppRootFile(applicationContext), applicationContext);
        return workerResult;
    }

    private File getAppRootFile(final Context context) {
        final String app_root = context.getFilesDir().getAbsolutePath() + "/" + YOUTUBE_DL;
        return new File(app_root);
    }

    private WorkerResult unpackData(final String resource, final File target, final Context applicationContext) {
        Timber.i( "unpackData(%s, %s)", resource, target.getName());
        final long time = System.currentTimeMillis();

        final ResourceManager resourceManager = new ResourceManager(applicationContext);
        final AssetExtract assetExtract = new AssetExtract(applicationContext);

        // The version of data in memory and on disk.
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        Timber.i( "Data version is %s", data_version);

        // If no version, no unpacking is necessary.
        if (data_version == null) {
            return WorkerResult.SUCCESS;
        }

        // Check the current disk version, if any.
        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(disk_version_fn);
            int len = is.read(buf);
            disk_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            disk_version = "";
        }

        // If the disk data is out of date, extract it and write the
        // version file.
        // if (! data_version.equals(disk_version)) {
        if (! data_version.equals(disk_version)) {
            Timber.i( "Extracting %s assets", resource);

            recursiveDelete(target);
            target.mkdirs();

            if (!assetExtract.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                //toastError("Could not extract " + resource + " data.");
                Timber.e( "Could not extract %s data", resource);
            }

            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Timber.e(e, "Exception in unpackData()");
                return WorkerResult.FAILURE;
            }
        }
        Timber.i( "unpackData() done, t: %dms", System.currentTimeMillis() - time);
        return WorkerResult.SUCCESS;
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }
}
