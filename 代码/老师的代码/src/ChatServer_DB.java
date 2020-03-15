/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * @author user
 */
///////////////////////////////////////////////
//服务器端程序 //
//多用户聊天室 //
// ChatServer_DB.java //
//是 Application 在单机运行 //
//姓名： //
//20XX 年 XX 月 XX 日 //
///////////////////////////////////////////////
//ChatServer_DB.java -Xlint:unchecked

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;

public class ChatServer_DB extends Thread {
    public final static int PORT = 8002;
    ServerSocket ss;
    java.awt.List user_list;//生成用户列表；
    Vector users;
    Vector connections;
    String username;//当前登录用户
    String passwd; //当前登录用户密码

    public synchronized void removeUser(String S_user, Connection1 con) {
        user_list.remove(S_user);
        users.remove(S_user);
        connections.remove(con);
    }

    public void out_println(String str) {//将跟踪信息显示在 DOS 界面上
        System.out.println(str);
    }

    public ChatServer_DB() {//构造函数初始化服务端界面；
        super("Server");
        try {
            ss = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Frame f = new Frame("服务器");
        user_list = new java.awt.List();
        Button warn = new Button("警告");
        Button delete = new Button("踢除");
        Panel p2 = new Panel();
        p2.add("West", warn);
        p2.add("East", delete);
        warn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
//8、"警告"操作，言论不合法，警告!!,并公布给大家 false,不删除 false
                serverWriter("用户" + user_list.getSelectedItem() + "言论不合法，警告!!!!!", false, null, false);
            }
        });
        delete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String S_user = user_list.getSelectedItem();//选中删除用户
//7、"踢除"("删除")操作，公布给大家 false,并删除!!true，删除者 S_user
                serverWriter("用户" + S_user + "言论不合法，不适合聊天，删除!!", false, S_user,

                        );
//在 serverWriter 中，通过识别"删除!!"来删除 S_user 聊客
            }
        });
        f.add("South", p2);
        f.add("Center", user_list);
        f.setSize(400, 200);
        f.setVisible(true);
        // f.addWindowListener(new closeWin());

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { //f.dispose();
                System.exit(0);
            }
        });

        users = new Vector();
        connections = new Vector();
        start();
        out_println("服务器运行成功！");
    }//public ChatServer_DB()

    public void run() {//用户登录线程启动（主线程启动）
        try {
            while (true) {
                out_println("主线程启动！\n");
                Socket s = ss.accept();//侦听并接受到此套接字的连接
                out_println("侦听启动！\n");
                Connection1 c = new Connection1(s, this);//this 为 f 窗口
                out_println("主线程 c：" + c + "\n");
                ServerTaber_DB js = new ServerTaber_DB();//创建数据库操作对象
                out_println("创建数据库操作对象\n");
                username = c.readData();
                passwd = c.readData();
                out_println("主线程启动！" + username + "\n");
                String str2 = new String();
//注册：用户名前端系统加"%%%%%"，表示客户端为注册操作
                if (username.startsWith("%%%%%")) {
                    String b = username.substring(5);
                    try {//数据库操作，查询此用户密码
                        str2 = js.queryTable("select passwd from userlist where name='" + b + "';");
                        str2 = str2.trim();
                    } catch (Exception e) {
                    }
//如果数据库中没有此用户密码，返回空时则加上"$$$$$$"
//以 username 和 passwd 注册插入更新数据库
                    if (str2.equals("$$$$$$")) {
                        try {//
                            js.insertRec("insert into userlist (name,passwd) values (?,?);", b, passwd);
                        } catch (Exception e) {
                        }
                        c.writeData("right");//告知用户注册成功
                        out_println(username + "注册成功！\n");
                    } else c.writeData("error");
                }//if
//连接：用户名前端系统没有加"%%%%%"，表示客户端为连接操作
                if (!username.startsWith("%%%%%")) {//无重名 break 跳出循环，可以连接
                    while (true) {//
                        try {//
                            str2 = js.queryTable("select passwd from userlist where name='" + username + "';");
                            str2 = str2.trim();
                        } catch (Exception e) {
                        }
                        if (passwd.equals("" + str2) && (!users.contains(username)))
                            break;//跳出循环，可以连接
                        if (!passwd.equals("" + str2))
                            c.writeData("error1");//密码错误或数据库没有连上
                        if (users.contains(username))
                            c.writeData("error2");//有重名或以登录
                        username = c.readData();//循环等候接收新的用户名
                        passwd = c.readData();
                    }//while(true)
                    c.writeData("right");//可以连接
                    c.username = username;
                    c.start();



                    synchronized (users) {//同步实现加载用户
                        users.addElement(username);
                        connections.addElement(c);
                        user_list.add(username);
                    }






//1、"加入"告知大家（你对大家），包括自己
                    //登录成功，信息发布给大家，添加到用户列表中
                    //登录用户 username 告知大家（公聊、不踢除）
                    serverWriter("用户" + username + "加入", false, null, false);//公聊 false
                    out_println("用户：" + username + "登录成功！null");

//2、"加入"，大家对你，将已有用户名加入到你的列表框中
                    //在新用户登录后，列表框中加入已有用户名，不包括自己
                    Enumeration em = users.elements();//已有用户向量
                    while (em.hasMoreElements()) {
                        String str = (String) em.nextElement();
                        if (!str.equals(username)) {//不包括自己，大家对你私聊
                            serverWriter("用户" + str + "加入", true, username, false);//私聊 true
                        }
                    }
                }//if
            }//while(true)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//run

    //str 发送内容；w 私聊 true，公聊 false；S_user 发送对象；
//b_delete 在服务器段不踢除 false，踢除 true
/*serverWriter()方法的功能
 参数：w S_user b_delete 操作事务
"加入"操作，客户端以"用户"和"加入\n"识别
1、 F null F 公聊、告知大家（你对大家，不踢除）,"加入"
2、 T 对象 F 私聊（大家对你，不踢除），"加入"将已有用户名加入列表框中
"退出"操作，客户端以"用户"和"退出"识别
3、 T 对象 F 私聊（对象），"退出"告知私聊对象
4、 F 对象 F 公聊，"退出"告知大家
"聊天"操作，不识别
5、 T 对象 F 私聊，与选择对象聊天
6、 F null F 公聊，与大家聊天、告知大家
"踢除"("删除")操作，客户端以"用户"和"删除"识别
7、 F 对象 T 公聊、"踢除"("删除")，告知大家
 "警告"操作，相当于公聊，不识别
 8、 F null F 公聊、告知大家（你对大家，不踢除）,"警告"
*/
    public synchronized void serverWriter(String str, boolean w, String S_user, boolean b_delete) {
        Enumeration em = connections.elements();//连接线程用户向量枚举
        Connection1 c_delet = null;
        while (em.hasMoreElements()) {//枚举循环
            Connection1 c = (Connection1) em.nextElement();
            if (w) { //私聊 true
                if (c.username.equals(S_user))//匹配私聊对象 S_user，以连接用户线程 c.username
                    c.writeData(str + "\n");
            } else { //公聊 false
//if (!S_user.equals(c.username)||str.indexOf("加入")!=-1 ){//不包括自己
                c.writeData(str + "\n"); //不匹配聊天对象 S_user=null
                out_println("c：" + c + ";S_c:" + c.username + ";S_user:" + S_user + ";str:" + str + "\n");
//}
            }//if
            if (b_delete) {//"删除"操作：匹配删除对象 S_user。不包括"退出"
                if (c.username.equals(S_user)) {//匹配删除对象 S_user
                    c_delet = (Connection1) c;
                }
            }
        }//while
        if (b_delete && c_delet.username.equals(S_user)) {//删除匹配对象 S_user
            removeUser(S_user, c_delet);//在服务器中删除
            try {
                c_delet.dis.close();//c_delet.close();不能用，c_delet 没有传过去
                c_delet.dos.close();
                c_delet.s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out_println("删除！" + S_user + ";c_delet:" + c_delet + "\n");
        }
    }// serverWriter()

    public static void main(String[] args) {
        new ChatServer_DB();
    }
}//ChatServer_DB



//聊天
class Connection1 extends Thread {
    ChatServer_DB cs;
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    public String username;
    String talker = null;
    boolean whisper;

    public Connection1(Socket s, ChatServer_DB cs) {
        this.cs = cs;
        this.s = s;
        try {
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cs.out_println("输入输出流启动！\n");
    }//Connection1()

    public void run() {//登录后聊天线程
        String temp;
        try {
            while (true) {
                temp = readData();//返回聊天内容，同时确定 username、whisper,talker
                if (temp.indexOf("退出") != -1) {
//3、或 4、"退出"操作，接收退出处理(公聊和私聊)
                    cs.serverWriter(temp, whisper, talker, false);//"["+username+"]"+
                    cs.out_println("聊天线程退出：" + username + ";" + temp + "\n");
                    close();//"退出"在此服务器中删除
                } else {
//5、或 6、"聊天"操作,非退出
                    cs.serverWriter("[" + username + "]" + temp, whisper, talker, false);
                    cs.out_println("聊天线程启动：" + username + "\n");
                }
            }//while
        } catch (IOException e2) {
        } finally { //close();
        }
    }//run()

    public void close() {
        cs.removeUser(username, this);
        try {
            dis.close();
            dos.close();
            s.close();
        } catch (IOException e) {
        }
        //cs.serverWriter("用户"+username+"退出",false,null,true);//公聊，告知所有聊客，退出
    }//close()

    //返回聊天内容
    public String readData() throws IOException {
        String temp = dis.readUTF();
        if (temp.charAt(0) == '#') { //私聊，确定 username、whisper,talker
            whisper = true; //私聊
            int kk = temp.indexOf('#', 1);
            talker = temp.substring(1, kk); //私聊对象
            return new String(temp.substring(kk + 1));//聊天内容
        }
        whisper = false;//公聊
        talker = null;
        return new String(temp.substring(1));//聊天内容
    }// readData()

    public void writeData(String str) {
        try {
            dos.writeUTF(str);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//writeData()
}//Connection1
