package cn.edu.uestc;

import cn.edu.uestc.animal.Monkey;
import cn.edu.uestc.utils.DeviceManager;

public class AutoTest {
    public static void main(String[] args) {
        DeviceManager.work();
//        new AppInstallThread().start();
        new Monkey().play(false);
    }
}
