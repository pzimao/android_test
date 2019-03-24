package cn.edu.uestc.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DBUtil {
    private static Connection connection = null;
//	private static ResultSet rs=null;
//	private static PreparedStatement ps=null;

    private static String url = "";
    private static String drivername = "";
    private static String username = "";
    private static String password = "";

    static {
        try {
            Properties properties = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("settings.properties");
            properties.load(is);

            drivername = properties.getProperty("dbDriver");
            username = properties.getProperty("dbUserName");
            password = properties.getProperty("dbPassword");
            url = properties.getProperty("dbUrl");
            Class.forName(drivername);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static Connection getCon() {
        if (connection != null) {
            return connection;
        }
        try {
            Class.forName(drivername);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static Object execute(String sql, String... args) {
        Object result = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
}
