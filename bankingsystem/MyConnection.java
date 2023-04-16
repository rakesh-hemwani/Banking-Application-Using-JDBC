package bankingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    static Connection connection;
    public static Connection getConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "root");
        }
        catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connection Failed");
        }
        return connection;
    }
}
