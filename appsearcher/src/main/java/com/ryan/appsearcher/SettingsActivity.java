package com.ryan.appsearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private int width, height;
    private Context theContext;
    private boolean isHoloDark;
    private boolean isDefaultIcon;
    private static final String showChatHead = "isPro";
    private static final String isHoloDarkKey = "HoloDark";
    private static final String isDefaultChatHeadKey = "defaultIcon";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        theContext = this;
        //Set theme, either Holo Dark or Holo Light
        isHoloDark = getHoloDarkStatus();
        if(isHoloDark)
            setTheme(android.R.style.Theme_Holo);
        else
            setTheme(android.R.style.Theme_Holo_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Get the width and height of the screen
        getWidthHeight();

        //For Chat head Status (either on or off)
        final Switch chatHeadStatus = (Switch) findViewById(R.id.appSearcherChatHeadSwitch);
        chatHeadStatus.setChecked(getChatHeadStatus());
        chatHeadStatus.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                saveChatHeadStatus(isChecked);
                if(isChecked)
                {
                    stopChatHead();
                    makeToast("Saved: Chat Head On");
                }
                else
                {
                    startCheadHead();
                    makeToast("Saved: Chat Head Off");
                }
            }
        });

        //For Holo theme, either Holo Dark or Holo Light
        final Switch holoDarkStatus = (Switch) findViewById(R.id.appSearcherHoloDarkSwitch);
        holoDarkStatus.setChecked(getHoloDarkStatus());
        holoDarkStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isHoloDark)
            {
                saveHoloDarkStatus(isHoloDark);
                String add = "";//"New theme will be applied after app restarts";
                if(isHoloDark)
                    makeToast("Saved: Holo Dark Theme On. " + add);
                else
                    makeToast("Saved: Holo Light Theme Off. " + add);
                recreate();

            }
        });

        //For whether chat head icon should be default
        final Switch chatHeadIcon = (Switch) findViewById(R.id.appSearcherChatHeadIconSwitch);
        chatHeadIcon.setChecked(getChatHeadIconDefault());
        chatHeadIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                saveChatHeadIconDefault(status);
                if(status)
                    makeToast("Saved: Default Icon On");
                else
                    makeToast("Saved: Default Icon Off");
                startCheadHead();
                stopChatHead();
            }
        });

        //Sets button for Rate on App Store
        setRateOnAppStore();

        //Sets button for Email Developer
        setEmailDeveloper();

        //Sets button for Request a Feature
        setRequestFeature();

        //Sets button to show explanation text
        showExplanation();

        startNotificationBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Intent theIntent = new Intent(this, AppSearcherHomeScreen.class);
            Intent theIntent = new Intent(this, AppSearcherHomeScreen.class);
            theIntent.putExtra("animation", false);
            startActivity(theIntent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /** Returns the status of the chat head */
    public boolean getChatHeadStatus()
    {
        SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
        return prefs.getBoolean(showChatHead, false);
    }

    /** Saves the status of the chat head */
    public void saveChatHeadStatus(final boolean theVal)
    {
        SharedPreferences.Editor editor =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", theContext.MODE_PRIVATE).edit();
        editor.putBoolean(showChatHead, theVal);
        editor.commit();
    }

    /** Returns the status of the theme */
    public boolean getHoloDarkStatus()
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);

        try { return prefs.getBoolean(isHoloDarkKey, true); }
        catch (Exception e) { return true; }
    }

    /** Saves the status of the theme */
    public void saveHoloDarkStatus(final boolean isHoloDark)
    {
        SharedPreferences.Editor editor =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", theContext.MODE_PRIVATE).edit();
        editor.putBoolean(isHoloDarkKey, isHoloDark);
        editor.commit();
    }

    /** Returns whether chathead icon should be default */
    public boolean getChatHeadIconDefault()
    {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
        try { return prefs.getBoolean(isDefaultChatHeadKey, true); }
        catch (Exception e) { return true; }
    }

    /** Saves whether chathead icon should be default */
    public void saveChatHeadIconDefault(final boolean status)
    {
        SharedPreferences.Editor editor =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", theContext.MODE_PRIVATE).edit();
        editor.putBoolean(isDefaultChatHeadKey, status);
        editor.commit();
    }


    public void setRequestFeature()
    {
        final Button reqFet = (Button) findViewById(R.id.requestAFeatureButton);
        reqFet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                String[] recipients = new String[]{"dsouzarc@gmail.com", "",};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feature Request");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, "Report feature to developer via email"));
            }
        });
    }

    public void setEmailDeveloper()
    {
        final Button email = (Button) findViewById(R.id.emailDevReportBugButton);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                String[] recipients = new String[]{"dsouzarc@gmail.com", "",};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, "Report bug to developer via email"));
            }
        });
    }

    public void setRateOnAppStore()
    {
        final Button rate = (Button) findViewById(R.id.rateOnPlayStoreButton);

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Uri uri = Uri.parse("market://details?id=" + theContext.getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try
                    {
                        startActivity(goToMarket);
                    }
                    catch (Exception e)
                    {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" +
                                theContext.getPackageName())));
                    }
                }
                catch(Exception e) { e.printStackTrace(); }
            }
        });
    }

    public void stopChatHead()
    {
        try
        {
            startService(new Intent(theContext, ChatHeadService.class));
        }
        catch(Exception e) {}
    }

    public void startCheadHead()
    {
        try
        {
            stopService(new Intent(theContext, ChatHeadService.class));
        }
        catch (Exception e) {}
    }

    public void startNotificationBar()
    {
        try
        {
            startService(new Intent(theContext, NotificationBarService.class));
        }
        catch (Exception e) {}
    }

    private void makeToast(final String message)
    {
        Toast.makeText(theContext, message, Toast.LENGTH_SHORT).show();
    }

    public void showExplanation()
    {
        TextView theExp = (TextView) findViewById(R.id.infoTextView);
        final String desc = "AppSearcher was created to function as an app searcher, " +
                "bringing Android users the efficient, easy, and convenient to use " +
                "app searcher that iOS users have. AppSearcher can be opened in three ways. " +
                "The first and most obvious way is via its app icon. " +
                "The second way is by clicking on its spot in the Notification Bar. The last way " +
                "way is by clicking on the chat head that should open every time AppSearcher runs. " +
                "Below, you have the option to either enable or disable the chat head. " +
                "Note, long clicking on the chat head will make it disappear. The chat head can " +
                "also be dragged around the screen. If you accidentally close the chat head, " +
                " reopen the app from either the notification bar or app icon, and the chat head will " +
                " reappear. " +
                "Although AppSearcher runs in the background, it uses very little memory and will not " +
                "have any effect on either this phone's performance or battery life. " +
                "If you have any recommendations for AppSearcher or would like to report a bug, " +
                "feel free to email the developer by clicking the respective button below. " +
                "If you've enjoyed AppSearcher, please rate it on the Play Store by clicking on the button below";
        theExp.setMaxHeight(height/4);
        theExp.setTextSize(18);
        theExp.setMovementMethod(new ScrollingMovementMethod());

        if(isHoloDark)
            theExp.setTextColor(Color.GRAY);
        else
            theExp.setTextColor(Color.parseColor("#ff0099cc"));
        theExp.setText(desc);
    }

    /** Intializes global variables with width and height */
    public void getWidthHeight()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    public void setActionBar()
    {
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.BLACK);
            }
        }
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.activitybar_color));
        getActionBar().setTitle("Settings");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
