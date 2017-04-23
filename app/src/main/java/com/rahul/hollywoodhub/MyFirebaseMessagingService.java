package com.rahul.hollywoodhub;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

/**
 * Created by root on 3/26/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public MyFirebaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
            }

        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification : "+ remoteMessage.getNotification().getBody());
            handleNow(remoteMessage);
        }
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    private void scheduleJob() {
        // [START dispatch_job]  FirebaseJobDispatcher
    }

    private void handleNow(RemoteMessage remoteMessage) {
        boolean isBigPicture = false;
        Bitmap bitmap = null;
        String imageUrl = null;
        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().containsKey("image_big")) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.header_movie);
                imageUrl = remoteMessage.getData().get("image_big");
                isBigPicture = true;
            } else if (remoteMessage.getData().containsKey("image_small")) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.header_icon);
                imageUrl = remoteMessage.getData().get("image_small");
                isBigPicture = false;
            }
            try {
                URL url = new URL(imageUrl);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException ignored) {}
        }
        String summary = remoteMessage.getData().containsKey("summary")
                ? remoteMessage.getData().get("summary")
                :  "Click to explore more...";
        if (isBigPicture) {
            sendNotificationsBig(remoteMessage, imageUrl, summary);
        } else {
            sendNotifications(remoteMessage, bitmap);
        }
    }

    private void sendNotifications(RemoteMessage remoteMessage, Bitmap bitmap) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(SplashScreen.class);
        taskStackBuilder.addNextIntent(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationsBig(RemoteMessage remoteMessage, String imageUrl, String summary) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(SplashScreen.class);
        taskStackBuilder.addNextIntent(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(false)
                .setSound(defaultSoundUri);
                if(imageUrl != null && !imageUrl.isEmpty()) {
                    NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
                    try {
                        s.bigPicture(Picasso.with(getApplicationContext()).load(imageUrl).get());
                        s.setSummaryText(summary);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    notificationBuilder.setStyle(s);
                }
                notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
