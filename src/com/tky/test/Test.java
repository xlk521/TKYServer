package com.tky.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jasper.tagplugins.jstl.core.Param;

import com.tky.server.JDBCServer;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JDBCServer jdbcServer=new JDBCServer();
		jdbcServer.getConnection();
		String sql = "select * from JcEmployee where E_name=? and Password=?";
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>(); 
		try {
			List<Object> params=new ArrayList<Object>();
			params.add("¿Ó—Ô");
			params.add("123456");
			list=jdbcServer.findMoreResult(sql,params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0;i<list.size();i++){
			Map<String, Object> map=new HashMap<String,Object>();
			map=list.get(i);
			System.out.println(map);
		}
		
	}

}
