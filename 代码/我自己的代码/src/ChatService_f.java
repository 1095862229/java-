//MADE BY 李家年

import JDBC_util.JDBCUTILS;

import javax.swing.*;
import javax.xml.transform.Source;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatService_f extends Thread{
    final public static String GETOFF = "@GETOFF";
    final public static String WARNING = "@WARNING";

    java.awt.List user_list;//生成用户列表；

    //连接数据库
    Connection conn = JDBCUTILS.getConnection();

    PreparedStatement pre;

    //存放sql语句
    String sql;

    //线程池，防止大量消耗资源
    private ExecutorService exec;

    // 存放客户端之间私聊的信息
    private Map<String,PrintWriter> storeInfo;

    //联系套接字
    private ServerSocket serverSocket;

    private Button warn;
    private Button delete;

    //在线人数
//    Vector UserList = new Vector();

    //动态添加Jlist
    DefaultListModel<String> model=new DefaultListModel<>();

    public static void main(String[] args) {
        ChatService_f chatService_f = new ChatService_f(8002);
        chatService_f.start();
    }

    //界面初始化
    private void init(){
        Frame f = new Frame("服务器");
        user_list = new java.awt.List();
        warn = new Button("警告");
        delete = new Button("踢除");
        Panel p2 = new Panel();
        p2.add("West", warn);
        p2.add("East", delete);
        f.add("South", p2);
        f.add("Center", user_list);
        f.setSize(400, 200);
        f.setVisible(true);

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { //f.dispose();
                System.exit(0);
            }
        });
    }

    public ChatService_f(int port){

        init();


        try {
            //每次服务器重制时，将所有登陆状态置于FALSE
            Connection conn = JDBCUTILS.getConnection();
            String sql = "UPDATE UserInformations SET Ulogining=FALSE;";
            PreparedStatement pre = conn.prepareStatement(sql);
            pre.executeUpdate();

            serverSocket = new ServerSocket(port);
            storeInfo = new HashMap<String, PrintWriter>();
            exec = Executors.newCachedThreadPool();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("服务器初始化失败");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库初始化失败");
        }

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = user_list.getSelectedItem().toString();
                sendToSomeone("",name,GETOFF,false);
//                System.out.println(name);
            }
        });

        warn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = user_list.getSelectedItem().toString();
                sendToSomeone("",name,WARNING,false);
            }
        });
    }

    // 将客户端的信息以Map形式存入集合中
    private void putIn(String key,PrintWriter value) {
        synchronized(this) {
            storeInfo.put(key, value);
        }
    }

    // 将给定的消息转发给所有客户端
    private synchronized void sendToAll(String message) {
        for(PrintWriter out: storeInfo.values()) {
            out.println(message);
        }
    }

    // 将给定的消息转发给私聊的客户端
    private synchronized void sendToSomeone(String fromName ,String toName,String message,boolean getBack) {

        PrintWriter pw = storeInfo.get(toName); //将对应客户端的聊天信息取出作为私聊内容发送出去
        if(pw != null) pw.println(message);
        if (getBack) {
            pw = storeInfo.get(fromName);//并且反馈到自己的聊天窗口
            if (pw != null) pw.println(message);
        }
    }


    public void start() {
        try {
            while(true) {
                System.out.println("等待客户端连接... ... ");
                Socket socket = serverSocket.accept();

                // 获取客户端的ip地址
                InetAddress address = socket.getInetAddress();
                System.out.println("客户端：“" + address.getHostAddress() + "”连接成功！ ");
                /*
                 * 启动一个线程，由线程来处理客户端的请求，这样可以再次监听
                 * 下一个客户端的连接
                 */

                exec.execute(new ListenrClient(socket)); //通过线程池来分配线程
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 该线程体用来处理给定的某一个客户端的消息，循环接收客户端发送
     * 的每一个字符串，并输出到控制台
     */
    class ListenrClient implements Runnable {

        private Socket socket;
        private String name;

        public ListenrClient(Socket socket) {
            this.socket = socket;
        }

        // 创建内部类来获取昵称
        private String getName() throws Exception {
            try {
                //服务端的输入流读取客户端发送来的昵称输出流
                BufferedReader bReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                //服务端将昵称验证结果通过自身的输出流发送给客户端
                PrintWriter ipw = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"),true);

                //读取客户端发来的昵称
                while(true) {
                    String nameString = bReader.readLine();
                    if ((nameString.trim().length() == 0) || storeInfo.containsKey(nameString)) {
                        ipw.println("FAIL");
                    } else {
                        ipw.println("OK");
                        return nameString;
                    }
                }
            } catch(Exception e) {
                throw e;
            }
        }

        /*
         * 查找离线消息
         */
        private void SendOffLine(){
            try {
                //遍历读出消息
                sql = "SELECT * FROM UserMessage WHERE toName = ? AND Uread = FALSE ;";
                pre = conn.prepareStatement(sql);
                pre.setObject(1,name);
                ResultSet ret= pre.executeQuery();
                while (ret.next()){
                    String fromname = ret.getString("fromName");
                    String info = ret.getString("message");
                    int index = ret.getInt("id");
                    info = "[离线消息]"+info;
                    sendToSomeone(fromname,name,info,false);
                }

                //将消息设置为已读
                sql = "UPDATE UserMessage set Uread=TRUE WHERE toName=?;";
                pre = conn.prepareStatement(sql);
                pre.setObject(1,name);
                pre.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                /*
                 * 通过客户端的Socket获取客户端的输出流
                 * 用来将消息发送给客户端
                 */

                name = getName();


                PrintWriter pw = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                /*
                 * 将客户昵称和其所说的内容存入共享集合HashMap中
                 */

                user_list.add(name); //在线人员
                putIn(name, pw);

                /*
                 * 查找离线消息
                 */
                SendOffLine();




                Thread.sleep(100);


                // 服务端通知所有客户端，某用户上线
                sendToAll("[系统通知] “" + name + "”已上线");

                //将原有的人发给新用户
                for (String key : storeInfo.keySet()){
                    if (!key.equals(name)) {
                        sendToSomeone("",name, "[ADD]" + key,false);
                    }
                }
                //将新的用户发给所有人
                sendToAll("[ADD]"+name);


                /*
                 * 通过客户端的Socket获取输入流
                 * 读取客户端发送来的信息
                 */


                BufferedReader bReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String msgString = null;




                while((msgString = bReader.readLine()) != null) {
                    System.out.println(msgString);
                    // 检验是否为私聊（格式：@昵称:内容）
                    if(msgString.startsWith("@")) {
                        int index = msgString.indexOf(":");
                        if(index >= 0) {
                            //获取昵称
                            String theName = msgString.substring(1, index);
                            String info = msgString.substring(index+1, msgString.length());

                            //判断用户是否在线
                            if (storeInfo.containsKey(theName)){//在线
                                //将数据存入数据库(已读)
                                sql = "INSERT INTO UserMessage(fromName, toName, message,Uread) VALUES (?,?,?,True);";
                                pre = conn.prepareStatement(sql);
                                pre.setObject(1,name);
                                pre.setObject(2,theName);
                                pre.setObject(3,info);

                                pre.executeUpdate();
                                //发送为 发送者+[私聊]+发送者+对+收件者说：信息
                                info="[私聊]"+name+"对"+theName+"说:"+info;

                                //将私聊信息发送出去
                                sendToSomeone(name,theName,info,true);
                                continue;
                            }else{//不在线
                                //将数据存入数据库
                                sql = "INSERT INTO UserMessage(fromName, toName, message,Uread) VALUES (?,?,?,FALSE);";
                                pre = conn.prepareStatement(sql);
                                pre.setObject(1,name);
                                pre.setObject(2,theName);
                                pre.setObject(3,info);

                                pre.executeUpdate();

                                info="[私聊]"+name+"对"+theName+"说:"+info;
                                sendToSomeone("",name,info,false);
                                continue;
                            }
                        }
                    }
                    // 遍历所有输出流，将该客户端发送的信息转发给所有客户端
                    System.out.println(name+":"+ msgString);
                    sendToAll("[全体消息]"+name+":"+ msgString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //放置数据库的登陆信息
                //下线时，将Ulogging信息放置false
                user_list.remove(name);

                Connection conn = JDBCUTILS.getConnection();
                String sql = "UPDATE UserInformations SET Ulogining=FALSE WHERE Uname =?;";
                PreparedStatement pre = null;
                try {
                    pre = conn.prepareStatement(sql);
                    pre.setObject(1,name);
                    pre.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("数据库错误");
                }



                // 通知所有客户端，某某客户已经下线
                sendToAll("[系统通知] "+name + "已经下线了。");
                sendToAll("[DELETE]"+name);
                storeInfo.remove(name);

                if(socket!=null) {
                    try {
                        socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
