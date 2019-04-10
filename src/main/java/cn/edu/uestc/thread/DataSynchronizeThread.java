package cn.edu.uestc.thread;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.FileNameFilter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Properties;

public class DataSynchronizeThread extends Thread {

    // 这里要
    private final Logger logger;
    private final HashSet<DataSource> dbSet = new HashSet<>();

    public DataSynchronizeThread() {
        logger = LogManager.getLogger("数据同步线程");
        // todo 这里添加数据库源
        dbSet.add(DataSource.CRAWLER_DB);
//        dbSet.add(DataSource.PACKAGE_DB);
//        dbSet.add(DataSource.URL_DB);
    }

    @Override
    public void run() {
        Long dataSynPeriod = 10 * 60 * 1000L;
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            properties.load(is);
            dataSynPeriod = Long.valueOf(properties.getProperty("dataSynPeriod"));

            logger.info("数据同步周期: " + dataSynPeriod / 1000 / 60 + "分钟");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }
        while (true) {
            int synCount = synDB();
            logger.info("同步了 " + synCount + " 条记录");
            try {
                Thread.sleep(dataSynPeriod);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int synDB() {
        int synCount = 0;
        String queryOnDstSql = "select count(*) from app where original_id = ? and app_from = ?";
        String insertOnDstSql = "insert into app (original_id, app.app_name, app.provided_pkg_name, app.dl_url, app.app_from) values (?, ?, ?, ?, ?)";
        try {
            for (DataSource dataSource : dbSet) {
                // 对每个输入数据库都做处理
                int cursor = 0;
                boolean workFlag = true;
                dc1:
                while (cursor % 1000 == 0 && workFlag) {
                    workFlag = false;
                    // 这条sql语句要保证查出来的字段一致
                    String queryOnSrcSql = dataSource.getQuerySql() + " ORDER BY id desc limit " + cursor + ", 1000";

                    ResultSet queryResultSet = (ResultSet) DBManager.execute(dataSource, queryOnSrcSql);
                    while (queryResultSet.next()) {
                        String id = queryResultSet.getString(1);
                        // 需要去查一下看是否已经同步过了
                        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, queryOnDstSql, id, String.valueOf(dataSource.getIndex()));
                        // 卫语句
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            if (count > 0) {
                                // 表明这以前的数据已经同步过了，可以停止了

                                break dc1;
                            }
                        }
                        String appName = queryResultSet.getString(2);
                        String packageName = queryResultSet.getString(3);
                        String dlUrl = queryResultSet.getString(4);
                        appName = FileNameFilter.filter(appName.replace(".apk", ""));
                        DBManager.execute(DataSource.APP_TEST_DB, insertOnDstSql, id, appName, packageName, dlUrl, String.valueOf(dataSource.getIndex()));
                        cursor++;
                        synCount++;
                        workFlag = true;
                        logger.debug("同步内容 : " + id + " " + appName + " " + packageName + " " + dlUrl);
                    }
                }
                logger.info("同步了 " + cursor + " 条 " + dataSource.getName() + "的记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return synCount;
    }
}
