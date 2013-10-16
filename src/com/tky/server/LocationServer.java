package com.tky.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocationServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public class HttpRequest {
	    /**
	     * ��ָ��URL����GET����������
	     * 
	     * @param url
	     *            ���������URL
	     * @param param
	     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
	     * @return URL ������Զ����Դ����Ӧ���
	     */
	    public String sendGet(String url, String param) {
	        String result = "";
	        BufferedReader in = null;
	        try {
	            String urlNameString = url + "?" + param;
	            URL realUrl = new URL(urlNameString);
	            // �򿪺�URL֮�������
	            URLConnection connection = realUrl.openConnection();
	            // ����ͨ�õ���������
	            connection.setRequestProperty("accept", "*/*");
	            connection.setRequestProperty("connection", "Keep-Alive");
	            connection.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            // ����ʵ�ʵ�����
	            connection.connect();
	            // ��ȡ������Ӧͷ�ֶ�
	            Map<String, List<String>> map = connection.getHeaderFields();
	            // �������е���Ӧͷ�ֶ�
	            for (String key : map.keySet()) {
	                System.out.println(key + "--->" + map.get(key));
	            }
	            // ���� BufferedReader����������ȡURL����Ӧ
	            in = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream()));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("����GET��������쳣��" + e);
	            e.printStackTrace();
	        }
	        // ʹ��finally�����ر�������
	        finally {
	            try {
	                if (in != null) {
	                    in.close();
	                }
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
	        }
	        return result;
	    }
	    /**
	     * ��ָ�� URL ����POST����������
	     * 
	     * @param url
	     *            ��������� URL
	     * @param param
	     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
	     * @return ������Զ����Դ����Ӧ���
	     */
	    public String sendPost(String url, String param) {
	        PrintWriter out = null;
	        BufferedReader in = null;
	        String result = "";
	        try {
	            URL realUrl = new URL(url);
	            // �򿪺�URL֮�������
	            URLConnection conn = realUrl.openConnection();
	            // ����ͨ�õ���������
	            conn.setRequestProperty("accept", "*/*");
	            conn.setRequestProperty("connection", "Keep-Alive");
	            conn.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            // ����POST�������������������
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            // ��ȡURLConnection�����Ӧ�������
	            out = new PrintWriter(conn.getOutputStream());
	            // �����������
	            out.print(param);
	            // flush������Ļ���
	            out.flush();
	            // ����BufferedReader����������ȡURL����Ӧ
	            in = new BufferedReader(
	                    new InputStreamReader(conn.getInputStream()));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("���� POST ��������쳣��"+e);
	            e.printStackTrace();
	        }
	        //ʹ��finally�����ر��������������
	        finally{
	            try{
	                if(out!=null){
	                    out.close();
	                }
	                if(in!=null){
	                    in.close();
	                }
	            }
	            catch(IOException ex){
	                ex.printStackTrace();
	            }
	        }
	        return result;
	    }    

	}

	   /** ��ȡ����λ�� */
    private String getLocation(SItude itude) throws Exception {
    	 String addressStr = "";
    	 /** �������get������ֱ�ӽ������ӵ�URL�� */
 	    String urlString = String.format("http://maps.google.cn/maps/geo?key=abcdefg&q=%s,%s", itude.latitude, itude.longitude);
 	    System.out.println("getLocation;urlstring="+urlString);

 	   HttpRequest request = new HttpRequest();
 	    
 	   
        
//        //���� POST ����
//        String sr=request.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
//        System.out.println(sr);

 	    try {
 	        /** ����GET���󲢻�÷������� */
 	    	//���� GET ����
 	        String result=request.sendGet("http://maps.google.cn/maps/geo", "key="+itude.latitude+"&v="+itude.longitude);
 	        System.out.println("getresult="+result);

 	        /** ����JSON���ݣ���������ַ */
 	        if (result != null && result.length() > 0) {
 	            JSONObject jsonobject = new JSONObject(result);
 	            JSONArray jsonArray = new JSONArray(jsonobject.get("Placemark").toString());
 	            result = "";
 	            for (int i = 0; i < jsonArray.length(); i++) {
 	            	addressStr = jsonArray.getJSONObject(i).getString("address");
 	            }
 	        }
 	    } catch (Exception e) {
 	        throw new Exception("��ȡ����λ�ó��ִ���:" + e.getMessage());
 	    } 
    	 return addressStr;
    }
    
    /** ��γ����Ϣ�ṹ�� */
    public class SItude{
        public double latitude; //γ��
        public double longitude; //����
    }
}
