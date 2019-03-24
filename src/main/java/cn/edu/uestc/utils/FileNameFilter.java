package cn.edu.uestc.utils;

import java.util.regex.Pattern;

public class FileNameFilter {
    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    public static String filter(String fileName) {
        fileName = fileName.trim();
        return fileName == null ? "" : FilePattern.matcher(fileName).replaceAll("#");
    }
}
