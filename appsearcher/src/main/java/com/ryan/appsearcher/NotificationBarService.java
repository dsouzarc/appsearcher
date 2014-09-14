package com.ryan.appsearcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationBarService extends Service {

    @Override
    public void onCreate()
    {
        showRecordingNotification();
    }

    private void showRecordingNotification()
    {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification not =
                new Notification(R.drawable.ic_launcher, "App Searcher Started", System.currentTimeMillis());

        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, (new Intent(this, AppSearcherHomeScreen.class)).putExtra("animation", false),
                Notification.FLAG_ONGOING_EVENT);

        not.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        not.setLatestEventInfo(this, "App Searcher", "Tap to search for an app", contentIntent);

        mNotificationManager.notify(1, not);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
