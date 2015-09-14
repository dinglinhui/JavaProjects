package ssh.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据库连接的 测试类
 * 
 * @author dinglinhui
 * 
 * 
 * **/
public class ConnectionFactory {

	public static Connection getCon() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// 加上字符串编码指定，防止乱码
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/db_pcserver?characterEncoding=utf8",
				"root", "root");
		return connection;
	}

	public static void main(String[] args) throws Exception {

		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/db_pcserver", "root", "root");
		System.out.println(connection);
		connection.close();

	}

}
