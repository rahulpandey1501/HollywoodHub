package com.rahul.hollywoodhub;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MovieInfo extends Activity {

    Information movieData;
    TextView title, release, duration, genre, story, rating, director, writer, cast, serverStatus;
    ImageView image;
    private String link;
    private RecyclerViewAdapter adapter;
    LinearLayout movieLayout, tvSeriesLayout;
    View rootLayout;
    List<Information> downloadList;
    RecyclerView recyclerView;
    RatingBar ratingBar;
    Spinner selectServer;
    ProgressBar recyclerProgressbar;
    private ProgressDialog dialog;
    public static String contentTitle;
    static boolean fetchFromMethod2 = false;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);
        movieData = new Information();
        intializeView();
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        link = (String) getIntent().getExtras().get("item");
        contentTitle = movieData.title = (String) getIntent().getExtras().get("title");
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        title.setText(movieData.title);
        Picasso.with(getApplicationContext()).load((String) getIntent().getExtras().get("image")).into(image);
        Picasso.with(getApplicationContext()).load((String) getIntent().getExtras().get("image")).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                rootLayout.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(getApplicationContext(), bitmap)));
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });

        initializeAd();

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                displayInterstitial();
            }
        });
        dialog = new ProgressDialog(MovieInfo.this);
        dialog.setTitle((String) getIntent().getExtras().get("title"));
        dialog.setMessage("Please wait while fetching movie data");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        ParserAsyncTask parserAsyncTask = new ParserAsyncTask();
        parserAsyncTask.execute(link);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        super.onBackPressed();
    }

    private void initializeAd() {
        AdView adView = (AdView) findViewById(R.id.adView);
        mInterstitialAd = new InterstitialAd(MovieInfo.this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        mInterstitialAd.loadAd(adRequest);
    }

    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean> {

        LinkedHashMap<String, String> serverList = new LinkedHashMap<>();
        @Override
        protected void onPreExecute() {
            downloadList = new ArrayList<>();
            recyclerView.setVisibility(View.INVISIBLE);
            serverStatus.setVisibility(View.GONE);
            recyclerProgressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0]+"watching.html")
                        .timeout(0)
                        .followRedirects(true)
                        .get();
                fetchMovieDetail(document);
                document = Jsoup.parseBodyFragment(getServerLists(document));
                Elements elements = document.getElementsByClass("les-content");
                Log.d("size", elements.size()+"");
                int i = 1;
                for (Element e : elements){
                    for (Element temp : e.select("a")) {
                        if (fetchFromMethod2) {
                            serverList.put("[Server " + i + "] " + temp.attr("title")
                                    , Constants.LOAD_EPISODE_PREFIX_1 + temp.attr("episode-id")
                            );
                        }
                        else {
                            serverList.put("[Server " + i + "] " + temp.attr("title")
                                    , Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash")
                            );
                        }
                    }
                    ++i;
                }

                // CHECK FOR SERVER BACKUP
                elements = document.getElementsByAttribute("data-episodes");
                for (Element e : elements){
                    if (fetchFromMethod2)
                        serverList.put("[Server "+i+"] Backup", Constants.LOAD_EPISODE_PREFIX_1+e.attr("data-episodes"));
                    else {
                        serverList.put("[Server " + i + "] Backup", Constants.LOAD_EPISODE_PREFIX + e.attr("data-episodes").split("-")[0]
                                + "/" + e.attr("data-episodes").split("-")[1]);
                    }
                }
            }catch (Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MovieInfo.this, "Network error please try again ", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
            return  null;
        }

        private String getServerLists(Document document) {
            StringBuilder result = new StringBuilder();
            URL url = null;
            String domStructure = "";
            try {
                url = new URL(getTokenEpisodeUrl(document));
                Log.d("link", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(0);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                JSONObject jsonObject = new JSONObject(result.toString());
                domStructure = jsonObject.get("content").toString().replace("\\/", "/").replace("\\\"","").replace("\\n","");
            } catch (Exception e) {
                domStructure = result.toString();
                e.printStackTrace();
            }finally {
                return domStructure;
            }
        }

        private String getTokenEpisodeUrl(Document document) {
            String id[] = link.split("-");
            String token = document.getElementById("mv-info").select("div[player-token]").attr("player-token");
            if (token == null || token.isEmpty())
                fetchFromMethod2 = true;
            return Constants.GET_EPISODES_PREFIX+id[id.length -1]+token;
        }

        private void fetchMovieDetail(Document document) {
            movieData.story = document.getElementsByClass("mvic-desc").first().getElementsByClass("desc").text();
            if (movieData.story.contains("123Movies"))
                movieData.story = "Not Available";
            Element IMDB_info = document.getElementsByClass("mvic-info").first();
            for (Element e : IMDB_info.getElementsByClass("mvici-left").select("p")){
                if (e.text().contains("Genre"))
                    movieData.genre = e.text();
                else if(e.text().contains("Actor"))
                    movieData.stars = e.text();
                else if(e.text().contains("Director"))
                    movieData.director = e.text();
                else if(e.text().contains("Country"))
                    movieData.country = e.text();
            }
            for (Element e : IMDB_info.getElementsByClass("mvici-right").select("p")){
                if (e.text().contains("Duration"))
                    movieData.duration = e.text();
                else if(e.text().contains("Release"))
                    movieData.release = e.text();
                else if(e.text().contains("IMDb"))
                    movieData.rating = e.text();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            movieLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerProgressbar.setVisibility(View.GONE);
            final ArrayAdapter<String> serverAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
            serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            for (String key : serverList.keySet()){
                serverAdapter.add(key);
                Log.d("server", serverList.get(key));
            }
            selectServer.setAdapter(serverAdapter);
            selectServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    downloadList.clear();
                    new ExtractDownloadLinkAsyncTask().execute(serverList.get(selectServer.getSelectedItem().toString()));
                    selectServer.setSelection(selectServer.getSelectedItemPosition(), false);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            setData();
            if (dialog.isShowing())
                dialog.dismiss();

        }
    }

    public class ExtractDownloadLinkAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            recyclerProgressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] params) {
            StringBuilder result = new StringBuilder();
            URL url = null;
            try {
                url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(0);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String linkResponse) {
            Document document = Jsoup.parse(linkResponse, "", Parser.xmlParser());
            for (Element ee : document.getElementsByAttribute("file")) {
                Information inf = new Information();
                if (ee.attr("file").contains(".srt"))
                    inf.title = "Type: Subtitle";
                else
                    inf.title = "Type: ["+ee.attr("type") +"] | Quality: ["+ ee.attr("label")+"]";
                inf.link = ee.attr("file");
                if (!inf.link.contains(Constants.DEFAULT_URL))
                    downloadList.add(inf);
            }
            if(downloadList.size() == 0) {
//                Toast.makeText(getApplicationContext(), "Link not found try another [Server]", Toast.LENGTH_SHORT).show();
                serverStatus.setText("Server seems to be down try another [Server]");
                serverStatus.setVisibility(View.VISIBLE);
            }
            else {
                serverStatus.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), downloadList.size() + " Link(s) found", Toast.LENGTH_SHORT).show();
            }
            adapter = new RecyclerViewAdapter(getApplicationContext(), downloadList, false);
            recyclerView.setAdapter(adapter);
            WrappingLinearLayoutManager layout = new WrappingLinearLayoutManager(getApplicationContext());
            layout.setSmoothScrollbarEnabled(true);
            recyclerView.setLayoutManager(layout);
            movieLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerProgressbar.setVisibility(View.GONE);
            super.onPostExecute(linkResponse);
        }
    }

    private void setData() {
        duration.setText(movieData.duration);
        release.setText(movieData.release);
        rating.setText(movieData.rating);
        if (movieData.rating != null) {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating((float) (Float.parseFloat((movieData.rating.split(":")[1]).trim()) / 2.0));

        }
        else ratingBar.setVisibility(View.GONE);
        genre.setText(movieData.genre);
        story.setText(movieData.story);
        if (movieData.director == null)
            director.setVisibility(View.GONE);
        director.setText(movieData.director);
        writer.setText(movieData.country);
        cast.setText(movieData.stars);
    }

    private void intializeView() {
        title = (TextView) findViewById(R.id.title);
        image = (ImageView) findViewById(R.id.image);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        release = (TextView) findViewById(R.id.release);
        duration = (TextView) findViewById(R.id.duration);
        genre = (TextView) findViewById(R.id.genre);
        story = (TextView) findViewById(R.id.story);
        rating = (TextView) findViewById(R.id.rating);
        director = (TextView) findViewById(R.id.director);
        writer = (TextView) findViewById(R.id.writer);
        cast = (TextView) findViewById(R.id.cast);
        movieLayout = (LinearLayout) findViewById(R.id.movie_layout);
        rootLayout = findViewById(R.id.movie_root_layout);
        tvSeriesLayout = (LinearLayout) findViewById(R.id.tvseries_layout);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        selectServer = (Spinner) findViewById(R.id.select_server);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_download);
        serverStatus = (TextView) findViewById(R.id.server_status);
        recyclerProgressbar = (ProgressBar) findViewById(R.id.recycler_progressbar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.rating), PorterDuff.Mode.SRC_ATOP);
    }
}
