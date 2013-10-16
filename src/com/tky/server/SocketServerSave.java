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
 *上传文件接收
 */
public class SocketServerSave {

	private String uploadPath="I:/uploadFile/";  
    private ExecutorService executorService;	// 线程池  
    private ServerSocket ss = null;  
    private int port;// 监听端口  
    private boolean quit;// 是否退出  
    boolean started = false;
    
    //chat
    List<Client> clients = new ArrayList<Client>();
    private String getnameString=null;
    List<Info> infos = new ArrayList<Info>();
  
    public SocketServerSave(int port) {  
        this.port = port;  
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
        // 初始化线程池  
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);  
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
			System.out.println("start to accept;s.getPort()="+s.getPort());
			// 为支持多用户并发访问，采用线程池管理每一个用户的连接请求
			executorService.execute(new SocketTask(s));// 启动一个线程来处理请求
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
                //得到客户端发来的第一行协议数据：Content-Length=143253434;filename=xxx.3gp;sourceid=  
                //如果用户初次上传文件，sourceid的值为空。  
                String head = StreamTool.readLine(inStream);  
                System.out.println("head="+head);  
                if (head != null) {  
                    // 下面从协议数据中读取各种参数值  
                    String[] items = head.split(";");  
                    String flag = items[0].substring(items[0].indexOf("=") + 1);
                    if(flag.equals("video_upload")){ //视频上传
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
//                 log = DBHelper.getUploadFile(id);//查找上传的文件是否存在上传记录  
                 log = JSONProcess.getUploadFile(id);
             }  
             File file = null;  
             int position = 0;  
             if(log==null){//如果上传的文件不存在上传记录,为文件添加跟踪记录  
                 String path = new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(new Date());  
                 File dir = new File(uploadPath+ path);  
                 if(!dir.exists()) dir.mkdirs();  
                 file = new File(dir, filename);  
                 if(file.exists()){//如果上传的文件发生重名，然后进行改名  
                     filename = filename.substring(0, filename.indexOf(".")-1)+ dir.listFiles().length+ filename.substring(filename.indexOf("."));  
                     file = new File(dir, filename);  
                 }  
//                 save(id, file);  
                 id =  JSONProcess.insertVideoInfo(file,Integer.parseInt(userId)); // 测试暂时使用userid为1；
//                   id =  JSONProcess.insertVideoInfo(file,userId); //userID改为了int
             }else{// 如果上传的文件存在上传记录,读取上次的断点位置  
                 file = new File(log.getPath());//从上传记录中得到文件的路径  
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
                         position = Integer.valueOf(properties.getProperty("length"));//读取断点位置  
                     }  
                 }  
             }  
               
             OutputStream outStream;
			try {
				outStream = socket.getOutputStream();
				String response = "sourceid="+ id+ ";position="+ position+ "\r\n";  
	             //服务器收到客户端的请求信息后，给客户端返回响应信息：sourceid=1274773833264;position=0  
	             //sourceid由服务生成，唯一标识上传的文件，position指示客户端从文件的什么位置开始上传  
	             outStream.write(response.getBytes());  
	               
	             RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");  
	             if(position==0){
	            	 fileOutStream.setLength(Integer.valueOf(filelength));//设置文件长度  
	             }
	             fileOutStream.seek(position);//移动文件指定的位置开始写入数据  
	             byte[] buffer = new byte[1024];  
	             int len = -1;  
	             int length = position;  
	             while( (len=inStream.read(buffer)) != -1){//从输入流中读取数据写入到文件中  
	                 fileOutStream.write(buffer, 0, len);  
	                 length += len;  
	                 Properties properties = new Properties();  
	                 properties.put("length", String.valueOf(length));  
	                 FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName()+".log"));  
	                 properties.store(logFile, null);//实时记录文件的最后保存位置  
	                 logFile.close();  
	             }  
	             if(length==fileOutStream.length()){
//	             	delete(id);  
//	             	DBHelper.updateVideoInfo(id);
	             	boolean upResult = JSONProcess.updateVideoInfo(id);
	             	if(upResult){
	             		System.out.println("更新数据进度为100成功的id为" + id);
	             	}else{
	             		System.out.println("更新数据进度为100失败的id为" + id);
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
