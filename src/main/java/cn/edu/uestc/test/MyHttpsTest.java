package cn.edu.uestc.test;


import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class MyHttpsTest {
    public static String pwd = "soulapp123!@#1";

    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        keyStore.load(MyHttpsTest.class.getResourceAsStream("client.p12"), pwd.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, pwd.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // todo 处理服务器证书信任
        X509TrustManager[] trustManagers = new X509TrustManager[1];
        trustManagers[0] = null;
        Security.addProvider(new BouncyCastleProvider());
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");

        InputStream r4 = new FileInputStream("C:\\Users\\pzima\\Desktop\\android_test\\src\\main\\resources\\client.crt");
        Certificate certificate = certificateFactory.generateCertificate(r4);
        r4.close();
        trustManagers[0] = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                for (X509Certificate x509Certificate : x509Certificates) {
                    x509Certificate.checkValidity();
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };


        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        sslContext.getClientSessionContext().setSessionTimeout(15000);
        sslContext.getServerSessionContext().setSessionTimeout(15000);


        String result = null;
        HttpURLConnection urlConnection = null;
        try {
            URL requestedUrl = new URL("https://account.soulapp.cn");
            urlConnection = (HttpURLConnection) requestedUrl.openConnection();
            if (urlConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(1500);
            urlConnection.setReadTimeout(1500);
//            lastResponseCode = urlConnection.getResponseCode();
            result = IOUtils.toString(urlConnection.getInputStream());
//            lastContentType = urlConnection.getContentType();
        } catch (Exception ex) {
            result = ex.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        System.out.println(result);
    }
}
