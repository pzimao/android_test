package cn.edu.uestc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

public class APKUtil {
    public static String getApkPackageName(String apkFilePath) {
        String apkPackageName = "";
        StringBuilder stringBuilder = new StringBuilder("aapt dump badging \"").append(apkFilePath).append("\" | findstr package");
        try {
            Process process = Runtime.getRuntime().exec(stringBuilder.toString());
            InputStreamReader isr = new InputStreamReader(process.getInputStream());//将字节流转化成字符流
            BufferedReader br = new BufferedReader(isr);//将字符流以缓存的形式一行一行输出
            apkPackageName = br.readLine();
            br.close();
            isr.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        apkPackageName = apkPackageName.substring(apkPackageName.indexOf('\'') + 1);
        apkPackageName = apkPackageName.substring(0, apkPackageName.indexOf('\''));
        return apkPackageName;
    }

    public static void main(String[] args) {
        File fileFolder = new File("d:/app_test");
        String sql = "UPDATE `app_db`.`app` SET `actual_pkg_name` = ? WHERE `id` = ?";
        Arrays.asList(fileFolder.listFiles((file) -> file.getName().endsWith(".apk"))).forEach(file -> {
            String originName = file.getAbsolutePath();
            String appId = file.getName().split("_")[0];
            if (Integer.valueOf(appId) > 156935) {
//            System.out.println(appId + "\t" + originName.substring(0, originName.lastIndexOf('_')));
                file.renameTo(new File(originName.substring(0, originName.lastIndexOf('_'))));
                String packageName = getApkPackageName(originName.substring(0, originName.lastIndexOf('_')));
                System.out.println(appId + "\t" + packageName);
                // 文件名改回去
                file.renameTo(new File(originName));
                // 更新包名
                DBUtil.execute(sql, packageName, appId);
            }
        });

    }
}
