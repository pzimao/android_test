package cn.edu.uestc.utils;

import cn.edu.uestc.utils.DBUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatePreprocess1 {

    /**
     * 作用是把文本文件里的域名和URL保存到数据库app_info表中
     *
     * @param args
     */
    public static void main(String args[]) {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            Connection conn = DBUtil.getCon();
            String sql = "INSERT INTO `app_info`(`app_name`, `dl_url`, `icon_url`, `jid`) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            String pathname = "C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\result.txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathname), "UTF-8"));
            String line = null;
            int jid = 0;
            while ((line = br.readLine()) != null) {
                String[] content = line.split("\t");
                String appName = content[1];
                appName = appName.replace("?", "_").replace(":", "_").strip();
                if (!(appName.endsWith(".apk") || appName.endsWith(".APK"))) {
                    appName = appName + ".apk";
                }
                String package_name = content[2];
                String dl_url = content[4];
                if ("-1".equals(dl_url)) {
                    continue;
                }
                preparedStatement.setString(1, appName);
                preparedStatement.setString(2, dl_url);
                preparedStatement.setString(3, package_name);
                preparedStatement.setInt(4, jid++);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}