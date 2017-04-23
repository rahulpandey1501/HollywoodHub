package com.rahul.hollywoodhub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 8/3/16.
 */
public class RecyclerViewFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private String link;
    private boolean fromSearch;
    private RecyclerViewAdapter adapter;
    private View layout;
    private View progressBar, swipeMessage;
    private List<Information> list;
    private GridLayoutManager mGridLayoutManager;
    private boolean loading = true;
    private SwipeRefreshLayout swipeContainer;
    private int previousListCount = 0, pageCount=1;

    public RecyclerViewFragment(){
        list = new ArrayList<>();
    }

    public static RecyclerViewFragment newInstance(String link) {
        RecyclerViewFragment recyclerViewFragment= new RecyclerViewFragment();
        Bundle arg = new Bundle();
        arg.putString("item", link);
        recyclerViewFragment.setArguments(arg);
        return recyclerViewFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        link = getArguments().getString("item");
        progressBar = view.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
//        mRecyclerView.setHasFixedSize(true);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeMessage = layout.findViewById(R.id.swipe_message);
        swipeMessage.setVisibility(View.GONE);
        initializeRecyclerView();
        isNetworkAvailable();
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                pageCount = 1;
                previousListCount = 1;
                swipeMessage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                mRecyclerView.removeAllViews();
                isNetworkAvailable();
            }
        });

    }

    class ParserAsyncTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            if (!swipeContainer.isRefreshing()) {
                progressBar.setAnimation(CustomAnimation.fadeIn(getContext()));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
            }
            previousListCount = list.size();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                Log.d("LINK ", params[0]);
                Document document = Jsoup.connect(params[0])
                        .timeout(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0")
                        .followRedirects(true)
                        .get();
                Elements elements = document.getElementsByClass("movies-list").first().select("div.ml-item");
                for (org.jsoup.nodes.Element e : elements){
                    Information information = new Information();
                    information.title = e.select("a").attr("title");
                    information.link = e.select("a").attr("href");
                    information.image = e.select("img").attr("data-original");
                    if (information.image.contains(".to"))
                        information.image = information.image.replace(".to", ".is");
                    list.add(information);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            try {
                swipeContainer.setRefreshing(false);
                progressBar.setAnimation(CustomAnimation.fadeOut(getContext()));
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (list.isEmpty()) {
                    swipeMessage.setVisibility(View.VISIBLE);
                    swipeMessage.bringToFront();
                }
                if (list.isEmpty() || list.size() == previousListCount) {
                    Toast.makeText(getContext(), "Content not found please try again", Toast.LENGTH_SHORT).show();
                    if (pageCount > 1)
                        pageCount--;
                }
                if (previousListCount != 0)
                    mRecyclerView.smoothScrollToPosition(previousListCount);
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (loading && dy > 0) {
                            if (mGridLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() - 1) {
                                loading = false;
                                pageCount++;
                                isNetworkAvailable();
                            }
                        }
                    }
                });
                loading = true;
            }catch (Exception  e){
                e.printStackTrace();
            }
        }
    }

    private void initializeRecyclerView() {
        adapter = new RecyclerViewAdapter(getContext(), list, true);
        mRecyclerView.setAdapter(adapter);
        int numberOfColumns = 3;
//        mGridLayoutManager = new VarColumnGridLayoutManager(getContext(), 290);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfColumns = 4;
        }
        mGridLayoutManager = new GridLayoutManager(getContext(), numberOfColumns, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
    }

    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            if (fromSearch) {
                new ParserAsyncTask().execute(link);
            }
            else {new ParserAsyncTask().execute(link + "/" + pageCount);}
        }
        else showNetworkDialogBox();
    }

    public boolean showNetworkDialogBox(){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
        dialog.setTitle("Network Connectivity");
        dialog.setMessage("No internet connection detected please try again");
        dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isNetworkAvailable();
            }
        });
        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        dialog.setCancelable(false);
        dialog.show();
        return true;
    }
}