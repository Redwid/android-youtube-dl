package org.redwid.android.youtube.dl.unpack;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import timber.log.Timber;

/**
 * The UnpackFilesTask class.
 */
public class UnpackFilesTask extends AsyncTask<Void, Void, Void> {

    private Context context = null;
    private ResourceManager resourceManager = null;
    private AssetExtract assetExtract = null;
    private UnpackFilesTaskCallback unpackFilesTaskCallback;

    private final Handler handler = new Handler();

    public UnpackFilesTask(final Context context, final UnpackFilesTaskCallback unpackFilesTaskCallback) {
        Timber.i("UnpackFilesTask <init>");
        this.context = context;
        this.resourceManager = new ResourceManager(context);
        this.assetExtract = new AssetExtract(context);
        this.unpackFilesTaskCallback = unpackFilesTaskCallback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Timber.i( "Ready to unpack");
        unpackData("private", new File(getAppRoot()));
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        unpackFilesTaskCallback.onPostExecute();
    }

    public String getAppRoot() {
        String app_root = context.getFilesDir().getAbsolutePath() + "/youtube_dl";
        return app_root;
    }

    public void unpackData(final String resource, File target) {
        Timber.i( "unpackData(%s, %s)", resource, target.getName());

        // The version of data in memory and on disk.
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        Timber.i( "Data version is %s", data_version);

        // If no version, no unpacking is necessary.
        if (data_version == null) {
            return;
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
            Timber.i( "Extracting %s assets.", resource);

            recursiveDelete(target);
            target.mkdirs();

            if (!assetExtract.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                toastError("Could not extract " + resource + " data.");
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
            }
        }
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    /**
     * Show an error using a toast. (Only makes sense from non-UI
     * threads.)
     */
    public void toastError(final String msg) {

        handler.post(new Runnable () {
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });

        // Wait to show the error.
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
