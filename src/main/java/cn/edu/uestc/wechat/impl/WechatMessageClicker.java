package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.utils.ExecUtil;
import cn.edu.uestc.utils.XMLUtil;
import cn.edu.uestc.wechat.EmulatorStateManager;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import cn.edu.uestc.wechat.service.MessageClicker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatMessageClicker implements MessageClicker {

    private Logger logger = LogManager.getLogger("URL点击线程");
    Pattern bizPattern = Pattern.compile("(\\w{14}==)");
    Pattern appidPattern = Pattern.compile("(wx[a-f0-9]{16})");
    File idFileFolder;

    public WechatMessageClicker(String idFilePath) {
        this.idFileFolder = new File(idFilePath);
    }

    public String constructMessage(String id) {
        String rawMessage = "";
        if (id.contains("==")) {
            // biz
            rawMessage = "https://mp.weixin.qq.com/mp/getverifyinfo?__biz=" + id;
        } else {
            rawMessage = "https://mp.weixin.qq.com/mp/waverifyinfo?action=get&appid=" + id;
        }
        return rawMessage;
    }

    public void sendMessage(String rawMessage) {

        logger.info("打开聊天框");
        // 打开聊天框
        EmulatorStateManager.gotoView(View.WECHAT_CHAT_V);
        // 获取文本框焦点
        int[] position = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, View.WECHAT_CHAT_V).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        logger.info("发送消息");
        // 输入消息并更新view
        String sendMessage = "adb shell input text \"" + rawMessage.replace("&", "\\&") + "\"";
        ExecUtil.exec(sendMessage);
        EmulatorStateManager.getCurrentView(true);
        position = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, Resource.CHAT_SEND_MESSAGE_BUTTON).getCenterPosition();
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        try {
            Thread.sleep(1000 + new Random().nextInt(3000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击最近的一条消息
     */
    public void clickMessage() {
        logger.info("点击最近的消息");
        // 不需要判断 view
        EmulatorStateManager.gotoView(View.WECHAT_CHAT_V);
        // 获取最近的一条消息的位置
        EmulatorStateManager.getCurrentView(true);
        int[] position = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, Resource.CHAT_LATEST_MESSAGE_BOX).getPositionToRight(0.5);
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        String title = XMLUtil.getText(Resource.WEB_PAGE);
        while ("".equals(title) || title == null) {
            try {
                Thread.sleep(new Random().nextInt(4000) + 2000); // 随机等待2-6秒
                title = XMLUtil.getText(Resource.WEB_PAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ("验证".equals(title)) {
            // todo 处理请求失败的情况
            logger.info("返回的是验证页面!");
        } else {
            logger.info("###完成#[" + title + "]");
        }
    }

    /**
     * 根据value构造url，发送到聊天界面，并点击
     *
     * @param
     */
    public void test(String id) {
        String message = constructMessage(id);
        sendMessage(message);
        clickMessage();
    }

    /**
     * 读文件构造url，发送到聊天界面，并点击
     *
     * @param
     */
    public void test() {
        File[] files = idFileFolder.listFiles(file -> file.getName().endsWith("1.txt"));
        for (File file : files) {
            String content = "";
            try {
                content = FileUtils.readFileToString(file, Charset.defaultCharset());
            } catch (Exception e) {
                e.printStackTrace();
                content = "";
            }
            HashSet<String> idSet = new HashSet<>();
            Matcher matcher = appidPattern.matcher(content);
            while (matcher.find()) {
                idSet.add(matcher.group(1));
            }
            matcher = bizPattern.matcher(content);
            while (matcher.find()) {
                idSet.add(matcher.group(1));
            }
            for (String appid : idSet) {
                String message = constructMessage(appid);
                sendMessage(message);
                clickMessage();
            }
            file.delete();
        }
    }

    public static void main(String[] args) {
        new WechatMessageClicker("D:/fiddler_gen").test();
    }
}
