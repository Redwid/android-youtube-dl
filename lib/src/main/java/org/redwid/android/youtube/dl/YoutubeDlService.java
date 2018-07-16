package org.redwid.android.youtube.dl;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.redwid.android.youtube.dl.unpack.UnpackWorker;
import org.redwid.youtube.dl.android.R;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import timber.log.Timber;

/**
 * Replace service to worker
 * https://medium.com/google-developer-experts/services-the-life-with-without-and-worker-6933111d62a6
 */
public class YoutubeDlService extends IntentService {

    public static final String ACTION_DUMP_JSON =    "org.redwid.android.youtube.dl.action.DUMP_JSON";
    public static final String JSON_RESULT_SUCCESS = "org.redwid.android.youtube.dl.result.JSON_RESULT_SUCCESS";
    public static final String JSON_RESULT_ERROR =   "org.redwid.android.youtube.dl.result.JSON_RESULT_ERROR";
    public static final String VALUE_JSON = "JSON";
    public static final String VALUE_URL = "URL";
    public static final String VALUE_TIME_OUT = "TIME_OUT";

    public static final String NOTIFICATION_CHANNEL_ID = "youtube-dl-service";

    public static final String UNPACK_WORK = "UNPACK_WORK";

    public YoutubeDlService() {
        super("YoutubeDlService");
        Timber.i("<init>");
        loadLibrary();
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        Timber.i("onHandleIntent(%s)", intent);
        final String action = intent.getAction();
        Timber.i("onHandleIntent(), action: %s", action);
        if (ACTION_DUMP_JSON.equals(action)) {
            final WorkManager workManager = WorkManager.getInstance();
            WorkContinuation continuation = workManager.beginUniqueWork(UNPACK_WORK,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequest.from(UnpackWorker.class));

            final String value = intent.getStringExtra(VALUE_URL);
            final OneTimeWorkRequest youtubeDl = new OneTimeWorkRequest.Builder(YoutubeDlWorker.class)
                    .setInputData(getDataInput(value))
                    .addTag(value)
                    .build();

            continuation = continuation.then(youtubeDl);
            continuation.enqueue();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate()");
        //startForegroundIfNeeded();
    }

    public Data getDataInput(final String stringExtra) {
        Data.Builder builder = new Data.Builder();
        builder.putString(VALUE_URL, stringExtra);
        return builder.build();
    }

    private void startForegroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        Timber.i("onDestroy()");
        super.onDestroy();
    }

    private void loadLibrary() {
        Timber.i("loadLibrary()");
        final String main = "main";
        try {
            System.loadLibrary(main);
            Timber.i("loadLibrary(), loaded: lib%s.so", main);
        } catch(UnsatisfiedLinkError e) {
            Timber.e(e, "UnsatisfiedLinkError in loadLibrary(), can't load: %s", main);
        } catch(Exception e) {
            Timber.e(e, "Exception in loadLibrary(), can't load: %s", main);
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
