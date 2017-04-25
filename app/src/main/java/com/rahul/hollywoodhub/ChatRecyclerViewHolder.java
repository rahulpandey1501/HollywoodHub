package com.rahul.hollywoodhub;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by root on 4/23/17.
 */

public class ChatRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private View mView;
    private Context mContext;

    public ChatRecyclerViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);
    }

    void bindChatView(ChatModel chatModel, boolean currentUser) {
        RelativeLayout layout;
        ImageView chatImageView;
        TextView messageTextView, timeTextView, userNameTextView;

        if (currentUser) {
            mView.findViewById(R.id.sender_chat).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.receiver_chat).setVisibility(View.GONE);
            chatImageView = (ImageView) mView.findViewById(R.id.sender_chat_IV);
            messageTextView = (TextView) mView.findViewById(R.id.sender_message_TV);
            timeTextView = (TextView) mView.findViewById(R.id.sender_time_TV);
            userNameTextView = (TextView) mView.findViewById(R.id.sender_user_name_TV);
        } else {
            mView.findViewById(R.id.sender_chat).setVisibility(View.GONE);
            mView.findViewById(R.id.receiver_chat).setVisibility(View.VISIBLE);
            chatImageView = (ImageView) mView.findViewById(R.id.receiver_chat_IV);
            messageTextView = (TextView) mView.findViewById(R.id.receiver_message_TV);
            timeTextView = (TextView) mView.findViewById(R.id.receiver_time_TV);
            userNameTextView = (TextView) mView.findViewById(R.id.receiver_user_name_TV);
        }

        Picasso.with(mContext)
                .load(chatModel.getImageUrl())
                .transform(new CircleTransform())
                .placeholder(R.drawable.header_icon)
                .into(chatImageView);

        userNameTextView.setText(chatModel.getUserName());
        timeTextView.setText(Utility.getDateString(chatModel.getTime()));
        messageTextView.setText(chatModel.getMessage());
    }

    @Override
    public void onClick(View v) {

    }
}
