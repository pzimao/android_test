package cn.edu.uestc.animal;

import cn.edu.uestc.utils.DeviceUtil;
import cn.edu.uestc.utils.TcpdumpUtil;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Monkey {

    public IChimpDevice device;

    public Monkey() {
        // 初始化设备；
        this.device = DeviceUtil.getDevice();
    }

    public void play() {
        final Logger logger = LogManager.getLogger("Monkey");
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
                    logger.info("当前测试的APP包名: " + packageName);
                    if (!whiteSet.contains(packageName)) {
                        logger.info("开始抓" + packageName + "的数据包");
                        new TcpdumpUtil(device, packageName).start();
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

                        // 停止monkey，停止tcpdump
                        String cmdResult = device.shell("ps | grep -E 'monkey|tcpdump'");
                        // 解析出进程ID号
                        Pattern pattern = Pattern.compile("\\W(\\d+)\\W+\\d+\\W+\\d+\\W+\\d+\\W");
                        Matcher processIdMatcher = pattern.matcher(cmdResult);
                        while (processIdMatcher.find()) {
                            String processId = processIdMatcher.group(1);
                            device.shell("kill -9 " + processId);
                        }
                        logger.info("超时，强制结束monkey, tcpdump");

                        // 移除已经测试过的APK文件。
                        whiteSet.add(packageName);
                        logger.info("测试完成 " + packageName);
                        // todo 这里可以有两种方式卸载APP
                        // 1. 通过Java。runtime exec 直接执行cmd命令
                        // 2. device removePackage
                        device.removePackage(packageName);
                        logger.info(packageName + "卸载完成");

                        // 此时子线程就拿到了抓到的包的信息
                        // 检查下模拟器网络
                        String resultString = device.shell("ping -c 1 baidu.com");
                        if (resultString.contains("unknown")) {
                            logger.info("模拟器网络异常，重启模拟器");
                            throw new Exception("网络异常");
                        }
                    }
                }
                try {
                    // todo 2-26 设置轮回时间
                    Thread.sleep(10 * 1000);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } catch (Exception e) {
                // 重启设备
                // todo 验证是否起作用
                // todo 2019-3-21 22点04分 这个reboot 似乎没起作用
                e.printStackTrace();
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
}

