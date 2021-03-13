package com.learn.library.fgtit.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * The class of configuring the SQLite database
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_USER_VERSION = 1;
    private static final String DB_USER = "user.db";
    public static final String TABLE_USER = "User";
    public static final String TABLE_USER_ID = "userId";
    public static final String TABLE_USER_ENROL1 = "userEnrol1";


    public DBHelper(Context context) {
        super(context, getMyDatabaseName(context), null, DB_USER_VERSION);
    }

    /**
     * re-write getMyDatabaseName method
     * save it into SD card
     * @param context
     * @return name of the database
     */
    private static String getMyDatabaseName(Context context) {
        String databasename;
        boolean isSdcardEnable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {  //check SDCard is plug in or not
            isSdcardEnable = true;
        }
        String dbPath;
        if (isSdcardEnable) {
            dbPath = Environment.getExternalStorageDirectory().getPath() + "/OnePass/";
        } else {   //if SDCard is not plugged save it into memory
            dbPath = context.getFilesDir().getPath() + "/OnePass/";
        }
        File dbp = new File(dbPath);
        if (!dbp.exists()) {
            dbp.mkdirs();
        }
        databasename = dbPath + DB_USER;
        return databasename;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table if not exists " + TABLE_USER + " ("
                + TABLE_USER_ID + " integer primary key AUTOINCREMENT,"
                + TABLE_USER_ENROL1 + " text)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
