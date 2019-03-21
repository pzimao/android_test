package cn.edu.uestc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class FileCheck {

    public static void main(String[] args) throws Exception {
        File file = new File("结果.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        while (line != null) {
            String[] array = line.split("\t");
            if (!array[3].endsWith(array[2])) {
                System.out.println(array[3]);
            }
            line = br.readLine();
        }
    }
}
