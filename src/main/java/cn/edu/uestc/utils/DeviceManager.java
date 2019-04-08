package cn.edu.uestc.utils;

import cn.edu.uestc.thread.DownloadThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

public class DeviceManager {

    private static String deviceId;
    private static String emulatorPath;
    private static String emulatorProcessName;
    private static Logger logger = LogManager.getLogger("设备管理线程");
    public static volatile boolean isStarted = false;
    private static int retryCount = 0;
    private static String emulatorBackupPath;

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
        emulatorBackupPath = properties.getProperty("emulatorBackupPath");
        emulatorProcessName = properties.getProperty("processName");
        deviceId = properties.getProperty("deviceId");
        long deviceStateCheckPeriod = Long.valueOf(properties.getProperty("deviceStateCheckPeriod"));
        long installThreadCheckPeriod = Long.valueOf(properties.getProperty("installThreadCheckPeriod"));
        long installThreadTimeout = Long.valueOf(properties.getProperty("installThreadTimeout"));
        long monkeyThreadCheckPeriod = Long.valueOf(properties.getProperty("monkeyThreadCheckPeriod"));
        logger.info("模拟器位置: " + emulatorPath);

        // 设置监视器，定时检查模拟器状态
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 检查是否需要重启设备

                // 检查网络
                final Exchanger<String> exchanger = new Exchanger<>();
                new Thread(() -> {
                    try {
                        exchanger.exchange(ExecUtil.exec("adb shell ping -c 1 baidu.com"));
                    } catch (Exception e) {
                    }
                }).start();
                String resultString = "";
                try {
                    // 如果30秒内收不到ping的结果，就认为网络状况异常
                    resultString = exchanger.exchange("", 30, TimeUnit.SECONDS);
                } catch (Exception e) {
                }

                if (!resultString.contains("time")) {
                    logger.info("模拟器网络异常");
                    isStarted = false;
                } else {
                    logger.info("模拟器网络正常");
                    retryCount = 0;
                }

                // todo 检查安装线程状态, 现在的思路是检查现有的APP里是否有新安装的

                if (!isStarted) {
                    if (retryCount < 3) {
                        logger.info("重启模拟器");
                        restartEmulator();
                        retryCount++;

                    } else {
                        logger.info("重置模拟器");
                        resetDevice();
                        retryCount = 0;
                    }

                }
            }
        };
        new Timer().schedule(timerTask, 50000, deviceStateCheckPeriod);
    }


    public static void work() {
        if (!restartEmulator()) {
            if (!restartEmulator()) {
                logger.info("模拟器启动失败，请检查");
                System.exit(-1);
            }
        }
    }

    public static boolean restartEmulator() {
        try {
            killEmulatorProcess();
            int count = 3;
            while (count-- > 0) {
                if (startEmulatorProcess()) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("启动模拟器失败，重置模拟器");
        resetDevice();
        return false;
    }

    // 这个方法要保证模拟器已经完全启动了
    public static boolean startEmulatorProcess() {
        new Thread(() -> ExecUtil.exec(emulatorPath)).start();
        int count = 10;
        while (!ExecUtil.exec("adb connect " + deviceId).contains("connected") && count-- > 0) {
            logger.info("尝试连接到 " + deviceId);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count <= 0) {
            logger.info("模拟器启动失败, 重试");
            return false;
        }
        for (int i = 0; i < 10; i++) {
            logger.info("测试连接...");
            if (ExecUtil.exec("adb shell echo hello, Zimao Pang").contains("Zimao")) {
                logger.info("模拟器启动了");
                isStarted = true;
                return true;
            }
            try {
                Thread.sleep(10000);
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

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }

        if (dir.getName().endsWith(".dll") || dir.getName().endsWith(".exe") || dir.getName().endsWith(".png") || dir.getName().endsWith(".sys")) {
            return true;
        }
        // 目录此时为空，可以删除
        logger.info("尝试删除 " + dir.getAbsolutePath());
        return dir.delete();
    }


    public static void copyFolder(File src, File dest) {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            try {
                if (src.getName().endsWith(".dll") || src.getName().endsWith(".exe") || src.getName().endsWith(".png") || src.getName().endsWith(".sys")) {
                    return;
                }
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);
                logger.info("尝试拷贝 " + src.getAbsolutePath());
                byte[] buffer = new byte[1024];

                int length;

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            } catch (Exception e) {
                // todo 有的文件会拒绝访问
            }
        }
    }

    private static boolean resetDevice() {

        String leftApps = ExecUtil.exec("adb shell pm list package -3");
        if (!"".equals(leftApps.trim())) {
            //导出已安装但还未测试的APP包名
            String export = System.currentTimeMillis() + ".txt";
            File exportFile = new File(export);
            try {
                FileWriter fileWriter = new FileWriter(exportFile);
                fileWriter.write(leftApps);
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("已安装但还未测试的APP已导出到 " + export);
        }

        // 关闭相关进程
        killEmulatorProcess();
        // 还原模拟器
        logger.info("开始还原模拟器");
        File srcFolder = new File(emulatorBackupPath);
        File dstFolder = new File(emulatorPath.substring(0, emulatorPath.indexOf("emulator")));
        deleteDir(dstFolder);
        copyFolder(srcFolder, dstFolder);
        logger.info("还原完成");
        return true;
    }

}
