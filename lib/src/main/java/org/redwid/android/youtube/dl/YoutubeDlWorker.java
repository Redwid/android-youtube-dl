package org.redwid.android.youtube.dl;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import androidx.work.Worker;
import timber.log.Timber;

import static org.redwid.android.youtube.dl.YoutubeDlService.JSON_RESULT_ERROR;
import static org.redwid.android.youtube.dl.YoutubeDlService.JSON_RESULT_SUCCESS;
import static org.redwid.android.youtube.dl.YoutubeDlService.VALUE_JSON;
import static org.redwid.android.youtube.dl.YoutubeDlService.VALUE_URL;

/**
 * The YoutubeDlWorker class.
 */
public class YoutubeDlWorker extends Worker {

    @NonNull
    @Override
    public Result doWork() {
        Timber.i("doWork()");
        final Context applicationContext = getApplicationContext();
        final String stringUrl = getInputData().getString(VALUE_URL);
        final String cacheDir = applicationContext.getCacheDir().getAbsolutePath();
        final String applicationRootDir = applicationContext.getFilesDir().getAbsolutePath();
        final String pythonApplicationRootDir = applicationRootDir + "/youtube_dl";

        final String applicationArguments[] = new String[] { "app_process",
                "-j", stringUrl,
                "--cache-dir", cacheDir};

        final File dlDoneFile = new File(pythonApplicationRootDir, "youtube_dl.done");
        final File dlJsonFile = new File(pythonApplicationRootDir, "youtube_dl.json");
        if(dlDoneFile.exists()) {
            dlDoneFile.delete();
        }
        if(dlJsonFile.exists()) {
            dlJsonFile.delete();
        }
        final long startTime = System.currentTimeMillis();
        Timber.i("nativeStart() begin");
        try {
            YoutubeDlService.nativeStart(applicationRootDir,
                    pythonApplicationRootDir,
                    "main.pyo",
                    "python2.7",
                    pythonApplicationRootDir,
                    pythonApplicationRootDir + ":" + pythonApplicationRootDir + "/lib",
                    applicationArguments);
        } catch(Throwable t) {
            Timber.e(t, "Exception in nativeStart()");
            broadcastFinishError(applicationContext, t, stringUrl);
            return Result.FAILURE;
        }
        Timber.i("doWork(), nativeStart() end, time: %dms", System.currentTimeMillis() - startTime);

        Timber.i("doWork(), dlDoneFile.exists(): %b", dlDoneFile.exists());
        Timber.i("doWork(), dlJsonFile.exists(): %b", dlJsonFile.exists());
        if(dlJsonFile.exists()) {
            broadcastFinishSuccess(applicationContext, dlJsonFile, stringUrl);
        }
        else {
            broadcastFinishError(applicationContext, dlDoneFile, stringUrl);
            return Result.FAILURE;
        }
        Timber.i("nativeStart() end");
        return Result.SUCCESS;
    }

    private void broadcastFinishSuccess(final Context context, final File file, final String url) {
        Timber.i("broadcastFinishSuccess()");
        final StringBuilder stringBuilder = readFile(file);
        if(stringBuilder.length() != 0) {
            sendBroadcast(context, url, JSON_RESULT_SUCCESS, stringBuilder);
        }
        else {
            broadcastFinishError(context, new IOException("Unable to read file: " + file.getName()), url);
        }
    }

    private void broadcastFinishError(final Context context, final File file, final String url) {
        Timber.i("broadcastFinishError()");
        final StringBuilder stringBuilder = readFile(file);
        if(stringBuilder.length() != 0) {
            sendBroadcast(context, url, JSON_RESULT_ERROR, stringBuilder);
        }
    }

    private void broadcastFinishError(final Context context, final Throwable throwable, final String url) {
        Timber.i("broadcastFinishError()");
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"result\":\"");
        stringBuilder.append(throwable);
        stringBuilder.append("\"}");
        if(stringBuilder.length() != 0) {
            sendBroadcast(context, url, JSON_RESULT_ERROR, stringBuilder);
        }
    }

    private void sendBroadcast(final Context context, final String url, final String action, final StringBuilder stringBuilder) {
        try {
            final Intent jsonIntent = new Intent(action);
            jsonIntent.putExtra(VALUE_URL, url);
            jsonIntent.putExtra(VALUE_JSON, stringBuilder.toString());
            context.sendBroadcast(jsonIntent);
        } catch(Throwable t) {
            Timber.e(t, "Exception in broadcastFinishError()");
        }
    }

    private StringBuilder readFile(final File file) {
        Timber.i("readFile(), file: %s", file);
        final StringBuilder stringBuilder = new StringBuilder();
        if(file != null && file.exists()) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                Timber.e(e, "Exception in readFile()");
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            stringBuilder.append("{\"result\":\"");
            stringBuilder.append("Error file is not exist: ").append(file.getAbsolutePath());
            stringBuilder.append("\"}");
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append("{\"result\":\"");
            stringBuilder.append("Unknown error");
            stringBuilder.append("\"}");
        }

        Timber.i("readFile(), stringBuilder: %s", stringBuilder);
        return stringBuilder;
    }
}
