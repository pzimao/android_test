package cn.edu.uestc.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBUtil {
	private static Connection ct = null;
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
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static Connection getCon() {
		if (ct != null) {
			return ct;
		}
		try {
			Class.forName(drivername);
			ct = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ct;
	}
}
