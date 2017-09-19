package com.weidi.dbutil;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by root on 17-1-4.
 */

public final class SimpleDao2 extends BaseDaoImpl {

    private volatile static SimpleDao2 sSimpleDao;
    private static Context mContext;
    private Class<?> clazz;


    private SimpleDao2(Context context) {
        super(context);
    }

    /**
     * 在Application这个类的onCreate()方法里设置
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
    public static SimpleDao2 getInstance() {
        if (sSimpleDao == null) {
            synchronized (SimpleDao2.class) {
                if (sSimpleDao == null) {
                    sSimpleDao = new SimpleDao2(mContext);
                }
            }
        }
        return sSimpleDao;
    }

    /**
     * 对不同的类进行操作时记得先设置Class对象
     *
     * @param clazz
     */
    public void setClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("设置的Class对象不能为null");
        }
        this.clazz = clazz;
    }

    public synchronized long add(Object object) {
        return super.add(clazz, object);
    }

    public synchronized long add(ContentValues values) {
        return super.add(clazz, values);
    }

    public synchronized int delete(int _id) {
        return super.delete(clazz, _id);
    }

    public synchronized int delete(
            String primaryKey,
            String primaryValue) {
        return super.delete(clazz, primaryKey, primaryValue);
    }

    public synchronized int delete(Map<String, String> map) {
        return super.delete(clazz, map);
    }

    public synchronized int update(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.update(clazz, values, primaryKey, primaryValue);
    }

    public synchronized int update(
            ContentValues values,
            Map<String, String> map) {
        return super.update(clazz, values, map);
    }

    public synchronized Object querySingle(int _id) {
        return super.querySingle(clazz, _id);
    }

    public synchronized Object querySingle(
            Object object,
            boolean isIdPrimary) {
        return super.querySingle(clazz, object, isIdPrimary);
    }

    public synchronized Object querySingle(
            String primaryKey,
            String primaryValue) {
        return super.querySingle(clazz, primaryKey, primaryValue);
    }

    public synchronized Object querySingle(Map<String, String> map) {
        return super.querySingle(clazz, map);
    }

    public synchronized ArrayList queryMore(
            String primaryKey,
            String primaryValue) {
        return super.queryMore(clazz, primaryKey, primaryValue);
    }

    public synchronized ArrayList queryMore(
            Map<String, String> map) {
        return super.queryMore(clazz, map);
    }

    public synchronized ArrayList queryAll() {
        return super.queryAll(clazz);
    }

    public synchronized boolean isExists(
            String primaryKey,
            String primaryValue) {
        return super.isExists(clazz, primaryKey, primaryValue);
    }

    public synchronized boolean isExists(
            Object object,
            boolean isIdPrimary) {
        return super.isExists(clazz, object, isIdPrimary);
    }

    public synchronized boolean isExists(
            Map<String, String> map) {
        return super.isExists(clazz, map);
    }

    public synchronized long getLastId() {
        return super.getLastId(clazz);
    }

    public synchronized long add2(
            Object object,
            String primaryKey,
            String primaryValue) {
        return super.add2(clazz, object, primaryKey, primaryValue);
    }

    public synchronized long add2OrUpdate(
            Object object,
            String primaryKey,
            String primaryValue) {
        return super.add2OrUpdate(clazz, object, primaryKey, primaryValue);
    }

    public synchronized long add2(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.add2(clazz, values, primaryKey, primaryValue);
    }

    public synchronized long add2OrUpdate(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        return super.add2OrUpdate(clazz, values, primaryKey, primaryValue);
    }

}
