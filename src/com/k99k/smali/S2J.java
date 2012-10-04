/**
 * 
 */
package com.k99k.smali;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.smalimv.PubReplace;
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
		Annotation a = new Annotation(this, null,null);
		contextMap.put(a.getKey(), a);
	}
	
	static final Logger log = Logger.getLogger(S2J.class);
	
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
	
	private static S2J s2j = new S2J();
	
	public static final S2J getS2J(){
		return s2j;
	}
	
	public String exec(String fileName,String encode){
		try {
			String t = IO.readTxt(fileName, encode);
			return exec(t);
		} catch (Exception e) {
			log.error(" ERROR: "+e.getStackTrace()[0]);
			e.printStackTrace();
		}
		return "ERR";
	}
	
	public String debug(String fileName,String encode){
		try {
			String t = IO.readTxt(fileName, encode);
			StringBuilder sb = new StringBuilder();
			try {
				ArrayList<String> lines = Tool.strToLine(t);
				while (!lines.isEmpty()) {
					String nextLine = lines.get(0);
					String key = Tool.getKey(nextLine);
					if (key != null) {
						Context con = this.createContext(key,lines,sb);
						if (con!=null) {
							con.debug();
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
				log.error(t+" ERROR: "+e.getStackTrace()[0]);
				e.printStackTrace();
			}
			return sb.toString();
		} catch (Exception e) {
			log.error(" ERROR: "+e.getStackTrace()[0]);
			e.printStackTrace();
		}
		return "ERR";
	}
	
	public String exec(String t){
		StringBuilder sb = new StringBuilder();
		try {
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
			log.error(t+" ERROR: "+e.getStackTrace()[0]);
			e.printStackTrace();
			
		}
		return sb.toString();
	}
	
	/**
	 * 转换整个目录,编码为utf-8
	 * @param srcFolder
	 * @param targetFolder
	 */
	public static final void doFolder(String srcFolder,String targetFolder){
		String ec = "utf-8";
		File from  = new File(srcFolder);
		File to  = new File(targetFolder);
		FolderS2J r = new FolderS2J();
		try {
			
			IO.copyFullDirWithFn(from, to, r, ec);
			
		} catch (IOException e) {
			log.error(" ERROR: "+e.getStackTrace()[0]);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		S2J s = new S2J();
		//String re = s.exec("h:/SMSTest.smali", "utf-8");
		
		
//		String tar = "f:/android/apk_manager/projects/SmaliTest.apk/smali/com/smlon/tools/Structs.smali";
//		//String tar = "F:/android/apk_manager/projects/ud.apk/smali/net/gatools/SpriteManager.smali";
//		String re = s.exec(tar, "utf-8");
//		
//		System.out.println(re);
		
		
		
		
		String from  = "F:/android/apk_manager/projects/com.game.UnicornDash.apk/smali";
		String to = "g:/UnicornDash";
		doFolder(from, to);
		System.out.println("--------- END ----------");
		
		
		/*
		//转换整个目录
		String ec = "utf-8";
		File from  = new File("F:/android/apk_manager/projects/ud.apk/smali");
		File to  = new File("g:/ud");
		FolderS2J r = new FolderS2J();
		try {
			
			IO.copyFullDirWithFn(from, to, r, ec);
			System.out.println("--------- END ----------");
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

}
