package com.tky.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JApplet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TKYServer extends HttpServlet {

	private static String oldPath = null;
	private static final long serialVersionUID = 4988624175878648807L;
	private static Connection conn;
	private static Statement st;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	private ServletFileUpload upload;
	private final long MAXSize = 4194304 * 2L;// 4*2MB
	private String filedir = null;
	public SocketServer server = null;
	public UploadSocket uploadServer = null;

	@Override
	public void init(ServletConfig config) throws ServletException { // ��ʼ��
		super.init();
		// String s = getServletContext().getContextPath();
		// System.out.println("init();"+s);

		FileItemFactory factory = new DiskFileItemFactory();// Create a factory
															// for disk-based
															// file items
		this.upload = new ServletFileUpload(factory);// Create a new file upload
														// handler
		this.upload.setSizeMax(this.MAXSize);// Set overall request size
												// constraint 4194304
		filedir = config.getServletContext().getRealPath("images");
		System.out.println("filedir=" + filedir);

		// ����socketServer
		server = new SocketServer(JSONKey.SOCKET_PORT);
		startSocketServer();
		
		uploadServer = new UploadSocket();
		startUploadSock();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String s = req.getParameter("json");// �õ��ͻ��˷��͵�����
		System.out.println("doPost();" + s);
		if(s==null){                                  //����ͼƬ
			PrintWriter out=resp.getWriter();
	        try {
				List<FileItem> items = this.upload.parseRequest(req);
				System.out.println(items.toString()+"...66");
				if(items!=null	&& !items.isEmpty()){
					for (FileItem fileItem : items) {
						String filename=fileItem.getName();
						System.out.println(filename+"...77");
						String filepath=filedir+File.separator+filename;
						System.out.println("�ļ�����·��Ϊ:"+filepath);
						File file=new File(filepath);
						if (file != null) {
							System.out.println("�ļ���file != null");
							if (!file.exists()){
								file.mkdirs();
								System.out.println("!file.exists()");
							}
							if (file.exists() && file.canWrite()) {
								file.delete();
								System.out.println("file.exists()");
							}
							file.createNewFile();
						}
						InputStream inputSteam=fileItem.getInputStream();
						BufferedInputStream fis=new BufferedInputStream(inputSteam);
					    FileOutputStream fos=new FileOutputStream(file);
					    int f;
					    while((f=fis.read())!=-1)
					    {
					       fos.write(f);
					    }
					    System.out.println("f1");
					    fos.flush();
					    fos.close();
					    fis.close();
						inputSteam.close();
						System.out.println("f2");
						oldPath = filepath;
						insertPhotoPath(filepath,filename,"");
						System.out.println("�ļ���"+filename+"�ϴ��ɹ�!");
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
				out.write("�ϴ��ļ�ʧ��:"+e.getMessage());
			}
		}
		else {
			PrintWriter out = null;
			JSONObject jsonObject = null;
			JSONArray jsonArray = null;
			Boolean array1 = false;
			try {
				jsonObject = new JSONObject(s);
				String flag = jsonObject.getString(JSONKey.FLAG);
				System.out.println("doPost();s is null and flag=" + flag);
				if (flag.equals("Welcom")) {
					jsonObject.put(JSONKey.WELCOM_LOG, "�����ɹ�");
				} else if (flag.equals(JSONKey.FLAG_LOGIN)) { // ��½
					jsonObject = query_Login(
							jsonObject.getString(JSONKey.USER_LOGIN),
							jsonObject.getString(JSONKey.USER_PASSWORD));
					if (jsonObject != null) {
						jsonObject.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_OK);
					} else {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "�û������������");
					}
				} else if (flag.equals(JSONKey.FLAG_RULES)) {// �͹�
					jsonObject = queryRegualation();
					if (jsonObject != null) {
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_OK);
					} else {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "���ݴ���");
					}
				} else if (flag.equals(JSONKey.FLAG_TELEGRAPH)) { // �籨
					jsonObject.getString(JSONKey.TELEGRAPH_AUTHOR);
					int i = insertTelegraph(
							jsonObject.getString(JSONKey.TELEGRAPH_AUTHOR),
							jsonObject.getString(JSONKey.TELEGRAPH_SINGERID),
							jsonObject.getString(JSONKey.TELEGRAPH_TITLE),
							jsonObject.getString(JSONKey.TELEGRAPH_MESSAGE),
							jsonObject.getString(JSONKey.TELEGRAPH_SENDUNIT),
							jsonObject.getString(JSONKey.TELEGRAPH_COPYUNIT));
					if (i > 0) {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_OK);
					} else {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "���ݴ���");
					}
				} else if (flag.equals(JSONKey.FLAG_MESSAGE)) { // ���Ͷ���
					int i = insertShortmessage(
							jsonObject.getString(JSONKey.MESSAGE_MSG),
							jsonObject.getString(JSONKey.MESSAGE_TPA),
							jsonObject.getString(JSONKey.MESSAGE_DEVNO),
							jsonObject.getString(JSONKey.MESSAGE_SMSTYPE),
							jsonObject.getString(JSONKey.MESSAGE_SIGN));
					if (i > 0) {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_OK);
					} else {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "���ݴ���");
					}
				} else if (flag.equals(JSONKey.FLAG_RECMESSAGE)) {// ���ն���
					jsonObject = queryMessage();
					if (jsonObject != null) {
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_OK);
					} else {
						jsonObject = new JSONObject();
						jsonObject.put(JSONKey.FLAG_RESULT,
								JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "���ݴ���");
					}

				} 
				//����ǲ��ŵ����ݣ�����Ҫ�������ݴ洢����
				else if(flag.equals("mms")){//
                	System.out.println("result ="+s);
					String id = jsonObject.getString(JSONKey.MMS_ID);
					int className = jsonObject.getInt(JSONKey.MMS_CLASS);
					int num = (int)jsonObject.getInt(JSONKey.MMS_NUM);
					System.out.println("result id="+id );
					System.out.println("result cls=" +className);
					System.out.println("result num=" +num);
					String text = (String)jsonObject.getString(JSONKey.MMS_TEXT);
					System.out.println("result text=" +text);
					int i = 0;
					i = insertMms(className, id, num, text);
					jsonObject = water_if(jsonObject,i);
				}else if(flag.equals("spinner")){
					System.out.println("spinner");
					searchForSpinner(jsonObject);
				}
				//��ˮ���ֵķ��������ܣ�ע�⣬�������ܶ���Ҫ�������������棬���ⷢ������
				else if (flag.equals(JSONKey.FLAG_WATER_INSERT)) {
					int i = JSONProcess.insertWater(
							jsonObject.getString(JSONKey.WATER_START_STATION_NAME),
							jsonObject.getString(JSONKey.WATER_END_STATION_NAME),
							jsonObject.getString(JSONKey.WATER_CHEMEID),
							jsonObject.getString(JSONKey.WATER_TRAIN_CODE),
							jsonObject.getString(JSONKey.WATER_STARTTIME),
							jsonObject.getString(JSONKey.WATER_BUREAU_NAME),
							jsonObject.getString(JSONKey.WATER_STATION),
							jsonObject.getString(JSONKey.WATER_COMPILE_GROUP),
							jsonObject.getString(JSONKey.WATER_WATHER_COACH),
							jsonObject.getString(JSONKey.WATER_SHOULDWATERCOUNT),
							jsonObject.getString(JSONKey.WATER_COUNT),
							jsonObject.getString(JSONKey.WATER_ISLIBRARY),
							jsonObject.getString(JSONKey.WATER_EID), 
							jsonObject.getString(JSONKey.WATER_REMARK));
					jsonObject = water_if(jsonObject,i);
				}
				else if (flag.equals(JSONKey.FLAG_WATER)) { // JSONDateUtil�ڷ�װJSONʱ��flag��ʾһ���װ
					System.out.println("in flag_water ");
					jsonObject = JSONProcess.selectWater(
							jsonObject.getString(JSONKey.WATER_TRAIN_CODE),
							jsonObject.getString(JSONKey.WATER_STATION));
					System.out.println("jsonObject="+jsonObject);
					if (jsonObject != null && jsonObject.length() > 1) {
						jsonObject.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_OK);
						System.out.println("007");
					}else {
						if(jsonObject == null){
							jsonObject = new JSONObject();
						}
						jsonObject.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_ERROR);
						jsonObject.put(JSONKey.ERROR_INFO, "��������г��Ż�վ����������������");
					}
				}
				else if (jsonObject.length() < 1) {
					jsonObject.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_OK);
					System.out.println("008");
				}
				else if(flag.equals(JSONKey.FLAG_WATER_WORNING)){     //ȱˮ�Ǽ�
					System.out.println("in flag_water :");
					JSONArray jArray = jsonObject.getJSONArray(JSONKey.WATER_SCONTENT);
					System.out.println("in flag_water :"+jArray.length());
					System.out.println("in flag_water :"+jArray);
					String message = "";
					JSONObject jObj = new JSONObject();
					jObj = jArray.getJSONObject(0);
					for (int j = 0; j < jObj.length(); j++) {
						message = message + jObj.getString(j+"");
					}
					int i=JSONProcess.sendWorning(
						jsonObject.getString(JSONKey.WATER_DEPTNAME),
						jsonObject.getString(JSONKey.WATER_TRAIN),
						jsonObject.getString(JSONKey.WATER_TOSTATION),
						jsonObject.getString(JSONKey.WATER_TITLE),
						message,
						jsonObject.getString(JSONKey.WATER_SENDER),
						jsonObject.getString(JSONKey.WATER_UP_DATE_KEY)
						);
					jsonObject = water_if(jsonObject,i);
	            }
				//��վ��
				else if(flag.equals(JSONKey.FLAG_WATER_STATION)){
					System.out.println("code1 = 000:��վ��");
	            	array1=true;
	                String code1 = jsonObject.getString(JSONKey.WATER_TRAIN_CODE);
	            	System.out.println("code1 = "+code1);
	            	System.out.println("station:jsonObject:"+jsonObject.toString());
	            	//��ȡ�û�id
	            	String eid1=jsonObject.getString(JSONKey.WATER_EID);
	            	System.out.println("station-eid: "+eid1);
	            	jsonObject = JSONProcess.Station(jsonObject,eid1,code1);
	            }
				//���κ�
				else if(flag.equals(JSONKey.FLAG_WATER_TRAINCODE)){
					System.out.println("trainCode:"+ jsonObject);
	            	array1=true;
	            	//��ȡ�û�id
	            	String eid1 = jsonObject.getString(JSONKey.WATER_EID);
	            	//��ȡ���ݿ��е���Ϣ
	            	jsonObject = JSONProcess.Traincode(jsonObject,eid1);
	            	System.out.println("jsonObject = "+ jsonObject);
	            }
				else if(flag.equals(JSONKey.WATER_RECORD)){
					System.out.println("water  record !!!");
					array1=true;
	            	//��ȡ�û�id
//	            	String eid1 = jsonObject.getString(JSONKey.WATER_EID);
//	            	String deptname = jsonObject.getString(JSONKey.WATER_DEPTNAME);
//	            	String trainCode = jsonObject.getString(JSONKey.WATER_TRAIN);
//	            	String startTime = jsonObject.getString(JSONKey.WATER_START_TIME);
	            	//��ȡ���ݿ��е���Ϣ
	            	jsonObject = JSONProcess.waterRecord(jsonObject);
	            	System.out.println("jsonObject = "+ jsonObject);
				}else if(flag.equals(JSONKey.WATER_RECORD_SEND)){//��ˮ�Ǽ�
					System.out.println("water  record !!!");
					array1=true;
	            	//��ȡ���ݿ��е���Ϣ
					int i=0;
	            	i = (int) JSONProcess.waterRecordSend(jsonObject);
	            	jsonObject = water_if(jsonObject,i);
//	            	if(i>0){
//	            		jsonObject = new JSONObject();
//	            		jsonObject.put(JSONKey.FLAG_RESULT, JSONKey.FLAG_RESULT_OK);
//	            	}else{
//						jsonObject=new JSONObject();
//						jsonObject.put(JSONKey.FLAG_RESULT, JSONKey.FLAG_RESULT_ERROR);
//						jsonObject.put(JSONKey.ERROR_INFO, "���ݲ������");
//					}
	            	System.out.println("jsonObject = "+ jsonObject);
	            	System.out.println("sql:");
				}
				else{
					System.out.println("flag====error===>"+flag);
				}
				resp.setContentType("text/plain");
				resp.setCharacterEncoding("UTF-8");
				out = resp.getWriter();
			    System.out.println("result =" + jsonObject.toString());
			    out.write(jsonObject.toString());
				System.out.println("this post out write="+jsonObject);
			} catch (JSONException e) {
				    e.printStackTrace();
			}
			if (out != null)
				out.close();
		}
	}
	public static JSONObject water_if(JSONObject obj,int i) throws JSONException{
		obj = new JSONObject();
		if (i > 0) {
			obj.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_OK);
		} else {
			obj.put(JSONKey.FLAG_RESULT,JSONKey.FLAG_RESULT_ERROR);
			obj.put(JSONKey.ERROR_INFO, "���ݲ������");
		}
		return obj;
	}
	public static int insertTelegraph(String author, String SignerId,
			String title, String teleMessage, String SendUnit, String copyUnit) {
		conn = JDBCServer.getConnection();
		int count = 0;
		try {
			String sql = "INSERT INTO telegraph(Signer,SignerID,SendUnit,Title,TeleMessage,SendDateTime,CopyUnit)"
					+ " VALUES ('"
					+ author
					+ "', '"
					+ SignerId
					+ "', '"
					+ SendUnit
					+ "', '"
					+ title
					+ "', '"
					+ teleMessage
					+ "', '"
					+ dateFormat.format(new Date()) + "', '" + copyUnit + "')"; // �������ݵ�sql���

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

	public static int insertShortmessage(String msg, String tpa, String devon,
			String smstype, String sign) {
		conn = JDBCServer.getConnection();
		int count = 0;
		int onlyid = 88880;
		try {
			String sql = "INSERT INTO tb_rcvtmp(smid,msg,tpa,recvdatetime,devno,smstype,sign)"
					+ " VALUES ("
					+ "(select isnull(MAX(smid),0)+1 from tb_rcvtmp)"
					+ ", '"
					+ msg
					+ "', '"
					+ tpa
					+ "', '"
					+ dateFormat.format(new Date())
					+ "', '"
					+ devon
					+ "', '"
					+ smstype + "', '" + sign + "')"; // �������ݵ�sql���

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

	public static JSONObject queryRegualation() {
		conn = JDBCServer.getConnection();
		JSONObject j = null;
		try {
			String sql = "select * from GwRulesAndroid where Type='Z' or Type ='T'";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			JSONArray jsonArray = new JSONArray();
			while (rs.next()) { // �ж��Ƿ�����һ������
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(JSONKey.RULE_ID, rs.getString("ID"));
				jsonObject.put(JSONKey.RULE_RULECHAPTERNAME,
						rs.getString("RuleChapterName"));
				jsonObject.put(JSONKey.RULE_RULECHAPTERKEYWORDS,
						rs.getString("RuleChapterKeyWords"));
				jsonObject.put(JSONKey.RULE_PARENTID, rs.getString("ParentID"));
				jsonObject.put(JSONKey.RULE_INDEXID, rs.getString("IndexID"));
				jsonObject.put(JSONKey.RULE_DIRECTORYLEVELID,
						rs.getString("DirectoryLevelID"));
				jsonObject.put(JSONKey.RULE_RULENAME, rs.getString("RuleName"));
				jsonObject.put(JSONKey.RULE_RULETABLEID,
						rs.getString("RuleTableID"));
				jsonObject.put(JSONKey.RULE_NEWCHAPTERURL,
						rs.getString("NewChapterURL"));
				jsonObject.put(JSONKey.RULE_NEWCHAPTERNAME,
						rs.getString("NewChapterName"));
				jsonObject.put(JSONKey.RULE_DELCHAPTERSIGN,
						rs.getString("DelChapterSign"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERSIGN,
						rs.getString("UpdateChapterSign"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERABSTRACT,
						rs.getString("UpdateChapterAbstract"));
				jsonObject.put(JSONKey.RULE_UPDATECHAPTERDATETIME,
						rs.getString("UpdateChapterDateTime"));
				jsonObject.put(JSONKey.RULE_POSITIONID,
						rs.getString("PositionID"));

				jsonArray.put(jsonObject);
			}
			j = new JSONObject();
			j.put(JSONKey.RULE_RULES, jsonArray);
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("��ѯ����ʧ��");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j;
	}

	public static JSONObject query_Login(String account, String pw) {
		conn = JDBCServer.getConnection(); // ͬ����Ҫ��ȡ���ӣ������ӵ����ݿ�
		JSONObject jsonObject = null;
		try {
			String sql = "select * from JcEmployee where E_name='" + account + "' and Password='" + pw + "'"; // ��ѯ���ݵ�sql���
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) { // �ж��Ƿ�����һ������
				jsonObject = new JSONObject();
				jsonObject.put(JSONKey.USER_ID, rs.getInt(JSONKey.USER_ID));
				jsonObject.put(JSONKey.USER_NAME, rs.getString("E_Name"));
				jsonObject.put(JSONKey.USER_PASSWORD, rs.getString("Password"));
				jsonObject.put(JSONKey.USER_EID, rs.getString("E_ID"));
				jsonObject.put(JSONKey.USER_TYPE, rs.getString("Type"));
				jsonObject.put(JSONKey.USER_SEX, rs.getString("E_Sex"));
				jsonObject.put(JSONKey.USER_POSITION, rs.getString("E_Position"));
				jsonObject.put(JSONKey.USER_GROUPNAME, rs.getString("GroupName"));
				jsonObject.put(JSONKey.USER_TEAMNAME, rs.getString("TeamName"));
				jsonObject.put(JSONKey.USER_BUREAUNAME,
						rs.getString("BureauName"));
				jsonObject.put(JSONKey.USER_DEPTNAME, rs.getString("Deptname"));
				break;
			}
			conn.close(); // �ر����ݿ�����

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonObject;
	}

	public static JSONObject queryMessage() {
		conn = JDBCServer.getConnection();
		JSONObject json = null;
		try {
			String sql = "select * from tb_sndtmp ;";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			JSONArray jsonArray = new JSONArray();
			while (rs.next()) { // �ж��Ƿ�����һ������
				JSONObject jsonmessage = new JSONObject();
				jsonmessage.put(JSONKey.RECMESSAGE_SMID, rs.getString("smid"));
				jsonmessage.put(JSONKey.RECMESSAGE_MSG, rs.getString("msg"));
				jsonmessage.put(JSONKey.RECMESSAGE_NAME, rs.getString("stype"));
				jsonmessage.put(JSONKey.RECMESSAGE_NUM, rs.getString("tpa"));
				jsonArray.put(jsonmessage);
			}
			json = new JSONObject();
			json.put(JSONKey.RECMESSAGE, jsonArray);
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return json;
	}

	public static String  dateNow(){
		 Date dt=new Date();
	     SimpleDateFormat matter1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     System.out.println(matter1.format(dt));
       return matter1.format(dt);
	}
	public static int insertPhotoPath(String address, String filename, String message) {
		conn = JDBCServer.getConnection();
		System.out.println("f3" +conn);
		int count = 0;
		try {
			String sql = "INSERT INTO photo200(class,id,num,address,filename,message,datatime)"
					+ " VALUES ('"
					+ "', '"
					+ "', '"
					+ "', '"
					+ address
					+ "', '"
					+ filename
					+ "', '"
					+ "', '"
					+ dateNow()
					+ "')"; // �������ݵ�sql���
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return count;
	}
	//�ϴ�����
	public static int insertMms( int className,String id,int num,String message){
		conn = JDBCServer.getConnection();
		System.out.println("f3" +conn+"======>"+className);
		int count = 0;
		try {
			String sql = "UPDATE photo200 SET class = '"
					+ className
					+ "', id = '"
					+ id
					+ "', num = '"
					+ num
					+ "', message = ' "
					+ message
					+ " ' WHERE datatime = ( SELECT max([datatime]) FROM photo200)";
			st = (Statement) conn.createStatement(); // ��������ִ�о�̬sql����Statement����
			count = st.executeUpdate(sql); // ִ�в��������sql��䣬�����ز������ݵĸ���
			conn.close(); // �ر����ݿ�����
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return count;
	}
	public static JSONObject searchForSpinner(JSONObject jsonObject){
		conn = JDBCServer.getConnection();
		try {
			String sql = "select * from photo200 WHERE datatime = ( SELECT max([datatime]) FROM photo200)";
			// ��������ִ�о�̬sql����Statement����
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			String id = null;
			int num;
			int classNum;
			String msg = null;
			if(rs.next()) {
				 id = rs.getString("id");
				 sql = "select * from photo200 where id = '" + id + "'";
				 ResultSet rs2 = st.executeQuery(sql);
				 while (rs2.next()){
					 System.out.println("=========================================================================>");
					 id = rs2.getString("id");
					 num = rs2.getInt("num");
					 classNum = rs2.getInt("class");
					 msg = rs2.getString("message");
					 try {
						 jsonObject.put("num", num);
						 jsonObject.put(""+classNum, msg);
					 } catch (JSONException e) {
						e.printStackTrace();
					 }
					 System.out.println("===>  id="+id+"  num="+num+"   classNum="+classNum +"    msg = "+msg);
				 }
			}
			System.out.println("sql==1===?"+jsonObject);
			conn.close(); // �ر����ݿ�����
			return jsonObject;
		} catch (SQLException e) {
			System.out.println("��������ʧ��" + e.getMessage());
		}
		return null;
	}
	// ����SocketServer
	private void startSocketServer() {
		System.out.println("startSocketServer");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					server.start();
					System.out.println("end socket server");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("start socket failed");
				}// TODO Auto-generated method stub
			}
		}).start();

	}
	// ����SocketServer
		private void startUploadSock() {
			System.out.println("startUploadSocketServer");
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						uploadServer.start();
						System.out.println("end upload socket server");
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("start upload socket failed");
					}// TODO Auto-generated method stub
				}
			}).start();
		}
}
