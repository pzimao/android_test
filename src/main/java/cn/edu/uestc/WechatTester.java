package cn.edu.uestc;

import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 旋转图片工具类
 */
class ImageUtil {

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

    /**
     * 计算图像相似度
     */

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
}

public class WechatTester extends Tester {

    public static Logger logger = LogManager.getLogger("wechat Tester");


    public BufferedImage enterImage = null;

    /**
     * 这个方法保证微信从首页进入小程序搜索页
     */
    public void prepare() {

        enterImage = takeSnapshot();
        try {
            // 下拉2次
            do {
                logger.info("第1次下拉");
                ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 323, 27, 1130, 500));
                Thread.sleep(3000);
            } while (ImageUtil.computeImageSimilarity(takeSnapshot(), enterImage) > 0.5);


            logger.info("第2次下拉");
            ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 323, 27, 590, 100));

            BufferedImage searchImage0 = takeSnapshot();
            BufferedImage searchImage1 = null;
            do {
                // 点击搜索框
                logger.info("点击搜索框0");
                ExecUtil.exec(String.format("adb shell input tap %d %d", 202, 218));
                Thread.sleep(2000);
                searchImage1 = takeSnapshot();
            } while (ImageUtil.computeImageSimilarity(searchImage0, searchImage1) > 0.7);

            // 输入消息
            logger.info("点击搜索框1");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 128, 80));
            Thread.sleep(500);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("在准备阶段发生异常");
        }
    }

    /**
     * 截屏
     *
     * @return
     */
    public BufferedImage takeSnapshot() {
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
                return ImageUtil.rotateImage(bufferedImage, 90);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bufferedImage;
    }


    /**
     * 拿到微信小程序安装包所在的路径
     *
     * @return
     */
    @Override
    public String getFolderName() {
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

    /**
     * 保存到数据库表
     *
     * @param wxxcx_id
     * @param wxapkPath
     */
    public void saveToDB(int wxxcx_id, String wxapkPath) {
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

    public void saveAppIdToDB(int id, String appIdStr) {
        String updateSql = "update wxxcx set app_id = ?, appid_extract_state = 1 where id = ?";
        DBManager.execute(DataSource.APP_TEST_DB, updateSql, appIdStr, String.valueOf(id));
    }

    /**
     * 得到已有的Fiddler抓包得到的appId的集合
     */
    @Override
    public void initSet() {
        try {
            // 把已有的app id放到一个集合中
            // 2019-5-14新增，如果文件不存在，直接返回
            if (!new File(txtPath).exists()) {
                return;
            }
            FileInputStream fileInputStream = new FileInputStream(txtPath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                Matcher matcher = appIdPattern.matcher(str);
                while (matcher.find()) {
                    String appId = matcher.group(0);
                    appIdSet.add(appId);
                }
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取要测试的微信小程序的集合
     *
     * @return
     */
    @Override
    public HashMap<Integer, String> getAppMap() {
        // 查找要测试的微信小程序，把它们放到集合中
        HashMap<Integer, String> appMap = new HashMap<>();
        String querySql = "select id, name from wxxcx where file_export_state = 0";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, querySql);
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                appMap.put(id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appMap;
    }

    /**
     * 模拟点击微信小程序
     *
     * @param appName
     */
    @Override
    public void click(String appName) {
        if (enterImage == null || ImageUtil.computeImageSimilarity(takeSnapshot(), enterImage) > 0.8) {
            prepare();
        }
        // 点击之前清空模拟器小程序文件夹
        ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*", folderName));

        double similarity = 0.0;
        logger.info("输入小程序名称 " + appName);
        ExecUtil.exec(String.format("adb shell input tap %d %d", 675, 79));

        for (char c : appName.toCharArray()) {
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
            similarity = ImageUtil.computeImageSimilarity(takeSnapshot(), inputImage0);
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
                if (whitePixelNumber + blackPixelNumber > 870000) {
                    logger.info("等待小程序加载...");
                } else {
                    ExecUtil.exec(String.format("adb shell input tap %d %d", 112, 233));
                    logger.info("点击小程序logo");
                }
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
                similarity = ImageUtil.computeImageSimilarity(takeSnapshot(), inputImage0);
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
                similarity = ImageUtil.computeImageSimilarity(tempImage, inputImage0);
                logger.info("后退前后相似度" + similarity + "，大于0.8时生效");
                double similarity2 = ImageUtil.computeImageSimilarity(enterImage, tempImage);
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
                ExecUtil.exec(String.format("adb shell input tap %d %d", 1215, 58));
            }
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 导出微信小程序安装文件
     *
     * @param appName
     */
    @Override
    public void exportFile(String appName) {
        final String out = dstPath.endsWith(File.separator) ? dstPath : dstPath + File.separator;
        File tmpFile = new File(out);
        tmpFile.mkdirs();
        Arrays.asList(tmpFile.listFiles()).forEach(file -> file.delete());

        ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/.*", folderName));
        ExecUtil.exec(String.format("adb pull \"/data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/\" %s", folderName, tmpFile));

        new File(tmpFile.getAbsolutePath() + File.separator + "pkg").renameTo(new File(tmpFile.getAbsolutePath() + File.separator + appName));

    }

    /**
     * 更新数据库表
     *
     * @param id
     */
    @Override
    public void updateTable(Integer id) {
        try {
            // 根据ID查到app name
            String sql = "select name from wxxcx where id = ?";
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql, String.valueOf(id));
            String appName = "";
            while (resultSet.next()) {
                appName = resultSet.getString(1);
                break;
            }
            // 更新小程序文件信息到数据库
            saveToDB(id, dstPath + appName);

            // 提取这次点击出现的APP id
            String appIdStr = "";


            // 把已有的app id放到一个集合中
            if (new File(txtPath).exists()) {
                FileInputStream fileInputStream = new FileInputStream(txtPath);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    Matcher matcher = appIdPattern.matcher(str);
                    while (matcher.find()) {
                        String appId = matcher.group(0);
                        if (appIdSet.add(appId)) {
                            // 发现新的APP id了
                            appIdStr += (appId + ";");
                        }
                    }
                }
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
            }
            // 把新发现的APP id 更新到数据库表
            saveAppIdToDB(id, appIdStr);
            if (appIdStr.length() > 0) {
                while (ImageUtil.computeImageSimilarity(takeSnapshot(), enterImage) < 0.8) {
                    ExecUtil.exec("adb shell input keyevent 4");
                }
                // 使用这些app id 构造连接，发送并点击
                sendAndClick(appIdStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据appId构造url，发送到聊天界面，并点击
     *
     * @param appIdStr
     */
    public void sendAndClick(String appIdStr) {
        try {
            String[] appIdArray = appIdStr.split(";");
            if (appIdArray.length == 0) {
                return;
            }
            // todo 进入聊天
            logger.info("打开会话");
            ExecUtil.exec("adb shell input tap " + 404 + " " + 167);

            logger.info("准备好了");


            for (String appId : appIdArray) {
                String message = "https://mp.weixin.qq.com/mp/waverifyinfo?action=get&appid=" + appId;

                // 确定输入框y坐标
                int messageTextFieldY = 1231;

                ExecUtil.exec("adb shell input tap " + 150 + " " + messageTextFieldY);
                // 输入消息
                String sendMessage = "adb shell input text \"" + message.replace("&", "\\&") + "\"";
                ExecUtil.exec(sendMessage);
                // 点击发送
                ExecUtil.exec("adb shell input tap " + 675 + " " + 1207);
                Thread.sleep(1000 + new Random().nextInt(3000));
                BufferedImage image1 = takeSnapshot();
                // 找刚才发出去的链接并点击
                int messageY = 1170;
                try {
                    while (image1.getRGB(330, messageY) != -11048043 && image1.getRGB(380, messageY) != -11048043) {
                        messageY--;
                    }
                } catch (Exception e) {
                    logger.warn("没找到刚才发送的消息");
                    prepare();
                }
                ExecUtil.exec("adb shell input tap " + 385 + " " + messageY);
                ExecUtil.exec("adb shell input tap " + 380 + " " + messageY);
                Thread.sleep(5000 + new Random().nextInt(3000));
                // 打开链接 & 从打开的页面返回
                ExecUtil.exec("adb shell input keyevent 4");
                Thread.sleep(3000);
                double similarity = ImageUtil.computeImageSimilarity(image1, takeSnapshot());
                logger.info("点击链接前后界面相似度 " + similarity);
                if (similarity < 0.66) {
                    // 操作错位了
                    logger.info("操作错位！！！");
                }
            }
            // todo 恢复小程序测试
            while (ImageUtil.computeImageSimilarity(takeSnapshot(), enterImage) < 0.8) {
                ExecUtil.exec("adb shell input keyevent 4");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WechatTester(String txtPath, String dstPath) {
        // todo 可以写在配置文件

        // 抓包得到的文本文件的路径
        this.txtPath = txtPath;
        // 文件保存到Windows上的路径
        this.dstPath = dstPath;
    }

    // 速度控制
    String txtPath;
    String dstPath;
    private final Pattern appIdPattern = Pattern.compile("wx[a-f0-9]{16}");

    public static void main(String[] args) {
        new WechatTester("1", "1").sendAndClick("wx76222b2ffe0c7ac7;wx76222b2ffe0c7ac7;");
    }
}


