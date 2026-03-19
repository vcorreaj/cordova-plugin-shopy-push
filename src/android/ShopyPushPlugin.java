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
        Log.i(TAG, "========================================");
        Log.i(TAG, "🚀 ShopyPushPlugin INICIALIZADO");
        Log.i(TAG, "========================================");
        registerFCMReceiver();
    }
    
    private void registerFCMReceiver() {
        try {
            Log.i(TAG, "📡 Registrando BroadcastReceiver para FCM...");
            
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.google.firebase.MESSAGING_EVENT");
            
            fcmReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "🔥🔥🔥 BROADCAST RECIBIDO 🔥🔥🔥");
                    Log.i(TAG, "Action: " + intent.getAction());
                    
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Log.i(TAG, "Extras recibidos:");
                        for (String key : extras.keySet()) {
                            Log.i(TAG, "   " + key + ": " + extras.get(key));
                        }
                        
                        try {
                            JSONObject data = new JSONObject();
                            for (String key : extras.keySet()) {
                                data.put(key, extras.get(key).toString());
                            }
                            
                            triggerLocalNotification(data);
                            
                        } catch (JSONException e) {
                            Log.e(TAG, "Error procesando JSON", e);
                        }
                    } else {
                        Log.w(TAG, "⚠️ Intent sin extras");
                    }
                }
            };
            
            cordova.getActivity().registerReceiver(fcmReceiver, filter);
            Log.i(TAG, "✅ BroadcastReceiver registrado exitosamente");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error registrando BroadcastReceiver", e);
        }
    }
    
    private void triggerLocalNotification(JSONObject data) {
        try {
            String title = data.optString("title", "Nueva notificación");
            String body = data.optString("body", "");
            String messageId = data.optString("messageId", String.valueOf(System.currentTimeMillis()));
            
            Log.i(TAG, "🔔 Activando Local Notification:");
            Log.i(TAG, "   Title: " + title);
            Log.i(TAG, "   Body: " + body);
            Log.i(TAG, "   MessageId: " + messageId);
            
            // Código para local notification
            String jsCode = "if(window.cordova && window.cordova.plugins && window.cordova.plugins.notification) { " +
                "window.cordova.plugins.notification.local.schedule({ " +
                "   id: " + messageId + ", " +
                "   title: '" + title.replace("'", "\\'") + "', " +
                "   text: '" + body.replace("'", "\\'") + "', " +
                "   foreground: true, " +
                "   vibrate: true " +
                "}); " +
                "console.log('✅ Notificación enviada'); " +
                "}";
            
            cordova.getActivity().runOnUiThread(() -> {
                webView.loadUrl("javascript:" + jsCode);
                Log.i(TAG, "✅ Código JavaScript ejecutado");
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error activando notificación", e);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fcmReceiver != null) {
            cordova.getActivity().unregisterReceiver(fcmReceiver);
            Log.i(TAG, "📡 BroadcastReceiver desregistrado");
        }
    }
}