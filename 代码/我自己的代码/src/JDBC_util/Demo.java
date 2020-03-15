package JDBC_util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Demo {
    public static void main(String[] args) throws SQLException {
        Connection con = JDBCUTILS.getConnection();

        PreparedStatement pst = con.prepareStatement("SELECT * FROM UserInformations;");

//        System.out.println(pst);
        ResultSet rs = pst.executeQuery();
        while(rs.next()){
            System.out.println(rs.getString("id"));
        }
    }
}
