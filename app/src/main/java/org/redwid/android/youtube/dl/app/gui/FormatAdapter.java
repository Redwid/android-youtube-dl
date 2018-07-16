package org.redwid.android.youtube.dl.app.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import org.redwid.android.youtube.dl.app.R;
import org.redwid.android.youtube.dl.app.model.Format;

import timber.log.Timber;

/**
 * The FormatAdapter class.
 */
public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.ViewHolder> {

    private final List<Format> formatList;
    private final Context context;

    public FormatAdapter(final List<Format> formatList, final Context context) {
        this.formatList = formatList;
        this.context = context;
    }

    public Format get(int position) {
        return formatList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Format format = get(position);
        if(holder.titleView != null) {
            holder.titleView.setText(format.getName());
        }
        if(holder.descriptionView != null) {
            holder.descriptionView.setText(String.format("acodec: %s, vcodec: %s, ext: %s, %sx%s, file size: %sb",
                    format.getAcodec(), format.getVcodec(), format.getExt(), format.getWidth(), format.getHeight(), format.getFileSize()));
        }
        if(position % 2 == 0) {
            if(holder.titleView != null) {
                holder.titleView.setBackgroundColor(context.getResources().getColor(R.color.rowBackground));
            }
            if(holder.descriptionView != null) {
                holder.descriptionView.setBackgroundColor(context.getResources().getColor(R.color.rowBackground));
            }
        }
        else {
            if(holder.titleView != null) {
                holder.titleView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            }
            if(holder.descriptionView != null) {
                holder.descriptionView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Timber.d("onClick(), url: %s", format.getUrl());
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(format.getUrl()));
                try {
                    context.startActivity(intent);
                } catch(Exception e) {
                    Timber.e(e, "ERROR in onClick(), e: %s", e);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return formatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView descriptionView;

        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.title);
            descriptionView = view.findViewById(R.id.description);
        }
    }
}
