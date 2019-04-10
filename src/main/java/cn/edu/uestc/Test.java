package cn.edu.uestc;

import cn.edu.uestc.utils.DBManager;

import java.sql.Connection;

public class Test {
    public static void main(String[] args) {
        System.out.println(1);
        System.out.println(DBManager.getConnection(DataSource.APP_TEST_DB));
        System.out.println(2);
        System.out.println(DBManager.getConnection(DataSource.APP_TEST_DB));
        System.out.println(3);
        System.out.println(DBManager.getConnection(DataSource.APP_TEST_DB));
        System.out.println(4);
        System.out.println(DBManager.getConnection(DataSource.CRAWLER_DB));
//        Connection connection1 = DBManager.getConnection(DataSource.APP_TEST_DB);
//        Connection connection2 = DBManager.getConnection(DataSource.APP_TEST_DB);
//        System.out.println(connection1 == connection2);
    }
}
