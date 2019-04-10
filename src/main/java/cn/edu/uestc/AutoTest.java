package cn.edu.uestc;

import cn.edu.uestc.animal.Monkey;
import cn.edu.uestc.thread.AppInstallThread;
import cn.edu.uestc.thread.DataSynchronizeThread;
import cn.edu.uestc.utils.DeviceManager;

public class AutoTest {
    public static void main(String[] args) {
        // 模拟器状态监视线程
        DeviceManager.work();
        // 数据库间数据同步线程
//        new DataSynchronizeThread().start();
        // APK安装线程
        new AppInstallThread().start();
        // APP模拟点击、抓包线程
        new Monkey().play(false);
    }
}
