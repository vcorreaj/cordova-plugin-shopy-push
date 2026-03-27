package sigmisa.com.shopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ShopyPushReceiver extends BroadcastReceiver {
    private static final String TAG = "ShopyPushReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Receiver triggered: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            Log.d(TAG, "Device boot completed, starting ShopyPushService");
            startService(context);
        }
    }
    
    private void startService(Context context) {
        Intent serviceIntent = new Intent(context, ShopyPushService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}