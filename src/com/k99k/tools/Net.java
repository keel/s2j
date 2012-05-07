/**
 * 
 */
package com.k99k.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;



/**
 * 网络相关
 * @author keel
 *
 */
public class Net {

	/**
	 * 
	 */
	public Net() {
	}
	
	/**
	 * get方式获 取url的文本内容
	 * @param url
	 * @param timeOut 超时毫秒数,如3000
	 * @param breakLine 是否加入换行符
	 * @param charset URL页面编码
	 * @return 返回内容String,失败则返回空String
	 */
	public final static String getUrlContent(String url,int timeOut,boolean breakLine,String charset){
		String str = "";
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
//			conn.setRequestProperty("appVersion", appver+"");
			conn.setConnectTimeout(timeOut);
			conn.connect();
			StringBuilder b = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		
			String line;
			if (breakLine) {
				while ((line = reader.readLine()) != null) {
					b.append(line);
					b.append("\r\n"); // 添加换行
				}
			}else{
				while ((line = reader.readLine()) != null) {
					b.append(line);
				}
			}
			
			reader.close();
			str = b.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return str;
	}
	
	/**
	 * post数据到一个url,并获取反回的String,需要拼合参数
	 * @param url Url
	 * @param data 参数合成后的String
	 * @param timeOut 超时毫秒数,如3000
	 * @param breakLine 是否加入换行符
	 * @param charset URL接受的编码，如utf-8
	 * @return 返回的结果页内容,失败则返回空String
	 */
	public final static String postUrl(String url,String data,int timeOut,boolean breakLine,String charset){
		try {
		    URL aUrl = new URL(url);
		    URLConnection conn = aUrl.openConnection();
		    conn.setConnectTimeout(timeOut);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    //System.out.println(data);
		    wr.flush();

		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),charset));
		    String line;
		    StringBuilder sb = new StringBuilder();
		    if (breakLine) {
				while ((line = rd.readLine()) != null) {
					sb.append(line);
					sb.append("\r\n"); // 添加换行
				}
			}else{
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
			}
		    wr.close();
		    rd.close();
		    return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
