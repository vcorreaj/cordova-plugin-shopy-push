package sigmisa.com.shopy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushService extends Service {
    private static final String TAG = "ShopyPushService";
    private static final String CHANNEL_ID = "shopy_push_channel";
    private static final int NOTIFICATION_ID = 9999;
    
    private static ShopyPushService instance;
    private static Handler mainHandler;
    private static NotificationListener notificationListener;
    
    public interface NotificationListener {
        void onMessageReceived(RemoteMessage message);
        void onTokenRefreshed(String token);
    }
    
    public static void setNotificationListener(NotificationListener listener) {
        notificationListener = listener;
    }
    
    public static ShopyPushService getInstance() {
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mainHandler = new Handler(Looper.getMainLooper());
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createForegroundNotification());
        
        Log.d(TAG, "ShopyPushService created");
        
        // Inicializar FCM
        initFCM();
    }
    
    private void initFCM() {
        // Obtener y guardar token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Log.d(TAG, "FCM Token obtained: " + token.substring(0, Math.min(token.length(), 20)) + "...");
                
                if (notificationListener != null) {
                    notificationListener.onTokenRefreshed(token);
                }
            } else {
                Log.e(TAG, "Failed to get FCM token", task.getException());
            }
        });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ShopyPushService onStartCommand");
        return START_STICKY; // 🔥 CRÍTICO: Mantener servicio vivo
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "ShopyPushService destroyed, restarting...");
        super.onDestroy();
        
        // Reiniciar el servicio inmediatamente
        Intent restartIntent = new Intent(this, ShopyPushService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Shopy Servicio",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Mantiene la app activa para recibir notificaciones");
            channel.setSound(null, null);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
    
    private Notification createForegroundNotification() {
        // Usar la clase de utilidad para crear la notificación
        return ShopyPushNotification.createForegroundNotification(
            this,
            CHANNEL_ID,
            "Shopy",
            "Recibiendo notificaciones..."
        );
    }
    
    // 🔥 MÉTODO PARA PROCESAR MENSAJES FCM
    public void processFcmMessage(final RemoteMessage message) {
        Log.d(TAG, "Processing FCM message");
        
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // Notificar al plugin
                if (notificationListener != null) {
                    notificationListener.onMessageReceived(message);
                }
                
                // Mostrar notificación local usando la clase de utilidad
                Map<String, Object> data = new HashMap<>();
                for (String key : message.getData().keySet()) {
                    data.put(key, message.getData().get(key));
                }
                
                String title = message.getData().get("title");
                String body = message.getData().get("body");
                
                if (title == null) title = "Nueva notificación";
                if (body == null) body = "";
                
                ShopyPushNotification.showNotification(
                    ShopyPushService.this,
                    CHANNEL_ID,
                    title,
                    body,
                    data
                );
                
                Log.d(TAG, "Notification displayed: " + title);
            }
        });
    }
}