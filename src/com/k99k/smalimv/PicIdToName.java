/**
 * 
 */
package com.k99k.smalimv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.k99k.tools.IOInterface;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class PicIdToName implements IOInterface {

	/**
	 * 
	 */
	public PicIdToName(String filePath,String fileEncode) {
		this.filePath = filePath;
		this.encode = fileEncode;
		this.initOK = this.init();
		if(initOK){
			System.out.println("PicIdToName init OK.");
		}else{
			System.out.println("PicIdToName init failed.");
		}
	}
	
	private boolean initOK = false;
	private String filePath = "";
	private String encode = "utf-8";
	private HashMap<String,String> map = new HashMap<String, String>();
	
	
	private boolean init(){
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.filePath), this.encode));
			String str;
			while ((str = in.readLine()) != null) {
				String[] arr = str.split(",");
				if (arr.length != 3) {
					continue;
				}
				String newVal = "R."+arr[0]+"."+arr[1];
				map.put(arr[2], newVal);
				String a10 = StringUtil.int16to10(arr[2]);
				map.put(a10, newVal);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#doFileContent(java.lang.String)
	 */
	@Override
	public String doFileContent(String fileContent) {
		if (!initOK) {
			System.out.println("init not OK!");
			return fileContent;
		}
		Iterator<Entry<String,String>> it = this.map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> etr = it.next();
			String o = etr.getKey();
			String t = etr.getValue();
			fileContent = fileContent.replaceAll("(?u)"+o, t);
			//fileContent = fileContent.replaceAll(o.toUpperCase(), t);
		}
		return fileContent;
	}

	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#doFilter(java.lang.String)
	 */
	@Override
	public boolean doFilter(String fileName) {
		String last = fileName.substring(fileName.lastIndexOf("."));
		if (last.equals(".java")) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#newFile(java.io.File)
	 */
	@Override
	public File newFile(File fileTo) {
		return fileTo;
	}


	public static void main(String[] args) {
		String enc = "utf-8";
		String file = "/Users/keel/Downloads/dev/public.txt";
		String s = "private static final int[] BTN_More00 = {0x7f020011,0x7f020012,0x7f020011,0x7f020012};";
		PicIdToName p = new PicIdToName(file,enc);
		String re = p.doFileContent(s);
		System.out.println(re);
	}
	
}
