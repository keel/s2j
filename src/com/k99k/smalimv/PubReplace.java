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

public class PubReplace implements IOInterface {
	
	private String conf;
	private String ec;
	private HashMap<String,String> map;
	
	public PubReplace(String cf,String ec){
		this.conf = cf;
		this.ec = ec;
		this.init();
	}
	
	void init(){
		this.map = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(conf), ec));
			String str;
//			StringBuilder sb = new StringBuilder();
			while ((str = in.readLine()) != null) {
//				sb.append(str).append("\r\n");
				String[] arr = str.split(",");
				if (arr.length == 3) {
					map.put(arr[2].substring(2), arr[0]);
					
//					PubReplace r = new PubReplace(arr[2].substring(2), arr[0]);
//					IO.copyFullDirWithFn(from, to, r, ec);
//					System.out.println(str);
				}
			}
			
			System.out.println("--------- PubReplace init OK ----------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String doFileContent(String fileContent) {
		Iterator<Entry<String,String>> it = this.map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> etr = it.next();
			String o = etr.getKey();
			String t = etr.getValue();
			fileContent = fileContent.replaceAll(o, t);
			fileContent = fileContent.replaceAll(o.toUpperCase(), t.toUpperCase());
			
		}
		return fileContent;
	}

	@Override
	public boolean doFilter(String fileName) {
		String last = fileName.substring(fileName.lastIndexOf("."));
		if (last.equals(".smali") || last.equals(".xml")) {
			return true;
		}
		return false;
	}

	@Override
	public File newFile(File fileTo) {
		return fileTo;
	}

}
