package cn.edu.uestc.wechat.bean;

public enum Activity {
    // 系统activity
    EMULATOR_A(0, "com.android.systemui/.recents.RecentsActivity"),
    // 桌面
    EMULATOR_DESKTOP_A(1, "com.vphone.launcher/.Launcher"), // 桌面
    // 微信主界面
    WECHAT_MAIN_A(2, "com.tencent.mm/.ui.LauncherUI"), // 微信主activity
    // 微信搜索
    WECHAT_SEARCH_A(3, "com.tencent.mm/.plugin.fts.ui.FTSMainUI"), //
    // 搜索小程序,有2种activity
    XCX_SEARCH1_A(3, "com.tencent.mm/.plugin.webview.ui.tools.fts.FTSSearchTabWebViewUI"),
    XCX_SEARCH_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandSearchUI"),
    // 内嵌浏览器
    WECHAT_WEB_A(3, "com.tencent.mm/.plugin.webview.ui.tools.WebviewMpUI"),
    // 小程序打开后的某个状态
    XCX_PLUGIN_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandPluginUI"),
    XCX_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI"), // 小程序
    XCX_1_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI1"),
    XCX_2_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI2"),
    XCX_3_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI3"),
    XCX_4_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI4"),
    XCX_5_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI5"),
    XCX_6_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI6"),
    // 小程序（微信小游戏？）加载前的加载
    XCX_XWEB_A(3, "com.tencent.mm/.plugin.appbrand.ui.AppBrandXWebDownloadProxyUI");

    public int level;
    public String name;

    Activity(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public static Activity getActivityByName(String name) {
        switch (name) {
            case "com.android.systemui/.recents.RecentsActivity":
                return EMULATOR_A;
            case "com.tencent.mm/.ui.LauncherUI":
                return WECHAT_MAIN_A;
            case "com.tencent.mm/.plugin.fts.ui.FTSMainUI":
                return WECHAT_SEARCH_A;
            case "com.tencent.mm/.plugin.webview.ui.tools.fts.FTSSearchTabWebViewUI":
                return XCX_SEARCH1_A;
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandSearchUI":
                return XCX_SEARCH_A;
            case "com.tencent.mm/.plugin.webview.ui.tools.WebviewMpUI":
                return WECHAT_WEB_A;
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandPluginUI":
                return XCX_PLUGIN_A;
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI1":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI2":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI3":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI4":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI5":
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandUI6":
                return XCX_A;
            case "com.tencent.mm/.plugin.appbrand.ui.AppBrandXWebDownloadProxyUI":
                return XCX_XWEB_A;
            case "com.vphone.launcher/.Launcher":
                return EMULATOR_DESKTOP_A;
            default:
                return EMULATOR_DESKTOP_A;
        }
    }
}
