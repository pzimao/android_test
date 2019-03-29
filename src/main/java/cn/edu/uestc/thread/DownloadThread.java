package cn.edu.uestc.thread;

import cn.edu.uestc.utils.APKUtil;
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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadThread extends Thread {
    // 多线程之间同步
    private static ConcurrentHashMap<Integer, Integer> taskMap;
    private static long maxDownloadApkFileSize;
    private static long minDownloadApkFileSize;
    private static String appFolder = "";
    private static String userAgent;

    static {
        Logger cLogger = LogManager.getLogger("下载线程类");

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";
    }


    private int threadId;
    private Logger logger;
    private String currentApp = "";
    private String percent = "";

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
        boolean flag = true;
        while (flag) {
            flag = false;
            String sql1 = "select id, app_name, dl_url from app where dl_state != 1 and dl_url is not null and id < 184398";
            String sql2 = "update app set dl_state = ?, actual_pkg_name = ? where id = ?";
            String sql3 = "update app set dl_state = ? where id = ?";
            ResultSet resultSet = (ResultSet) DBUtil.execute(sql1);
            try {
                dl:
                while (resultSet.next()) {
                    flag = true;
                    int id = resultSet.getInt(1);
                    String appName = resultSet.getString(2);
                    String url = resultSet.getString(3); // APP下载链接
                    if (taskMap.putIfAbsent(id, threadId) != null) {
                        // 其他线程正在下载这个APP
                        continue;
                    }

                    // 检查是否有未释放的资源
                    // 如果有，则释放。
                    if (httpResponse != null) {
                        httpResponse.close();
                    }

                    // 在setURI之前，检查下载地址
                    // 如果下载地址无效，直接把结果写入数据库
                    if (!(url.contains("http:") || url.contains("https:"))) {
                        DBUtil.execute(sql3, "-1", String.valueOf(id));
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
                        DBUtil.execute(sql3, "-1", String.valueOf(id));
                        continue;
                    }

                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    // 判断是否重定向
                    while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
                            || statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {

                        Header[] headers = httpResponse.getHeaders("Location");
                        if (headers == null || headers.length <= 0) {
                            // 会有这种问题吗？
                            DBUtil.execute(sql3, "-1", String.valueOf(id));
                            // 继续下载其他APP
                            continue dl;
                        }
                        String redirectUrl = headers[0].getValue();

                        redirectUrl = redirectUrl.replace("[", "%5B").replace("]", "%5D");
//                        logger.info("重定向的URL：" + redirectUrl);
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
                            DBUtil.execute(sql3, "-1", String.valueOf(id));
                            continue dl;
                        }

                        statusCode = httpResponse.getStatusLine().getStatusCode();
                    }

                    if (statusCode != HttpStatus.SC_OK) {
                        DBUtil.execute(sql3, "-1", String.valueOf(id));
                        continue;
                    }

                    long apkLength = httpResponse.getEntity().getContentLength();

                    if (apkLength > maxDownloadApkFileSize || apkLength < minDownloadApkFileSize) {
                        logger.info(appName + ":" + apkLength + "字节:文件太大或太小,跳过");
                        DBUtil.execute(sql3, "2", String.valueOf(id));
                        continue;
                    }
                    logger.info(appName + ":" + apkLength + "字节:开始下载");

                    File xml = new File(appFolder + id);
                    FileOutputStream outputStream = new FileOutputStream(xml);

                    InputStream inputStream = httpResponse.getEntity().getContent();
                    byte[] buff = new byte[1024 * 1024];
                    int counts;
                    long dSize = 0;
                    String appPackageName = "";
                    try {
                        while ((counts = inputStream.read(buff)) != -1) {
                            outputStream.write(buff, 0, counts);
                            // 生成进度条
                            this.percent = getProgress(dSize += counts, apkLength);
                        }
                        inputStream.close();
                        outputStream.flush();
                        outputStream.close();
                        // 获取文件包名
                        appPackageName = APKUtil.getApkPackageName(xml.getAbsolutePath());
                        // 改名
                        logger.info(id + " 号文件的包名: " + appPackageName);
                        xml.renameTo(new File(appFolder + id + "_" + appName + ".apk"));
                    } catch (Exception e) {
                        logger.warn("下载出错");
                        xml.delete();
                        DBUtil.execute(sql3, "-1", String.valueOf(id));
                        httpGet.releaseConnection();
                        continue;
                    }

                    httpGet.releaseConnection();
                    logger.info(appName + ":下载完成");
                    DBUtil.execute(sql2, "1", appPackageName, String.valueOf(id));

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
                        logger.info("线程" + String.format("%2s", downloadThread.threadId) + downloadThread.percent + ":" + downloadThread.currentApp)
                );
            }
        };
        new Timer().schedule(timerTask, 5000, 5000);


    }
}
