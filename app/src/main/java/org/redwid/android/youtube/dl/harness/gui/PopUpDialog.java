package org.redwid.android.youtube.dl.harness.gui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.redwid.android.youtube.dl.harness.R;

/**
 * The PopUpDialog class.
 */
public class PopUpDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private static final String LOG_TAG = "PopUpDialog";

    public static final String STRING_TITLE = "title";
    public static final String STRING_SUB_TITLE = "sub_title";
    public static final String STRING_DESCRIPTION = "description";

    private String stringTitle;
    private String stringSubTitle;
    private String stringDescription;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        setRetainInstance(true);

        if(getArguments() != null) {
            stringTitle = getArguments().getString(STRING_TITLE);
            stringSubTitle = getArguments().getString(STRING_SUB_TITLE);
            stringDescription = getArguments().getString(STRING_DESCRIPTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pop_up_dialog, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = view.findViewById(R.id.title);
        if(textView != null) {
            textView.setText(stringTitle);
        }
        textView = view.findViewById(R.id.sub_title);
        if(textView != null) {
            textView.setText(stringSubTitle);
        }
        textView = view.findViewById(R.id.description);
        if(textView != null) {
            textView.setText(stringDescription);
        }

        final Button okButton = view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(this);

        final Dialog dialog = getDialog();
        if(dialog != null) {
        }
    }

    @Override
    public void onClick(final View v) {
        Log.d(LOG_TAG, "onClick()");
        if(v.getId() == R.id.ok_button) {
            final Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

//    private Spanned getCountString(final long value) {
//        final String string = getString(R.string.switching_list_dialog_count, value/1000);
//        return Html.fromHtml(string);
//    }
}
