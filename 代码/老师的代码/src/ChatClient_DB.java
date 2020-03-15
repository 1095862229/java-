/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * @author user
 */
/////////////////////////////////////////////////////////////////////////////////////////////////////
//客户端程序 //
//多用户聊天室 //
//是 Application，也是 Applet ，在单机和浏览器上都可运行 //
//ChatClient_DB.java //
//姓名： //
//20XX 年 XX 月 XX 日 //
/////////////////////////////////////////////////////////////////////////////////////////////////

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.JLabel.*;
import javax.swing.JApplet;
import javax.swing.event.*;

public class ChatClient_DB extends JApplet implements ActionListener, ItemListener {
    static int n = 0;
    JLabel lb1 = new JLabel("在线人员");
    JLabel lb2 = new JLabel("颜色选择");
    JLabel lb3 = new JLabel("我是：", JLabel.RIGHT);//用户名称
    JLabel lb4 = new JLabel("用户名称:", JLabel.LEFT);
    JTextField textField1 = new JTextField(30);//用于数据输入
    JButton send = new JButton("发送");
    JButton exit = new JButton("退出");
    //Image myImage;
    JComboBox userList = new JComboBox(); //定义用户列表
    JComboBox mychoice;
    JCheckBox sl = new JCheckBox("私聊"); //定义私聊复选
    JTextArea ta = new JTextArea(null, 20, 50); //用于显示数据
    JScrollPane SC_ta = new JScrollPane(ta);




    Socket clientsocket = null; //用于通信的套接字


    DataOutputStream os;
    DataInputStream is;
    String ss3 = "";
    String S_items[] = {"red", "blue", "green", "yellow", "black", "orange", "pink"};
    int t; //次整形变量为颜色信息长度
    boolean boo;//私聊状态 true
    // public void paint(Graphics g){
// g.drawImage(myImage,10,10,this);
// }
    LoginClient login = null; //将聊天的数据显示在 ta 中的一个线程

    public static void main(String args[]) { // 一个 application 的 main()
        Frame frame = new Frame("客户端窗口"); // 创建窗口 frame to house the applet
        ChatClient_DB app = new ChatClient_DB(); // Create an instance of the class (applet)
        n = 30;
//Add it to the center of the frame
        frame.add("Center", app);
        frame.setSize(355, 400);
        frame.validate();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { // frame.dispose();
                System.exit(0);
            }
        });
//Register the AppBoth class as the listener for a Window Destroy event
//frame.addWindowListener(new WindowControl(app));
//frame.addWindowListener(new closeWin());
        app.init(); //Call the applet methods
        app.start();
    }

    public void init() {
        mychoice = new JComboBox(S_items);//定义颜色
        userList.addItem("大家");
        ta.setLineWrap(true);//设置换行方式
        setLayout(null);
        setBounds(5, 8, 355, 370);
        Panel p1 = new Panel(); //定义容器
        p1.setLayout(null);
        lb3.setBounds(new Rectangle(5, n + 5, 55, 15));
        lb4.setBounds(new Rectangle(65, n + 5, 100, 15));
        SC_ta.setBounds(new Rectangle(5, n + 25, 325, 210));//5,60,325,210
        ta.setEditable(false); //设容器位置
        //lb3.setVisible(true);
        //lb4.setVisible(true);
        textField1.setBounds(new Rectangle(5, 285, 200, 20));
        send.setBounds(new Rectangle(208, 285, 60, 20));
        exit.setBounds(new Rectangle(270, 285, 60, 20));
        lb1.setBounds(new Rectangle(5, 315, 55, 20));
        userList.setBounds(new Rectangle(65, 315, 70, 20));
        lb2.setBounds(new Rectangle(150, 315, 55, 20));
        mychoice.setBounds(new Rectangle(210, 315, 50, 20));
        sl.setBounds(new Rectangle(275, 315, 55, 20));
        //setVisible(true);
        p1.setBounds(5, 5, 355, 360);
        p1.add(lb3);
        p1.add(lb4);
        p1.add(SC_ta);
        p1.add(textField1); //把控件添加在容器中
        p1.add(send);
        p1.add(exit);
        p1.add(lb1);
        p1.add(userList);
        p1.add(lb2);
        p1.add(mychoice);
        p1.add(sl);
        add(p1);
        send.addActionListener(this); //设置监听器
        exit.addActionListener(this);
        textField1.addActionListener(this);
        mychoice.addItemListener(this);
        userList.addItemListener(this);
        userList.addActionListener(this);
        setSize(355, 370);
        ConnectDialog cd = new ConnectDialog(this, new Frame(), "连接", true); //输入用户名和主机，以便登录
        cd.setVisible(true);
    }//init()

    public void itemStateChanged(ItemEvent e) {
        Object obj = e.getSource();
        if (obj.equals(mychoice)) {
            //设置发送端颜色
            ss3 = (String) mychoice.getSelectedItem();
            if (ss3 == "red")
                textField1.setForeground(Color.red);
            if (ss3 == "blue")
                textField1.setForeground(Color.blue);
            if (ss3 == "yellow")
                textField1.setForeground(Color.yellow);
            if (ss3 == "black")
                textField1.setForeground(Color.black);
            if (ss3 == "orange")
                textField1.setForeground(Color.orange);
            if (ss3 == "pink")
                textField1.setForeground(Color.pink);
            if (ss3 == "green")
                textField1.setForeground(Color.green);
            t = ss3.length();
            ss3 = "#&@#" + ss3;
        }
    }

    public void actionPerformed(ActionEvent evt) {//获取触发事件的对象
        String sendString;
        String sList = (String) userList.getSelectedItem(); //定义选择用户数组
        Object obj = evt.getSource();
        //boo=sl.isSelected();//私聊状态 true
        boo = (sList.indexOf("大家") != -1) ? false : true;//boolean 加此句，便可去掉私聊复选
        //可去掉复选 JCheckBox sl=new JCheckBox("私聊"); //定义私聊复选
        //out_println("boo："+boo+"/n");
        if ((obj.equals(textField1) || obj.equals(send)) && !boo) {//公聊
            sendString = "对" + sList + "说：" + textField1.getText() + ss3;
            sendData(sendString);
        } else if (!obj.equals(exit) && boo) {//私聊，被聊天用户名前后加"#"号
            sendString = "#" + sList + "#" + textField1.getText() + ss3;
            sendData(sendString);
        } else if (obj.equals(exit)) {//退出按钮
            sendString = "用户" + lb4.getText() + "退出";//+textField1.getText()
            sendData(sendString);
            closeSocket();//本聊客退出
//this.dispose();
            System.exit(0);
        }
        this.display(lb3.getText() + "对" + sList + "说：" + textField1.getText() + "\n");
        textField1.setText(""); //发送后清空
        textField1.requestFocus();//焦点
    }

    boolean sendData(String toServer) { //自定义向服务器端发送数据方法

        try {
            if (toServer.length() > 0) {//判断语句是否为空
                if (toServer.charAt(0) != '#')//无"#"号，则前面加"*"号
                    toServer = '*' + toServer;//
                os.writeUTF(toServer);
                os.flush();
            }
        } catch (Exception e) {
            System.out.println("Exception in sendData:" + e);
            return false;
        }
        return true;
    }

    String getData() {//服务器端得到数据
        int i, j;
        String user;
        String fromServer = null;
        out_println("fromServer0：");
        try {
            fromServer = is.readUTF();//从服务器端接收信息，从其它用户下拉列表中加入或删除
            out_println("fromServer1：" + fromServer);
            if (fromServer.indexOf("用户") != -1) {
//"警告"、"退出"、"删除"和"加入"操作
                if (fromServer.indexOf("退出") != -1 || fromServer.indexOf("删除") != -1) {//在用户端列表中删除
                    if (fromServer.indexOf(lb3.getText()) != -1) {//"删除"自己
                        userList.removeAllItems();//"删除"操作：被剔除的用户下拉列表中清空
                    } else {//其余用户的"退出"和"删除"操作：删除此用户
                        j = userList.getItemCount();
                        for (int k = 1; k <= j; ++k) {
                            user = (String) userList.getItemAt(k - 1);
                            if (fromServer.indexOf(user) != -1) {//
                                userList.removeItem(user);//其余的将"退出"和"删除"用户删除
                                --j;
                                out_println("userList0：" + j + ";k=" + k);
                            }
                            out_println("userList1：" + j + ";k=" + k);
                        }//for
                    }//if lb3
                }//if"退出"，"删除"
                //"加入"操作，包括自己
                if (fromServer.endsWith("加入\n")) {//在用户端添加列表
                    i = fromServer.length();
                    user = fromServer.substring(2, i - 3);//2 指"用户
                    userList.addItem(user);
                }//if 加入
            } else { //"聊天"和"警告"操作，获取用户名
                i = fromServer.length();
                user = fromServer.substring(4, i);
                //userList.addItem(user);//userList.add(user);
            } //if 用户

        } catch (IOException e) {
            return null;
        } //try
        return fromServer;
    }//get

    void closeSocket() {//本聊客退出，关闭服务器连接
        try {
            os.close();
            is.close();
            clientsocket.close();//套接字
            //userList.removeAllItems();//清除所有
        } catch (Exception e) {
            System.out.println("Exception in closeData:" + e);
        }
    }

    boolean connectHost(String hostName) {//建立服务器连接
        if (clientsocket == null) {
            try {
                clientsocket = new Socket(hostName, 8002);
                is = new DataInputStream(clientsocket.getInputStream());
                os = new DataOutputStream(clientsocket.getOutputStream());
            } catch (UnknownHostException e) {
                System.out.println("Trying to connect unknown Host" + e);
                return false;
            } catch (IOException e) {
                System.out.println(e.toString());
                return false;
            }
        }
        return true;
    }

    public void display(String str) {//在 ta 中显示收到的数据
        ta.append(str);
    }

    public void YHM(String str) {//在 lb4 中显示用户名
        lb4.setText(str);
    }

    public void out_println(String str) {//显示
        System.out.println(str);
    }

    public void display1(String str) { //在客户端显示颜色
        if (str.indexOf("pink") != -1) {
            ta.setForeground(Color.pink);
        } else if (str.indexOf("orange") != -1) { //判断颜色
            ta.setForeground(Color.orange);
        } else if (str.indexOf("green") != -1) {
            ta.setForeground(Color.green);
        } else if (str.indexOf("black") != -1) {
            ta.setForeground(Color.black);
        } else if (str.indexOf("red") != -1) {
            ta.setForeground(Color.red);
        } else if (str.indexOf("yellow") != -1) {
            ta.setForeground(Color.yellow);
        } else if (str.indexOf("blue") != -1) {
            ta.setForeground(Color.blue);
        }
    }
}

//客户聊天线程
class LoginClient extends Thread {
    ChatClient_DB client = null;
    String fromServer1;

    //将客户对象 client 与客户线程捆绑在一起
    public LoginClient(ChatClient_DB client) {
        this.client = client;
    }

    public void run() {
        String fromServer = null;
        while ((fromServer = client.getData()) != null) {//始终循环
            if (fromServer.indexOf("#&@#") != -1) {// 取颜色信息并在用户端设置颜色
                int i = fromServer.lastIndexOf("#&@#");
                fromServer1 = fromServer.substring(fromServer.indexOf("#&@#"));//获取 #$@#
                fromServer = fromServer.substring(0, fromServer.length() - fromServer1.length());
                fromServer1 = fromServer1.substring(4, fromServer1.length() - 1);//获取颜色名称
                client.display(fromServer + "\n"); //在 ta 中显示收到的数据
                client.display1(fromServer1); //在客户端显示颜色
            } else client.display(fromServer); //在 ta 中显示收到的数据
        }
    }//run()
}//LoginClient

class ConnectDialog extends JDialog { //建立登录信息对话框
    ChatClient_DB app;
    JPanel DialogPane = new JPanel();
    JTextField host = new JTextField("127.0.0.1", 20); //本地主机名
    JTextField user = new JTextField(20);
    JTextField passwd = new JTextField(20);
    JButton register = new JButton("注册");
    JButton connect = new JButton("登录");//连接
    JButton cancel = new JButton("取消");
    JLabel L_user = new JLabel("用户名：", JLabel.RIGHT);
    JLabel L_host = new JLabel("主机名 IP：", JLabel.RIGHT);
    JLabel L_passwd = new JLabel("密码：", JLabel.RIGHT);

    ConnectDialog(ChatClient_DB ap, Frame f1, String str, boolean b1) {
        super(f1, str, b1);
        app = ap;
        app.out_println("登录对话框 0");
        DialogPane.setLayout(null);
        L_user.setBounds(new Rectangle(10, 10, 70, 20));
        user.setBounds(new Rectangle(83, 10, 100, 20));
        L_passwd.setBounds(new Rectangle(10, 45, 70, 20));
        passwd.setBounds(new Rectangle(83, 45, 100, 20));
        L_host.setBounds(new Rectangle(10, 80, 70, 20));
        host.setBounds(new Rectangle(83, 80, 100, 20));
        register.setBounds(new Rectangle(10, 115, 60, 20));
        connect.setBounds(new Rectangle(75, 115, 60, 20));
        cancel.setBounds(new Rectangle(140, 115, 60, 20));
        DialogPane.add(L_user);
        DialogPane.add(user);
        DialogPane.add(L_passwd);
        DialogPane.add(passwd);
        DialogPane.add(L_host);
        DialogPane.add(host);
        DialogPane.add(register);
        DialogPane.add(connect);
        DialogPane.add(cancel);
        add(DialogPane);
        app.out_println("登录对话框 1");
        setBounds(5, 5, 240, 230);
        register.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                register_mouseClicked(e);
            }//登录、注册
        });
        connect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                connect_mouseClicked(e);
            }//登录、连接
        });
        cancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cancle_mouseClicked(e);
            }
        });
    }

    //放弃连接服务器
    void cancle_mouseClicked(MouseEvent e) {
        app.closeSocket();
        app.destroy();
        dispose();
        System.exit(0);
    }

    //注册。可判断空、可识别用户名是否已经被注册
    void register_mouseClicked(MouseEvent e) {
        String S_user = user.getText().trim();
        String S_passwd = passwd.getText().trim();
        app.out_println("注册 S_passwd：" + S_passwd);
        if (b_tf(S_user, S_passwd)) { //b_tf()字符串是否为空？不空返回 true
            if (app.connectHost(host.getText())) {
                app.sendData("%%%%%" + S_user);//用户名前端系统加"%%%%%"，表示客户端为登录、注册操作
                app.sendData(S_passwd);
                if (app.getData().equals("error"))
                    user.setText("用户名已经被注册，请换一个");
                else {
                    user.setText("注册成功");//"right"
                    app.out_println("用户：" + S_user + "注册成功！");
                }
            } else host.setText("不能连接服务器");
        }//if
    }//register_mouseClicked

    //登录、连接
    void connect_mouseClicked(MouseEvent e) { //连接服务器
        String S_user = user.getText().trim();
        String S_passwd = passwd.getText().trim();
        app.out_println("登录 S_passwd0：" + S_passwd);
        if (b_tf(S_user, S_passwd)) { //b_tf()字符串是否为空？不空返回 true
//app.out_println("S_passwd1："+S_passwd);
            if (app.connectHost(host.getText())) { //连接主机
                app.sendData(S_user); //发送用户名
                app.sendData(S_passwd);
                //app.out_println("S_passwd2："+S_passwd);
                if (app.getData().equals("error1"))//出现异常！
                    user.setText("密码错误或数据库没有连上！");
                else if (app.getData().equals("error2")) {//出现异常！（//有重名或密码错误或数据库没有连上等）
                    user.setText("有重名或以登录！");
                } else { //"right"
                    app.out_println("“" + S_user + "”用户名登录成功！");
                    app.YHM(S_user); //在 lb4 中显示用户名
                    app.login = new LoginClient(app);
                    app.login.start();
                    app.setName("Client-user[" + S_user + "]:");
                    dispose();
                }
            } else {
                host.setText("不能连接主机");
            }
        }//if
    }//connect_mouseClicked

    //字符串是否为空？空返回 false，不空返回 true
    boolean b_tf(String S_u, String S_p) {
        if (S_u.equals("") || S_u.equals("用户名不能为空!")) {
            user.setText("用户名不能为空!");
            return false;
        } else if (S_p.equals("") || S_p.equals("密码不能为空!")) {
            passwd.setText("密码不能为空!");
            return false;
        } else {
            if (!S_u.equals("") && !S_p.equals(""))
                return true;
            else return false;
        }//if
    }//b_tf
}//end ConnectDialog
