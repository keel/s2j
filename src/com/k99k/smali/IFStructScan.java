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
	
//	/**
//	 * 某一内容块相关的if语句集
//	 */
//	private ArrayList<Sentence> ifls = new ArrayList<Sentence>();

//	/**
//	 * 是否已包含内容块
//	 */
//	private boolean contentIn = false;
	
	/**
	 * 内容块反向开始的lineNum,注意是反向开始,数值>contStart
	 */
	private int contStart = -1;
	/**
	 * 内容块反向结束的lineNum,注意是反向结束,数值<contStart
	 */
	private int contEnd = -1;
	/**
	 * return开始行所在的senList中的index,保存此位置以在return后置后识别后置前if的真正endSen
	 */
	private int returnLine = -1;
	
	/**
	 * return 所在的index
	 */
	private int returnIndex = -1;
	
	/**
	 * 本语句中最大的lineNum
	 */
	private int maxLineNum = -1;
	/**
	 * contStart 所在Sen在senList中的index,注意是内容块下方结束位置
	 */
	private int contStartIndex = -1;
	
	/**
	 * contEnd所在Sen在senList中的index,注意是内容块上方结束位置
	 */
	private int contEndIndex = -1;
	/**
	 * 每个if内容块内结束标记map,用于判断是否为else
	 */
	private HashMap<Integer,IfSentence> ifEndMap = new HashMap<Integer, IfSentence>();
	
	/**
	 * 扫描并处理if结构
	 * @param senList
	 */
	public void scan(){
		if(!this.init()){
			return;
		}
		//处理do while结构
		//this.scanDoWhile();
		//处理while结构
		this.scanReversedIf();
		
		
		//return 语句后置
		this.backReturnSens();
		
		this.scanWhileStruct();
		
		//-------------------------------------------------------------------------
		//if结构中包含return时只处理一次
		boolean isReturned = true;
		//从后向前扫描
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				IfSentence ifs = (IfSentence)s;
				TagSentence ifTag = ifs.getCondTag();
				int endIfTagIndex = this.senList.indexOf(ifTag);
				//向下查找内容块
				boolean firstContent = true;
				for (int j = i+1; j < endIfTagIndex; j++) {
					Sentence sc = this.senList.get(j);
					if (sc.getName().equals("return")) {
						//中间被return中断,需要将ifs的tag指向return下方的tag
						if (ifTag.isReturn() && isReturned) {
							//ifs的对应tag指向return时,更换ifs的tag为内容块下方的tag
							for (int k = j+1; k < endIfTagIndex; k++) {
								Sentence tagAfterReturn = this.senList.get(k);
								if (tagAfterReturn.getName().equals("tag")) {
									TagSentence ts = (TagSentence)tagAfterReturn;
									ifs.setCondTag(ts);
									ifs.setCond(ts.getTag());
									break;
								}
							}
							isReturned = false;
							break;
						}else{
							isReturned = false;
						}
					}
					if (sc.getType() == Sentence.TYPE_LINE || (sc.getName().equals("if") && sc.getState() == Sentence.STATE_OVER)) {
						if (firstContent) {
							this.contEndIndex = j;
							this.contEnd = sc.getLineNum();
							firstContent = false;
						}
						this.contStartIndex = j;
						this.contStart = sc.getLineNum();
					}
				}
				if(this.doIfStruct()){
					i = this.contEndIndex;
				}
			}
			
		}
	}
	
	/**
	 * 分析并处理某一内容块的if结构
	 * @return 返回是否所包含语句全部处理完
	 */
	private boolean doIfStruct(){
//		if (this.ifls.isEmpty()) {
//			return true;
//		}
//		int le = this.ifls.size();
		if (contStartIndex == -1 || contEndIndex == -1) {
			return false;
		}
		IfSentence ifs = null;
		int ifEnd = -1;
		//正向最开始的第一个if
		int condStartIndex = -1;
		//确定内容块最上方if位置,可能为内容块下方tag指向的最上方if
		int topIfIndex = 0;
		Sentence afterContSen = this.senList.get((contStartIndex+1 == this.len)?contStartIndex:contStartIndex+1);
		if (afterContSen.getName().equals("tag")) {
			//tag对应的最上方if
			IfSentence topIf = (IfSentence) ((TagSentence)afterContSen).getIfSen();
			topIfIndex = this.senList.indexOf(topIf);
		}
		//进一步确认if最上方的位置topIfIndex
		for (int i = this.contEndIndex+1; i >= topIfIndex; i--) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag")) {
				TagSentence tag = (TagSentence)s;
				int ti = this.senList.indexOf(tag.getIfSen());
				if (ti < topIfIndex) {
					topIfIndex = ti;
				}
			}
		}
		
		//仍然是从后向前的顺序,确定最上方的if位置
		for (int i = this.contEndIndex-1; i >= topIfIndex; i--) {
			Sentence s = this.senList.get(i);
			//碰到topIfIndex下方新的内容块时中止
			if (s.getType() == Sentence.TYPE_LINE) {
				break;
			}
			//查找内容块之上的if
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				ifs = (IfSentence)s;
				if (ifEnd == -1) {
					//设置ifEndLineNum
					ifEnd = this.findIfEndLineNum(this.contStartIndex+1);
				}
				//确定条件范围,内容块最上方一个if
				condStartIndex = this.senList.indexOf(s);
			}
		}
		//合并多条件
		if (condStartIndex < 0) {
			return false;
		}
		ifs = this.mergeConds(condStartIndex, this.contEndIndex);
		if (ifs == null) {
			return false;
		}
		TagSentence ifCond = (TagSentence)ifs.getCondTag();
		ifCond.setEndStruct();
		ifCond.over();
		ifs.setEndSenLineNum(ifEnd);
		
		//更新内容块范围
		this.contEndIndex = condStartIndex;
		this.contEnd = this.senList.get(condStartIndex).getLineNum();
		//this.contStartIndex = (this.contStartIndex+1==this.len) ? this.contStartIndex : this.contStartIndex + 1;
		//this.contStart = this.senList.get(this.contStartIndex).getLineNum();
		
		
		//处理else
		if (this.ifEndMap.containsKey(ifEnd)) {
			this.ifEndMap.get(ifEnd).setElse(true);
		}
		this.ifEndMap.put(ifEnd, ifs);
		return true;
	}


	/**
	 * 合并多条件,注意必须确定是多条件而非包含语句
	 * @param startIndex 条件正向开始语句(包含)
	 * @param endIndex 条件正向结束语句(不包含)
	 */
	private IfSentence mergeConds(int startIndex,int endIndex){
		
		int ifCount = 2;
		IfSentence ifs = null;
		TagSentence tag = null;
	
		while (ifCount >= 2) {
			ifCount = 0;
			for (int i = startIndex; i < endIndex; i++) {
				Sentence s = this.senList.get(i);
				boolean doMerge = false;
				//第一个if(doing状态)
				if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
					ifCount++;
					ifs = (IfSentence)s;
					tag = ifs.getCondTag();
					IfSentence ifs2 = null;
					TagSentence tag2 = null; 
					//在if和对应的tag之间查找第二个if
					//内部if数,仅有一个时才进行合并
					int innerIfCount = 0;
					for (int j = i+1; j < this.senList.indexOf(tag); j++) {
						
						Sentence sen = this.senList.get(j);
						//中间不能有未over的tag
						if (sen.getName().equals("tag") && sen.getState() == Sentence.STATE_DOING) {
							break;
						}
						//第二个if(doing状态)
						if (sen.getName().equals("if") && sen.getState() == Sentence.STATE_DOING) {
							ifCount++;
							innerIfCount++;
							ifs2 = (IfSentence)sen;
							tag2 = ifs2.getCondTag();
						}
						if (innerIfCount > 1) {
							ifCount = ifCount-2;
							doMerge = false;
							break;
						}else{
							doMerge = true;
						}
					}
					//仅有两个if包含时进行合并
					if (doMerge) {
						//仅有一个if，无需要合并的情况下
						if (ifs2 == null) {
							if (tag.getLineNum()>this.contStart) {
								ifs.reverseCompare();
							}
							continue;
						}
						//需要合并
						boolean isAnd = true;
						//如果外部if对应的tag指向内容块之前
						if(tag.getLineNum() < this.contEnd){
							isAnd = true;
							if (tag.getLineNum()<ifs2.getLineNum()) {
								isAnd = false;
							}else{
								if (tag2.getLineNum() > this.contStart) {
									ifs2.reverseCompare();
									isAnd = false;
								}else if(tag2.getLineNum() < this.contEnd){
									isAnd = true;
								}
							}
						}
						//外部if正指向内容块后
						else if(tag.getLineNum() >  this.contStart){
							if (tag.getLineNum()<ifs2.getLineNum()) {
								isAnd = false;
							}else{
								if (tag2.getLineNum() > this.contStart) {
									ifs2.reverseCompare();
									isAnd = true;
								}else if(tag2.getLineNum() < this.contEnd){
									isAnd = false;
								}
							}
						}
						// 合并|| 或 &&
						if (isAnd) {
							ifs.reverseCompare();
						}
						ifs.mergeIf(isAnd, ifs2);
						tag.over();
						//外部if新的tag,//修改合并后的指向
						ifs.setCondTag(tag2);
						ifs.setCond(tag2.getTag());
						tag2.over();
						//是否要加括号保护
						if (tag.getLineNum() != tag2.getLineNum()) {
							ifs.addCondProtect();
						}
						//合并后减少1
						ifCount--;
						
					}
				}
				
			}
		}
		//最后一个if设置over
		if (ifs != null) {
			ifs.over();
			ifs.getCondTag().over();
		}
		return ifs;
	}
	/**
	 * 处理do while结构
	 */
	private void scanDoWhile(IfSentence ifs,int i){
		TagSentence tag = ifs.getCondTag();
		if (tag.getLineNum()< ifs.getLineNum()) {
			//do while结构
			//确定最后一个指向此tag的if
			for (int j = i+1; j < this.len; j++) {
				Sentence s2 = this.senList.get(j);
				if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
					IfSentence ifs2 = (IfSentence)s2;
					if (ifs2.getCondTag().getLineNum() == tag.getLineNum()) {
						ifs = ifs2;
						i = j;
					}
				}
			}
			
			tag.appendOut(StaticUtil.NEWLINE+StaticUtil.TABS[tag.getLevel()]+"do {");
			//向前定位最上面的if
			IfSentence firstCond = ifs;
			for (int j = i-1; j >= 0; j--) {
				Sentence s1 = this.senList.get(j);
				if (s1.getName().equals("tag")) {
					continue;
				}else if(s1.getName().equals("if") && s1.getState() == Sentence.STATE_DOING){
					firstCond = (IfSentence)s1;
				}else {
					break;
				}
			}
			//多个条件
			if (firstCond.getLineNum() != ifs.getLineNum()) {
				int tagLineNum = tag.getLineNum();
				int tagIndex = this.senList.indexOf(tag);
				this.senList.remove(tagIndex);
				this.senList.add(i,tag);
				//倒置的tag插入最后一个if后面,lineNum+1
				int ifsLN = ifs.getLineNum();
				tag.setLineNum(ifsLN+1);
				//虚拟一个Sentence 加入其后
				TagSentence vv = new TagSentence(ifs.mgr, "");
				vv.setLineNum(ifsLN+2);
				vv.setType(Sentence.TYPE_LINE);
				this.senList.add(i+1,vv);
				//后面的所有lineNum+1
				for (int j = i+2; j < this.len+1; j++) {
					Sentence s3 = this.senList.get(j);
					s3.setLineNum(s3.getLineNum()+1);
				}
				maxLineNum++;
				
				//确定内容块位置,将最后一个if作为内容块行
				this.contEndIndex = this.senList.indexOf(vv);
				this.contStartIndex = contEndIndex;
				this.contStart = this.senList.get(contStartIndex).getLineNum();
				this.contEnd = this.senList.get(contEndIndex).getLineNum();
				int ifStart = this.senList.indexOf(firstCond);
				//合并条件
				this.mergeCondsForWhile(ifStart, i+1,ifs.getLineNum());
				//回复以前状态
				tag.setLineNum(tagLineNum);
				this.senList.remove(tag);
				this.senList.add(tagIndex,tag);
				this.senList.remove(vv);
			}
			
			tag.over();
			//设置处理完
			firstCond.setDoWhile(true);
			firstCond.appendOut(";");
			firstCond.over();
			firstCond.getCondTag().over();
		}
	}
	
	/**
	 * 扫描tag前置的while结构,有可能为do-while
	 */
	private void scanReversedIf(){
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence tag = ifs.getCondTag();
				if (tag.getLineNum()< ifs.getLineNum()) {
					//确定最后一个指向此tag的if
					for (int j = i+1; j < this.len; j++) {
						Sentence s2 = this.senList.get(j);
						if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
							IfSentence ifs2 = (IfSentence)s2;
							if (ifs2.getCondTag().getLineNum() == tag.getLineNum()) {
								ifs = ifs2;
								i = j;
							}
						}
					}
					//前置的tag
					int tagIndex = this.senList.indexOf(tag);
					int firstIfIndex = tagIndex+1;
					Sentence s1 = this.senList.get(firstIfIndex);
					if (s1.getName().equals("if") && s1.getState() == Sentence.STATE_DOING) {
						//如为while后面必定紧跟if,也有可能为do-while内部第一句为if
						ifs = (IfSentence)s1;
						IfSentence lastCond = ifs;
						TagSentence EndTag = lastCond.getCondTag();
						int lastCondIndex = tagIndex+1;
						int conStart = lastCondIndex+1;
						//定位多条件,先确定最下方的tag为EndTag
						ArrayList<IfSentence> conds =new ArrayList<IfSentence>();
						conds.add(ifs);
						for (int j = conStart; j < this.len; j++) {
							Sentence s2 = this.senList.get(j);
							if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
								IfSentence ifss = (IfSentence)s2;
								//确定最末的tag，//一定是在return之后
								TagSentence laTag = ifss.getCondTag();
//								int laTagIndex = this.senList.indexOf(laTag);
								//boolean isInnerIf = false;
//								Sentence beforeLa = this.senList.get(laTagIndex-1);
//								if (beforeLa.getName().equals("return") || beforeLa.getName().equals("goto")) {
//									
//								}
								/*
								//是否已经是包含的if了,判断tag往上是否有其他的内容块
								for (int k = laTagIndex-1; k > j; k--) {
									Sentence ss = this.senList.get(k);
									if ((ss.getName().equals("tag") || ss.getName().equals("if")) && ss.getState() == Sentence.STATE_DOING) {
										continue;
									}else if(ss.getName().equals("return") || ss.getName().equals("goto")){
										break;
									}else{
										isInnerIf = true;
										break;
									}
								}
								//内部包含的if，说明多条件已结束
								if (isInnerIf) {
									break;
								}
								*/
								//最下面的tag为endTag
								if (laTag.getLineNum() >= EndTag.getLineNum()) {
									EndTag = laTag;
								}
								lastCond = ifss;
								lastCondIndex = j;
								conStart = j+1;
								conds.add(lastCond);
							}else if(s2.getName().equals("tag")){
								conStart = j+1;
								continue;
							}else{
								break;
							}
						}
						//将while包含的内部if从多条件中去除
						for (int j = lastCondIndex; j > firstIfIndex; j--) {
							Sentence s2 = this.senList.get(j);
							if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
								IfSentence ifs2 = (IfSentence)s2;
								TagSentence ifs2Tag = ifs2.getCondTag();
								//最下面的if对应的tag不是EndTag时去除,否则表示已到达多条件的最下方if
								if (ifs2Tag.getLineNum() != EndTag.getLineNum()) {
									conds.remove(ifs2);
								}else{
									break;
								}
							}
						}
						lastCond = conds.get(conds.size()-1);
						lastCondIndex = this.senList.indexOf(lastCond);
						conds = null;
						
						//将EndTag移动到最下面一个if后面,lineNum+1
						int endTagLineNum = EndTag.getLineNum();
						int endTagIndex = this.senList.indexOf(EndTag);
					/*	this.senList.remove(EndTag);
						this.senList.add(lastCondIndex+1,EndTag);
						int endTagLN = lastCond.getLineNum()+1;
						EndTag.setLineNum(endTagLN);
						//虚拟一个Sentence 加入其后
						TagSentence vv = new TagSentence(this.mgr, "");
						vv.setLineNum(endTagLN+1);
						vv.setType(Sentence.TYPE_LINE);
						this.senList.add(lastCondIndex+2,vv);
						//后面的所有lineNum+1
						for (int j = lastCondIndex+2; j < this.len+1; j++) {
							Sentence s3 = this.senList.get(j);
							s3.setLineNum(s3.getLineNum()+1);
						}
						
						//确定内容块
						this.contStartIndex = this.senList.indexOf(vv);
						this.contEndIndex = contStartIndex;
						this.contEnd = this.senList.get(this.contEndIndex).getLineNum();
						this.contStart = this.senList.get(this.contStartIndex).getLineNum();
						*/
						this.contStartIndex = endTagIndex+1;
						this.contEndIndex = contStartIndex;
						this.contEnd = this.senList.get(this.contEndIndex).getLineNum();
						this.contStart = this.senList.get(this.contStartIndex).getLineNum();
						
						//合并条件
						ifs = this.mergeCondsForWhile(firstIfIndex, lastCondIndex+1,lastCond.getLineNum());
						if (ifs == null) {
							System.err.println("scanReversedWhile mergeCondsForWhile failed.");
							continue;
						}
/*						//回复以前状态
						EndTag.setLineNum(endTagLineNum);
						this.senList.remove(EndTag);
						this.senList.add(endTagIndex+1,EndTag);
						this.senList.remove(vv);
						*/
						
						//移动内容块,将最后一个tag后面的内容移到lastCond后
						ArrayList<Sentence> temp = new ArrayList<Sentence>();
						int moveStart = endTagIndex+1;
						for (int j = moveStart; j < this.len; j++) {
							Sentence s2 = this.senList.get(moveStart);
							if (s2.getName().equals("return")) {
								break;
							}
							//s2.setLevel(s2.getLevel()+1);
							temp.add(this.senList.remove(moveStart));
						}
						this.senList.remove(EndTag);
						temp.add(EndTag);
						this.senList.addAll(lastCondIndex, temp);
						ifs.setWhile();
						ifs.over();
						if (EndTag != null) {
							EndTag.setEndStruct();
							EndTag.over();
						}
						//FIXME 下一步要处理continue和break;
					}
					else{
						//初步判定为do-while结构,也有可能是break或continue语句
						
						//this.scanDoWhile(ifs,i);
					}
					
				}
			}
		}
	}

	/**
	 * 扫描并处理while结构(不包含do while)
	 */
	private void scanWhileStruct(){
		
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("gotoTag") && s.getState() == Sentence.STATE_DOING) {
				GotoTagSentence gtTagSen = (GotoTagSentence)s;
				if (gtTagSen.isReturn()) {
					continue;
				}
				if (i+1 == this.len) {
					return;
				}
				//goto tag后紧跟if,有可能为while
				Sentence sen = this.senList.get(i+1);
				if (sen.getName().equals("if") && sen.getState() == Sentence.STATE_DOING) {
				//if (sen.getName().equals("if")) {
					String gotoTag = gtTagSen.getTag();
					//第一个if
					IfSentence ifs = (IfSentence)sen;
					//-------------------------------------
					/*//处理do while包含
					if (ifs.isDoWhile() && ifs.getState() == Sentence.STATE_OVER) {
						TagSentence doTag = ifs.getCondTag();
						
						//将do 后面紧跟的if直到第一个非line的语句进行while合并处理
						
						//将do移动后ifs后面的goto语句后,成为外部while的结束tag 
						
						//继续处理ifs
						//continue;
					}*/
					//-------------------------------------
					//找到紧跟的if,碰到非tag和if时终止, lastCond为最下方一个if
					IfSentence lastCond = ifs;
					int lastCondIndex = i+1;
					for (int j = i+2; j < this.len; j++) {
						Sentence s1 = this.senList.get(j);
						if (s1.getName().equals("tag")) {
							continue;
						}else if(s1.getName().equals("if") && s1.getState() == Sentence.STATE_DOING){
							//FIXME 判断是否内部的if
							
							
							
							lastCond = (IfSentence)s1;
							lastCondIndex = j;
						}else {
							break;
						}
					}
					//确认if目标后有goto指向goto tag
					TagSentence lastIfTag = lastCond.getCondTag();
					int start = this.senList.indexOf(lastIfTag);
					//#####################################
					//注意lastCond可能为倒置状态
					if (lastIfTag.getLineNum()< lastCond.getLineNum()) {
						//倒置结构,start位置在lastCond，或lastCond后紧跟的最后一个tag(如果有的话)
						start = lastCondIndex;
						for (int j = lastCondIndex+1; j < this.len; j++) {
							Sentence s4 = this.senList.get(j);
							if (s4.getName().equals("tag")) {
								start = j;
							}else{
								break;
							}
						}
					}
					//####################################
					int gotoS = this.findSenIndex(start+1, "goto");
					boolean isWhile = false;
					GotoSentence gs = null;
					while (gotoS > start) {
						gs = (GotoSentence)this.senList.get(gotoS);
						if (gs.getTarget().equals(gotoTag)) {
							//确认为while结构
							ifs.setWhile();
							isWhile = true;
							break;
							
						}
					}
					if (isWhile) {
						//确定内容块位置
						this.contEndIndex = start+1;
						this.contStartIndex = gotoS-1;
						this.contStart = this.senList.get(contStartIndex).getLineNum();
						this.contEnd = this.senList.get(contEndIndex).getLineNum();
						                          
						//合并条件
						int lastIfIndex = this.senList.indexOf(lastCond);
						this.mergeCondsForWhile(i+1,lastIfIndex+1,lastCond.getLineNum());
						//将while的内容块移动
						ArrayList<Sentence> temp = new ArrayList<Sentence>();
						for (int j = this.contEndIndex; j <= this.contStartIndex && j< this.senList.size(); j++) {
							Sentence s1 = this.senList.remove(j);
							temp.add(s1);
						}
						ifs.over();
						gs.over();
						lastIfTag.setEndStruct();
						lastIfTag.over();
						this.senList.remove(lastIfTag);
						//最后将lastIfTag作为结束加入temp
						temp.add(lastIfTag);
						this.senList.addAll(lastIfIndex+1, temp);
					}
					
				}else{
					continue;
				}
			}
		}
	}

	/**
	 * 合并多条件,注意必须确定是多条件而非包含语句
	 * @param startIndex 条件正向开始语句(包含)
	 * @param endIndex 条件正向结束语句(不包含)
	 * @param lastIfLineNum 最后一个条件if的lineNum
	 */
	private IfSentence mergeCondsForWhile(int startIndex,int endIndex,int lastIfLineNum){
		//将if多条件后的最后一个tag后置
		Sentence afterLastIfTag = null;
		int afterLastIfTagSenIndex = -1;
		int afterLastIfTagSenLn = -1;
		for (int i = endIndex; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag")) {
				afterLastIfTag = s;
			}else{
				break;
			}
		}
		if (afterLastIfTag != null) {
			afterLastIfTagSenIndex = this.senList.indexOf(afterLastIfTag);
			afterLastIfTagSenLn = afterLastIfTag.getLineNum();
//			this.senList.remove(afterLastIfTagSenIndex);
//			this.senList.add(this.contStartIndex,afterLastIfTag);
//			afterLastIfTag.setLineNum(this.maxLineNum+1);
		}
		Sentence beforeContentTag = this.senList.get(this.contEndIndex-1);
		int beforeContentTagLn = beforeContentTag.getLineNum();
		//虚拟内容行
		float contentLineNum = Float.parseFloat(((afterLastIfTag == null)?this.senList.get(endIndex).getLineNum():afterLastIfTagSenLn)+".5");
		//----------
		int ifCount = 2;
		IfSentence ifs = null;
		TagSentence tag = null;
		boolean hasReverseTag = false;
		while (ifCount >= 2) {
			ifCount = 0;
			for (int i = startIndex; i < endIndex; i++) {
				Sentence s = this.senList.get(i);
				boolean doMerge = false;
				//第一个if(doing状态)
				if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
					ifCount++;
					ifs = (IfSentence)s;
					tag = ifs.getCondTag();
					int tagIndex = this.senList.indexOf(tag);
					float tag1LineNum = tag.getLineNum();
					//############ 处理倒置的情况,虚拟一个Tag
					if (tag1LineNum < ifs.getLineNum()) {
						//tag = new TagSentence(mgr, "");
						tag1LineNum = this.contStart+1;
						//tag.setLineNum(this.contStart+1);
						tagIndex = this.contStartIndex+1;
						hasReverseTag = true;
					}
					//###################
					//外部if的tag正是内容块前tag,置于内容块前
					else if (tag1LineNum == beforeContentTagLn) {
						
						tagIndex = endIndex;
						tag1LineNum = contentLineNum - 0.2F;
						
					}else if(afterLastIfTagSenIndex>-1 && tag1LineNum == afterLastIfTagSenLn){
						//外部if的tag正是最下方if后的tag,置于内容块后
						tag1LineNum = contentLineNum + 1F;
						
					}
					//####################
					IfSentence ifs2 = null;
					TagSentence tag2 = null; 
					float tag2LineNum = -1F;
					//在if和对应的tag之间查找第二个if
					//内部if数,仅有一个时才进行合并
					int innerIfCount = 0;
					for (int j = i+1; j < tagIndex; j++) {
						
						Sentence sen = this.senList.get(j);
						//中间不能有未over的tag
						if (sen.getName().equals("tag") && sen.getState() == Sentence.STATE_DOING) {
							break;
						}
						//第二个if(doing状态)
						if (sen.getName().equals("if") && sen.getState() == Sentence.STATE_DOING) {
							ifCount++;
							innerIfCount++;
							ifs2 = (IfSentence)sen;
							tag2 = ifs2.getCondTag();
							tag2LineNum = tag2.getLineNum();
							//############ 处理倒置的情况,虚拟一个Tag
							if (tag2.getLineNum() < ifs2.getLineNum()) {
								//tag2 = new TagSentence(mgr, "");
								//tag2.setLineNum(this.contStart+1);
								tag2LineNum = this.contStart+1;
								hasReverseTag = true;
							}
							//############
							//内部if的tag正是内容块前tag,置于内容块前
							else if (tag2LineNum == beforeContentTagLn) {
								
								tag2LineNum = contentLineNum - 0.2F;
								
							}else if(afterLastIfTagSenIndex>-1 && tag2LineNum == afterLastIfTagSenLn){
								//内部if的tag正是最下方if后的tag,置于内容块后
								tag2LineNum = contentLineNum + 1F;
								
							}
							//####################
						}
						if (innerIfCount > 1) {
							ifCount = ifCount-2;
							doMerge = false;
							break;
						}else{
							doMerge = true;
						}
					}
					//仅有两个if包含时进行合并
					if (doMerge) {
						//仅有一个if，无需要合并的情况下
						if (ifs2 == null) {
							if (tag.getLineNum()>contentLineNum) {
								ifs.reverseCompare();
							}
							continue;
						}
						//先默认为and关系
						boolean isAnd = true;
						//如果外部if对应的tag指向内容块之前
						if(tag1LineNum < contentLineNum){
							isAnd = true;
							if (tag1LineNum < ifs2.getLineNum()) {
								isAnd = false;
							}else{
//								if (ifs2.getLineNum() == lastIfLineNum && !hasReverseTag) {
//									isAnd = false;
//								}else 
									
									if (tag2LineNum > contentLineNum) {
									ifs2.reverseCompare();
									isAnd = false;
								}else if(tag2LineNum < contentLineNum){
									isAnd = true;
								}
							}
						}
						//外部if正指向内容块后
						else if(tag1LineNum >  contentLineNum){
							if (tag1LineNum < ifs2.getLineNum()) {
								isAnd = false;
							}else{
//								if (tag2LineNum == lastIfLineNum && !hasReverseTag) {
//									isAnd = true;
//								}else 
									
								if (tag2LineNum > contentLineNum) {
									ifs2.reverseCompare();
									isAnd = false;
								}else if(tag2LineNum < contentLineNum){
									isAnd = true;
								}
							}
						}
						// 合并|| 或 &&
						if (tag1LineNum > contentLineNum) {
							ifs.reverseCompare();
						}
						ifs.mergeIf(isAnd, ifs2);
						//是否要加括号保护
						if (tag1LineNum != tag2LineNum) {
							ifs.addCondProtect();
						}
						else {
							if(tag2LineNum == lastIfLineNum){
								ifs.addCondProtect();
							}
						}
						//合并之后调整lastIfLineNum
						if (ifs2.getLineNum() == lastIfLineNum) {
							lastIfLineNum = ifs.getLineNum();
						}
						tag.over();
						//外部if新的tag,//修改合并后的指向
						ifs.setCondTag(tag2);
						ifs.setCond(tag2.getTag());
						tag2.over();
						
						//合并后减少1
						ifCount--;
						
					}
				}
				
			}
		}
		//最后一个if设置over
		if (ifs != null) {
			ifs.over();
			ifs.getCondTag().over();
		}
		//-----------
		//回复位置
//		beforeContentTag.setLineNum(beforeContentTagOldLineNum);
//		if (afterLastIfTag != null) {
//			afterLastIfTag.setLineNum(afterLastIfTagSenLn);
//		}
		return ifs;
	}

	
	private int findSenIndex(int start,String typeName){
		for (int i = start; i < this.len; i++) {
			Sentence sen = this.senList.get(i);
			if (sen.getName().equals(typeName)) {
				return i;
			}
		}
		return -1;
	}
	
//	private int findSenIndex(int start,String typeName,String[] exceptNames){
//		for (int i = start; i < this.len; i++) {
//			Sentence sen = this.senList.get(i);
//			if (exceptNames != null) {
//				for (int j = 0; j < exceptNames.length; j++) {
//					if (sen.getName().equals(exceptNames[j])) {
//						return -1;
//					}
//				}
//			}
//			if (sen.getName().equals(typeName)) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
	/**
	 * 向下查找if内容块中最后一条语句的lineNum
	 * @param start senList-index 内容块下方第一条语句的index
	 * @return
	 */
	private int findIfEndLineNum(int start){
		for (int i = start; i < this.len; i++) {
			if (i == this.returnLine) {
				//直接返回最后一行，即return行
				return this.senList.get(this.len-1).getLineNum();
			}
			Sentence s = this.senList.get(i);
			if (s.getType() == Sentence.TYPE_LINE || s.getName().equals("if")|| s.getName().equals("return")) {
				return s.getLineNum();
			}else if(s.getName().equals("goto")){
				i = this.senList.indexOf(((GotoSentence)s).getTargetSen());
			}
		}
		return -1;
	}

	/**
	 * 初始化cond和ifsen以及goto的对应关系，return语句后置
	 */
	private boolean init(){
		//是否需要将return后置,0表示未处理或不处理,1:不需要,2:需要,3处理中
		int returnToEnd = 0;
		this.maxLineNum = this.senList.get(len-1).getLineNum();
		//先扫描两次将cond和if对应上
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
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
			return false;
		}
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence ts = condMap.get(ifs.getCond());
				if (ts != null) {
					//对于1个tag对应多个if的情况,最终为最上方的if与tag相互对应,其他为if单向对应tag
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
			}else if(s.getName().equals("return")){
				this.returnIndex = i;
			}
			if(returnToEnd == 2){
				if (returnLine == -1 && s.getName().equals("return")) {
					returnLine = this.senList.indexOf(s);
					returnToEnd = 3;
				}
//				else if(returnToEnd == 3){
//					if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
//						returnLine = this.senList.indexOf(s);
//					}else{
//						returnToEnd = 0;//不再处理
//					}
//				}
			}else if(returnToEnd == 3){
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					returnLine = this.senList.indexOf(s);
				}else{
					returnToEnd = 0;//不再处理
				}
			}
		}
		lastSenLineNum = this.senList.get(this.len-1).getLineNum();
		return true;
	}
	private int lastSenLineNum = -1;
	private void backReturnSens(){
		//return 后置
		if (returnLine > -1) {
			ArrayList<Sentence> temp = new ArrayList<Sentence>();
			while (true) {
				Sentence s1 = this.senList.get(returnLine);
				if (s1.getName().equals("return")) {
					//return 语句本身不后置
//					s1.setLineNum(last+1);
//					temp.add(s1);
//					this.senList.remove(s1);
					break;
				}else if (s1.getName().equals("if") || s1.getName().equals("goto")) {
					break;
				}else{
					if (s1.getName().equals("tag")) {
						((TagSentence)s1).setReturn(true);
					}else if(s1.getName().equals("gotoTag")){
						((GotoSentence)((GotoTagSentence)s1).getIfSen()).setReturn(true);
					}
					s1.setLineNum(lastSenLineNum+1);
					temp.add(s1);
					this.senList.remove(s1);
					lastSenLineNum++;
					continue;
				}
			}
			if (!temp.isEmpty()) {
				this.senList.addAll(this.senList.size(), temp);
			}
		}
	}
	
}
