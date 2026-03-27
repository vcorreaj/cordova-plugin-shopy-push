package sigmisa.com.shopy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShopyPushPlugin extends CordovaPlugin implements ShopyPushService.NotificationListener {
    private static final String TAG = "ShopyPushPlugin";
    private static ShopyPushPlugin instance;
    private CallbackContext messageCallback;
    private CallbackContext tokenCallback;
    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        instance = this;
        
        // Iniciar el servicio
        Context context = cordova.getActivity().getApplicationContext();
        Intent serviceIntent = new Intent(context, ShopyPushService.class);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        ShopyPushService.setNotificationListener(this);
        
        Log.d(TAG, "ShopyPushPlugin initialized");
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "getToken":
                getToken(callbackContext);
                return true;
            case "onMessageReceived":
                messageCallback = callbackContext;
                // Mantener el callback para futuros mensajes
                return true;
            case "onTokenRefresh":
                tokenCallback = callbackContext;
                return true;
            case "isServiceRunning":
                isServiceRunning(callbackContext);
                return true;
        }
        return false;
    }
    
    private void getToken(CallbackContext callbackContext) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                callbackContext.success(task.getResult());
                Log.d(TAG, "Token obtenido: " + task.getResult());
            } else {
                callbackContext.error("Failed to get token");
                Log.e(TAG, "Error getting token", task.getException());
            }
        });
    }
    
    private void isServiceRunning(CallbackContext callbackContext) {
        boolean running = (ShopyPushService.getInstance() != null);
        callbackContext.success(running ? 1 : 0);
    }
    
    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (messageCallback != null) {
            try {
                JSONObject data = new JSONObject();
                for (String key : message.getData().keySet()) {
                    data.put(key, message.getData().get(key));
                }
                // Agregar metadata
                data.put("wasTapped", false);
                data.put("fromBackground", true);
                data.put("sentTime", System.currentTimeMillis());
                
                messageCallback.success(data);
                Log.d(TAG, "Message forwarded to JS: " + data.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON response", e);
                messageCallback.error(e.getMessage());
            }
        } else {
            Log.w(TAG, "No message callback registered");
        }
    }
    
    @Override
    public void onTokenRefreshed(String token) {
        if (tokenCallback != null) {
            tokenCallback.success(token);
            Log.d(TAG, "Token refresh forwarded to JS: " + token);
        } else {
            Log.w(TAG, "No token callback registered");
        }
    }
}