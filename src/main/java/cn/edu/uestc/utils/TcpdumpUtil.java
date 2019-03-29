package cn.edu.uestc.utils;

import cn.edu.uestc.animal.ChinazCrawler;
import com.android.chimpchat.core.IChimpDevice;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpdumpUtil extends Thread {
    private IChimpDevice device;
    private String packageName;
    final Logger logger;
    private ArrayList<Integer> appIdList = new ArrayList<>();

    public TcpdumpUtil(IChimpDevice device, String packageName) {
        logger = LogManager.getLogger("Tcpdump");
        this.device = device;
        this.packageName = packageName;
        // 把获取APP id 的过程放在了构造方法里
        String sql0 = "select id from app where actual_pkg_name = ?";
        ResultSet resultSet = (ResultSet) DBUtil.execute(sql0, packageName);
        try {
            if (resultSet.next()) {
                // 根据包名拿到app id
                int appId = resultSet.getInt(1);
                logger.info("获取到 " + packageName + " 的app id: " + appId);
                appIdList.add(appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appIdList.size() == 0) {
            logger.warn("没有找到包名 " + packageName + " 对应的APP id");
        }
    }

    @Override
    public void run() {
        // 抓取DNS请求和相应包
        String[] packetArray = capturePacket();

        ArrayList<String> requestPacketList = new ArrayList<>();
        ArrayList<String> responsePacketList = new ArrayList<>();

        for (String packet : packetArray) {
            if (packet.contains("A?")) {
                // 请求包
                requestPacketList.add(packet);
            } else {
                // 响应包
                responsePacketList.add(packet);
            }
        }
        // 对抓取的DNS包进行解析
        LinkedHashMap<Integer, String> requestMap = parseRequestPacket(requestPacketList);
        HashMap<Integer, ArrayList<ArrayList<String>>> responseMap = parseResponsePacket(responsePacketList);

        // 存储所有内容
        saveDomain(requestMap, responseMap);
    }

    // 捕获请求包和响应包
    public String[] capturePacket() {
        logger.info(packageName + ":启动抓包线程");
        String packageContent = device.shell("/data/local/tcpdump -i any port 53 -s 0");
        String[] packetArray = packageContent.split("\n");
        logger.info(packageName + ":抓到的包数量: " + packetArray.length);
        return packetArray;
    }

    /*
     解析请求包 这里不做去重。因为我认为同一个域名，请求序列号不一样时，得到的响应也可能不一样。
     */
    public LinkedHashMap<Integer, String> parseRequestPacket(ArrayList<String> requestPacketArray) {
        // 用来存储请求序号和请求域名
        LinkedHashMap<Integer, String> requestMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(\\d+)\\+\\sA\\?\\s(([0-9a-z-]+\\.)+[0-9a-z-]+)\\.");
        for (String requestPacket : requestPacketArray) {
            Matcher matcher = pattern.matcher(requestPacket);
            if (matcher.find()) {
                int sequenceNumber = Integer.valueOf(matcher.group(1));
                String domain = matcher.group(2);

                requestMap.put(sequenceNumber, domain);
            }
        }
        return requestMap;
    }

    // 解析响应包
    public HashMap<Integer, ArrayList<ArrayList<String>>> parseResponsePacket(ArrayList<String> responsePacketArray) {
        Pattern patternSequence = Pattern.compile(":\\s(\\d+)");
        Pattern patternCname = Pattern.compile("CNAME\\s(([0-9a-z-]+\\.)+[0-9a-z-]+)\\.");
        Pattern patternA = Pattern.compile("\\sA\\s((\\d+\\.)+\\d+)");
        HashMap<Integer, ArrayList<ArrayList<String>>> responseMap = new HashMap<>();
        for (String responsePacket : responsePacketArray) {
            Matcher matcher = patternSequence.matcher(responsePacket);
            if (!matcher.find()) {
                // 未匹配到序列号
                continue;
            }
            Integer sequenceNumber = Integer.valueOf(matcher.group(1));
            ArrayList<String> cnameList = new ArrayList<>();
            matcher = patternCname.matcher(responsePacket);
            while (matcher.find()) {
                cnameList.add(matcher.group(1));
            }
            ArrayList<String> aList = new ArrayList<>();
            matcher = patternA.matcher(responsePacket);
            while (matcher.find()) {
                aList.add(matcher.group(1));
            }
            ArrayList<ArrayList<String>> valueList = new ArrayList<>();
            valueList.add(cnameList);
            valueList.add(aList);
            responseMap.put(sequenceNumber, valueList);
        }
        return responseMap;
    }


    public void saveDomain(LinkedHashMap<Integer, String> requestMap, HashMap<Integer, ArrayList<ArrayList<String>>> responseMap) {
        // 存储app-域名信息
        HashSet<String> domainSet = saveAppDomain(requestMap);

        String queryDomainCnameSql = "select * from domain_cname where domain = ? and cname = ? ";
        String insertDomainCnameSql = "insert into domain_cname(domain, cname) values (?, ?)";
        String queryDomainIpSql = "select * from domain_ip where domain = ? and ip = ?";
        String insertDomainIpSql = "insert into domain_ip(domain, ip) values (?, ?)";
        // 应该遍历responseMap
        for (Integer sequenceNumber : responseMap.keySet()) {
            String requestDomain = requestMap.get(sequenceNumber);
            if (domainSet.contains(requestDomain)) {
                ArrayList<ArrayList<String>> valueList = responseMap.get(sequenceNumber);
                ArrayList<String> cnameList = valueList.get(0);
                ArrayList<String> aList = valueList.get(1);

                try {
                    // 先保存到domain_cname表
                    for (String cname : cnameList) {
                        // 保存到domain_cname 表
                        ResultSet resultSet = (ResultSet) DBUtil.execute(queryDomainCnameSql, requestDomain, cname);
                        if (!resultSet.next()) {
                            // 如果没有这样的记录，就插入
                            DBUtil.execute(insertDomainCnameSql, requestDomain, cname);
                        }
                    }

                    // 再保存到domain_cname表
                    for (String a : aList) {
                        // 保存到domain_ip 表
                        ResultSet resultSet = (ResultSet) DBUtil.execute(queryDomainIpSql, requestDomain, a);
                        if (!resultSet.next()) {
                            // 如果没有这样的记录，就插入
                            DBUtil.execute(insertDomainIpSql, requestDomain, a);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 保存请求域名，域名去重操作也在这里
    // 返回的是保存到domain表的域名
    public HashSet<String> saveAppDomain(LinkedHashMap<Integer, String> requestMap) {
        // 检查域名是否已存在
        String sql1 = "select * from domain where domain = ?";
        // 插入域名
        String sql2 = "INSERT INTO `domain` (`domain`, `domain_desc`) VALUES (?, ?)";
        // 插入APP 域名 对应关系
        String sql3 = "insert into app_domain(app_id, domain) values(? ,?)";
        // 检查app 与域名对应关系是否已存在
        String sql4 = "select * from app_domain where app_id = ? and domain = ?";
        // 用来去重的集合
        LinkedHashSet<String> domainSet = new LinkedHashSet<>();
        for (Integer sequenceNumber : requestMap.keySet()) {
            String domain = requestMap.get(sequenceNumber);
            domainSet.add(domain);
        }
        /*
           解决 sc-troy.com的问题.
            比如 07:10:34.446212 IP 10.0.3.15.43106 > 61.139.2.69.53: 34427+ A? mysuper.ccnew.com.sc-troy.com. (47)
         */
        HashSet<String> excludeDomainSet = new HashSet<>();
        for (String srcDomain : domainSet) {
            for (String targetDomain : domainSet) {
                if (srcDomain.length() > targetDomain.length()) {
                    // 此时srcDomain 可能是 targetDomain的错误形式
                    if (srcDomain.startsWith(targetDomain + ".")) {
                        // targetDomain 是加了后缀的域名，剔除
                        excludeDomainSet.add(srcDomain);
                    }
                }
            }
        }
        for (String excludeDomain : excludeDomainSet) {
            domainSet.remove(excludeDomain);
            logger.info("剔除域名: " + excludeDomain);
        }

        // 更新app域名表
        for (String domain : domainSet) {
            logger.info(packageName + " 查询域名 " + "\t" + domain);
            // 检查domain表
            ResultSet resultSet = (ResultSet) DBUtil.execute(sql1, domain);
            try {
                if (!resultSet.next()) {
                    // domain表不包含这个域名，则添加
                    // 先爬取域名备案
                    String domain_desc = ChinazCrawler.getNameByDomain(domain);
                    DBUtil.execute(sql2, domain, domain_desc);
                    logger.info("发现新域名 " + domain + " " + domain_desc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 对每个app id 保存一遍
            for (Integer appId : appIdList) {
                // 先检查app与域名对应是否已存在
                try {
                    if (!((ResultSet) DBUtil.execute(sql4, String.valueOf(appId), domain)).next()) {
                        DBUtil.execute(sql3, String.valueOf(appId), domain);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return domainSet;
    }
}
