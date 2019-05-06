package cn.edu.uestc;

import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;


/**
 * 旋转图片工具类
 */
class RotateImageUtil {

    /**
     * 旋转图片
     *
     * @param image 图片
     * @param angel 旋转角度
     * @return
     */
    public static BufferedImage rotateImage(Image image, int angel) {
        if (image == null) {
            return null;
        }
        if (angel < 0) {
            // 将负数角度，纠正为正数角度
            angel = angel + 360;
        }
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        Rectangle rectangle = calculatorRotatedSize(new Rectangle(new Dimension(imageWidth, imageHeight)), angel);
        BufferedImage newImage = null;
        newImage = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = newImage.createGraphics();
        // transform
        graphics.translate((rectangle.width - imageWidth) / 2, (rectangle.height - imageHeight) / 2);
        graphics.rotate(Math.toRadians(angel), imageWidth / 2, imageHeight / 2);
        graphics.drawImage(image, null, null);
        return newImage;
    }

    /**
     * 计算旋转后的尺寸
     *
     * @param src
     * @param angel
     * @return
     */
    private static Rectangle calculatorRotatedSize(Rectangle src, int angel) {
        if (angel >= 90) {
            if (angel / 90 % 2 == 1) {
                int temp = src.height;
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % 90;
        }
        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;
        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;
        double angel_dalta_width = Math.atan((double) src.height / src.width);
        double angel_dalta_height = Math.atan((double) src.width / src.height);

        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        int des_width = src.width + len_dalta_width * 2;
        int des_height = src.height + len_dalta_height * 2;
        return new java.awt.Rectangle(new Dimension(des_width, des_height));
    }
}

public class WechatTest4 {

    public static Logger logger = LogManager.getLogger("wechat Tester");
//    static Pattern pattern = Pattern.compile("\\w{14}==");
//    static Pattern wxapkgPattern = Pattern.compile("([\\w-]*.wxapkg)");

    static BufferedImage enterImage = null;
    public static void prepare() {
        enterImage = takeSnapshot();
        try {
            // 下拉2次
            logger.info("第1次下拉");
            ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 423, 27, 1130, 500));

            logger.info("第2次下拉");
            ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 423, 27, 590, 100));

            BufferedImage searchImage0 = takeSnapshot();
            BufferedImage searchImage1 = null;
            do {
                // 点击搜索框
                logger.info("点击搜索框0");
                ExecUtil.exec(String.format("adb shell input tap %d %d", 202, 218));
                Thread.sleep(2000);
                searchImage1 = takeSnapshot();
            } while (computeImageSimilarity(searchImage0, searchImage1) > 0.7);

            // 输入消息
            logger.info("点击搜索框1");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 128, 80));
            Thread.sleep(500);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("在准备阶段发生异常");
        }
    }

    public static BufferedImage takeSnapshot() {
        // 先截图，保存在模拟器里
        ExecUtil.exec("adb shell screencap /data/1.png");
        // 从模拟器导出来
        ExecUtil.exec("adb pull /data/1.png ./1.png");
        // 创建对象，把图片读到内存
        BufferedImage bufferedImage = null;
        try {
            Thread.sleep(1000);
            bufferedImage = ImageIO.read(new File("1.png"));
            if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                return RotateImageUtil.rotateImage(bufferedImage, 90);
            }
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
        int totalPixel = image1.getWidth() * image2.getHeight() / 25;
        int diffPixel = 0;
        // 步长设为5，加快速度
        for (int i = 0; i < image1.getWidth(); i += 5) {
            for (int j = 0; j < image1.getHeight(); j += 5) {
                if (image1.getRGB(i, j) != image2.getRGB(i, j)) {
                    diffPixel++;
                }
            }
        }
        BigDecimal bd = new BigDecimal(1.0 - diffPixel * 1.0 / totalPixel);
        double d1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return d1;
    }

    public static String getWxapkgFodlerName() {
        String execStr = ExecUtil.exec("adb shell ls /data/data/com.tencent.mm/MicroMsg/");
        for (String line : execStr.split("\n")) {
            if (line.length() != 32) {
                continue;
            }
            boolean isAppbrandExists = !"".equals(ExecUtil.exec(String.format("adb shell \"ls /data/data/com.tencent.mm/MicroMsg/%s | grep appbrand\"", line)));
            if (isAppbrandExists) {
                return line;
            }
        }
        return "";
    }

    public static void wxapkgClick(String wxapkgName) {

        double similarity = 0.0;
        logger.info("输入小程序名称 " + wxapkgName);
        ExecUtil.exec(String.format("adb shell input tap %d %d", 675, 79));

        for (char c : wxapkgName.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
        }
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("看到搜索结果了");
        BufferedImage inputImage0 = takeSnapshot();
        // todo 处理没有搜索结果的情况

        int count = 4;

        do {
            // 点击列表第1个位置的小程序
            logger.info("点击结果列表中的第1个小程序");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 58, 184));
            try {
                Thread.sleep(4000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            similarity = computeImageSimilarity(takeSnapshot(), inputImage0);
            logger.info("点击前后相似度" + similarity + "， 相似度小于0.85时生效");
            count--;
        } while (similarity > 0.85 && count > 0);
        if (count > 0) {
            // 这里已经看到小程序logo了
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            inputImage0 = takeSnapshot();
            logger.info("截图作为参照");
            BufferedImage inputImage1 = null;
            // 计算截图中白色部分比例
            int whitePixelNumber = 0;
            int blackPixelNumber = 0;
            count = 8;

            do {
                ExecUtil.exec(String.format("adb shell input tap %d %d", 112, 233));
                logger.info("点击小程序logo");
                whitePixelNumber = 0;
                blackPixelNumber = 0;
                try {
                    Thread.sleep(15000);
                    inputImage1 = takeSnapshot();

                    for (int i = 0; i < inputImage1.getWidth(); i++) {
                        for (int j = 0; j < inputImage1.getHeight(); j++) {
                            if (inputImage1.getRGB(i, j) == -1) {
                                whitePixelNumber++;
                            } else if (inputImage1.getRGB(i, j) == -16777216) {
                                blackPixelNumber++;
                            }
                        }
                    }

                    count--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                similarity = computeImageSimilarity(takeSnapshot(), inputImage0);
                logger.info("黑白点一共有" + (whitePixelNumber + blackPixelNumber) + "个，点击前后相似度" + similarity + ", < 870000 并且 < 0.8时，点击生效");
            } while (count > 0 && (whitePixelNumber + blackPixelNumber > 870000 || similarity > 0.8));
            count = 4;
            do {
                ExecUtil.exec("adb shell input keyevent 4");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BufferedImage tempImage = takeSnapshot();
                similarity = computeImageSimilarity(tempImage, inputImage0);
                logger.info("后退前后相似度" + similarity + "，大于0.8时生效");
                double similarity2 = computeImageSimilarity(enterImage, tempImage);
                logger.info("与首页相似度" + similarity2 + "，大于0.8时生效");
                if (similarity2 > 0.8) { // 防止无限后退
                    logger.info("停止后退...");
                    prepare();
                    break;
                }
                count--;
            } while (similarity < 0.8 && count > 0);
            if (count == 0) {
                // 点击关闭按钮
                logger.info("通过按钮关闭");
                ExecUtil.exec(String.format("adb shell input tap %d %d", 655, 55));
            }
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void wxapkgFolderExport(String wxapkgName, String outDirectory) {
        final String out = outDirectory.endsWith(File.separator) ? outDirectory : outDirectory + File.separator;
        File tmpFile = new File(out);
        tmpFile.mkdirs();
        Arrays.asList(tmpFile.listFiles()).forEach(file -> file.delete());

        // todo pull的时候保证小程序已经下载完成了
        String wxapkgFodler = getWxapkgFodlerName();
        ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/.*", wxapkgFodler));
        ExecUtil.exec(String.format("adb pull \"/data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/\" %s", wxapkgFodler, tmpFile));

        new File(tmpFile.getAbsolutePath() + File.separator + "pkg").renameTo(new File(tmpFile.getAbsolutePath() + File.separator + wxapkgName));
    }


    public static void saveToDB(int wxxcx_id, String wxapkPath) {
        File[] wxapkgs = new File(wxapkPath).listFiles();
        String updateSql = "update wxxcx set file_export_state = 1 where id = ?";
        String queryWxapkg = "select id from wxapkg where wxapkg_name = ?";
        String insertToWxapkgSql = "insert into wxapkg (wxapkg_name, path)  values (?, ?)";
        String queryId = "select last_insert_id()";
        String insertToWxxcxWxapkgSql = "insert into wxxcx_wxapkg(wxxcx_id, wxapkg_id) values (?, ?)";
        try {
            // 更新点击状态
            DBManager.execute(DataSource.APP_TEST_DB, updateSql, String.valueOf(wxxcx_id));
            for (File wxapkg : wxapkgs) {
                // 先检查保存小程序安装包id是否已存在
                ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, queryWxapkg, wxapkg.getName());
                int wxapkg_id;
                if (resultSet.next()) {
                    wxapkg_id = resultSet.getInt(1);
                } else {
                    DBManager.execute(DataSource.APP_TEST_DB, insertToWxapkgSql, wxapkg.getName(), wxapkg.getPath());
                    resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, queryId);
                    resultSet.next();
                    wxapkg_id = resultSet.getInt(1);
                }
                // 关系也要保存吖
                DBManager.execute(DataSource.APP_TEST_DB, insertToWxxcxWxapkgSql, String.valueOf(wxxcx_id), String.valueOf(wxapkg_id));
            }
        } catch (NullPointerException e) {
            logger.info("没抓到wxapkg文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//        DeviceManager.initial();
        String querySql = "select id, name from wxxcx where file_export_state = 0";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, querySql);
        HashMap<Integer, String> wxxcxMap = new HashMap<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            String name = resultSet.getString(2);
            wxxcxMap.put(id, name);
        }
        prepare();
        for (int id : wxxcxMap.keySet()) {
            String name = wxxcxMap.get(id);
            ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*", getWxapkgFodlerName()));
            WechatTest4.wxapkgClick(name);
            WechatTest4.wxapkgFolderExport(name, "d:/weixin/");

            // 把刚才导出来的微信小程序保存到数据库
            WechatTest4.saveToDB(id, "d:/weixin/" + name);
        }
    }
}


