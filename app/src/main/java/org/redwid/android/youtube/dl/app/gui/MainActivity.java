package org.redwid.android.youtube.dl.app.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.redwid.android.youtube.dl.YoutubeDlService;
import org.redwid.android.youtube.dl.app.R;
import org.redwid.android.youtube.dl.app.model.Format;
import org.redwid.android.youtube.dl.app.utils.JsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static org.redwid.android.youtube.dl.YoutubeDlService.VALUE_URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TEXT_PLAIN = "text/plain";

    private String stringTitle;
    private String stringSubTitle;
    private String stringDescription;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Timber.i("onReceive(), action: %s", intent.getAction());
            final String jsonAsString = intent.getStringExtra(YoutubeDlService.VALUE_JSON);
            Timber.i("onReceive(): is jsonAsString empty: %s", TextUtils.isEmpty(jsonAsString));
            hideLoading();
            if(YoutubeDlService.JSON_RESULT_SUCCESS.equals(intent.getAction())) {
                final Gson gson = new Gson();
                final JsonObject jsonObject = gson.fromJson(jsonAsString, JsonObject.class);
                initData(jsonObject);
            }
            else
            if(YoutubeDlService.JSON_RESULT_ERROR.equals(intent.getAction())) {
                final Gson gson = new Gson();
                final JsonObject jsonObject = gson.fromJson(jsonAsString, JsonObject.class);
                initData(null);
                showError(jsonObject, intent.getStringExtra(VALUE_URL));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate(%s)", savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData(null);
        registerBroadcastReceiver();
        processSendIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel_all_works:
//                final WorkManager workManager = WorkManager.getInstance();
//                workManager.cancelAllWork();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        Timber.i("onNewIntent()");
        super.onNewIntent(intent);
        processSendIntent(intent);
    }

    @Override
    protected void onDestroy() {
        Timber.i("onDestroy()");
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void registerBroadcastReceiver() {
        Timber.i("registerBroadcastReceiver()");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(YoutubeDlService.JSON_RESULT_SUCCESS);
        intentFilter.addAction(YoutubeDlService.JSON_RESULT_ERROR);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void processSendIntent(final Intent intent) {
        Timber.d("processSendIntent()");
        final String action = intent.getAction();
        final String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (TEXT_PLAIN.equals(type)) {
                final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Timber.d("processSendIntent(), action: %s, type: %s, sharedText: %s", sharedText, action, type);

                final Intent serviceIntent = new Intent(YoutubeDlService.ACTION_DUMP_JSON);
                serviceIntent.setClass(this, YoutubeDlService.class);
                serviceIntent.putExtra(VALUE_URL, Uri.parse(sharedText).toString());
                serviceIntent.putExtra(YoutubeDlService.VALUE_TIME_OUT, 30000l);
                startService(serviceIntent);
                showLoading();
            }
        }
    }

    private void initData(final JsonObject jsonObject) {
        final View content = findViewById(R.id.content);
        final View empty = findViewById(R.id.empty);

        if(jsonObject == null) {
            content.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            return;
        }

        content.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        final String thumbnail = JsonHelper.getAsString(jsonObject,"thumbnail");
        final ImageView thumbnailView = findViewById(R.id.thumbnail);
        if(thumbnailView != null && !TextUtils.isEmpty(thumbnail)) {
            final RequestOptions options = new RequestOptions().
                    centerCrop().
                    diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(this).
                    load(thumbnail).
                    apply(options).
                    transition(withCrossFade()).
                    into(thumbnailView);
        }

        final long duration = JsonHelper.getAsLong(jsonObject,"duration");
        final TextView durationView = findViewById(R.id.duration);
        if(durationView != null) {
            durationView.setText(formatVideoFileDuration(duration));
        }

        stringTitle = JsonHelper.getAsString(jsonObject,"title");
        final TextView titleView = findViewById(R.id.title);
        if(titleView != null) {
            titleView.setText(stringTitle);
            titleView.setOnClickListener(this);
        }

        final String uploader = JsonHelper.getAsString(jsonObject,"uploader", "unknown");
        final String view_count = JsonHelper.getAsString(jsonObject,"view_count", "0");
        final String upload_date = JsonHelper.getAsString(jsonObject,"upload_date", "none");
        stringSubTitle = String.format("%s - %s views - %s", uploader, view_count,upload_date);

        final TextView sub_titleView = findViewById(R.id.sub_title);
        if(sub_titleView != null) {
            sub_titleView.setText(stringSubTitle);
            sub_titleView.setOnClickListener(this);
        }

        stringDescription = JsonHelper.getAsString(jsonObject,"description");
        final TextView descriptionView = findViewById(R.id.description);
        if(descriptionView != null) {
            descriptionView.setText(stringDescription);
            descriptionView.setOnClickListener(this);
        }

        final List<Format> formatList = parseFormats(jsonObject);
        if(!formatList.isEmpty()) {
            final RecyclerView recycler_view = findViewById(R.id.recycler_view);
            recycler_view.setLayoutManager(new LinearLayoutManager(this));
            if(recycler_view != null) {
                Collections.sort(formatList);
                recycler_view.setAdapter(new FormatAdapter(formatList, this));
            }
        }
    }

    public String formatVideoFileDuration(long timeInMillisec) {
        final String format = String.format("%%0%dd", 2);
        timeInMillisec = timeInMillisec / 1000;
        String seconds = String.format(format, timeInMillisec % 60);
        String minutes = String.format(format, (timeInMillisec % 3600) / 60);
        String hours = String.format(format, timeInMillisec / 3600);
        String time = "";

        if(hours.equals("00")) {
            time = minutes + ":" + seconds;
        }
        else {
            time = hours + ":" + minutes + ":" + seconds;
        }
        return time;
    }

    public List<Format> parseFormats(final JsonObject jsonObject) {
        final JsonArray array = jsonObject.getAsJsonArray("formats");
        if(array != null) {
            JsonObject item;
            final List<Format> formatList = new ArrayList<>();
            for (JsonElement jsonElement : array) {
                item = ((JsonObject) jsonElement);
                formatList.add(new Format(item));
            }
            return formatList;
        }
        return Collections.emptyList();
    }

    protected void showLoading() {
        changeLoadingVisibility(View.VISIBLE);
    }

    protected void hideLoading() {
        changeLoadingVisibility(View.GONE);
    }

    protected void changeLoadingVisibility(int visibility) {
        final View loading = findViewById(R.id.loading);
        if(loading != null) {
            loading.setVisibility(visibility);
        }
    }

    @Override
    public void onClick(final View v) {
        showPopUpDialog(stringTitle, stringSubTitle, stringDescription);
    }

    private void showError(final JsonObject jsonObject, final String stringUrl) {
        Timber.d("showError(%s)", jsonObject);
        showPopUpDialog(getString(R.string.error_title), getString(R.string.error_text, stringUrl), jsonObject.toString());
    }

    private void showPopUpDialog(final String stringTitle, final String stringSubTitle, final String stringDescription) {
        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Bundle bundle = new Bundle();
        bundle.putString(PopUpDialog.STRING_TITLE, stringTitle);
        bundle.putString(PopUpDialog.STRING_SUB_TITLE, stringSubTitle);
        bundle.putString(PopUpDialog.STRING_DESCRIPTION, stringDescription);

        final PopUpDialog popUpDialog = new PopUpDialog();
        popUpDialog.setArguments(bundle);

        try {
            popUpDialog.show(fragmentManager, "pop_up_dialog");
        } catch(IllegalStateException e) {
            Timber.e(e, "ERROR showing popUpDialog.show()");
        }
    }
}
