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
        registerFCMReceiver();
    }
    
    private void registerFCMReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.google.firebase.MESSAGING_EVENT");
        
        fcmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "📲 Broadcast recibido de FCM (fork Moodle)");
                
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    JSONObject data = new JSONObject();
                    try {
                        for (String key : extras.keySet()) {
                            data.put(key, extras.get(key).toString());
                        }
                        
                        Log.i(TAG, "Mensaje: " + data.toString());
                        
                        // 🔥 ACTIVAR LOCAL NOTIFICATION (fork Moodle)
                        triggerLocalNotification(data);
                        
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando datos", e);
                    }
                }
            }
        };
        
        cordova.getActivity().registerReceiver(fcmReceiver, filter);
        Log.i(TAG, "✅ Receptor FCM registrado (fork Moodle)");
    }
    
    private void triggerLocalNotification(JSONObject data) {
        try {
            String title = data.optString("title", "Nueva notificación");
            String body = data.optString("body", "");
            String messageId = data.optString("messageId", String.valueOf(System.currentTimeMillis()));
            
            // 🔥 IMPORTANTE: Este es el código correcto para el fork de Moodle
            String jsCode = "if(window.cordova && window.cordova.plugins && window.cordova.plugins.notification) { " +
                
                // 1. Primero verificar/ solicitar permisos (fork Moodle)
                "window.cordova.plugins.notification.local.hasPermission(function(has) { " +
                "   if (!has) { " +
                "       window.cordova.plugins.notification.local.requestPermission(); " +
                "   } " +
                "}); " +
                
                // 2. Programar la notificación
                "window.cordova.plugins.notification.local.schedule({ " +
                "   id: " + messageId + ", " +
                "   title: '" + title.replace("'", "\\'") + "', " +
                "   text: '" + body.replace("'", "\\'") + "', " +
                "   foreground: true, " +
                "   vibrate: true, " +
                "   icon: 'res://ic_launcher', " +
                "   smallIcon: 'res://ic_launcher', " +
                "   color: '#e72b3b', " +
                "   priority: 2, " +
                "   wakeup: true, " +
                "   lockscreen: true, " +
                "   channel: { " +
                "       id: 'shopy-channel', " +
                "       description: 'Canal de notificaciones de Shopi' " +
                "   } " +
                "}); " +
                
                "console.log('✅ Notificación enviada con fork Moodle'); " +
                "}";
            
            // Ejecutar en UI thread
            cordova.getActivity().runOnUiThread(() -> {
                webView.loadUrl("javascript:" + jsCode);
            });
            
            Log.i(TAG, "✅ Notificación activada con fork Moodle");
            
        } catch (Exception e) {
            Log.e(TAG, "Error activando notificación", e);
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