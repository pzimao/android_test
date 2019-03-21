package cn.edu.uestc.mythread;

import cn.edu.uestc.utils.DBUtil;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpdumpThread extends Thread {
    public IChimpDevice device;
    public String packageName;
    public Connection connection;

    public TcpdumpThread(IChimpDevice device, String packageName) {
        this.device = device;
        this.packageName = packageName;
        this.connection = DBUtil.getCon();
    }

    @Override
    public void run() {
        final Logger logger = LogManager.getLogger(packageName + " tcpdump");
        try {
            logger.info(packageName + ":启动抓包线程");
            String packageContent = device.shell("/data/local/tcpdump -i any port 53 -s 0");

            String pkgs[] = packageContent.split("\n");
            logger.info(packageName + ":抓到的包的数量: " + pkgs.length);
            String sql = "INSERT INTO `app_domain` (`package_name`, `type`, `content`, `count`) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            LinkedHashMap<String, Integer> resultMap = new LinkedHashMap<>();
            for (String pkg : pkgs) {
                // 解析抓到的数据：
                // 1. 判断DNS包类型
                // 2. 解析出域名和IP
                logger.debug("包内容: " + pkg);
                String type = "";
                String content = "";
                Pattern pattern;
                if (pkg.contains(".53 > ")) {
                    // 响应
                    type = "response";
                    pattern = Pattern.compile("[(CNAME)|A]\\s(([0-9a-z-]+\\.)+[0-9a-z-]+\\.?)");
                    // TODO 这里抓到DNS的解析结果了，后续处理
                    continue;
                } else {
                    // 请求
                    type = "request";
                    pattern = Pattern.compile("A\\?\\s(([0-9a-z-]+\\.)+[0-9a-z-]+\\.?)");
                }
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
            // 3. 更新数据库表
            for (String content : resultMap.keySet()) {
                preparedStatement.setString(1, packageName);
                preparedStatement.setString(2, "request");
                preparedStatement.setString(3, content);
                preparedStatement.setInt(4, resultMap.get(content));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            logger.info(packageName + ":一共解析到 " + resultMap.size()+ " 个域名保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
