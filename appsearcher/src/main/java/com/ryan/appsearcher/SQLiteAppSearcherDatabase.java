package com.ryan.appsearcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLiteAppSearcherDatabase extends SQLiteOpenHelper
{
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "AppSearcherDB";

    public SQLiteAppSearcherDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        final String CREATE_APPSEARCHER_TABLE = "CREATE TABLE appsearcher ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "AppName TEXT, "+
                "AppOpen TEXT, "+
                "NumOpen SHORT )";

        // create books table
        db.execSQL(CREATE_APPSEARCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS appsearcher");

        // create fresh books table
        this.onCreate(db);
    }
    private final String TABLE_APPSEARCHER = "appsearcher";

    private final String KEY_ID = "id";
    private final String KEY_APP_NAME = "appname";
    private final String KEY_APP_OPEN = "appopen";
    private final String KEY_NUM_OPEN = "numopen";

    private final String[] COLUMNS = {KEY_ID, KEY_APP_NAME, KEY_APP_OPEN, KEY_NUM_OPEN};

    public void addApp(final AppInfo theApp)
    {
        // 1. get reference to writable DB
        final SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        final ContentValues values = new ContentValues();
        values.put(KEY_APP_NAME, theApp.getAppName());
        values.put(KEY_APP_OPEN, theApp.getAppOpen());
        values.put(KEY_NUM_OPEN, theApp.getNumTime());

        // 3. insert
        db.insert(TABLE_APPSEARCHER, null, values);

        // 4. close
        db.close();
    }

    public List<AppInfo> getAllApps()
    {
        final Set<AppInfo> theApps = new HashSet<AppInfo>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_APPSEARCHER;

        // 2. get reference to writable DB
        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst())
        {
            do
            {
                theApps.add(new AppInfo(cursor.getString(1),
                        cursor.getString(2), Short.parseShort(cursor.getString(3))));
            }
            while (cursor.moveToNext());
        }

        db.close();
        return new ArrayList<AppInfo>(theApps);
    }

    public HashMap<String, Short> getHashMapApps() {
        //The Hash Map
        final HashMap<String, Short> theMap = new HashMap<String, Short>();

        // 1. build the query
        final String query = "SELECT  * FROM " + TABLE_APPSEARCHER;

        // 2. get reference to writable DB
        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst())
        {
            do
            {
                theMap.put(cursor.getString(2), Short.parseShort(cursor.getString(3)));
            }
            while (cursor.moveToNext());
        }

        //log("GetAllApps: " + theMap.toString());
        db.close();

        return theMap;
    }

    public void deleteApp(AppInfo theApp)
    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        try {
            db.delete(TABLE_APPSEARCHER,
                    KEY_APP_OPEN+" = ?",
                    new String[] {theApp.getAppOpen()});
        } catch (Exception e) {}

        // 3. close
        db.close();
    }

    //Delete everything
    public void deleteAllApps() {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.delete("appsearcher", null, null);
        theDB.close();
    }

    public void addApps(AppInfo[] theApps) {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.beginTransaction();
        final ContentValues theVals = new ContentValues();
        for(AppInfo anApp : theApps) {
            theVals.put(KEY_APP_NAME, anApp.getAppName());
            theVals.put(KEY_APP_OPEN, anApp.getAppOpen());
            theVals.put(KEY_NUM_OPEN, anApp.getNumTime());
            theDB.insert(TABLE_APPSEARCHER, null, theVals);
        }
        theDB.setTransactionSuccessful();
        theDB.endTransaction();
        theDB.close();
    }


    public void log(final String message)
    {
        Log.d("com.ryan.appsearcher", message);
    }
}