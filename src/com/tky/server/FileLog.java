package com.tky.server;

public class FileLog {
	 private int id;  
     private String path;  
       
     public FileLog(int id, String path) {  
         super();  
         this.id = id;  
         this.path = path;  
     }  

     public int getId() {  
         return id;  
     }  

     public void setId(int id) {  
         this.id = id;  
     }  

     public String getPath() {  
         return path;  
     }  
//
     public void setPath(String path) {  
         this.path = path;  
     }  


}
