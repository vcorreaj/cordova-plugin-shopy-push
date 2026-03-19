package com.shopy.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ShopyPushReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ShopyPushReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed - restarting push service");
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reiniciar el servicio después del reinicio del dispositivo
            Intent serviceIntent = new Intent(context, ShopyPushService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}