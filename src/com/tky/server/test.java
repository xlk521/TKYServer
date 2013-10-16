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
					"sa", "111111");// ������������
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

			while (rs.next()) { // �ж��Ƿ�����һ������
				System.out.println( rs.getString("RuleName"));
				break;
			}
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("��ѯ����ʧ��");
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
					+ dateFormat.format(new Date()) + "')"; // �������ݵ�sql���

			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			int count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
	}

	public static JSONObject query_Login(String account, String pw) {
		conn = getConnection(); // ͬ����Ҫ��ȡ���ӣ������ӵ����ݿ�
		JSONObject jsonObject = null;
		try {
			String sql = "select * from JcEmployee where E_name='" + account
					+ "' and Password='" + pw + "'"; // ��ѯ���ݵ�sql���
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) { // �ж��Ƿ�����һ������
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
			conn.close(); // �ر����ݿ�����

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("��ѯ����ʧ��");
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
					+ dateFormat.format(new Date()) + "')"; // �������ݵ�sql���

			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return count;
	}
}
