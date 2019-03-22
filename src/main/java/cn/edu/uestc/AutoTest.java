package cn.edu.uestc;

import cn.edu.uestc.animal.Monkey;
import cn.edu.uestc.thread.AppInstallThread;

public class AutoTest {
    public static void main(String[] args) {
        new AppInstallThread().start();
        new Monkey().play();
    }
}
