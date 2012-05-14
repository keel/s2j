/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * if结构扫描器
 * @author keel
 *
 */
public class IFStructScan {

	public IFStructScan(SentenceMgr mgr) {
		this.mgr = mgr;
	}
	
	private SentenceMgr mgr;
	
	private HashMap<String,IfSentence> ifCondMap = new HashMap<String, IfSentence>();
	
	private ArrayList<Sentence> ifsList = new ArrayList<Sentence>();
	
	/**
	 * 扫描并处理if结构
	 * @param senList
	 */
	public void scan(ArrayList<Sentence> senList){
		int len = senList.size();
		//先扫描两次将cond和if对应上
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				ifCondMap.put(ifs.getCond(), ifs);
				this.ifsList.add(s);
			}else if(SentenceMgr.isIFS(s.getName())){
				this.ifsList.add(s);
			}
		}
		if (ifCondMap.isEmpty()) {
			//无if结构，直接返回
			return;
		}
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("tag")) {
				TagSentence ts = (TagSentence)s;
				IfSentence ifs = ifCondMap.get(ts.getTag());
				if (ifs != null) {
					//相互设置cond和if
					ifs.setCondTag(ts);
					ts.setIfSen(ifs);
				}
//				else{
					//为:goto_X 标签,跳过
//					continue;
//				}
				
			}
		}
		//-------------------------------------------------------------------------
		//从后向前扫描
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("goto")) {
				GotoSentence gs = (GotoSentence)s;
				Sentence s1 = this.mgr.findLastTag(gs.getTarget(),i);
				Sentence s2 = this.mgr.findIFSentence(false, s1.getLineNum());
//				if (s2) {
//					
//				}
			}else if(s.getName().equals("tag")){
				
			}else if(s.getName().equals("if")){
				
			}else if(s.getName().equals("return")){
				
			}else if(s.getType() == Sentence.TYPE_LINE){
				//有内容输出
				this.doStruct(s.getLineNum());
			}
			
		}
		
		//设置level
		
		//进行over操作
		
	}
	
	/**
	 * 处理内容行外的结构
	 * @param lineNum 内容行的行号
	 */
	private void doStruct(int lineNum){
		Sentence s = this.mgr.findIFSentence(true, lineNum);
		if (s != null && s.getName().equals("if")) {
			IfSentence ifs = (IfSentence)s;
			if (ifs.getCondTag().getLineNum() > lineNum) {
				
			}
		}
		
		
	}
	

}
