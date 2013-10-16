package com.tky.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class test {
	static Connection conn;
	static Statement st;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		queryRegualation();
		insertPhotoPath("c:/ddd","dd","ddd");
	}

	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			conn = DriverManager.getConnection(
					"jdbc:sqlserver://ICT-LY:1433;databaseName=kydjn",
					"sa", "111111");// 创建数据连接
		} catch (Exception e) {
		}
		return conn;
	}

	public static JSONObject queryRegualation() {
		conn = getConnection();
		JSONObject jsonObject = null;
		try {
			String sql = "select * from GwRulesAndroid ;";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) { // 判断是否还有下一个数据
				System.out.println( rs.getString("RuleName"));
				break;
			}
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("查询数据失败");
		} 
		return jsonObject;
	}

	public static void insertTelegraph(String author, String authorId,
			String SendUnit, String title, String teleMessage) {
		conn = getConnection();
		try {
			String sql = "INSERT INTO telegraph(Author,AuthorID,SendUnit,Title,TeleMessage,SendDateTime)"
					+ " VALUES ('"
					+ author
					+ "', '"
					+ authorId
					+ "', '"
					+ SendUnit
					+ "', '"
					+ title
					+ "', '"
					+ teleMessage
					+ "', '"
					+ dateFormat.format(new Date()) + "')"; // 插入数据的sql语句

			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			int count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
	}

	public static JSONObject query_Login(String account, String pw) {
		conn = getConnection(); // 同样先要获取连接，即连接到数据库
		JSONObject jsonObject = null;
		try {
			String sql = "select * from JcEmployee where E_name='" + account
					+ "' and Password='" + pw + "'"; // 查询数据的sql语句
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) { // 判断是否还有下一个数据
				jsonObject = new JSONObject();
				jsonObject.put(JSONKey.USER_NAME, rs.getString("E_Name"));
				jsonObject.put(JSONKey.USER_PASSWORD, rs.getString("Password"));
				jsonObject.put(JSONKey.USER_EID, rs.getString("E_ID"));
				jsonObject.put(JSONKey.USER_TYPE, rs.getString("Type"));
				jsonObject.put(JSONKey.USER_SEX, rs.getString("E_Sex"));
				jsonObject.put(JSONKey.USER_POSITION,
						rs.getString("E_Position"));
				jsonObject.put(JSONKey.USER_GROUPNAME,
						rs.getString("GroupName"));
				jsonObject.put(JSONKey.USER_TEAMNAME, rs.getString("TeamName"));
				jsonObject.put(JSONKey.USER_BUREAUNAME,
						rs.getString("BureauName"));
				jsonObject.put(JSONKey.USER_DEPTNAME, rs.getString("Deptname"));
				break;
			}
			conn.close(); // 关闭数据库连接

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("查询数据失败");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	public static int insertPhotoPath(String address, String filename,
			String message) {
		conn = getConnection();
		int count = 0;
		try {
			String sql = "INSERT INTO photos(address,filename,message,datatime)"
					+ " VALUES ('"
					+ address
					+ "', '"
					+ filename
					+ "', '"
					+ message
					+ "', '"
					+ dateFormat.format(new Date()) + "')"; // 插入数据的sql语句

			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
		return count;
	}
}
