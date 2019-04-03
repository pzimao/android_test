package cn.edu.uestc.animal;

import cn.edu.uestc.thread.DownloadThread;
import cn.edu.uestc.utils.DeviceManager;
import cn.edu.uestc.utils.TcpdumpUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Monkey {

    private final Logger logger;
    private int monkeyEventNumber;
    private long monkeyTimeout;

    public Monkey() {
        logger = LogManager.getLogger("Monkey");

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
        long monkeyThreadCheckPeriod = Long.valueOf(properties.getProperty("monkeyThreadCheckPeriod"));
        monkeyEventNumber = Integer.valueOf(properties.getProperty("monkeyEventNumber"));
        monkeyTimeout = Long.valueOf(properties.getProperty("monkeyTimeout"));

        // 设置监视器，定时检查模拟器网络状态
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final Exchanger<String> exchanger = new Exchanger<>();
                new Thread(() -> {
                    try {
                        exchanger.exchange(DeviceManager.getDevice().shell("ping -c 1 baidu.com"));
                    } catch (Exception e) {
                    }
                }).start();
                String resultString = "";
                try {
                    // 如果30秒内收不到ping的结果，就认为网络状况异常
                    resultString = exchanger.exchange("", 30, TimeUnit.SECONDS);
                } catch (Exception e) {
                }

                if ("".equals(resultString) || resultString.contains("unknown")) {
                    logger.info("模拟器网络异常，准备重启模拟器");
                    // 置请求重启标志
                    if (!DeviceManager.rebootRequested) {
                        DeviceManager.rebootRequested = true;
                    }
                } else {
                    logger.info("模拟器网络正常");
                }
            }
        };
        new Timer().schedule(timerTask, 30000, monkeyThreadCheckPeriod);
    }

    // APP测试方法
    public void play(boolean filterInstalledAppFlag) {

        HashSet<String> whiteSet = new HashSet<>();
        if (filterInstalledAppFlag) {
            Matcher matcher0 = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(DeviceManager.getDevice().shell("pm list package -3"));
            while (matcher0.find()) {
                String packageName = matcher0.group();
                whiteSet.add(packageName);
            }
        }
        boolean workFlag = true;
        while (workFlag) {
            try {
                Matcher matcher = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(DeviceManager.getDevice().shell("pm list package -3"));
                while (matcher.find()) {
                    // 检查设备状态
                    while (DeviceManager.rebootRequested) {
                        logger.info("暂停， 等待设备重启");
                        Thread.sleep(60000);
                    }
                    String packageName = matcher.group().trim();

                    if (!whiteSet.contains(packageName)) {
                        logger.info("当前测试的APP包名: " + packageName);
                        logger.info("开始抓" + packageName + "的数据包");
                        new TcpdumpUtil(packageName).start();
                        logger.info("开始测试: " + packageName);

                        Thread monkeyThread = new Thread(() ->
                                DeviceManager.getDevice().shell("monkey -p " + packageName + " " + monkeyEventNumber));
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
                        String cmdResult = DeviceManager.getDevice().shell("ps | grep -E 'monkey|tcpdump'");
                        // 解析出进程ID号
                        Pattern pattern = Pattern.compile("\\W(\\d+)\\W+\\d+\\W+\\d+\\W+\\d+\\W");
                        Matcher processIdMatcher = pattern.matcher(cmdResult);
                        while (processIdMatcher.find()) {
                            String processId = processIdMatcher.group(1);
                            DeviceManager.getDevice().shell("kill -9 " + processId);
                        }
                        logger.info("超时，强制结束monkey, tcpdump");

                        // 移除已经测试过的APK文件。
                        logger.info("测试完成 " + packageName);
                        // 这里可以有两种方式卸载APP
                        // 1. 通过Java。runtime exec 直接执行cmd命令
                        // 2. device removePackage
                        DeviceManager.getDevice().removePackage(packageName);
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

