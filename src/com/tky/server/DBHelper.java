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
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// �������ڸ�ʽ
			String uploadTime = df.format(new Date());
//			Date uploadTime = new Date();
//			String sql = "INSERT INTO VideosInfo(video_creat_time,video_upload_time,video_user_id,video_file_url,video_file_upload_percentage) "
//					+ " VALUES ('"+uploadTime+"', '"+uploadTime+"', '1','" + videoFile.getAbsolutePath() + "', '0') SELECT @@IDENTITY AS Id "; // �������ݵ�sql���
			String sql = "INSERT INTO VideosInfo(video_creat_time,video_upload_time,video_user_id,video_file_url,video_file_upload_percentage) "
					+ " VALUES ('"+uploadTime+"', '"+uploadTime+"', '"+userId+"','" + videoFile.getAbsolutePath() + "', '0')"; 
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			int count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			System.out.println("insert :" + sql+"; and count ="+count);
			if(count > 0){
				String selectSql = "select top 1 id from VideosInfo order by id desc";
				System.out.println("selected :" + sql);
				ResultSet rs = st.executeQuery(selectSql);
				while (rs.next()) { // �ж��Ƿ�����һ������
					inertId = rs.getInt("id");
				}
				//�������������ini�ļ�
				try {
					FileTools.CreateFile(sql);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			conn.close(); // �ر����ݿ�����
			System.out.println("insert :inertId=" + inertId);
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return inertId;
	}
	
	public static int updateVideoInfo(int id){
		conn =JDBCServer.getConnection();
		int count = 0;
		try {
			String sql = "Update VideosInfo set video_file_upload_percentage = '100' where id = '"+id+"'"; // �������ݵ�sql���
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			if(count > 0){
				//�������������ini�ļ�
				try {
					FileTools.CreateFile(sql);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return count;
	}
	
}
