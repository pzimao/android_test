package cn.edu.uestc.test;

import cn.edu.uestc.utils.ExecUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkDump {


    Pattern domainPattern = Pattern.compile("(https?:\\/\\/(\\w+\\.)+\\w+)");
    Pattern permissionPattern = Pattern.compile("(([\\w_]+\\.)+permission(\\.[\\w_]+)+)");
    Logger logger = LogManager.getLogger("APK解析");
    LinkedList<String> contentList;


    private File decompile(File apkFile) {
        logger.info("执行反编译...");
        File jarFile = new File(apkFile.getParentFile(), apkFile.getName().substring(0, apkFile.getName().lastIndexOf('.')) + ".jar");
        if (jarFile.exists()) { // 如果目标jar已存在，需要先删除
            jarFile.delete();
        }
        ExecUtil.exec("C:\\Users\\pzima\\Downloads\\dex2jar-2.0\\dex2jar-2.0\\d2j-dex2jar.bat \"" + apkFile.getAbsolutePath() + "\" -o \"" + jarFile.getAbsolutePath() + "\" --force");
        return jarFile;
    }

    public File unzip(File jarFile) {
        logger.info("解压jar...");
        File dstFile = new File(jarFile.getParent(), jarFile.getName().substring(0, jarFile.getName().lastIndexOf('.')));
        ExecUtil.exec("unzip -qq -o \"" + jarFile.getAbsolutePath() + "\" -d \"" + dstFile.getAbsolutePath() + "\"");
        logger.info("解压完成，删除jar");
        jarFile.delete();
        return dstFile;
    }

    private LinkedList<String> parseStringList(File rootFile) {

        if (contentList == null) {
            logger.info("获取.class文件内容");
            contentList = new LinkedList<>();
        }
        File[] files = rootFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                parseStringList(file);
            } else if (file.getName().endsWith(".class")) {
                try {
                    contentList.add(FileUtils.readFileToString(file, "US-ASCII"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // 逐个删除unzip得到的文件
            file.delete();
        }
        rootFile.delete();
        return contentList;
    }


    public HashSet<String> getPermissionSet() {
        logger.info("匹配权限字符串...");
        HashSet<String> permissionSet = new HashSet<>();
        for (String content : contentList) {
            Matcher matcher = permissionPattern.matcher(content);
            while (matcher.find()) {
                permissionSet.add(matcher.group(1));
            }
        }
        return permissionSet;
    }

    public HashSet<String> getDomainSet() {
        logger.info("匹配域名字符串...");
        HashSet<String> domainSet = new HashSet<>();
        for (String content : contentList) {
            Matcher matcher = domainPattern.matcher(content);
            while (matcher.find()) {
                domainSet.add(matcher.group(1));
            }
        }
        return domainSet;
    }

    public void dump(String apkFilePath) {
        contentList = null;
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            logger.info("文件不存在，请检查路径");
            return;
        }
        parseStringList(unzip(decompile(apkFile)));
    }

    public static void main(String[] args) {
        ApkDump apkDump = new ApkDump();
        apkDump.dump("D:\\app_test_complete\\139377_汽车商城.apk");

        HashSet<String> permissionSet = apkDump.getPermissionSet();
        HashSet<String> domainSet = apkDump.getDomainSet();
        permissionSet.forEach(permission -> System.out.println(permission));
        System.out.println("---------------");
        domainSet.forEach(domain -> System.out.println(domain));
    }
}