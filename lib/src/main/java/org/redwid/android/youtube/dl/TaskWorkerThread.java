package org.redwid.android.youtube.dl;

import android.content.Context;

import org.redwid.android.youtube.dl.unpack.UnpackTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class TaskWorkerThread extends Thread {

    public interface TaskWorkerThreadListener {

        void onCompleteAllItems();
    }

    private final List<String> list = Collections.synchronizedList(new ArrayList<String>());

    private Context context;
    private UnpackTask unpackTask;

    private boolean active = true;
    private Object taskInProgress = new Object();

    public TaskWorkerThread(final Context context) {
        this.context = context;
        start();
    }

    public void run() {
        while (active) {
            if (!list.isEmpty()) {

                final String stringUrl = list.get(0);
                Timber.i("performTask(), stringUrl: %s", stringUrl);

                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        performTask(stringUrl);
                        wakeUpTaskInProgress();
                    }
                });
                thread.start();

                sleepTaskInProgress();
            }
            else {
                Timber.i("run() sleep");
                sleep();
            }
        }
    }

    private void sleepTaskInProgress() {
        Timber.i("sleepTaskInProgress() begin");
        synchronized (taskInProgress) {
            try {
                taskInProgress.wait(10000);
            } catch (InterruptedException e) {
                Timber.e(e, "ERROR in sleepTaskInProgress() interrupted");
            }
        }
        Timber.i("sleepTaskInProgress() done");
    }

    private void wakeUpTaskInProgress() {
        Timber.i("wakeUpTaskInProgress()");
        synchronized (taskInProgress) {
            taskInProgress.notify();
        }
    }

    public void cancel() {
        active = false;
    }

    public void add(final String valueUrl) {
        Timber.i("add(), list: %d", list.size());
        if(!list.contains(valueUrl)) {
            list.add(valueUrl);
        }
        wakeUp();
    }

    public synchronized void wakeUp() {
        Timber.i("wakeUp()");
        notify();
    }

    private synchronized void sleep() {
        try {
            Timber.i("sleep() begin ...");
            wait();
            Timber.i("sleep() done ...");
        } catch (InterruptedException e) {
            Timber.e(e,"ERROR in sleep()");
        }
    }

    private void performTask(final String stringUrl) {
        Timber.i("performTask()");
        try {
            if (unpackTask == null) {
                unpackTask = new UnpackTask();
                if (!unpackTask.unpack(context.getApplicationContext())) {
                    unpackTask = null;
                }
            }

            final YoutubeDlWorker youtubeDlWorker = new YoutubeDlWorker();
            if (youtubeDlWorker.process(context.getApplicationContext(), stringUrl)) {
                Timber.i("performTask(), success");
            }
            list.remove(stringUrl);
            if (list.isEmpty()) {
                Timber.i("performTask(), list.isEmpty()");
                if (context instanceof TaskWorkerThreadListener) {
                    Timber.i("performTask(), context instanceof TaskWorkerThreadListener");
                    ((TaskWorkerThreadListener) context).onCompleteAllItems();
                }
            }
        }
        catch (Exception e) {
            Timber.e(e, "ERROR in performTask(%s)", stringUrl);
        }
    }
}
