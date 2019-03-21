package cn.edu.uestc.mythread;

import cn.edu.uestc.utils.DBUtil;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class AppInstallThread extends Thread {

    public IChimpDevice device;
    public Connection connection;
    final Logger logger = LogManager.getLogger("APP安装线程");
    public HashSet<String> installSet = new HashSet<>();
    public HashSet<String> appPackageSet = new HashSet<>();

    public AppInstallThread(IChimpDevice device) {
        this.device = device;
        this.connection = DBUtil.getCon();

        HashSet<String> installSet = new HashSet<>();
        // 更新环境
        HashSet<String> appPackageSet = new HashSet<>();
        String installedPackageList[] = device.shell("pm list package -3").split("\n");
        for (String packageName : installedPackageList) {
            if (packageName.split(":").length > 1) {
                appPackageSet.add(packageName.split(":")[1].trim());
            }
        }
    }

    @Override
    public void run() {
        String appFolder = "";
        try {
            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            properties.load(is);
            appFolder = properties.getProperty("appFolder");
            logger.info("APK文件下载位置: " + appFolder);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错");
            System.exit(-1);
        }
        while (true) {
            // 检查手机上现在有几个APP
            // 扫描文件夹，如果不空，并且手机上的APP不超过5个， 就安装，否则等待10秒。
            File apkFolder = new File(appFolder);
            try {
                // todo 2-26修改，删除了APP个数限制
                if (apkFolder.listFiles().length > 0) {
//                if (apkFolder.listFiles().length > 0 && device.shell("pm list package -3").split("\n").length < 20) {
                    Arrays.asList(apkFolder.listFiles((file) -> file.getName().endsWith(".apk") && !installSet.contains(file.getName()))).forEach(apkFile -> {
                        // 加入集合。
                        installSet.add(apkFile.getName());
                        if (device.installPackage(apkFile.getAbsolutePath())) {
                            logger.info(apkFile.getName() + ":安装完成");
                        }

                        // 把它从以.apk结尾改成以其它结尾
                        if (!apkFile.renameTo(new File("D:/app_test_complete/" + apkFile.getName()))) {
                            // 重复文件
                            // 删掉
                            apkFile.delete();
                        }
                        //                        apkFile.renameTo(new File(新文件夹名字 + apkFile.getName()));
                        // TODO 更新数据库APP包名
                        String packageList[] = device.shell("pm list package -3").split("\n");
                        String packageName = "";
                        for (String pn : packageList) {
                            if (pn.contains(":") && appPackageSet.add(pn.trim())) {
                                packageName = pn.split(":")[1].trim();
                                logger.info(apkFile.getName() + ":包名:" + packageName);
                                break;
                            }
                        }
                        String sql = "UPDATE `app_info` SET `packagename`=? WHERE `id` = ?";

                        try {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, packageName);
                            preparedStatement.setInt(2, Integer.valueOf(apkFile.getName().split("_")[0]));
                            preparedStatement.execute();
                            Thread.sleep(10 * 1000);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });
                } else {
                    Thread.sleep(30 * 1000);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                try {
                    Thread.sleep(10 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }
}
