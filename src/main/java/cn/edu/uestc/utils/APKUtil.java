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
        if (apkPackageName == null) {
            return "";
        }
        apkPackageName = apkPackageName.substring(apkPackageName.indexOf('\'') + 1);
        apkPackageName = apkPackageName.substring(0, apkPackageName.indexOf('\''));
        return apkPackageName;
    }

    public static void main(String[] args) {
        File fileFolder = new File("d:/app_test");
        String sql = "UPDATE `app_db`.`app` SET `actual_pkg_name` = ?, dl_state = 1 WHERE `id` = ?";
        Arrays.asList(fileFolder.listFiles()).forEach(file -> {
            String originName = file.getAbsolutePath();
            String appId = file.getName().split("_")[0];
            String packageName = getApkPackageName(originName);
            System.out.println(appId + "\t" + packageName);
            DBUtil.execute(sql, packageName, appId);
        });
//        Arrays.asList(fileFolder.listFiles()).forEach(file -> {
//            System.out.println(file.getName());
//            String packageName = getApkPackageName(file.getAbsolutePath());
//            if ("".equals(packageName)) {
//                System.out.println(file.getName() + "\t" + file.delete());
//            }
//        });

    }
}
