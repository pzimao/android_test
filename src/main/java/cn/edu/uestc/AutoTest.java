package cn.edu.uestc;

import cn.edu.uestc.animal.Monkey;
import cn.edu.uestc.thread.AppInstallThread;
import cn.edu.uestc.utils.DeviceManager;

import java.util.concurrent.Executors;

public class AutoTest {
    public static void main(String[] args) {
        // 模拟器状态监视线程
        DeviceManager.initial();
        // 数据库间数据同步线程
//        Executors.newSingleThreadExecutor().execute(new DataSynchronizeThread());
        // APK安装线程
//        Executors.newSingleThreadExecutor().execute(new AppInstallThread());
        // APP模拟点击、抓包线程
        new Monkey().play(false);
    }
}
