/**
 * 
 */
package com.k99k.smalimv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.k99k.tools.IO;
import com.k99k.tools.IOInterface;
import com.k99k.tools.JSON;

/**
 * 
 * 获取
 * @author keel
 *
 */
public class PubValues {

	/**
	 * 
	 */
	public PubValues() {
	}
	
	static HashMap<String,Integer> pubs = new HashMap<String, Integer>();
	static HashMap<String,String> pubIds = new HashMap<String, String>();
	static HashMap<String,Object> root = new HashMap<String, Object>();
	
	static{
//		pubs.put("drawable", "2130837516,118");
//		pubs.put("layout", "2130903040,23");
//		pubs.put("array", "2130968576,3");
//		pubs.put("color", "2131034112,2");
//		pubs.put("string", "2131099648,38");
//		pubs.put("style", "2131165184,9");
//		pubs.put("id", "2131230720,139");
//		
		
		
	}
	
	public void makeNewIds(){
		
	}
	
	public void readJson(){
		try {
			String file = IO.readTxt("g:/json.txt", "utf-8");
			HashMap pubALL = (HashMap) JSON.read(file);
			if (pubALL == null || pubALL.size() <2) {
				System.out.println("pubAL err");
				return;
			}
			Iterator it = pubALL.entrySet().iterator();
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				String key = (String) entry.getKey();
				ArrayList ls = (ArrayList) entry.getValue();
//				pubs.put(key, value)
			}

		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static final String a10to16(int ten){
		return Integer.toHexString(ten);
	}
	
	public static final String a16to10(String sixten){
		return Integer.valueOf(sixten,16).toString() ;
	}
	
	public static void main(String[] args) {
		String ec = "utf-8";
		String conf = "g:/cc3.txt";
		File from  = new File("F:/android/apk_manager/projects/EgameSocialSdk.apk");
		File to  = new File("g:/EgameSocialSdk");
		PubReplace r = new PubReplace(conf,ec);
		try {
			
			IO.copyFullDirWithFn(from, to, r, ec);
			
			System.out.println("--------- END ----------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
