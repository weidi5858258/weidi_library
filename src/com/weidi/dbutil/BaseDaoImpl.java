package com.weidi.dbutil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;


import com.weidi.log.Log;
import com.weidi.utils.MyToast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.security.AccessController.getContext;

/***
 Created by root on 16-7-31.
 说明:
 1.只有add的操作才区分有没有把"_id"作为主键,其他操作时不区分
 2.add方法用于"_id"设为主键时,add2方法用于其他字符串作为主键时
 */

public class BaseDaoImpl extends ABaseDao {

    private static final String TAG = "BaseDaoImpl";
    private static final String INITIALIZING = "数据库正在初始化...";
    private static final String INITIALIZATION_FAILED = "数据库正在初始化失败,建议清除应用数据";
    private MySQLiteOpenHelper helper;
    private Context mContext;

    public static HashMap<String, String> mDataBackupAndRestoreMap;

    // public static SparseArray mMimeTypesSparseArray;

    static {
        mDataBackupAndRestoreMap = new HashMap<String, String>();
        mDataBackupAndRestoreMap.put("Contacts", "content://com.android.contacts/contacts");
        mDataBackupAndRestoreMap.put("RawContacts", "content://com.android.contacts/raw_contacts");
        mDataBackupAndRestoreMap.put("Data", "content://com.android.contacts/data");
        mDataBackupAndRestoreMap.put("MimeTypes", "content://com.android.contacts/mimetypes");
        mDataBackupAndRestoreMap.put("Sms", "content://sms");

        /*mMimeTypesSparseArray = new SparseArray();
        mMimeTypesSparseArray.put(1, "vnd.android.cursor.item/email_v2");
        mMimeTypesSparseArray.put(2, "vnd.android.cursor.item/im");
        mMimeTypesSparseArray.put(3, "vnd.android.cursor.item/nickname");
        mMimeTypesSparseArray.put(4, "vnd.android.cursor.item/organization");
        mMimeTypesSparseArray.put(5, "vnd.android.cursor.item/phone_v2");
        mMimeTypesSparseArray.put(6, "vnd.android.cursor.item/sip_address");
        mMimeTypesSparseArray.put(7, "vnd.android.cursor.item/name");
        mMimeTypesSparseArray.put(8, "vnd.android.cursor.item/postal-address_v2");
        mMimeTypesSparseArray.put(9, "vnd.android.cursor.item/identity");
        mMimeTypesSparseArray.put(10, "vnd.android.cursor.item/photo");
        mMimeTypesSparseArray.put(11, "vnd.android.cursor.item/group_membership");
        mMimeTypesSparseArray.put(12, "vnd.android.cursor.item/note");
        mMimeTypesSparseArray.put(13, "vnd.android.cursor.item/website");
        mMimeTypesSparseArray.put(
                14, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile");
        mMimeTypesSparseArray.put(
                15, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video");
        mMimeTypesSparseArray.put(
                15, "vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline");
        mMimeTypesSparseArray.put(
                15, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voiceaction");*/
    }

    public interface IOperDBResult {

        void onAddResult(boolean isSuccess);

        void onDeleteResult(boolean isSuccess);

        void onUpdateResult(boolean isSuccess);

        void onQueryResult(boolean isSuccess);

    }

    public BaseDaoImpl(Context context) {
        if (context == null) {
            throw new NullPointerException("BaseDaoImpl中的context不能为null");
        }
        if (helper == null) {
            helper = new MySQLiteOpenHelper(context);
        }
        mContext = context;
    }

    public void close() {
        if (getHelper() != null) {
            getHelper().closeDb();
        }
    }

    /*************************************主键是_id时的操作*************************************/

    /**
     * OK
     *
     * @param clazz
     * @param object 需要被添加的java bean
     * @return 新添加的_id号
     */
    public synchronized long add(Class<?> clazz, Object object) {
        long index = -1;
        try {
            if (clazz == null || object == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    values.put(fieldName, (String) field.get(object));

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getLong(object)));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getShort(object)));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    if ("_id".equals(fieldName)) {
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getDouble(object)));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getFloat(object)));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getBoolean(object)));

                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getChar(object)));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getByte(object)));

                }
            }

            index = getWritableDb().insert(tableName, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /***
     * 如果已经有_id的数据了,那么不再添加
     *
     * @param clazz
     * @param object
     * @param _id
     * @return
     */
    public synchronized long add(Class<?> clazz, Object object, String primaryName, int _id) {
        long index = -1;
        try {
            if (clazz == null || object == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            // 复制短信的表时,为什么"_id"在2422这个位置开始倒数添加时,
            // 去查询是否存在时,一直查出是true,但是真实情况应该是false的
            if (isExists(clazz, _id)) {
                Log.d(TAG, " id = " + _id);
                return index;
            }

            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    values.put(fieldName, (String) field.get(object));

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getLong(object)));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getShort(object)));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    // 自增长,所以不能设置值
                    if ("id".equals(fieldName) && "id".equals(primaryName)
                            || "_id".equals(fieldName) && "_id".equals(primaryName)) {
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getDouble(object)));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getFloat(object)));
                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getBoolean(object)));

                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getChar(object)));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getByte(object)));

                }
            }

            index = getWritableDb().insert(tableName, null, values);
            Log.d(TAG, "_id = " + _id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values
     * @return 新添加的_id号(_id就是主键)
     */
    @Override
    public synchronized long add(Class<?> clazz, ContentValues values) {
        long index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();

            index = getWritableDb().insert(tableName, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /*************************************主键不是_id时的操作*************************************/

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),也不更新
     *
     * @param clazz
     * @param object
     * @return 新添加的_id号
     */
    public synchronized long add2(
            Class<?> clazz,
            Object object,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null
                    || object == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            if (isExists(clazz, primaryKey, primaryValue)) {
                return index;
            }

            // Log.d(TAG, "primaryKey = " + primaryKey + " primaryValue = " + primaryValue);
            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    values.put(fieldName, (String) field.get(object));

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getLong(object)));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getShort(object)));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    if ("_id".equals(fieldName)) {
                        values.put(fieldName, String.valueOf(_id));
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getDouble(object)));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getFloat(object)));

                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {

                    values.put(fieldName, String.valueOf(field.getBoolean(object)));
                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getChar(object)));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getByte(object)));

                }
            }

            index = getWritableDb().insert(tableName, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),而是更新
     *
     * @param clazz
     * @param object
     * @return 新添加的_id号
     */
    public synchronized long add2OrUpdate(
            Class<?> clazz,
            Object object,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null
                    || object == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return index;
            }
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = fields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    values.put(fieldName, (String) field.get(object));

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getLong(object)));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getShort(object)));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    if ("_id".equals(fieldName)) {
                        values.put(fieldName, String.valueOf(_id));
                        continue;
                    }
                    values.put(fieldName, String.valueOf(field.getInt(object)));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getDouble(object)));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getFloat(object)));

                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getBoolean(object)));

                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getChar(object)));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    values.put(fieldName, String.valueOf(field.getByte(object)));

                }
            }

            if (!isExists(clazz, primaryKey, primaryValue)) {
                index = getWritableDb().insert(tableName, null, values);
            } else {
                // String table, ContentValues values, String whereClause, String[] whereArgs
                index = getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),也不更新
     *
     * @param clazz
     * @param values
     * @return 新添加的_id号
     */
    public synchronized long add2(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null
                    || values == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            if (isExists(clazz, primaryKey, primaryValue)) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            long _id = getLastId(clazz);
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;
            values.put("_id", String.valueOf(_id));

            index = getWritableDb().insert(tableName, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * @param clazz
     * @param values
     * @return 新添加的_id号
     */
    public synchronized long add2OrUpdate(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            if (clazz == null
                    || values == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            long _id = getLastId(clazz);
            Log.d(TAG, "_id = " + String.valueOf(_id));
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            if (!isExists(clazz, primaryKey, primaryValue)) {
                values.put("_id", String.valueOf(_id));
                index = getWritableDb().insert(tableName, primaryKey, values);
                Log.d(TAG, "insert():index = " + index);
            } else {
                index = getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
                Log.d(TAG, "update():index = " + index);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /******************************************************************************************/

    /**
     * OK
     *
     * @param clazz
     * @param _id   当_id设置为主键时,可以用这个删,如果是其他主键,不建议
     * @return 删除的条数
     */
    @Override
    public synchronized int delete(Class<?> clazz, int _id) {
        int index = -1;
        try {
            if (clazz == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();

            index = getWritableDb().delete(tableName, "_id=?", new String[]{String.valueOf(_id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param primaryKey
     * @param primaryValue
     * @return 删除的条数
     */
    public int delete(
            Class<?> clazz,
            String primaryKey,
            String primaryValue) {
        int index = -1;
        try {
            if (clazz == null || TextUtils.isEmpty(primaryKey) || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            // getWritableDb().delete(tableName, "number=? and flag=?", new String[]{"12345", "5"})
            // String table, String whereClause, String[] whereArgs

            index = getWritableDb().delete(
                    tableName,
                    primaryKey + "=?",
                    new String[]{primaryValue});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return 删除的条数
     */
    @Override
    public synchronized int delete(Class<?> clazz, Map<String, String> map) {
        int index = -1;
        try {
            if (clazz == null || map == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");

                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");

                }
                values[j] = entry.getValue();
            }
            /*Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Entry<String, String>) iterator.next();
                System.out.println("key:" + entry.getKey() + " value" + entry.getValue());
            }
            getWritableDb().delete(tableName, "number=? and flag=?", new String[]{"12345", "5"});*/

            index = getWritableDb().delete(tableName, sb.toString(), values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * @param clazz
     * @param values
     * @param _id    一般是主键.不是主键时也可以更新那个数据
     * @return 更新的条数 不为-1时表示更新成功
     */
    public int update(Class<?> clazz, ContentValues values, int _id) {
        int index = -1;
        try {
            if (clazz == null || values == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(
                    tableName,
                    values,
                    "_id=?",
                    new String[]{String.valueOf(_id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values       需要更新的的内容
     * @param primaryKey   更新哪一条的条件 一般是主键
     * @param primaryValue 更新哪一条的条件
     * @return 更新的条数 不为-1时表示更新成功
     */
    public synchronized int update(
            Class<?> clazz,
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        int index = -1;
        try {
            if (clazz == null
                    || values == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(
                    tableName,
                    values,
                    primaryKey + "=?",
                    new String[]{primaryValue});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param values 需要更新的的内容
     * @param map    更新哪一条的条件,多个条件
     * @return 更新的条数 不为-1时表示更新成功
     */
    @Override
    public synchronized int update(
            Class<?> clazz,
            ContentValues values,
            Map<String, String> map) {
        int index = -1;
        try {
            if (clazz == null || values == null || map == null) {
                return index;
            }

            if (checkDatabaseInitialization() == -1) {
                return index;
            }

            String tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String whereArgs[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");

                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");

                }
                whereArgs[j] = entry.getValue();
            }
            // update(String table, ContentValues values, String whereClause, String[] whereArgs)

            index = getWritableDb().update(tableName, values, sb.toString(), whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param clazz
     * @param _id   当_id被设置为主键时,可以用这个查,如果是其他主键,不建议
     * @return 单个对象 返回不为null就表示查到一条数据
     */
    @Override
    public Object querySingle(Class<?> clazz, int _id) {
        Object object = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return object;
            }

            if (checkDatabaseInitialization() == -1) {
                return object;
            }

            String tableName = clazz.getSimpleName();
            cursor = getReadableDb().query(
                    tableName, null,
                    "_id=?",
                    new String[]{String.valueOf(_id)},
                    null, null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return object;
    }

    /**
     * OK
     *
     * @param clazz
     * @param primaryKey   一般用主键去查找
     * @param primaryValue
     * @return 单个对象
     */
    public Object querySingle(Class<?> clazz, String primaryKey, String primaryValue) {
        Object object = null;
        Cursor cursor = null;
        try {
            if (clazz == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return object;
            }

            if (checkDatabaseInitialization() == -1) {
                return object;
            }

            String tableName = clazz.getSimpleName();
            /**
             * query(String table, String[] columns,
             * String selection, String[] selectionArgs,
             * String groupBy, String having, String orderBy)
             */
            cursor = getReadableDb().query(tableName, null,
                    primaryKey + "=?", new String[]{primaryValue}, null, null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return object;
    }

    /**
     * OK
     * 查找是否有符合一个对象的一些属性的对象
     *
     * @param clazz
     * @param object
     * @param isIdPrimary "_id"是否是主键,是则为true,不是则为false
     * @return 单个对象
     */
    public Object querySingle(Class<?> clazz, Object object, boolean isIdPrimary) {
        Object obj = null;
        Cursor cursor = null;
        try {
            if (clazz == null || object == null) {
                return obj;
            }

            if (checkDatabaseInitialization() == -1) {
                return obj;
            }

            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return obj;
            }
            int length = fields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = (String) field.get(object);

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getLong(object));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getShort(object));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    if ("_id".equals(fieldName)) {
                        if (!isIdPrimary) {
                            sb.append(fieldName);
                            sb.append("=?");
                            sb.append(" and ");
                            values[index++] = String.valueOf(field.getInt(object));
                        }
                        continue;
                    }
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getInt(object));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getDouble(object));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getFloat(object));

                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getBoolean(object));

                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getChar(object));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getByte(object));

                }
            }
            if (sb.toString().endsWith("and ")) {
                sb.delete(sb.lastIndexOf("a"), sb.lastIndexOf(" "));
            }
            String values_[] = new String[index];
            int values_count = values_.length;
            for (int i = 0; i < values_count; i++) {
                values_[i] = values[i];
            }

            cursor = getReadableDb().query(
                    tableName, null, sb.toString(), values_, null, null, null);
            if (cursor == null) {
                return obj;
            }

            if (cursor.getCount() > 0) {
                Object object_ = clazz.newInstance();
                obj = internalQuerySingle(clazz, object_, cursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return obj;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件
     * @return 单个对象
     */
    @Override
    public Object querySingle(Class<?> clazz, Map<String, String> map) {
        Object object = null;
        Cursor cursor = null;
        try {
            if (clazz == null || map == null) {
                return object;
            }

            if (checkDatabaseInitialization() == -1) {
                return object;
            }

            String tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");
                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");
                }
                values[j] = entry.getValue();
            }
            /**
             * query(String table, String[] columns,
             * String selection, String[] selectionArgs,
             * String groupBy, String having, String orderBy)
             */
            cursor = getReadableDb().query(
                    tableName, null, sb.toString(), values, null, null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = clazz.newInstance();
                object = internalQuerySingle(clazz, object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return object;
    }

    /**
     * 不管主键是_id还是其他值
     *
     * @param clazz
     * @param key   查找符合某种键值对的记录(如性别是男的记录),这里不是指_id
     * @param value 如果条件为null的话,就表示查找全部,那就用queryAll(Class<?> clazz)
     * @return
     */
    public ArrayList<Object> queryMore(Class<?> clazz, String key, String value) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return mList;
            }

            if (checkDatabaseInitialization() == -1) {
                return mList;
            }

            String tableName = clazz.getSimpleName();
            cursor = getReadableDb().query(
                    tableName, null, key + "=?", new String[]{value}, null, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }
            mList = internalQueryMore(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件 如果条件为null的话,就表示查找全部,那就用queryAll(Class<?> clazz)
     * @return
     */
    @Override
    public ArrayList<Object> queryMore(Class<?> clazz, Map<String, String> map) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null || map == null) {
                return mList;
            }

            if (checkDatabaseInitialization() == -1) {
                return mList;
            }

            String tableName = clazz.getSimpleName();
            if (!map.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int count = map.size();
                int i = 0;
                int j = i;
                String values[] = new String[count];
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    j = i++;
                    if (j < count - 1) {
                        sb.append(entry.getKey());
                        sb.append("=?");
                        sb.append(" and ");

                    } else {
                        sb.append(entry.getKey());
                        sb.append("=?");

                    }
                    values[j] = entry.getValue();
                }
                cursor = getReadableDb().query(
                        tableName, null, sb.toString(), values, null, null, null);
            } else {
                cursor = getReadableDb().query(
                        tableName, null, null, null, null, null, null);
            }

            mList = internalQueryMore(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    public ArrayList<Object> queryAll(Class<?> clazz) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return mList;
            }

            if (checkDatabaseInitialization() == -1) {
                return mList;
            }

            String tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName;
            cursor = getReadableDb().rawQuery(sql, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }

            mList = internalQueryMore(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    /***
     *
     * @param clazz
     * @param key 按哪个字段进行排序
     * @param isDescending true表示降序排序
     * @return
     */
    public ArrayList<Object> queryAll(Class<?> clazz, String key, boolean isDescending) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return mList;
            }

            if (checkDatabaseInitialization() == -1) {
                return mList;
            }

            String tableName = clazz.getSimpleName();
            String sql = null;
            if (isDescending) {
                sql = "SELECT * FROM " + tableName + " ORDER BY " + key + " DESC;";
            } else {
                sql = "SELECT * FROM " + tableName + " ORDER BY " + key + " ASC;";
            }
            cursor = getReadableDb().rawQuery(sql, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }

            mList = internalQueryMore(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
            return mList;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    public boolean isExists(Class<?> clazz, int _id) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return isExists;
            }

            if (checkDatabaseInitialization() == -1) {
                return isExists;
            }

            String tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " WHERE "
                    + "_id = \'" + String.valueOf(_id) + "\';";
            cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return isExists;
    }

    /**
     * OK
     * 当_id不被设置为主键时用这个方法判断
     *
     * @param clazz
     * @param primaryKey   主键
     * @param primaryValue 主键的值
     * @return
     */
    public boolean isExists(Class<?> clazz, String primaryKey, String primaryValue) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            if (clazz == null
                    || TextUtils.isEmpty(primaryKey)
                    || TextUtils.isEmpty(primaryValue)) {
                return isExists;
            }

            if (checkDatabaseInitialization() == -1) {
                return isExists;
            }

            String tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " where "
                    + primaryKey + " = \'" + primaryValue + "\';";
            cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return isExists;
    }

    public boolean isExists(Class<?> clazz, Object object, boolean isIdPrimary) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            if (clazz == null || object == null) {
                return isExists;
            }

            if (checkDatabaseInitialization() == -1) {
                return isExists;
            }

            String tableName = clazz.getSimpleName();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null) {
                return isExists;
            }
            int length = fields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$") || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (fieldTypeName.equals(String.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = (String) field.get(object);

                } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                        fieldTypeName.equals(Long.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getLong(object));

                } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                        fieldTypeName.equals(Short.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getShort(object));

                } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                        fieldTypeName.equals(Integer.class.getSimpleName())) {
                    if ("_id".equals(fieldName)) {
                        if (!isIdPrimary) {
                            sb.append(fieldName);
                            sb.append("=?");
                            sb.append(" and ");
                            values[index++] = String.valueOf(field.getInt(object));
                        }
                        continue;
                    }
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getInt(object));

                } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                        fieldTypeName.equals(Double.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getDouble(object));

                } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                        fieldTypeName.equals(Float.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getFloat(object));

                } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                        fieldTypeName.equals(Boolean.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getBoolean(object));

                } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                        fieldTypeName.equals(Character.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getChar(object));

                } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                        fieldTypeName.equals(Byte.class.getSimpleName())) {
                    sb.append(fieldName);
                    sb.append("=?");
                    sb.append(" and ");
                    values[index++] = String.valueOf(field.getByte(object));

                }
            }
            if (sb.toString().endsWith("and ")) {
                sb.delete(sb.lastIndexOf("a"), sb.lastIndexOf(" "));
            }
            String values_[] = new String[index];
            int values_count = values_.length;
            for (int i = 0; i < values_count; i++) {
                values_[i] = values[i];
            }

            cursor = getReadableDb().query(
                    tableName, null, sb.toString(), values_, null, null, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return isExists;
    }

    /**
     * OK
     *
     * @param clazz
     * @param map   多个条件判断有没有这条记录
     * @return
     */
    public boolean isExists(Class<?> clazz, Map<String, String> map) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            if (clazz == null || map == null) {
                return isExists;
            }

            if (checkDatabaseInitialization() == -1) {
                return isExists;
            }

            String tableName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            for (Map.Entry<String, String> entry : map.entrySet()) {
                j = i++;
                if (j < count - 1) {
                    sb.append(entry.getKey());
                    sb.append("=?");
                    sb.append(" and ");

                } else {
                    sb.append(entry.getKey());
                    sb.append("=?");

                }
                values[j] = entry.getValue();
            }
            cursor = getReadableDb().query(
                    tableName, null, sb.toString(), values, null, null, null);
            if (cursor == null) {
                return isExists;
            }
            if (cursor.getCount() > 0) {
                isExists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return isExists;
    }

    /**
     * 当_id不是主键时,先取出最后的_id值,然后加1,再存入数据
     * 41条数据时用时大概2~4ms
     *
     * @param clazz
     * @return
     */
    public long getLastId(Class<?> clazz) {
        long lastId = -1;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return lastId;
            }

            if (checkDatabaseInitialization() == -1) {
                return lastId;
            }

            String tableName = clazz.getSimpleName();
            String sql = "SELECT * FROM " + tableName + " ORDER BY " + "_id DESC LIMIT 0,1;";
            cursor = getReadableDb().rawQuery(sql, null);
            if (cursor == null) {
                return lastId;
            }
            if (cursor.getCount() <= 0) {
                return lastId;
            }
            cursor.moveToFirst();
            lastId = cursor.getInt(cursor.getColumnIndex("_id"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return lastId;
    }

    /***
     * 拷贝之前会清空掉原来的数据
     *
     * @param clazz
     * @param cursor
     * @return
     */
    public synchronized boolean copyData(Class<?> clazz, Cursor cursor) {
        boolean isSuccess = false;
        try {
            if (clazz == null || cursor == null) {
                return isSuccess;
            }

            if (checkDatabaseInitialization() == -1) {
                return isSuccess;
            }

            isSuccess = internalCopyData(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        Log.d(TAG, "isSuccess = " + isSuccess);
        return isSuccess;
    }

    public synchronized boolean restoreData(Class<?> clazz) {
        boolean isSuccess = false;
        Cursor cursor = null;
        try {
            if (clazz == null) {
                return isSuccess;
            }

            if (checkDatabaseInitialization() == -1) {
                return isSuccess;
            }

            cursor = getReadableDb().query(
                    clazz.getSimpleName(), null, null, null, null, null, null);
            if (cursor == null) {
                return isSuccess;
            }

            isSuccess = internalRestoreData(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        Log.d(TAG, "isSuccess = " + isSuccess);
        return isSuccess;
    }

    public synchronized Cursor getLocalData(Class<?> clazz) {
        Cursor cursor = null;
        try {
            if (clazz == null || cursor == null) {
                return cursor;
            }

            if (checkDatabaseInitialization() == -1) {
                return cursor;
            }

            cursor = getReadableDb().query(
                    clazz.getSimpleName(), null, null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return cursor;
    }

    private Object internalQuerySingle(Class<?> clazz, Object object, Cursor cursor) {
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            Map<String, String> map = new HashMap<String, String>();
            if (cursor.moveToNext()) {
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    //                System.out.println("------------>" + columnName + " " + temp);
                    map.put(columnName, temp);
                }
            }

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return null;
            }
            int fields_count = fields.length;
            for (i = 0; i < fields_count; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();

                if (fieldName.contains("$")
                        || fieldName.contains("serialVersionUID")) {
                    continue;
                }

                if (map.containsKey(fieldName)) {
                    String value = map.get(fieldName);
                    if (TextUtils.isEmpty(value) || "null".equals(value)) {
                        continue;
                    }
                    if (fieldTypeName.equals(String.class.getSimpleName())) {
                        field.set(object, value);
                    } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                            fieldTypeName.equals(Long.class.getSimpleName())) {
                        field.setLong(object, Long.parseLong(value));
                    } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                            fieldTypeName.equals(Short.class.getSimpleName())) {
                        field.setShort(object, Short.parseShort(value));
                    } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                            fieldTypeName.equals(Integer.class.getSimpleName())) {
                        field.setInt(object, Integer.parseInt(value));
                    } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                            fieldTypeName.equals(Double.class.getSimpleName())) {
                        field.setDouble(object, Double.parseDouble(value));
                    } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                            fieldTypeName.equals(Float.class.getSimpleName())) {
                        field.setFloat(object, Float.parseFloat(value));
                    } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                            fieldTypeName.equals(Boolean.class.getSimpleName())) {
                        field.setBoolean(object, Boolean.parseBoolean(value));
                    } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                            fieldTypeName.equals(Character.class.getSimpleName())) {
                        field.setChar(object, value.charAt(0));
                    } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                            fieldTypeName.equals(Byte.class.getSimpleName())) {
                        field.setByte(object, Byte.parseByte(value));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }

    private ArrayList<Object> internalQueryMore(Class<?> clazz, Cursor cursor) {
        ArrayList<Object> mList = null;
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            int j = 0;
            ArrayList<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
            Map<String, String> map = null;
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    map.put(columnName, temp);
                }
                //                System.out.println("------------->"+map);
                tempList.add(map);
            }

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return null;
            }
            int fields_count = fields.length;
            mList = new ArrayList<Object>();

            int tempListCount = tempList.size();
            for (i = 0; i < tempListCount; i++) {
                map = tempList.get(i);
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object = clazz.newInstance();
                for (j = 0; j < fields_count; j++) {
                    if (fields[j].getName().contains("$")) {
                        continue;
                    }
                    Field field = fields[j];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String fieldTypeName = field.getType().getSimpleName();
                    if (map.containsKey(fieldName)) {
                        String value = map.get(fieldName);
                        if (TextUtils.isEmpty(value) || "null".equals(value)) {
                            continue;
                        }
                        if (fieldTypeName.equals(String.class.getSimpleName())) {
                            field.set(object, value);
                        } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                                fieldTypeName.equals(Long.class.getSimpleName())) {
                            field.setLong(object, Long.parseLong(value));
                        } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                                fieldTypeName.equals(Short.class.getSimpleName())) {
                            field.setShort(object, Short.parseShort(value));
                        } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                                fieldTypeName.equals(Integer.class.getSimpleName())) {
                            field.setInt(object, Integer.parseInt(value));
                        } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                                fieldTypeName.equals(Double.class.getSimpleName())) {
                            field.setDouble(object, Double.parseDouble(value));
                        } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                                fieldTypeName.equals(Float.class.getSimpleName())) {
                            field.setFloat(object, Float.parseFloat(value));
                        } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                                fieldTypeName.equals(Boolean.class.getSimpleName())) {
                            field.setBoolean(object, Boolean.parseBoolean(value));
                        } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                                fieldTypeName.equals(Character.class.getSimpleName())) {
                            field.setChar(object, value.charAt(0));
                        } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                                fieldTypeName.equals(Byte.class.getSimpleName())) {
                            field.setByte(object, Byte.parseByte(value));
                        }
                    }
                }
                mList.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mList;
    }

    /***
     * 使用这个方法,前提是"_id"为主键
     * @param clazz
     * @param cursor
     */
    private boolean internalCopyData(Class<?> clazz, Cursor cursor) {
        boolean isSuccess = false;
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            int j = 0;
            ArrayList<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
            Map<String, String> map = null;
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    map.put(columnName, temp);
                }
                //                System.out.println("------------->"+map);
                tempList.add(map);
            }

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return isSuccess;
            }

            int fields_count = fields.length;
            Field primaryField = null;
            for (i = 0; i < fields_count; i++) {
                Field fieldTemp = fields[i];
                if (fieldTemp == null) {
                    continue;
                }
                fieldTemp.setAccessible(true);
                Primary primary = fieldTemp.getAnnotation(Primary.class);
                if (primary != null) {
                    primaryField = fieldTemp;
                    break;
                }
            }
            String primaryName = null;
            if (primaryField != null) {
                primaryName = primaryField.getName();
            }
            if (TextUtils.isEmpty(primaryName)) {
                throw new RuntimeException(clazz.getSimpleName() +
                        " 类中没有设置主键,这是不允许的,可以把\"_id\"属性设置为主键");
            }

            int tempListCount = tempList.size();
            Log.d(TAG, "数据库中的数据数量tempListCount = " + tempListCount);
            /*String tableName = clazz.getSimpleName();
            // 先清空原来的数据
            getWritableDb().execSQL("DELETE FROM " + tableName + ";");
            // update sqlite_sequence SET seq = 0 where name ='TableName'; // 自增长ID为0
            getWritableDb().execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = \'" +
                    tableName + "\';");*/
            for (i = 0; i < tempListCount; i++) {
                map = tempList.get(i);
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object = clazz.newInstance();
                int primaryValue = -1;
                for (j = 0; j < fields_count; j++) {
                    if (fields[j].getName().contains("$")) {
                        continue;
                    }
                    Field field = fields[j];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String fieldTypeName = field.getType().getSimpleName();
                    if (map.containsKey(fieldName)) {
                        String value = map.get(fieldName);
                        if (TextUtils.isEmpty(value) || "null".equals(value)) {
                            continue;
                        }

                        if (fieldTypeName.equals(String.class.getSimpleName())) {
                            field.set(object, value);

                        } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                                fieldTypeName.equals(Long.class.getSimpleName())) {
                            field.setLong(object, Long.parseLong(value));

                        } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                                fieldTypeName.equals(Short.class.getSimpleName())) {
                            field.setShort(object, Short.parseShort(value));

                        } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                                fieldTypeName.equals(Integer.class.getSimpleName())) {
                            field.setInt(object, Integer.parseInt(value));
                            if ("id".equals(primaryName)) {
                                if ("_id".equals(fieldName)) {
                                    primaryValue = Integer.parseInt(value);
                                }
                            } else if ("_id".equals(primaryName)) {
                                if ("id".equals(fieldName)) {
                                    primaryValue = Integer.parseInt(value);
                                }
                            }

                        } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                                fieldTypeName.equals(Double.class.getSimpleName())) {
                            field.setDouble(object, Double.parseDouble(value));

                        } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                                fieldTypeName.equals(Float.class.getSimpleName())) {
                            field.setFloat(object, Float.parseFloat(value));

                        } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                                fieldTypeName.equals(Boolean.class.getSimpleName())) {
                            field.setBoolean(object, Boolean.parseBoolean(value));

                        } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                                fieldTypeName.equals(Character.class.getSimpleName())) {
                            field.setChar(object, value.charAt(0));

                        } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                                fieldTypeName.equals(Byte.class.getSimpleName())) {
                            field.setByte(object, Byte.parseByte(value));

                        }
                    }
                }
                // add2(clazz, object, primaryName, primaryValue);
                // Log.d(TAG, object.toString());
                add(clazz, object, primaryName, primaryValue);
            }
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    private boolean internalRestoreData(Class<?> clazz, Cursor cursor) {
        boolean isSuccess = false;
        try {
            int columnCount = cursor.getColumnCount();
            String temp = null;
            String columnName = null;
            int i = 0;
            int j = 0;
            ArrayList<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
            Map<String, String> map = null;
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                for (i = 0; i < columnCount; i++) {
                    temp = cursor.getString(i);
                    columnName = cursor.getColumnName(i);
                    map.put(columnName, temp);
                }
                //                System.out.println("------------->"+map);
                tempList.add(map);
            }

            Field fields[] = clazz.getDeclaredFields();
            if (fields == null) {
                return isSuccess;
            }

            int fields_count = fields.length;
            Field primaryField = null;
            for (i = 0; i < fields_count; i++) {
                Field fieldTemp = fields[i];
                if (fieldTemp == null) {
                    continue;
                }
                fieldTemp.setAccessible(true);
                Primary primary = fieldTemp.getAnnotation(Primary.class);
                if (primary != null) {
                    primaryField = fieldTemp;
                    break;
                }
            }
            String primaryName = null;
            if (primaryField != null) {
                primaryName = primaryField.getName();
            }
            if (TextUtils.isEmpty(primaryName)) {
                throw new RuntimeException(clazz.getSimpleName() +
                        " 类中没有设置主键,这是不允许的,可以把\"_id\"属性设置为主键");
            }

            /*String tableName = clazz.getSimpleName();
            // 先清空原来的数据
            getWritableDb().execSQL("DELETE FROM " + tableName + ";");
            // update sqlite_sequence SET seq = 0 where name ='TableName'; // 自增长ID为0
            getWritableDb().execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = \'" +
                    tableName + "\';");*/

            int tempListCount = tempList.size();
            Log.d(TAG, "数据库中的数据数量tempListCount = " + tempListCount);
            for (i = 0; i < tempListCount; i++) {
                map = tempList.get(i);
                ContentValues contentValues = new ContentValues();
                for (j = 0; j < fields_count; j++) {
                    if (fields[j].getName().contains("$")) {
                        continue;
                    }
                    Field field = fields[j];
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String fieldTypeName = field.getType().getSimpleName();
                    if (map.containsKey(fieldName)) {
                        String value = map.get(fieldName);
                        if (TextUtils.isEmpty(value) || "null".equals(value)) {
                            continue;
                        }

                        if (fieldTypeName.equals(String.class.getSimpleName())) {
                            contentValues.put(fieldName, value);

                        } else if (fieldTypeName.equals(long.class.getSimpleName()) ||
                                fieldTypeName.equals(Long.class.getSimpleName())) {
                            contentValues.put(fieldName, Long.parseLong(value));

                        } else if (fieldTypeName.equals(short.class.getSimpleName()) ||
                                fieldTypeName.equals(Short.class.getSimpleName())) {
                            contentValues.put(fieldName, Short.parseShort(value));

                        } else if (fieldTypeName.equals(int.class.getSimpleName()) ||
                                fieldTypeName.equals(Integer.class.getSimpleName())) {
                            if ("id".equals(fieldName) && "id".equals(primaryName)
                                    || "_id".equals(fieldName) && "_id".equals(primaryName)) {
                                continue;
                            }
                            contentValues.put(fieldName, Integer.parseInt(value));

                        } else if (fieldTypeName.equals(double.class.getSimpleName()) ||
                                fieldTypeName.equals(Double.class.getSimpleName())) {
                            contentValues.put(fieldName, Double.parseDouble(value));

                        } else if (fieldTypeName.equals(float.class.getSimpleName()) ||
                                fieldTypeName.equals(Float.class.getSimpleName())) {
                            contentValues.put(fieldName, Float.parseFloat(value));

                        } else if (fieldTypeName.equals(boolean.class.getSimpleName()) ||
                                fieldTypeName.equals(Boolean.class.getSimpleName())) {
                            contentValues.put(fieldName, Boolean.parseBoolean(value));

                        } else if (fieldTypeName.equals(char.class.getSimpleName()) ||
                                fieldTypeName.equals(Character.class.getSimpleName())) {
                            contentValues.put(fieldName, value);

                        } else if (fieldTypeName.equals(byte.class.getSimpleName()) ||
                                fieldTypeName.equals(Byte.class.getSimpleName())) {
                            contentValues.put(fieldName, Byte.parseByte(value));

                        }
                    }
                }
                if (mContext != null) {
                    mContext.getContentResolver().insert(
                            Uri.parse(mDataBackupAndRestoreMap.get(
                                    clazz.getSimpleName())),
                            contentValues);
                }
            }
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    private MySQLiteOpenHelper getHelper() {
        if (helper == null) {
            helper = new MySQLiteOpenHelper(mContext);
        }
        return helper;
    }

    private SQLiteDatabase getReadableDb() {
        return getHelper().getReadableDb();
    }

    private SQLiteDatabase getWritableDb() {
        return getHelper().getWritableDb();
    }

    private int checkDatabaseInitialization() {
        if (DbUtils.getInstance().getInitializationState() ==
                DbUtils.INITIALIZING) {
            MyToast.show(INITIALIZING);
            return -1;
        } else if (DbUtils.getInstance().getInitializationState() ==
                DbUtils.INITIALIZATION_FAILED) {
            MyToast.show(INITIALIZATION_FAILED);
            return -1;
        }
        return 0;
    }

    //查询所有联系人的姓名，电话，邮箱
    public void TestContact(Context context) throws Exception {
        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
        while (cursor.moveToNext()) {
            int contractID = cursor.getInt(0);
            StringBuilder sb = new StringBuilder("contractID=");
            sb.append(contractID);
            uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
            Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1", "data2"},
                    null, null, null);
            while (cursor1.moveToNext()) {
                String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/name".equals(mimeType)) { //是姓名
                    sb.append(",name=" + data1);
                } else if ("vnd.android.cursor.item/email_v2".equals(mimeType)) { //邮箱
                    sb.append(",email=" + data1);
                } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                    sb.append(",phone=" + data1);
                }
            }
            cursor1.close();
            Log.i(TAG, sb.toString());
        }
        cursor.close();
    }

}
