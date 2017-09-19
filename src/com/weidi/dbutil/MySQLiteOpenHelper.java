package com.weidi.dbutil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/***
 数据库帮助类

 如果只是单单创建MySQLiteOpenHelper对象，即使是第一次也不会回调onCreate()方法，
 只有MySQLiteOpenHelper对象调用了getReadableDatabase者getWritableDatabase()
 方法后才会回调onCreate()方法。调用之后，以后执行相同的操作都不会被再次调用。
 只有当数据库没有了，再次执行相同的操作后，onCreate()方法才会被回调。
 version这个参数的值必须大于0。

 要得到数据库名用helper.getDatabaseName()。
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "artifact.db";
    public static final String SHAREDPREFERENCES = "record_value";

    private SQLiteDatabase mReadableDb;
    private SQLiteDatabase mWritableDb;

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);
        mReadableDb = getReadableDatabase();
        mWritableDb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public SQLiteDatabase getReadableDb() {
        if (mReadableDb == null || !mReadableDb.isOpen()) {
            mReadableDb = getReadableDatabase();
        }
        return mReadableDb;
    }

    public SQLiteDatabase getWritableDb() {
        if (mWritableDb == null || !mWritableDb.isOpen()) {
            mWritableDb = getWritableDatabase();
        }
        return mWritableDb;
    }

    public void closeDb() {
        if (mReadableDb != null && mReadableDb.isOpen()) {
            mReadableDb.close();
            mReadableDb = null;
        }
        if (mWritableDb != null && mWritableDb.isOpen()) {
            mWritableDb.close();
            mWritableDb = null;
        }
    }

    public void execSQL(String sql) {
        if (TextUtils.isEmpty(sql)) {
            return;
        }
        getWritableDb().execSQL(sql);
    }

}
