package cn.edu.uestc.utils;

import cn.edu.uestc.thread.DownloadThread;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.core.IChimpDevice;
import com.android.ddmlib.DdmPreferences;

import java.io.InputStream;
import java.util.Properties;

public class DeviceUtil {
    private static IChimpDevice device = null;

    public static IChimpDevice getDevice() {
        if (device == null) {

            Properties properties = new Properties();
            InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
            try {
                properties.load(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            device = new AdbBackend().waitForConnection(1000000, properties.getProperty("deviceId"));
            DdmPreferences.setTimeOut(500000);
        }
        return device;
    }
}
