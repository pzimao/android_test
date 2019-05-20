package cn.edu.uestc.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecUtil {
    private static Logger logger = LogManager.getLogger("cmd执行线程");

    public static String exec(String command) {
        logger.info(command);
        String result = "";
        try {
            Process pr = Runtime.getRuntime().exec(command);

            new Thread(() -> {
                try {
                    InputStream ise = pr.getErrorStream();
                    InputStreamReader isre = new InputStreamReader(ise);
                    BufferedReader bre = new BufferedReader(isre);
                    while (bre.readLine() != null) {
                    }
                    ise.close();
                    isre.close();
                    bre.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            InputStream fis = pr.getInputStream();
            //用一个读输出流类去读
            InputStreamReader isr = new InputStreamReader(fis);
            //用缓冲器读行
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            //直到读完为止
            while ((line = br.readLine()) != null) {
                result += (line + "\n");
            }
            br.close();
            isr.close();
            fis.close();
            pr.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        result = result.trim();
        return result;
    }
}
