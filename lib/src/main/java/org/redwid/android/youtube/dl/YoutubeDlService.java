package org.redwid.android.youtube.dl;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.redwid.android.youtube.dl.TaskWorkerThread.TaskWorkerThreadListener;
import org.redwid.youtube.dl.android.R;

import timber.log.Timber;

/**
 * Replace service to worker
 * https://medium.com/google-developer-experts/services-the-life-with-without-and-worker-6933111d62a6
 */
public class YoutubeDlService extends Service implements TaskWorkerThreadListener {

    public static final String ACTION_DUMP_JSON =    "org.redwid.android.youtube.dl.action.DUMP_JSON";
    public static final String JSON_RESULT_SUCCESS = "org.redwid.android.youtube.dl.result.JSON_RESULT_SUCCESS";
    public static final String JSON_RESULT_ERROR =   "org.redwid.android.youtube.dl.result.JSON_RESULT_ERROR";
    public static final String VALUE_JSON = "JSON";
    public static final String VALUE_URL = "URL";
    public static final String VALUE_TIME_OUT = "TIME_OUT";

    public static final String NOTIFICATION_CHANNEL_ID = "youtube-dl-service";
    public static final int NOTIFICATION_ID = 111;

    private TaskWorkerThread taskWorkerThread;
    private NotificationManager notificationManager;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public YoutubeDlService getService()
        {
            return YoutubeDlService.this;
        }
    }

    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate()");
        notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        startForegroundIfNeeded();
        taskWorkerThread = new TaskWorkerThread(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Timber.i("onStart(%s, %d)", intent, startId);
        if(intent != null) {
            final String action = intent.getAction();
            final String valueUrl = intent.getStringExtra(VALUE_URL);
            Timber.i("onStart(), action: %s, valueUrl: %s", action, valueUrl);
            if (ACTION_DUMP_JSON.equals(action)) {
                taskWorkerThread.add(valueUrl);
            }
        } else {
            Timber.e("onStart() intent is null");
        }
    }

    public Notification getNotification(String title, String text) {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .build();
    }

    private void startForegroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[]{0L});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);

            startForeground(NOTIFICATION_ID, getNotification("", ""));
        }
    }

    @Override
    public void onDestroy() {
        Timber.i("onDestroy()");
        taskWorkerThread.cancel();
        taskWorkerThread = null;
        super.onDestroy();
    }

    @Override
    public void onCompleteAllItems() {
        Timber.i("onCompleteAllItems()");
        stopSelf();
    }

}
