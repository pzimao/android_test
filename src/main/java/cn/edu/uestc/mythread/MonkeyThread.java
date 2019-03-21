package cn.edu.uestc.mythread;

import cn.edu.uestc.utils.DBUtil;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.DdmPreferences;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonkeyThread {

    public IChimpDevice device;
    public Connection connection;

    public static Thread watchThread;
    public String deviceId;

    public MonkeyThread(String deviceId) {
        // 初始化设备；
        this.deviceId = deviceId;
        device = new AdbBackend().waitForConnection(1000000, deviceId);
        DdmPreferences.setTimeOut(500000);
        connection = DBUtil.getCon();
    }

    public MonkeyThread() {
        connection = DBUtil.getCon();
    }


    public void appInstall() {
        new AppInstallThread(device).start();
    }


    public void autoTest() {
        final Logger logger = LogManager.getLogger("tester");
        HashSet<String> whiteSet = new HashSet<>();
        // todo 是否需要过滤已经安装的app？
//        Matcher matcher0 = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(device.shell("pm list package -3"));
//        while (matcher0.find()) {
//            String packageName = matcher0.group();
//            whiteSet.add(packageName);
//        }

        // 不测试QQ app
        whiteSet.add("com.tencent.mobileqq");
        while (true) {
            try {
                Matcher matcher = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(device.shell("pm list package -3"));
                while (matcher.find()) {

                    String packageName = matcher.group().trim();
                    if (!whiteSet.contains(packageName)) {
                        logger.info("开始抓" + packageName + "的数据包");
                        new TcpdumpThread(device, packageName).start();

                        logger.info("开始测试: " + packageName);

                        Thread monkeyThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                device.shell("monkey --pct-touch 80 --pct-motion 10 --pct-syskeys 10 -p " + packageName + " 10000");
                                device.shell("monkey -p " + packageName + " 20000");
                            }
                        });
                        monkeyThread.start();
                        long waitTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - waitTime < 60000 && monkeyThread.isAlive()) {
                            try {
                                Thread.sleep(6000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // 停止monkey线程
                        String cmdResult = device.shell("ps | grep monkey");
                        // 解析出进程ID号
                        Pattern pattern = Pattern.compile("\\W(\\d+)\\W+\\d+\\W+\\d+\\W+\\d+\\W");
                        Matcher monkeyMatcher = pattern.matcher(cmdResult);
                        while (monkeyMatcher.find()) {
                            String monkeyProcessId = monkeyMatcher.group(1);
                            // 发送kill 命令
                            device.shell("kill -9 " + monkeyProcessId);
                            logger.info("超时，强制结束monkey进程" + monkeyProcessId);
                        }


                        // 移除已经测试过的APK文件。
                        whiteSet.add(packageName);
                        logger.info("测试完成");
                        // todo 这里可以有两种方式卸载APP
                        // 1. 通过Java。runtime exec 直接执行cmd命令
                        // 2. device removePackage
                        device.removePackage(packageName);
                        logger.info(packageName + "卸载完成");
                        String pids = device.shell("pidof tcpdump").trim();
                        Matcher pidMatcher = Pattern.compile("\\d+").matcher(pids);
                        while (pidMatcher.find()) {
                            String pid = pidMatcher.group();
//                        logger.info("tcpdump的PID： " + pid);
                            device.shell("kill " + pid);
                            logger.info("kill抓包进程");
                        }
                        // 此时子线程就拿到了抓到的包的信息
//                    logger.info(packageInfo);

                        // 检查下模拟器网络
                        String resultString = device.shell("ping -c 1 baidu.com");
                        if (resultString.contains("unknown")) {
                            logger.info("模拟器网络异常，重启模拟器");
                            throw new Exception("网络异常");
                        }
                    }
                }
                try {
                    // todo 2-26 设置安装间隔时间
                    Thread.sleep(10 * 1000);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } catch (Exception e) {
                // 重启设备
                // todo 验证是否起作用
                // todo 2019-3-21 22点04分 这个reboot 似乎没起作用
                device.reboot(null);
                logger.info("设备重启了");
                try {
                    Thread.sleep(10000);
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    continue;
                }
            }
        }
    }

    public void close() {
        this.device.dispose();
        try {
            this.connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public static void main(String[] args) {
        MonkeyThread test = new MonkeyThread("127.0.0.1:7555");
        // 启动安装APP的线程；
        test.appInstall();
        // 自动测试；
        test.autoTest();
        // 释放资源。
//        test.close();
    }
}
