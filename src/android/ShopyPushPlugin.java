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
import android.os.PowerManager;  // ← AGREGADO
import android.provider.Settings;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    private static CallbackContext notificationCallbackContext;
    private static CallbackContext tokenRefreshCallbackContext;
    private static Map<String, Object> lastNotificationData;
    
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        
        Context context = cordova.getActivity().getApplicationContext();
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        
        startPushService();
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
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
                return false;
        }
    }
    
    private void getToken(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            callbackContext.success(task.getResult());
                        } else {
                            callbackContext.error("Error obteniendo token FCM");
                        }
                    });
            }
        });
    }
    
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