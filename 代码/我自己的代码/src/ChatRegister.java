import JDBC_util.JDBCUTILS;
import sun.jvm.hotspot.tools.PStack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatRegister extends JFrame{
    static final String HOST = "127.0.0.1";
    static final int PORT = 8002;

    private JPanel panel1;
    private JTextField textField1;//用户名
    private JTextField textField2;//密码
    private JButton SureButton;
    private JButton BackButton;

    public ChatRegister(){
        JFrame frame = new JFrame("ChatRegister");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);//设置不可拉伸
        frame.setLocationRelativeTo(null);//设置屏幕居中


        //确认按钮的事件
        SureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = textField1.getText();
                String password = textField2.getText();

                Connection conn = JDBCUTILS.getConnection();

                String sql = "INSERT INTO UserInformations(Uname, Upassword,Ulogining) VALUES (?,?,?);";

                try {
                    PreparedStatement pre = conn.prepareStatement(sql);

                    pre.setObject(1,username);
                    pre.setObject(2,password);
                    pre.setObject(3,false);

                    if (pre.executeUpdate()!=0){
                        JOptionPane.showMessageDialog(null, "注册成功", "请返回主界面登陆",JOptionPane.WARNING_MESSAGE);
                        System.out.println("Successful");

                    }else{
                        JOptionPane.showMessageDialog(null, "已经注册", "错误", JOptionPane.ERROR_MESSAGE);
                        System.out.println("failed");
                    }


                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "数据库错误请重新注册", "错误", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        //返回按钮的事件
        BackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new ChatLogin();
            }
        });
    }

}
