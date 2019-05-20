package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.ExecUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatTest3 {

    public static Logger logger = LogManager.getLogger("wechat Tester");
    static Pattern pattern = Pattern.compile("\\w{14}==");
    static Pattern wxapkgPattern = Pattern.compile("([\\w-]*.wxapkg)");

    // 这个方法要保证执行完成后模拟器是竖屏状态
    public static void prepare() {
        // 为了保证打开时是初始页面，先做一次关闭微信的操作
        ExecUtil.exec("adb shell am force-stop com.tencent.mm");
        BufferedImage image = takeSnapshot();
        try {
            while (image.getHeight() < image.getWidth()) {
                // 打开微信
                logger.info("启动微信...");
                ExecUtil.exec("adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI");
                Thread.sleep(4000);
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
            // 下拉2次
            logger.info("第1次下拉");
            ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 123, 27, 430, 300));

            logger.info("第2次下拉");
            ExecUtil.exec(String.format("adb shell input swipe %d %d %d %d %d", 27, 323, 27, 390, 100));
            logger.info("准备好了");
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
            bufferedImage = ImageIO.read(new File("1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 这两步可以不做
        // 删除模拟器里的图片
//        ExecUtil.exec("adb shell rm /data/1.png");
        // 删除导出来的图片
//        ExecUtil.exec("del 1.png");
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
        BufferedImage initialImage = takeSnapshot();
        // 如果没有打开微信，则打开
        if (initialImage.getWidth() > initialImage.getHeight()) {
            prepare();
        }

        BufferedImage searchImage0 = takeSnapshot();
        BufferedImage searchImage1 = null;
        do {
            // 点击搜索框
            logger.info("点击搜索框0");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 340, 200));
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchImage1 = takeSnapshot();
        } while (computeImageSimilarity(searchImage0, searchImage1) > 0.7);
        // 输入消息
        logger.info("点击搜索框1");
        ExecUtil.exec(String.format("adb shell input tap %d %d", 80, 80));
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("输入小程序名称 " + wxapkgName);
        for (char c : wxapkgName.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedImage inputImage0 = takeSnapshot();
        BufferedImage inputImage1 = null;
        // 处理没有搜索结果的情况

        // 点击列表第1个位置的小程序
        logger.info("点击第1个小程序");
        ExecUtil.exec(String.format("adb shell input tap %d %d", 260, 202));
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        inputImage1 = takeSnapshot();
        if (computeImageSimilarity(inputImage0, inputImage1) > 0.9) {
            // 没有结果，后退吧
            logger.info("没有结果,后退");
            ExecUtil.exec("adb shell input keyevent 4");
            return;
        }

        // todo 这里可能闪退
        BufferedImage wxappImage0 = takeSnapshot();
        BufferedImage wxappImage1 = null;

        if (computeImageSimilarity(wxappImage0, searchImage0) > 0.8) {
            // 后退1次
            logger.info("闪退到了初始页面");
            return;
        }
        do {
            logger.info("再次点击第1个小程序");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 260, 160));
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            wxappImage1 = takeSnapshot();
        } while (computeImageSimilarity(wxappImage0, wxappImage1) > 0.7);
        try {
            // 等待一段时间
            Thread.sleep(6000);
            // todo 判断当前位置
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedImage finishImage = takeSnapshot();
        if (computeImageSimilarity(wxappImage0, finishImage) > 0.8) {
            // 后退1次
            logger.info("闪退到了小程序搜索结果页面");
            ExecUtil.exec("adb shell input keyevent 4");
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
        ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*", wxapkgFodler));
        new File(tmpFile.getAbsolutePath() + File.separator + "pkg").renameTo(new File(tmpFile.getAbsolutePath() + File.separator + wxapkgName));
    }

    public static void wxapkgExportByTime(String outDirectory, String wxapkgName, Date startDate) throws Exception {
        String out = outDirectory.endsWith(File.separator) ? outDirectory : outDirectory + File.separator;
        // 临时文件夹
        File tmpFile = new File(out + "pkg" + File.separator);
        tmpFile.mkdirs();
        Arrays.asList(tmpFile.listFiles()).forEach(file -> file.delete());


        // todo pull的时候保证小程序已经下载完成了
        String wxapkgFodler = getWxapkgFodlerName();
        LinkedList<String> wxapkgList = new LinkedList<>();
        // 拿到pkg文件夹下的所有wxapkg文件名和创建时间
        String wxapkgString = ExecUtil.exec(String.format("adb shell ls -l /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*.wxapkg", wxapkgFodler));
        // 筛选出最近的文件
        for (String line : wxapkgString.split("\n")) {
            Matcher matcher = wxapkgPattern.matcher(line);
            while (matcher.find()) {
                String wxapkgFileName = matcher.group(1);
                // 这里应该拿到精确到秒的时间
                String fileInfo = ExecUtil.exec(String.format("adb shell stat /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/%s | grep Modify", wxapkgFodler, wxapkgFileName));
                Date fileDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fileInfo.substring(fileInfo.indexOf(" ") + 1, fileInfo.lastIndexOf(".")));
                if (fileDate.after(startDate)) {
                    logger.info("添加 " + wxapkgFileName + "\t开始时间: " + startDate.toString() + " 文件创建时间: " + fileDate.toString());

                    wxapkgList.add(wxapkgFileName);
                }
            }
        }
        for (String wxapkgFileName : wxapkgList) {
            ExecUtil.exec(String.format("adb pull \"/data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/%s\" %s", wxapkgFodler, wxapkgFileName, tmpFile));
        }

        tmpFile.renameTo(new File(tmpFile.getAbsolutePath().substring(0, tmpFile.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + wxapkgName));
    }

    public static void wxapkgProcess(ArrayList<String> wxapkgNameList, String outDirectory) throws Exception {
        for (String wxapkgName : wxapkgNameList) {
            Date startDate = new Date();
            // 这里拿到模拟器时间
            String emulatorTime = ExecUtil.exec("adb shell date +%s") + "000";
            startDate.setTime(Long.valueOf(emulatorTime));
            // 点击小程序
            wxapkgClick(wxapkgName);
            // 文件导出
            wxapkgExportByTime(outDirectory, wxapkgName, startDate);
//            wxapkgFolderExport(outDirectory, wxapkgName);
        }
    }

    public static void saveToDB(String wxapkPath) {
        String insertToWxxcxSql = "insert into wxxcx(wxxcx_name) select ? from dual where not exists (select * from wxxcx where wxxcx_name = ? )";
        DBManager.execute(DataSource.APP_TEST_DB, insertToWxxcxSql, wxapkPath.substring(wxapkPath.lastIndexOf("/") + 1), wxapkPath.substring(wxapkPath.lastIndexOf("/") + 1));
        File[] wxapkgs = new File(wxapkPath).listFiles();
        String insertToWxapkgSql = "insert into wxapkg (wxapkg_name, path) select ?, ? from dual where not exists (select * from wxapkg where wxapkg_name = ?)";
        String insertToWxxcxWxapkgSql = "insert into wxxcx_wxapkg(wxxcx_name, wxapkg_name) select ?, ? where not exists (select * from wxxcx_wxapkg where wxxcx_name = ? and wxapkg_name = ?)";
        for (File wxapkg : wxapkgs) {
            DBManager.execute(DataSource.APP_TEST_DB, insertToWxapkgSql, wxapkg.getName(), wxapkg.getAbsolutePath(), wxapkg.getName());
            // 关系也要保存吖
            DBManager.execute(DataSource.APP_TEST_DB, insertToWxxcxWxapkgSql, wxapkPath.substring(wxapkPath.lastIndexOf("/") + 1), wxapkg.getName(), wxapkPath.substring(wxapkPath.lastIndexOf("/") + 1), wxapkg.getName());
        }
    }

    public static void main(String[] args) throws Exception {
//        DeviceManager.initial();
        ArrayList<String> list = new ArrayList<>();
        list.add("裹然好");
        list.add("睿意生活馆");
        list.add("卫卿书院");
        list.add("君品优选");
        for (String wxapkName : list) {
            // 做2遍以保证包完全下载了
            for (int i = 0; i < 2; i++) {
                WechatTest3.wxapkgClick(wxapkName);
            }
            WechatTest3.wxapkgFolderExport(wxapkName, "d:/weixin/");

            // 把刚才导出来的微信小程序保存到数据库
            WechatTest3.saveToDB("d:/weixin/" + wxapkName);
        }

    }
}
