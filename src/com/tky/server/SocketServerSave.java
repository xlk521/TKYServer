package com.tky.server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tky.server.ChatSocketServer.Client;
import com.tky.server.ChatSocketServer.Info;

//import SocketServer.FileLog;
//import SocketServer.SocketTask;
/**
 * 
 * @author HJ
 *�ϴ��ļ�����
 */
public class SocketServerSave {

	private String uploadPath="I:/uploadFile/";  
    private ExecutorService executorService;	// �̳߳�  
    private ServerSocket ss = null;  
    private int port;// �����˿�  
    private boolean quit;// �Ƿ��˳�  
    boolean started = false;
    
    //chat
    List<Client> clients = new ArrayList<Client>();
    private String getnameString=null;
    List<Info> infos = new ArrayList<Info>();
  
    public SocketServerSave(int port) {  
        this.port = port;  
        try {
            ss = new ServerSocket(port);
            System.out.println("����������");
            started = true;
         } catch (BindException e) {
               System.out.println(" �˿��Ѿ���ռ��");
               System.exit(0);
            }
           catch (IOException e) {
              e.printStackTrace();
           }
        // ��ʼ���̳߳�  
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);  
    }  
  
	// ��������
	public void start() throws Exception {
		if (!started) {
			System.out.println("socket is closed");
			return;
		}
		while (started) {
			System.out.println("start to accept");
			Socket s = ss.accept();
			System.out.println("start to accept;s.getPort()="+s.getPort());
			// Ϊ֧�ֶ��û��������ʣ������̳߳ع���ÿһ���û�����������
			executorService.execute(new SocketTask(s));// ����һ���߳�����������
		}

	} 
  
    // �˳�  
    public void quit() {  
        this.quit = true;
        this.started = false;
        try {  
            ss.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
    private class SocketTask implements Runnable {  
        private Socket socket;  
  
        public SocketTask(Socket socket) {  
            this.socket = socket;  
        }  
  
        @Override  
        public void run() {  
            try {  
                System.out.println("accepted connenction from "  + socket.getInetAddress() + " @ " + socket.getPort());  
                PushbackInputStream inStream = new PushbackInputStream( socket.getInputStream());  
                //�õ��ͻ��˷����ĵ�һ��Э�����ݣ�Content-Length=143253434;filename=xxx.3gp;sourceid=  
                //����û������ϴ��ļ���sourceid��ֵΪ�ա�  
                String head = StreamTool.readLine(inStream);  
                System.out.println("head="+head);  
                if (head != null) {  
                    // �����Э�������ж�ȡ���ֲ���ֵ  
                    String[] items = head.split(";");  
                    String flag = items[0].substring(items[0].indexOf("=") + 1);
                    if(flag.equals("video_upload")){ //��Ƶ�ϴ�
                    	uploadVideo(items,inStream);
                    }
                }
            } catch (Exception e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    if(socket != null && !socket.isClosed()) socket.close();  
                } catch (IOException e) {}  
            }  
        }  
  
        private void uploadVideo(String[] items,PushbackInputStream inStream){
        	 String filelength = items[1].substring(items[1].indexOf("=") + 1);  
             String filename = items[2].substring(items[2].indexOf("=") + 1);  
             String sourceid = items[3].substring(items[3].indexOf("=") + 1);
             String userId = items[4].substring(items[4].indexOf("=") + 1);
             int id = -1;  
             FileLog log = null;  
             if (null != sourceid && !"".equals(sourceid)) {  
                 id = Integer.valueOf(sourceid);  
//                 log = DBHelper.getUploadFile(id);//�����ϴ����ļ��Ƿ�����ϴ���¼  
                 log = JSONProcess.getUploadFile(id);
             }  
             File file = null;  
             int position = 0;  
             if(log==null){//����ϴ����ļ��������ϴ���¼,Ϊ�ļ���Ӹ��ټ�¼  
                 String path = new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(new Date());  
                 File dir = new File(uploadPath+ path);  
                 if(!dir.exists()) dir.mkdirs();  
                 file = new File(dir, filename);  
                 if(file.exists()){//����ϴ����ļ�����������Ȼ����и���  
                     filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                     file = new File(dir, filename);  
                 }  
//                 save(id, file);  
                 id =  JSONProcess.insertVideoInfo(file,Integer.parseInt(userId)); // ������ʱʹ��useridΪ1��
//                   id =  JSONProcess.insertVideoInfo(file,userId); //userID��Ϊ��int
             }else{// ����ϴ����ļ������ϴ���¼,��ȡ�ϴεĶϵ�λ��  
                 file = new File(log.getPath());//���ϴ���¼�еõ��ļ���·��  
                 if(file.exists()){  
                     File logFile = new File(file.getParentFile(), file.getName()+".log");  
                     if(logFile.exists()){  
                         Properties properties = new Properties();  
                         try {
							properties.load(new FileInputStream(logFile));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  
                         position = Integer.valueOf(properties.getProperty("length"));//��ȡ�ϵ�λ��  
                     }  
                 }  
             }  
               
             OutputStream outStream;
			try {
				outStream = socket.getOutputStream();
				String response = "sourceid="+ id+ ";position="+ position+ "\r\n";  
	             //�������յ��ͻ��˵�������Ϣ�󣬸��ͻ��˷�����Ӧ��Ϣ��sourceid=1274773833264;position=0  
	             //sourceid�ɷ������ɣ�Ψһ��ʶ�ϴ����ļ���positionָʾ�ͻ��˴��ļ���ʲôλ�ÿ�ʼ�ϴ�  
	             outStream.write(response.getBytes());  
	               
	             RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");  
	             if(position==0){
	            	 fileOutStream.setLength(Integer.valueOf(filelength));//�����ļ�����  
	             }
	             fileOutStream.seek(position);//�ƶ��ļ�ָ����λ�ÿ�ʼд������  
	             byte[] buffer = new byte[1024];  
	             int len = -1;  
	             int length = position;  
	             while( (len=inStream.read(buffer)) != -1){//���������ж�ȡ����д�뵽�ļ���  
	                 fileOutStream.write(buffer, 0, len);  
	                 length += len;  
	                 Properties properties = new Properties();  
	                 properties.put("length", String.valueOf(length));  
	                 FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
	                 properties.store(logFile, null);//ʵʱ��¼�ļ�����󱣴�λ��  
	                 logFile.close();  
	             }  
	             if(length==fileOutStream.length()){
//	             	delete(id);  
//	             	DBHelper.updateVideoInfo(id);
	             	boolean upResult = JSONProcess.updateVideoInfo(id);
	             	if(upResult){
	             		System.out.println("�������ݽ���Ϊ100�ɹ���idΪ" + id);
	             	}else{
	             		System.out.println("�������ݽ���Ϊ100ʧ�ܵ�idΪ" + id);
	             	}
	             }
	             fileOutStream.close();                    
	             outStream.close();  
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            file = null;  
        }
    }  
}
