package com.shopy.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
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
        
        // Crear canal de notificaciones para Android 8+
        ShopyPushNotification.createNotificationChannel(this, CHANNEL_ID);
        
        // Iniciar foreground service
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
        
        // Procesar datos de la notificación
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            data.put("title", notification.getTitle() != null ? notification.getTitle() : "");
            data.put("body", notification.getBody() != null ? notification.getBody() : "");
            data.put("wasTapped", false);
            data.put("clickAction", notification.getClickAction());
        }
        
        // Procesar datos adicionales
        for (String key : remoteMessage.getData().keySet()) {
            data.put(key, remoteMessage.getData().get(key));
        }
        
        // Determinar si la app está en foreground
        boolean isForeground = ShopyPushNotification.isAppInForeground(this);
        
        if (isForeground) {
            // App en primer plano - enviar a JS para manejo personalizado
            ShopyPushPlugin.setLastNotificationData(data);
        } else {
            // App en segundo plano/cerrada - mostrar notificación del sistema
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
    
    Notification notification;
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
        notification = new NotificationCompat.Builder(this, channelId)
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
        notification = ShopyPushNotification.createForegroundNotification(
            this, CHANNEL_ID, title, text);
        startForeground(NOTIFICATION_ID, notification);
    }
}
    
    public static boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}