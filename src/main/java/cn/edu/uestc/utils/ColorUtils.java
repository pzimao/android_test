package cn.edu.uestc.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ColorUtils {
    public static void main(String[] args) {
        System.out.println(new ColorUtils().getIntFromColor(0, 0, 0));
    }

    /**
     * 颜色计算，输入3个颜色值，计算出Int颜色
     *
     * @param Red
     * @param Green
     * @param Blue
     * @return
     */
    public int getIntFromColor(int Red, int Green, int Blue) {
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
    public static void func1() {
        int rgbR;
        int rgbG;
        int rgbB;
        int minx = 0;
        int miny = 0;
        try {
            File file = new File("C:\\Users\\pzima\\1.png");
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();//图片宽度
            int height = image.getHeight();//图片高度
            for (int i = minx; i < width; i++) {
                for (int j = miny; j < height; j++) {
                    int pixel = image.getRGB(i, j); // 下面三行代码将一个数字转换为RGB数字
                    rgbR = (pixel & 0xff0000) >> 16;
                    rgbG = (pixel & 0xff00) >> 8;
                    rgbB = (pixel & 0xff);
                    System.out.println("i=" + i + ",j=" + j + ":(" + rgbR + "," + rgbG + "," + rgbB + ")");
                }
            }
            System.out.println("图片宽度为："+width+",高度为:"+height);
        } catch (IOException e) {
            System.out.println("读取文件出错");
            e.printStackTrace();
        }
    }
}
