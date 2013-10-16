package com.tky.server;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* 
* ��������������ini�ļ������ж���д���޸Ĳ���
*  
**/    
public class FileTools {
	public static BufferedReader bufread;
    public static String basePath = "D:/tongxin/";
    
    public static String createFilePath(){
    	
    	String fileName = (new Date()).toString() + ".ini";
    	String filePath = basePath + fileName;
    	return filePath;
    }
    
    /**
     * �����ı��ļ�.
     * @throws IOException 
     * 
     */
    public static File creatIniFile(String filePath) throws IOException{
    	File file = new File(filePath);
        if (!file.exists()) {
        	file.createNewFile();
        }
        return file;
    }
    
    /**
     * ��ȡ�ı��ļ�.
     * 
     */
    public static String readIniFile(File filename){
    	String nowStr = "";
        String read;
//        FileReader fileread;
        try {
//            fileread = new FileReader(filename);
            InputStreamReader inputStream;
			try {
				inputStream = new InputStreamReader(new FileInputStream(filename),"UTF-8");
				bufread = new BufferedReader(inputStream);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   
            try {
                while ((read = bufread.readLine()) != null) {
                	nowStr = nowStr + read+ "\r\n";
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("�ļ�������:"+ "\r\n" + nowStr);
        return nowStr;
    }
    
    /**
     * д�ļ�.
     * 
     */
    public static void writeTxtFile(String newStr, File filename,String oldStr) throws IOException{
        //�ȶ�ȡԭ���ļ����ݣ�Ȼ�����д�����
        String filein = oldStr + "\r\n" + newStr;
        RandomAccessFile mm = null;
        try {
//            mm = new RandomAccessFile(filename, "rw");
//            mm.writeBytes(filein);
            
            
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filename),"UTF-8");   
            BufferedWriter Writer=new BufferedWriter(write);     
            Writer.write(filein);   
            Writer.close();   

        } catch (IOException e1) {
            // TODO �Զ����� catch ��
            e1.printStackTrace();
        } finally {
            if (mm != null) {
                try {
                    mm.close();
                } catch (IOException e2) {
                    // TODO �Զ����� catch ��
                    e2.printStackTrace();
                }
            }
        }
    }
    
    /**
     * ���ļ���ָ�����ݵĵ�һ���滻Ϊ��������.��ʱδʹ�ø÷���
     * 
     * @param oldStr
     *            ��������
     * @param replaceStr
     *            �滻����
     */
    public static void replaceTxtByStr(String oldStr,String replaceStr, String path) {
        String temp = "";
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // �������ǰ�������
            for (int j = 1; (temp = br.readLine()) != null
                    && !temp.equals(oldStr); j++) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }

            // �����ݲ���
            buf = buf.append(replaceStr);

            // ������к��������
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }

            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * ��������
     * @param s
     * @throws IOException
     */
    public static void CreateFile(String newStr) throws IOException {
    	//�ļ����Ƿ���ڣ����������򴴽�
    	File dir = new File(basePath);
    	if(!dir.exists()){
    		dir.mkdirs();
    	}
    	
    	Date now = new Date();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    	String name = dateFormat.format(now);
    	System.out.println("createfile,name="+name);
    	String fileName = name + ".ini";
    	String filePath = basePath + "/"+ fileName;
    	System.out.println("createfile,filePath="+filePath);
    	
    	File file = new File(filePath);
        if (!file.exists()) {
        	file.createNewFile();
        }
        String oldStr = readIniFile(file);
        writeTxtFile(newStr,file,oldStr);
    }
}
