/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * @author user
 */

import java.sql.*;

public class ServerTaber_DB {
    Statement stmt = null;
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String URL;

    //连接数据库源
    public ServerTaber_DB() {
//userlist_DB数据源事先已建成（邦定Access 数据库） 
        URL = "jdbc:odbc:userlist_DB";
//1、加载驱动程序，同时向 DriverManager注册，DriverManager为数据库查询驱动程序对象的类 
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return;
        }
//2、连接数据库源 
        try {
            con = (Connection) DriverManager.getConnection(URL);
//3、创建发送SQL语句的Statement对象 
            stmt = con.createStatement();
        } catch (Exception e) {
            System.err.println("problems connecting to" + URL);
        }
    }//end ServerTaber_DB()


    //创建表单
    public void createTable(String sql) {  //4、执行SQL语句
        try {
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("createTabele:" + e);
        }
    }

    //插入记录
    public void insertRec(String sql, String name, String
            passwd) {
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, passwd);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("queryTable:" + e);
        }
    }

    //条件查询
    public String queryTable(String sql) throws SQLException {
        try {
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("queryTable:" + e);
        }
        return (dispResultSet(rs));//  调用 dispResultSet(rs)方法显示结果
    }

    //删除记录
    public void deleteRec(String sql) throws
            SQLException {
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.out.println("detele:" + e);
        }
    }

    //显示结果
    private String dispResultSet(ResultSet rs) throws SQLException {
        int i;
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCols = rsmd.getColumnCount();   //字段数或多少列数
        if (rs.next() == false) // 当 前 行 不 存 在 返 回"$$$$$$"，可能是用户名或密码
            return ("$$$$$$");
        else {
            String str = rs.getString(numCols);//返回当前行中的列值
            return (str);
        }
    }
}