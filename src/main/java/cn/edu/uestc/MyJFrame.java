package cn.edu.uestc;

import cn.edu.uestc.utils.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MyJFrame extends JFrame {

    // 定义一些必要的组件
    private JTextField appNameLabel;
    private JTextField appPkgNameLabel;
    private JPanel panel;
    private JTable table;
    private JLabel footLabel;
    private ArrayList<String> arrayList;
    private int skipCount = 0;

    public MyJFrame() {
        InitialComponent();
        arrayList = new ArrayList<>();

        arrayList.add("域名标注程序");
    }

    private void InitialComponent() {

        setLayout(null);
        setSize(1000, 820);
        //setSize(1000, 870);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 初始化面板
        panel = new JPanel();
        panel.setSize(this.getWidth(), this.getHeight());
        panel.setLocation(0, 0);
        panel.setLayout(null);

        appNameLabel = new JTextField();
        appNameLabel.setSize(200, 40);
        appNameLabel.setLocation(120, 10);
        appPkgNameLabel = new JTextField();
        appPkgNameLabel.setSize(450, 40);
        appPkgNameLabel.setLocation(330, 10);

        JButton submitButton = new JButton("下一组");
        submitButton.setSize(160, 40);
        submitButton.setLocation(800, 10);

        submitButton.setFont(new Font("楷体", Font.BOLD, 22));
        appNameLabel.setFont(new Font("", Font.PLAIN, 22));
        appPkgNameLabel.setFont(new Font("", Font.PLAIN, 22));
        // 初始化表格
        table = new JTable();
        table.getTableHeader().setFont(new Font("楷体", Font.PLAIN, 22));
//        table.setFont(new Font("", Font.PLAIN, 22));
        table.setRowHeight(28);
        table.setFont(new Font("", Font.PLAIN, 22));
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setSize(new Dimension(950, 700));
        scrollpane.setLocation(20, 60);


        // 按钮点击时显示当前选中项
        submitButton.addActionListener(new ActionListener() {
            int tableRows = 0;
            String sql = "update app_domain set label = ? where app_id = ? and domain = ?";

            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableRows != 0) {
                    String appId = MyJFrame.super.getTitle();
                    System.out.println(appId);
                    HashSet<Integer> set = new HashSet<>();
                    // tableRows 不包括最后一行
                    for (int i = 0; i < tableRows; i++) {
                        set.add(i);
                    }
                    System.out.println("一共 " + set.size() + " 个域名");
                    int[] selectedRowIndexes = table.getSelectedRows();
                    for (int rowIndex : selectedRowIndexes) {
                        // 更新选择的域名
                        String domain = table.getValueAt(rowIndex, 0).toString();
                        if ("".equals(domain)) {
                            break;
                        }
                        DBUtil.execute(sql, "1", appId, domain);
                        // 移除
                        set.remove(rowIndex);
                    }
                    System.out.println("标注了" + (tableRows - set.size()) + " 条");
                    for (int rowIndex : set) {
                        // 更新未选择的域名
                        String domain = table.getValueAt(rowIndex, 0).toString();
                        DBUtil.execute(sql, "-1", appId, domain);
                    }
                    System.out.println("排除了" + set.size() + " 条");
                }

                // 更新表格
                tableRows = updateTable();
            }
        });
        JButton skipButton = new JButton("跳过");
        skipButton.setFont(new Font("楷体", Font.BOLD, 22));
        skipButton.setLocation(20, 10);
        skipButton.setSize(80, 40);
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                skipCount++;
                updateTable();
            }
        });
        footLabel = new JLabel();
        footLabel.setSize(200, 14);
        footLabel.setLocation(20, 762);
        footLabel.setFont(new Font("楷体", Font.PLAIN, 12));

        panel.add(skipButton);
        panel.add(appNameLabel);
        panel.add(appPkgNameLabel);
        panel.add(submitButton);
        panel.add(scrollpane);
        panel.add(footLabel);


        this.add(panel);
        new Thread(() -> {
            int i = 0;
            Random random = new Random();
            while (true) {
                footLabel.setText(arrayList.get((i++) % arrayList.size()));
                footLabel.setLocation(random.nextInt(780) + 20, 762);
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    System.out.println("睡觉也能异常？");
                }
            }
        }).start();

    }


    public int updateTable() {
        String sql = "select * from `视图1_所有域名` where id in ( select * from (select DISTINCT app_domain.app_id from app_domain where label = 0 and app_domain.app_id in (select DISTINCT id from `视图1_所有域名`) limit " + skipCount + ", 1) as t)";
        ResultSet resultSet = (ResultSet) DBUtil.execute(sql);

        ArrayList<String[]> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String[] strings = new String[5];
                for (int j = 0; j < 5; j++) {
                    strings[j] = resultSet.getString(j + 1);
                }
                list.add(strings);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.add(new String[]{list.get(0)[0], list.get(0)[1], list.get(0)[2], "", "没有自己的域名"});
        Object[][] showDates = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            String[] string = list.get(i);
            String[] subDate = new String[]{string[3], string[4]};
            showDates[i] = subDate;
        }

        TableModel tableModel = new DefaultTableModel(showDates, new String[]{"域名", "域名信息"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        table.setModel(tableModel);

        // 设置为右对齐
        TableColumn column = table.getColumnModel().getColumn(0);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);
        column.setCellRenderer(render);

        column = table.getColumnModel().getColumn(1);
        render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);

        column.setCellRenderer(render);

        table.setSize(panel.getWidth(), panel.getHeight() - 90);
        table.setLocation(0, 0);
        this.setTitle(list.get(0)[0]);
        appNameLabel.setText(list.get(0)[1]);
        appPkgNameLabel.setText(list.get(0)[2]);
        return list.size() - 1;
    }

    public static void main(String[] args) {
        new MyJFrame().setVisible(true);
    }
}