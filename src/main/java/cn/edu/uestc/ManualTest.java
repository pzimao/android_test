package cn.edu.uestc;

import cn.edu.uestc.mythread.AppInstallThread;
import cn.edu.uestc.mythread.DownloadThread;
import cn.edu.uestc.mythread.TcpdumpThread;
import cn.edu.uestc.utils.DBUtil;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualTest {

    public static IChimpDevice device;
    public static Connection connection;


    static {
        // 初始化设备；
        device = new AdbBackend().waitForConnection(1000000, "127.0.0.1:7555");
        DdmPreferences.setTimeOut(500000);
        connection = DBUtil.getCon();
    }

    public static void test() {
        final Logger logger = LogManager.getLogger("manual test: ");
        HashSet<String> whiteSet = new HashSet<>();
        // 排除已经安装的app
        Matcher matcher0 = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(device.shell("pm list package -3"));
        while (matcher0.find()) {
            String packageName = matcher0.group();
            whiteSet.add(packageName);
        }
        while (true) {
            try {
                Matcher matcher = Pattern.compile("(\\w+\\.)+\\w+\\n?").matcher(device.shell("pm list package -3"));
                while (matcher.find()) {

                    String packageName = matcher.group().trim();
                    if (!whiteSet.contains(packageName)) {
                        // 是新安装的APP
                        logger.info("开始抓【" + packageName + "】的数据包");
                        new TcpdumpThread(device, packageName).start();

                        logger.info("开始测试【 " + packageName+"】，请手动操作APP");
                        logger.info("在这里输入任意字符可以结束测试...");

                        // 等待控制台输入
                        new Scanner(System.in).next();

                        logger.info("【" + packageName+"】" + "结束测试");
                        device.removePackage(packageName);
                        logger.info("【" + packageName+"】" + "已经被卸载了");
                        String pids = device.shell("pidof tcpdump").trim();
                        Matcher pidMatcher = Pattern.compile("\\d+").matcher(pids);
                        while (pidMatcher.find()) {
                            String pid = pidMatcher.group();
                            device.shell("kill " + pid);
                            logger.info("kill抓包进程");
                        }
                    }
                }
                logger.info("请手动安装APP");
                Thread.sleep(10 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ManualTest.test();
    }
}
