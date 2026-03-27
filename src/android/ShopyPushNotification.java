package sigmisa.com.shopy;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Map;

public class ShopyPushNotification {
    
    private static final String TAG = "ShopyPushNotification";
    private static final String DEFAULT_CHANNEL_ID = "shopy_notifications_channel";
    
    // Crear canal de notificación
    public static void createNotificationChannel(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId != null ? channelId : DEFAULT_CHANNEL_ID,
                "Shopy Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de Shopy");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + channelId);
            }
        }
    }
    
    // Mostrar notificación push
    public static void showNotification(Context context, String channelId, 
                                        String title, String body, Map<String, Object> data) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (channelId == null) {
                channelId = DEFAULT_CHANNEL_ID;
            }
            
            // Crear canal si no existe
            createNotificationChannel(context, channelId);
            
            // Intent para abrir la app al tocar la notificación
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                // Agregar datos extras para navegación
                intent.putExtra("fromNotification", true);
                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        if (entry.getValue() != null) {
                            intent.putExtra(entry.getKey(), entry.getValue().toString());
                        }
                    }
                }
                intent.putExtra("screen", "/tabs/notifications");
            }
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, flags);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title != null ? title : "Nueva notificación")
                .setContentText(body != null ? body : "")
                .setSmallIcon(getNotificationIcon(context))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            
            // Configurar vibración y sonido
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
            
            Notification notification = builder.build();
            
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, notification);
            
            Log.d(TAG, "Notification shown: " + title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage(), e);
        }
    }
    
    // Crear notificación para foreground service
    public static Notification createForegroundNotification(Context context, String channelId, 
                                                           String title, String text) {
        if (channelId == null) {
            channelId = DEFAULT_CHANNEL_ID;
        }
        
        createNotificationChannel(context, channelId);
        
        // Intent para abrir la app al tocar la notificación
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);
        
        return new NotificationCompat.Builder(context, channelId)
            .setContentTitle(title != null ? title : "Shopy")
            .setContentText(text != null ? text : "Recibiendo notificaciones...")
            .setSmallIcon(getNotificationIcon(context))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true)
            .build();
    }
    
    // Obtener icono de notificación
    private static int getNotificationIcon(Context context) {
        int icon = context.getApplicationInfo().icon;
        if (icon == 0) {
            icon = android.R.drawable.ic_dialog_info;
        }
        return icon;
    }
    
    // Limpiar todas las notificaciones
    public static void clearAllNotifications(Context context) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
            Log.d(TAG, "All notifications cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing notifications: " + e.getMessage());
        }
    }
    
    // Verificar si la app está en primer plano
    public static boolean isAppInForeground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
                if (processes != null) {
                    for (ActivityManager.RunningAppProcessInfo process : processes) {
                        if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            for (String pkg : process.pkgList) {
                                if (pkg.equals(context.getPackageName())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else {
                List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (topActivity != null && topActivity.getPackageName().equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking foreground: " + e.getMessage());
        }
        return false;
    }
}