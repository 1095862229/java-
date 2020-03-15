

import JDBC_util.JDBCUTILS;
import sun.rmi.rmic.Names;

import javax.sql.rowset.JdbcRowSet;
import javax.swing.*;
import javax.swing.text.*;
import javax.xml.soap.Text;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ChatClient_f {

    static final String HOST = "127.0.0.1";
    static final int PORT = 8002;


    static private Socket clientSocket;
    public ArrayList<String> MessList = new ArrayList<String>();
//    public static Vector UserVector = new Vector();

    private JPanel panel1;
//    private JTextArea textArea1;
    private JTextField textField1;
    private JButton SureButton;
    private JButton cancelButton;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JLabel label;
    private JTextPane textPane1;
    private Document docs;
    private String Username;
    private SimpleAttributeSet attrset;
    private SimpleAttributeSet rootAttrset;
    private SimpleAttributeSet myAttrset;
    private String[] color = {"green","yellow","black","orange","pink"};

    private void init(){
        label.setText("当前用户:"+Username);

        JFrame frame = new JFrame("ChatClient");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);//设置不可拉伸
        frame.setLocationRelativeTo(null);//设置屏幕居中
        frame.setSize(550,500);
        docs = textPane1.getDocument();

        //设置字体颜色
        rootAttrset = new SimpleAttributeSet();
        StyleConstants.setForeground(rootAttrset,Color.red);

        myAttrset = new SimpleAttributeSet();
        StyleConstants.setForeground(myAttrset,Color.blue);

        comboBox1.addItem("大家");
        textPane1.setEditable(false);

        attrset = new SimpleAttributeSet();

        panel1.setSize(500,400);

        for (String col : color){
            comboBox2.addItem(col);
        }


    }



    public ChatClient_f(String Usernames){
        Username = Usernames;
        init();
        try {
            clientSocket = new Socket(HOST,PORT);
//            UserVector.addElement(Username);
            setName(Username);


            // 接收服务器端发送过来的信息的线程启动
            ExecutorService exec = Executors.newCachedThreadPool();
            exec.execute(new ListenrServser());

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("连接账户失败");
            JOptionPane.showMessageDialog(null, "服务器连接失败，请确定服务器已经开启", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        //登陆用户




        //确定按钮的事件
        SureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mess = textField1.getText();
                if (mess.equals("")){
                    JOptionPane.showMessageDialog(null, "发送消息不可为空", "错误", JOptionPane.ERROR_MESSAGE);
                }else {
                    //User:[在线]
                    String who = comboBox1.getSelectedItem().toString();
                    if (who.startsWith("[在线]")) {
                        who = who.substring("[在线]".length());
                    }

                    if(!who.equals("大家")){
                        mess = "@"+who+":"+mess;
                        start(mess);
                        textField1.setText("");
                    }else{
                        start(mess);
                        textField1.setText("");
                    }
                }
            }
        });


        //关闭按钮的事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        //线程更新聊天界面
//        new Thread(new Runnable() {
//            public void run() {
//                ShowMessage();
//            }
//        }).start();

        comboBox2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                String selectColor = comboBox2.getSelectedItem().toString();
                System.out.println(selectColor);
                switch (selectColor){

                    //{"red","blue","green","yellow","black","orange","pink"};
                    case "green":
                        StyleConstants.setForeground(attrset,Color.green);
                        break;
                    case "yellow":
                        StyleConstants.setForeground(attrset,Color.yellow);
                        break;
                    case "black":
                        StyleConstants.setForeground(attrset,Color.black);
                        break;
                    case "orange":
                        StyleConstants.setForeground(attrset,Color.orange);
                        break;
                    case "pink":
                        StyleConstants.setForeground(attrset,Color.pink);
                        break;
                    default:
                        break;
                }
            }
        });


    }

    public void start(String name) {
        try {

            // 建立输出流，给服务端发信息
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
            pw.println(name);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    class ListenrServser implements Runnable{
        @Override
        public void run() {
            try {
                //获取所有信息
                Connection conn = JDBCUTILS.getConnection();
                String sql = "SELECT (Uname) FROM UserInformations WHERE Ulogining = FALSE ";
                PreparedStatement pre = conn.prepareStatement(sql);
                ResultSet ret= pre.executeQuery();
                while (ret.next()){
                    if (!ret.getString("Uname").equals(Username)) {
                        comboBox1.addItem(ret.getString("Uname"));
                    }
                }

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                String msgString;
                while((msgString = br.readLine())!= null) {
                    //警告信息
                        if (msgString.startsWith("@")){
                        String action = msgString.substring(1,msgString.length());

                        if (action.equals("GETOFF")){
                            JOptionPane.showMessageDialog(null, "您已经被勒令退出", "错误", JOptionPane.ERROR_MESSAGE);
                            comboBox1.removeItem("[在线]"+Username);
                            comboBox1.addItem(Username);
                            System.exit(1);
                        }else if (action.equals("WARNING")){
                            Object[] options = { "OK", "CANCEL" };
                            JOptionPane.showOptionDialog(null, "您的语言不当，已经被警告", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                        }
                    } else if (msgString.startsWith("[ADD]")){//添加消息
                        int index = msgString.indexOf("]");
                        if(index > 0) {
                            //获取昵称
                            String info = msgString.substring(index+1, msgString.length());
                            System.out.println(info);
                            if (!info.equals(Username)) {
                                comboBox1.addItem("[在线]"+info);
                                comboBox1.removeItem(info);
                            }
                        }
                    }else if(msgString.startsWith("[DELETE]")){
                            int index = msgString.indexOf("]");
                            if(index > 0) {
                                //获取昵称
                                String info = msgString.substring(index+1, msgString.length());
                                System.out.println(info);
                                if (!info.equals(Username)) {
                                    comboBox1.addItem(info);
                                    comboBox1.removeItem("[在线]"+info);
                                }
                            }
                        } else {
                        MessList.add(msgString);
                        ShowMessage(msgString);
                    }
                }


            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void setName(String NAMES) throws Exception {
        String name;
        //创建输出流
        PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"),true);
        //创建输入流
        BufferedReader br = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));

        while(true) {
            name = NAMES;
            if (name.trim().equals("")) {
                System.out.println("昵称不得为空");
            } else {
                pw.println(name);//发送名字
                String pass = br.readLine();
                if (pass != null && (!pass.equals("OK"))) {
                    System.out.println("昵称已经被占用");
                } else {
//                    UserName = name;
                    System.out.println("昵称“"+name+"”已设置成功，可以开始聊天了");

                    break;
                }
            }
        }
    }

    //一直更新聊天界面
    public void ShowMessage(String mess){
        int i = 0;
//        while (true) {
//            System.out.println(MessList.size());
//            for (; i < MessList.size(); i++) {
                try {if (mess.startsWith("[系统通知]")) {
                    docs.insertString(docs.getLength(), mess+"\n" , rootAttrset);
//                    System.exit(1);
                }else if (mess.startsWith("[私聊]"+Username)||mess.startsWith(Username)){
                    //发送为 发送者+[私聊]+发送者+对+收件者说：信息 截取头个发送者
                    if (mess.startsWith(Username)) mess = mess.substring(Username.length());
                    docs.insertString(docs.getLength(), mess+"\n", myAttrset);
//                    System.out.println("个人");
                } else if(mess.startsWith("[全体消息]"+Username)){
                    docs.insertString(docs.getLength(), mess+"\n" , myAttrset);
                }
                else{
                    docs.insertString(docs.getLength(), mess+"\n" , attrset);
//                    System.out.println("all");
                } }catch (BadLocationException e){
                    e.printStackTrace();
                }
//            }
//        }
    }

}
