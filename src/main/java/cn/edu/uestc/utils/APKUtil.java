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
}
