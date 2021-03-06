package com.ryan.appsearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class AlphabeticalLayoutFragment extends Activity {

    private AppInfo[] theApps;
    private Context theC;
    private boolean isHoloDark = true;
    private static final String showChatHead = "isPro";
    private static final String isHoloDarkKey = "HoloDark";

    @Override
    public void onCreate(Bundle savedInstance)
    {
        //Set the theme, either Holo Dark or Holo Light
        isHoloDark = isHoloDark();
        if(!isHoloDark)
            setTheme(android.R.style.Theme_Holo_Light);
        else
            setTheme(android.R.style.Theme_Holo);

        super.onCreate(savedInstance);

        theC = getApplicationContext();

        //All previously stored apps.
        final SQLiteAppSearcherDatabase mySQLiteAdapter = new SQLiteAppSearcherDatabase(theC);

        //Get the stored apps and remove duplicates
        theApps = removeDuplicates(mySQLiteAdapter.getAllApps());
        mySQLiteAdapter.close();

        Arrays.sort(theApps, new Comparator<AppInfo>() {
            public int compare(AppInfo first, AppInfo second)
            {
                return first.getAppName().compareTo(second.getAppName());
            }
        });

        LinearLayout theLL = new LinearLayout(theC);
        theLL.setOrientation(LinearLayout.VERTICAL);
        theLL.setPadding(10, 10, 0, 0);
        theLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        for(int i = 0; i < theApps.length; i++)
            theLL.addView(getView(i, theApps[i], false));

        ScrollView theSV = new ScrollView(theC);
        theSV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        theSV.addView(theLL);
        theSV.setPadding(20, 20, 0, 0);

        setContentView(theSV);
    }

    /** Return TextView for each app */
    public TextView getView(final int counter, final AppInfo app, boolean showNumOpened)
    {
        TextView theView = new TextView(theC);

        if(counter % 2 == 0)
        {
            if(!isHoloDark)
                theView.setTextColor(android.graphics.Color.GRAY);
                //theView.setTextColor(Color.parseColor("#ff0099cc"));//<-- Blue
            else
                theView.setTextColor(Color.WHITE);
        }
        else
        {
            if(!isHoloDark)
                theView.setTextColor(Color.BLACK);
            else
                theView.setTextColor(Color.GRAY);
        }

        if(showNumOpened)
        {
            int n = app.getNumTime();
            String text = "(" + n + " time";
            if(n == 0 || n > 1)
                text += "s)";
            else
                text += ")";
            text += ": " + app.getAppName();
            theView.setText(text);
        }
        else
            theView.setText(app.getAppName());

        theView.setTextSize(20);
        theView.setPadding(0, 0, 0, 20);

        theView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(app.getAppOpen())));
                incrementApp(theApps[counter]);
                //if(theAlert != null)
                    //theAlert.cancel();
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
    public void incrementApp(final AppInfo theApp)
    {
        final SQLiteAppSearcherDatabase mySQLiteAdapter = new SQLiteAppSearcherDatabase(theC);

        //Delete the old app
        mySQLiteAdapter.deleteApp(theApp);

        //Increment the number of times it was opened
        theApp.increment();

        //Re-add the app
        mySQLiteAdapter.addApp(theApp);
    }

    /** Return if the theme is Holo Dark */
    public boolean isHoloDark()
    {
        try
        {
            SharedPreferences thePrefs =
                    getApplicationContext().getSharedPreferences("com.ryan.appsearcher", Context.MODE_PRIVATE);
            return thePrefs.getBoolean(isHoloDarkKey, true);
        }
        catch (Exception e) { return false; }
    }


    /** Try to delete the app if long clicked */
    public void deleteApp(final AppInfo theApp)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + theApp.getAppOpen()));
            startActivity(intent);

            SQLiteAppSearcherDatabase theDB = new SQLiteAppSearcherDatabase(theC);
            theDB.deleteApp(theApp);
            theDB.close();
        }
        catch (Exception e) {}
    }


    public AppInfo[] removeDuplicates(List<AppInfo> theApps)
    {
        SortedSet<AppInfo> theSorted = new TreeSet<AppInfo>(new Comparator<AppInfo>()
        {
            @Override
            public int compare(AppInfo arg0, AppInfo arg1)
            {
                return arg0.getAppOpen().compareTo(arg1.getAppOpen());
            }
        });
        theSorted.addAll(theApps);
        return theSorted.toArray(new AppInfo[theSorted.size()]);
    }

}
