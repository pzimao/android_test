package cn.edu.uestc.wechat.impl;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.EmulatorStateManager;
import cn.edu.uestc.utils.ExecUtil;
import cn.edu.uestc.utils.XMLUtil;
import cn.edu.uestc.wechat.bean.Activity;
import cn.edu.uestc.wechat.bean.Boundary;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import cn.edu.uestc.wechat.service.Tester;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class WechatTester implements Tester {

    public static Logger logger = LogManager.getLogger("微信测试");

    public WechatMessageClicker messageClicker;

    /**
     * 拿到微信小程序安装包所在的路径
     *
     * @return
     */
    public ArrayList<String> getFolderName() {
        ArrayList<String> list = new ArrayList<>();
        String execStr = ExecUtil.exec("adb shell ls /data/data/com.tencent.mm/MicroMsg/");
        for (String line : execStr.split("\n")) {
            if (line.length() != 32) {
                continue;
            }
            boolean isAppbrandExists = !"".equals(ExecUtil.exec(String.format("adb shell \"ls /data/data/com.tencent.mm/MicroMsg/%s | grep appbrand\"", line)));
            if (isAppbrandExists) {
                list.add(line);
            }
        }
        return list;
    }

    /**
     * 保存到数据库表
     *
     * @param wxxcx_id
     * @param wxapkPath
     */
    public void saveToDB(int wxxcx_id, File wxapkPath) {
        File[] wxapkgs = wxapkPath.listFiles();
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

    /**
     * 获取要测试的微信小程序的集合
     *
     * @return
     */
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
    public void click(int id, String appName) {
        // 点击之前清空模拟器小程序文件夹
        logger.info("清空小程序文件夹");
        for (String folderName : getFolderName()) {
            ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/*", folderName));
        }
        logger.info("进入小程序搜索页");
        EmulatorStateManager.gotoView(View.XCX_SEARCH1_BEFORE_V);

        logger.info("输入小程序名称 [" + appName + "]");

        Boundary clearButton = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, Resource.CLEAR_BUTTON_X);
        int[] position;
        if (clearButton != null) {
            position = clearButton.getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));

        } else {
            Boundary boundary = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, View.XCX_SEARCH1_BEFORE_V);
            position = boundary.getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        }


        for (char c : appName.toCharArray()) {
            ExecUtil.exec(String.format("adb shell am broadcast -a ADB_INPUT_TEXT --es msg '%c'", c));
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EmulatorStateManager.getCurrentView(true);
        logger.info("点击结果列表中的第1个小程序");
        position = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, Resource.XCX_RESULT_LIST_0).getPositionToBottom(0.95);
        ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // #START_判断 这里开始，操作要判断是否生效
        boolean isValid = false;
        int count = 0;
        boolean newPosition = false;
        for (int i = 0; i < 3; i++) {
            if (!newPosition) {
                EmulatorStateManager.getCurrentView(true);
                // 看到小程序logo，todo 可能会同时出现小程序和公众号，需要进一步确定点击位置
                position = XMLUtil.getBoundary(EmulatorStateManager.currentDocument, Resource.XCX_RESULT_LIST_0).getPositionToBottom(0.78);
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            } else {
                // TODO 这个位置如何确定呢？
                ExecUtil.exec(String.format("adb shell input tap %d %d", 56, 424));
            }
            try {

                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 小程序就开始加载了
            if (EmulatorStateManager.getCurrentActivity() == Activity.XCX_PLUGIN_A) {
                // 不是想要的页面
                ExecUtil.exec("adb shell input keyevent 4");
                newPosition = true;
                try {

                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (EmulatorStateManager.getCurrentView(true) != View.XCX_SEARCH_AFTER_V) {
                while (EmulatorStateManager.getCurrentActivity() == Activity.XCX_XWEB_A) {
                    try {
                        logger.info("结果页面加载");
                        Thread.sleep(5000);
                        count++;
                        if (count % 11 == 10) {
                            ExecUtil.exec("adb shell input keyevent 4");
                            ExecUtil.exec(String.format("adb shell input tap %d %d", 56, 424));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isValid = true;

                break;
            }
        }
        // #END_判断
        if (!isValid) {
            return;
        }


        String title = XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE);
        count = 0;
        while (title != null) {
            logger.info("正在加载: " + title);
            try {
                Thread.sleep(new Random().nextInt(4000) + 2000); // 随机等待2-6秒
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count > 10) {
                ExecUtil.exec("adb shell input keyevent 4");
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            title = XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE);
        }
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 需要检查是否含【关闭】图片按钮，如果含，则是小程序，否则不是
        if (XMLUtil.getBoundary(Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON) == null) {
            logger.info("点进来的不是小程序页面");
            ExecUtil.exec("adb shell input keyevent 4");
            // todo 文字识别，确定位置
            logger.info("重试");
            ExecUtil.exec(String.format("adb shell input tap %d %d", 56, 424));
            try {
                Thread.sleep(8000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            title = XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE);
            count = 15;
            while (title != null) {

                logger.info("正在加载: " + title);
                try {
                    Thread.sleep(new Random().nextInt(4000) + 2000); // 随机等待2-6秒
                } catch (Exception e) {
                    e.printStackTrace();
                }
                title = XMLUtil.getText(Resource.XCX_LOADING_STATE_3, Resource.XCX_GAME_LOADING_TITLE);
                count--;
                if (count < 0) {
                    break;
                }
            }
        }
        logger.info("###完成!####");


        // 导出安装包文件
        exportFile(appName);
        // 处理抓包文件
        if (messageClicker != null) {
            logger.info("开始处理抓包得到的文件、appid、 biz等");
            messageClicker.test(id);
        }

        // 更新wxapkg、wxxcx、wxapkg表
        // 更新小程序文件信息到数据库
        saveToDB(id, new File(dstPath, appName));
    }

    /**
     * 导出微信小程序安装文件
     *
     * @param appName
     */
    public void exportFile(String appName) {
        File tmpFile = new File(dstPath);
        File pkgFile = new File(tmpFile, "pkg");
        if (pkgFile.exists()) {
            FileUtils.deleteQuietly(pkgFile);
        }
        for (String folderName : getFolderName()) {

            tmpFile.mkdirs();
            Arrays.asList(tmpFile.listFiles()).forEach(file -> file.delete());

            ExecUtil.exec(String.format("adb shell rm /data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/.*", folderName));
            ExecUtil.exec(String.format("adb pull \"/data/data/com.tencent.mm/MicroMsg/%s/appbrand/pkg/\" %s", folderName, tmpFile));
            if (pkgFile.exists()) {
                if (!pkgFile.renameTo(new File(tmpFile.getAbsolutePath() + File.separator + appName))) {
                    FileUtils.deleteQuietly(pkgFile);
                }
            }
        }
    }


    public WechatTester(String fileExportPath, WechatMessageClicker wechatMessageClicker) {
        // wxapkg文件保存到Windows上的路径
        this.dstPath = fileExportPath;
        this.messageClicker = wechatMessageClicker;

    }

    String dstPath;

    public void test() {
        HashMap<Integer, String> map = getAppMap();
        for (Integer id : map.keySet()) {
            String appName = map.get(id);
            try {
                click(id, appName);
            } catch (Exception e) {
                try {
                    Thread.sleep(5000);
                    click(id, appName);
                    continue;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                EmulatorStateManager.restart();
                click(id, appName);
            }

        }
    }

    public static void main(String[] args) {
        new WechatTester("d:/weixin", new WechatMessageClicker("d:/fiddler_gen")).test(); // 点完小程序后处理结果
//        new WechatTester("d:/weixin", null).test(); // 只点小程序

    }
}


