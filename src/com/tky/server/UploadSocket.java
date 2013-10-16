package com.tky.server;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadSocket {
	private String uploadPath = "D:/uploadFile/";

	private ExecutorService executorService; // �̳߳�
	private ServerSocket ss = null;
	private boolean quit;// �Ƿ��˳�
	boolean started = false;

	public UploadSocket() {
		try {
			ss = new ServerSocket(JSONKey.SOCKET_PORT + 100);
			System.out.println("upload����������");
			started = true;
		} catch (BindException e) {
			System.out.println("upload�˿��Ѿ���ռ��");
			System.exit(0);
		} catch (IOException e) {
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
				System.out.println("accepted connenction from " + socket.getInetAddress() + " @ " + socket.getPort());
				PushbackInputStream inStream = new PushbackInputStream( socket.getInputStream());
				// �õ��ͻ��˷����ĵ�һ��Э�����ݣ�Content-Length=143253434;filename=xxx.3gp;sourceid=
				// ����û������ϴ��ļ���sourceid��ֵΪ�ա�
				String head = StreamTool.readLine(inStream);
				System.out.println("head=" + head);
				if (head != null) {
					// �����Э�������ж�ȡ���ֲ���ֵ
//					String[] items = head.split(";");
//					String flag = items[0].substring(items[0].indexOf("=") + 1);
//					if (flag.equals("video_upload")) { // ��Ƶ�ϴ�
//						uploadVideo(items, inStream);
//					}
					
					try{
						JSONObject headObj = new JSONObject(head);
						String flag = headObj.getString(JSONKey.FLAG);
						if(flag.equals("video_upload")){
							uploadVideo(headObj, inStream);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null && !socket.isClosed())
						socket.close();
				} catch (IOException e) {
				}
			}
		}

		private void uploadVideo(JSONObject headObj, PushbackInputStream inStream) {
			long filelength = 0;
			String filename = null;
			String username = null;
			int sourceid = -1;
			int userId = -1;
			
			try {
				filelength = headObj.getLong("filelength");
				filename = headObj.getString("filename");
				sourceid = headObj.getInt("sourceid");
				userId = headObj.getInt("userid");
				username = headObj.getString("username");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int id = -1;
			FileLog log = null;
			if (sourceid != -1) {
				id = sourceid;
				// log = DBHelper.getUploadFile(id);//�����ϴ����ļ��Ƿ�����ϴ���¼
				log = JSONProcess.getUploadFile(id);
			}
			File file = null;
			int position = 0;
			if (log == null) {// ����ϴ����ļ��������ϴ���¼,Ϊ�ļ���Ӹ��ټ�¼
				String path = new SimpleDateFormat("yyyy/MM/dd/HH/mm")
						.format(new Date());
				File dir = new File(uploadPath + path);
				if (!dir.exists())
					dir.mkdirs();
				file = new File(dir, filename);
				if (file.exists()) {// ����ϴ����ļ�����������Ȼ����и���
					filename = filename.substring(0, filename.indexOf(".") - 1)
							+ dir.listFiles().length
							+ filename.substring(filename.indexOf("."));
					file = new File(dir, filename);
				}
				id = JSONProcess.insertVideoInfo(file, userId); 
			} else {// ����ϴ����ļ������ϴ���¼,��ȡ�ϴεĶϵ�λ��
				file = new File(log.getPath());// ���ϴ���¼�еõ��ļ���·��
				if (file.exists()) {
					File logFile = new File(file.getParentFile(),file.getName() + ".log");
					if (logFile.exists()) {
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
						position = Integer.valueOf(properties.getProperty("length"));// ��ȡ�ϵ�λ��
					}
				}
			}

			OutputStream outStream;
			try {
				outStream = socket.getOutputStream();
//				String response = "sourceid=" + id + ";position=" + position+ "\r\n";
				try{
					JSONObject reponseObj = new JSONObject();
					reponseObj.put(JSONKey.FLAG, "video_upload");
					reponseObj.put("username", username);
					reponseObj.put("userid", userId);
					reponseObj.put("sourceid", sourceid);
					reponseObj.put("position", position);
					// �������յ��ͻ��˵�������Ϣ�󣬸��ͻ��˷�����Ӧ��Ϣ��sourceid=1274773833264;position=0
					// sourceid�ɷ������ɣ�Ψһ��ʶ�ϴ����ļ���positionָʾ�ͻ��˴��ļ���ʲôλ�ÿ�ʼ�ϴ�
					outStream.write((reponseObj.toString()+"\r\n").getBytes());
				}catch(Exception e){
					e.printStackTrace();
				}

				RandomAccessFile fileOutStream = new RandomAccessFile(file,"rwd");
				if (position == 0) {
					fileOutStream.setLength(filelength);// �����ļ�����
				}
				fileOutStream.seek(position);// �ƶ��ļ�ָ����λ�ÿ�ʼд������
				byte[] buffer = new byte[1024];
				int len = -1;
				int length = position;
				while ((len = inStream.read(buffer)) != -1) {// ���������ж�ȡ����д�뵽�ļ���
					fileOutStream.write(buffer, 0, len);
					length += len;
					Properties properties = new Properties();
					properties.put("length", String.valueOf(length));
					FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName() + ".log"));
					properties.store(logFile, null);// ʵʱ��¼�ļ�����󱣴�λ��
					logFile.close();
				}
				if (length == fileOutStream.length()) {
					// delete(id);
					// DBHelper.updateVideoInfo(id);
					boolean upResult = JSONProcess.updateVideoInfo(id);
					if (upResult) {
						System.out.println("�������ݽ���Ϊ100�ɹ���idΪ" + id);
						//�������������ini�ļ�
						try {
							String sql1 = "Update VideosInfo set video_file_upload_percentage = '100' where id ="+id; // �������ݵ�sql���
							FileTools.CreateFile(sql1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
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