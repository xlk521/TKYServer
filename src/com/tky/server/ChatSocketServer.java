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

public class ChatSocketServer {

    List<Client> clients = new ArrayList<Client>();
    private String getnameString=null;
    List<Info> infos = new ArrayList<Info>();
	private ServerSocket ss = null;  
    private int port;// 监听端口  
    private boolean quit;// 是否退出  
    boolean started = false;
  
    public ChatSocketServer(int port) {  
        this.port = port;  
        try {
            ss = new ServerSocket(port);
            System.out.println("服務器啟動");
            started = true;
         } catch (BindException e) {
               System.out.println(" 端口已经被占用");
               System.exit(0);
            }
           catch (IOException e) {
              e.printStackTrace();
           }
    }  
  
	// 启动服务
	public void start() throws Exception {
		if (!started) {
			System.out.println("socket is closed");
			return;
		}
		while (started) {
			System.out.println("start to accept");
			Socket s = ss.accept();
			Client c = new Client (s);
            System.out.println("a client is connected");
            new Thread(c).start();
            clients.add(c);
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
        private String chatKey="SLEEKNETGEOCK4stsjeS";
        private Socket s = null;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;
        private boolean bConnected = false;
        private String sendmsg=null;
        Client (Socket s) {
           this.s = s;
           try {
             dis = new DataInputStream (s.getInputStream());
             dos = new DataOutputStream (s.getOutputStream());
             bConnected = true;
             System.out.println("Client create success");
           } catch(IOException e) {
                 e.printStackTrace();
              }
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
            	   System.out.println("Client run str="+dis.readUTF());
                   String str = dis.readUTF();
                   
                   DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                   String date = "  ["+df.format(new Date())+"]";
                   if(str.startsWith(chatKey+"online:")){
                       Info info = new Info();
                       getnameString = str.substring(27);
                       
                       info.setName(getnameString);
                       infos.add(info);
                       for (int i=0; i<clients.size(); i++) {
                         Client c = clients.get(i);
                         c.send(getnameString+" on line."+date);
                       }
                       System.out.println(getnameString+" on line."+date);
                   }else if(str.startsWith(chatKey+"offline:")){
                       getnameString = str.substring(28);
                       clients.remove(this);
                       for (int i=0; i<clients.size(); i++) {
                             Client c = clients.get(i);
                             c.send(getnameString+" off line."+date);
                           }
                       System.out.println(getnameString+" off line."+date);
                   }
                   else{
                       int charend = str.indexOf("end;");
                       String chatString = str.substring(charend+4);
                       String chatName = str.substring(25, charend);
                       
                       sendmsg=chatName+date+"\n"+chatString; 
                       for (int i=0; i<clients.size(); i++) {
                           Client c = clients.get(i);
                           c.send(sendmsg);
                         }
                       System.out.println(sendmsg);
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
     }
    class Info{
        private String info_name = null;
        public Info(){
            
        }
        public void setName(String name){
            info_name = name;
        }
        public String getName(){
            return info_name;
        }
    }

}
