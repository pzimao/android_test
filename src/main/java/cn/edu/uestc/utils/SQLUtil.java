package cn.edu.uestc.utils;

import java.lang.reflect.Field;

public class SQLUtil {
    public static String getSQL(Object object) {
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
        Object o = new StringBuilder();
        System.out.println(o.getClass().getName());
    }
}
