package com.weidi.dbutil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/***
 Created by root on 16-7-31.

 这个类与BaseDaoImpl的不同之处就是操作增删改查之前
 需要先调用setClass(Class cls)方法,目的是操作哪个表.
 对同一张表进行多次操作或者不断重复操作,那么使用这个类比较好.

 增删改查时都是调用的java方法,因此效率上可能会差一些.

 select * from TestBean limit 4 offset 5;
 从第6条数据开始，查询4条信息
 select * from TestBean limit 4,5;
 从第5条数据开始，查询5条信息
 select count(*) from TestBean;
 查询数据库中有多少条信息
 select * from TestBean order by 字段 (desc降序或asc升序)
 按什么字段进行降序或者升序排列

 创建表:     create  table 表名(元素名 类型,…);
 删除表:     drop  table 表名;
 插入数据:   insert  into 表名 values(, , ,) ;
 创建索引:   create [unique] index 索引名on 表名(col….);
 删除索引：  drop index 索引名(索引是不可更改的，想更改必须删除重新建)
 删除数据:   delete from 表名;
 更新数据:   update 表名 set 字段=’修改后的内容’ where 条件;
 增加一个列:  Alter table 表名 add column 字段 数据类型;
 选择查询:   select 字段(以”,”隔开) from 表名 where 条件;
 日期和时间: select datetime('now')
 日期:      select date('now');
 时间:      select time('now');
 总数：     select count(*) from table1;
 求和：     select sum(field1) from table1;
 平均：     select avg(field1) from table1;
 最大：     select max(field1) from table1;
 最小：     select min(field1) from table1;
 排序：     select 字段 from table1 order by 字段(desc或asc)  ;(降序或升序)
 分组：     select 字段 from table1 group by 字段,字段…  ;
 限制输出:  select 字段fromtable1 limit x offset y;
 = select 字段 from table1 limit y , x;
 */
class BaseDaoImpl2 { //extends ABaseDao {

    private static final String TAG = "BaseDaoImpl2";
    private static final String INITIALIZING = "数据库正在初始化...";
    private static final String INITIALIZATION_FAILED = "数据库正在初始化失败,建议清除应用数据";
    private MySQLiteOpenHelper helper;
    private Context mContext;
    private Class mClass;// 类的Class对象
    private String tableName;// 表名(也就是类名)
    private Field[] mFields;// 表的字段(也就是类的属性名)
    private HashMap<String, Long> mTableNameAndIdMap = new HashMap<String, Long>();

    public interface IOperDBResult {

        void onAddResult(boolean isSuccess);

        void onDeleteResult(boolean isSuccess);

        void onUpdateResult(boolean isSuccess);

        void onQueryResult(boolean isSuccess);

    }

    public BaseDaoImpl2(Context context) {
        mContext = context;
        if (mContext == null) {
            throw new NullPointerException("BaseDaoImpl2中的context不能为null");
        }
        if (helper == null) {
            helper = new MySQLiteOpenHelper(context);
        }
    }

    public synchronized void setClass(Class cls) {
        mClass = cls;
        if (mClass == null) {
            throw new NullPointerException("BaseDaoImpl2中Class对象不能为null");
        }
        tableName = mClass.getSimpleName();
        if (TextUtils.isEmpty(tableName)) {
            throw new NullPointerException("BaseDaoImpl2中tableName为null");
        }
        mFields = mClass.getDeclaredFields();
        if (mFields == null) {
            throw new NullPointerException("BaseDaoImpl2中Field[]对象为null");
        }
        if (!mTableNameAndIdMap.containsKey(tableName)) {
            mTableNameAndIdMap.put(tableName, getLastId());
        }
    }

    public synchronized void beginTransaction() {
        getHelper().getWritableDb().beginTransaction();
    }

    public synchronized void setTransactionSuccessful() {
        getHelper().getWritableDb().setTransactionSuccessful();
    }

    public synchronized void endTransaction() {
        getHelper().getWritableDb().endTransaction();
    }

    /*************************************主键是_id时的操作*************************************/

    /***
     OK

     @param object
     @return 新添加的_id号(_id就是主键)
     */
    public synchronized long add(Object object) {
        long index = -1;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            if (mFields == null) {
                return index;
            }
            int length = mFields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            // 改成执行sql语句
            index = getHelper().getWritableDb().insert(tableName, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return index;
    }

    /***
     OK

     @param values
     @return 新添加的_id号(_id就是主键)
     */

    public synchronized long add(ContentValues values) {
        long index = -1;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            index = getHelper().getWritableDb().insert(tableName, null, values);

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
     * @param object
     * @return 新添加的_id号
     */
    public synchronized long add2(
            Object object,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        if (object == null
                || TextUtils.isEmpty(primaryKey)
                || TextUtils.isEmpty(primaryValue)) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            if (isExists(primaryKey, primaryValue)) {
                return index;
            }

            // long _id = getLastId();;
            long _id = -1;
            if (mTableNameAndIdMap.containsKey(tableName)) {
                _id = mTableNameAndIdMap.get(tableName);
            }
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = mFields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            index = getHelper().getWritableDb().insert(tableName, null, values);
            mTableNameAndIdMap.put(tableName, _id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),也不更新
     *
     * @param values
     * @return 新添加的_id号
     */
    public synchronized long add2(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            if (isExists(primaryKey, primaryValue)) {
                return index;
            }

            // long _id = getLastId();
            long _id = -1;
            if (mTableNameAndIdMap.containsKey(tableName)) {
                _id = mTableNameAndIdMap.get(tableName);
            }
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;
            values.put("_id", String.valueOf(_id));

            index = getHelper().getWritableDb().insert(tableName, null, values);
            mTableNameAndIdMap.put(tableName, _id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * 当数据库里已经有这个主键的值时,不再添加(添加会出错),而是更新
     *
     * @param object
     * @return 新添加的_id号
     */
    public synchronized long add2OrUpdate(
            Object object,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            // long _id = getLastId();
            long _id = -1;
            if (mTableNameAndIdMap.containsKey(tableName)) {
                _id = mTableNameAndIdMap.get(tableName);
            }
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            int length = mFields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            if (!isExists(primaryKey, primaryValue)) {
                index = getHelper().getWritableDb().insert(
                        tableName,
                        null,
                        values);
            } else {
                // String table, ContentValues values, String whereClause, String[] whereArgs
                index = getHelper().getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
            }
            mTableNameAndIdMap.put(tableName, _id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * @param values
     * @return 新添加的_id号
     */
    public synchronized long add2OrUpdate(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        long index = -1;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            // long _id = getLastId();
            long _id = -1;
            if (mTableNameAndIdMap.containsKey(tableName)) {
                _id = mTableNameAndIdMap.get(tableName);
            }
            Log.d(TAG, "_id = " + String.valueOf(_id));
            if (_id < 0) {
                _id = 0;
            }
            _id += 1;

            if (!isExists(primaryKey, primaryValue)) {
                values.put("_id", String.valueOf(_id));
                index = getHelper().getWritableDb().insert(tableName, primaryKey, values);
                Log.d(TAG, "insert():index = " + index);
            } else {
                index = getHelper().getWritableDb().update(
                        tableName,
                        values,
                        primaryKey + "=?",
                        new String[]{primaryValue});
                Log.d(TAG, "update():index = " + index);
            }
            mTableNameAndIdMap.put(tableName, _id);
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
     * @param _id 当_id设置为主键时,可以用这个删,如果是其他主键,不建议
     * @return 删除的条数
     */
    public synchronized int delete(int _id) {
        int index = -1;
        if (_id < 0) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            index = getHelper().getWritableDb().delete(
                    tableName,
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
     * @param primaryKey
     * @param primaryValue
     * @return 删除的条数
     */
    public int delete(
            String primaryKey,
            String primaryValue) {
        int index = -1;
        if (TextUtils.isEmpty(primaryKey) || TextUtils.isEmpty(primaryValue)) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            // getHelper().getWritableDb().delete(tableName, "number=? and flag=?", new
            // String[]{"12345", "5"})
            // String table, String whereClause, String[] whereArgs

            index = getHelper().getWritableDb().delete(
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
     * @param map 多个条件
     * @return 删除的条数
     */
    public synchronized int delete(Map<String, String> map) {
        int index = -1;
        if (map == null) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
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

            // getHelper().getWritableDb().delete(tableName, "number=? and flag=?", new
            // String[]{"12345", "5"})
            index = getHelper().getWritableDb().delete(tableName, sb.toString(), values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * @param object
     * @param _id    一般是主键
     * @return
     */
    public int update(
            Object object,
            int _id) {
        int index = -1;
        if (object == null || _id < 0 || mFields == null) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            int length = mFields.length;
            ContentValues values = new ContentValues();
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            // update(String table, ContentValues values, String whereClause, String[] whereArgs)
            index = getHelper().getWritableDb().update(
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
     * @param values
     * @param _id    一般是主键
     * @return
     */
    public int update(
            ContentValues values,
            int _id) {
        int index = -1;
        if (values == null || _id < 0) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            // update(String table, ContentValues values, String whereClause, String[] whereArgs)
            index = getHelper().getWritableDb().update(
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
     * @param values       需要更新的的内容
     * @param primaryKey   更新哪一条的条件 一般是主键
     * @param primaryValue 更新哪一条的条件
     * @return 更新的条数
     */
    public synchronized int update(
            ContentValues values,
            String primaryKey,
            String primaryValue) {
        int index = -1;
        if (values == null
                || TextUtils.isEmpty(primaryKey)
                || TextUtils.isEmpty(primaryValue)) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

            // update(String table, ContentValues values, String whereClause, String[] whereArgs)
            index = getHelper().getWritableDb().update(
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
     * @param values 需要更新的的内容
     * @param map    更新哪一条的条件,多个条件
     * @return 更新的条数
     */
    public synchronized int update(ContentValues values, Map<String, String> map) {
        int index = -1;
        if (values == null || map == null) {
            return index;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return index;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return index;
            }*/

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
            index = getHelper().getWritableDb().update(
                    tableName, values, sb.toString(), whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return index;
    }

    /**
     * OK
     *
     * @param _id 当_id设置为主键时,可以用这个查,如果是其他主键,不建议
     * @return 单个对象
     */
    public Object querySingle(int _id) {
        Object object = null;
        Cursor cursor = null;
        if (_id < 0) {
            return object;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return object;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return object;
            }*/

            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    "_id=?",
                    new String[]{String.valueOf(_id)},
                    null,
                    null,
                    null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = mClass.newInstance();
                object = internalQuerySingle(object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = null;
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
     * @param primaryKey   一般是主键
     * @param primaryValue
     * @return 单个对象
     */
    public Object querySingle(String primaryKey, String primaryValue) {
        Object object = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return object;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return object;
            }*/

            /**
             * query(String table, String[] columns,
             * String selection, String[] selectionArgs,
             * String groupBy, String having, String orderBy)
             */
            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    primaryKey + "=?",
                    new String[]{primaryValue},
                    null,
                    null,
                    null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = mClass.newInstance();
                object = internalQuerySingle(object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = null;
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
     * @param object
     * @param isIdPrimary
     * @return 单个对象
     */
    public Object querySingle(Object object, boolean isIdPrimary) {
        Object obj = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return object;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return object;
            }*/

            int length = mFields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    sb.toString(),
                    values_,
                    null,
                    null,
                    null);
            if (cursor == null) {
                return obj;
            }

            if (cursor.getCount() > 0) {
                Object object_ = mClass.newInstance();
                obj = internalQuerySingle(object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = null;
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
     * @param map 多个条件
     * @return 单个对象
     */
    public Object querySingle(Map<String, String> map) {
        Object object = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return object;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return object;
            }*/

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
            cursor = getHelper().getReadableDb().query(tableName, null, sb.toString(), values, null,
                    null, null);
            if (cursor == null) {
                return object;
            }

            if (cursor.getCount() > 0) {
                /**
                 * 要求创建的java bean有无参的构造方法
                 */
                Object object_ = mClass.newInstance();
                object = internalQuerySingle(object_, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return object;
    }

    /**
     * 主键是_id时
     *
     * @param primaryKey   查找符合某种键值对的记录(如性别是男的记录),这里不是指_id
     * @param primaryValue
     * @return
     */
    public ArrayList queryMore(String primaryKey, String primaryValue) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return mList;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return mList;
            }*/

            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    primaryKey + "=?",
                    new String[]{primaryValue},
                    null,
                    null,
                    null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }
            mList = internalQueryMore(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            mList = null;
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
     * @param map 多个条件
     * @return
     */
    public ArrayList queryMore(Map<String, String> map) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return mList;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return mList;
            }*/

            if (map != null && !map.isEmpty()) {
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
                cursor = getHelper().getReadableDb().query(
                        tableName,
                        null,
                        sb.toString(),
                        values,
                        null,
                        null,
                        null);
            } else {
                cursor = getHelper().getReadableDb().query(
                        tableName,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
            }

            mList = internalQueryMore(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            mList = null;
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
     * @return
     */
    public ArrayList queryMore(int startLine, int count) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return mList;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return mList;
            }*/

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(tableName);
            sql.append(" LIMIT ");
            sql.append(startLine);
            sql.append(", ");
            sql.append(count);
            sql.append(";");
            Log.i(TAG, sql.toString());
            // String table, String[] columns, String selection,
            // String[] selectionArgs, String groupBy, String having,
            // String orderBy
            cursor = getHelper().getReadableDb().rawQuery(
                    sql.toString(),
                    null);
            mList = internalQueryMore(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            mList = null;
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
     * @return
     */
    public ArrayList queryMore(Map<String, String> map, int startLine, int count) {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return mList;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return mList;
            }*/

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(tableName);
            sql.append(" WHERE ");
            if (map != null && !map.isEmpty()) {
                int mapCount = map.size();
                int i = 0;
                int j = i;
                String values[] = new String[mapCount];
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    j = i++;
                    if (j < mapCount - 1) {
                        sql.append(entry.getKey());
                        sql.append("=\'");
                        sql.append(entry.getValue());
                        sql.append("\'");
                        sql.append(" and ");
                    } else {
                        sql.append(entry.getKey());
                        sql.append("=\'");
                        sql.append(entry.getValue());
                        sql.append("\'");
                    }
                }
            }
            sql.append(" LIMIT ");
            sql.append(startLine);
            sql.append(", ");
            sql.append(count);
            sql.append(";");
            // SELECT * FROM TestBean WHERE name1='aaa' and name5='aaa' and name4='aaa' and
            // name3='aaa' and name2='aaa' LIMIT 50, 100;
            Log.i(TAG, sql.toString());
            cursor = getHelper().getReadableDb().rawQuery(
                    sql.toString(),
                    null);
            mList = internalQueryMore(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            mList = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    public ArrayList queryAll() {
        ArrayList mList = null;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return mList;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return mList;
            }*/

            String sql = "SELECT * FROM " + tableName;
            cursor = getHelper().getReadableDb().rawQuery(sql, null, null);
            if (cursor == null) {
                return mList;
            }
            if (cursor.getCount() <= 0) {
                return mList;
            }

            mList = internalQueryMore(cursor);
        } catch (Exception e) {
            e.printStackTrace();
            mList = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mList;
    }

    public boolean isExists(int _id) {
        boolean isExists = false;
        Cursor cursor = null;
        if (_id < 0) {
            return isExists;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return isExists;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return isExists;
            }*/

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(tableName);
            sql.append(" where _id = \'");
            sql.append(String.valueOf(_id));
            sql.append("\';");
            cursor = getHelper().getReadableDb().rawQuery(sql.toString(), null);
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
     * 当_id不设置为主键时用这个方法判断
     *
     * @param primaryKey   主键
     * @param primaryValue 主键的值
     * @return
     */
    public boolean isExists(String primaryKey, String primaryValue) {
        boolean isExists = false;
        Cursor cursor = null;
        if (TextUtils.isEmpty(primaryKey)
                || TextUtils.isEmpty(primaryValue)) {
            return isExists;
        }
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return isExists;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return isExists;
            }*/

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(tableName);
            sql.append(" where ");
            sql.append(primaryKey);
            sql.append(" = \'");
            sql.append(primaryValue);
            sql.append("\';");
            cursor = getHelper().getReadableDb().rawQuery(sql.toString(), null);
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

    public boolean isExists(Object object, boolean isIdPrimary) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return isExists;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return isExists;
            }*/

            int length = mFields.length;
            StringBuilder sb = new StringBuilder();
            String values[] = new String[length];
            int index = 0;
            for (int i = 0; i < length; i++) {
                Field field = mFields[i];
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

            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    sb.toString(),
                    values_,
                    null,
                    null,
                    null);
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
     * @param map 多个条件
     * @return
     */
    public boolean isExists(Map<String, String> map) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return isExists;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return isExists;
            }*/

            StringBuilder sb = new StringBuilder();
            int count = map.size();
            int i = 0;
            int j = i;

            String values[] = new String[count];
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
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
            cursor = getHelper().getReadableDb().query(
                    tableName,
                    null,
                    sb.toString(),
                    values,
                    null,
                    null,
                    null);
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
     * @return
     */
    public long getLastId() {
        long lastId = -1;
        Cursor cursor = null;
        try {
            /*if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZING) {
                MyToast.show(INITIALIZING);
                return lastId;
            } else if (DbUtils.getInstance().getInitializationState() ==
                    DbUtils.INITIALIZATION_FAILED) {
                MyToast.show(INITIALIZATION_FAILED);
                return lastId;
            }*/

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ");
            sql.append(tableName);
            sql.append(" ORDER BY _id DESC LIMIT 0,1;");
            cursor = getHelper().getReadableDb().rawQuery(sql.toString(), null);
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
            lastId = -1;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return lastId;
    }

    private Object internalQuerySingle(Object object, Cursor cursor)
            throws IllegalAccessException {
        int columnCount = cursor.getColumnCount();
        String temp = null;
        String columnName = null;
        int i = 0;
        HashMap<String, String> map = new HashMap<String, String>();
        if (cursor.moveToNext()) {
            for (i = 0; i < columnCount; i++) {
                temp = cursor.getString(i);
                columnName = cursor.getColumnName(i);
                map.put(columnName, temp);
            }
        }

        int fields_count = mFields.length;
        for (i = 0; i < fields_count; i++) {
            Field field = mFields[i];
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldTypeName = field.getType().getSimpleName();
            if (fieldName.contains("$")
                    || fieldName.contains("serialVersionUID")
                    || !map.containsKey(fieldName)) {
                continue;
            }

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
        return object;
    }

    private ArrayList<Object> internalQueryMore(Cursor cursor)
            throws IllegalAccessException, InstantiationException {
        ArrayList<Object> mList = null;
        int columnCount = cursor.getColumnCount();
        String temp = null;
        String columnName = null;
        int i = 0;
        int j = 0;
        ArrayList<HashMap<String, String>> tempList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = null;
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            for (i = 0; i < columnCount; i++) {
                temp = cursor.getString(i);
                columnName = cursor.getColumnName(i);
                map.put(columnName, temp);
            }
            tempList.add(map);
        }

        int fields_count = mFields.length;
        mList = new ArrayList<Object>();
        int tempListCount = tempList.size();
        for (i = 0; i < tempListCount; i++) {
            map = tempList.get(i);
            /**
             * 要求创建的java bean有无参的构造方法
             */
            Object object = mClass.newInstance();
            for (j = 0; j < fields_count; j++) {
                Field field = mFields[j];
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldTypeName = field.getType().getSimpleName();
                if (fieldName.contains("$")
                        || fieldName.contains("serialVersionUID")
                        || !map.containsKey(fieldName)) {
                    continue;
                }

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
            mList.add(object);
        }
        return mList;
    }

    protected MySQLiteOpenHelper getHelper() {
        if (helper == null) {
            helper = new MySQLiteOpenHelper(mContext);
        }
        return helper;
    }

}
