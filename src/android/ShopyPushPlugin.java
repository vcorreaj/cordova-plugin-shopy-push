package com.shopy.push;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        if (action.equals("onMessageReceived")) {
            // Este método es llamado por FCM cuando llega un mensaje
            JSONObject messageData = args.getJSONObject(0);
            this.handleMessage(messageData);
            callbackContext.success();
            return true;
        }
        
        return false;
    }
    
    private void handleMessage(JSONObject data) {
        try {
            Log.i(TAG, "📲 Mensaje recibido en plugin personalizado");
            
            // Extraer datos
            String title = data.optString("title", "Nueva notificación");
            String body = data.optString("body", "");
            
            // 🔥 ACTIVAR LOCAL NOTIFICATION
            this.triggerLocalNotification(title, body, data);
            
        } catch (Exception e) {
            Log.e(TAG, "Error manejando mensaje", e);
        }
    }
    
    private void triggerLocalNotification(String title, String body, JSONObject data) {
        // Aquí invocamos el plugin de local notification
        // Usando el contexto de Cordova para ejecutar JavaScript
        
        String jsCode = "setTimeout(function() { " +
            "if (window.cordova && window.cordova.plugins && window.cordova.plugins.notification) { " +
            "   window.cordova.plugins.notification.local.schedule({ " +
            "       title: '" + title.replace("'", "\\'") + "', " +
            "       text: '" + body.replace("'", "\\'") + "', " +
            "       foreground: true, " +
            "       vibrate: true " +
            "   }); " +
            "} }, 100);";
        
        // Ejecutar el código JavaScript
        cordova.getActivity().runOnUiThread(() -> {
            webView.loadUrl("javascript:" + jsCode);
        });
        
        Log.i(TAG, "✅ Local Notification activada");
    }
}