package cn.edu.uestc.wechat;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.utils.SQLUtil;
import cn.edu.uestc.wechat.bean.Wxgzh1;
import cn.edu.uestc.wechat.bean.Wxxcx1;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static Object parseInfo(File file) {
        String labelClass = "info";
        if (file.getName().contains("==")) {
            // biz
            labelClass = "verify";
        }

        // 公共部分
        HashSet<String> set = new HashSet<>();
        set.add("该帐号部分功能由以下服务商提供");
        set.add("服务类目");
        set.add("更新时间");
        HashMap<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("<h3 class=\"" + labelClass + "_item_title\">(.*)<\\/h3>\\W+<div class=\"" + labelClass + "_item_desc\">(.*)<\\/div>");

        String content = "";

        try {
            content = FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {

            String key = matcher.group(1).trim();
            if (set.contains(key)) {
                continue;
            }
            String value = matcher.group(2).trim();
            map.put(key, value);
        }
        if (file.getName().contains("==")) {
            // 处理biz
            map.put("biz", file.getName().split("\\.")[0]);

            pattern = Pattern.compile("thrid_list = (\\[[.\\W]*\\])");
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                StringBuilder stringBuilder = new StringBuilder();
                new JSONArray(matcher.group(1)).forEach(value -> stringBuilder.append(((String) value).trim()).append(";"));

                map.put("服务商", stringBuilder.toString());
            }
        } else {
            // 处理app id
//            map.put("appid", file.getName().split("\\.")[0]);
            // 解析类别
            pattern = Pattern.compile("var category_list = \\{\"cate\":(.*)};");
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                String rawCategory = matcher.group(1);
                final StringBuilder category = new StringBuilder();
                new JSONArray(rawCategory).forEach(value -> category.append(((String) value).trim()).append(";"));

                map.put("服务类目", category.toString());
            }
            // 解析域名列表
            pattern = Pattern.compile("request_domain_list = (\\[[\\W\\w]*?\\])");
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                String rawDomain = matcher.group(1);
                final StringBuilder domain = new StringBuilder();
                JSONArray jsonArray = new JSONArray(rawDomain);
                jsonArray.forEach(value -> domain.append(((String) value).trim()).append(";"));

                map.put("服务及数据", domain.toString());
            }
            // 解析时间
            pattern = Pattern.compile("output_time\\(\"(\\d+)\"");
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                long time = Long.valueOf(matcher.group(1)) * 1000;
                map.put("更新时间", new SimpleDateFormat("yyyy年MM月dd日").format(new Date(time)));
            }
            // 解析名称记录
            pattern = Pattern.compile("info_item_desc js_div_rename_history\">[\\W\\w]*>([\\w\\W]*)<\\/p>");
            matcher = pattern.matcher(content);
            while (matcher.find()) {
                map.put("名称记录", matcher.group(1).trim());
            }
        }
        Object object = null;
        if ("info".equals(labelClass)) {
            object = new Wxxcx1(map);
        } else {
            object = new Wxgzh1(map);
        }
        return object;
    }

    public static void main(String[] args) {
//        System.out.println(SQLUtil.getSQL(Parser.parseInfo("D:\\fiddler_gen\\wxcb1d78142e986838.html")));
        DBManager.execute(DataSource.APP_TEST_DB, SQLUtil.getSQL(Parser.parseInfo(new File("D:\\fiddler_gen\\MjM5NTE3MTI5NQ==.html"))));
    }
}
