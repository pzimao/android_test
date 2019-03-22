package cn.edu.uestc.thread;

import cn.edu.uestc.utils.DBUtil;
import cn.edu.uestc.utils.DeviceUtil;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class AppInstallThread extends Thread {

    private IChimpDevice device;
    protected final Logger logger;
    private HashSet<String> appPackageSet;

    private String appFolder;
    private String appBackupFolder;
    private int intervalTimeForInstall;

    public AppInstallThread() {

        this.device = DeviceUtil.getDevice();
        logger = LogManager.getLogger("APP安装线程");
        appBackupFolder = "";
        appFolder = "";
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            properties.load(is);
            appFolder = properties.getProperty("appFolder");
            appBackupFolder = properties.getProperty("appBackupFolder");
            intervalTimeForInstall = Integer.valueOf(properties.getProperty("intervalTimeForInstall"));
            logger.info("APK文件下载位置: " + appFolder);
            logger.info("APP安装间隔时间: " + intervalTimeForInstall / 1000 + "秒");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }

        String installedPackageList[] = device.shell("pm list package -3").split("\n");
        appPackageSet = new HashSet<>();
        for (String packageName : installedPackageList) {
            if (packageName.split(":").length > 1) {
                appPackageSet.add(packageName.split(":")[1].trim());
            }
        }
    }

    @Override
    public void run() {
        File apkFolder = new File(appFolder);
        while (true) {
            Arrays.asList(apkFolder.listFiles((file) -> file.getName().endsWith(".apk"))).forEach(apkFile -> {
                if (device.installPackage(apkFile.getAbsolutePath())) {
                    logger.info(apkFile.getName() + ":安装完成");
                }
                // 把它从以.apk结尾改成以其它结尾
                if (!apkFile.renameTo(new File(appBackupFolder + apkFile.getName()))) {
                    // 如果改名返回false，就直接删掉
                    apkFile.delete();
                }
                // 更新数据库APP包名
                String packageList[] = device.shell("pm list package -3").split("\n");
                for (String pn : packageList) {
                    if (pn.contains(":") && appPackageSet.add(pn.trim())) {
                        String packageName = pn.split(":")[1].trim();
                        logger.info(apkFile.getName() + ":包名:" + packageName);
                        String sql = "UPDATE `app` SET `actual_pkg_name`=? WHERE `id` = ?";
                        DBUtil.execute(sql, packageName, apkFile.getName().split("_")[0]);
                        break;
                    }
                }
                try {
                    Thread.sleep(intervalTimeForInstall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            try {
                // 一轮后休息5分钟
                Thread.sleep(5 * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
