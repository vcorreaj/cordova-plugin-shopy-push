package com.shopy.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class ShopyPushService extends FirebaseMessagingService {
    
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_foreground_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "🚀 SERVICIO PERSONALIZADO INICIADO");
        Log.i(TAG, "========================================");
        
        createNotificationChannel();
        startForegroundService();
    }
    
    // 🔥 ELIMINADO: onStartCommand (no se puede sobrescribir)
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "⏹️ Servicio personalizado detenido");
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG, "📲 MENSAJE FCM RECIBIDO");
        
        Map<String, String> data = remoteMessage.getData();
        
        String title = data.get("title");
        String body = data.get("body");
        String messageId = data.get("messageId");
        
        showLocalNotification(title, body, messageId);
    }
    
    private void showLocalNotification(String title, String body, String messageId) {
        try {
            Intent intent = new Intent(this, org.apache.cordova.CordovaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("messageId", messageId);
            intent.putExtra("screen", "/tabs/notifications");
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title != null ? title : "Nueva notificación")
                .setContentText(body != null ? body : "")
                .setSmallIcon(getApplicationInfo().icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int id = messageId != null ? Integer.parseInt(messageId) : (int) System.currentTimeMillis();
            manager.notify(id, builder.build());
            
            Log.i(TAG, "✅ Notificación mostrada");
            
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando notificación", e);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopi Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            );
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shopi")
            .setContentText("Notificaciones activas")
            .setSmallIcon(getApplicationInfo().icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
}