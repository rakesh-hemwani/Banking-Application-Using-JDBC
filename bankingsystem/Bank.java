package bankingsystem;

import java.sql.*;
import java.util.Scanner;

public class Bank {

    private static long STARTING_ACCOUNT_NUMBER = 52029;
    private static PreparedStatement psmt;

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        Connection connection = MyConnection.getConnection();
        int accountNumber;
        String password;
        int securityPin;
        int result;

        System.out.println("......................Banking Application.................");

        while (true) {
            System.out.println("1. Open Your Account");
            System.out.println("2. Withdraw Amount");
            System.out.println("3. Deposit Amount");
            System.out.println("4. Transfer Amount to Another Account");
            System.out.println("5. Show Account Balance");
            System.out.println("6. Close Your Account");
            System.out.println("7. Exit");

            Scanner sc = new Scanner(System.in);
            int options = sc.nextInt();
            switch (options) {
                case 1:
                    sc.nextLine();
                    System.out.println("Enter Your Name");
                    String accountHolderName = sc.nextLine();
                    System.out.println("Enter Password for your account");
                    password = sc.next();
                    System.out.println("Enter Security Pin For Your Account");
                    securityPin= sc.nextInt();
                    System.out.println("Enter Initial Deposit Amount \n Note : Minimum Balance required to open account is 2500 ");
                    int accountBalance = sc.nextInt();
                    if (accountBalance < 2500) {
                        System.out.println("Minimum Balance Not Fulfilled ......");
                    } else {
                        result = CreateAccount(accountHolderName, accountBalance, password,securityPin,connection);
                        if (result > 0){
                            System.out.println("Account Created Successfully");
                            psmt=connection.prepareStatement("select * FROM ACCOUNT_INFO ORDER BY ACCOUNT_NUMBER DESC LIMIT 1");
                            ResultSet set=psmt.executeQuery();
                            while (set.next()){
                                System.out.println("Your Account Number is :" + set.getInt(1));
                            }
                        }
                        else System.out.println("Some Error Occurred.............");
                    }
                    break;
                case 2:
                    System.out.println("Enter Your Account Number");
                    accountNumber = sc.nextInt();
                    System.out.println("Enter Password");
                    password = sc.next();
                    System.out.println("Enter amount you want to withdraw");
                    int withdrawAmount = sc.nextInt();
                    if (withdrawAmount < getBalance(accountNumber, password, connection)) {
                        System.out.println("Enter Security Pin To Complete Transaction");
                        securityPin = sc.nextInt();
                        result = withdraw(accountNumber, password, securityPin, withdrawAmount, connection);
                        if (result > 0) {
                            System.out.println("Transaction Completed Now Your Balance is RS: " + getBalance(accountNumber, password, connection));
                        } else System.out.println("Transcation Failed");
                    } else {
                        System.out.println("Insufficient Balance............");
                    }
                    break;
                case 3:
                    System.out.println("Enter Your Account Number");
                    accountNumber = sc.nextInt();
                    System.out.println("Enter Password");
                    password = sc.next();
                    System.out.println("Enter amount you want to deposit");
                    int depositAmount = sc.nextInt();
                    System.out.println("Enter Security Pin To Complete Transaction");
                    securityPin = sc.nextInt();
                    result = deposit(accountNumber, password, securityPin, depositAmount, connection);
                    if (result > 0) {
                        System.out.println("Transaction Completed Now Your Balance is RS: " + getBalance(accountNumber, password, connection));
                    } else System.out.println("Transcation Failed");
                    break;
                case 4:
                    System.out.println("Transfer Amount to Another Account");
                    System.out.println("Enter Your Account Number");
                    accountNumber = sc.nextInt();
                    System.out.println("Enter Password");
                    password = sc.next();
                    System.out.println("Enter amount you want to transfer");
                    int amount = sc.nextInt();
                    System.out.println("Enter Account Number to whom you have to tranfer amount");
                    int reciverAccountNumber=sc.nextInt();
                    System.out.println("Enter Security Pin To Complete Transaction");
                    securityPin = sc.nextInt();
                    if (amount < getBalance(accountNumber, password, connection)) {
                        result = transfer(accountNumber, password, securityPin, amount,reciverAccountNumber,connection);
                        if (result > 0) {
                            System.out.println("Transaction Completed Now Your Balance is RS: " + getBalance(accountNumber, password, connection));
                        } else System.out.println("Transcation Failed");
                    } else {
                        System.out.println("Insufficient Balance............");
                    }
                    break;
                case 5:
                    System.out.println("Show Balance .........");
                    System.out.println("Enter Your Account Number");
                    accountNumber = sc.nextInt();
                    System.out.println("Enter Password");
                    password = sc.next();
                    System.out.println(getBalance(accountNumber,password,connection));
                    break;
                case 6:
                    System.out.println("Close your Account");
                    System.out.println("Enter Your Account Number");
                    accountNumber = sc.nextInt();
                    System.out.println("Enter Password");
                    password = sc.next();

                    result=deleteAccount(accountNumber,password,connection);
                    if(result>0){
                        System.out.println("Account Successfully Closed "+ accountNumber+"\n Money Handed Over To Person");
                    }
                    else System.out.println("Some Error Occurred Try Again");
                    break;
                default:
                    System.out.println("Thank You for visiting our bank");
                    connection.close();
                    sc.close();
                    System.exit(0);
                    break;
        }
    }
}

    private static int deleteAccount(int accountNumber, String password, Connection connection) throws SQLException {
        psmt=connection.prepareStatement("delete from ACCOUNT_INFO where accountNumber=? AND password=?");
        psmt.setInt(1,accountNumber);
        psmt.setString(2,password);
        return psmt.executeUpdate();
    }

    private static int transfer(int accountNumber, String password, int securityPin, int amount, int reciverAccountNumber, Connection connection) throws SQLException {
        try{
            connection.setAutoCommit(false);
            connection.setSavepoint();
            int result=withdraw(accountNumber,password,securityPin,amount,connection);
            if(result>0){
                System.out.println("Amount Debited From "+ accountNumber);
                psmt=connection.prepareStatement("update ACCOUNT_INFO set balance=balance+? where ACCOUNT_NUMBER=?");
                psmt.setInt(1,amount);
                psmt.setInt(2,reciverAccountNumber);
                int result2=psmt.executeUpdate();
                if (result2>0){
                    System.out.println("Amount credited to "+reciverAccountNumber );
                    connection.commit();
                    connection.setAutoCommit(true);
                    return 1;
                }
                else {
                    System.out.println("Transaction Failed");
                    return -1;
                }
            }
            else {
                System.out.println("Transaction Failed");
                return -1;
            }
        }catch (SQLException e){
            System.out.println("Transaction Failed");
            connection.rollback();
            e.printStackTrace();
        }
        return 1;
    }

    private static int deposit(int accountNumber, String password, int securityPin, int depositAmount, Connection connection) throws SQLException {
        psmt = connection.prepareStatement("update ACCOUNT_INFO set balance=balance + ? where ACCOUNT_NUMBER=?" +
                                               " and password=? and security_pin=?");
        psmt.setInt(1, depositAmount);
        psmt.setInt(2, accountNumber);
        psmt.setString(3, password);
        psmt.setInt(4, securityPin);
        return psmt.executeUpdate();
    }

    private static int withdraw(int accountNumber, String password, int securityPin, int amount, Connection connection) throws SQLException {
        psmt = connection.prepareStatement("update ACCOUNT_INFO set balance=balance - ? where ACCOUNT_NUMBER=?" +
                                               " and password=? and security_pin=?");
        psmt.setInt(1, amount);
        psmt.setInt(2, accountNumber);
        psmt.setString(3, password);
        psmt.setInt(4, securityPin);
        return psmt.executeUpdate();
    }

    private static int getBalance(int accountNumber, String password, Connection connection) throws SQLException {
        int balance = -1;
        psmt = connection.prepareStatement("select balance from ACCOUNT_INFO where ACCOUNT_NUMBER=? AND password=?");
        psmt.setInt(1, accountNumber);
        psmt.setString(2, password);
        ResultSet result = psmt.executeQuery();
        if (result.next()) {
            balance = result.getInt("balance");
        } else {
            System.out.println("Invalid Credentials");
        }
        return balance;
    }

    private static int CreateAccount(String accountHolderName, int accountBalance,String password, int securityPin, Connection connection) throws SQLException {
        psmt = connection.prepareStatement("Insert into ACCOUNT_INFO(ACCOUNT_HOLDER_NAME,balance,security_pin,password) values (?,?,?,?)");
        psmt.setString(1, accountHolderName);
        psmt.setInt(2, accountBalance);
        psmt.setInt(3,securityPin);
        psmt.setString(4,password);
        STARTING_ACCOUNT_NUMBER++;
        return psmt.executeUpdate();
    }
}
