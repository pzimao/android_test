package cn.edu.uestc.wechat.bean;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Boundary {
    int x1;
    int y1;
    int x2;
    int y2;

    public Boundary(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Boundary(String boundStr) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(boundStr);
        ArrayList<Integer> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(Integer.valueOf(matcher.group(1)));
        }
        this.x1 = list.get(0);
        this.y1 = list.get(1);
        this.x2 = list.get(2);
        this.y2 = list.get(3);
    }

    public int[] getCenterPosition() {
        int[] postion = {(x2 - x1) / 2 + x1, (y2 - y1) / 2 + y1};
        return postion;
    }

    public int[] getPositionToBottom(double scale) { // scale 从-1 到 1, 越大表示越远离
        int[] postion = {(x2 - x1) / 2 + x1, (int) (((y2 - y1) / 2) * (1 - scale)) + y1};
        return postion;
    }

    public int[] getPositionToRight(double scale) {
        int[] postion = {(int) (((x2 - x1) / 2) * (1 - scale)) + x1, (y2 - y1) / 2 + y1};
        return postion;
    }

    @Override
    public String toString() {
        return "Boundary{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                '}';
    }
}
