package cn.edu.uestc.utils;

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
import java.util.Scanner;
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

            if (key.equals("企业全称") || key.equals("企业名称") || key.equals("政府名称") || key.equals("其他组织名称")) {
                key = "企业全称";
            }
            if (key.contains("工商执照注册号") || key.contains("组织机构代码")) {
                key = "工商执照注册号";
            }
            set.add(key);
            String value = matcher.group(2).trim();
            Pattern otherPattern = Pattern.compile("<.*>(.*)<\\/.*>");
            Matcher otherMatcher = otherPattern.matcher(value);
            if (otherMatcher.find()) {
                // 2019-5-27 处理电话字段
                value = otherMatcher.group(1);
                if (!set.contains(key)) {
                    map.put(key, key + ":" + value);
                }
            } else {
                map.put(key, value);
            }
        }

        pattern = Pattern.compile("thrid_list = (\\[[.\\W]*\\])");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            StringBuilder stringBuilder = new StringBuilder();
            new JSONArray(matcher.group(1)).forEach(value -> {
                if (((String) value).trim().length() > 0) {
                    stringBuilder.append(((String) value).trim()).append(";");
                }
            });

            map.put("服务商", stringBuilder.toString());
        }

        // 解析名称记录
        pattern = Pattern.compile("<p class=\\\"js_item\\\"[\\S\\s.]*?>([\\S\\s.]*?)<\\/p>");
        matcher = pattern.matcher(content);
        String mcjl = "";
        while (matcher.find()) {
            String jl = matcher.group(1).trim().replaceAll("\\s", "");
            if (jl.length() > 0) {
                mcjl += jl + ";";
            }
        }
        if (mcjl.length() > 0) {
            map.put("名称记录", mcjl);
        }
        if (file.getName().contains("==")) {
            // 处理biz
            map.put("biz", file.getName().split("\\.")[0]);

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

            // todo 解析简称记录
            // 解析服务支持
            pattern = Pattern.compile("var plugin_list = (\\[[.\\S\\s]*?\\]);");
            matcher = pattern.matcher(content);
            while (matcher.find()) {

                map.put("服务支持", matcher.group(1).replaceAll("\\s", "").trim());
            }
        }
        Object object = null;
        if ("info".equals(labelClass)) {
            object = new Wxxcx1(map);
        } else {
            object = new Wxgzh1(map);
        }

        System.out.println("================");
        for (String key1 : map.keySet()) {
            System.out.println(key1 + " " + map.get(key1));
        }
        System.out.println("================");
        return object;
    }

    public static String parseGzhMc() {
        Pattern pattern = Pattern.compile("id=\"js_name\">([\\w\\W]*?)<");
        File file = new File("D:\\fiddler_gen\\gzh_mc.html");
        String content = "";

        try {
            content = FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(Parser.parseGzhMc());
//        DBManager.execute(DataSource.APP_TEST_DB, SQLUtil.getSQL(Parser.parseInfo(new File("D:\\fiddler_gen\\_wxad6b99eff4edb027.html"))));
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String str = scanner.nextLine();
            System.out.println(Parser.parseInfo(new File("D:\\fiddler_gen\\" + str + ".html")));
        }
    }
}
