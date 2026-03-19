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

import java.util.Map;

public class ShopyPushService extends FirebaseMessagingService {
    
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "🚀 SERVICIO BACKGROUND INICIADO");
        Log.i(TAG, "========================================");
        
        createNotificationChannel();
        startForegroundService();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "⏹️ Servicio background detenido");
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "📲 Mensaje FCM recibido (solo data)");
        
        // Obtener los datos (vienen de la sección 'data' del payload)
        Map<String, String> data = remoteMessage.getData();
        
        String title = data.get("title");
        String body = data.get("body");
        String messageId = data.get("messageId");
        String screen = data.get("screen");
        
        Log.d(TAG, "Título: " + title);
        Log.d(TAG, "Cuerpo: " + body);
        Log.d(TAG, "MessageId: " + messageId);
        
        // 🔥 ACTIVAR LOCAL NOTIFICATION
        triggerLocalNotification(title, body, messageId, screen);
    }
    
    private void triggerLocalNotification(String title, String body, String messageId, String screen) {
        try {
            // Crear intent para abrir la app cuando toquen la notificación
            Intent intent = new Intent(this, org.apache.cordova.CordovaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("messageId", messageId);
            intent.putExtra("screen", screen);
            
            // Flags correctos para Android 12+
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);
            
            // Crear la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getApplicationInfo().icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(Integer.parseInt(messageId), builder.build());
            
            Log.d(TAG, "✅ Notificación mostrada con Local Notification");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando notificación", e);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopy Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de Shopy");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void startForegroundService() {
        // Notificación persistente para mantener la app en segundo plano
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shopi")
            .setContentText("Notificaciones activas")
            .setSmallIcon(getApplicationInfo().icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
        
        Log.i(TAG, "✅ Foreground service activo");
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
}