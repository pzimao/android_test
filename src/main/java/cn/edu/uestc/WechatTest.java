package cn.edu.uestc;

import cn.edu.uestc.utils.DeviceUtil;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;

public class WechatTest {

    private IChimpDevice device;
    private Connection connection;
    private Logger logger;

    public WechatTest() {
        // 初始化设备；
        device = DeviceUtil.getDevice();
        logger = LogManager.getLogger("wechat Tester");
    }

    public void prepare() {
        try {
            // 为了保证打开时是初始页面，先做一次关闭微信的操作
            device.shell("am force-stop com.tencent.mm");
            // 打开微信
            logger.info("启动微信");
            device.shell("am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI");
            Thread.sleep(10000);

            // 打开好友对话
            // 认为这个对话已经置顶了
            device.shell("input tap " + 404 + " " + 167);
            logger.info("打开会话");
            Thread.sleep(2000);
            logger.info("开始模拟点击");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("在准备阶段发生异常");
        }
    }


    public void close() {
        this.device.dispose();
        try {
            this.connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int repeatClick(int msgX, int msgY) throws Exception {
        logger.info("点击一条消息");
        device.shell("input tap " + msgX + " " + msgY);
        // 点击消息后，等待一段时间
        Thread.sleep(2500);

        // 寻找公众号名字位置
        // 如果操作失败，则重试2次
        int officialAccountX = -1;
        int officialAccountY = -1;
        L1:
        for (int tryCount = 0; tryCount < 3; tryCount++) {
            IChimpImage startImage = device.takeSnapshot();
            for (int x = 40; x <= 700; x += 5) {
                for (int y = 110; y <= 710; y += 5) {
                    // -11048043是蓝色的公众号名字
                    if (startImage.getPixel(x, y) == -11048043) {
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
        device.shell("input tap " + officialAccountX + " " + officialAccountY);
        Thread.sleep(1000);
        // 点击详情按钮
        // 验证是否应该点击
        // 如果有3个黑点，那就可以点
        boolean tempFlag = false;
        for (int tryCount = 0; tryCount < 3; tryCount++) {
            IChimpImage officialAccountPage = device.takeSnapshot();
            if (!(officialAccountPage.getPixel(757, 72) == -16777216 && officialAccountPage.getPixel(757, 72) == -16777216 && officialAccountPage.getPixel(757, 72) == -16777216)) {
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
        device.shell("input tap " + 757 + " " + 72);
        Thread.sleep(1000);

        // 寻找更多资料的位置
        boolean abFlag = false; // 认为正常情况时，flag是false
        int moreInfoX = 50;
        int moreInfoY = -1;
        IChimpImage menuImage = device.takeSnapshot();
        if (menuImage.getPixel(152, 1289) == -10263709) {
            moreInfoY = 1198;
        } else if (menuImage.getPixel(50, 1394) == -10263709) {
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
        device.shell("input tap " + moreInfoX + " " + moreInfoY);
        Thread.sleep(1500);
        // 点击账号主体
        logger.info("点击[账号主体]");
        // 不同情况时，【账号主体】的位置不同
        if (abFlag) {
            device.shell("input tap " + 111 + " " + 153);
            Thread.sleep(1000);
        } else {
            device.shell("input tap " + 111 + " " + 240);
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
        IChimpImage masterImage;
        do {
            // 屏幕截屏
            masterImage = device.takeSnapshot();
            // 从下往上找
            boolean isClicked = false;
            for (int startY = 1333; startY > 123; startY--) {
                if (masterImage.getPixel(355, startY) == bgColor) {
                    isClicked = false;
                } else if (masterImage.getPixel(355, startY) == msgColor) {
                    if (!isClicked) {

                        int backNumber = repeatClick(355, startY);
                        for (int i = 0; i < backNumber; i++) {
                            device.shell("input keyevent 4");
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
                device.shell("input swipe " + 27 + " " + 123 + " " + 27 + " " + 270);
            }

        } while (!device.takeSnapshot().sameAs(masterImage, 1)); // 如果滑动后没有发生变化，就结束
    }

    public static void main(String[] args) throws Exception {
        WechatTest test = new WechatTest();
        test.play();
        // 释放资源。
        test.close();
    }
}
