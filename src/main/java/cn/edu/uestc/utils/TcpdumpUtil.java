package cn.edu.uestc.utils;

import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpdumpUtil extends Thread {
    private IChimpDevice device;
    private String packageName;

    public TcpdumpUtil(IChimpDevice device, String packageName) {
        this.device = device;
        this.packageName = packageName;
    }

    @Override
    public void run() {
        final Logger logger = LogManager.getLogger(packageName + " tcpdump");
        try {
            logger.info(packageName + ":启动抓包线程");
            String packageContent = device.shell("/data/local/tcpdump -i any port 53 -s 0");

            String[] pkgs = packageContent.split("\n");
            logger.info(packageName + ":抓到的包的数量: " + pkgs.length);

            LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();
            for (String pkg : pkgs) {
                // 解析抓到的数据：
                // 1. 判断DNS包类型
                // 2. 解析出域名和IP
                logger.debug("包内容: " + pkg);
                String content = "";

                if (pkg.contains(".53 > ")) {
                    // 响应,忽略
                    continue;
                }
                Pattern pattern = Pattern.compile("A\\?\\s(([0-9a-z-]+\\.)+[0-9a-z-]+\\.?)");
                Matcher matcher1 = pattern.matcher(pkg);
                while (matcher1.find()) {
                    content += matcher1.group(1);
                    if (content.endsWith(".")) {
                        content = content.substring(0, content.length() - 1);
                    }

                    if ("".equals(content)) {
                        continue;
                    }

                    if (!resultMap.containsKey(content)) {
                        resultMap.put(content, 0);
                    }
                    // 该域名计数 +1
                    resultMap.replace(content, resultMap.get(content) + 1);
                    logger.info(packageName + ":解析得到:" + content);
                }
            }
            // 获取到APP id
            String sql0 = "select id from app where actual_pkg_name = ?";
            ResultSet resultSet = (ResultSet) DBUtil.execute(sql0, packageName);
            int appId = 0;
            if (resultSet.next()) {
                appId = resultSet.getInt(1);
            }
            // 更新domain表和app_domain表
            String sql1 = "INSERT INTO `domain` (`domain`) VALUES (?)";
            String sql2 = "insert into app_domain(app_id, domain, count) values(? ,? ,?)";
            for (String content : resultMap.keySet()) {
                DBUtil.execute(sql1, content);
                DBUtil.execute(sql2, String.valueOf(appId), content, String.valueOf(resultMap.get(content)));
            }
            logger.info(packageName + ":一共解析到 " + resultMap.size() + " 个域名,已保存");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
