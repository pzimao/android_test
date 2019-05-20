package cn.edu.uestc.test;

import cn.edu.uestc.wechat.EmulatorStateManager;
import cn.edu.uestc.wechat.bean.View;
import cn.edu.uestc.wechat.impl.WechatTester;


public class Test {


    public static void main(String[] args) {
        new WechatTester("d:/weixin", null).exportFile("测试");
    }
}