package cn.edu.uestc;

import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;

public class WechatTest {

    private Connection connection;
    private Logger logger;

    public WechatTest() {
        // 初始化设备；
        logger = LogManager.getLogger("wechat Tester");
    }

    public void prepare() {
        try {
            // 为了保证打开时是初始页面，先做一次关闭微信的操作
            ExecUtil.exec("adb shell am force-stop com.tencent.mm");
            // 打开微信
            logger.info("启动微信");
            ExecUtil.exec("adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI");
            Thread.sleep(10000);

            // 打开好友对话
            // 认为这个对话已经置顶了
            ExecUtil.exec("adb shell input tap " + 404 + " " + 167);
            logger.info("打开会话");
            Thread.sleep(2000);
            logger.info("开始模拟点击");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("在准备阶段发生异常");
        }
    }


    public void close() {
        try {
            this.connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int repeatClick(int msgX, int msgY) throws Exception {
        logger.info("点击一条消息");
        ExecUtil.exec("adb shell input tap " + msgX + " " + msgY);
        // 点击消息后，等待一段时间
        Thread.sleep(2500);

        // 寻找公众号名字位置
        // 如果操作失败，则重试2次
        int officialAccountX = -1;
        int officialAccountY = -1;
        L1:
        for (int tryCount = 0; tryCount < 3; tryCount++) {
            BufferedImage startImage = takeSnapshot();
            for (int x = 40; x <= 700; x += 5) {
                for (int y = 110; y <= 710; y += 5) {
                    // -11048043是蓝色的公众号名字
                    if (startImage.getRGB(x, y) == -11048043) {
                        officialAccountX = x;
                        officialAccountY = y;
                        break L1;
                    }
                }
            }
            logger.info("未找到[公众号名称]，重试");
        }
        if (officialAccountX == -1) {
            // 未发现公众号位置
            // 可能是没点进去或者文章被删除了
            logger.info("[公众号名称]不存在");
            return 1; // 按一下返回键
        }
        logger.info("[公众号名称]位置是[" + officialAccountX + ", " + officialAccountY + "]");
        ExecUtil.exec("adb shell input tap " + officialAccountX + " " + officialAccountY);
        Thread.sleep(1000);
        // 点击详情按钮
        // 验证是否应该点击
        // 如果有3个黑点，那就可以点
        boolean tempFlag = false;
        for (int tryCount = 0; tryCount < 3; tryCount++) {
            BufferedImage officialAccountPage = takeSnapshot();
            if (!(officialAccountPage.getRGB(757, 72) == -16777216 && officialAccountPage.getRGB(757, 72) == -16777216 && officialAccountPage.getRGB(757, 72) == -16777216)) {
                // 没发现【...】选项
                logger.info("未找到[...]，重试");
            } else {
                // 可以点
                tempFlag = true;
                break;
            }
        }
        if (!tempFlag) {
            logger.info("[...]不存在");
            return 2; // 按2下返回键
        }
        logger.info("点击[...]");
        ExecUtil.exec("adb shell input tap " + 757 + " " + 72);
        Thread.sleep(1000);

        // 寻找更多资料的位置
        boolean abFlag = false; // 认为正常情况时，flag是false
        int moreInfoX = 50;
        int moreInfoY = -1;
        BufferedImage menuImage = takeSnapshot();
        if (menuImage.getRGB(152, 1289) == -10263709) {
            moreInfoY = 1198;
        } else if (menuImage.getRGB(50, 1394) == -10263709) {
            // 只有一项
            // 更多资料在最底下
            moreInfoY = 1394;
            abFlag = true;
        }
        if (moreInfoY == -1) {
            logger.info("[更多资料]不存在");
            return 3; // 按3下返回键
        }
        logger.info("[更多资料]的位置是[" + moreInfoX + ", " + moreInfoY + "]");

        // 点击[更多资料]
        logger.info("点击[更多资料]");
        ExecUtil.exec("adb shell input tap " + moreInfoX + " " + moreInfoY);
        Thread.sleep(1500);
        // 点击账号主体
        logger.info("点击[账号主体]");
        // 不同情况时，【账号主体】的位置不同
        if (abFlag) {
            ExecUtil.exec("adb shell input tap " + 111 + " " + 153);
            Thread.sleep(1000);
        } else {
            ExecUtil.exec("adb shell input tap " + 111 + " " + 240);
            Thread.sleep(1000);
        }
        return 4;
    }

    // 先截个屏，然后确定此屏上要点的位置
    // 再依次点
    public void play() throws Exception {
        int msgColor = -11048043;// 消息蓝色字的颜色是 -11048043
        int bgColor = -1184275;// 底色的颜色是 -1184275
        int frameColor = -6951831;// 绿框的颜色是 -6951831
        BufferedImage masterImage;
        do {
            // 屏幕截屏
            masterImage = takeSnapshot();
            // 从下往上找
            boolean isClicked = false;
            for (int startY = 1333; startY > 123; startY--) {
                if (masterImage.getRGB(355, startY) == bgColor) {
                    isClicked = false;
                } else if (masterImage.getRGB(355, startY) == msgColor) {
                    if (!isClicked) {

                        int backNumber = repeatClick(355, startY);
                        for (int i = 0; i < backNumber; i++) {
                            ExecUtil.exec("adb shell input keyevent 4");
                            Thread.sleep(300);
                        }
                        isClicked = true;
                    }
                } else {
                    continue;
                }
            }
            // 下滑一页
            for (int i = 0; i < 8; i++) {
                ExecUtil.exec("adb shell input swipe " + 27 + " " + 123 + " " + 27 + " " + 270);
            }

        } while (!(computeImageSimilarity(takeSnapshot(), masterImage) == 1)); // 如果滑动后没有发生变化，就结束
    }

    public static BufferedImage takeSnapshot() {
        // 先截图，保存在模拟器里
        ExecUtil.exec("adb shell screencap /data/1.png");
        // 从模拟器导出来
        ExecUtil.exec("adb pull /data/1.png ./1.png");
        // 创建对象，把图片读到内存
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File("1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 这两步可以不做
        // 删除模拟器里的图片
        ExecUtil.exec("adb shell rm /data/1.png");
        // 删除导出来的图片
        ExecUtil.exec("del 1.png");
        return bufferedImage;
    }

    // 这个实现只能检查图片是否完全一样
    public static float computeImageSimilarity(BufferedImage image1, BufferedImage image2) {
        if (image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth()) {
            return 0;
        }
        for (int i = 0; i < image1.getWidth(); i++) {
            for (int j = 0; j < image1.getHeight(); j++) {
                if (image1.getRGB(i, j) != image2.getRGB(i, j)) {
                    return 0;
                }
            }
        }
        return 1;
    }

    public static void main(String[] args) throws Exception {
        WechatTest test = new WechatTest();
        test.play();
        // 释放资源。
        test.close();
    }
}
