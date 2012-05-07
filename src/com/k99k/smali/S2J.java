/**
 * 
 */
package com.k99k.smali;

import java.io.IOException;
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
		Header h = new Header(this,null);
		contextMap.put(h.getKey(), h);
		Comm c = new Comm(this,null);
		contextMap.put(c.getKey(), c);
		Fields f = new Fields(this, null);
		contextMap.put(f.getKey(), f);
		Methods m = new Methods(this, null);
		contextMap.put(m.getKey(), m);
	}
	
	private final HashMap<String,Context> contextMap = new HashMap<String, Context>();
	
	public final Context createContext(String key,Context superContext){
		if (key != null && this.contextMap.containsKey(key)) {
			return this.contextMap.get(key).newOne(this,superContext);
		}
		System.out.println("Key is not exist!! key:"+key);
		return null;
	}
	//本类的类名
	String className;
	String packageName;
	
	public String exec(String fileName,String encode){
		StringBuilder sb = new StringBuilder();
		try {
			String t = IO.readTxt("h:/SMSTest.smali", "utf-8");
			ArrayList<String> sa = Tool.strToLine(t);
			Context c = this.createContext(StaticUtil.TYPE_CLASS,null);
			c.render(sa, sb);
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
