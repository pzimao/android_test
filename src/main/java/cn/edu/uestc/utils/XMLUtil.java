package cn.edu.uestc.utils;

import cn.edu.uestc.wechat.bean.Boundary;
import cn.edu.uestc.wechat.bean.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

public class XMLUtil {

    private static Logger logger = LogManager.getLogger("XML解析器");

    /**
     * 获取UI布局文件
     *
     * @return
     */
    public static Document getUIXmlDocument() {
        while (true) {
            // 先截图，保存在模拟器里
            ExecUtil.exec("adb shell uiautomator dump /sdcard/ui.xml");
            // 从模拟器导出来
            ExecUtil.exec("adb pull /sdcard/ui.xml ./ui.xml");
            // 创建对象，把图片读到内存
            SAXReader saxReader = new SAXReader();
            Document document;
            try {
                document = saxReader.read(new File("./ui.xml"));
            } catch (Exception e) {
                continue;
            }
            if (document != null) {
                return document;
            }
        }
    }


    public static Boundary getBoundary(String xpath) {
        Document document = getUIXmlDocument();
        return getBoundary(document, xpath);
    }

    public static Boundary getBoundary(Document document, String xpath) {
        Element connectorElement = (Element) document.selectSingleNode(xpath);
        if (connectorElement == null) {
            return null;
        }
        String result = connectorElement.attributeValue("bounds");
        return new Boundary(result);
    }

    public static Boundary getBoundary(View view) {
        Document document = getUIXmlDocument();
        return getBoundary(document, view);
    }

    public static boolean checkView(Document document, View view) {
        if (document.selectSingleNode(view.resource) == null) {
            return false;
        }
        return true;
    }

    public static Boundary getBoundary(Document document, View view) {
        if (!EmulatorStateManager.getCurrentActivity().equals(view.activity)) {
            return null;
        }
        String xpath = view.resource;
        Element connectorElement = (Element) document.selectSingleNode(xpath);
        if (connectorElement == null) {
            return null;
        }
        String result = connectorElement.attributeValue("bounds");
        return new Boundary(result);
    }

    public static String getText(String... xpaths) {
        Document document = getUIXmlDocument();
        Element connectorElement = null;
        for (String xpath : xpaths) {
            connectorElement = (Element) document.selectSingleNode(xpath);
            if (connectorElement != null) {
                String result = connectorElement.attributeValue("text");
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
