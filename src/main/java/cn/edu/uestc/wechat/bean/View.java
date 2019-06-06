package cn.edu.uestc.wechat.bean;

import java.util.ArrayList;
import java.util.Comparator;

public enum View implements Comparator<View> {
    // 首页
    WECHAT_MAIN_V(0, Activity.WECHAT_MAIN_A, Resource.CHAT_SESSION_LIST_ITEM_X, Resource.SEARCH_IMAGE_X, Resource.CHAT_SESSION_LIST_ITEM_X), // 首页
    // 总类别搜索页
    WECHAT_SEARCH_V(101, Activity.WECHAT_SEARCH_A, Resource.SEARCH_TYPE_4_X, Resource.SEARCH_BACK_BUTON_X, Resource.SEARCH_TYPE_4_X), // 搜索0
    // 小程序搜索页，搜索前
    XCX_SEARCH_BEFORE_V(102, Activity.XCX_SEARCH_A, Resource.SEARCH_TEXT_FIELD_X, Resource.SEARCH_BACK_BUTON_X, null),
    // 小程序搜索页，搜索前
    XCX_SEARCH1_BEFORE_V(102, Activity.XCX_SEARCH1_A, Resource.SEARCH_TEXT_FIELD_X, Resource.SEARCH_BACK_BUTON_X, null),

    // 小程序搜索页，搜索后
    XCX_SEARCH_AFTER_V(103, Activity.XCX_SEARCH_A, Resource.CLEAR_BUTTON_X, Resource.CLEAR_BUTTON_X, null), // 搜索结果

    // 小程序、小程序授权页面，使用后退或者跳转activity离开小程序页，不需要处理权限申请对话框
    XCX_V(104, Activity.XCX_A, Resource.XCX_PLUGIN_BACK, Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON, null), // 小程序
    XCX_CONFIRM_V(105, Activity.XCX_A, Resource.XCX_PAGE_PERMISSION_AGREE, Resource.XCX_PAGE_PERMISSION_AGREE, null), // 小程序
    XCX_PLUGIN_V(107, Activity.XCX_PLUGIN_A, Resource.XCX_PLUGIN_PROGRESSBAR, Resource.XCX_BACK_BUTTON, null), // 小程序
    // 小程序加载前的加载视图，可能会很耗时，也可能卡住
    XCX_XWEB_V(108, Activity.XCX_XWEB_A, Resource.XCX_LOADING_RESOURCE, null, null), // 小程序
    XCX_LOADING_V(109, Activity.XCX_PLUGIN_A, Resource.XCX_LOADING_STATE_3, Resource.XCX_PAGE_IMAGE_CLOSE_BUTTON, null), // 小程序

    // 聊天框
    WECHAT_CHAT_V(201, Activity.WECHAT_MAIN_A, Resource.CHAT_TEXT_FIELD_X, Resource.CHAT_BACK_BUTTON_X, null), // 对话框打开后的页面
    // 浏览器
    WECHAT_WEB_V(202, Activity.WECHAT_WEB_A, Resource.WEB_CLOSE_BUTTON_X, Resource.WEB_CLOSE_BUTTON_X, null); // url点开后的网页

    public int index;
    public Activity activity;
    public String resource; // 用来标识view

    public String backward;

    public String forward;

    View(int index, Activity activity, String resource, String backward, String forward) {
        this.index = index;
        this.activity = activity;
        this.resource = resource;

        this.backward = backward;
        this.forward = forward;
    }

    public View get(int index) {
        switch (index) {

            case 101:
                return View.WECHAT_SEARCH_V;
            case 102:
                return View.XCX_SEARCH_BEFORE_V;
            case 103:
                return View.XCX_SEARCH_AFTER_V;
            case 104:
                return View.XCX_V;
            case 105:
                return View.XCX_CONFIRM_V;
            case 201:
                return View.WECHAT_CHAT_V;
            case 202:
                return View.WECHAT_WEB_V;
            case 107:
                return View.XCX_PLUGIN_V;
            case 108:
                return View.XCX_XWEB_V;
            case 109:
                return View.XCX_LOADING_V;
            default:
                return View.WECHAT_MAIN_V;
        }
    }

    public static ArrayList<View> getViewList(Activity activity) {
        ArrayList<View> viewList = new ArrayList<>();
        for (View view : getViewList()) {
            if (view.activity == activity) {
                viewList.add(view);
            }
        }
        return viewList;
    }

    public static ArrayList<View> getViewList() {
        ArrayList<View> viewList = new ArrayList<>();
        viewList.add(XCX_CONFIRM_V);
        viewList.add(XCX_V);
        viewList.add(WECHAT_MAIN_V);
        viewList.add(XCX_SEARCH_AFTER_V);
        viewList.add(WECHAT_SEARCH_V);
        viewList.add(XCX_SEARCH_BEFORE_V);
        viewList.add(XCX_SEARCH1_BEFORE_V);

        viewList.add(XCX_PLUGIN_V);

        viewList.add(WECHAT_CHAT_V);
        viewList.add(WECHAT_WEB_V);
        viewList.add(XCX_XWEB_V);
        return viewList;
    }

    @Override
    public String toString() {
        return "View{" +
                "index=" + index +
                ", activity=" + activity.name +
                ", resource='" + resource + '\'' +
                '}';
    }

    @Override
    public int compare(View o1, View o2) {
        return Integer.compare(o1.index, o2.index);
    }
}
