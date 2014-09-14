package com.ryan.appsearcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartServiceAtBoostReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceIntent = new Intent(context, NotificationBarService.class);
        context.startService(serviceIntent);

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            SharedPreferences prefs =
                    context.getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);

            boolean proVersion = prefs.getBoolean("isPro", false);

            if(proVersion)
            {
                Intent chatHead = new Intent(context, ChatHeadService.class);
                context.startService(chatHead);
            }
        }
    }
}
