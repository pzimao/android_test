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

    private JLabel label1;
    private JLabel label2;
    private JLabel appIdLabel1;
    private JLabel appIdLabel2;

    public MyJFrame() {
        InitialComponent();
    }

    private void InitialComponent() {

        this.setTitle("域名标注");
        setLayout(null);
        setSize(1140, 620);
        //setSize(1000, 870);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 初始化面板
        panel = new JPanel();
        panel.setSize(this.getWidth(), this.getHeight());
        panel.setLocation(0, 0);
        panel.setLayout(null);

        appIdLabel1 = new JLabel("APP ID:");
        appIdLabel1.setLocation(10, 10);
        appIdLabel1.setSize(75, 28);
        appIdLabel1.setFont(new Font("楷体", Font.PLAIN, 18));

        appIdLabel2 = new JLabel("");
        appIdLabel2.setLocation(85, 10);
        appIdLabel2.setSize(90, 28);
        appIdLabel2.setFont(new Font("楷体", Font.PLAIN, 18));
        appIdLabel2.setHorizontalAlignment(SwingConstants.LEFT);
        label1 = new JLabel("筛出至少");
        label1.setLocation(10, 50);
        label1.setSize(180, 28);
        label1.setFont(new Font("楷体", Font.PLAIN, 18));

        label2 = new JLabel("个APP引用的域名");
        label2.setFont(new Font("楷体", Font.PLAIN, 18));
        label2.setLocation(150, 50);
        label2.setSize(180, 28);
        skipButton = new JButton("跳过");
        skipButton.setFont(new Font("楷体", Font.PLAIN, 18));
        skipButton.setLocation(460, 10);
        skipButton.setSize(98, 58);

        filterDegreeTextField = new JTextField();
        filterDegreeTextField.setSize(45, 25);
        filterDegreeTextField.setLocation(105, 50);
        filterDegreeTextField.setFont(new Font("", Font.PLAIN, 18));
        filterDegreeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        appNameLabel = new JTextField();
        appNameLabel.setSize(420, 28);
        appNameLabel.setLocation(570, 10);
        appNameLabel.setFont(new Font("", Font.PLAIN, 18));

        appPkgNameLabel = new JTextField();
        appPkgNameLabel.setSize(420, 28);
        appPkgNameLabel.setLocation(570, 40);
        appPkgNameLabel.setFont(new Font("", Font.PLAIN, 18));


        submitButton = new JButton("下一组");
        submitButton.setSize(120, 58);
        submitButton.setLocation(1000, 10);
        submitButton.setFont(new Font("楷体", Font.PLAIN, 18));

        // 初始化表格
        candidateTable = new MyTable(500, 60, 450, 700);
        filteredTable = new MyTable(500, 60, 450, 700);

        JScrollPane candidateScrollpane = new JScrollPane(candidateTable);
        candidateScrollpane.setLocation(570, 75);
        candidateScrollpane.setSize(550, 500);

        JScrollPane filteredScrollpane = new JScrollPane(filteredTable);
        filteredScrollpane.setLocation(10, 75);
        filteredScrollpane.setSize(550, 500);

        // 提交submit按钮点击事件
        submitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String appId = appIdLabel2.getText();
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
        panel.add(label1);
        panel.add(label2);
        panel.add(appIdLabel1);
        panel.add(appIdLabel2);
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
        appIdLabel2.setText(candidateList.get(0)[0]);
        appNameLabel.setText(candidateList.get(0)[1]);
        appPkgNameLabel.setText(candidateList.get(0)[2]);

        return filteredList;
    }

    public static void main(String[] args) {
        new MyJFrame().setVisible(true);
    }
}

class MyTable extends JTable {
    private ArrayList<String[]> list;

    MyTable(int x, int y, int width, int height) {
        this.getTableHeader().setFont(new Font("楷体", Font.PLAIN, 18));
        this.setRowHeight(28);
        this.setFont(new Font("", Font.PLAIN, 18));
        this.setLocation(0, 0);
        this.setSize(width, height);

    }

    public void setData(ArrayList<String[]> list) {
        this.list = list;
        Object[][] showDates = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            String[] string = list.get(i);
            String domain = string[3];
            String domainDesc = string[4];
            if (domain.length() > 26) {
                domain = "..." + domain.substring(domain.length() - 26);
            }
//            if (domainDesc.length() > 15) {
//                domainDesc = "..." + domainDesc.substring(domainDesc.length() - 15);
//            }
            String[] subDate = new String[]{domain, domainDesc};
            showDates[i] = subDate;
        }

        this.setModel(new DefaultTableModel(showDates, new String[]{"域名", "域名信息"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
//                return true;
                return false;
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
                    DBUtil.execute(sql, "-1", appId, this.list.get(i)[3]);
                    i++;
                }
                DBUtil.execute(sql, "1", appId, this.list.get(i)[3]);
            } else {
                do {
                    DBUtil.execute(sql, "-1", appId, this.list.get(i)[3]);
                } while (++i < this.getRowCount());
                break;
            }
        }
    }
}