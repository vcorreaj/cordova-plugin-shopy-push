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
    
    // Variable para tracking
    private static boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "🚀 SERVICIO PERSONALIZADO INICIADO");
        Log.i(TAG, "✅ Reemplaza al plugin background-mode");
        Log.i(TAG, "✅ Compatible con Android 14+");
        Log.i(TAG, "========================================");
        
        createNotificationChannel();
        startForegroundService();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Android lo reinicia si lo mata
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "⏹️ Servicio personalizado detenido");
    }
    
    /**
     * 🔥 CORAZÓN DEL SISTEMA - Se ejecuta cuando llega un mensaje FCM
     * incluso con la app cerrada o en segundo plano
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG, "📲 MENSAJE FCM RECIBIDO (app en segundo plano/cerrada)");
        
        // Extraer datos del mensaje
        Map<String, String> data = remoteMessage.getData();
        
        String title = data.get("title");
        String body = data.get("body");
        String messageId = data.get("messageId");
        
        Log.i(TAG, "   Título: " + title);
        Log.i(TAG, "   Cuerpo: " + body);
        Log.i(TAG, "   ID: " + messageId);
        
        // 🔥 ACTIVAR LOCAL NOTIFICATION
        showLocalNotification(title, body, messageId);
    }
    
    /**
     * Muestra la notificación usando el fork de Moodle
     * (Funciona en Android 14+)
     */
    private void showLocalNotification(String title, String body, String messageId) {
        try {
            // Intent para abrir la app cuando tocan la notificación
            Intent intent = new Intent(this, org.apache.cordova.CordovaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("messageId", messageId);
            intent.putExtra("screen", "/tabs/notifications");
            
            // 🔥 FLAGS CORRECTOS PARA ANDROID 12+
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);
            
            // Crear la notificación (usa el fork de Moodle)
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title != null ? title : "Nueva notificación")
                .setContentText(body != null ? body : "")
                .setSmallIcon(getApplicationInfo().icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            // Para Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
            }
            
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int id = messageId != null ? Integer.parseInt(messageId) : (int) System.currentTimeMillis();
            manager.notify(id, builder.build());
            
            Log.i(TAG, "✅ Notificación mostrada correctamente");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error mostrando notificación", e);
        }
    }
    
    /**
     * Crea el canal de notificaciones (necesario para Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopi Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal principal para notificaciones de Shopi");
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.i(TAG, "✅ Canal de notificaciones creado");
            }
        }
    }
    
    /**
     * Inicia el servicio en primer plano (obligatorio para Android 14+)
     * Esta notificación es la que el usuario ve mientras la app está en bg
     */
    private void startForegroundService() {
        String title = "Shopi";
        String text = "Notificaciones activas";
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(getApplicationInfo().icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)  // No se puede deslizar para quitar
            .build();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requiere tipo de servicio
            startForeground(FOREGROUND_NOTIFICATION_ID, notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            Log.i(TAG, "✅ Foreground service iniciado (Android 14+ con tipo dataSync)");
        } else {
            // Android 8-13
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            Log.i(TAG, "✅ Foreground service iniciado");
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
}