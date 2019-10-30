package com.weidi.dbutil;

import android.content.Context;

/***
 例子
 Created by root on 19-10-30.
 在java bean中必须要有一个_id的整形.
 如果这个类的属性名有改变,有增加,有删除等操作时,
 "version"的值就增1,这样数据库就会相应的改变.

 这里的类名就是数据库中的表名
 */
@ClassVersion(version = 0)
public class Test {
    // 从1开始递增
    @Primary
    private int _id;

    // 修改成功后会在下个版本中删除掉
    // "name"是之前的属性名,现在改成"sakura"这个属性名
    @OriginalField(value = "name")
    private String sakura;

    private void test() {
        Context context = null;
        Class[] beanClass = new Class[]{Test.class};
        DbUtils.getInstance().createOrUpdateDBWithVersion(context, beanClass);
    }
}
