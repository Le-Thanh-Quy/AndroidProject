package com.quy.chatapp.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.quy.chatapp.Model.User;
import com.quy.chatapp.R;
import com.quy.chatapp.View.ChatActivity;
import com.quy.chatapp.View.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class PushChatNotification extends FirebaseMessagingService {
    public PushChatNotification() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataPayload = remoteMessage.getData();
        String title = dataPayload.get("title");
        String image = dataPayload.get("image");
        String mess = dataPayload.get("body");
        String id = dataPayload.get("id");
        String type = dataPayload.get("type");
        String uriImage = "";
        if ("image".equals(type)) {
            uriImage = mess;
            mess = "Đã gửi một ảnh!";
        } else if ("icon".equals(type)) {
            mess = "Đã gửi một biểu cảm!";
        } else if ("video".equals(type)) {
            mess = "Đã gửi một video!";
        }
        if(MainActivity.id_user.equals(id)) {
            return;
        }


        Uri soundUri = Uri.parse(
                "android.resource://" +
                        getApplicationContext().getPackageName() +
                        "/" +
                        R.raw.messenger_ios);

        InputStream in;
        Bitmap myBitmap = null;
        try {
            URL url = new URL(uriImage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String CHANNEL_ID = "MESSAGE" + System.currentTimeMillis();

        Intent resultIntent = new Intent(this, MainActivity.class);
        User user = new User();
        user.phoneNumber = id;
        user.userName = title;
        user.userAvatar = image;
        resultIntent.putExtra("other_user", user);
        resultIntent.putExtra("isNotification", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.app_icon)
                            .setContentTitle(title)
                            .setContentText(mess)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                            .setSound(soundUri)
                            .setContentIntent(pendingIntent);

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            notificationChannel.setSound(soundUri, att);
            notificationChannel.setDescription(mess);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{300, 200, 0, 0});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            if (myBitmap != null) {
                notificationBuilder.setStyle(new Notification.BigPictureStyle()
                        .bigPicture(myBitmap).setSummaryText(mess));
            }
            Random random = new Random();
            notificationManager.notify(random.nextInt() /* ID of notification */, notificationBuilder.build());

        } else {
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.app_icon)
                            .setContentTitle(title)
                            .setContentText(mess)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                            .setSound(soundUri)
                            .setContentIntent(pendingIntent);
            if (myBitmap != null) {
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(myBitmap).setSummaryText(mess));
            }
            Random random = new Random();
            notificationManager.notify(random.nextInt() /* ID of notification */, notificationBuilder.build());
        }
        super.onMessageReceived(remoteMessage);
    }
}