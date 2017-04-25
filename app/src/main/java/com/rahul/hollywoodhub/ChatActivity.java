package com.rahul.hollywoodhub;

import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    private EditText messageET;
    private FloatingActionButton sendMessageFab;
    private ListView messageListView;
    private RecyclerView mRecyclerView;
    private ContentLoadingProgressBar progressBar;
    private FirebaseRecyclerAdapter mFirebaseAdapter;
    private boolean firstTimeDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setActionBar();
        bindViews();
        setListeners();
        setUpChatAdapter();
        showProgressBar();
    }

    private void setActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorAccent));
        }
        Utility.changeStatusBarColor(this, R.color.colorAccent);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void showProgressBar() {
        progressBar.show();
    }

    private void hideProgressBar() {
        progressBar.hide();
    }

    private void setUpChatAdapter() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("group_chat").orderByChild("time").getRef();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatModel, ChatRecyclerViewHolder>
                (ChatModel.class, R.layout.chat_item_layout, ChatRecyclerViewHolder.class, ref) {

            @Override
            protected void populateViewHolder(ChatRecyclerViewHolder viewHolder, ChatModel model, int position) {
                if (model.getuID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    viewHolder.bindChatView(model, true);
                } else {
                    viewHolder.bindChatView(model, false);
                }
            }

            @Override
            protected void onDataChanged() {
                super.onDataChanged();
                if (firstTimeDataLoaded) {
                    scrollToLastPosition(mFirebaseAdapter.getItemCount()-1);
                }
                else {
                    hideProgressBar();
                    firstTimeDataLoaded = true;
                }
            }
        };

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

    private void scrollToLastPosition(final int position) {
        mRecyclerView.scrollToPosition(position);
    }

    private void bindViews() {
        messageET = (EditText) findViewById(R.id.input);
        sendMessageFab = (FloatingActionButton) findViewById(R.id.fab);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_of_messages);
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.content_loading_progressbar);
    }

    private void setListeners() {
        sendMessageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(messageET.getText())) {
                    messageET.setError("Message cannot be empty");
                    return;
                }
                sendMessage(messageET.getText().toString());
            }
        });
    }

    private void sendMessage(String message) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Some error occurred. Please signout and signin again", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String image = (currentUser.getPhotoUrl() != null) ? currentUser.getPhotoUrl().toString() : null;
        FirebaseDatabase.getInstance()
                .getReference()
                .child("group_chat")
                .push()
                .setValue(new ChatModel(message,
                        currentUser.getDisplayName(),
                        image,
                        currentUser.getUid()));
        messageET.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }
}
