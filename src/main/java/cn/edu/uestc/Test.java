package cn.edu.uestc;

import cn.edu.uestc.utils.APKUtil;
import cn.edu.uestc.utils.DBUtil;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        String str = "";
        char[] charArray = str.toCharArray();
        boolean is1NumReady = false; // 第一个操作数准备好了吗
        boolean opFlag = true;
        while (opFlag) {
            opFlag = false;
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == '*' || c == '/' || c == '+' || c == '-') {
                    // 寻找前后两个操作数
                    int number1 = 0;
                    int number2 = 0;
                    char c1 = charArray[i - 1];
                    for (int i1 = i-1;i1 >= 0;i1--) {

                    }
                    while (c1 >= '0' && c1 <= '9') {
                        number1 = Integer.valueOf(c1) * 10 + number1;
                    }
                }
            }
        }
    }
}
