/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * @author keel
 *
 */
public class SwitchScan {

	public SwitchScan(SentenceMgr mgr,ArrayList<Sentence> senList) {
		this.mgr = mgr;
		this.senList = senList;
		this.len = this.senList.size();
	}
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
	private int len;
	
	public void scan(){
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch")) {
				SwitchSentence ss = (SwitchSentence)s;
				String key = ss.getKey();
				if (key.equals("packed-switch") || key.equals("sparse-switch")) {
					//定位到switch的起始句,结束位置
					
				}else if(key.startsWith(":pswitch_") || key.startsWith(":sswitch_")){
					//将对应的case块处理后移动到switch之后，并标为over
					
				}
				
				
				
			}
		}
		
		
	}
	
	
	
	
}
