package com.tky.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author HJ
 *上传文件接收
 */
public class SocketServer {

    private ServerSocket ss = null;  
    private boolean quit;// 是否退出  
    boolean started = false;
    
    List<Client> clients = new ArrayList<Client>();
    List<Info> infos = new ArrayList<Info>();
  
    public SocketServer(int port) { //port：监听端口 
        try {
            ss = new ServerSocket(port);
            System.out.println("服务器启动");
            started = true;
         } catch (BindException e) {
               System.out.println(" 端口已经被占用");
               System.exit(0);
            }
           catch (IOException e) {
              e.printStackTrace();
           }
    }  
  
	// 启动长连服务
	public void start() throws Exception {
		if (!started) {
			System.out.println("socket is closed");
			return;
		}
		while (started) {
			Socket s = ss.accept();
        	Client c = new Client (s);
        	clients.add(c);
        	System.out.println("a client is connected");
        	new Thread(c).start();
		}
	} 
  
    // 退出  
    public void quit() {  
        this.quit = true;
        this.started = false;
        try {  
            ss.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
    
    class Client implements Runnable {
        private Socket s = null;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;
        private boolean bConnected = false;
        private String sendmsg=null;
        private Info userInfo;
        
        private int heartSendTime = 0;
    	private Timer heartTimer;
    	private final static int HEART_DEALY_TIME = 10000;
    	
    	private Object heartNumChangeObj = new Object();
        
		Client(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				bConnected = true;
				System.out.println("Client create success");

			} catch (IOException e) {
				e.printStackTrace();
			}
			//心跳启动
			heartTimer = new Timer();
			heartTimer.schedule(new socketHeartTask(), HEART_DEALY_TIME, HEART_DEALY_TIME);
		}
        
        private Info getMyUserInfo(){
        	return userInfo;
        }
        
        public void send (String str) {
            
            try {
                //System.out.println(s);
                dos.writeUTF(str+"");
                dos.flush();
            } catch(IOException e) {
                clients.remove(this);
                System.out.println("对方已经退出了");
            }
        }
        public void run() {
            try {
            	System.out.println("Client run bConnected="+bConnected);
               while (bConnected) {
            	   System.out.println("Client run while loop");
            	   String headStr = dis.readUTF();
                   System.out.println("Client run str="+headStr);
                   if(headStr != null && !headStr.equals("")){
                	   try {
                		   JSONObject myObj = new JSONObject(headStr);
                		   if(myObj != null){
        	                   String flag = myObj.getString(JSONKey.FLAG);
        	                   String name = myObj.getString("username");
        	                   int userId = myObj.getInt("userid");
        	                   if(flag.equals("online")){ //客户端上线
        	                	   DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	                           String date = df.format(new Date());
        	                	   Info info = new Info();
    	                		   info.setName(name);
    	                		   info.setId(userId);
    	                		   info.setOnLineTime(date);
    	                           infos.add(info);
    	                           userInfo = info;
    	                           
    	                           //通知其它所有客户端，有新客户端上线
    	                           JSONObject onlineObj = basicJSONobj(flag,name,userId);
    	                           onlineObj.put("time", date);
    	                           System.out.println("Client run sendonline clients.size="+clients.size());
									for (int i = 0; i < clients.size(); i++) {
										Client c = clients.get(i);
										c.send(onlineObj.toString());

										// 通知新客户端，其它所有已上线客户端信息
										Info otherInfo = c.getMyUserInfo();
										JSONObject alreadOnlineObj = basicJSONobj(flag, otherInfo.getName(),otherInfo.getId());
										alreadOnlineObj.put("time",otherInfo.getOnLineTime());
										send(alreadOnlineObj.toString());
									}
    	                           
        	                   }else if(flag.equals("sendmsg")){ //发送信息
        	                	   String msg = myObj.getString("msg"); //客户端发送文字
        	                	   int touserid = myObj.getInt("touserid");
        	                	   String time = myObj.getString("time");
        	                	   int msgType = myObj.getInt("msgtype");
        	                	   int chatType = myObj.getInt("chattype");
        	                	   
        	                	   JSONObject msgObj = basicJSONobj("sendmsg",name,userId);
        	                	   msgObj.put("msg", msg);
        	                	   msgObj.put("time", time);
        	                	   msgObj.put("msgtype", msgType);
        	                	   msgObj.put("chattype", chatType);
    	                           System.out.println("Client run msg="+msgObj.toString());
    	                           
    	                           String str = msgObj.toString();
    	                           if(touserid == -1){ //群发消息
    	                        	   System.out.println("Client sendmsg to everyone");
    	                        	   for (int i = 0; i < clients.size(); i++) {
											Client c = clients.get(i);
											c.send(str);
										}
									} else { //单聊
										for (int i = 0; i < clients.size(); i++) {
											Client c = clients.get(i);
											int cUserId = c.getMyUserInfo().getId();
											if (cUserId == touserid) {
												System.out.println("Client sendmsg to userId="+ cUserId);
												c.send(str);
												break;
											}
										}
    	                           }
        	                   }else if(flag.equals("offline")){ //下线
        	                	   System.out.println("Client run remove client;clients.size="+clients.size());
    	                		   JSONObject offlineObj = basicJSONobj(flag,name,userId);
    	                           DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	                           String date = df.format(new Date());
    	                           offlineObj.put("time", date);
    	                           for (int i=0; i<clients.size(); i++) {
    	                                 Client c = clients.get(i);
    	                                 c.send(offlineObj.toString());
    	                           }
    	                           clients.remove(this);
    	                           System.out.println("Client run remove client end;clients.size="+clients.size());
    	                           try {
    	                               if (dis != null) dis.close();
    	                               if (dos != null) dos.close();
    	                               if (s != null) s.close();
    	                           } catch (IOException e) {
    	                                   e.printStackTrace();
    	                           }
								} else if (flag.equals("heart_check")) { // 心跳检查
									changHeartNum(-1);
									JSONObject heartCheckObj = basicJSONobj(flag, name, userId);
									DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									String date = df.format(new Date());
									heartCheckObj.put("time", date);
									for (int i = 0; i < clients.size(); i++) {
										Client c = clients.get(i);
										int cUserId = c.getMyUserInfo().getId();
										if (cUserId == this.getMyUserInfo().getId()) {
											System.out.println("Client heart_check to userId=" + cUserId);
											c.send(heartCheckObj.toString());
											break;
										}
									}
        	                   }
							}
                	   } catch (JSONException e) {
                		   // TODO Auto-generated catch block
                		   e.printStackTrace();
                	   }
                   }else{
                	   try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                   }
                }
            } catch (SocketException e) {
                System.out.println("SocketException;client is closed!");
                clients.remove(this);
            } catch (EOFException e) {
                  System.out.println("EOFException;client is closed!");
                  clients.remove(this);
               }
               catch (IOException e) {
                  e.printStackTrace();
               }
              finally {
                try {
                  if (dis != null) dis.close();
                  if (dos != null) dos.close();
                  if (s != null) s.close();
                } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
        }
        
        private JSONObject basicJSONobj(String flag,String username,int userId){
    		JSONObject myObject = new JSONObject();
    		try {
    			myObject.put(JSONKey.FLAG, flag);
    			myObject.put("username", username);
    			myObject.put("userid", userId);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		return myObject;
    	}
        
        private void disConnect(){
        	if(heartTimer != null){
        		heartTimer.cancel();
        		heartTimer = null;
        	}
        	JSONObject offlineObj = basicJSONobj("offline",this.getMyUserInfo().getName(),this.getMyUserInfo().getId());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String date = df.format(new Date());
            try {
				offlineObj.put("time", date);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            for (int i=0; i<clients.size(); i++) {
                  Client c = clients.get(i);
                  c.send(offlineObj.toString());
            }
            clients.remove(this);
            System.out.println("Client run remove client end;clients.size="+clients.size());
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (s != null) s.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }

		// socket心跳
		class socketHeartTask extends TimerTask {
			@Override
			public void run() {
				if (s.isConnected() && dos != null) {
					if (heartSendTime > 3) { // 心跳N次后断开连接
						disConnect();
					} else {
						changHeartNum(1);
					}

				}
			}
		}
        
		private void changHeartNum(int addNum){
			synchronized (heartNumChangeObj) {
				heartSendTime = heartSendTime + addNum;
			}
		}
     }
    class Info{
        private String info_name = null;
        private int info_id = -1;
        private String onLineTime = null;
        public Info(){
            
        }
        public Info(int info_id,String name){
        	this.info_id = info_id;
        	this.info_name = name;
        }
        public void setName(String name){
            info_name = name;
        }
        public String getName(){
            return info_name;
        }
        public void setId(int info_id){
        	this.info_id = info_id;
        }
        public int getId(){
            return info_id;
        }
        public void setOnLineTime(String onLineTime){
        	this.onLineTime = onLineTime;
        }
        public String getOnLineTime(){
            return onLineTime;
        }
    }
}
