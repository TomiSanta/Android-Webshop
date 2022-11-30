package com.example.webshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Notification {
    private static final String CHANNEL_ID = "shop_notification_channel";
    private final int NOTIFICATION_ID = 0;
    private NotificationManager mManager;
    private Context mContext;

    public Notification(Context context){
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "WebShop Notification", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(android.R.color.holo_green_light);
        channel.setDescription("Értesítés a WebShop-ból");
        mManager.createNotificationChannel(channel);
    }

    public void send(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Új elem került a bevásárló kosárba!")
                .setContentText(message)
                .setSmallIcon(R.drawable.shopping_cart_logo);
        mManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancel() {
        mManager.cancel(NOTIFICATION_ID);
    }
}
