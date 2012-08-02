/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * @author keel
 *
 */
public class TryCatchScan {
	public TryCatchScan(SentenceMgr mgr,ArrayList<Sentence> senList) {
		this.mgr = mgr;
		this.senList = senList;
		this.len = this.senList.size();
	}
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
	private int len;
	
	
	public void scan(){
		
	}
}
