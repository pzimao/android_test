package cn.edu.uestc.animal;

import cn.edu.uestc.utils.DBUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChinazCrawler {
    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpGet httpGet = new HttpGet();
    private static Pattern pattern = Pattern.compile("网站名称</span><p>(.*)</p></li>");
    private static String baseUrl = "http://icp.chinaz.com/";


    public static String getNameByDomain(String domain) {
        httpGet.setURI(URI.create(baseUrl + domain));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            Matcher matcher = pattern.matcher(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            while (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpGet.reset();
        }
        return "未知";
    }

    private ChinazCrawler() {
    }

    public static void main(String[] args) throws Exception {
        ChinazCrawler chinazCrawler = new ChinazCrawler();
        Connection connection = DBUtil.getCon();
        String querySql = "select domain from domain where domain_desc is null";
        String updateSql = "update domain set domain_desc= ? where domain = ?";
        ResultSet resultSet = (ResultSet) DBUtil.execute(querySql);
        while (resultSet.next()) {
            // 域名
            String domain = resultSet.getString(1);
            // 爬到的中文名字
            String name = chinazCrawler.getNameByDomain(domain);
            DBUtil.execute(updateSql, name, domain);
            System.out.println(domain + "\t" + name);
        }
    }
}
