package cn.edu.uestc.wechat.bean;

public class Resource {
    // 点击类
    public static final String CHAT_TEXT_FIELD_X = "//node[@resource-id='com.tencent.mm:id/amh']";
    public static final String CHAT_SESSION_LIST_ITEM_X = "//node[@resource-id='com.tencent.mm:id/b6c']";
    public static final String CHAT_SEND_MESSAGE_BUTTON = "//node[@resource-id='com.tencent.mm:id/amp']";
    public static final String CHAT_LATEST_MESSAGE_BOX = "(//node[@resource-id='com.tencent.mm:id/oe'])[last()]";
    public static final String CHAT_BACK_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/kn']";
    public static final String XCX_SEARCH_TEXT_FIELD_0 = "//node[@resource-id='com.tencent.mm:id/q8']";
    public static final String SEARCH_TEXT_FIELD_X = "//node[@resource-id='com.tencent.mm:id/l3']";
    public static final String XCX_RESULT_LIST_0 = "//node[@resource-id='com.tencent.mm:id/bho']"; // TODO 需要进一步确认
    public static final String XCX_RESULT_LIST_1_NAME = "//node[@resource-id='com.tencent.mm:id/l3']"; // 点过来的小程序名字框，在text属性里

    // 搜索框清除按钮
    public static final String CLEAR_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/kz']"; // 清除文本框内容

    public static final String SEARCH_IMAGE_X = "//node[@resource-id='com.tencent.mm:id/jb']"; // 首页的查询按钮
    public static final String SEARCH_TYPE_4_X = "//node[@text='小程序']"; // 查询页中【小程序】选项
    public static final String SEARCH_BACK_BUTON_X = "//node[@resource-id='com.tencent.mm:id/l1']"; // 查询页的后退按钮

    public static final String XCX_SEARCH_RESULT_VIEW = "(//node[@class='android.webkit.WebView'])[last()]"; // 搜索结果
    public static final String XCX_SEARCH_RESULT_LOADING = "com.tencent.mm:id/a70"; // 搜索结果上的加载图标

    public static final String XCX_BACK_BUTTON = "//node[@resource-id='com.tencent.mm:id/oo']"; // 小程序后退按钮
    public static final String XCX_PLUGIN_PROGRESSBAR = "//node[@resource-id='com.tencent.mm:id/oz']"; // 小程序后退按钮
    // 小程序页面，显示小程序标题或者正在加载
    public static final String XCX_LOADING_WORD = "//node[@resource-id='com.tencent.mm:id/ox']"; // 加载中字样
    //    public static final String XCX_LOADING_WORD = "//node[@text='加载中 ...']"; // 加载中字样
    public static final String XCX_LOADING_STATE_1 = "//node[@resource-id='com.tencent.mm:id/w4']";
    public static final String XCX_LOADING_STATE_2 = "//node[@resource-id='com.tencent.mm:id/w5']";
    // 小程序加载过程中的标题
    public static final String XCX_LOADING_STATE_3 = "//node[@resource-id='com.tencent.mm:id/w6']";
    public static final String XCX_LOADING_STATE_4 = "//node[@resource-id='com.tencent.mm:id/w7']";

    public static final String XCX_LOADING_PROCESSBAR = "//node[@resource-id='com.tencent.mm:id/oz']"; // 加载状态图标
    // 微信小游戏加载时的界面
    public static final String XCX_GAME_LOADING_TITLE = "//node[@resource-id='com.tencent.mm:id/vq']"; // 加载时的状态
    public static final String WEB_PAGE = "//node[@resource-id='android:id/text1']"; // url 页面打开后的状态
    public static final String WEB_CLOSE_BUTTON_X = "//node[@resource-id='com.tencent.mm:id/kx']"; // url 页面关闭按钮的id

    // 小程序授权页面后退按钮
    public static final String XCX_PLUGIN_BACK = "//node[@resource-id='com.tencent.mm:id/om']";
    public static final String XCX_PLUGIN_RESOURCE = "//node[@resource-id='com.tencent.mm:id/u9']";

    // 加载进度条，小程序加载前的加载页面会有
    public static final String XCX_LOADING_RESOURCE = "//node[@resource-id='com.tencent.mm:id/a6z']";
    // 备用
    public static final String XCX_PAGE_FORWARD_ITEM = "com.tencent.mm:id/l_";

    // xpath
    public static final String XCX_PAGE_IMAGE_MORE_BUTTON = "(//node[@class='android.widget.ImageButton'])[1]";
    public static final String XCX_PAGE_IMAGE_CLOSE_BUTTON = "(//node[@class='android.widget.ImageButton'])[2]";

    // 小程序页面权限
    public static final String XCX_PAGE_PERMISSION_AGREE = "//node[@resource-id='com.tencent.mm:id/st']";

    public static final String GZH_RELATED_GZH = "//node[@text='相关公众号']";
}
