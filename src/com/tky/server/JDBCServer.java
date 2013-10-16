package com.tky.server;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCServer {               //������������ӵ�½���ݿ⣬����֮�����˶����ݿ�Ĳ����������Ժ�����ݿ��������ֱ�ӵ������µķ���ʵ��;
	    // ��ʾ�������ݿ���û���
		public final static String USERNAME = "sa";
		// �������ݿ������

		public final static String PASSWORD = "111111";
//		public final static String PASSWORD = "future";

		// �������ݿ��������Ϣ
		public final static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		// ����������ݿ�ĵ�ַ

		public final static String URL = "jdbc:sqlserver://192.168.1.5:1433;DatabaseName=kydJN";
	
		// �������ݿ������
		private static Connection connection;
		// ����sql����ִ�ж���
		private PreparedStatement pstmt;
		// �����ѯ���صĽ������
		private ResultSet resultSet;

		// ���������ݿ������
		public static Connection getConnection() {
			try {
				Class.forName(DRIVER);
				System.out.println("ע�������ɹ�!!");
				connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return connection;
		}
		/**
		 * ��ɶ����ݿ�ı�����ɾ�����޸ĵĲ���
		 * 
		 * @param sql
		 * @param params
		 * @return
		 * @throws SQLException
		 */
		public boolean updateByPreparedStatement(String sql, List<Object> params)
				throws SQLException {
			boolean flag = false;
			int result = -1;// ��ʾ���û�ִ�����ɾ�����޸ĵ�ʱ����Ӱ�����ݿ������
			pstmt = connection.prepareStatement(sql);
			int index = 1;
			if (params != null && !params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(index++, params.get(i));
				}
			}
			result = pstmt.executeUpdate();
			flag = result > 0 ? true : false;
			return flag;
		}

		/**
		 * ��ѯ���ص�����¼
		 * 
		 * @param sql
		 * @param params
		 * @return
		 * @throws SQLException
		 */
		public Map<String, Object> findSimpleResult(String sql, List<Object> params)
				throws SQLException {
			Map<String, Object> map = new HashMap<String, Object>();
			int index = 1;
			pstmt = connection.prepareStatement(sql);
			if (params != null && !params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(index++, params.get(i));
				}
			}
			resultSet = pstmt.executeQuery();// ���ز�ѯ���
			ResultSetMetaData metaData = resultSet.getMetaData();
			int col_len = metaData.getColumnCount();// ����е�����
			while (resultSet.next()) {
				for (int i = 0; i < col_len; i++) {
					String cols_name = metaData.getColumnName(i + 1);
					Object cols_value = resultSet.getObject(cols_name);
					if (cols_value == null) {
						cols_value = "";
					}
					map.put(cols_name, cols_value);
				}
			}
			return map;
		}

		/**
		 * ��ѯ���ض��м�¼
		 * 
		 * @param sql
		 * @param params
		 * @return
		 * @throws SQLException
		 */
		public List<Map<String, Object>> findMoreResult(String sql,
				List<Object> params) throws SQLException {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			int index = 1;
			pstmt = connection.prepareStatement(sql);
			if (params != null && !params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(index++, params.get(i));
				}
			}
			resultSet = pstmt.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int cols_len = metaData.getColumnCount();
			while (resultSet.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = metaData.getColumnName(i + 1);
					Object cols_value = resultSet.getObject(cols_name);
					if (cols_value == null) {
						cols_value = "";
					}
					map.put(cols_name, cols_value);
				}
				list.add(map);
			}
			return list;
		}

		// jdbc�ķ�װ�����÷����������װ��
		public <T> T findSimpleRefResult(String sql, List<Object> params,
				Class<T> cls) throws Exception {
			T resultObject = null;
			int index = 1;
			pstmt = connection.prepareStatement(sql);
			if (params != null && !params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(index++, params.get(i));
				}
			}
			resultSet = pstmt.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int cols_len = metaData.getColumnCount();
			while (resultSet.next()) {
				// ͨ��������ƴ���ʵ��
				resultObject = cls.newInstance();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = metaData.getColumnName(i + 1);
					Object cols_value = resultSet.getObject(cols_name);
					if (cols_value == null) {
						cols_value = "";
					}
					Field field = cls.getDeclaredField(cols_name);
					field.setAccessible(true);// ��javabean�ķ���privateȨ��
					field.set(resultObject, cols_value);
				}
			}
			return resultObject;
		}

		/**
		 * ͨ��������Ʒ������ݿ�
		 * 
		 * @param <T>
		 * @param sql
		 * @param params
		 * @param cls
		 * @return
		 * @throws Exception
		 */
		public <T> List<T> findMoreRefResult(String sql, List<Object> params,
				Class<T> cls) throws Exception {
			List<T> list = new ArrayList<T>();
			int index = 1;
			pstmt = connection.prepareStatement(sql);
			if (params != null && !params.isEmpty()) {
				for (int i = 0; i < params.size(); i++) {
					pstmt.setObject(index++, params.get(i));
				}
			}
			resultSet = pstmt.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int cols_len = metaData.getColumnCount();
			while (resultSet.next()) {
				T resultObject = cls.newInstance();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = metaData.getColumnName(i + 1);
					Object cols_value = resultSet.getObject(cols_name);
					if (cols_value == null) {
						cols_value = "";
					}
					Field field = cls.getDeclaredField(cols_name);
					field.setAccessible(true);
					field.set(resultObject, cols_value);
				}
				list.add(resultObject);
			}
			return list;
		}

		public void releaseConn() {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

}
