/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import com.k99k.tools.IO;

/**
 * S2J主类
 * @author keel
 *
 */
public class S2J {

	/**
	 * 初始化Context
	 */
	public S2J() {
		Header h = new Header(this,null,null);
		contextMap.put(h.getKey(), h);
		Comm c = new Comm(this,null,null);
		contextMap.put(c.getKey(), c);
		Fields f = new Fields(this, null,null);
		contextMap.put(f.getKey(), f);
		Methods m = new Methods(this, null,null);
		contextMap.put(m.getKey(), m);
	}
	
	private final HashMap<String,Context> contextMap = new HashMap<String, Context>();
	
	public final Context createContext(String key,ArrayList<String> lines,StringBuilder out){
		if (key != null && this.contextMap.containsKey(key)) {
			return this.contextMap.get(key).newOne(this,lines,out);
		}
		System.out.println("Key is not exist!! key:"+key);
		return null;
	}
	/**
	 * 本类的类名
	 */
	String className;
	/**
	 * 包名
	 */
	String packageName;
	
	public String exec(String fileName,String encode){
		StringBuilder sb = new StringBuilder();
		try {
			String t = IO.readTxt("h:/SMSTest.smali", "utf-8");
			ArrayList<String> lines = Tool.strToLine(t);
			
			while (!lines.isEmpty()) {
				String nextLine = lines.get(0);
				String key = Tool.getKey(nextLine);
				if (key != null) {
					Context con = this.createContext(key,lines,sb);
					if (con!=null) {
						con.render();
					}else{
						sb.append("//FIXME context not found! key: "+key);
					}
				}else{
					sb.append("//FIXME key is empty! line: "+nextLine);
				}
			}
			//结束
			sb.append(StaticUtil.NEWLINE).append("}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		S2J s = new S2J();
		String re = s.exec("h:/SMSTest.smali", "utf-8");
		System.out.println(re);
	}

}
