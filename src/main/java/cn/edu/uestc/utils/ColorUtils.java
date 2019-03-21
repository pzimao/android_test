package cn.edu.uestc.utils;

public class ColorUtils {
    public static void main(String[] args) {
        System.out.println(new ColorUtils().getIntFromColor(99, 99, 99));
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
}
