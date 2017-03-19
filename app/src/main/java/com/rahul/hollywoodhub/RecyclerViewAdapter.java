package com.rahul.hollywoodhub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
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
    final static private String DOWNLOAD_REQUEST = "DOWNLOAD_REQUEST", SHARE_REQUEST = "SHARE_REQUEST", COPY_REQUEST = "COPY_REQUEST";

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
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_list, parent, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final Information information = list.get(position);
        if (fromMovieInfo) {
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
        } else {
            holder.dTitle.setText(information.title);
            holder.dButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SHORTEST_API_TOKEN_LINK + information.link));
                    if (information.link.contains("m3u8")) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setDataAndType(Uri.parse(information.link), "video/mp4");
                        context.startActivity(i);
                    } else {
                        showDialogOption(information);
                    }
                }
            });
            holder.dButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new GenerateDownloadLink(information, context, COPY_REQUEST).execute();
                    return true;
                }
            });
        }
    }

    private void showDialogOption(final Information information) {
        final CharSequence options[] = {
                Html.fromHtml("<b><u><big><font>" + information.contentTitle + "</font></big></u></b>")
                , Html.fromHtml("<b><big><font color=#808080>Download</font></big></b>")
                , Html.fromHtml("<b><big><font color=#808080>Copy link</font></big></b>")
                , Html.fromHtml("<b><big><font color=#808080>Share with friends</font></big></b>")
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1:
                        new GenerateDownloadLink(information, context, DOWNLOAD_REQUEST).execute();
                        break;
                    case 2:
                        new GenerateDownloadLink(information, context, COPY_REQUEST).execute();
                        break;
                    case 3:
                        new GenerateDownloadLink(information, context, SHARE_REQUEST).execute();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static class GenerateDownloadLink extends AsyncTask<String, Void, String> {
        Information information;
        ProgressDialog progressDialog;
        Context mContext;
        String request;

        GenerateDownloadLink(Information information, Context context, String request) {
            mContext = context;
            this.information = information;
            this.request = request;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Connecting to server...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
//            try {
//                URL url = new URL(information.link);
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.setRequestProperty("referer", information.contentUrl);
//                urlConnection.setRequestProperty("user-agent",
//                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
//                urlConnection.setReadTimeout(0);
//                return urlConnection.getHeaderField("location").replace("apm;","");
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            return null;
            return information.link;
        }

        @Override
        protected void onPostExecute(String link) {
            progressDialog.dismiss();
            if (!URLUtil.isValidUrl(link))
                link = information.link;
            link = link.replace("&amp;", "&");
            switch (request) {
                case DOWNLOAD_REQUEST:
                    openBrowserIntent(link);
                    break;
                case SHARE_REQUEST:
                    openShareIntent(link);
                    break;
                case COPY_REQUEST:
                    copyToClipBoard(link);
            }
            super.onPostExecute(link);
        }

        private void openShareIntent(String link) {
            Intent s = new Intent(android.content.Intent.ACTION_SEND);
            s.setType("text/plain");
            s.putExtra(Intent.EXTRA_SUBJECT, "Download Link: " + information.contentTitle);
            s.putExtra(Intent.EXTRA_TEXT, information.contentTitle + "\n" + information.title + "\n" + link + "\n\n--HollywoodHub");
            mContext.startActivity(Intent.createChooser(s, "Share link..."));
        }

        private void openBrowserIntent(String link) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        private void copyToClipBoard(String responseLink) {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
            clipboard.setText(responseLink);
            Toast.makeText(mContext, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView title, dTitle;
        ImageView image, dButton;

        public CustomViewHolder(View itemView) {
            super(itemView);
            if (fromMovieInfo) {
                image = (ImageView) itemView.findViewById(R.id.movie_IV);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                title = (TextView) itemView.findViewById(R.id.movie_title_TV);
            } else {
                dButton = (ImageView) itemView.findViewById(R.id.dButton);
                dTitle = (TextView) itemView.findViewById(R.id.dTitle);
            }
        }
    }
}
