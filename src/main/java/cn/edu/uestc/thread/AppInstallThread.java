package cn.edu.uestc.thread;

import cn.edu.uestc.utils.DeviceUtil;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class AppInstallThread extends Thread {

    private IChimpDevice device;
    private final Logger logger;

    private String appFolder;
    private String appBackupFolder;
    private int intervalTimeForInstall;
    private static boolean workFlag = true;

    public AppInstallThread() {

        this.device = DeviceUtil.getDevice();
        logger = LogManager.getLogger("Installer");
        appBackupFolder = "";
        appFolder = "";
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            assert is != null;
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
    }

    @Override
    public void run() {
        File apkFolder = new File(appFolder);
        while (workFlag) {
//            Arrays.asList(apkFolder.listFiles((file) -> file.getName().endsWith(".apk"))).forEach(apkFile -> {
            Arrays.asList(apkFolder.listFiles()).forEach(apkFile -> {
                if (device.installPackage(apkFile.getAbsolutePath())) {
                    logger.info(apkFile.getName() + ":安装完成");
                }
                if (!apkFile.renameTo(new File(appBackupFolder + apkFile.getName()))) {
                    // 如果改名返回false，就直接删掉
                    if (apkFile.delete()) {
                        logger.info("删除了文件 " + apkFile.getAbsolutePath());
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
