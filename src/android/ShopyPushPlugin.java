package com.shopy.push;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    private BroadcastReceiver fcmReceiver;
    
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        
        // Registrar receiver para escuchar mensajes FCM
        registerFCMReceiver();
    }
    
    private void registerFCMReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.google.firebase.MESSAGING_EVENT");
        
        fcmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "📲 Broadcast recibido de FCM");
                
                // Obtener datos del mensaje
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    JSONObject data = new JSONObject();
                    try {
                        for (String key : extras.keySet()) {
                            data.put(key, extras.get(key).toString());
                        }
                        
                        Log.i(TAG, "Mensaje recibido en segundo plano: " + data.toString());
                        
                        // 🔥 ACTIVAR LOCAL NOTIFICATION
                        triggerLocalNotification(data);
                        
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando datos", e);
                    }
                }
            }
        };
        
        cordova.getActivity().registerReceiver(fcmReceiver, filter);
        Log.i(TAG, "✅ Receptor FCM registrado");
    }
    
    private void triggerLocalNotification(JSONObject data) {
        try {
            String title = data.optString("title", "Nueva notificación");
            String body = data.optString("body", "");
            String messageId = data.optString("messageId", String.valueOf(System.currentTimeMillis()));
            
            // Ejecutar en UI thread para llamar JavaScript
            final String jsCode = "setTimeout(function() { " +
                "if (window.cordova && window.cordova.plugins && window.cordova.plugins.notification) { " +
                "   window.cordova.plugins.notification.local.schedule({ " +
                "       id: " + messageId + ", " +
                "       title: '" + title.replace("'", "\\'") + "', " +
                "       text: '" + body.replace("'", "\\'") + "', " +
                "       foreground: true, " +
                "       vibrate: true, " +
                "       icon: 'res://ic_launcher', " +
                "       color: '#e72b3b' " +
                "   }); " +
                "} }, 100);";
            
            cordova.getActivity().runOnUiThread(() -> {
                webView.loadUrl("javascript:" + jsCode);
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error activando Local Notification", e);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fcmReceiver != null) {
            cordova.getActivity().unregisterReceiver(fcmReceiver);
        }
    }
}