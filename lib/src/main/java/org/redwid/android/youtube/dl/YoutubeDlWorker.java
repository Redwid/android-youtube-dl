package org.redwid.android.youtube.dl;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import timber.log.Timber;

import static org.redwid.android.youtube.dl.YoutubeDlService.JSON_RESULT_ERROR;
import static org.redwid.android.youtube.dl.YoutubeDlService.JSON_RESULT_SUCCESS;
import static org.redwid.android.youtube.dl.YoutubeDlService.VALUE_JSON;
import static org.redwid.android.youtube.dl.YoutubeDlService.VALUE_URL;

/**
 * The YoutubeDlWorker class.
 */
public class YoutubeDlWorker {

    public boolean process(final Context context, final String stringUrl) {
        Timber.i("process(%s)", this.hashCode());

        try {
            final long time = System.currentTimeMillis();
            final String cacheDir = context.getCacheDir().getAbsolutePath();
            final String applicationRootDir = context.getFilesDir().getAbsolutePath();
            final String pythonApplicationRootDir = applicationRootDir + "/youtube_dl";
            Timber.i("process(%s), stringUrl: %s", this.hashCode(), stringUrl);

            final String applicationArguments[] = new String[]{"app_process",
                    "-j", stringUrl,
                    "--cache-dir", cacheDir};

            final File dlDoneFile = new File(pythonApplicationRootDir, "youtube_dl.done");
            final File dlJsonFile = new File(pythonApplicationRootDir, "youtube_dl.json");
            if (dlDoneFile.exists()) {
                dlDoneFile.delete();
            }
            if (dlJsonFile.exists()) {
                dlJsonFile.delete();
            }
            loadNativeLibrary();
            final long startTime = System.currentTimeMillis();
            try {
                Timber.i("process(%s), nativeStart() begin", this.hashCode());
                nativeStart(applicationRootDir,
                        pythonApplicationRootDir,
                        "main.pyo",
                        "python2.7",
                        pythonApplicationRootDir,
                        pythonApplicationRootDir + ":" + pythonApplicationRootDir + "/lib",
                        applicationArguments);
            } catch (Throwable t) {
                Timber.e(t, "Exception in nativeStart()");
                broadcastFinishError(context, t, stringUrl);
                return false;
            }
            Timber.i("process(%s), nativeStart() end, time: %dms", this.hashCode(), System.currentTimeMillis() - startTime);

            Timber.i("process(%s), dlDoneFile.exists(): %b, dlJsonFile.exists(): %b", this.hashCode(), dlDoneFile.exists(), dlJsonFile.exists());
            if (dlJsonFile.exists()) {
                broadcastFinishSuccess(context, dlJsonFile, stringUrl);
            } else {
                broadcastFinishError(context, dlDoneFile, stringUrl);
                return false;
            }
            Timber.i("process(%s) end, t: %dms", this.hashCode(), System.currentTimeMillis() - time);
        } catch( Exception e) {
            Timber.e(e, "process(%s) InterruptedException", this.hashCode());
            return false;
        }
        return false;
    }

    private void broadcastFinishSuccess(final Context context, final File file, final String url) {
        //Timber.i("broadcastFinishSuccess()");
        final StringBuilder stringBuilder = readFile(file);
        if(stringBuilder.length() != 0) {
            sendBroadcast(context, url, JSON_RESULT_SUCCESS, stringBuilder);
        }
        else {
            broadcastFinishError(context, new IOException("Unable to read file: " + file.getName()), url);
        }
    }

    private void broadcastFinishError(final Context context, final File file, final String url) {
        //Timber.i("broadcastFinishError()");
        final StringBuilder stringBuilder = readFile(file);
        if(stringBuilder.length() != 0) {
            sendBroadcast(context, url, JSON_RESULT_ERROR, stringBuilder);
        }
    }

    private void broadcastFinishError(final Context context, final Throwable throwable, final String url) {
        //Timber.i("broadcastFinishError()");
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
        //Timber.i("readFile(), file: %s", file);
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

        //Timber.i("readFile(), stringBuilder: %s", stringBuilder);
        return stringBuilder;
    }

    private void loadNativeLibrary() {
        Timber.i("loadNativeLibrary(%s)", this.hashCode());
        final String main = "main";
        try {
            System.loadLibrary(main);
            Timber.i("loadNativeLibrary(%s), loaded: lib%s.so", this.hashCode(), main);
        } catch(UnsatisfiedLinkError e) {
            Timber.e(e, "UnsatisfiedLinkError in loadNativeLibrary(), can't load: %s", main);
        } catch(Exception e) {
            Timber.e(e, "Exception in loadNativeLibrary(), can't load: %s", main);
        }
    }

    // Native part
    public static native void nativeStart(String androidPrivate,
                                          String androidArgument,
                                          String applicationEntrypoint,
                                          String pythonName,
                                          String pythonHome,
                                          String pythonPath,
                                          String applicationArguments[]);
}
