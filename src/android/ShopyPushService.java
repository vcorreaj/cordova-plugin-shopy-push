package com.shopy.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ShopyPushService extends Service {
    
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_background_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    // 🔥 Variable estática para tracking
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "Servicio background CREADO");
        Log.i(TAG, "========================================");
        
        createNotificationChannel();
        startForegroundService();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand - Servicio ejecutándose");
        return START_STICKY; // Reiniciar si Android lo mata
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "========================================");
        Log.i(TAG, "Servicio background DESTRUIDO");
        Log.i(TAG, "========================================");
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopi Background Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Mantiene la app activa para recibir notificaciones");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.i(TAG, "✅ Canal de notificaciones creado");
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
            // Android 14+
            startForeground(NOTIFICATION_ID, notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            Log.i(TAG, "✅ Foreground service iniciado (Android 14+ con tipo dataSync)");
        } else {
            // Android 8-13
            startForeground(NOTIFICATION_ID, notification);
            Log.i(TAG, "✅ Foreground service iniciado");
        }
    }
    
    // 🔥 Método público para verificar estado
    public static boolean isRunning() {
        return isRunning;
    }
}