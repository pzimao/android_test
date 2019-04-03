package cn.edu.uestc.thread;

import cn.edu.uestc.utils.DeviceManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class AppInstallThread extends Thread {

    private final Logger logger;

    private String appFolder;
    private String appBackupFolder;
    private long intervalTimeForInstall;
    private long installThreadTimeout;

    private long lastInstallTime = System.currentTimeMillis();

    public AppInstallThread() {
        logger = LogManager.getLogger("Installer");
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            assert is != null;
            properties.load(is);
            appFolder = properties.getProperty("appFolder");
            appBackupFolder = properties.getProperty("appBackupFolder");
            intervalTimeForInstall = Integer.valueOf(properties.getProperty("intervalTimeForInstall"));
            long installThreadCheckPeriod = Long.valueOf(properties.getProperty("installThreadCheckPeriod"));
            installThreadTimeout = Long.valueOf(properties.getProperty("installThreadTimeout"));
            logger.info("APK文件下载位置: " + appFolder);
            logger.info("APP安装间隔时间: " + intervalTimeForInstall / 1000 + "秒");

            // 设置监视器，定时检查模拟器状态
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    // 检查安装线程状态
                    if ((System.currentTimeMillis() - lastInstallTime) > installThreadTimeout) {
                        logger.info("APP安装超时，模拟器卡住了，准备重启模拟器");
                        if (!DeviceManager.rebootRequested) {
                            DeviceManager.rebootRequested = true;
                        }
                    } else {
                        logger.info("安装线程正常");
                    }
                }
            };
            new Timer().schedule(timerTask, 30000, installThreadCheckPeriod);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        File apkFolder = new File(appFolder);
        boolean workFlag = true;
        while (workFlag) {
            try {
//            Arrays.asList(apkFolder.listFiles((file) -> file.getName().endsWith(".apk"))).forEach(apkFile -> {
                Arrays.asList(apkFolder.listFiles()).forEach(apkFile -> {
                    // 检查设备状态
                    while (DeviceManager.rebootRequested) {
                        logger.info("暂停， 等待设备重启");
                        try {
                            Thread.sleep(60000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lastInstallTime = System.currentTimeMillis();
                    }
                    if (DeviceManager.getDevice().installPackage(apkFile.getAbsolutePath())) {
                        logger.info(apkFile.getName() + ":安装完成");
                    }
                    lastInstallTime = System.currentTimeMillis();
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
                    // 一轮后休息2分钟
                    Thread.sleep(2 * 60 * 1000);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                try {
                    // 一轮后休息2分钟
                    logger.info("等待设备就绪...");
                    Thread.sleep(60 * 1000);
                } catch (Exception e1) {
                }
            }
        }
    }
}
