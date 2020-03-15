import JDBC_util.JDBCUTILS;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatChange {
    private JPanel panel1;
    private JButton SureButton;
    private JButton CancelButton;
    private JTextField textField1;//用户名
    private JTextField textField2;//原先密码
    private JTextField textField3;//新密码
    private JFrame frame;

    private void init(){
        frame = new JFrame("ChatChange");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320,180);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);//设置不可拉伸
        frame.setLocationRelativeTo(null);//设置屏幕居中
    }

    public static void main(String [] args){
        new ChatChange();
    }


    public ChatChange() {
        init();

        SureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Connection conn = JDBCUTILS.getConnection();
                synchronized (conn) {
                    String sql = "UPDATE UserInformations SET Upassword=? WHERE Uname=? AND Upassword=?;";
                    try {
                        PreparedStatement pre = conn.prepareStatement(sql);
                        pre.setObject(1, textField3.getText());
                        pre.setObject(2, textField1.getText());
                        pre.setObject(3, textField2.getText());
                        if (pre.executeUpdate() != 0) {
                            Object[] options = {"OK", "CANCEL"};
                            JOptionPane.showOptionDialog(null, "更改成功", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                        } else {
                            JOptionPane.showMessageDialog(null, "用户名或者密码不正确", "错误", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "用户名或者密码不正确", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        CancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new ChatLogin();
            }
        });
    }
}
