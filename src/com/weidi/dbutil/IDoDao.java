package com.weidi.dbutil;

import android.content.ContentValues;

import java.util.List;
import java.util.Map;

/**
 * Created by root on 16-7-31.
 */

public interface IDoDao {

    long add(Class<?> clazz, Object object);

    long add(Class<?> clazz, ContentValues values);

    int delete(Class<?> clazz, int id);

    int delete(Class<?> clazz, Map<String, String> map);

    int update(Class<?> clazz, ContentValues values, Map<String, String> map);

    Object querySingle(Class<?> clazz, int id);

    Object querySingle(Class<?> clazz, Map<String, String> map);

    List<Object> queryMore(Class<?> clazz, Map<String, String> map);

}
