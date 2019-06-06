package cn.edu.uestc.wechat;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import cn.edu.uestc.wechat.impl.WechatMessageClicker;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTransfer {
    private Logger logger = LogManager.getLogger("公众号名称获取程序");
    File idFileFolder;
    HttpClient httpClient;
    HttpGet httpGet;


    public DataTransfer(String folderPath) {
        idFileFolder = new File(folderPath);
        httpClient = HttpClients.createDefault();
        httpGet = new HttpGet();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000).build();
        httpGet.setConfig(requestConfig);
    }

    /**
     * 把原始biz文件的biz存到数据库表
     */
    public void bizToDb() {
        // 尝试读取cur文件，这个文件记录了开始
        HashSet<String> idSet = new HashSet<>(100000);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idFileFolder, "biz.txt"))));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.substring(1, 17);
                if (idSet.add(line)) {
                    DBManager.execute(DataSource.APP_TEST_DB, "insert into wxgzh1 (biz) values (?)", line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从原始文件获取公众号和文章链接的对应关系，做成一个map
     *
     * @return
     */
    public HashMap<String, LinkedList<String>> getGzhWzMap() {
        // 获取到公众号id和文章链接的对应map
        HashMap<String, LinkedList<String>> map = new HashMap<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idFileFolder, "biz.txt"))));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String biz = line.substring(1, 17);
                String value = line.substring(line.indexOf("_"), line.lastIndexOf("\""));
                LinkedList<String> valueList;
                if (map.containsKey(biz)) {
                    valueList = map.get(biz);
                } else {
                    valueList = new LinkedList<>();
                    map.put(biz, valueList);
                }
                valueList.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(map.size());
        return map;
    }


    /**
     * 从原始文件获取公众号和文章链接的对应关系，做成一个map
     *
     * @return
     */
    public HashMap<String, HashSet<String>> getGzhWzMap2() {
        // 获取到公众号id和文章链接的对应map
        HashMap<String, HashSet<String>> map = new HashMap<>();


        File[] fileArray = idFileFolder.listFiles(file -> file.getName().endsWith(".sql"));
        for (File file : fileArray) {

            try {
                while (System.currentTimeMillis() - file.lastModified() < 10000) {
                    System.out.println(System.currentTimeMillis() - file.lastModified());
                    logger.info("等待" + file.getName() + "完全拷贝");
                    Thread.sleep(3000);
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String biz = line.substring(line.indexOf('\'') + 1, line.indexOf('\'') + 17);
                    String value = line.substring(line.indexOf("__biz"), line.lastIndexOf('\''));
                    HashSet<String> valueList;
                    if (map.containsKey(biz)) {
                        valueList = map.get(biz);
                    } else {
                        valueList = new HashSet<>();
                        map.put(biz, valueList);
                    }
                    valueList.add(value);
                }
                bufferedReader.close();
                file.delete();
            } catch (FileNotFoundException e) {
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fileArray.length > 0) {
            logger.info("添加 biz " + map.size() + "条");
        }
        return map;
    }

    /**
     * 生成格式化的公众号文本文件
     * 格式是：biz 文章链接 文章链接...
     *
     * @param map
     */
    public void writeFormattedFile(HashMap<String, HashSet<String>> map) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(idFileFolder, "biz_formatted.txt"), true)))) {

            for (String biz : map.keySet()) {
                HashSet<String> wzList = map.get(biz);
                // 构造行
                stringBuilder.append(biz);
                for (String wz : wzList) {
                    stringBuilder.append("\t")
                            .append(wz);
                }
                stringBuilder.append("\n");
                // 把行写入文件
                out.write(stringBuilder.toString());
                // 清空构造器
                stringBuilder.delete(0, stringBuilder.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 读格式化的biz文件，获取下一个公众号ID及其文章链接
     *
     * @param
     */
    public String[] getNextGzh() {
        // 尝试读取cur文件，这个文件记录了开始
        File curFile;
        int cur;
        File[] curFiles = idFileFolder.listFiles(file -> !file.getName().contains("."));
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(idFileFolder, "biz_formatted.txt"))))) {
            if (curFiles.length != 0) {
                curFile = curFiles[0];
            } else {
                curFile = new File(idFileFolder, "0");
                curFile.createNewFile();
            }
            String line;
            cur = Integer.valueOf(curFile.getName());
            for (int i = 0; i < cur; i++) {
                if (bufferedReader.readLine() == null) {
                    return null;
                }
            }

            if ((line = bufferedReader.readLine()) != null) {
                cur++;
                File newFile = new File(idFileFolder, String.valueOf(cur));
                curFile.renameTo(newFile);
                logger.info("当前第" + (cur - 1) + "个公众号, 更新行记录到" + cur);
                return line.split("\t");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 爬取公众号名称
     *
     * @param array
     * @return
     */
    public String crawlGzhMc(String[] array, boolean checkTable) {
        if (checkTable) {
            // 爬之前先检查表
            String sql = "select mc from wxgzh1 where biz = ?";
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql, array[0]);
            try {
                if (resultSet.next()) {
                    String mc = resultSet.getString(1);
                    if (mc != null && !"".equals(mc)) {
                        return mc;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Pattern pattern = Pattern.compile("id=\\\"js_name\\\">([\\s\\S]*?)<\\/a>");
        try {
            // 爬map中的每项内容
            for (int i = 1; i < array.length; i++) {
                String wz = array[i];
                logger.info("https://mp.weixin.qq.com/s?" + wz);
                httpGet.setURI(new URI("https://mp.weixin.qq.com/s?" + wz.trim()));
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        String mc = matcher.group(1).trim();
                        if (mc.length() > 0) {
                            // 2019-6-4添加爬取微信号
                            Pattern wxhPattern = Pattern.compile("微信号<\\/label>\\s+<span.*?>(.+)<\\/span>");
                            Matcher wxhMatcher = wxhPattern.matcher(content);
                            while (wxhMatcher.find()) {
                                logger.info("发现微信号");
                                return mc + ";" + wxhMatcher.group(1);
                            }
                            return mc;
                        }
                    }
                } else {
                    logger.info("状态码：" + response.getStatusLine().getStatusCode());
                    try {
                        // 状态码不对的话就休息10秒
                        Thread.sleep(10000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 把公众号名称更新到数据库表
     *
     * @param biz
     * @param gzhMc
     */
    public boolean saveGzhMc(String biz, String gzhMc) {

        // 先查表中是否存在biz的记录，如果存在，用update，否则用insert
        String querySql = "select * from wxgzh1 where biz = ?";
        String executeSql = "";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, querySql, biz);
        try {
            if (resultSet.next()) {
                if (gzhMc == "") {
                    logger.info("公众号名称是空\t" + biz);
                    return false;
                }
                executeSql = "update wxgzh1 set mc = ? where biz = ?";
                logger.info("更新\t" + gzhMc + "\t" + biz);
            } else {
                executeSql = "insert into wxgzh1 (mc, biz) values (?, ?)";
                logger.info("插入\t" + gzhMc + "\t" + biz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBManager.execute(DataSource.APP_TEST_DB, executeSql, gzhMc, biz);
        return true;
    }

    public String clickGzhMc(String[] array) {
        WechatMessageClicker wechatMessageClicker = new WechatMessageClicker(idFileFolder.getPath());
        File mcFile = new File(wechatMessageClicker.getIdFileFolder(), "gzh_mc.html");
        if (mcFile.exists()) {
            mcFile.delete();
        }
        Pattern mcPattern = Pattern.compile("id=\\\"js_name\\\">([\\s\\S]*?)<\\/a>");

        try {
            // 爬map中的每项内容
            for (int i = 1; i < array.length; i++) {
                String wz = array[i];
                logger.info("https://mp.weixin.qq.com/s?" + wz);
                wechatMessageClicker.sendMessage("https://mp.weixin.qq.com/s?" + wz);

                wechatMessageClicker.clickMessage2();
                Thread.sleep(20000);
                if (!mcFile.exists()) {
                    continue;
                }
                String content = FileUtils.readFileToString(mcFile, Charset.defaultCharset());
                Matcher matcher = mcPattern.matcher(content);
                if (matcher.find()) {
                    String mc = matcher.group(1).trim();
                    if (mc.length() > 0) {
                        // 2019-6-4添加爬取微信号
                        Pattern wxhPattern = Pattern.compile("微信号<\\/label>\\s+<span.*?>(.+)<\\/span>");
                        Matcher wxhMatcher = wxhPattern.matcher(content);
                        while (wxhMatcher.find()) {
                            logger.info("发现微信号");
                            return mc + ";" + wxhMatcher.group(1);
                        }
                        return mc;
                    }
                }
                logger.info("页面错误，换一个页面");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        DataTransfer dataTransfer = new DataTransfer("D:\\fiddler_gen"); // 存放*.sql文件的文件夹路径
        while (true) {
            HashMap<String, HashSet<String>> map = dataTransfer.getGzhWzMap2(); // 从文件夹下的*.sql文件中读原始记录，组成Map，每次读到的一批*.sql中的biz去了重
            dataTransfer.writeFormattedFile(map); // 把Map的内容追加写到biz_formatted.txt

            String[] array = dataTransfer.getNextGzh(); // 拿到一条要处理的公众号biz和它的文章链接列表
            if (array == null) {
                dataTransfer.logger.info("没有biz了...");
                try {
                    Thread.sleep(10 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
            // 爬名称
            String mc = dataTransfer.crawlGzhMc(array, true); // 使用httpClient发起请求获取公众号页面，第2个参数表示在请求前是否查表，如果是true表中已有，就不爬了(此时认为公众号名称不会改变)
            // String mc = dataTransfer.clickGzhMc(array); // 通过模拟点击的方式获取公众号页面，页面保存在文件夹下的"gzh_mc.html"


            dataTransfer.saveGzhMc(array[0], mc); // 插入或者更新数据库wxgzh1表
            // 每次请求间隔时间
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
