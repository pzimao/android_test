package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Activity;
import cn.edu.uestc.wechat.bean.Resource;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorStateManager {
    private static Logger logger = LogManager.getLogger("模拟器状态管理器");
    private static View currentView = null;
    public static Document currentDocument = null;

    public static Activity getCurrentActivity() {
        return getCurrentActivityList().get(0);
    }

    public static ArrayList<Activity> getCurrentActivityList() {
        ArrayList<Activity> activityList = new ArrayList<>();
        Pattern pattern = Pattern.compile("Run #\\d+: \\w+\\{\\w+ \\w+ ((\\w*?\\.)+\\w*\\/(\\.\\w+)*\\w+)");
        String cmd = "adb shell dumpsys activity activities";
        do {
            String result = ExecUtil.exec(cmd);
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                activityList.add(Activity.getActivityByName(matcher.group(1)));
            }
        } while (activityList.size() == 0);

        return activityList;
    }

    public static View getCurrentView(boolean needUpdate) {

        if (!needUpdate && currentView != null) {
            return currentView;
        }
        currentDocument = XMLUtil.getUIXmlDocument();
        Activity activity = getCurrentActivity();
        ArrayList<View> views = View.getViewList(activity);
        for (View view : views) {
            if (XMLUtil.checkView(currentDocument, view)) {
                logger.info("当前view 是 " + view);
                currentView = view;
                break;
            }
        }
        return currentView;
    }

    public static void restart() {
        ExecUtil.exec("adb shell am start -W -S com.tencent.mm/.ui.LauncherUI");
        while (XMLUtil.getBoundary(Resource.CHAT_SESSION_LIST_ITEM_X) == null) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void gotoDefaultView() {
        if (!getCurrentActivity().name.equals(Activity.WECHAT_MAIN_A.name)) {
            do {
                ExecUtil.exec("adb shell am start -R 3 com.tencent.mm/.ui.LauncherUI");
            } while (!getCurrentActivity().name.equals(Activity.WECHAT_MAIN_A.name));
            // 更新view
            getCurrentView(true);
        }
        int count = 10;
        while (getCurrentView(false).compareTo(View.WECHAT_MAIN_V) > 0) {
            int[] position = XMLUtil.getBoundary(currentDocument, currentView.backward).getCenterPosition();
            ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            // 更新currentView
            getCurrentView(true);
            count--;
            // todo 处理无限循环
            if (count < 0) {
                ExecUtil.exec("adb shell am start -R 3 com.tencent.mm/.ui.LauncherUI");
                try {
                    Thread.sleep(20000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gotoDefaultView();
            }
        }
    }

    /**
     * 这个方法要保证进入targetView
     *
     * @param targetView
     */
    public static void gotoView(View targetView) {
//        // 如果应用没有启动 那么需要先启动应用
//        if ("".equals(ExecUtil.exec("adb shell \"ps|grep com.tencent.mm:appbrand0\"").trim())) {
//            ExecUtil.exec("adb shell am start -S -W com.tencent.mm/.ui.LauncherUI");
//        }
        getCurrentView(true);
        if (currentView == null || currentView.index / 100 != targetView.index / 100) {
            // 需要经过首页
            logger.info("跳到默认页");
            gotoDefaultView();
            logger.info("打开默认页");
        }
        if (currentView == targetView) {
            return;
        }

        int count = 0;
        while (currentView != targetView) {
            count++;
            String xpath = "";
            if (currentView == View.WECHAT_MAIN_V) {

                if (targetView.index > 200) {
                    xpath = currentView.forward;
                } else {
                    xpath = currentView.backward;
                }
            } else {
                if (currentView.compareTo(targetView) > 0) {
                    xpath = currentView.backward;
                } else {
                    xpath = currentView.forward;
                }
            }
            if (count >= 10 || xpath == null) {
                ExecUtil.exec("adb shell input keyevent 4");
                count = 0;
            } else {
                int[] position = XMLUtil.getBoundary(currentDocument, xpath).getCenterPosition();
                ExecUtil.exec(String.format("adb shell input tap %d %d", position[0], position[1]));
            }
            // 更新currentView
            getCurrentView(true);
        }
    }


    public static void main(String[] args) {

        while (true) {
            System.out.println(getCurrentView(true));
//            System.out.println(getCurrentActivity());
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
