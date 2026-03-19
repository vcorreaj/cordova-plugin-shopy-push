package com.shopy.push;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Log.i(TAG, "Iniciando ShopyPushPlugin (background service)");
        startBackgroundService();
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "startBackgroundService":
                startBackgroundService();
                callbackContext.success();
                return true;
            case "stopBackgroundService":
                stopBackgroundService();
                callbackContext.success();
                return true;
            case "isServiceRunning":
                isServiceRunning(callbackContext);
                return true;
            default:
                return false;
        }
    }
    
    private void startBackgroundService() {
        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, ShopyPushService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        Log.i(TAG, "✅ Servicio background iniciado");
    }
    
    private void stopBackgroundService() {
        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, ShopyPushService.class);
        context.stopService(intent);
        Log.i(TAG, "⏹️ Servicio background detenido");
    }
 
    private void isServiceRunning(CallbackContext callbackContext) {
    boolean isRunning = ShopyPushService.isRunning();
        Log.i(TAG, "Verificando estado del servicio: " + (isRunning ? "ACTIVO" : "INACTIVO"));
        callbackContext.success(isRunning ? 1 : 0);
    }
}