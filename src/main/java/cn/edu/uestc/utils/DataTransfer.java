package cn.edu.uestc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataTransfer {

    public static void txtToApp() throws Exception {
        Connection connection = DBUtil.getCon();
        String sql = "INSERT INTO `app_db`.`app_info2`(`id`, `app_name`, `pkg_name`, dl_url) VALUES (? , ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        File file = new File("C:\\Users\\pzima\\Desktop\\android_test\\src\\main\\resources\\part_100.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        while (line != null) {
            String[] array = line.split("\t");
            String appId = array[0];
            String appName = FileNameFilter.filter(array[1]);
            String appPackageName = array[2];
            String dlUrl = "";
            if (array.length > 4) {
                dlUrl = array[4];
            }
            if ("-1".equals(dlUrl)) {
                dlUrl = "";
            }
            preparedStatement.setInt(1, Integer.valueOf(appId));
            preparedStatement.setString(2, appName);
            preparedStatement.setString(3, appPackageName);
            preparedStatement.setString(4, dlUrl);
            preparedStatement.addBatch();
            line = br.readLine();
        }
        preparedStatement.executeBatch();
    }

    /**
     * 把app_info表的内容导入app表
     */
    public static void appInfoToApp() throws Exception {
        Connection connection = DBUtil.getCon();
        String sql = "select provided_pkg_name, dl_url, pkg_name from app_info";
        String updateSql = "update app set actual_pkg_name = ?, dl_url = ? where provided_pkg_name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String providedPackageName = resultSet.getString(1);
            String dlUrl = resultSet.getString(2);
            String packageName = resultSet.getString(3);
            PreparedStatement updatePs = connection.prepareStatement(updateSql);
            updatePs.setString(1, packageName);
            updatePs.setString(2, dlUrl);
            updatePs.setString(3, providedPackageName);

            System.out.println("更新了 " + updatePs.executeUpdate() + " 条记录.");
        }
    }

    /**
     * 把app_info表的内容导入app表
     */
    public static void appDlToApp() throws Exception {
        Connection connection = DBUtil.getCon();
        String sql = "select url, state, remark from app_dl";
        String updateSql = "update app set dl_state = ? where dl_url = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String url = resultSet.getString(1);
            int state = resultSet.getInt(2);
            PreparedStatement updatePs = connection.prepareStatement(updateSql);
            updatePs.setInt(1, state);
            updatePs.setString(2, url);

            System.out.println("更新了 " + updatePs.executeUpdate() + " 条记录.");
        }
    }

    public static void appDomainToDomain() throws Exception {
        Connection connection = DBUtil.getCon();
        String sql = "select content, type from app_domain GROUP BY content";
        String insertSql = "INSERT INTO `app_db`.`domain`(`domain`, `domain_desc`) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        PreparedStatement insertPs = connection.prepareStatement(insertSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String content = resultSet.getString(1);
            String type = resultSet.getString(2);
            // 把它插入domain表中
            insertPs.setString(1, content);
            insertPs.setString(2, type);
            insertPs.addBatch();
        }
        insertPs.executeBatch();
    }

    public static void appDomainToTemp() throws Exception {
        Connection connection = DBUtil.getCon();
        String querySql0 = "select package_name, content, count from app_domain";
        String querySql1 = "select id from app where actual_pkg_name = ?";
        String querySql2 = "select id from domain where domain = ?";
        String insertSql = "INSERT INTO `app_db`.`temp`(`app_id`, `domain_id`, `count`) VALUES (?, ?, ?)";
        PreparedStatement queryPs0 = connection.prepareStatement(querySql0);
        PreparedStatement queryPs1 = connection.prepareStatement(querySql1);
        PreparedStatement queryPs2 = connection.prepareStatement(querySql2);
        PreparedStatement insertPs = connection.prepareStatement(insertSql);
        ResultSet resultSet = queryPs0.executeQuery();
        int c = 0;
        while (resultSet.next()) {
            String appPackageName = resultSet.getString(1);
            String domain = resultSet.getString(2);
            int count = resultSet.getInt(3);
            // 查 packagename对应的APP id
            int appId = 0;
            queryPs1.setString(1, appPackageName);
            ResultSet rs = queryPs1.executeQuery();
            if (rs.next()) {
                appId = rs.getInt(1);
            } else {
                continue;
            }

            // 查 domain 的id
            int domainId = 0;
            queryPs2.setString(1, domain);
            rs = queryPs2.executeQuery();
            if (rs.next()) {
                domainId = rs.getInt(1);
            } else {
                continue;
            }
            // 插入 app_domain表
            insertPs.setInt(1, appId);
            insertPs.setInt(2, domainId);
            insertPs.setInt(3, count);
            insertPs.addBatch();
            System.out.println(++c);
            try {
                insertPs.executeBatch();
            } catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 下载的APK文件改名
     */
    public static void apkRename() throws Exception {
        Connection connection = DBUtil.getCon();
        File apkFolder = new File("D:\\app_test_complete");
        for (File apkFile : apkFolder.listFiles(file -> file.getName().endsWith(".apk"))) {
            String apkName = apkFile.getName();
            // 得到旧ID， 从app_info表中查出package name
            int outdatedId = Integer.valueOf(apkName.split("_")[0]);
            // 在app表中查出新id
            String sql = "select provided_pkg_name from app_info where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, outdatedId);
            ResultSet resultSet = preparedStatement.executeQuery();
            String provided_pkg_name = "";
            if (resultSet.next()) {
                provided_pkg_name = resultSet.getString(1);

                sql = "select id, app_name from app where provided_pkg_name = ?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, provided_pkg_name);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int newId = resultSet.getInt(1);
                    String appName = resultSet.getString(2);
                    appName = FileNameFilter.filter(appName);
                    String newName = String.valueOf(newId) + "_" + appName + ".apk";
                    System.out.println(apkFile.getName() + " 改成 " + newName);
                    apkFile.renameTo(new File(apkFile.getParent() + File.separator + newName));
                }
            }
        }
    }

    public static void test() throws Exception {
        Connection connection = DBUtil.getCon();
        String sql = "select id, domain from domain";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String id = String.valueOf(resultSet.getInt(1));
            String domain = resultSet.getString(2);
            sql = "update app_domain set domain = ? where domain = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, domain);
            preparedStatement.setString(2, id);
            preparedStatement.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        txtToApp();
//        appInfoToApp();
//        appDlToApp();
//        appDomainToDomain();
//        appDomainToTemp();
//        apkRename();
//        test();
    }
}
