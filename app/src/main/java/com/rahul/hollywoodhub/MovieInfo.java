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
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class MovieInfo extends Activity {

    Information movieData;
    TextView title, release, duration, genre, story, rating, director, writer, cast, serverStatus;
    ImageView image;
    private String link, streamLink=null;
    LinearLayout movieLayout, selectEpisodeLayout;
    View rootLayout, streamLayout;
    List<Information> downloadList, streamingList;
    RecyclerView downloadRecyclerView, streamRecyclerView;
    RatingBar ratingBar;
    Spinner selectServer, selectEpisode;
    ProgressBar recyclerProgressbar;
    private ProgressDialog dialog;
    public static String contentTitle;
    static boolean fetchFromMethod2 = false;
    boolean isTVSeries = false;
    private InterstitialAd mInterstitialAd;
    WebView webView;
    WebViewClient webViewClient;
    Button fetchButton;
    String jsFunc;
    boolean receivedUrl;

    @JavascriptInterface
    public void processHTML(String html) {
        if (html == null)
            return;
        Log.d("source", html);
        int start = html.indexOf("{");
        int end = html.lastIndexOf("}");
        if (start == -1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerProgressbar.setVisibility(View.GONE);
                    serverStatus.setText("Server seems to be down try another [Server]");
                    serverStatus.setVisibility(View.VISIBLE);
                }
            });
            return;
        }
        String data = html.substring(start, end+1);
        Gson gson = new Gson();
        DownloadModel downloadData = gson.fromJson(data, DownloadModel.class);
        downloadList.clear();
        for (DownloadModel.DownloadPlaylist.DownloadInfo model :downloadData.getPlaylist().get(0).getSources()) {
            Information inf = new Information();
            inf.contentTitle = contentTitle;
            inf.contentUrl = link;
            inf.link = model.getFile().replace("apm;","");
            inf.title = "Quality " + model.getType() + " ["+model.getLabel()+"]";
            downloadList.add(inf);
        }
        for (DownloadModel.DownloadPlaylist.Subtitle model :downloadData.getPlaylist().get(0).getTracks()) {
            Information inf = new Information();
            inf.contentTitle = contentTitle;
            inf.contentUrl = link;
            inf.link = model.getFile().replace("apm;","");
            inf.title = "Subtitle [" + model.getLabel() + "]";
            downloadList.add(inf);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverStatus.setVisibility(View.GONE);
                recyclerProgressbar.setVisibility(View.GONE);
                setDownloadsRecylerView();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);
        movieData = new Information();
        webView = new WebView(this);
        webViewClient = new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("/ajax/get_source"))
                    webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                else
                    webView.loadUrl(jsFunc);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final String url)
            {
                if (url.contains("/ajax/get_source") && !receivedUrl) {
                    Log.d("source", url);
                    receivedUrl = true;
                    final Handler mainHandler = new Handler(getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            webView.stopLoading();
                            webView.loadUrl(url);
                            mainHandler.removeCallbacksAndMessages(null);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                return super.shouldInterceptRequest(view, url);
            }
        };
        intializeView();
        link = getIntent().getExtras().get("item")+"watching.html";
        initializeWebView(link);
        contentTitle = movieData.title = (String) getIntent().getExtras().get("title");
        downloadRecyclerView.setHasFixedSize(false);
        downloadRecyclerView.setNestedScrollingEnabled(false);
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

    private void initializeWebView(String link) {
        webView.loadUrl(link);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(false);
        webView.addJavascriptInterface(this, "HTMLOUT");
        webView.setWebViewClient(webViewClient);
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
        TreeMap<String, List<String>> serverListTVSeries = new TreeMap<>(new MyComparator());
        @Override
        protected void onPreExecute() {
            streamingList = new ArrayList<>();
            downloadList = new ArrayList<>();
            streamLayout.setVisibility(View.GONE);
            downloadRecyclerView.setVisibility(View.GONE);
            serverStatus.setVisibility(View.GONE);
            recyclerProgressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Document document = Jsoup.connect(params[0])
                        .timeout(0)
                        .followRedirects(true)
                        .get();
                checkForTVSeries(document);
                fetchMovieDetail(document);
                document = Jsoup.parseBodyFragment(getServerLists(document));
                Elements elements = document.getElementsByClass("les-content");
                int i = 1;
                for (Element e : elements){
                    for (Element temp : e.select("a")) {
                        if (fetchFromMethod2) {
                            Log.d("method", "from method 2");
                            if (isTVSeries){
                                String episodeName = temp.attr("title");
                                episodeName = episodeName.contains(":")?episodeName.split(":")[0]:episodeName;
                                if (serverListTVSeries.containsKey(episodeName)) {
//                                    serverListTVSeries.get(episodeName).add(Constants.LOAD_EPISODE_PREFIX_1 + temp.attr("episode-id"));
                                    serverListTVSeries.get(episodeName).add(temp.attr("onclick"));
                                }
                                else {
                                    List<String> tempList = new ArrayList<>();
//                                    tempList.add(Constants.LOAD_EPISODE_PREFIX_1 + temp.attr("episode-id"));
                                    tempList.add(temp.attr("onclick"));
                                    serverListTVSeries.put(episodeName, tempList);
                                }
                            }else {
//                                sserverList.put("[Server " + i + "] " + temp.attr("title")
//                                        , Constants.LOAD_EPISODE_PREFIX_1 + temp.attr("episode-id")
//                                );
                                serverList.put("[Server " + i + "] " + temp.attr("title")
                                        , temp.attr("onclick"));

                            }
                        }
                        else {
                            if (isTVSeries) {
                                boolean flag = true;
                                String checkKey, episodeName = temp.attr("title").trim();
                                checkKey = episodeName.contains(":")?episodeName.split(":")[0]:episodeName;
                                checkKey = checkKey.trim();
                                for (String key: serverListTVSeries.keySet()){
                                    String currentCheckKey = key.contains(":")?key.split(":")[0]:key;
                                    currentCheckKey = currentCheckKey.trim();
                                    if(currentCheckKey.equalsIgnoreCase(checkKey)){
                                        Log.d("same key", key+"  "+episodeName);
                                        serverListTVSeries.get(key).add(Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
                                        List<String> list = serverListTVSeries.remove(key);
//                                        list.add(Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
                                        if (episodeName.compareTo(key.trim()) > 0)
                                            key = episodeName;
                                        serverListTVSeries.put(key.trim(), list);
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag){
                                    Log.d("new", episodeName);
                                    List<String> tempList = new ArrayList<>();
                                    tempList.add(Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
                                    serverListTVSeries.put(episodeName, tempList);
                                }
//                                if (serverListTVSeries.containsKey(checkKey)) {
//                                    serverListTVSeries.get(checkKey)
//                                            .add(Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
//                                }
//                                else {
//                                    List<String> tempList = new ArrayList<>();
//                                    tempList.add(Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
//                                    serverListTVSeries.put(checkKey, tempList);
//                                }
                            }else {
                                serverList.put("[Server " + i + "] " + temp.attr("title")
                                        , Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash")
                                );
                                Log.d("server", Constants.LOAD_EPISODE_PREFIX + temp.attr("episode-id") + "/" + temp.select("a").attr("hash"));
                            }
                        }
                    }
                    ++i;
                }

                // CHECK FOR SERVER STREAMING LINK
                elements = document.getElementsByAttribute("data-episodes");
                for (Element e : elements){
                    if(!isTVSeries) {
                        if (fetchFromMethod2) {
                            streamLink = Constants.LOAD_EPISODE_PREFIX_1 + e.attr("data-episodes");
                        }
//                            serverList.put("[Server " + i + "] Streaming Video", Constants.LOAD_EPISODE_PREFIX_1 + e.attr("data-episodes"));
                        else {
                            streamLink = Constants.LOAD_EPISODE_PREFIX + e.attr("data-episodes").split("-")[0]
                                                    + "/" + e.attr("data-episodes").split("-")[1];
//                            serverList.put("[Server " + i + "] Streaming Video", Constants.LOAD_EPISODE_PREFIX + e.attr("data-episodes").split("-")[0]
//                                    + "/" + e.attr("data-episodes").split("-")[1]);
                        }
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

        private void checkForTVSeries(Document document) {
            try {
                if (document.getElementsByClass("breadcrumb").first().text().contains("Series")) {
                    isTVSeries = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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

        class MyComparator implements Comparator<String> {
            public int compare(String lhs, String rhs) {
                if (StringUtil.isNumeric(lhs.split(" ")[1].replace(":", "")) && StringUtil.isNumeric(rhs.split(" ")[1].replace(":", "")))
                    return Integer.parseInt(lhs.split(" ")[1].replace(":", "")) - Integer.parseInt(rhs.split(" ")[1].replace(":", ""));
                else
                    return 0;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (isTVSeries){
                selectEpisodeLayout.setVisibility(View.VISIBLE);
//                fetchButton.setVisibility(View.VISIBLE);
            }else {
                if (streamLink != null)
//                    new ExtractDownloadLinkAsyncTask().execute(streamLink);
                selectEpisodeLayout.setVisibility(View.GONE);
//                fetchButton.setVisibility(View.GONE);
            }
            movieLayout.setVisibility(View.VISIBLE);
//            downloadRecyclerView.setVisibility(View.VISIBLE);
//            recyclerProgressbar.setVisibility(View.GONE);
            final ArrayAdapter<String> serverAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
            serverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final ArrayAdapter<String> episodeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
            episodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (isTVSeries) {
                for (String key : serverListTVSeries.keySet()){
                    episodeAdapter.add(key);
                }
                selectEpisode.setAdapter(episodeAdapter);
            }else {
                for (String key : serverList.keySet())
                    serverAdapter.add(key);
                selectServer.setAdapter(serverAdapter);
            }
            selectEpisode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    serverAdapter.clear();
                    for (int i=1; i<=serverListTVSeries.get(selectEpisode.getSelectedItem().toString()).size(); ++i)
                        serverAdapter.add("Try [ Server "+i+" ] ");
//                    serverAdapter.add("Try [Server "+i+"] "+selectEpisode.getSelectedItem().toString());
                    selectServer.setAdapter(serverAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            selectServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    downloadList.clear();
                    if (isTVSeries){
//                        new ExtractDownloadLinkAsyncTask().execute(serverListTVSeries
//                                .get(selectEpisode.getSelectedItem().toString())
//                                .get(position)
//                        );
                        extractDataFromWebView(serverListTVSeries.get(selectEpisode.getSelectedItem().toString())
                                .get(position));

                    }else {
                        extractDataFromWebView(serverList.get(selectServer.getSelectedItem().toString()));
//                        new ExtractDownloadLinkAsyncTask().execute(serverList.get(selectServer.getSelectedItem().toString()));
                    }
//                    selectServer.setSelection(position, false);
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

    private void extractDataFromWebView(String jsFunc) {
        recyclerProgressbar.setVisibility(View.VISIBLE);
        if (!webView.getUrl().contains("watching.html"))
            webView.loadUrl(link);
        receivedUrl = false;
        this.jsFunc = "javascript:" + jsFunc;
    }

    public class ExtractDownloadLinkAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            serverStatus.setVisibility(View.GONE);
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
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Connection", "keep-alive");
                urlConnection.setRequestProperty("host", Constants.HOST);
                urlConnection.setRequestProperty("Referer", link);
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36#4f6c9a");
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
            boolean streamFlag = false;
            for (Element ee : document.getElementsByAttribute("file")) {
                Information inf = new Information();
                inf.contentUrl = link;
                inf.contentTitle = contentTitle;
                if (ee.attr("type").equalsIgnoreCase("m3u8")) {
                    streamFlag = true;
                    inf.title = "STREAM NOW...";
                    inf.link = ee.attr("file");
                    streamingList.add(inf);
                    setStreamRecylerView();
                }else if(!streamFlag){
                    if (ee.attr("file").contains(".srt"))
                        inf.title = "Type: Subtitle";
                    else
                        inf.title = "Type: [" + ee.attr("type") + "] | Quality: [" + ee.attr("label") + "]";
                    inf.link = ee.attr("file");
                    if (!inf.link.contains(Constants.DEFAULT_URL))
                        downloadList.add(inf);
                }
            }
            if(!streamFlag) {
                if (downloadList.isEmpty()) {
                    serverStatus.setText("Server seems to be down try another [Server]");
                    serverStatus.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), downloadList.size() + " Link(s) found", Toast.LENGTH_SHORT).show();
                    setDownloadsRecylerView();
                }
                recyclerProgressbar.setVisibility(View.GONE);
            }
            super.onPostExecute(linkResponse);
        }

        private void setStreamRecylerView() {
            streamLayout.setVisibility(View.VISIBLE);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(MovieInfo.this, streamingList, false);
            streamRecyclerView.setAdapter(adapter);
            WrappingLinearLayoutManager layout = new WrappingLinearLayoutManager(getApplicationContext());
            layout.setSmoothScrollbarEnabled(true);
            streamRecyclerView.setLayoutManager(layout);
        }
    }

    private void setDownloadsRecylerView() {
        downloadRecyclerView.setVisibility(View.VISIBLE);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(MovieInfo.this, downloadList, false);
        downloadRecyclerView.setAdapter(adapter);
        WrappingLinearLayoutManager layout = new WrappingLinearLayoutManager(getApplicationContext());
        layout.setSmoothScrollbarEnabled(true);
        downloadRecyclerView.setLayoutManager(layout);
        movieLayout.setVisibility(View.VISIBLE);
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
        selectEpisodeLayout = (LinearLayout) findViewById(R.id.episode_layout);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        selectServer = (Spinner) findViewById(R.id.select_server);
        fetchButton = (Button) findViewById(R.id.submitButtonTVSeries);
        selectEpisode = (Spinner) findViewById(R.id.select_episode);
        downloadRecyclerView = (RecyclerView) findViewById(R.id.recycler_download);
        streamRecyclerView = (RecyclerView) findViewById(R.id.recycler_streaming);
        serverStatus = (TextView) findViewById(R.id.server_status);
        streamLayout = findViewById(R.id.streaming_view);
        recyclerProgressbar = (ProgressBar) findViewById(R.id.recycler_progressbar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.rating), PorterDuff.Mode.SRC_ATOP);
    }
}
