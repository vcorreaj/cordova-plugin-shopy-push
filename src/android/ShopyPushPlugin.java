package com.shopy.push;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

public class ShopyPushPlugin extends CordovaPlugin {
    
    private static final String TAG = "ShopyPushPlugin";
    
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Log.i(TAG, "========================================");
        Log.i(TAG, "ShopyPushPlugin inicializado");
        Log.i(TAG, "========================================");
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "execute: " + action);
        
        switch (action) {
            case "isServiceRunning":
                callbackContext.success(ShopyPushService.isRunning() ? 1 : 0);
                return true;
            default:
                Log.w(TAG, "Acción desconocida: " + action);
                return false;
        }
    }
}