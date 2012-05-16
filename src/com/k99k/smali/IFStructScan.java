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

	public IFStructScan(SentenceMgr mgr,ArrayList<Sentence> senList) {
		this.mgr = mgr;
		this.senList = senList;
		this.len = this.senList.size();
	}
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
	private int len;
	
	private HashMap<String,TagSentence> condMap = new HashMap<String, TagSentence>();
	
//	private ArrayList<Sentence> ifsList = new ArrayList<Sentence>();
	
	/**
	 * 某一内容块相关的if语句集
	 */
	private ArrayList<Sentence> ifls = new ArrayList<Sentence>();
	/**
	 * 是否在if结构收集中
	 */
	private boolean look = false;
//	/**
//	 * 是否已包含内容块
//	 */
//	private boolean contentIn = false;
	
	private int contStart = -1;
	private int contEnd = -1;
	
	/**
	 * 扫描并处理if结构
	 * @param senList
	 */
	public void scan(){
		this.init();
		//-------------------------------------------------------------------------
		//从后向前扫描
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			
			if (!look) {
				//不在if范围内时
				if (!SentenceMgr.isIFS(s.getName())) {
					continue;
				}
				if(SentenceMgr.isIFS(s.getName())){
					//进入if范围
					if (!look) {
						look = true;
					}
					ifls.add(s);
					continue;
				}
			}else{
				//if范围界定
				if (this.contStart == -1) {
					//无内容块时
					if (SentenceMgr.isIFS(s.getName())) {
						ifls.add(s);
						continue;
					}else if (s.getType() == Sentence.TYPE_LINE) {
						this.contStart = s.getLineNum();
//						ifls.add(s);
						continue;
					}
				}else{
					//有内容块时
					if (SentenceMgr.isIFS(s.getName())) {
						if (this.contEnd == -1) {
							this.contEnd = s.getLineNum()+1;
						}
						ifls.add(s);
						continue;
					}else if (s.getType() == Sentence.TYPE_LINE) {
						if (this.contEnd == -1) {
							//内容块还未结束，继续
							continue;
						}
						//新内容块出现,需要分析界定if
						if(this.doStruct()){
							//已全部处理完
							ifls = new ArrayList<Sentence>();
							look = false;
							this.contEnd  = -1;
							this.contStart = -1;
						}
					}
				}
				
				
			}
			
			
			
			/*
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
			}*/
			
			
			
		}
		
		//设置level
		
		//进行over操作
		
	}
	
	/**
	 * 分析并处理某一内容块的if结构
	 * @return 返回是否所包含语句全部处理完
	 */
	private boolean doStruct(){
		if (this.ifls.isEmpty()) {
			return true;
		}
		int le = this.ifls.size();
		//仍然是从后向前的顺序,
		for (int i = 0; i < le; i++) {
			Sentence s = ifls.get(i);
			/*
			//先处理goto
			if (s.getName().equals("goto")) {
				GotoSentence g = (GotoSentence)s;
				TagSentence t = g.getTargetSen();
				if (t.getLineNum() < g.getLineNum()) {
				}
				continue;
			}*/
			
			//先查找内容块之上的第一个if
			if (s.getLineNum() < this.contEnd && s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence ifCond = (TagSentence)ifs.getCondTag();
				if (ifCond.getLineNum() > this.contStart) {
					ifs.reverseCompare();
					ifs.over();
					ifCond.setOut("}");
					ifCond.over();
				}else{
					
				}
			}
		}
		
		return true;
	}
	

	/**
	 * 初始化cond和ifsen以及goto的对应关系，return语句后置
	 */
	private void init(){
		//是否需要将return后置,0表示未处理或不处理,1:不需要,2:需要,3处理中
		int returnToEnd = 0;
		//先扫描两次将cond和if对应上
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("tag")) {
				TagSentence ts = (TagSentence)s;
				condMap.put(ts.getTag(), ts);
			}else if(returnToEnd==0){
				if (s.getType() == Sentence.TYPE_LINE) {
					returnToEnd = 2;
				}else if(s.getName().equals("return")){
					returnToEnd = 1;
				}
			}
		}
		if (condMap.isEmpty()) {
			//无tag结构，直接返回
			return;
		}
		int returnLine = -1;
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence ts = condMap.get(ifs.getCond());
				if (ts != null) {
					ifs.setCondTag(ts);
					ts.setIfSen(ifs);
				}else{
					System.err.println("ifs cond not found:"+ifs.getCond());
				}
			}else if(s.getName().equals("goto")){
				GotoSentence gs = (GotoSentence)s;
				TagSentence ts = condMap.get(gs.getTarget());
				if (ts != null) {
					ts.setIfSen(gs);
					gs.setTargetSen(ts);
				}else{
					System.err.println("goto cond not found:"+gs.getTarget());
				}
			}else if(returnToEnd >= 2){
				if (returnLine == -1 && s.getName().equals("return")) {
					returnLine = this.senList.indexOf(s);
					returnToEnd = 3;
				}else if(returnToEnd == 3){
					if (s.getName().equals("tag")) {
						returnLine = this.senList.indexOf(s);
					}else{
						returnToEnd = 0;//不再处理
					}
				}
			}
		}
		
		//return 后置
		if (returnLine > -1) {
			ArrayList<Sentence> temp = new ArrayList<Sentence>();
			int last = this.senList.get(this.len-1).getLineNum();
			while (true) {
				Sentence s1 = this.senList.get(returnLine);
				if (s1.getName().equals("return")) {
					s1.setLineNum(last+1);
					temp.add(s1);
					this.senList.remove(s1);
					break;
				}else if (s1.getName().equals("if") || s1.getName().equals("goto")) {
					break;
				}else{
					s1.setLineNum(last+1);
					temp.add(s1);
					this.senList.remove(s1);
					last++;
					continue;
				}
			}
			this.senList.addAll(this.senList.size(), temp);
		}
		
	}
	
}
