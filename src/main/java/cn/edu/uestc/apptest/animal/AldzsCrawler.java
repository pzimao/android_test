package cn.edu.uestc.apptest.animal;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AldzsCrawler {
    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpPost httpPost = new HttpPost();

    static {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3724.8 Safari/537.36");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Origin", "http://www.aldzs.com");
        headers.put("Referer", "http://www.aldzs.com/toplist");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3724.8 Safari/537.36");
        httpPost.addHeader("Accept", "application/json, text/plain, */*");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Origin", "http://www.aldzs.com");
        httpPost.addHeader("Referer", "http://www.aldzs.com/toplist");
    }

    private static Logger logger = LogManager.getLogger("AldzsCrawler");

    public String getContent(List<NameValuePair> params) {
        String baseUrl = "https://zhishuapi.aldwx.com/Main/action/Dashboard/Homepage/data_list";

        httpPost.setURI(URI.create(baseUrl));
        try {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(uefEntity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            return unicodeToString(result);

        } catch (UnknownHostException e) {
            logger.warn("请检查主机网络状况");
        } catch (Exception e) {
//            e.printStackTrace();
            httpPost.reset();
        }
        return "{}";
    }


    public LinkedList<String[]> parseContent(String content) {
        System.out.println(content);
        LinkedList<String[]> list = new LinkedList<>();
        // 解析json
        JSONObject jsonObject = new JSONObject(content);
        JSONArray wxxcxArray = jsonObject.getJSONArray("data");
        // 遍历小程序数组
        for (int i = 0; i < wxxcxArray.length(); i++) {
            String[] wxxcxInfo = new String[5];
            JSONObject wxxcx = wxxcxArray.getJSONObject(i);
            String id = wxxcx.getString("id");
            String appkey = wxxcx.getString("appkey");
            String name = wxxcx.getString("name");
            String category = wxxcx.getString("category");
            String desc = wxxcx.getString("desc");
            wxxcxInfo[0] = id;
            wxxcxInfo[1] = appkey;
            wxxcxInfo[2] = name;
            wxxcxInfo[3] = category;
            wxxcxInfo[4] = desc;
            list.add(wxxcxInfo);
        }
        return list;
    }

    HashMap<String, String[]> map = new HashMap<>(4096);
    public void saveToDB(LinkedList<String[]> list) {
        for (String[] array : list) {
            map.putIfAbsent(array[0], array);
            for (String item : array) {
                System.out.print(item);

            }
            System.out.println();
        }
        System.out.println();
        return;
    }

    private AldzsCrawler() {
    }

    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");

        Matcher matcher = pattern.matcher(str);

        char ch;

        while (matcher.find()) {

            ch = (char) Integer.parseInt(matcher.group(2), 16);

            str = str.replace(matcher.group(1), ch + "");

        }

        return str;

    }

    public LinkedList<List<NameValuePair>> getParams() {
        LinkedList<List<NameValuePair>> paramList = new LinkedList<>();


        for (int i = 0; i < 2; i++) { // 选榜
            for (int j = 0; j < 30; j++) { // 选类别
                for (int k = 0; k < 2; k++) { // 选时间


                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("type", String.valueOf(i)));
                    list.add(new BasicNameValuePair("typeid", String.valueOf(j)));
                    list.add(new BasicNameValuePair("date", String.valueOf(k)));
                    list.add(new BasicNameValuePair("size", String.valueOf(30)));
                    list.add(new BasicNameValuePair("token", ""));
                    paramList.add(list);
                }
            }
        }

        return paramList;
    }

    public void crawl() {
        for (List<NameValuePair> params : getParams()) {
            String content = getContent(params);
            LinkedList<String[]> list = parseContent(content);
            saveToDB(list);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        AldzsCrawler crawler = new AldzsCrawler();
        crawler.crawl();
        String sql = "insert into wxxcx values (?, ?, ?, ?, ?, 0, 0)";
        for (String id : crawler.map.keySet()) {
            String[] array = crawler.map.get(id);
            DBManager.execute(DataSource.APP_TEST_DB, sql, array[0], array[2], array[1], array[3], array[4]);
        }
    }
}
