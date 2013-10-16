package com.tky.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONProcess {                                //这个类写入了所有对数据库的操作方法；
	private static Connection conn;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	private static Statement st;
	private static JDBCServer jdbcServer=new JDBCServer();
	public static boolean insertTelegraph(String author, String SignerId,            //此方法用来执行报文发送操作;
			String title, String teleMessage, String SendUnit, String copyUnit) {
		JDBCServer jdbcServer=new JDBCServer();
		conn = JDBCServer.getConnection();
		boolean count =true;
		try {
			String sql = "INSERT INTO telegraph(Signer,SignerID,SendUnit,Title,TeleMessage,SendDateTime,CopyUnit) values(?,?,?,?,?,?,?)";
			List<Object> params=new ArrayList<Object>();
			params.add(author);
			params.add(SignerId);
			params.add(SendUnit);
			params.add(title);
			params.add(teleMessage);
			params.add(dateFormat.format(new Date()));
			params.add(copyUnit);
			count=jdbcServer.updateByPreparedStatement(sql, params);
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
		jdbcServer.releaseConn();
		return count;
	}
	
	public static int insertShortmessage( String msg,                 //此方法用于在插入短信操作;
			String tpa, String devon, String smstype ,String sign) {
		conn = JDBCServer.getConnection();
		int count = 0;
		try {
			String sql = "INSERT INTO tb_rcvtmp(smid,msg,tpa,recvdatetime,devno,smstype,sign)"
					+ " VALUES ("
					+ "(select isnull(MAX(smid),0)+1 from tb_rcvtmp)"
					+ ", '"
					+ msg
					+ "', '"
					+ tpa
					+ "', '"
					+ dateFormat.format(new Date()) +
					"', '"
					+ devon
					+ "', '"
					+ smstype 
					+ "', '"
					+ sign + "')"; // 插入数据的sql语句

			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
		return count;
	}

	public static JSONObject queryRegualation() {                 //用于规章制度查询;
		conn =JDBCServer.getConnection();
		JSONObject j = null;
		try {
			String sql = "select * from GwRulesAndroid where Type='Z' or Type ='T'";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			JSONArray jsonArray = new JSONArray();
			while (rs.next()) { // 判断是否还有下一个数据
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(JSONKey.RULE_ID, rs.getString("ID"));
				jsonObject.put(JSONKey.RULE_RULECHAPTERNAME,rs.getString("RuleChapterName"));
				jsonObject.put(JSONKey.RULE_RULECHAPTERKEYWORDS,rs.getString("RuleChapterKeyWords"));
				jsonObject.put(JSONKey.RULE_PARENTID, rs.getString("ParentID"));
				jsonObject.put(JSONKey.RULE_INDEXID, rs.getString("IndexID"));
				jsonObject.put(JSONKey.RULE_DIRECTORYLEVELID,rs.getString("DirectoryLevelID"));
				jsonObject.put(JSONKey.RULE_RULENAME, rs.getString("RuleName"));
				jsonObject.put(JSONKey.RULE_RULETABLEID,rs.getString("RuleTableID"));
				jsonObject.put(JSONKey.RULE_NEWCHAPTERURL,rs.getString("NewChapterURL"));
				jsonObject.put(JSONKey.RULE_NEWCHAPTERNAME,rs.getString("NewChapterName"));
				jsonObject.put(JSONKey.RULE_DELCHAPTERSIGN,rs.getString("DelChapterSign"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERSIGN,rs.getString("UpdateChapterSign"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERABSTRACT,rs.getString("UpdateChapterAbstract"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERDATETIME,rs.getString("UpdateChapterDateTime"));
				jsonObject.put(JSONKey.RULE_POSITIONID,rs.getString("PositionID"));

				jsonArray.put(jsonObject);
			}
			j = new JSONObject();
			j.put(JSONKey.RULE_RULES, jsonArray);
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("查询数据失败");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j;
	}

	public static JSONObject query_Login(String account, String pw) {       //用于前台登陆功能实现;
		conn = JDBCServer.getConnection(); // 同样先要获取连接，即连接到数据库
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
				jsonObject.put(JSONKey.USER_POSITION,rs.getString("E_Position"));
				jsonObject.put(JSONKey.USER_GROUPNAME,rs.getString("GroupName"));
				jsonObject.put(JSONKey.USER_TEAMNAME, rs.getString("TeamName"));
				jsonObject.put(JSONKey.USER_BUREAUNAME,rs.getString("BureauName"));
				jsonObject.put(JSONKey.USER_DEPTNAME, rs.getString("Deptname"));
				break;
			}
			conn.close(); // 关闭数据库连接

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}
	public static JSONObject queryMessage() throws Exception {             //此方法用于发送彩信
		conn = JDBCServer.getConnection();
		JDBCServer jdbcServer=new JDBCServer();
		JSONObject json = null;
		try {
			String sql="select * from tb_sndtmp";
			List<Map<String, Object>> list=jdbcServer.findMoreResult(sql, null);
			System.out.println(list);
			JSONArray jsonArray = new JSONArray();
			System.out.println(list.size());
			for(int i=0;i<list.size();i++){
				Map<String, Object> map=new HashMap<String, Object>();
				map=list.get(i);
				JSONObject jsonmessage = new JSONObject();
				jsonmessage.put(JSONKey.RECMESSAGE_SMID, map.get("smid"));
				jsonmessage.put(JSONKey.RECMESSAGE_MSG, map.get("msg"));
				jsonmessage.put(JSONKey.RECMESSAGE_NAME, map.get("stype"));
				jsonmessage.put(JSONKey.RECMESSAGE_NUM, map.get("tpa"));
				jsonArray.put(jsonmessage);
			}
			json = new JSONObject();
			json.put(JSONKey.RECMESSAGE, jsonArray);
			System.out.println(json);
			jdbcServer.releaseConn();
		   }
		 catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		finally{
			new JDBCServer().releaseConn();
		}
		return json;
	 
	}
	/******************************上水部分函数***********************************/
	public static int sendWorning(String deptname,String train,String tostation,
            String title,String scontent,String sender,String dateStr){
			conn=JDBCServer.getConnection();
			int count = 0;
			try {
			//缺水信息登记
			int num = 1;
			String sql = "INSERT INTO Run_Tky_InWater(DeptName,Train,ToStation," +
			"Title,SContent,Sucess,SendTime,Sender)"
			+ " VALUES ('"
			+ deptname
			+ "', '"
			+ train 
			+ "', '"
			+ tostation
			+ "', '"
			+ title
			+ "', '"
			+ scontent
			+ "', '"
			+ num
			+ "', '"
			+ dateStr
			+ "', '"
			+ sender
			+"')";
			st = (Statement) conn.createStatement();
			count = st.executeUpdate(sql);
			conn.close();
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
			return count;
	}
	public static JSONObject Station(JSONObject jsonObject,String eid,String traincode) throws JSONException{
		conn=JDBCServer.getConnection();
	    ResultSet slt=null;
	    try{
	    	String sql="select Water_Station , TrainStartTime from SSwaterScheme where Train_Code='"+traincode+"' and E_ID = '"+eid+"'";
	    	st=(Statement)conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    	slt=st.executeQuery(sql);
	    	int i = 0;
	    	while(slt.next()){
	    		++i;
	    		String str;
	    		try {
					 str = slt.getString("Water_Station");
				} catch (Exception e) {
					str = "no";
				}
	    		jsonObject.put(i + "" , str);
	    	    jsonObject.put("startDate", slt.getString("TrainStartTime"));
	    	}
	    	jsonObject.put("num", i);
            conn.close();
            System.out.println("jsonObject =.........=>>>>> "+ jsonObject);
	    }catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}
	public static JSONObject waterRecord(JSONObject jsonObject) throws JSONException{
		conn = JDBCServer.getConnection();
		String eid = jsonObject.getString(JSONKey.WATER_EID);
    	String deptname = jsonObject.getString(JSONKey.WATER_DEPTNAME);
    	String trainCode = jsonObject.getString(JSONKey.WATER_TRAIN);
    	String startTime = jsonObject.getString(JSONKey.WATER_START_TIME);
		ResultSet slt = null;
		try{
			String sql="select Start_Station_Name,End_Station_Name,Chemeid,Compile_Group,Bureau_Name,Wather_Coach,Water_Count from SSwaterScheme where E_ID='"+eid+"' and Train_Code = '"+trainCode+"'";
			st=(Statement)conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			slt = st.executeQuery(sql);
			int i = 0;
	    	if(slt.next()){
	    		++i;
	    		String StartStation = null;
	    		String EndStatin = null;
	    		String chemeid = null;
	    		String comGroup = null;
	    		String bureauName = null;
	    		String Wather_Coach = null;
	    		String Water_Count = null;
	    		try {
	    			StartStation = slt.getString("Start_Station_Name");
	    			EndStatin = slt.getString("End_Station_Name");
	    			chemeid = slt.getString("Chemeid");
	    			comGroup = slt.getString("Compile_Group");
	    			bureauName = slt.getString("Bureau_Name");
	    			Wather_Coach = slt.getString("Wather_Coach");
	    			Water_Count = slt.getString("Water_Count");
	    			jsonObject.put(JSONKey.WATER_START_STATION, StartStation);
	    			jsonObject.put(JSONKey.WATER_END_STATION, EndStatin);
	    			jsonObject.put(JSONKey.WATER_CHEMEID, chemeid);
	    			jsonObject.put(JSONKey.WATER_COM_GROUP, comGroup);
	    			jsonObject.put(JSONKey.WATER_BUREAU_NAME, bureauName);
	    			jsonObject.put(JSONKey.WATER_WATHER_COACH, Wather_Coach);
	    			jsonObject.put(JSONKey.WATER_SHOULDWATERCOUNT, Water_Count);
				} catch (Exception e) {
					StartStation = "no";
	    			EndStatin = "no";
	    			chemeid = "no";
	    			comGroup = "no";
				}
	    	}
	    	jsonObject.put("num", i);
            conn.close();
            System.out.println("jsonObject = "+ jsonObject);
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}
	
	public static int waterRecordSend(JSONObject jsonObject) throws JSONException{//上水登记
		conn = JDBCServer.getConnection();
		String eid = jsonObject.getString(JSONKey.WATER_EID);
		//客运段
    	String deptname = jsonObject.getString(JSONKey.WATER_DEPTNAME);
    	//车号
    	String trainCode = jsonObject.getString(JSONKey.WATER_TRAIN);
    	//始发时间
    	String startTime = jsonObject.getString(JSONKey.WATER_START_TIME);
    	//上水时间
    	String waterUpDate = jsonObject.getString(JSONKey.WATER_DATETIMES);
    	//实际上水量
    	String watercount = jsonObject.getString(JSONKey.WATER_COUNT);
    	//备注
    	String waterRemark = jsonObject.getString(JSONKey.WATER_REMARK);
    	//是否库内上水
    	int water_In = jsonObject.getInt(JSONKey.WATER_ISLIBRARY);
    	//上水站
    	String waterstation = jsonObject.getString(JSONKey.WATER_STATION);
    	//始发站
    	String StartStation = jsonObject.getString(JSONKey.WATER_START_STATION);
    	//终点站
    	String EndStatin = jsonObject.getString(JSONKey.WATER_END_STATION);
    	//上水方案号
    	String Chemeid = jsonObject.getString(JSONKey.WATER_CHEMEID);
    	//编组数
    	String comGroup = jsonObject.getString(JSONKey.WATER_COM_GROUP);
    	//但当局
    	String bureauName = jsonObject.getString(JSONKey.WATER_BUREAU_NAME);
    	//给水车厢
    	String wathercoach = jsonObject.getString(JSONKey.WATER_WATHER_COACH);
    	//应上水
    	String waterShouldCount = jsonObject.getString(JSONKey.WATER_SHOULDWATERCOUNT);
    	//班组
    	String groupname = jsonObject.getString(JSONKey.WATER_GROUPNAME);
    	ResultSet slt = null;
    	int count = 0;
    	System.out.println("waterRecordSend");
		try{
			String sql = "insert into SSWaterRecords(Chemeid,Train_Code,GroupName,Start_Station_Name,End_Station_Name,StartTime"+
			",Bureau_Name,Water_Station,Compile_Group,Wather_Coach,ShouldWaterCount,Water_Count,Datetimes,IsLibrary,E_ID,Remark) values('"+
			Chemeid +
			"','"+
			trainCode +
			"','"+
			groupname +
			"','"+
			StartStation +
			"','"+
			EndStatin +
			"','"+
			startTime +
			"','"+
			bureauName +
			"','"+
			waterstation +
			"','"+
			comGroup +
			"','"+
			wathercoach +
			"','"+
			waterShouldCount +
			"','"+
			watercount +
			"','"+
			waterUpDate +
			"','"+
			water_In +
			"','"+
			eid +
			"','"+
			waterRemark +
			"')";
			st = (Statement) conn.createStatement();
			System.out.println("sql:"+sql);
			count = st.executeUpdate(sql);
            conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return count;
	}
	public static JSONObject Traincode(JSONObject jsonObject,String eid) throws JSONException{
		conn = JDBCServer.getConnection();
		ResultSet slt = null;
		try{
			String sql="select distinct Train_Code from SSwaterScheme where E_ID='"+eid+"'"; 
			st=(Statement)conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			slt = st.executeQuery(sql);
			int i = 0;
	    	while(slt.next()){
	    		++i;
	    		String str;
	    		try {
					 str = slt.getString("Train_Code");
				} catch (Exception e) {
					str = "no";
				}
	    		jsonObject.put(i + "" , str);
	    	}
	    	jsonObject.put("num", i);
            conn.close();
            System.out.println("jsonObject = "+ jsonObject);
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}
	
	public static JSONObject selectWater(String trainCode,String waterStation){     
		conn=JDBCServer.getConnection();
		JSONObject jsonObject=null;
		ResultSet slt = null;
		System.out.println("111111111111111;conn="+conn);
		try{
			String sql="select * from SSwaterScheme where Train_Code='"+trainCode+"' and Water_Station='"+waterStation+"'";
			st=(Statement)conn.createStatement();
			slt=st.executeQuery(sql);//当输入的车次中含有字母的时候，此处返回值为空；
			while (slt.next()) { // 判断是否还有下一个数据
				jsonObject = new JSONObject();
				jsonObject.put(JSONKey.WATER_CHEMEID, slt.getString("Chemeid"));
				jsonObject.put(JSONKey.WATER_SHUNNO, slt.getString("ShunNo"));
				jsonObject.put(JSONKey.WATER_TRAIN_CODE, slt.getString("Train_Code"));
				jsonObject.put(JSONKey.WATER_START_STATION_NAME, slt.getString("Start_Station_Name"));
				jsonObject.put(JSONKey.WATER_END_STATION_NAME, slt.getString("End_Station_Name"));
				jsonObject.put(JSONKey.WATER_BUREAU_NAME,slt.getString("Bureau_Name"));
				jsonObject.put(JSONKey.WATER_STATION,slt.getString("Water_Station"));
				jsonObject.put(JSONKey.WATER_ARRIVETIME, slt.getString("ArriveTime"));
				jsonObject.put(JSONKey.WATER_TRAIN_STARTTIME,slt.getString("TrainStartTime"));
				jsonObject.put(JSONKey.WATER_DISTANCETIME, slt.getString("DistanceTime"));
				jsonObject.put(JSONKey.WATER_STOPTIME, slt.getString("Stop_Time"));
				jsonObject.put(JSONKey.WATER_WATHER_COACH, slt.getString("Wather_Coach"));
				jsonObject.put(JSONKey.WATER_COMPILE_GROUP, slt.getString("Compile_Group"));
				jsonObject.put(JSONKey.WATER_JOIN_TRUCKBED, slt.getString("Join_Truckbed"));
				jsonObject.put(JSONKey.WATER_COUNT, slt.getString("Water_Count"));
				jsonObject.put(JSONKey.WATER_ISGETWATER, slt.getString("IsGetWater"));
				jsonObject.put(JSONKey.WATER_DATETIMES, slt.getString("Datetimes"));
				jsonObject.put(JSONKey.WATER_EID, slt.getString("E_ID"));
				jsonObject.put(JSONKey.WATER_STARTTIME, slt.getString("StartTime"));
				jsonObject.put(JSONKey.WATER_ENDTIME, slt.getString("EndTime"));
				jsonObject.put(JSONKey.WATER_STATION_TPYE, slt.getString("WaterStationTpye"));
				jsonObject.put(JSONKey.WATER_ISLIBRARY, slt.getString("IsLibrary"));
				jsonObject.put(JSONKey.WATER_AUTID, slt.getString("Audit"));
				jsonObject.put(JSONKey.WATER_AUTID_ID, slt.getString("Audit_ID"));
				jsonObject.put(JSONKey.WATER_TRAIN_NO, slt.getString("Train_no"));
				jsonObject.put(JSONKey.WATER_REMARK, slt.getString("Remark"));
				break;
			}
			conn.close();
		}                   //关闭数据库连接
		catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}
	public static int insertWater(String start_station,String end_station,String chemeid,String trainCode,
			                      String starttime,String bureauName,String waterStation,String compileGroup,
			                      String watherCoach,String shouldCount,String waterCount,
			                      String isLibrary,String Eid,String remark){
		conn = JDBCServer.getConnection();
		int count = 0;
		try {
			Date day1=new Date();
			java.sql.Date day=new java.sql.Date(day1.getTime());
			String sql = "INSERT INTO SSWaterRecords(Start_Station_Name,End_Station_Name,Chemeid," +
					"Train_Code,StartTime,Bureau_Name," +
					"Water_Station,Compile_Group,Wather_Coach,ShouldWaterCount," +
					"Water_Count,Datetimes,IsLibrary,E_ID,Remark)"
					+ " VALUES ('"
					+ start_station
					+ "', '"
					+ end_station 
					+ "', '"
					+ chemeid
					+ "', '"
					+ trainCode
					+ "', '"
					+ starttime
					+ "', '"
					+ bureauName
					+ "', '"
					+ waterStation
					+ "', '"
					+ compileGroup
					+ "', '"
					+ watherCoach
					+ "', '"
					+ shouldCount
					+ "', '"
					+ waterCount
					+ "', '"
					+ day
					+ "', '"
					+ isLibrary
					+ "', '"
					+ Eid
					+ "', '"
					+ remark
					+"')"; // 插入数据的sql语句

			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
			conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("插入数据失败" + e.getMessage());
		}
		return count;
	}
	//发送缺水登记信息
	public static int sendWorning(String deptname,String train,String tostation,
				                      String title,String scontent,String sender){
			conn=JDBCServer.getConnection();
			int count = 0;
			try {
				Date day1=new Date();
			    int num=1;
			    System.out.print("count01");
				java.sql.Date day=new java.sql.Date(day1.getTime());
				String sql = "INSERT INTO Run_Tky_InWater(DeptName,Train,ToStation," +
						"Title,SContent,Sucess,SendTime,Sender)"
						+ " VALUES ('"
						+ deptname
						+ "', '"
						+ train 
						+ "', '"
						+ tostation
						+ "', '"
						+ title
						+ "', '"
						+ scontent
						+ "', '"
						+ num
						+ "', '"
						+ day
						+ "', '"
						+ sender
						+"')"; // 插入数据的sql语句
				st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
				System.out.println("count=");
				count = st.executeUpdate(sql); // 执行插入操作的sql语句，并返回插入数据的个数
				System.out.println("count02");
				conn.close(); // 关闭数据库连接
			} catch (SQLException e) {
				System.out.println("插入数据失败" + e.getMessage());
			}
			System.out.println("count");
			System.out.println("count:"+count);
			return count;
	}
	//
	public static JSONArray Station(String traincode) throws JSONException{
		conn=JDBCServer.getConnection();
		JSONArray jsonArray;
	    ResultSet slt=null;
	    try{
	    	String sql="select Water_Station from SSwaterScheme where Train_Code='"+traincode+"'";
	    	st=(Statement)conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    	slt=st.executeQuery(sql);
	    	jsonArray = new JSONArray();
	    	System.out.println(slt);
	    	slt.last();
	    	int n=slt.getRow();
	    	System.out.println("slt.getRow="+n);
	    	slt.first();
	    	while(slt.next()){
	    		jsonArray.put(slt.getString("Water_Station"));
	    	}
            conn.close();
            System.out.println("jsonArray =1 "+ jsonArray);
	    }catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		return jsonArray;
	}
	public static JSONArray Traincode(String eid) throws JSONException{
		conn = JDBCServer.getConnection();
		JSONArray jsonArray;
		ResultSet slt = null;
		try{
			String sql="select distinct Train_Code from SSwaterScheme where E_ID='"+eid+"'"; 
			st=(Statement)conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			slt = st.executeQuery(sql);
			jsonArray = new JSONArray();
			slt.last();
	    	int n=slt.getRow();
	    	slt.first();
	    	while(slt.next()){
	    		jsonArray.put(slt.getString("Train_Code"));
	    	}
            conn.close();
            System.out.println("jsonArray =1 "+ jsonArray);
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		return jsonArray;
	}
	public static int insertVideoInfo(File videoFile,int userId){
		conn = JDBCServer.getConnection();
		int inertId = -1;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			String uploadTime = df.format(new Date());
//					Date uploadTime = new Date();
//					String sql = "INSERT INTO VideosInfo(video_creat_time,video_upload_time,video_user_id,video_file_url,video_file_upload_percentage) "
//							+ " VALUES ('"+uploadTime+"', '"+uploadTime+"', '1','" + videoFile.getAbsolutePath() + "', '0') SELECT @@IDENTITY AS Id "; // 插入数据的sql语句
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
			System.out.println("insertVideoInfo;插入数据失败" + e.getMessage());
		}
		return inertId;
	}
	
	public static FileLog getUploadFile(int id){
		conn = JDBCServer.getConnection();
		FileLog myFilelog = null;
//				try {
//					String sql = "select video_file_url from VideosInfo where id="+id+"\"";
//					System.out.println("selected url = "+sql);
//					st = (Statement) conn.createStatement();
//					ResultSet rs = st.executeQuery(sql);
//					if(!rs.wasNull()){
//						myFilelog.setId(rs.getLong("id"));
//						myFilelog.setPath(rs.getString("video_file_url"));
//					}
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		try{
			String sql = "select video_file_url from VideosInfo where id=?";
			Map<String, Object> map = new HashMap<String, Object>();
			ArrayList<Object> myPar = new ArrayList<Object>();
			myPar.add(0, id);
			map = jdbcServer.findSimpleResult(sql, myPar);
			if(map.size()>0){
				myFilelog.setId(Integer.valueOf(map.get("id").toString()));
				myFilelog.setPath(map.get("video_file_url").toString());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			conn.close();
			jdbcServer.releaseConn();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myFilelog;
	}
	
	public static boolean updateVideoInfo(int id){
		conn = JDBCServer.getConnection();
		boolean updateResult = false;
		try {
//					String sql = "Update VideosInfo set video_file_upload_percentage = '100' where id = '"+id+"'"; // 更新数据的sql语句
//					st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象
			String sql = "Update VideosInfo set video_file_upload_percentage = '100' where id =?"; // 更新数据的sql语句
			ArrayList<Object> myPar = new ArrayList<Object>();
			myPar.add(0, id);
			updateResult = jdbcServer.updateByPreparedStatement(sql, myPar);
		} catch (SQLException e) {
			System.out.println("更新数据失败" + e.getMessage());
		}
		try {
			conn.close();// 关闭数据库连接
			jdbcServer.releaseConn();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateResult;
	}
}