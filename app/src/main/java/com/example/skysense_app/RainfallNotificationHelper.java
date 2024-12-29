package com.example.skysense_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;


public class RainfallNotificationHelper {
    private static final String CHANNEL_ID = "rainfall_channel";
    private static final int NOTIFICATION_ID = 1;
    private Context context;
    private NotificationManager notificationManager;
    private double previousRainfall = 0.0;
    private String previousCategory = "Tidak Hujan";

    public RainfallNotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rainfall Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for rainfall alerts");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getRainfallCategory(double rainfall) {
        if (rainfall <= 0) {
            return "Tidak Hujan";
        } else if (rainfall <= 5) {
            return "Hujan Ringan";
        } else if (rainfall <= 10) {
            return "Hujan Sedang";
        } else if (rainfall <= 20) {
            return "Hujan Lebat";
        } else {
            return "Hujan Sangat Lebat";
        }
    }

    public void checkRainfallAndNotify(double rainfall) {
        String currentCategory = getRainfallCategory(rainfall);

        // Cek apakah ada perubahan kategori
        if (!currentCategory.equals(previousCategory)) {
            String title;
            String message;

            if (rainfall <= 0) {
                if (!previousCategory.equals("Tidak Hujan")) {
                    title = "Hujan Berhenti";
                    message = "Cuaca sudah berubah menjadi tidak hujan";
                    sendNotification(title, message);
                }
            } else {
                title = "Perubahan Intensitas Hujan";
                message = "Curah hujan saat ini " + rainfall + " mm (" + currentCategory + ")";
                sendNotification(title, message);
            }
        }

        // Update status sebelumnya
        previousRainfall = rainfall;
        previousCategory = currentCategory;
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}