package com.shopy.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushService extends FirebaseMessagingService {
    
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_push_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        
        ShopyPushNotification.createNotificationChannel(this, CHANNEL_ID);
        startForegroundService();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);
        ShopyPushPlugin.sendTokenRefresh(token);
    }
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Mensaje FCM recibido");
        
        Map<String, Object> data = new HashMap<>();
        
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            data.put("title", notification.getTitle() != null ? notification.getTitle() : "");
            data.put("body", notification.getBody() != null ? notification.getBody() : "");
            data.put("wasTapped", false);
            data.put("clickAction", notification.getClickAction());
        }
        
        for (String key : remoteMessage.getData().keySet()) {
            data.put(key, remoteMessage.getData().get(key));
        }
        
        boolean isForeground = ShopyPushNotification.isAppInForeground(this);
        
        if (isForeground) {
            ShopyPushPlugin.setLastNotificationData(data);
        } else {
            showSystemNotification(data);
        }
    }
    
    private void showSystemNotification(Map<String, Object> data) {
        String title = data.get("title") != null ? data.get("title").toString() : "Nueva notificación";
        String body = data.get("body") != null ? data.get("body").toString() : "";
        
        ShopyPushNotification.showNotification(this, CHANNEL_ID, title, body, data);
    }
    
    private void startForegroundService() {
        String channelId = CHANNEL_ID;
        String title = "Shopy - Notificaciones Activas";
        String text = "Manteniendo notificaciones activas para no perderte nada";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
            Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(getApplicationInfo().icon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
            
            startForeground(NOTIFICATION_ID, notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8-13
            Notification notification = ShopyPushNotification.createForegroundNotification(
                this, CHANNEL_ID, title, text);
            startForeground(NOTIFICATION_ID, notification);
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
     
}