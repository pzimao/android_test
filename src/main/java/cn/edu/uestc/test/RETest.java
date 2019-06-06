package cn.edu.uestc.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RETest {
    public static void main(String[] args) {
        getName().forEach(wxh -> System.out.println(wxh));
    }

    public static ArrayList<String> getName() {
        try {
            String str = FileUtils.readFileToString(new File("C:\\Users\\pzima\\Desktop\\深圳时间文化周.html"));
            Pattern pattern = Pattern.compile("微信号<\\/label>\\s+<span.*?>(.+)<\\/span>");
            Matcher matcher = pattern.matcher(str);
            ArrayList<String> list = new ArrayList<>();
            while (matcher.find()) {
                list.add(matcher.group(1));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
