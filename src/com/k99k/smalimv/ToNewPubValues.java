/**
 * 
 */
package com.k99k.smalimv;

import java.io.File;
import java.io.IOException;
import com.k99k.tools.IO;

/**
 * 
 * 批处理ID转换
 * @author keel
 *
 */
public class ToNewPubValues {

	/**
	 * 
	 */
	public ToNewPubValues() {
	}
	
//	public void readJson(){
//		try {
//			String file = IO.readTxt("g:/json.txt", "utf-8");
//			HashMap pubALL = (HashMap) JSON.read(file);
//			if (pubALL == null || pubALL.size() <2) {
//				System.out.println("pubAL err");
//				return;
//			}
//			Iterator it = pubALL.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry entry = (Entry) it.next();
//				String key = (String) entry.getKey();
//				ArrayList ls = (ArrayList) entry.getValue();
////				pubs.put(key, value)
//			}
//
//		
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public static final String a10to16(int ten){
		return Integer.toHexString(ten);
	}
	
	public static final String a16to10(String sixten){
		return Integer.valueOf(sixten,16).toString() ;
	}
	
	
	
	/**
	 * g:/cc3.txt内容为：新的16进制值,public.xml的name,原16进制值
	 * 类似如下：
<pre>	 
7f09007d,face0,0x7f080025
7f09007e,face1,0x7f080026
7f09007f,face2,0x7f080027
7f090080,face3,0x7f080028
7f090081,face4,0x7f080029
</pre>
	 * @param args
	 */
	public static void main(String[] args) {
		String ec = "utf-8";
		String conf = "g:/cc3.txt";
		File from  = new File("F:/android/apk_manager/projects/EgameSocialSdk1.apk");
		File to  = new File("g:/EgameSocialSdk2");
		PubReplace r = new PubReplace(conf,ec);
		try {
			
			IO.copyFullDirWithFn(from, to, r, ec);
			
			System.out.println("--------- END ----------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
