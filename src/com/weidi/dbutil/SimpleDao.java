package com.weidi.dbutil;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by root on 17-1-4.
 */

public final class SimpleDao extends BaseDaoImpl {

    private volatile static SimpleDao sSimpleDao;
    private static Context mContext;


    private SimpleDao(Context context) {
        super(context);
    }

    /**
     * 在Application这个类的onCreate()方法里先设置一下
     *
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
    }

    /**
     * 可以在任何地方使用了
     *
     * @return
     */
    public static SimpleDao getInstance() {
        if (sSimpleDao == null) {
            synchronized (SimpleDao.class) {
                if (sSimpleDao == null) {
                    sSimpleDao = new SimpleDao(mContext);
                }
            }
        }
        return sSimpleDao;
    }

    public long add(Class<?> clazz, Object object) {
        return super.add(clazz, object);
    }

    public long add(Class<?> clazz, ContentValues values) {
        return super.add(clazz, values);
    }

    public long add2(
            Class<?> clazz,
            Object object,
            String primaryKey,
            String primaryValue) {
        return super.add2(clazz, object, primaryKey, primaryValue);
    }

    public long add2OrUpdate(
            Class<?> clazz,
            Object object,
            String primaryKey,
            String primaryValue) {
        return super.add2OrUpdate(clazz, object, primaryKey, primaryValue);
    }

    public long add2(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.add2(clazz, values, primaryKey, primaryValue);
    }

    public long add2OrUpdate(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.add2OrUpdate(clazz, values, primaryKey, primaryValue);
    }

    public int delete(Class<?> clazz, int _id) {
        return super.delete(clazz, _id);
    }

    public int delete(
            Class<?> clazz,
            String primaryKey,
            String primaryValue) {
        return super.delete(clazz, primaryKey, primaryValue);
    }

    public int delete(Class<?> clazz, Map<String, String> map) {
        return super.delete(clazz, map);
    }

    public int update(
            Class<?> clazz,
            ContentValues values,
            int _id) {
        return super.update(clazz, values, _id);
    }

    public int update(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.update(clazz, values, primaryKey, primaryValue);
    }

    public int update(
            Class<?> clazz,
            ContentValues values,
            Map<String, String> map) {
        return super.update(clazz, values, map);
    }

    public Object querySingle(
            Class<?> clazz,
            int _id) {
        return super.querySingle(clazz, _id);
    }

    public Object querySingle(
            Class<?> clazz,
            String primaryKey,
            String primaryValue) {
        return super.querySingle(clazz, primaryKey, primaryValue);
    }

    public Object querySingle(
            Class<?> clazz,
            Object object,
            boolean isIdPrimary) {
        return super.querySingle(clazz, object, isIdPrimary);
    }

    public Object querySingle(
            Class<?> clazz,
            Map<String, String> map) {
        return super.querySingle(clazz, map);
    }

    public ArrayList queryMore(
            Class<?> clazz,
            String key,
            String value) {
        return super.queryMore(clazz, key, value);
    }

    public ArrayList queryMore(
            Class<?> clazz,
            Map<String, String> map) {
        return super.queryMore(clazz, map);
    }

    public ArrayList queryAll(
            Class<?> clazz) {
        return super.queryAll(clazz);
    }

    public boolean isExists(Class<?> clazz, int _id) {
        return super.isExists(clazz, _id);
    }

    public boolean isExists(
            Class<?> clazz,
            String primaryKey,
            String primaryValue) {
        return super.isExists(clazz, primaryKey, primaryValue);
    }

    public boolean isExists(
            Class<?> clazz,
            Object object,
            boolean isIdPrimary) {
        return super.isExists(clazz, object, isIdPrimary);
    }

    public boolean isExists(
            Class<?> clazz,
            Map<String, String> map) {
        return super.isExists(clazz, map);
    }

    public long getLastId(
            Class<?> clazz) {
        return super.getLastId(clazz);
    }

}
