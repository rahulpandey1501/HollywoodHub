package com.rahul.hollywoodhub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Rahul on 05 May 2016.
 */
public class CheckForUpdate {
    public static void getStatus(final Context mContext){
        {
            new AsyncTask<String, Void, String>(){
                @Override
                protected String doInBackground(String... params) {
                    String extractJSON = "{}";
                    try {
                        Document document = Jsoup.connect(Constants.UPDATE_CHECKER_URL)
                                .timeout(0)
                                .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                                .followRedirects(true)
                                .get();
                        extractJSON = document.getElementById("update_json").text();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return extractJSON;
                }

                @Override
                protected void onPostExecute(String json) {
                    JSONObject jsonObject = null;
                    final String URL, VERSION_MESSAGE;
                    String POSITIVE_BUTTON = "";
                    try {
                        jsonObject = new JSONObject(json);
                        jsonObject = jsonObject.getJSONObject(mContext.getResources().getString(R.string.app_name));
                        URL = jsonObject.getString("url");
                        VERSION_MESSAGE = "Current Version :  "+BuildConfig.VERSION_NAME+"\n"+
                                "Latest Version   :  "+jsonObject.getString("version")+"\n\n";
                        Log.d("version", VERSION_MESSAGE);
                        if (jsonObject.getBoolean("show_dialog")
                                && (!BuildConfig.VERSION_NAME.equals(jsonObject.getString("version"))||jsonObject.getString("type").equals("message"))){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                            dialog.setTitle(Html.fromHtml("<b><u><big><font color=#ff0000>"+jsonObject.getString("title")
                                            + "</font></big></u></b>"));
                            if (!BuildConfig.VERSION_NAME.equals(jsonObject.getString("version"))) {
                                dialog.setMessage(VERSION_MESSAGE+ jsonObject.getString("message"));
                                POSITIVE_BUTTON = "update";
                            }
                            else if(jsonObject.getString("type").equals("message")){
                                dialog.setMessage(jsonObject.getString("message"));
                            }
                            dialog.setPositiveButton(POSITIVE_BUTTON, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(browserIntent);
                                        }
                                    })
                                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
//                                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onPostExecute(json);
                }
            }.execute();
        }
    }
}
