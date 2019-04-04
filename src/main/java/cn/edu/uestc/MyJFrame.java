package cn.edu.uestc;

import cn.edu.uestc.utils.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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
    private MyTable candidateTable;
    private MyTable filteredTable;
    private JLabel footLabel;
    private int skipCount = 0;
    private JButton submitButton;
    private JButton skipButton;
    private JTextField filterDegreeTextField;

    public MyJFrame() {
        InitialComponent();
    }

    private void InitialComponent() {

        setLayout(null);
        setSize(1140, 705);
        //setSize(1000, 870);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 初始化面板
        panel = new JPanel();
        panel.setSize(this.getWidth(), this.getHeight());
        panel.setLocation(0, 0);
        panel.setLayout(null);

        skipButton = new JButton("跳过");
        skipButton.setFont(new Font("楷体", Font.BOLD, 22));
        skipButton.setLocation(10, 10);
        skipButton.setSize(80, 40);

        filterDegreeTextField = new JTextField();
        filterDegreeTextField.setSize(50, 40);
        filterDegreeTextField.setLocation(100, 10);
        filterDegreeTextField.setFont(new Font("", Font.PLAIN, 22));

        appNameLabel = new JTextField();
        appNameLabel.setSize(400, 40);
        appNameLabel.setLocation(160, 10);
        appNameLabel.setFont(new Font("", Font.PLAIN, 22));

        appPkgNameLabel = new JTextField();
        appPkgNameLabel.setSize(420, 40);
        appPkgNameLabel.setLocation(570, 10);
        appPkgNameLabel.setFont(new Font("", Font.PLAIN, 22));


        submitButton = new JButton("下一组");
        submitButton.setSize(120, 40);
        submitButton.setLocation(1000, 10);
        submitButton.setFont(new Font("楷体", Font.BOLD, 22));

        // 初始化表格
        candidateTable = new MyTable(20, 60, 450, 700);
        filteredTable = new MyTable(500, 60, 450, 700);

        JScrollPane candidateScrollpane = new JScrollPane(candidateTable);
        candidateScrollpane.setLocation(570, 60);
        candidateScrollpane.setSize(550, 600);

        JScrollPane filteredScrollpane = new JScrollPane(filteredTable);
        filteredScrollpane.setLocation(10, 60);
        filteredScrollpane.setSize(550, 600);

        // 提交submit按钮点击事件
        submitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String appId = MyJFrame.super.getTitle();
                candidateTable.processData(appId);
                filteredTable.processData(appId);
                updateTable();
            }
        });

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
        panel.add(filterDegreeTextField);
        panel.add(submitButton);
        panel.add(candidateScrollpane);
        panel.add(filteredScrollpane);
        panel.add(footLabel);

        this.add(panel);
    }


    public ArrayList<String[]> updateTable() {

        int filterDegree = 10;
        try {
            filterDegree = Integer.valueOf(filterDegreeTextField.getText());
        } catch (Exception e) {
            filterDegreeTextField.setText("10");
        }
        String preSql = "select domain, freq from (select domain, count(*) as freq from app_domain  GROUP BY domain) as t where freq > " + String.valueOf(filterDegree) + " ORDER BY freq desc ";
        String sql = "select * from `视图1_所有域名` where id in ( select * from (select DISTINCT app_domain.app_id from app_domain where label = 0 and app_domain.app_id in (select DISTINCT id from `视图1_所有域名`) limit " + skipCount + ", 1) as t)";
        String updateSql = "update app_domain set label = ? where app_id = ? and domain = ?";
        ResultSet resultSet = (ResultSet) DBUtil.execute(sql);

        // 需要在表格中显示的数据
        ArrayList<String[]> candidateList = new ArrayList<>();
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
                    filteredList.add(strings);
                } else {
                    candidateList.add(strings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        candidateTable.setData(candidateList);
        filteredTable.setData(filteredList);
        if (candidateList.size() == 0) {
            candidateList = filteredList;
        }
        this.setTitle(candidateList.get(0)[0]);
        appNameLabel.setText(candidateList.get(0)[1]);
        appPkgNameLabel.setText(candidateList.get(0)[2]);

        return filteredList;
    }

    public static void main(String[] args) {
        new MyJFrame().setVisible(true);
    }
}

class MyTable extends JTable {
    MyTable(int x, int y, int width, int height) {
        this.getTableHeader().setFont(new Font("楷体", Font.PLAIN, 22));
        this.setRowHeight(28);
        this.setFont(new Font("", Font.PLAIN, 22));
        this.setLocation(0, 0);
        this.setSize(width, height);
    }

    public void setData(ArrayList<String[]> list) {
        Object[][] showDates = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            String[] string = list.get(i);
            String[] subDate = new String[]{string[3], string[4]};
            showDates[i] = subDate;
        }

        this.setModel(new DefaultTableModel(showDates, new String[]{"域名", "域名信息"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        });

        // 设置为右对齐
        TableColumn column = this.getColumnModel().getColumn(0);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);
        column.setCellRenderer(render);

        column = this.getColumnModel().getColumn(1);
        render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);

        column.setCellRenderer(render);

        this.setSize(60, 90);
        this.setLocation(0, 0);
    }

    public void processData(String appId) {
        String sql = "update app_domain set label = ? where app_id = ? and domain = ?";
        for (int i = 0, j = 0; i < this.getRowCount(); i++, j++) {
            if (j < this.getSelectedRows().length) {
                while (i < this.getSelectedRows()[j]) {
                    DBUtil.execute(sql, "-1", appId, this.getValueAt(i, 0).toString());
                    i++;
                }
                DBUtil.execute(sql, "1", appId, this.getValueAt(i, 0).toString());
            } else {
                do {
                    DBUtil.execute(sql, "-1", appId, this.getValueAt(i, 0).toString());
                } while (++i < this.getRowCount());
                break;
            }
        }
    }
}