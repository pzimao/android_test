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

public class MyJFrame extends JFrame {

    // 定义一些必要的组件
    private JTextField appNameLabel;
    private JTextField appPkgNameLabel;
    private JPanel panel;
    private JTable table;
    private JLabel footLabel;
    private int skipCount = 0;
    private JButton submitButton;
    private JButton skipButton;

    public MyJFrame() {
        InitialComponent();
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

        submitButton = new JButton("下一组");
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


        // 提交submit按钮点击事件
        submitButton.addActionListener(new ActionListener() {
            String sql = "update app_domain set label = ? where app_id = ? and domain = ?";

            @Override
            public void actionPerformed(ActionEvent e) {
                String appId = MyJFrame.super.getTitle();
                System.out.println(appId);
                for (int i = 0, j = 0; i < table.getRowCount(); i++, j++) {
                    if (j < table.getSelectedRows().length) {
                        while (i < table.getSelectedRows()[j]) {
                            DBUtil.execute(sql, "-1", appId, table.getValueAt(i, 0).toString());
                            i++;
                        }
                        DBUtil.execute(sql, "1", appId, table.getValueAt(i, 0).toString());
                    } else {
                        do {
                            DBUtil.execute(sql, "-1", appId, table.getValueAt(i, 0).toString());
                        } while (++i < table.getRowCount());
                        break;
                    }
                }
                updateTable();
            }
        });
        skipButton = new JButton("跳过");
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

        }).start();

    }


    public ArrayList<String[]> updateTable() {

        String preSql = "select domain, freq from (select domain, count(*) as freq from app_domain  GROUP BY domain) as t where freq > 80 ORDER BY freq desc ";
        String sql = "select * from `视图1_所有域名` where id in ( select * from (select DISTINCT app_domain.app_id from app_domain where label = 0 and app_domain.app_id in (select DISTINCT id from `视图1_所有域名`) limit " + skipCount + ", 1) as t)";
        String updateSql = "update app_domain set label = ? where app_id = ? and domain = ?";
        ResultSet resultSet = (ResultSet) DBUtil.execute(sql);

        // 需要在表格中显示的数据
        ArrayList<String[]> list = new ArrayList<>();
        ArrayList<String[]> filteredList = new ArrayList<>();
        try {
            ResultSet preResultSet = (ResultSet) DBUtil.execute(preSql);
            HashSet<String> preSet = new HashSet<>();
            while (preResultSet.next()) {
                preSet.add(preResultSet.getString(1));
            }
            while (resultSet.next()) {
                String[] strings = new String[5];
                for (int j = 0; j < 5; j++) {
                    strings[j] = resultSet.getString(j + 1);
                }
                // todo 2019-4-4 测试功能，频繁出现的域名从查询结果中排除
                if (preSet.contains(strings[3])) {
                    // 这条标-1
                    DBUtil.execute(updateSql, "-1", strings[0], strings[3]);
                    filteredList.add(strings);
                    System.out.println("过滤 " + strings[3]);
                } else {
                    list.add(strings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        if (list.size() == 0) {
            list = filteredList;
        }
        this.setTitle(list.get(0)[0]);
        appNameLabel.setText(list.get(0)[1]);
        appPkgNameLabel.setText(list.get(0)[2]);

        return filteredList;
    }

    public static void main(String[] args) {
        new MyJFrame().setVisible(true);
    }
}