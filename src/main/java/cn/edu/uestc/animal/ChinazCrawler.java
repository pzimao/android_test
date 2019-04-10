package cn.edu.uestc.animal;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChinazCrawler {
    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpGet httpGet = new HttpGet();
    private static Pattern pattern = Pattern.compile("网站名称</span><p>(.*)</p></li>");
    private static String baseUrl = "http://icp.chinaz.com/";
    private static Logger logger = LogManager.getLogger("ChinazCrawler");

    public static String getNameByDomain(String domain) {
        httpGet.setURI(URI.create(baseUrl + domain));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            Matcher matcher = pattern.matcher(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            while (matcher.find()) {
                return matcher.group(1);
            }
        } catch (UnknownHostException e) {
            logger.warn("请检查主机网络状况");
        } catch (Exception e) {
//            e.printStackTrace();
            httpGet.reset();
        }
        return "未知";
    }

    private ChinazCrawler() {
    }

    public static void main(String[] args) throws Exception {
        ChinazCrawler chinazCrawler = new ChinazCrawler();
        String querySql = "select domain from domain where domain_desc is null";
        String updateSql = "update domain set domain_desc= ? where domain = ?";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, querySql);
        while (resultSet.next()) {
            // 域名
            String domain = resultSet.getString(1);
            // 爬到的中文名字
            String name = chinazCrawler.getNameByDomain(domain);
            DBManager.execute(DataSource.APP_TEST_DB, updateSql, name, domain);
            System.out.println(domain + "\t" + name);
        }
    }
}
