package org.redwid.android.youtube.dl.harness;

import android.app.Application;

import org.redwid.youtube.dl.android.BuildConfig;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

/**
 * The MainApplication class.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        setupTimber();
        Timber.i("onCreate()");
    }

    private void setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //Timber.plant(new CrashlyticsTree());
        }
    }
}
