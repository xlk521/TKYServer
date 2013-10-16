package com.tky.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBHelper {

	private static Connection conn;
	private static Statement st;
	
	public static FileLog getUploadFile(int id){
		conn =  JDBCServer.getConnection();
		FileLog myFilelog = null;
		try {
			String sql = "select video_file_url from VideosInfo where id="+id+"\"";
			System.out.println("selected url = "+sql);
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if(!rs.wasNull()){
				myFilelog.setId(rs.getInt("id"));
				myFilelog.setPath(rs.getString("video_file_url"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myFilelog;
	}
	
	public static int insertVideoInfo(File videoFile,String userId){
		conn = JDBCServer.getConnection();
		int inertId = -1;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			String uploadTime = df.format(new Date());
//			Date uploadTime = new Date();
//			String sql = "INSERT INTO VideosInfo(video_creat_time,video_upload_time,video_user_id,video_file_url,video_file_upload_percentage) "
//					+ " VALUES ('"+uploadTime+"', '"+uploadTime+"', '1','" + videoFile.getAbsolutePath() + "', '0') SELECT @@IDENTITY AS Id "; // 插入数据的sql语句
			String sql = "INSERT INTO VideosInfo(video_creat_time,video_upload_time,video_user_id,video_file_url,video_file_upload_percentage) "
					+ " VALUES ('"+uploadTime+"', '"+uploadTime+"', '"+userId+"','" + videoFile.getAbsolutePath() + "', '0')"; 
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			int count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			System.out.println("insert :" + sql+"; and count ="+count);
			if(count > 0){
				String selectSql = "select top 1 id from VideosInfo order by id desc";
				System.out.println("selected :" + sql);
				ResultSet rs = st.executeQuery(selectSql);
				while (rs.next()) { // 判断是否还有下一个数据
					inertId = rs.getInt("id");
				}
				//将插入语句生成ini文件
				try {
					FileTools.CreateFile(sql);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			conn.close(); // 关闭数据库连接
			System.out.println("insert :inertId=" + inertId);
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
		return inertId;
	}
	
	public static int updateVideoInfo(int id){
		conn =JDBCServer.getConnection();
		int count = 0;
		try {
			String sql = "Update VideosInfo set video_file_upload_percentage = '100' where id = '"+id+"'"; // 更新数据的sql语句
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			if(count > 0){
				//将插入语句生成ini文件
				try {
					FileTools.CreateFile(sql);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("更新数据失败" + e.getMessage());
		}
		return count;
	}
	
}
