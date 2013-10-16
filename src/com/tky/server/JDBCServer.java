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

public class JDBCServer {               //这个类用来连接登陆数据库，并且之后定义了对数据库的操作函数，以后对数据库操作可以直接调用以下的方法实现;
	    // 表示定义数据库的用户名
		public final static String USERNAME = "sa";
		// 定义数据库的密码

		public final static String PASSWORD = "111111";
//		public final static String PASSWORD = "future";

		// 定义数据库的驱动信息
		public final static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		// 定义访问数据库的地址

		public final static String URL = "jdbc:sqlserver://192.168.1.5:1433;DatabaseName=kydJN";
	
		// 定义数据库的链接
		private static Connection connection;
		// 定义sql语句的执行对象
		private PreparedStatement pstmt;
		// 定义查询返回的结果集合
		private ResultSet resultSet;

		// 定义获得数据库的链接
		public static Connection getConnection() {
			try {
				Class.forName(DRIVER);
				System.out.println("注册驱动成功!!");
				connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return connection;
		}
		/**
		 * 完成对数据库的表的添加删除和修改的操作
		 * 
		 * @param sql
		 * @param params
		 * @return
		 * @throws SQLException
		 */
		public boolean updateByPreparedStatement(String sql, List<Object> params)
				throws SQLException {
			boolean flag = false;
			int result = -1;// 表示当用户执行添加删除和修改的时候所影响数据库的行数
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
		 * 查询返回单条记录
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
			resultSet = pstmt.executeQuery();// 返回查询结果
			ResultSetMetaData metaData = resultSet.getMetaData();
			int col_len = metaData.getColumnCount();// 获得列的名称
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
		 * 查询返回多行记录
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

		// jdbc的封装可以用反射机制来封装：
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
				// 通过反射机制创建实例
				resultObject = cls.newInstance();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = metaData.getColumnName(i + 1);
					Object cols_value = resultSet.getObject(cols_name);
					if (cols_value == null) {
						cols_value = "";
					}
					Field field = cls.getDeclaredField(cols_name);
					field.setAccessible(true);// 打开javabean的访问private权限
					field.set(resultObject, cols_value);
				}
			}
			return resultObject;
		}

		/**
		 * 通过反射机制访问数据库
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
