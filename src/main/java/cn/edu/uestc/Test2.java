package cn.edu.uestc;

import cn.edu.uestc.utils.DeviceManager;
import cn.edu.uestc.utils.ExecUtil;

public class Test2 {
    public static void main(String[] args) {
        try {
//            DeviceManager.getDevice().shell("pm list package -3");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ExecUtil.exec("tasklist | findstr Nemu");
    }
}
