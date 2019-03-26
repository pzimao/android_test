package cn.edu.uestc;

import cn.edu.uestc.animal.ChinazCrawler;

import java.util.Random;

public class Test {

    public static void sort1(int[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length - 1; j++) {
                if (array[j] > array[j + 1]) { // 这句话执行了 array.length * (array.length - 1)次
                    int t = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = t;
                }
            }
        }
    }

    public static void sort2(int[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if (array[j] > array[j + 1]) { // 这句话执行了 array.length * (array.length - 1) / 2 次
                    int t = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = t;
                }
            }
        }
    }

    public static void print(int[] array) {
        for (int a : array) {
            System.out.print(a + "\t");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // 随机生成2个一样的数组，每个有200000个数
        Random random = new Random();
        int[] array1 = new int[200000];
        int[] array2 = new int[200000];
        for (int i = 0; i < 200000; i++) {
            int number = random.nextInt(10000);
            array1[i] = number;
            array2[i] = number;
        }

        // 分别用两种方法对这两个数组排序
        // 改进前的方法
        long startTime = System.currentTimeMillis();
        sort1(array1);
        System.out.println("改进前的排序耗时 " + (System.currentTimeMillis() - startTime) / 1000 + "秒");

        // 改进后的方法
        startTime = System.currentTimeMillis();
        sort2(array2);
        System.out.println("改进后排序耗时 " + (System.currentTimeMillis() - startTime) / 1000 + "秒");
    }
}