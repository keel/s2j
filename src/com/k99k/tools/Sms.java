/**
 * 
 */
package com.k99k.tools;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 短信接口
 * @author keel
 *
 */
public class Sms {
	

	private final static String dburl = "jdbc:microsoft:sqlserver://202.102.40.43:1433;DatabaseName=cdma_sms";

	private final static String dbuser = "sa";

	private final static String dbpwd = "chqzsjzxx";
	
	private static Connection conn;
	
	/**
	 * 设置数据库
	 * @return 数据库连接
	 */
	private static final Connection getConn() {
		try {
			if (conn==null || conn.isClosed()) {
				Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver")
				.newInstance();
				conn = DriverManager.getConnection(dburl, dbuser, dbpwd);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		return conn;
	}

	
	/**
	 * 发送单条短信
	 * @param phone
	 * @param content
	 * @return
	 */
	public static final boolean sendOne(String phone,String content){
		try {
			Connection conn = getConn();
			CallableStatement cstmt = conn.prepareCall("{call sendFreeSms(?,?,?)}");
			cstmt.setString(1, phone);
			cstmt.setString(2, content);
			cstmt.setString(3, phone);
			cstmt.executeUpdate();
			cstmt.close();
			//conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
//	public static void main(String[] args) {
//		System.out.println(sendOne("15301588025","发送测试test ..sat"));
//	}
}
