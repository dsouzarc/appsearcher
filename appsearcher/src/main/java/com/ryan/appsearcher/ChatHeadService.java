package com.ryan.appsearcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private ImageView chatHead;
    private static final String showChatHead = "isPro";
    private static final String isDefaultChatHeadKey = "defaultIcon";

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = new ImageView(this);

        if(getChatHeadIconDefault())
            chatHead.setImageResource(R.drawable.ic_launcher);
        else
            chatHead.setImageResource(R.drawable.search_icon);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP;// | Gravity.RIGHT;
        params.x = 150;
        params.y = 150;


        final RelativeLayout.LayoutParams params_imageview = new RelativeLayout.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        params_imageview.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        try
        {
            chatHead.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            // Get current time in nano seconds.
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            try { windowManager.updateViewLayout(chatHead, paramsF); }
                            catch (Exception e) {}
                            break;
                    }
                    return false;
                }
            });
        }
        catch (Exception e) { }

        chatHead.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                Intent dialogIntent = new Intent(getBaseContext(), AppSearcherHomeScreen.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try
                {
                    dialogIntent.putExtra("animation", false);
                    getApplication().startActivity(dialogIntent);
                }
                catch (Exception e) { e.printStackTrace(); }
            }
        });


        chatHead.setOnLongClickListener(new android.view.View.OnLongClickListener() {
            @Override
            public boolean onLongClick(android.view.View view) {
                stopSelf();
                return false;
            }
        });

        if(getChatHeadStatus())
            windowManager.addView(chatHead, params);
    }

    public boolean getChatHeadStatus()
    {
        try
        {
            SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);

            return prefs.getBoolean(showChatHead, false);
        }
        catch (Exception e) { return false; }
    }

    /** Returns whether chathead icon should be default */
    public boolean getChatHeadIconDefault()
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
        try { return prefs.getBoolean(isDefaultChatHeadKey, true); }
        catch (Exception e) { return true; }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try
        {
            if (chatHead != null)
                windowManager.removeView(chatHead);
        }
        catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}