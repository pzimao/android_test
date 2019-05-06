package cn.edu.uestc;

import cn.edu.uestc.utils.DeviceManager;
import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatTest2 {

    private Logger logger;
    Pattern pattern = Pattern.compile("(\\w{14}==)");

    public WechatTest2() {
        // 初始化设备；
        logger = LogManager.getLogger("wechat Tester");
    }

    // 这个方法要保证执行完成后模拟器是竖屏状态
    public void prepare() {
        // 为了保证打开时是初始页面，先做一次关闭微信的操作
        ExecUtil.exec("adb shell am force-stop com.tencent.mm");
        BufferedImage image = takeSnapshot();
        try {
            while (image.getHeight() < image.getWidth()) {
                // 打开微信
                logger.info("启动微信...");
                ExecUtil.exec("adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI");
                image = takeSnapshot();
            }
            // 判断微信是否已经完全启动了
            // 检查底部中间，如果是灰色，则启动成功了
            image = takeSnapshot();

            for (int i = 0; i < 10; i++) {
                Thread.sleep(2000);
                if (image.getRGB(410, 1386) == -1710619) {
                    break;
                }
                image = takeSnapshot();
            }
            // 打开好友对话
            // 认为这个对话已经置顶了
            // 判断是否已经正确打开会话框了

            logger.info("打开会话");
            ExecUtil.exec("adb shell input tap " + 404 + " " + 167);

            image = takeSnapshot();
            for (int i = 0; i < 10; i++) {
                Thread.sleep(300);
                if (image.getRGB(405, 1386) == -1) {
                    break;
                }
                image = takeSnapshot();
            }
            logger.info("准备好了");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("在准备阶段发生异常");
        }
    }

    public BufferedImage takeSnapshot() {
        BufferedImage bufferedImage = null;
        try {
            // 先截图，保存在模拟器里
            ExecUtil.exec("adb shell screencap /data/1.png");
            // 从模拟器导出来
            ExecUtil.exec("adb pull /data/1.png ./1.png");
            // 创建对象，把图片读到内存


            bufferedImage = ImageIO.read(new File("1.png"));
            // 这两步可以不做
            // 删除模拟器里的图片
//        ExecUtil.exec("adb shell rm /data/1.png");
            // 删除导出来的图片
//        ExecUtil.exec("del 1.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    // 计算图像相似度
    public static double computeImageSimilarity(BufferedImage image1, BufferedImage image2) {
        if (image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth()) {
            return 0;
        }
        int totalPixel = image1.getWidth() * image2.getHeight();
        int diffPixel = 0;
        for (int i = 0; i < image1.getWidth(); i++) {
            for (int j = 0; j < image1.getHeight(); j++) {
                if (image1.getRGB(i, j) != image2.getRGB(i, j)) {
                    diffPixel++;
                }
            }
        }
        return 1.0 - diffPixel * 1.0 / totalPixel;
    }

    public LinkedList<String> getMessages(String filePath) {
        LinkedList<String> messageList = new LinkedList<>();
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 检查下消息的类型
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (line.split("\t")[0].equals(matcher.group(0))) {
                        // 说明是只有biz
                        messageList.add(String.format("http://mp.weixin.qq.com/mp/getverifyinfo?__biz=%s&from=singlemessage#wechat_webview_type=1&wechat_redirect", line.split("\t")[0]));
                    } else {
                        // todo 处理消息类型
//                        messageList.add(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }

    public void sendAndClick(String bizFilePath) throws Exception {
        this.prepare();
        LinkedList<String> bizList = getMessages(bizFilePath);

        for (String message : bizList) {
            BufferedImage image1 = takeSnapshot();
            // 确定输入框y坐标
            int messageTextFieldY = image1.getHeight() - 10;
            try {
                while (image1.getRGB(150, messageTextFieldY) != -1) {
                    messageTextFieldY--;
                }
            } catch (Exception e) {
                logger.warn("未找到输入框");
                prepare();
            }
            ExecUtil.exec("adb shell input tap " + 150 + " " + messageTextFieldY);
            // 输入消息
            String sendMessage = "adb shell input text \"" + message.replace("&", "\\&") + "\"";
            ExecUtil.exec(sendMessage);
            // 确定发送按钮位置
            image1 = takeSnapshot();
            int sendButtonY = image1.getHeight() - 10;
            try {
                while (image1.getRGB(775, sendButtonY) != -16268960) {
                    sendButtonY--;
                }
            } catch (Exception e) {
                logger.warn("没找到发送按钮");
                prepare();
            }
            // 点击发送
            ExecUtil.exec("adb shell input tap " + 790 + " " + sendButtonY);
            image1 = takeSnapshot();
            // 找刚才发出去的链接并点击
            int messageY = 1300;
            try {
                while (image1.getRGB(385, messageY) != -11048043 && image1.getRGB(380, messageY) != -11048043) {
                    messageY--;
                }
            } catch (Exception e) {
                logger.warn("没找到刚才发送的消息");
                prepare();
            }
            ExecUtil.exec("adb shell input tap " + 385 + " " + messageY);
            ExecUtil.exec("adb shell input tap " + 380 + " " + messageY);
            Thread.sleep(5000);
            // 打开链接 & 从打开的页面返回
            ExecUtil.exec("adb shell input keyevent 4");

            BufferedImage image2 = takeSnapshot();
            double similarity = computeImageSimilarity(image1, image2);
            logger.info("点击链接前后界面相似度 " + similarity);
            if (similarity < 0.9) {
                // 操作错位了
                logger.info("操作错位，重启应用");
                prepare();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        DeviceManager.initial();
        WechatTest2 test = new WechatTest2();
        test.sendAndClick("C:\\Users\\pzima\\Desktop\\biz.txt");
    }
}
