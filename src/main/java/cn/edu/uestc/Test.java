package cn.edu.uestc;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;


/**
 * #1
 * HTTPS 双向认证 - direct into cacerts
 *
 * @Author Ye_Wenda
 * @Date 7/11/2017
 */
public class Test {

    public static CloseableHttpClient httpclient;
    public static final String KEY_STORE_CLIENT_PATH = "C:\\Users\\pzima\\Desktop\\android_test\\src\\main\\resources\\client.p12";
    public static final String KEY_STORE_TYPE_P12 = "PKCS12";
    private static final String KEY_STORE_PASSWORD = "soulapp123!@#1";

    // 获得池化得HttpClient
    static {
        SSLContext sslcontext = null;
        try {
            // 设置truststore
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
            InputStream ksIn = new FileInputStream(KEY_STORE_CLIENT_PATH);
            try {
                keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
            } finally {
                try {
                    ksIn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).loadKeyMaterial(keyStore, KEY_STORE_PASSWORD.toCharArray()).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 客户端支持TLSV1，TLSV2,TLSV3这三个版本
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                new String[]{"TLSv1", "TLSv2", "TLSv3"}, null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());// 客户端验证服务器身份的策略

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext)).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        httpclient = HttpClients.custom().setConnectionManager(connManager).build();

    }

    /**
     * 单向验证且服务端的证书可信
     *
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void get(String url) throws ClientProtocolException, IOException {
        // Execution context can be customized locally.
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet(url);
        // 设置请求的配置
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000).build();
        httpget.setConfig(requestConfig);

        System.out.println("executing request " + httpget.getURI());
        CloseableHttpResponse response = httpclient.execute(httpget, context);
        try {
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(EntityUtils.toString(response.getEntity()));
            System.out.println("----------------------------------------");

            // Once the request has been executed the local context can
            // be used to examine updated state and various objects affected
            // by the request execution.

            // Last executed request
            context.getRequest();
            // Execution route
            context.getHttpRoute();
            // Target auth state
            context.getTargetAuthState();
            // Proxy auth state
            context.getTargetAuthState();
            // Cookie origin
            context.getCookieOrigin();
            // Cookie spec used
            context.getCookieSpec();
            // User security token
            context.getUserToken();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        new Test().get("https://account.soulapp.cn");
    }
}