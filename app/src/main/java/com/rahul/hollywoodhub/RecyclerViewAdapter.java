package com.rahul.hollywoodhub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rahul on 8/3/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder> {

    List<Information> list;
    Context context;
    boolean fromMovieInfo;

    public RecyclerViewAdapter(Context context, List<Information> list, boolean fromMovieInfo) {
        this.list = list;
        this.context = context;
        this.fromMovieInfo = fromMovieInfo;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (fromMovieInfo)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_small, parent, false);
        else view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_list, parent, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final Information information = list.get(position);
        if(fromMovieInfo) {
            Picasso.with(context).load(information.image).placeholder(R.drawable.placeholder).into(holder.image);
            holder.title.setText(information.title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MovieInfo.class);
                    intent.putExtra("item", information.link);
                    intent.putExtra("image", information.image);
                    intent.putExtra("title", information.title);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }
        else{
            holder.dTitle.setText(information.title);
            holder.dButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SHORTEST_API_TOKEN_LINK + information.link));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(information.link));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browserIntent);
                }
            });
            holder.dButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    copyToClipBoard(Constants.SHORTEST_API_TOKEN_LINK + information.link);
                    return true;
                }
            });
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView title, dTitle;
        ImageView image, dButton;
        public CustomViewHolder(View itemView) {
            super(itemView);
            if (fromMovieInfo) {
                image = (ImageView) itemView.findViewById(R.id.movie_IV);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                title = (TextView) itemView.findViewById(R.id.movie_title_TV);
            }
            else {
                dButton = (ImageView) itemView.findViewById(R.id.dButton);
                dTitle = (TextView) itemView.findViewById(R.id.dTitle);
            }
        }
    }

    private void copyToClipBoard(String responseLink) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        clipboard.setText(responseLink);
        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
