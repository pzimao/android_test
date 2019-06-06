package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Wxgzh1;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SQLUtil {
    public static String getUpdateSQL(Object object) {
        String fullName = object.getClass().getName();

        Field[] fields = object.getClass().getDeclaredFields();
        HashMap<String, String> map = new HashMap<>();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String value = (String) field.get(object);
                if (value != null) {
                    map.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (map.size() == 0) {
            return ";";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("update ")
                .append(fullName.substring(fullName.lastIndexOf('.') + 1))
                .append(" set");
        for (String key : map.keySet()) {
            if (!"biz".equals(key.toLowerCase())) {
                stringBuilder.append(" `")
                        .append(key)
                        .append("` = '")
                        .append(map.get(key))
                        .append("',");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(" where biz = '")
                .append(map.get("biz"));
        stringBuilder.append("';");
        return stringBuilder.toString();
    }

    public static String getSQL(Object object) {
        if (object.getClass() == Wxgzh1.class) {
            return getUpdateSQL(object);
        }
        String fullName = object.getClass().getName();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ")
                .append(fullName.substring(fullName.lastIndexOf('.') + 1))
                .append("(");
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            stringBuilder.append('`')
                    .append(field.getName())
                    .append("`, ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        stringBuilder.append(") values (");
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String value = (String) field.get(object);
                if (value == null) {
                    value = "";

                }
                stringBuilder.append('\'')
                        .append(value.replace("'", "\\'"))
                        .append('\'')
                        .append(", ");
                field.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        stringBuilder.append(");");
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        System.out.println(SQLUtil.getUpdateSQL(new Wxgzh1()));
    }
}
