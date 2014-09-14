package com.ryan.appsearcher;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

public class AppSearcherHomeScreen extends TabActivity
{
    private ArrayAdapter<String> adapter;
    private Context theC;
    private LinearLayout theLayout;
    private AutoCompleteTextView actv;

    private AlertDialog theDialog = null;
    private AlertDialog theAlert = null;

    private static final String showChatHead = "isPro";
    private static final String isHoloDarkKey = "HoloDark";

    //Holds list of apps
    private AppInfo[] theApps;

    private boolean isAlphabetical = true;
    private boolean isHoloDark;
    private boolean isChatHead;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Set the theme, either Holo Dark or Holo Light
        isHoloDark = isHoloDark();
        if(!isHoloDark)
            setTheme(android.R.style.Theme_Holo_Light);
        else
            setTheme(android.R.style.Theme_Holo);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_home_screen);

        startNotificationBar();

        theC = this;

        //All previously stored apps.
        final SQLiteAppSearcherDatabase mySQLiteAdapter = new SQLiteAppSearcherDatabase(theC);
        theApps = removeDuplicates(mySQLiteAdapter.getAllApps());
        mySQLiteAdapter.close();

        //For searching
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        actv.setText("");

        //For adding stuff
        theLayout = (LinearLayout) findViewById(R.id.theLinear);

        //Holds app names ex. "Facebook", "AppSearcher"
        String[] appNames = new String[theApps.length];

        //Go through all stored apps
        for(int i = 0; i < theApps.length; i++) {
            //Store the app name in the array
            appNames[i] = theApps[i].getAppName();
        }

        //Autocomplete TV
        adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, appNames);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED);

        //When item is clicked
        actv.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //App name of that was clicked
                final String appNameS = ((TextView) view).getText().toString();

                //Look through all stored apps
                for(int i = 0; i < theApps.length; i++) {
                    //If the app name is the same as the stored name
                    if(appNameS.equals(theApps[i].getAppName())) {
                        //Official AppName and AppOpen
                        String selectedAppName = theApps[i].getAppName();
                        String selectedAppOpen = theApps[i].getAppOpen();

                        //Dialog to show we are trying to open the app
                        theAlert = new AlertDialog.Builder(theC)
                                .setTitle("")
                                .setMessage("Opening " + selectedAppName + ".")
                                .show();

                        //Try to open it
                        try {
                            startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(selectedAppOpen)));
                            incrementApp(theApps[i]);
                            if(theAlert != null)
                                theAlert.cancel();
                        }
                        catch (Exception e) { showToast("Sorry, something went wrong"); }

                        //Reset everything and exit loop
                        actv.setText("");
                        i = Integer.MAX_VALUE;
                    }
                }
            }
        });

        final TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

        TabSpec tab1 = tabHost.newTabSpec("Alphabetical");
        TabSpec tab2 = tabHost.newTabSpec("Most Used");

        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Alphabetical");
        tab1.setContent(new Intent(this, AlphabeticalLayoutFragment.class));

        tab2.setIndicator("Most Used");
        tab2.setContent(new Intent(this, MostUsedLayoutFragment.class));

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);

        //Get current list of apps, store it to database, and display
        new PrepareAdapter1().execute();

        //If enabled, open chat head
        if(isChatHead)
            startChatHead();

    }

    /** Gets current list of apps in the background
     * Saves it to database
     * Clears the current view and displays */
    private class PrepareAdapter1 extends AsyncTask<Void,Void, Void>
    {
        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params)
        {
            Thread.currentThread().setPriority(8);

            //Get a list of installed apps.
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            //If first time running, show we are trying to open the apps
            try {
                theAlert = new AlertDialog.Builder(theC)
                        .setTitle("Loading over " + packages.size() + " apps")
                        .setMessage("Please wait a few seconds while we load your apps. " +
                                "Don't worry. You should only see this message the first time " +
                                "you ever run this app. It'll be quicker after this. ")
                        .show();
            }
            catch (Exception e) {}

            //Show the number of apps there are in the ActionBar
            try { getActionBar().setTitle("AppSearcher. " + packages.size() + " apps"); }
            catch (Exception e) {}

            //Hold the actual app objects
            final LinkedList<AppInfo> newApps = new LinkedList<AppInfo>();

            //List of all stored apps
            long sApps = System.currentTimeMillis();
            final SQLiteAppSearcherDatabase mySQLiteAdapter = new SQLiteAppSearcherDatabase(theC);
            final HashMap<String, Short> savedApps =  mySQLiteAdapter.getHashMapApps();
            mySQLiteAdapter.close();
            log("Finished getting saved apps: " + (sApps - System.currentTimeMillis()));

            //Go through all official apps
            long installT = System.currentTimeMillis();
            for(int i = 0; i < packages.size(); i++) {
                final ApplicationInfo packageInfo = packages.get(i);

                //Official app and package name
                final String appName = pm.getApplicationLabel(packageInfo).toString();
                final String appOpen = packageInfo.packageName;

                Short theNumOpened = savedApps.get(appOpen);
                if(theNumOpened != null && theNumOpened > 0) {
                    newApps.add(new AppInfo(appName, appOpen, theNumOpened));
                }
                else {
                    newApps.add(new AppInfo(appName, appOpen));
                }
            }
            savedApps.clear();

            //Remove duplicates
            theApps = removeDuplicates(newApps);
            newApps.clear();
            log("Finished removing duplicates: " + (installT - System.currentTimeMillis()));

            //Update the database
            new UpdateDatabase().execute();

            return null;
        }

        /** Remove the list of old apps and show the new apps */
        @Override
        protected void onPostExecute(Void result) {
            //Remove all the TextViews
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            if(theDialog != null)
                theDialog.cancel();

            //Array of all the AppNames, ie. Facebook, AppSearcher
            final String[] appNames = new String[theApps.length];

            long rStart = System.currentTimeMillis();

            for(int i = 0; i < theApps.length; i++) {
                //Store its official name in the array
                appNames[i] = theApps[i].getAppName();
            }
            //theDB.close();
            log("Finished redraw " + (rStart - System.currentTimeMillis()));

            //The entered text
            final String enteredText = actv.getText().toString();

            //AutoComplete TextView
            actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
            adapter = new ArrayAdapter<String>(theC, android.R.layout.select_dialog_item, appNames);

            //Start at first character
            actv.setThreshold(1);
            actv.setAdapter(adapter);
            actv.setTextColor(Color.RED);
            actv.setText(enteredText);
            actv.setSelection(enteredText.length());

            //When item is clicked
            actv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //The AppName in the view
                    String appNameS = ((TextView) view).getText().toString();

                    //Look through all the stored apps
                    for(int i = 0; i < theApps.length && i != Integer.MAX_VALUE; i++) {
                        //If the two have the same name
                        if(appNameS.equals(theApps[i].getAppName())) {
                            //Get their app name and package
                            final String selectedAppName = theApps[i].getAppName();
                            final String selectedAppOpen = theApps[i].getAppOpen();

                            theAlert = new android.app.AlertDialog.Builder(theC)
                                    .setTitle("")
                                    .setMessage("Opening " + selectedAppName + "...")
                                    .show();
                            try {
                                //Try to start the activity
                                startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(selectedAppOpen)));

                                if(theAlert != null) {
                                    theAlert.cancel();
                                }

                                //Increment the counter
                                incrementApp(theApps[i]);

                                startNotificationBar();
                                if(isChatHead) {
                                    startChatHead();
                                }
                            }
                            catch (Exception e) { e.printStackTrace(); }

                            actv.setText("");

                            i = Integer.MAX_VALUE;
                            break;
                        }
                    }
                }
            });
        }
    }

    private class UpdateDatabase extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final long dbStart = System.currentTimeMillis();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            final SQLiteAppSearcherDatabase theDatabase = new SQLiteAppSearcherDatabase(theC);
            theDatabase.deleteAllApps();
            theDatabase.addApps(theApps);
            theDatabase.close();
            log("FINISHED DB SAVE: " + (System.currentTimeMillis() - dbStart));
            return null;
        }
    }

    /** Return TextView for each app */
    public TextView getView(final int counter, final AppInfo app, boolean showNumOpened) {
        final TextView theView = new TextView(theC);

        if(counter % 2 == 0) {
            if(!isHoloDark) {
                theView.setTextColor(android.graphics.Color.GRAY);
                //theView.setTextColor(Color.parseColor("#ff0099cc"));//<-- Blue
            }
            else {
                theView.setTextColor(Color.WHITE);
            }
        }
        else {
            if(!isHoloDark) {
                theView.setTextColor(Color.BLACK);
            }
            else {
                theView.setTextColor(Color.GRAY);
            }
        }

        if(showNumOpened) {
            int n = app.getNumTime();
            String text = "(" + n + " time";

            if(n == 0 || n > 1) {
                text += "s)";
            }
            else {
                text += ")";
            }
            text += ": " + app.getAppName();
            theView.setText(text);
        }
        else {
            theView.setText(app.getAppName());
        }

        theView.setTextSize(20);
        theView.setPadding(0, 0, 0, 20);

        theView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(app.getAppOpen())));
                incrementApp(theApps[counter]);
                if(theAlert != null) {
                    theAlert.cancel();
                }
            }
        });

        theView.setOnLongClickListener(new android.view.View.OnLongClickListener() {
            @Override
            public boolean onLongClick(android.view.View v) {
                deleteApp(app);
                return false;
            }
        });
        return theView;
    }


    /** Increment the app's number of times open counter in the database */
    public void incrementApp(final AppInfo theApp) {
        final SQLiteAppSearcherDatabase mySQLiteAdapter = new SQLiteAppSearcherDatabase(theC);

        //Delete the old app
        mySQLiteAdapter.deleteApp(theApp);

        //Increment the number of times it was opened
        theApp.increment();

        //Re-add the app
        mySQLiteAdapter.addApp(theApp);
    }

    /** Try to delete the app if long clicked */
    public void deleteApp(final AppInfo theApp) {
        try {
            final Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + theApp.getAppOpen()));
            startActivity(intent);

            final SQLiteAppSearcherDatabase theDB = new SQLiteAppSearcherDatabase(theC);
            theDB.deleteApp(theApp);
            theDB.close();
        }
        catch (Exception e) {}
    }

    public AppInfo[] removeDuplicates(List<AppInfo> theApps) {
        SortedSet<AppInfo> theSorted = new TreeSet<AppInfo>(new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo arg0, AppInfo arg1) {
                return arg0.getAppOpen().compareTo(arg1.getAppOpen());
            }
        });
        theSorted.addAll(theApps);
        return theSorted.toArray(new AppInfo[theSorted.size()]);
    }

    public void sortAppsAlphabetically() {
        Arrays.sort(theApps, new Comparator<AppInfo>() {
            public int compare(AppInfo a1, AppInfo a2) {
                return a1.getAppName().compareTo(a2.getAppName());
            }
        });
    }

    /** Returns whether chathead is enabled or disabled */
    public boolean getChatHeadStatus() {
        final SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
        isChatHead = prefs.getBoolean(showChatHead, false);
        return isChatHead;
    }

    /** Tries to start the chathead services */
    private void startChatHead() {
        try {
            startService(new Intent(this, ChatHeadService.class));
        }
        catch (Exception e) {}
    }

    /** Tries to start the NotificationBar service */
    private void startNotificationBar() {
        try {
            startService(new Intent(this, NotificationBarService.class));
        }
        catch (Exception e) {}
    }

    /** Return if the theme is Holo Dark */
    public boolean isHoloDark() {
        try {
            final SharedPreferences thePrefs =
                    getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
            return thePrefs.getBoolean(isHoloDarkKey, true);
        }
        catch (Exception e) { return false; }
    }

    /** Show Toast messages */
    public void showToast(final String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /** Log messages */
    public void log(final String logMessage)
    {
        Log.d("com.ryan.appsearcher", logMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater theInflater = getMenuInflater();
        theInflater.inflate(R.menu.homescreen_activitybar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean actionBarClickEvent(MenuItem item) {
        switch (item.getItemId()) {
            //If video record button in ActionMenu clicked
            case R.id.settings_icon_action:

                startActivity(new Intent(theC, SettingsActivity.class));
                finish();
                return true;

            case R.id.clear_ram_icon_action:
                showToast("Clearing RAM");

                //get a list of installed apps.
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(0);

                final ActivityManager mActivityManager =
                        (ActivityManager)theC.getSystemService(Context.ACTIVITY_SERVICE);

                for (ApplicationInfo packageInfo : packages) {
                    if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
                        continue;
                    if(packageInfo.packageName.equals("mypackage"))
                        continue;
                    mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                }
                showToast("Finished Clearing RAM");
                return true;

            case R.id.refresh_list_action:
                recreate();
                new PrepareAdapter1().execute();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroy() {
        startNotificationBar();
        super.onDestroy();
        startNotificationBar();
        if(isChatHead)
            startChatHead();
    }
}
