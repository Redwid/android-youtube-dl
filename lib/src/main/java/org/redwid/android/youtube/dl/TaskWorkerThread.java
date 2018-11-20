package org.redwid.android.youtube.dl;

import android.content.Context;

import org.redwid.android.youtube.dl.unpack.UnpackTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class TaskWorkerThread extends Thread {

    public interface TaskWorkerThreadListener {

        public void onCompleteAllItems();
    }

    private final List<String> list = Collections.synchronizedList(new ArrayList<String>());

    private Context context;
    private UnpackTask unpackTask;

    private boolean active = true;

    public TaskWorkerThread(final Context context) {
        this.context = context;
        start();
    }

    public void run() {
        while (active) {
            if (!list.isEmpty()) {
                try {
                    performTask();
                }
                catch (Exception e) {
                    Timber.e(e, "ERROR in run()");
                }
            }
            else {
                Timber.i("run() sleep");
                sleep();
            }
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
        } catch (InterruptedException e) {
            Timber.i("sleep() end ...");
        }
    }

    private void performTask() {
        Timber.i("performTask()");

        if(unpackTask == null) {
            unpackTask = new UnpackTask();
            if(!unpackTask.unpack(context.getApplicationContext())) {
                unpackTask = null;
            }
        }

        final String stringUrl = list.get(0);
        Timber.i("performTask(), stringUrl: %s", stringUrl);

        final YoutubeDlWorker youtubeDlWorker = new YoutubeDlWorker();
        if(youtubeDlWorker.process(context.getApplicationContext(), stringUrl)) {
            Timber.i("performTask(), success");
        }
        list.remove(0);
        if(list.isEmpty()) {
            Timber.i("performTask(), list.isEmpty()");
            if(context instanceof TaskWorkerThreadListener) {
                Timber.i("performTask(), context instanceof TaskWorkerThreadListener");
                ((TaskWorkerThreadListener)context).onCompleteAllItems();
            }
        }
    }
}
