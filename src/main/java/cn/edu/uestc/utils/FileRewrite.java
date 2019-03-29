package cn.edu.uestc.utils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRewrite {

    public static void main(String[] args) throws Exception {
        collect();
        split();
    }

    public static void collect() throws Exception {

        // 以下处理结果域名
        HashMap<String, HashSet<String>> resultMap = new HashMap<>();

        File file = new File("C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\result_3_15_下午.txt");
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        while (line != null) {
            line = line.split(".apk")[1];
            Pattern pattern = Pattern.compile("([\\w.]+)");
            Matcher matcher = pattern.matcher(line);
            String packageName = "";
            if (matcher.find()) {
                packageName = matcher.group(0);
            }
            String content = line.split(packageName)[1].trim();
            if (resultMap.containsKey(packageName)) {
                resultMap.get(packageName).add(content);
            } else {
                HashSet<String> contentSet = new HashSet<>();
                contentSet.add(content);
                resultMap.put(packageName, contentSet);
            }
            line = br.readLine();
        }

        // 处理结果域名完成

        // 以下处理包名不一致的APP
        HashSet<String> diffSet = new HashSet<>();
        file = new File("C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\包名不一致的APP.txt");
        br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        line = br.readLine();
        while (line != null) {
            diffSet.add(line.split("\t")[2]);
            line = br.readLine();
        }
        System.out.println("包名不一致的APP数量: " + diffSet.size());
        // 包名不一致的APP处理完成

        // 创建目的文件
        File resultFile = new File("C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\resultFile.txt");
        resultFile.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));

        // 下面进行重写
        File referFile = new File("C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\result-第300个以后的.txt");
        reader = new InputStreamReader(new FileInputStream(referFile));
        br = new BufferedReader(reader);
        line = br.readLine();
        int count = 0;
        while (line != null) {
            System.out.println("第 " + ++count + " 行...");
            String[] fieldArray = line.split("\t");
            String appName = fieldArray[0];
            String packageName = fieldArray[1];
            String url = fieldArray[2].trim();

            // 在这里进行处理
            if (resultMap.containsKey(packageName)) {
                // 遍历内容set
                HashSet<String> contentSet = resultMap.get(packageName);
                for (String content : contentSet) {
                    String newLine = appName + "\t" + packageName + "\t" + content + "\r\n";
                    bw.write(newLine);
                }
            } else {
                System.out.println(packageName + "\t" + diffSet.contains(packageName));
                if ("-1".equals(url)) {
                    String newLine = appName + "\t" + packageName + "\t" + "没找到下载链接" + "\r\n";
                    bw.write(newLine);
                } else if (diffSet.contains(packageName)) {
                    String newLine = appName + "\t" + packageName + "\t" + "APP包名不一致" + "\r\n";
                    bw.write(newLine);
                } else {
                    String newLine = appName + "\t" + packageName + "\t" + "没有抓到主域名" + "\r\n";
                    bw.write(newLine);
                }
            }
            line = br.readLine();
        }

        bw.flush();
        bw.close();
    }

    public static void split() throws Exception {
        File file0 = new File("C:\\Users\\pzima\\Desktop\\APP\\file\\Java_test\\android_test_demo\\src\\main\\java\\result_3_15_下午.txt");
        File file1 = new File("结果.txt");
        File file2 = new File("没抓到包.txt");
        File file3 = new File("包名不一致.txt");
        File file4 = new File("没找到下载链接.txt");

        // 创建3个文件
        file1.createNewFile();
        file2.createNewFile();
        file3.createNewFile();
        // 1个读， 3个写
        BufferedReader reader0 = new BufferedReader(new InputStreamReader(new FileInputStream(file0)));

        BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2));
        BufferedWriter bw3 = new BufferedWriter(new FileWriter(file3));
        BufferedWriter bw4 = new BufferedWriter(new FileWriter(file4));
        String line = reader0.readLine();
        while (line != null) {
            if (line.contains("包名不一致")) {
                bw3.write(line + "\n");
            } else if (line.contains("没有抓到")) {
                bw2.write(line + "\n");
            } else if (line.contains("没找到下载")) {
                bw4.write(line + "\n");
            } else {
                bw1.write(line + "\n");
            }
            line = reader0.readLine();
        }
        bw1.flush();
        bw2.flush();
        bw3.flush();
        bw4.flush();
        bw1.close();
        bw2.close();
        bw3.close();
        bw4.close();
    }
}
