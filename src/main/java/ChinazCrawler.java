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
    private HttpClient httpClient;
    private HttpGet httpGet;
    private Pattern pattern;
    private String baseUrl;

    public ChinazCrawler() {
        httpClient = HttpClients.createMinimal();
        httpGet = new HttpGet();
        pattern = Pattern.compile("网站名称</span><p>(.*)</p></li>");
        baseUrl = "http://icp.chinaz.com/";
    }

    public String getNameByDomain(String domain) {
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

    public static void main(String[] args) throws Exception {
        ChinazCrawler chinazCrawler = new ChinazCrawler();
        Connection connection = DBUtil.getCon();
        String querySql = "select DISTINCT(content) from app_domain where type='request'";
        String updateSql = "update app_domain set type= ? where content = ?";
        PreparedStatement queryStatement = connection.prepareStatement(querySql);
        PreparedStatement updateStatement = connection.prepareStatement(updateSql);
        ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            // 域名
            String domain = resultSet.getString(1);
            // 爬到的中文名字
            String name = chinazCrawler.getNameByDomain(domain);
            updateStatement.setString(1, name);
            updateStatement.setString(2, domain);
            updateStatement.execute();
            System.out.println(domain + "\t" + name);
        }
    }
}
