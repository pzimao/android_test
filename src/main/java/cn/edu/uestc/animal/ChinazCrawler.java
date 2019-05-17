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
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChinazCrawler {
    private static HttpClient httpClient = HttpClients.createMinimal();
    private static HttpGet httpGet = new HttpGet();
    private static Logger logger = LogManager.getLogger("ChinazCrawler");

    public static String getNameByDomain(String domain) {
        String result = getNameByDomain2(domain);
        if ("未知".equals(result)) {
            result = getNameByDomain1(domain);
        }
        return result;
    }

    public static String getNameByDomain1(String domain) {
        String baseUrl = "http://icp.chinaz.com/";
        Pattern pattern = Pattern.compile("网站名称</span><p>(.*)</p></li>");
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

    public static String getNameByDomain2(String domain) {
        String baseUrl = "https://www.beian.org/website/";
        Pattern pattern = Pattern.compile("网站名称<\\/li>\\s+<li>(.*)<\\/li>");
        httpGet.setURI(URI.create(baseUrl + domain));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
//            System.out.println(EntityUtils.toString(entity, StandardCharsets.UTF_8));
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

    public static String getAreaByIp(String ip) {
        Pattern pattern = Pattern.compile("Whwtdhalf w50-0\">(.*)</span>");
        String baseUrl = "http://ip.tool.chinaz.com/";
        httpGet.setURI(URI.create(baseUrl + ip));
        String area = "";
        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            Matcher matcher = pattern.matcher(EntityUtils.toString(entity, StandardCharsets.UTF_8));

            while (matcher.find()) {
                area = matcher.group(1);
            }
        } catch (UnknownHostException e) {
            logger.warn("请检查主机网络状况");
        } catch (Exception e) {
//            e.printStackTrace();
            httpGet.reset();
        }
        return area;
    }

    private ChinazCrawler() {
    }

    public void updateTable1() throws Exception {
        ChinazCrawler chinazCrawler = new ChinazCrawler();
        String querySql = "select domain from domain where domain_desc = '未知' order by domain DESC";
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

    public void updateTable2() throws Exception {
//        String sql = "select app_domain.domain, domain_ip.ip from app_domain inner join domain_ip on app_domain.domain = domain_ip.domain where app_domain.app_id = 147890 and area is null";
        String sql = "select ip from ip";
        String sql2 = "select area from domain_ip where ip = ?";
//        String sql = "select DISTINCT(domain_ip.ip) from app INNER JOIN app_domain on app.id = app_domain.app_id INNER JOIN domain on app_domain.domain = domain.domain INNER JOIN domain_ip on domain_ip.domain = domain.domain where app.app_name = '知到' and domain_ip.area is null";
        String updateSql = "update ip set area = ? where ip = ?";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        while (resultSet.next()) {
            String ip = resultSet.getString(1);
            ResultSet resultSet1 = (ResultSet) DBManager.execute(DataSource.CRAWLER_DB, sql2, ip);
            while (resultSet1.next()) {
                String area = resultSet1.getString(1);
                if (area == null || area.equals("")) {
                    area = getAreaByIp(ip);
                }
                DBManager.execute(DataSource.APP_TEST_DB, updateSql, area, ip);
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new ChinazCrawler().updateTable2();
//        System.out.println(getNameByDomain2("zzkj.rubaoo.com").trim());
    }
}
