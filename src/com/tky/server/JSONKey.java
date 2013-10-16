package com.tky.server;

public final class JSONKey {                //用来定义所有的返回值以及JSON包头名;

	// ================================================
	// Result
	// ================================================
	public final static String FLAG_RESULT = "result";
	public final static String FLAG_RESULT_OK = "0000";
	public final static String FLAG_RESULT_ERROR = "4000";
	public final static String ERROR_INFO = "error";
	public final static String WELCOM_LOG= "welcom";
	// ================================================
	// Flag
	// ================================================
	public final static String FLAG = "flag";
	public final static String FLAG_LOGIN = "flag_login";
	public final static String FLAG_RULES = "flag_rules";
	public final static String FLAG_TELEGRAPH = "flag_telegraph";
	public final static String FLAG_MESSAGE = "flag_message";
	public final static String FLAG_RECMESSAGE = "flag_recmessage";
	public final static String FLAG_WATER="flag_water";
	public final static String FLAG_WATER_INSERT="water_insert";
	public final static String FLAG_WATER_WORNING="water_worning";
	public final static String FLAG_WATER_STATION="water_station";
	public final static String FLAG_WATER_TRAINCODE="water_traincode";
	public final static String FLAG_VIDEO = "flag_video_upload";
	public final static String FLAG_CHAT = "flag_chat";


	// ================================================
	// User
	// ================================================
	public final static String USER_LOGIN = "login";
	
	public final static String USER_ID = "ID";

	public final static String USER_NAME = "name";

	public final static String USER_PASSWORD = "password";

	public final static String USER_EID = "eid";

	public final static String USER_TYPE = "type";

	public final static String USER_SEX = "sex";

	public final static String USER_POSITION = "position";

	public final static String USER_GROUPNAME = "groupname";

	public final static String USER_TEAMNAME = "groupname";

	public final static String USER_BUREAUNAME = "bureauname";

	public final static String USER_DEPTNAME = "deptname";
	// ================================================
	// Telegraph
	// ================================================
	public final static String TELEGRAPH_TID = "tid";

	public final static String TELEGRAPH_AUTHOR = "author";

	public final static String TELEGRAPH_AUTHOR_ID = "authorID";

	public final static String TELEGRAPH_REVIEWER = "reviewer";

	public final static String TELEGRAPH_SINGER = "signer";
	
	public final static String TELEGRAPH_SINGERID = "signerID";

	public final static String TELEGRAPH_TITLE = "title";

	public final static String TELEGRAPH_MESSAGE = "message";

	public final static String TELEGRAPH_DATE = "date";

	public final static String TELEGRAPH_TYPE = "type";

	public final static String TELEGRAPH_RECEIVERID = "receiverId";

	public final static String TELEGRAPH_COPYTOID = "copytoId";

	public final static String TELEGRAPH_READ = "read";

	public final static String TELEGRAPH_RECEIVERS = "receivers";

	public final static String TELEGRAPH_RECEIVER_NAME = "receiver_name";

	public final static String TELEGRAPH_SENDUNIT = "sendunit";

	public final static String TELEGRAPH_COPYUNIT = "copyunit";
	// MMS================================================
	public final static String MMS_ID = "id";
	public final static String MMS_CLASS = "class";
	public final static String MMS_NUM = "num";
	public final static String MMS_IMGPATH = "imgpath";
	public final static String MMS_TEXT = "text";
	public final static String MMS_PHOTO_LOCAL = "photo_local";
	// ================================================
	// Water                                                  //以下为新添加上水项
	// ================================================
	public final static String WATER_CHEMEID="chemeid";
	
	public final static String WATER_SHUNNO="shunno";
				
	public final static String WATER_TRAIN_CODE="code";
				
	public final static String WATER_GROUPNAME="groupname";
				
	public final static String WATER_START_STATION_NAME="start_station_name";
				
	public final static String WATER_END_STATION_NAME="end_station_name";
				
	public final static String WATER_ARRIVETIME="arrivetime";
				
	public final static String WATER_TRAIN_STARTTIME="train_starttime";
				
	public final static String WATER_STARTTIME="starttime";
				
	public final static String WATER_ENDTIME="endtime";
				
	public final static String WATER_DISTANCETIME="distancetime";
				
	public final static String WATER_STOPTIME="stoptime";
				
	public final static String WATER_BUREAU_NAME="bureau_name";
				
	public final static String WATER_STATION="station";
				
	public final static String WATER_COMPILE_GROUP="compile_group";
				
	public final static String WATER_WATHER_COACH="wather_coach";
				
	public final static String WATER_JOIN_TRUCKBED="join_truckbed";
				
	public final static String WATER_ISGETWATER="isgetwater";
			
	public final static String WATER_SHOULDWATERCOUNT="shouldwatercount";
				
	public final static String WATER_COUNT="count";
				
	public final static String WATER_DATETIMES="datetimes";
				
	public final static String WATER_STATION_TPYE="station_tpye";
			
	public final static String WATER_AUTID="water_autid";
				
	public final static String WATER_AUTID_ID="water_autid_id";
				
	public final static String WATER_ISLIBRARY="islibrary";
				
	public final static String WATER_EID="eid";
				
	public final static String WATER_TRAIN_NO="water_train_no";
				
	public final static String WATER_REMARK="remark";
	
    public final static String WATER_DEPTNAME="deptname";
	
	public final static String WATER_TRAIN="train";
	
	public final static String WATER_TITLE="title";
	
	public final static String WATER_TOSTATION="tostation";
	
	public final static String WATER_SCONTENT="scontent";
	
	public final static String WATER_SENDER="sender";
	
	public final static String WATER_UP_DATE_KEY = "waterUpDate";
	public final static String WATER_RECORD = "waterRecord";
	public final static String WATER_RECORD_SEND = "waterRecordSend";
	public final static String WATER_START_TIME = "starttime";
	public final static String WATER_START_STATION = "StartStation";
	public final static String WATER_END_STATION = "EndStatin";
//	public final static String WATER_BUREAU_NAME = "Bureau_Name";
	public final static String WATER_COM_GROUP = "comGroup";
	// ================================================
	// WdInfo
	// ================================================
	public final static String WDINFO_GROUPNAME = "groupname";

	public final static String WDINFO_MINUTE = "minute";

	// ================================================
	// Rules
	// ================================================
	public final static String RULE_RULES = "rules";

	public final static String RULE_TABLE_ID = "table_id";

	public final static String RULE_ID = "id";

	public final static String RULE_RULECHAPTERNAME = "ruleChapterName";

	public final static String RULE_RULECHAPTERKEYWORDS = "ruleChapterKeyWords";

	public final static String RULE_PARENTID = "parentID";

	public final static String RULE_INDEXID = "indexID";

	public final static String RULE_DIRECTORYLEVELID = "directoryLevelID";

	public final static String RULE_RULENAME = "ruleName";

	public final static String RULE_RULETABLEID = "ruleTableID";

	public final static String RULE_NEWCHAPTERURL = "newChapterURL";

	public final static String RULE_NEWCHAPTERNAME = "newChapterName";

	public final static String RULE_DELCHAPTERSIGN = "delChapterSign";

	public final static String RULE_UPDATECHAPTERSIGN = "updateChapterSign";

	public final static String RULE_UPDATECHAPTERABSTRACT = "updateChapterAbstract";

	public final static String RULE_UPDATECHAPTERDATETIME = "updateChapterDateTime";

	public final static String RULE_POSITIONID = "positionID";

	public final static String RULE_TYPE = "type";
	// ShortMessage
	// ================================================
	
	public final static String MESSAGE_TID = "smid";

	public final static String MESSAGE_MSG = "msg";

	public final static String MESSAGE_TPA = "tpa";

	public final static String MESSAGE_TIME = "recvdatetime";

	public final static String MESSAGE_DEVNO = "devno";
	
	public final static String MESSAGE_SMSTYPE = "smstype";

	public final static String MESSAGE_MMSFILESID = "mmsfilesid";

	public final static String MESSAGE_SIGN = "sign";
    
	//recvMessage
	public final static String RECMESSAGE = "recmessage";
	
	public final static String RECMESSAGE_SMID = "rec_smid";

	public final static String RECMESSAGE_MSG = "rec_msg";
	
	public final static String RECMESSAGE_NUM = "rec_num";

	public final static String RECMESSAGE_NAME = "rec_name";
	
	//socket server
	public final static int SOCKET_PORT = 8087;
	public final static int SOCKET_FOR_VIDEO = 0;
	public final static int SOCKET_FOR_CHAT = 1;
	
}