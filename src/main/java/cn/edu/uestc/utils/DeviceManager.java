package cn.edu.uestc.utils;

import cn.edu.uestc.thread.DownloadThread;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class DeviceManager {

    private static String deviceId;
    private static String emulatorPath;
    private static String emulatorProcessName;
    private static Logger logger = LogManager.getLogger("设备管理线程");
    public static IChimpDevice device;
    public static volatile boolean rebootRequested = false;

    private DeviceManager() {
    }

    static {
        Properties properties = new Properties();
        InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {
            logger.warn("读取配置文件失败");
            logger.error("DeviceManager加载失败");
            System.exit(-1);
        }
        //读取配置文件
        emulatorPath = properties.getProperty("emulatorPath");
        emulatorProcessName = properties.getProperty("processName");
        deviceId = properties.getProperty("deviceId");
        long deviceStateCheckPeriod = Long.valueOf(properties.getProperty("deviceStateCheckPeriod"));
        logger.info("模拟器位置: " + emulatorPath);

        // 设置监视器，定时检查模拟器状态
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 检查是否需要重启设备
                if (rebootRequested) {
                    logger.info("重启模拟器");
                    restartEmulator();
                    DeviceManager.rebootRequested = false;
                } else {
                    logger.info("模拟器正常运行");
                }
            }
        };
        new Timer().schedule(timerTask, 50000, deviceStateCheckPeriod);
    }

    // 模拟器启动之后，调用这个方法得到设备对象
    // 这个方法需要同步
    public synchronized static IChimpDevice getDevice() {
        if (device == null) {
            // 重新启动模拟器
            restartEmulator();

            // 获取device 对象
            AndroidDebugBridge.terminate();
            device = new AdbBackend().waitForConnection(1000000, deviceId);
            DdmPreferences.setTimeOut(500000);
        }
        return device;
    }

    public static void restartEmulator() {
        try {
            killEmulatorProcess();
            while (!startEmulatorProcess()) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 这个方法要保证模拟器已经完全启动了
    public static boolean startEmulatorProcess() {
        new Thread(() -> ExecUtil.exec(emulatorPath)).start();
        while (!ExecUtil.exec("adb connect " + deviceId).contains("connected")) {
            logger.info("尝试连接到 " + deviceId);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 10; i++) {
            logger.info("测试连接...");
            if (ExecUtil.exec("adb shell echo hello, Zimao Pang").contains("Zimao")) {
                logger.info("模拟器启动了");
                return true;
            }
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info("模拟器启动失败, 重试");
        return false;
    }

    public static void killEmulatorProcess() {
        ExecUtil.exec("adb kill-server");
        // 声明文件读取流
        BufferedReader out = null;
        BufferedReader br = null;
        try {
            // 创建系统进程
            ProcessBuilder pb = new ProcessBuilder("tasklist");
            Process p = pb.start();
            // 读取进程信息
            out = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream()), Charset.forName("GB2312")));
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getErrorStream())));

            // 创建集合 存放 进程+pid
            List<String> list = new ArrayList<>();
            // 读取
            String ostr;
            while ((ostr = out.readLine()) != null) {
                // 将读取的进程信息存入集合
                list.add(ostr);
            }

            // 遍历所有进程
            for (int i = 3; i < list.size(); i++) {
                // 必须写死,截取长度,因为是固定的
                String process = list.get(i).substring(0, 25).trim(); // 进程名
                String pid = list.get(i).substring(25, 35).trim();    // 进程号
                // 匹配指定的进程名,若匹配到,则立即杀死
                if (process.contains(emulatorProcessName)) {
                    Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                    logger.info("杀死进程 " + process + " 进程ID是 " + pid);
                }
            }

            // 若有错误信息 即打印日志
            String estr = br.readLine();
            if (estr != null) {
                logger.warn(estr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 把设备引用指向空
            device = null;
            // 关流
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
