package cn.edu.uestc.utils;

import cn.edu.uestc.DataSource;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Properties;

public class DBManager {
    private static Connection crawlerDBConnection;
    private static Connection appTestDBConnection;
    private static Connection urlDBConnection;
    private final static Logger logger = Logger.getLogger("数据库管理线程");

    public static Connection getConnection(DataSource dataSource) {
        // 利用反射拿到静态字段
        Connection connection = null;
        try {
            Field connectionFiled = DBManager.class.getDeclaredField(dataSource.getName() + "Connection");
            connection = (Connection) connectionFiled.get(null);
            // 双检锁
            if (connection == null) {
                synchronized (DBManager.class) {
                    if (connection == null) {
                        // 此时需要创建数据库连接对象
                        StringBuilder sb = new StringBuilder();
                        Properties properties = new Properties();
                        InputStream is = DBManager.class.getClassLoader().getResourceAsStream("settings.properties");
                        properties.load(is);

                        String driver = properties.getProperty("driver");
                        String address = properties.getProperty(dataSource.getName() + "Address");
                        String port = properties.getProperty(dataSource.getName() + "Port");
                        String name = properties.getProperty(dataSource.getName() + "Name");
                        String username = properties.getProperty(dataSource.getName() + "Username");
                        String password = properties.getProperty(dataSource.getName() + "Password");
                        String url = sb.append("jdbc:mysql://")
                                .append(address)
                                .append(":")
                                .append(port)
                                .append("/")
                                .append(name)
                                .append("?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT")
                                .toString();
                        Class.forName(driver);
                        connection = DriverManager.getConnection(url, username, password);
                        connectionFiled.set(null, connection);
                        logger.info("连接 : " + url);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return connection;
    }

    public static Object execute(DataSource dataSource, String sql, String... args) {
        System.out.println(sql);
        Arrays.asList(args).forEach(arg -> System.out.print(arg + " "));
        System.out.println();
        Object result = null;
        try {
            PreparedStatement preparedStatement = DBManager.getConnection(dataSource).prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setString(i + 1, args[i]);
            }
            if (sql.toLowerCase().startsWith("select")) {
                // 查询语句
                result = preparedStatement.executeQuery();
            } else {
                result = preparedStatement.execute();
            }

        } catch (Exception e) {
            // todo 处理这里的异常
            e.printStackTrace();
        }
        return result;
    }

    public static Connection getCon() {
        return null;
    }

    public static String arrayToString(String[] args) {
        String str = "";
        for (int i = 0; i < args.length; i++) {
            str += (args[i] + "\t");
        }
        return str;
    }
}
