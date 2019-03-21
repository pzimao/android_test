package cn.edu.uestc.mythread;

import cn.edu.uestc.utils.DBUtil;
import org.apache.http.*;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadThread extends Thread {
    // 多线程之间同步
    static ConcurrentHashMap<Integer, Integer> taskMap;
    static Connection connection;
    static long maxDownloadApkFileSize;
    static long minDownloadApkFileSize;
    static String appFolder = "";
    static Logger cLogger;
    static SimpleDateFormat sdf;
    static String userAgent;

    static {
        cLogger = LogManager.getLogger("下载线程类");

        connection = DBUtil.getCon();

        taskMap = new ConcurrentHashMap();

        Properties properties = new Properties();
        InputStream is = DownloadThread.class.getClassLoader().getResourceAsStream("settings.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            cLogger.warn("读取配置文件失败");
            cLogger.error("DownloadThread类加载失败");
            System.exit(-1);
        }
        //读取配置文件
        appFolder = properties.getProperty("appFolder");
        maxDownloadApkFileSize = Long.valueOf(properties.getProperty("maxDownloadApkFileSize"));
        minDownloadApkFileSize = Long.valueOf(properties.getProperty("minDownloadApkFileSize"));
        cLogger.info("本地APK位置: " + appFolder + " 最大:" + maxDownloadApkFileSize / 1024 / 1024 + "MB 最小:" + minDownloadApkFileSize / 1024 / 1024 + "MB");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";
    }


    public int threadId = 0;
    public Logger logger;
    public String currentApp = "";
    public String percent = "";

    public DownloadThread() {
        logger = LogManager.getLogger("下载线程-" + threadId);
    }

    public DownloadThread(int threadId) {
        this.threadId = threadId;
        logger = LogManager.getLogger("下载线程-" + threadId);
    }

    @Override
    public void run() {


        CloseableHttpClient httpClient = HttpClients.custom().
                setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).
                setUserAgent(userAgent).
                setRedirectStrategy(new RedirectStrategy() {    //设置重定向处理方式

                    @Override
                    public boolean isRedirected(HttpRequest arg0,
                                                HttpResponse arg1, HttpContext arg2)
                            throws ProtocolException {

                        return false;
                    }

                    @Override
                    public HttpUriRequest getRedirect(HttpRequest arg0,
                                                      HttpResponse arg1, HttpContext arg2)
                            throws ProtocolException {

                        return null;
                    }
                }).
                build();

        HttpGet httpGet = new HttpGet();
        CloseableHttpResponse httpResponse = null;
        // 从数据库读取下载链接
        boolean taskFlag = true;
        while (taskFlag) {
            taskFlag = false;
            try {
                String sql = "select a.dl_url, a.id, a.app_name, a.jid from app_info as a left join app_dl as b on a.jid=b.jid where a.id < 25462 and (b.jid is null or b.state != '1')";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                dl:
                while (resultSet.next()) {
                    int id = resultSet.getInt(2);
                    String appName = resultSet.getString(3);
//                    if (!(appName.endsWith(".apk") || appName.endsWith(".APK"))) {
//                        appName = appName.strip() + ".apk";
//                    }
                    String url = resultSet.getString(1); // APP下载链接
                    logger.info(url);
                    int jid = resultSet.getInt(4);
                    if (taskMap.putIfAbsent(id, threadId) != null) {
                        // 其他线程正在下载这个APP
                        continue;
                    }
                    // 置标志
                    taskFlag = true;
                    // 获取开始时间
                    String startTime = sdf.format(new Date());
                    // 检查是否有未释放的资源
                    // 如果有，则释放。
                    if (httpResponse != null) {
                        httpResponse.close();
                    }

                    // 在setURI之前，检查下载地址
                    // 如果下载地址无效，直接把结果写入数据库
                    if (!(url.contains("http:") || url.contains("https:"))) {
                        updateTable(jid, url, startTime, sdf.format(new Date()), -1, "下载地址无效", "-", threadId);
                        continue;
                    }

                    // 正式开始下载
                    currentApp = appName;
                    httpGet.setURI(URI.create(url));

                    try {
                        httpResponse = httpClient.execute(httpGet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.warn("执行GET请求时出错");
                        // 更新数据库
                        updateTable(jid, url, startTime, sdf.format(new Date()), -1, "GET请求时出错", "-", threadId);
                        continue;
                    }

                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    // 判断是否重定向
                    while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
                            || statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {

                        Header[] headers = httpResponse.getHeaders("Location");
                        if (headers == null || headers.length <= 0) {
                            // 会有这种问题吗？
                            updateTable(jid, url, startTime, sdf.format(new Date()), -1, "发生了重定向, 但地址是空", "-", threadId);
                            // 继续下载其他APP
                            continue dl;
                        }
                        String redirectUrl = headers[0].getValue();

                        redirectUrl = redirectUrl.replace("[", "%5B").replace("]", "%5D");
                        logger.info("重定向的URL：" + redirectUrl);
                        httpGet.setURI(URI.create(redirectUrl));
                        // 再次请求
                        // 请求前释放上一个资源
                        httpResponse.close();
                        try {
                            httpResponse = httpClient.execute(httpGet);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.warn("执行GET请求时出错");
                            // 更新数据库
                            updateTable(jid, url, startTime, sdf.format(new Date()), -1, "状态码是103???", "-", threadId);
                            continue dl;
                        }

                        statusCode = httpResponse.getStatusLine().getStatusCode();
                    }

                    if (statusCode != HttpStatus.SC_OK) {
                        updateTable(jid, url, startTime, sdf.format(new Date()), -1, "结果状态异常, 状态码是" + statusCode, "-", threadId);
                        continue dl;
                    }

                    long apkLength = httpResponse.getEntity().getContentLength();

                    if (apkLength > maxDownloadApkFileSize || apkLength < minDownloadApkFileSize) {
                        logger.info(appName + ":" + apkLength + "字节:文件太大或太小,跳过");
                        updateTable(jid, url, startTime, sdf.format(new Date()), 2, "文件太大或太小, 跳过", "", threadId);
                        continue dl;
                    }
                    logger.info(appName + ":" + apkLength + "字节:开始下载");

                    File xml = new File(appFolder + id + "_" + appName + ".tmp");
                    FileOutputStream outputStream = new FileOutputStream(xml);

                    InputStream inputStream = httpResponse.getEntity().getContent();
                    byte buff[] = new byte[1024 * 1024];
                    int counts = 0;
                    long dSize = 0;
                    try {
                        while ((counts = inputStream.read(buff)) != -1) {
                            outputStream.write(buff, 0, counts);

                            // 生成进度条
                            this.percent = getProgress(dSize += counts, apkLength);
                        }
                        outputStream.flush();
                        outputStream.close();
                        // 改名
                        xml.renameTo(new File(appFolder + id + "_" + appName));
                    } catch (Exception e) {
                        logger.warn("下载出错");
                        xml.delete();
                        updateTable(jid, url, startTime, sdf.format(new Date()), -1, "下载时出现错误 connect reset", "", threadId);
                        httpGet.releaseConnection();
                        continue;
                    }

                    httpGet.releaseConnection();
                    logger.info(appName + ":下载完成");
                    updateTable(jid, url, startTime, sdf.format(new Date()), 1, "", appFolder + id + "_" + appName, threadId);

                    this.percent = "";
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                this.percent = "-";
                this.currentApp = "任务完成，线程已退出";
            }
        }
        try {
            httpGet.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(threadId + ":下载线程退出");
    }


    public boolean updateTable(int jid, String url, String startTime, String endTime, int state, String remark, String path, int mechineId) {
        boolean result = false;
        try {
            // 先尝试删除旧纪录
            String sql = "delete from `app_dl` where `jid` = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, jid);
            preparedStatement.execute();

            // 再插入
            sql = "INSERT INTO `app_dl` (`jid`, `url`, `start_time`, `end_time`, `state`, `remark`, `path`, `mechine_id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, jid);
            preparedStatement.setString(2, url);
            preparedStatement.setString(3, startTime);
            preparedStatement.setString(4, endTime);
            preparedStatement.setInt(5, state);
            preparedStatement.setString(6, remark);
            preparedStatement.setString(7, path);
            preparedStatement.setInt(8, mechineId);
            result = preparedStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
            this.logger.warn("更新表失败");
        } finally {
            return result;
        }
    }

    public String getProgress(long cur, long total) {
        String pStr = "▏";
        int progress = Math.round(cur * 30 / total);
        for (int i = 0; i < progress; i++) {
            pStr += "▉";
        }
//                        percent += "&#x27a4;";
        for (int i = 0; i < (30 - progress); i++) {
            pStr += " ";
        }
        pStr += "▏";
        return pStr;
    }

    public static void main(String[] args) {

        final Logger logger = LogManager.getLogger("定时任务");
        ArrayList<DownloadThread> downloadThreadList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DownloadThread downloadThread = new DownloadThread(i);
            downloadThreadList.add(downloadThread);
            downloadThread.start();
        }
        // 设置定时器，查看任务进度
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                downloadThreadList.forEach(downloadThread ->
                        logger.info("线程" + downloadThread.threadId + downloadThread.percent + ":" + downloadThread.currentApp)
                );
            }
        };
        new Timer().schedule(timerTask, 5000, 5000);


    }
}
