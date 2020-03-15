import JDBC_util.JDBCUTILS;
import javafx.scene.control.Alert;

import javax.swing.*;
import javax.xml.transform.Source;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatLogin extends JFrame{
    private JPanel panel1;
    private JTextField textField1;//用户名
    private JButton registButton;
    private JButton loginButton;
    private JButton cancelButton;
    private JPasswordField passwordField1;//密码
    private JButton ChangeButton;
    public String UserName;

    public static void main(String[] args) {
        new ChatLogin();
    }

    public JFrame frame;
    public void init(){
        frame = new JFrame("ChatLogin");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);//设置不可拉伸
        frame.setLocationRelativeTo(null);//设置屏幕居中
        frame.setSize(320,180);
        frame.pack();
        frame.setVisible(true);
    }

    public ChatLogin(){
        init();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = textField1.getText();
                String password = passwordField1.getText();


                Connection conn = JDBCUTILS.getConnection();

                String sql = "SELECT * FROM UserInformations where Uname=? and Upassword=? AND Ulogining=?;";
                try {
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setObject(1,user);
                    preparedStatement.setObject(2,password);
                    preparedStatement.setObject(3,false);

                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()){
                        frame.dispose();
                        new ChatClient_f(user);
                        sql = "UPDATE UserInformations SET Ulogining=? WHERE Uname = ?;";
                        preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setObject(1,true);
                        preparedStatement.setObject(2,user);
                        preparedStatement.executeUpdate();
                        System.out.println("Successful");
                    }else{
                        JOptionPane.showMessageDialog(null, "用户名或者密码不正确或者已经登陆", "错误", JOptionPane.ERROR_MESSAGE);
                        System.out.println("Failed");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    System.out.println("登陆异常");
                }
            }
        });

        registButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new ChatRegister();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        ChangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new ChatChange();
            }
        });
    }
}
