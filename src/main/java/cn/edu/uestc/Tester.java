package cn.edu.uestc;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public abstract class Tester {


    Tester() {
        this.folderName = this.getFolderName();
    }

    public HashSet<String> appIdSet = new HashSet<>();
    public String folderName;

    public abstract void initSet();

    // 获取所在文件夹
    public abstract String getFolderName();

    // 获取要测试的微信小程序
    public abstract HashMap<Integer, String> getAppMap();

    // 点击
    public abstract void click(String appName);

    // 文件导出
    public abstract void exportFile(String appName);

    // 更新数据库表
    public abstract void updateTable(Integer id);

    public void test(double rateLimit) {
        HashMap<Integer, String> appMap = getAppMap();
        initSet();
        Queue<Long> queue = new ArrayDeque<>(5);
        for (Integer id : appMap.keySet()) {
            String appName = appMap.get(id);
            if (queue.size() >= 5) {
                queue.poll(); // 出队
            }
            queue.offer(System.currentTimeMillis()); // 入队
            click(appName);
            exportFile(appName);
            updateTable(id);
            // todo 速度限制
            long head = queue.peek();
            double currentRate = queue.size() * 1.0 / (System.currentTimeMillis() - head) / 1000.0 / 60;

            BigDecimal bd = new BigDecimal(currentRate);
            double d1 = bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            System.out.println("当前速度 " + d1 + " 速度上限是 " + rateLimit);
            while (currentRate > rateLimit) {
                try {
                    Thread.sleep(20000);
                    currentRate = queue.size() * 1.0 / (System.currentTimeMillis() - head) / 1000.0 / 60;
                    d1 = bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    System.out.println("当前速度 " + d1 + " 速度上限是 " + rateLimit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        // DeviceManager.initial();
        Tester tester = new WechatTester("d:\\1.txt", "d://weixin/");
        tester.test(0.5);
    }
}
