package cn.edu.uestc.animal;

import cn.edu.uestc.thread.DownloadThread;
import cn.edu.uestc.utils.DeviceManager;
import cn.edu.uestc.utils.ExecUtil;
import cn.edu.uestc.utils.TcpdumpUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Monkey {

    private final Logger logger;
    private int monkeyEventNumber;
    private long monkeyTimeout;

    public Monkey() {
        logger = LogManager.getLogger("Monkey线程");

        // 读取配置文件
        Properties properties = new Properties();
        InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
        try {
            if (is != null) {
                properties.load(is);
            }

        } catch (IOException e) {
            logger.warn("读取配置文件失败");
            System.exit(-1);
        }
        // 解析配置信息

        monkeyEventNumber = Integer.valueOf(properties.getProperty("monkeyEventNumber"));
        monkeyTimeout = Long.valueOf(properties.getProperty("monkeyTimeout"));
    }

    // APP测试方法
    public void play(boolean filterInstalledAppFlag) {

        HashSet<String> whiteSet = new HashSet<>();
        if (filterInstalledAppFlag) {
            for (String pkgName : ExecUtil.exec("adb shell pm list package -3").split("\n")) {
                Matcher matcher0 = Pattern.compile("(\\w+\\.)+\\w+").matcher(pkgName);
                while (matcher0.find()) {
                    String packageName = matcher0.group();
                    whiteSet.add(packageName);
                }
            }
        }
        boolean workFlag = true;
        while (workFlag) {
            try {
                Matcher matcher = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(ExecUtil.exec("adb shell pm list package -3"));
                while (matcher.find()) {
                    // 检查设备状态
                    while (!DeviceManager.isStarted) {
                        logger.info("暂停， 等待设备");
                        Thread.sleep(30000);
                    }
                    String packageName = matcher.group().trim();

                    if (!whiteSet.contains(packageName)) {
                        logger.info("当前测试的APP包名: " + packageName);
                        logger.info("开始抓" + packageName + "的数据包");
                        new TcpdumpUtil(packageName).start();
                        logger.info("开始测试: " + packageName);

                        Thread monkeyThread = new Thread(() ->
                                ExecUtil.exec("adb shell monkey -p " + packageName + " " + monkeyEventNumber));
                        monkeyThread.start();
                        long waitTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - waitTime < monkeyTimeout && monkeyThread.isAlive()) {
                            try {
                                Thread.sleep(10000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // 停止monkey，停止tcpdump
                        String cmdResult = ExecUtil.exec("adb shell ps | grep -E 'monkey|tcpdump'");
                        // 解析出进程ID号
                        Pattern pattern = Pattern.compile("\\W(\\d+)\\W+\\d+\\W+\\d+\\W+\\d+\\W");
                        Matcher processIdMatcher = pattern.matcher(cmdResult);
                        while (processIdMatcher.find()) {
                            String processId = processIdMatcher.group(1);
                            ExecUtil.exec("adb shell kill -9 " + processId);
                        }
                        logger.info("超时，强制结束monkey, tcpdump");

                        // 移除已经测试过的APK文件。
                        logger.info("测试完成 " + packageName);
                        // 这里可以有两种方式卸载APP
                        // 1. 通过Java。runtime exec 直接执行cmd命令
                        // 2. device removePackage
                        ExecUtil.exec("adb uninstall " + packageName);
                        logger.info(packageName + "卸载完成");

                        // 此时子线程就拿到了抓到的包的信息
                    }
                }
                try {
                    // 做完一遍了
                    Thread.sleep(3 * 1000);
                } catch (Exception exception) {
                }
            } catch (Exception e) {
                try {
                    logger.info("等待设备就绪...");
                    Thread.sleep(60 * 1000);
                } catch (Exception exception) {
                }
            }
        }
    }
}

