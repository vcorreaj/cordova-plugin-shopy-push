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
        Log.i(TAG, "🔌 Plugin personalizado INICIALIZADO");
        Log.i(TAG, "✅ Actúa como background service");
        Log.i(TAG, "========================================");
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        if (action.equals("isServiceRunning")) {
            callbackContext.success(ShopyPushService.isRunning() ? 1 : 0);
            return true;
        }
        
        return false;
    }
}