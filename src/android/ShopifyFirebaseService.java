package sigmisa.com.shopy;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushFirebaseService extends FirebaseMessagingService {
    private static final String TAG = "ShopyPushFirebase";
    
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "🔥 FCM Message Received! Data size: " + message.getData().size());
        
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data: " + message.getData());
            
            // Intentar enviar al servicio principal
            ShopyPushService service = ShopyPushService.getInstance();
            if (service != null) {
                Log.d(TAG, "Service is alive, forwarding message");
                service.processFcmMessage(message);
            } else {
                Log.w(TAG, "Service not alive, showing notification directly");
                // Si el servicio no está vivo, mostrar notificación directamente
                showNotificationDirectly(message);
            }
        }
    }
    
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "🔥 FCM Token Refreshed: " + token.substring(0, Math.min(token.length(), 20)) + "...");
        
        ShopyPushService service = ShopyPushService.getInstance();
        if (service != null) {
            // Notificar al plugin que el token se refrescó
            if (ShopyPushService.NotificationListener.class.isInstance(service)) {
                // El listener manejará guardarlo en el backend
                Log.d(TAG, "Token refresh forwarded to service");
            }
        }
    }
    
    private void showNotificationDirectly(RemoteMessage message) {
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        
        if (title == null) title = "Nueva notificación";
        if (body == null) body = "";
        
        Map<String, Object> data = new HashMap<>();
        for (String key : message.getData().keySet()) {
            data.put(key, message.getData().get(key));
        }
        
        Log.d(TAG, "Showing direct notification: " + title);
        
        ShopyPushNotification.showNotification(
            getApplicationContext(),
            "shopy_notifications_channel",
            title,
            body,
            data
        );
    }
}