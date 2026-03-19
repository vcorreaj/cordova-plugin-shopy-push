package com.shopy.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushService extends FirebaseMessagingService {
    
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_push_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "🚀 SERVICIO BACKGROUND ACTIVADO");
        Log.i(TAG, "========================================");
        
        createNotificationChannel();
        startForegroundService(); // ← Mantiene app activa en segundo plano
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "⏹️ Servicio background DESTRUIDO");
    }
    
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔄 Nuevo token FCM: " + token);
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "📲 NOTIFICACIÓN RECIBIDA EN BACKGROUND");
        
        Map<String, Object> data = new HashMap<>();
        
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            data.put("title", notification.getTitle() != null ? notification.getTitle() : "");
            data.put("body", notification.getBody() != null ? notification.getBody() : "");
            data.put("wasTapped", false);
        }
        
        for (String key : remoteMessage.getData().keySet()) {
            data.put(key, remoteMessage.getData().get(key));
        }
        
        showNotification(data);
    }
    
    private void showNotification(Map<String, Object> data) {
        String title = data.get("title") != null ? data.get("title").toString() : "Nueva notificación";
        String body = data.get("body") != null ? data.get("body").toString() : "";
        
        Intent intent = new Intent(this, org.apache.cordova.CordovaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                intent.putExtra(entry.getKey(), entry.getValue().toString());
            }
        }
        
        // 🔥 FLAGS CORRECTOS PARA ANDROID 12+
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, flags);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(getApplicationInfo().icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
        
        Log.d(TAG, "✅ Notificación mostrada correctamente");
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopy Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de Shopy");
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Canal de notificaciones creado");
            }
        }
    }
    
    private void startForegroundService() {
        String title = "Shopi";
        String text = "Manteniendo notificaciones activas";
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(getApplicationInfo().icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            Log.i(TAG, "✅ Foreground service iniciado (Android 14+)");
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            Log.i(TAG, "✅ Foreground service iniciado");
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
}