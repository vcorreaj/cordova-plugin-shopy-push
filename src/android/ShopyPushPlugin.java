package com.shopy.push;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    private static CallbackContext notificationCallbackContext;
    private static CallbackContext tokenRefreshCallbackContext;
    private static Map<String, Object> lastNotificationData;
    private static boolean firebaseInitialized = false;
    
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Log.i(TAG, "========================================");
        Log.i(TAG, "pluginInitialize: Iniciando ShopyPushPlugin");
        Log.i(TAG, "========================================");
        
        // Intentar inicializar Firebase inmediatamente
        initializeFirebase();
        
        // Iniciar servicio en segundo plano
        startPushService();
    }
    
    private synchronized void initializeFirebase() {
        try {
            Log.i(TAG, "Inicializando Firebase...");
            
            Context context = cordova.getActivity().getApplicationContext();
            Log.i(TAG, "Context obtenido: " + context.getPackageName());
            
            // Verificar si ya hay instancias de Firebase
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.i(TAG, "No hay instancias de Firebase, creando nueva...");
                FirebaseApp.initializeApp(context);
                firebaseInitialized = true;
                Log.i(TAG, "✅ Firebase inicializado correctamente");
            } else {
                Log.i(TAG, "✅ Firebase ya estaba inicializado");
                firebaseInitialized = true;
            }
            
            // Verificar que FirebaseMessaging esté disponible
            try {
                FirebaseMessaging.getInstance();
                Log.i(TAG, "✅ FirebaseMessaging disponible");
            } catch (Exception e) {
                Log.e(TAG, "❌ FirebaseMessaging NO disponible: " + e.getMessage());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Firebase: " + e.getMessage(), e);
            firebaseInitialized = false;
        }
    }
    
    private void ensureFirebaseInitialized() throws Exception {
        if (!firebaseInitialized) {
            Log.w(TAG, "Firebase no inicializado, intentando de nuevo...");
            initializeFirebase();
        }
        if (!firebaseInitialized) {
            throw new Exception("Firebase no pudo inicializarse después de reintentar");
        }
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "execute: " + action);
        
        try {
            // Para acciones que requieren Firebase, asegurar inicialización
            if (action.equals("getToken") || action.equals("onTokenRefresh")) {
                ensureFirebaseInitialized();
            }
            
            switch (action) {
                case "getToken":
                    getToken(callbackContext);
                    return true;
                case "checkPermissions":
                    checkPermissions(callbackContext);
                    return true;
                case "requestPermissions":
                    requestPermissions(callbackContext);
                    return true;
                case "onNotification":
                    onNotification(callbackContext);
                    return true;
                case "onTokenRefresh":
                    onTokenRefresh(callbackContext);
                    return true;
                case "isServiceActive":
                    isServiceActive(callbackContext);
                    return true;
                case "getAndroidVersion":
                    getAndroidVersion(callbackContext);
                    return true;
                case "openAppSettings":
                    openAppSettings(callbackContext);
                    return true;
                case "clearAllNotifications":
                    clearAllNotifications(callbackContext);
                    return true;
                case "checkBatteryOptimizations":
                    checkBatteryOptimizations(callbackContext);
                    return true;
                case "openBatteryOptimizationSettings":
                    openBatteryOptimizationSettings(callbackContext);
                    return true;
                default:
                    Log.w(TAG, "Acción desconocida: " + action);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en execute: " + e.getMessage(), e);
            callbackContext.error("Error: " + e.getMessage());
            return true;
        }
    }
    
    private void getToken(final CallbackContext callbackContext) {
        Log.i(TAG, "getToken: iniciando...");
        
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Verificar Firebase antes de continuar
                    ensureFirebaseInitialized();
                    
                    Log.i(TAG, "Solicitando token a FirebaseMessaging...");
                    
                    FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String token = task.getResult();
                                Log.i(TAG, "✅ Token obtenido: " + token);
                                callbackContext.success(token);
                            } else {
                                Exception e = task.getException();
                                String errorMsg = (e != null) ? e.getMessage() : "Error desconocido";
                                Log.e(TAG, "❌ Error en task: " + errorMsg);
                                callbackContext.error("Error obteniendo token: " + errorMsg);
                            }
                        });
                        
                } catch (Exception e) {
                    Log.e(TAG, "❌ Excepción en getToken: " + e.getMessage(), e);
                    callbackContext.error("Error: " + e.getMessage());
                }
            }
        });
    }
    
    // ... resto de métodos igual que antes ...
    
    private void checkPermissions(CallbackContext callbackContext) {
        JSONObject result = new JSONObject();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.put("notificationPermission", 
                    cordova.getActivity().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                    == PackageManager.PERMISSION_GRANTED);
            } else {
                result.put("notificationPermission", true);
            }
            
            result.put("foregroundServicePermission", true);
            
        } catch (JSONException e) {
            callbackContext.error("Error creando respuesta");
            return;
        }
        callbackContext.success(result);
    }
    
    private void requestPermissions(CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cordova.requestPermission(this, 1001, android.Manifest.permission.POST_NOTIFICATIONS);
        }
        callbackContext.success("Permisos solicitados");
    }
    
    private void onNotification(CallbackContext callbackContext) {
        notificationCallbackContext = callbackContext;
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
        
        if (lastNotificationData != null) {
            sendNotificationData();
        }
    }
    
    private void onTokenRefresh(CallbackContext callbackContext) {
        tokenRefreshCallbackContext = callbackContext;
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
    
    private void isServiceActive(CallbackContext callbackContext) {
        JSONObject result = new JSONObject();
        try {
            result.put("isActive", ShopyPushService.isRunning());
        } catch (JSONException e) {
            callbackContext.error("Error creando respuesta");
            return;
        }
        callbackContext.success(result);
    }
    
    private void getAndroidVersion(CallbackContext callbackContext) {
        JSONObject result = new JSONObject();
        try {
            result.put("sdkVersion", Build.VERSION.SDK_INT);
            result.put("versionName", Build.VERSION.RELEASE);
            result.put("manufacturer", Build.MANUFACTURER);
            result.put("model", Build.MODEL);
        } catch (JSONException e) {
            callbackContext.error("Error creando respuesta");
            return;
        }
        callbackContext.success(result);
    }
    
    private void openAppSettings(CallbackContext callbackContext) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + cordova.getActivity().getPackageName()));
        cordova.getActivity().startActivity(intent);
        callbackContext.success();
    }
    
    private void clearAllNotifications(CallbackContext callbackContext) {
        ShopyPushNotification.clearAllNotifications(cordova.getActivity());
        callbackContext.success();
    }
    
    private void checkBatteryOptimizations(CallbackContext callbackContext) {
        JSONObject result = new JSONObject();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = cordova.getActivity().getPackageName();
                PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
                boolean isIgnoring = pm.isIgnoringBatteryOptimizations(packageName);
                result.put("isIgnoringBatteryOptimizations", isIgnoring);
            } else {
                result.put("isIgnoringBatteryOptimizations", true);
            }
        } catch (JSONException e) {
            callbackContext.error("Error checking battery optimizations");
            return;
        }
        callbackContext.success(result);
    }
    
    private void openBatteryOptimizationSettings(CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + cordova.getActivity().getPackageName()));
            cordova.getActivity().startActivity(intent);
        }
        callbackContext.success();
    }
    
    private void startPushService() {
        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, ShopyPushService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
    
    public static void sendNotificationData() {
        if (notificationCallbackContext != null && lastNotificationData != null) {
            JSONObject data = new JSONObject(lastNotificationData);
            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
            result.setKeepCallback(true);
            notificationCallbackContext.sendPluginResult(result);
            lastNotificationData = null;
        }
    }
    
    public static void setLastNotificationData(Map<String, Object> data) {
        lastNotificationData = data;
        sendNotificationData();
    }
    
    public static void sendTokenRefresh(String token) {
        if (tokenRefreshCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, token);
            result.setKeepCallback(true);
            tokenRefreshCallbackContext.sendPluginResult(result);
        }
    }
}