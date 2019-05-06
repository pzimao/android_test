package cn.edu.uestc;

import cn.edu.uestc.utils.DBManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提取邮件地址
 *
 * @author Winter Lau
 * @date 2010-6-14 下午04:56:15
 */
public class AppIdExtractor {

    private final static Pattern appIdPattern = Pattern.compile("appId:\"(wx[a-f0-9]{16})");
//    private final static Pattern appIdPattern = Pattern.compile("navigateToMiniProgram\",\\{appId:\"(wx[a-f0-9]{16})");


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        String sql = "select wxapkg_name, path from wxapkg where extappid is null";
        String updateSql = "update wxapkg set extappid = ? where wxapkg_name = ?";
        ResultSet resultset = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        while (resultset.next()) {
            String wxapkgName = resultset.getString(1);
            String path = resultset.getString(2);
            String appId = "";
            String txt = FileUtils.readFileToString(new File(path), Charset.defaultCharset());
            Matcher matchr = appIdPattern.matcher(txt);
            while (matchr.find()) {
                appId += matchr.group(1) + ' ';
                System.out.println(matchr.group(1));
            }
            // 更新数据库表
            DBManager.execute(DataSource.APP_TEST_DB, updateSql, appId, wxapkgName);
        }
    }
}