/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

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
	static final Logger log = Logger.getLogger(IFStructScan.class);
	
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
	private int returnLineNum = -1;
	
	/**
	 * return语句
	 */
	private ReturnSentence returnSentence;
	
//	/**
//	 * return 所在的index
//	 */
//	private int returnIndex = -1;
	
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
//	/**
//	 * 每个if内容块内结束标记map,用于判断是否为else
//	 */
//	private HashMap<Integer,IfSentence> ifEndMap = new HashMap<Integer, IfSentence>();
	
	/**
	 * 判断是否是结构语句,返回false为内容,true为未处理的结构
	 * @param sen Sentence
	 * @return 返回false为内容,true为未处理的结构
	 */
	public static final boolean isStructSen(Sentence sen){
		if (sen.getType() != Sentence.TYPE_STRUCT) {
			return false;
		}else if(sen.getState() == Sentence.STATE_OVER){
			return false;
		}
//		else if(sen.getOut().trim().startsWith("//")){
//			return false;
//		}
		return true;
	}
	
	/**
	 * 扫描并处理if结构
	 * @param senList
	 */
	public void scan(){
		if(!this.init()){
			return;
		}
		
		this.scanReversedIf();
		
		this.scanWhileStruct();
		//处理while结构
		
		//return 语句后置
//		this.backReturnSens();
		
		
		this.scanIf();
		
		/*
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
					if (!isStructSen(sc)) {
						if (firstContent) {
							this.contEndIndex = j;
							this.contEnd = sc.getLineNum();
							firstContent = false;
						}
						this.contStartIndex = j;
						this.contStart = sc.getLineNum();
					}
					//注意内容块中的gotoTag，有可能是else入口
					else if(sc.getName().equals("gotoTag") && sc.getState() == Sentence.STATE_DOING){
						this.elseEntrys.add((GotoTagSentence)sc);
					}
				}
				if(this.doIfStruct()){
					if (!this.elseEntrys.isEmpty()) {
						this.doElses();
					}
					i = this.contEndIndex;
				}
			}
			
		}*/
		log.debug("IfScan end");
	}
	


	private void scanIf(){
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				//定位条件块
				int[] re = this.defineIfBlock(i);
				IfSentence ifs  = (IfSentence) this.senList.get(re[1]);
				TagSentence tag = ifs.getCondTag();
				this.contStart = this.senList.indexOf(tag)-1;
				this.contEnd = re[0]+1;
				ifs = this.mergeConds(i, this.contEnd);
				//判断是否是到return的if
				this.setIfToRetrun(contStart, ifs);
				//tag是否在return后
				if (tag.getLineNum()>this.returnLineNum && (!tag.isShift())) {
					//可能有else块,定位ifs后的gotoTag
					GotoTagSentence gts = null;
					int po = 0;
					String elseGtTag = null;
					for (int j = i+1; j < this.senList.size(); j++) {
						Sentence s1 = this.senList.get(j);
						if (s1.getName().equals("gotoTag") && s1.getState() == Sentence.STATE_DOING) {
							gts = (GotoTagSentence)s1;
							po = j;
//							if (gts.getInsertPosition()<0) {
//								gts.setInsertPosition(po);
//								//向上跳过catch块部分
//								while (po >= 0) {
//									Sentence s2 = this.senList.get(po-1);
//									if (s2.getName().equals("try")) {
//										po--;
//										gts.setInsertPosition(po);
//									}else{
//										break;
//									}
//								}
//							}else{
//								po = gts.getInsertPosition();
//							}
							elseGtTag = gts.getTag();
							break;
						}
					}
					
					if (gts == null) {
						//gts没找到gotoTag时,用if下面的goto定位po
						boolean findGT = false;
						for (int j = i+1; j < this.senList.size(); j++) {
							Sentence s1 = this.senList.get(j);
							if (s1.getName().equals("goto")) {
								po = j+1;
								findGT = true;
								break;
							}
						}
						if (!findGT) {
							log.error(this.mgr.getMeth().getName()+" - scanIf error. gotoTag not found.");
							return;
						}
					}
					//向上跳过catch块部分
					while (po >= 0) {
						Sentence s2 = this.senList.get(po-1);
						if (s2.getName().equals("try")) {
							po--;
//							gts.setInsertPosition(po);
						}else{
							break;
						}
					}
					//移动else块
					this.shiftElse(po, this.contStart+1, tag, elseGtTag);
					
				}else if(tag.isShift()){
					ifs.setElse(true);
//					ifs.reverseCompare();
//					tag.setEndStruct();
					tag.over();
				}else{
					tag.setEndStruct();
					tag.over();
				}
				//处理if语句结束
				if (ifs.isToReturn()) {
					TagSentence t = ifs.getCondTag();
					String tab = StaticUtil.TABS[t.level];
					t.setOut(tab+"return; //ifToReturn add return."+StaticUtil.NEWLINE+tab+t.getOut());
				}
				ifs.over();
				
			}
		}
	}

//	/**
//	 * else 入口,初始值为-1
//	 */
//	private int elseEntry = -1;
	
	private ArrayList<GotoTagSentence> elseEntrys = new ArrayList<GotoTagSentence>();
	
	/**
	 * 处理多个if的else
	 */
	private void doElses(){
		int len = this.elseEntrys.size();
		for (int i = 0; i < len; i++) {
			GotoTagSentence po = this.elseEntrys.get(i);
			this.doElse(po);
		}
	}
	
	/**
	 * 处理if的else语句(非"}if else{",仅针对最后的else)
	 */
	private void doElse(GotoTagSentence elseGtTag){
		int po = this.senList.indexOf(elseGtTag);
		//Sentence elseGtTag = this.senList.get(po);
//		if (!elseGtTag.getName().equals("gotoTag")) {
//			log.debug(this.mgr.getMeth().getName()+" - elseGtTag is not gotoTag");
//			return;
//		}
		String elseGtTagName = elseGtTag.getTag();
		//向上跳过catch块部分
		while (po >= 0) {
			Sentence s = this.senList.get(po-1);
			if (s.getName().equals("try")) {
				po--;
			}else{
				break;
			}
		}
		//找到上一个tag在return之后的if
		for (int i = po-1; i >= 0; i--) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				if (ifs.getCondTag().getLineNum() > this.returnLineNum) {
					//定位到elseTag
					String elseTagName = ifs.getCondTag().getTag();
					//从return后查找所有的elseTag
					for (int j = this.senList.indexOf(this.returnSentence); j < this.senList.size(); j++) {
						Sentence s2 = this.senList.get(j);
						if (s2.getName().equals("tag")) {
							TagSentence ts = (TagSentence)s2;
							if (ts.getTag().equals(elseTagName)) {
								//处理else块
								j = this.shiftElse(po,j, ts, elseGtTagName)-1;
							}
						}
					}
					
					
				}
			}
			
		}
		/*//
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		//定位到goto语句，并移动语句块
		boolean elseStart = false;
		for (int i = this.contStartIndex+1; i < this.senList.size();) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("gotoTag")) {
				s.appendOut(" else {");
				elseStart = true;
			}else if(!elseStart){
				i++;
				continue;
			}
			ls.add(this.senList.remove(i));
			if (s.getName().equals("goto")) {
				GotoSentence gt = (GotoSentence)s;
				if (gt.getTarget().equals(elseTag)) {
					gt.setOut("} //end of ifElse: "+gt.getLine());
					gt.over();
					elseGtTag.setOut("//"+elseGtTag.getLine());
					elseGtTag.over();
					this.senList.addAll(po, ls);
					this.contStartIndex = this.contStartIndex + ls.size();
					return;
				}
			}
		}
		log.error(this.mgr.getMeth().getName()+" - elseGoto is not found.");
		*/
	}
	
	/**
	 * 移动else块，返回移动后的处理位置
	 * @param po 移动块插入位置
	 * @param startTagIndex else块开始的tag的index
	 * @param ts else块开始的tag
	 * @param elseGtTag gotoTag的tagName
	 * @return 返回移动的语句数
	 */
	private int shiftElse(int po,int startTagIndex,TagSentence ts,String elseGtTagName){
//		Sentence nextSentence = this.senList.get(startTagIndex+1);
		//定位到goto语句，并移动语句块
		int elseEnd = startTagIndex;
		for (int i = startTagIndex; i < this.senList.size();i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("goto")) {
				GotoSentence gt = (GotoSentence)s;
				//如果else块的结束goto句的target不是if的 gotoTag,则判断是否处理为return
				GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
				if (gtTag.isReturn()) {
					if (this.mgr.getMeth().getReturnStr().equals("void")) {
						gt.setOut("return; //else end. : "+gt.getLine());
					}else{
						//向上一句获取返回值
						Sentence pre = gt.getPreSen();
						gt.setOut("return "+pre.getOut()+";  //else end. : "+gt.getLine());
					}
				}else if (gt.getTarget().equals(elseGtTagName)) {
					gt.setOut("//end of ifElse: "+gt.getLine());
//					if (i+1<this.senList.size()) {
//						nextSentence = this.senList.get(i+1);
//					}
					((GotoTagSentence)gt.getTargetSen()).lessGotoTimes();
				}else{
					//可能为break或continue;
					gt.setOut("//maybe break or continue: "+gt.getLine());
				}
				elseEnd = i;
				gt.over();
				gtTag.lessGotoTimes();
				break;
			}
		}
		//找不到goto语句时,说明已被其他else语句移动，将最末句作为elseEnd，同时需要补一个if的结束句
		boolean fixEnd = false;
		if (elseEnd == startTagIndex) {
			elseEnd = this.senList.size()-1;
//			log.error(this.mgr.getMeth().getName()+" - elseGoto is not found.");
//			return 0;
			fixEnd = true;
		}
		Sentence tss = this.senList.get(startTagIndex+1);
		if (tss.getName().equals("if")) {
			//处理else if
			IfSentence ifs = (IfSentence)tss;
			ifs.setElse(true);
//			if (ifs.getState() == Sentence.STATE_DOING) {
//				//处理此if块
//				int[] re = this.defineIfBlock(startTagIndex+1);
//				ifs = this.mergeConds(startTagIndex+1, re[0]);
//				if (ifs == null) {
//					log.error(this.mgr.getMeth().getName()+" - elseif mergeConds error.");
//					return startTagIndex+1;
//				}
//			}
			//ifs.over(); //ifs实际上可能还未over
			ts.appendOut("//if's else will start");
			ts.setEndStruct();
			//po最后位置
			for (int j = po-1; j >= 0; j--) {
				String sn = this.senList.get(j).getName();
				if (sn.equals("tag") || sn.equals("gotoTag")) {
					po--;
				}else{
					break;
				}
			}
		}else{
			//最后的else块
			ts.setOut("} else { //"+ts.getLine());
			fixEnd = true;
			//else 块的位置在po之后
			po = po+1;
			//po最后位置
			for (int j = po; j <this.senList.size(); j++) {
				String sn = this.senList.get(j).getName();
				if (sn.equals("tag") || sn.equals("gotoTag")) {
					po++;
				}else{
					break;
				}
			}
		}
		ts.over();
		//移动
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		
		int moveCount = elseEnd-startTagIndex+1;
		for (int i = 0; i < moveCount;i++) {
			Sentence s2 = this.senList.remove(startTagIndex);
			if (s2.getName().equals("tag")) {
				((TagSentence)s2).setShift(true);
			}
			ls.add(s2);
		}
		if (fixEnd) {
			//给最后一句补一个if的结束块 
			Sentence lastMv = ls.get(ls.size()-1);
			lastMv.appendOut(";");
			lastMv.appendOut(StaticUtil.NEWLINE);
			lastMv.appendOut(StaticUtil.TABS[lastMv.level]);
			lastMv.appendOut("} //fixEnd");
		}
		int cc = ls.size();
		this.senList.addAll(po, ls);
		return cc;
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
		if (contStartIndex < 0 || contEndIndex < 0) {
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
		
		
//		//处理else
//		if (this.ifEndMap.containsKey(ifEnd)) {
//			this.ifEndMap.get(ifEnd).setElse(true);
//		}
//		this.ifEndMap.put(ifEnd, ifs);
		return true;
	}


	/**
	 * 合并多条件,注意必须确定是多条件而非包含语句
	 * @param startIndex 条件正向开始语句(包含)
	 * @param endIndex 条件正向结束语句(不包含)
	 * @return 合并后的IfSentence
	 */
	private IfSentence mergeConds(int startIndex,int endIndex){
		IfSentence ifs = null;
		if (startIndex == endIndex) {
			ifs = (IfSentence) this.senList.get(startIndex);
			ifs.reverseCompare();
			return ifs;
		}
		int ifCount = 2;
		TagSentence tag = null;
		int maxTimes = 1000;
		while (ifCount >= 2) {
			maxTimes--;
			if (maxTimes <= 0) {
				log.error(this.mgr.getMeth().getName()+" - mergeConds out of maxTimes.");
				return null;
			}
			ifCount = 0;
			for (int i = startIndex; i < endIndex; i++) {
				Sentence s = this.senList.get(i);
				boolean doMerge = true;
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
					for (int j = i+1; j < endIndex; j++) {
						
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
						//是否要加括号保护
						if (tag.getLineNum() != tag2.getLineNum()) {
							ifs.addCondProtect();
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
		return ifs;
	}
	
	private void scanBreakGoto(){
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("gotoTag")) {
				s = this.senList.get(i+1);
				if (s.getName().equals("return")) {
					//处理breakGoto -----------------------
					//向上查找if块
					
					//交换
					
				}
			}
		}
	}
	
	/**
	 * 扫描tag倒置的while结构,有可能为do-while等
	 */
	private void scanReversedIf(){
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence tag = ifs.getCondTag();
				//原来的i位置
				int ori = i;
				//倒置if
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
					Sentence s1 = null;
					//定位前置tag下方的非tag语句
					while (firstIfIndex < this.senList.size()) {
						s1 = this.senList.get(firstIfIndex);
						if (s1.getState() == Sentence.STATE_DOING && (s1.getName().equals("tag") || s1.getName().equals("gotoTag"))) {
							firstIfIndex++;
						}else{
							break;
						}
					}
					boolean isReverseWhile = false;
					if (s1.getName().equals("if") && s1.getState() == Sentence.STATE_DOING) {
						//如为while后面必定紧跟if
						ifs = (IfSentence)s1;
						int[] ifR = this.defineIfBlock(firstIfIndex);
						
						int lastIfTagLineNum= ((IfSentence)this.senList.get(ifR[1])).getCondTag().getLineNum();
						//其if块最终tag在return之后则为reverseWhile
						if (lastIfTagLineNum > this.returnLineNum) {
							isReverseWhile = true;
						}
					}
					
					if(isReverseWhile){
						this.reverseWhile(ifs, firstIfIndex);
					}else{
						//判断是否在某一正常while的内部,即if块的上一语句为while的最后if的tag
						Sentence s2 = this.senList.get(ori-1);
						if (s2.getName().equals("tag")) {
							//向上定位外部while的gotoTag
							//String outWhileLastTag = ((TagSentence)s2).getTag();
							GotoTagSentence outWhileGtag = null;
							int outWhileGtagIndex = -1;
							String outWhileGTTag = null;
							int outWhileGTIndex = -1;
							for (int j = i+1; j < this.len; j++) {
								Sentence s3 = this.senList.get(j);
								if (s3.getName().equals("goto") && s3.getState() == Sentence.STATE_DOING) {
									GotoSentence gt = (GotoSentence)s3;
									outWhileGTTag = gt.getTarget();
									outWhileGTIndex = j;
									break;
								}
							}
							for (int j = ori-2; j >= 0; j--) {
								Sentence sj = this.senList.get(j);
								if (sj.getName().equals("gotoTag") && sj.getState() == Sentence.STATE_DOING) {
									GotoTagSentence gtt = (GotoTagSentence)sj;
									if (gtt.getTag().equals(outWhileGTTag)) {
										outWhileGtagIndex = j;
										outWhileGtag = gtt;
										break;
									}
								}
							}
							if (outWhileGtagIndex == -1) {
								System.out.println("scanReversedIf error! outWhileGtagIndex == -1, fn:"+this.mgr.getMeth().getName());
								return;
							}
							
							int[] ifRE = this.defineIfBlock(ori);
							IfSentence lastIf = (IfSentence) this.senList.get(ifRE[1]);
							TagSentence lastIfTag = lastIf.getCondTag();
							int lastIfTagIndex = this.senList.indexOf(lastIfTag);
							//如果if块最终tag在外部while的gotoTag之上,即为倒置的while或continue
							if (lastIfTag.getLineNum() < outWhileGtag.getLineNum()) {
								//当if块后方无内容直接是goto语句时,为倒置while,否则为continue 
								boolean isContinue = true;
								//FIXME 需要重新找条件判断!!!!!!!!!!!!!!!!! 此处暂不处理非continue的情况
//								if (outWhileGTIndex == ifRE[0]+1) {
//									//倒置while
//									isContinue = false;
//								}
								//continue
								if (isContinue) {
									//FIXME 可能需要特殊的merge
									IfSentence ifss = this.mergeCondsForWhile(ori, ifRE[1]+1,lastIf,true);
									ifss.appendOut(StaticUtil.NEWLINE).appendOut(StaticUtil.TABS[ifss.getLevel()]).appendOut("continue;");
									//调整最终tag位置
									this.senList.remove(lastIfTagIndex);
									this.senList.add(outWhileGTIndex-1,lastIfTag);
									ifss.over();
									ifss.getCondTag().setEndStruct();
									ifss.getCondTag().over();
									
								}
								//倒置while //TODO 如在while内部，应该不可能出现??
								else{
									IfSentence ifss = this.mergeCondsForWhile(ori, ifRE[1]+1,lastIf,true);
									ifss.setWhile();
									ifss.over();
									ifss.getCondTag().over();
									//移动位置
									int contStart = this.senList.indexOf(lastIfTag)+1;
									ArrayList<Sentence> conts = new ArrayList<Sentence>();
									for (int j = contStart; j < ori-2; j++) {
										conts.add(this.senList.remove(contStart));
									}
									this.senList.remove(lastIfTag);
									conts.add(lastIfTag);
									this.senList.addAll(ifRE[0]+1,conts);
								}
								
							}else{
								//如果最终tag在return之后则为倒置while，否则为break
								if (lastIfTag.getLineNum() > this.returnLineNum) {
									//倒置while
									IfSentence ifss = this.mergeCondsForWhile(ori, ifRE[1]+1,lastIf,true);
									ifss.setWhile();
									ifss.over();
									ifss.getCondTag().over();
									//移动位置
									int contStart = this.senList.indexOf(lastIfTag)+1;
									ArrayList<Sentence> conts = new ArrayList<Sentence>();
									for (int j = contStart; j < ori-2; j++) {
										conts.add(this.senList.remove(contStart));
									}
									this.senList.remove(lastIfTag);
									conts.add(lastIfTag);
									this.senList.addAll(ifRE[0]+1,conts);
								}else{
									//break;
									IfSentence ifss = this.mergeConds(ori, ifRE[1]+1);
									ifss.appendOut(StaticUtil.NEWLINE).appendOut(StaticUtil.TABS[ifss.getLevel()]).appendOut("break;");
									//调整最终tag位置
									this.senList.remove(lastIfTagIndex);
									this.senList.add(outWhileGTIndex-1,lastIfTag);
									ifss.getCondTag().setEndStruct();
									ifss.getCondTag().over();
								}
								
							}
						}else{
							//FIXME if块的上一语句为内容,则一定为do while,为else if的倒置条件句
							
						}
						
					}
					
				}
			}
		}
	}
	
	/**
	 * 处理倒置if,其后置的if为倒置的while的情况
	 * @param ifs
	 */
	private void reverseWhile2(IfSentence ifs,int ifsIndex){
		IfSentence lastCond = ifs;
		TagSentence EndTag = lastCond.getCondTag();
		int lastCondIndex = ifsIndex;
		int conStart = lastCondIndex+1;
		
		
		
	}
	
	/**
	 * 定位if条件块,去除if条件之外的if
	 * @param beginIndex 开始if的index,包含
	 * @return 数组:[条件块真正结束的index,最后一个if的index]
	 */
	private int[] defineIfBlock(int beginIndex){
		int endIndex = beginIndex;
		int lastCondIndex = beginIndex;
		int realEndIndex = endIndex;
//		if (!isReverse) {
			//正向查询,先将连续的if和tag扫描完
			for (int i = beginIndex; i < this.len; i++) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
					endIndex = i;
					continue;
				}else if(s.getName().equals("tag") && s.getState() == Sentence.STATE_DOING){
					endIndex = i;
					continue;
				}else{
					break;
				}
			}
			//beginIndex到endIndex为index区域
			boolean hasAReverseTag = false;
			int lastIfTagIndex = -1;
			for (int i = beginIndex; i <= endIndex ; i++) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("if")) {
					IfSentence ifs = (IfSentence)s;
					int condIndex = this.senList.indexOf(ifs.getCondTag());
					if (condIndex>=beginIndex && condIndex <= endIndex) {
						//cond在index区块内
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else if(condIndex < i){
						//倒置tag
						if (!hasAReverseTag) {
							//有一个if对应的倒置tag可以不在这个index区域内
							hasAReverseTag = true;
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else if(lastIfTagIndex == -1){
							//最末一个if对应的tag以及以tag对应的其他if可以不在这个index区域内
							lastIfTagIndex = condIndex;
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else if(lastIfTagIndex == condIndex){
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else{
							break;
						}
					}else if(lastIfTagIndex == -1){
						//最末一个if对应的tag以及以tag对应的其他if可以不在这个index区域内
						lastIfTagIndex = condIndex;
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else if(lastIfTagIndex == condIndex){
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else{
						break;
					}
				}else if(s.getName().equals("tag")){
					TagSentence tag = (TagSentence)s;
					//注意只对应最上方的if的index
					int ifIndex = this.senList.indexOf(tag.getIfSen());
					if (ifIndex >= beginIndex && ifIndex <= endIndex) {
						realEndIndex = i;
						continue;
					}else{
						break;
					}
				}else{
					break;
				}
			}
			if (realEndIndex < beginIndex) {
				realEndIndex = beginIndex;
			}
		/*}else{
			//反向
			for (int i = beginIndex; i >= 0; i--) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
					endIndex = i;
					continue;
				}else if(s.getName().equals("tag")){
					endIndex = i;
					continue;
				}else{
					break;
				}
			}
			//beginIndex到endIndex为index区域
			boolean hasAReverseTag = false;
			int lastIfTagIndex = -1;
			for (int i = beginIndex; i >= endIndex ; i--) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("if")) {
					IfSentence ifs = (IfSentence)s;
					int condIndex = this.senList.indexOf(ifs.getCondTag());
					if (condIndex>=beginIndex && condIndex <= endIndex) {
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else if(condIndex < i){
						//倒置tag
						if (!hasAReverseTag) {
							hasAReverseTag = true;
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else if(lastIfTagIndex == -1){
							lastIfTagIndex = condIndex;
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else if(lastIfTagIndex == condIndex){
							lastCondIndex = i;
							realEndIndex = i;
							continue;
						}else{
							break;
						}
					}else if(lastIfTagIndex == -1){
						lastIfTagIndex = condIndex;
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else if(lastIfTagIndex == condIndex){
						lastCondIndex = i;
						realEndIndex = i;
						continue;
					}else{
						break;
					}
				}else if(s.getName().equals("tag")){
					TagSentence tag = (TagSentence)s;
					//注意只对应最上方的if的index
					int ifIndex = this.senList.indexOf(tag.getIfSen());
					if (ifIndex >= beginIndex && ifIndex <= endIndex) {
						realEndIndex = i;
						continue;
					}else{
						break;
					}
				}else{
					break;
				}
			}
			if (realEndIndex > beginIndex) {
				realEndIndex = beginIndex;
			}
		}*/
		return new int[]{realEndIndex,lastCondIndex};
	}
	
	/**
	 * 处理倒置if,其前置的tag后为while的情况
	 * @param ifs
	 * @param firstIfIndex
	 */
	private void reverseWhile(IfSentence ifs,int firstIfIndex){
		IfSentence lastCond = ifs;
//		TagSentence EndTag = lastCond.getCondTag();
		int lastCondIndex = firstIfIndex;
		//内容开始位置
		int conStart = lastCondIndex+1;
		//定位多条件,先确定最下方的tag为EndTag
//		ArrayList<IfSentence> conds =new ArrayList<IfSentence>();
//		conds.add(ifs);
//		for (int j = conStart; j < this.len; j++) {
//			Sentence s2 = this.senList.get(j);
//			if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
//				IfSentence ifss = (IfSentence)s2;
//				//确定最末的tag，
//				TagSentence laTag = ifss.getCondTag();
//				//最下面的tag为endTag
//				if (laTag.getLineNum() >= EndTag.getLineNum()) {
//					EndTag = laTag;
//				}
//				lastCond = ifss;
//				lastCondIndex = j;
//				conStart = j+1;
//				conds.add(lastCond);
//			}else if(s2.getName().equals("tag")){
//				conStart = j+1;
//				continue;
//			}else{
//				break;
//			}
//		}
//		//将while包含的内部if从多条件中去除
//		for (int j = lastCondIndex; j > firstIfIndex; j--) {
//			Sentence s2 = this.senList.get(j);
//			if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
//				IfSentence ifs2 = (IfSentence)s2;
//				TagSentence ifs2Tag = ifs2.getCondTag();
//				//最下面的if对应的tag不是EndTag时去除,否则表示已到达多条件的最下方if
//				if (ifs2Tag.getLineNum() != EndTag.getLineNum()) {
//					conds.remove(ifs2);
//				}else{
//					break;
//				}
//			}
//		}
		int[] ifRE = this.defineIfBlock(firstIfIndex);
//		lastCond = conds.get(conds.size()-1);
//		lastCondIndex = this.senList.indexOf(lastCond);
//		conds = null;
		lastCondIndex = ifRE[1];
		lastCond = (IfSentence) this.senList.get(lastCondIndex);
		conStart = ifRE[0] + 1;
		TagSentence EndTag = lastCond.getCondTag();
		
		//将EndTag移动到最下面一个if后面,lineNum+1
		//int endTagLineNum = EndTag.getLineNum();
		int endTagIndex = this.senList.indexOf(EndTag);
		this.contStartIndex = endTagIndex+1;
		this.contEndIndex = contStartIndex;
		this.contEnd = this.senList.get(this.contEndIndex).getLineNum();
		this.contStart = this.senList.get(this.contStartIndex).getLineNum();
		
		//合并条件
		ifs = this.mergeCondsForWhile(firstIfIndex, lastCondIndex+1,lastCond,true);
		if (ifs == null) {
			log.error(this.mgr.getMeth().getName()+" - scanReversedWhile mergeCondsForWhile failed.");
			return;
		}
		//移动内容块,将最后一个tag后面的内容移到lastCond后
		ArrayList<Sentence> temp = new ArrayList<Sentence>();
		int moveStart = endTagIndex+1;
		for (int j = moveStart; j < this.len; j++) {
			Sentence s2 = this.senList.get(moveStart);
			if (s2.getName().equals("return")) {
				break;
			}else if(s2.getLineNum() >= this.maxLineNum){
				//到初始化前的最末一个语句时,下一步即跳出
				j = this.len;
			}
			//s2.setLevel(s2.getLevel()+1);
			temp.add(this.senList.remove(moveStart));
		}
		this.senList.remove(EndTag);
		temp.add(EndTag);
		this.senList.addAll(lastCondIndex+1, temp);
		ifs.setWhile();
		ifs.over();
		if (EndTag != null) {
			EndTag.setEndStruct();
			EndTag.over();
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
//					TagSentence EndTag = ifs.getCondTag();
					int lastCondIndex = i+1;
//					ArrayList<IfSentence> conds = new ArrayList<IfSentence>();
//					conds.add(ifs);
//					boolean hasReverse = false;
//					for (int j = i+2; j < this.len; j++) {
//						Sentence s1 = this.senList.get(j);
//						if (s1.getName().equals("tag")) {
//							continue;
//						}else if(s1.getName().equals("if") && s1.getState() == Sentence.STATE_DOING){
//							IfSentence iff = (IfSentence)s1;
//							TagSentence tiff = iff.getCondTag();
//							if (tiff.getLineNum() < iff.getLineNum()) {
//								//倒置的一定是EndTag
//								EndTag = tiff;
//								hasReverse = true;
//							}else if (!hasReverse && tiff.getLineNum()> EndTag.getLineNum()) {
//								EndTag = tiff;
//							}
//							lastCondIndex = j;
//							conds.add(iff);
//						}else {
//							break;
//						}
//					}
//					
//					//将while包含的内部if从多条件中去除
//					for (int j = lastCondIndex; j > i+1; j--) {
//						Sentence s2 = this.senList.get(j);
//						if (s2.getName().equals("if") && s2.getState() == Sentence.STATE_DOING) {
//							IfSentence ifs2 = (IfSentence)s2;
//							TagSentence ifs2Tag = ifs2.getCondTag();
//							//最下面的if对应的tag不是EndTag时去除,否则表示已到达多条件的最下方if
//							if (ifs2Tag.getLineNum() != EndTag.getLineNum()) {
//								conds.remove(ifs2);
//							}else{
//								break;
//							}
//						}
//					}
//					lastCond = conds.get(conds.size()-1);
//					lastCondIndex = this.senList.indexOf(lastCond);
//					conds = null;
					int[] ifRE = this.defineIfBlock(lastCondIndex);
					lastCondIndex = ifRE[1];
					lastCond = (IfSentence) this.senList.get(lastCondIndex);
					
					//确认if目标后有goto指向goto tag
					TagSentence lastIfTag = lastCond.getCondTag();
					int start = this.senList.indexOf(lastIfTag);
					//#####################################
					//注意lastCond可能为倒置状态
//					boolean isReverse = false;
					if (lastIfTag.getLineNum()< lastCond.getLineNum()) {
						//倒置结构,start位置在lastCond，或lastCond后紧跟的最后一个tag(如果有的话)
//						isReverse = true;
						start = lastCondIndex;
//						for (int j = lastCondIndex+1; j < this.len; j++) {
							Sentence s4 = this.senList.get(lastCondIndex+1);
							if (s4.getName().equals("tag")) {
								start = lastCondIndex+1;
							}
//							else{
//								break;
//							}
//						}
					}
					//####################################
					int gotoS = -1;
					GotoSentence gs = null;
					for (int j = start+1; j < this.len; j++) {
						Sentence sg = this.senList.get(j);
						if (sg.getName().equals("goto") && sg.getState() == Sentence.STATE_DOING) {
							GotoSentence gsg = (GotoSentence)sg;
							if (gsg.getTarget().equals(gotoTag)) {
								gotoS = j;
								gs = gsg;
							}
						}
					}
					boolean isWhile = false;
					if (gotoS > start) {
//						gs = (GotoSentence)this.senList.get(gotoS);
//						if (gs.getTarget().equals(gotoTag)) {
						//确认为while结构
						ifs.setWhile();
						isWhile = true;
//							break;
							
//						}
					}
					if (isWhile) {
						//确定内容块位置
						this.contEndIndex = start+1;
						this.contStartIndex = gotoS-1;
						this.contStart = this.senList.get(contStartIndex).getLineNum();
						this.contEnd = this.senList.get(contEndIndex).getLineNum();
						                          
						//合并条件
//						int lastIfIndex = this.senList.indexOf(lastCond);
						this.mergeCondsForWhile(i+1,lastCondIndex+1,lastCond,false);
						//将while的内容块移动
						ArrayList<Sentence> temp = new ArrayList<Sentence>();
						for (int j = this.contEndIndex; j <= this.contStartIndex; j++) {
							Sentence s1 = this.senList.remove(this.contEndIndex);
							temp.add(s1);
						}
						ifs.over();
						gs.over();
						lastIfTag.setEndStruct();
						lastIfTag.over();
						this.senList.remove(lastIfTag);
						//最后将lastIfTag作为结束加入temp
						temp.add(lastIfTag);
						this.senList.addAll(lastCondIndex+1, temp);
						//将倒置的if回复为STATE_DOING状态
//						if (isReverse) {
//							for (int j = i+1; j < lastIfIndex+1; j++) {
//								Sentence ifse = this.senList.get(j);
//								if (ifse.getName().equals("if")) {
//									IfSentence ifsee = (IfSentence)ifse;
//									if (ifsee.getCondTag().getLineNum()< ifsee.getLineNum()) {
//										ifsee.setState(Sentence.STATE_DOING);
//									}
//								}
//							}
//						}
					}
					
				}else{
					//FIXME 另一情况
					
					
					continue;
				}
			}
		}
	}

	/**
	 * 合并多条件,注意必须确定是多条件而非包含语句
	 * @param startIndex 条件正向开始语句(包含)
	 * @param endIndex 条件正向结束语句(不包含)
	 * @param lastIf 最后一个条件if
	 * @param isReversedIf 是否是reversedIf
	 */
	private IfSentence mergeCondsForWhile(int startIndex,int endIndex,IfSentence lastIf,boolean isReversedIf){
		//处理仅有一个if的情况
		Sentence one = null;
		int ifCC = 0;
		for (int i = startIndex; i < endIndex; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				one = s;
				ifCC++;
			}
			if (ifCC>1) {
				ifCC = 2;
				break;
			}
		}
		if (ifCC == 1) {
			IfSentence ifOne = (IfSentence)one;
			if (ifOne.getCondTag().getLineNum() < ifOne.getLineNum()) {
				ifOne.reverseCompare();
			}
			ifOne.over();
			ifOne.getCondTag().over();
			return ifOne;
		}
		//--------------------------------------------------
		int lastIfLineNum = lastIf.getLineNum();
		//确定多个if条件之后的tag
		TagSentence afterLastIfTag = null;
		int afterLastIfTagSenLn = -1;
		
		for (int i = endIndex; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag")) {
				afterLastIfTag = (TagSentence) s;
			}else{
				break;
			}
		}
		//确定afterLastIfTagSenLn
		if (afterLastIfTag == null) {
			//afterLastIfTag不存在时，虚拟一个,将afterLastIfTagSenLn设置为最后一个if的LineNum+1
			afterLastIfTag = new TagSentence(this.mgr, "");
			afterLastIfTag.setLineNum(lastIfLineNum+1);
		}
		afterLastIfTagSenLn = afterLastIfTag.getLineNum();
		
		//确定内容块前的tag，即真正的while开始位置
		TagSentence beforeContentTag = null;;
		int beforeContentTagLn = -1;
		//倒置情况下beforeContentTag需要虚拟一个,lineNum为contentLineNum+1，即afterLastIfTagSenLn+2
		boolean isReverse = false;
		TagSentence reversedTag = null;
		int reversedTagLn = -1;
		if (lastIf.getCondTag().getLineNum() < lastIf.getLineNum()) {
			beforeContentTag = new TagSentence(this.mgr, "");
			beforeContentTag.setLineNum(afterLastIfTagSenLn + 2);
			isReverse = true;
			reversedTag = lastIf.getCondTag();
			reversedTagLn = reversedTag.getLineNum();
		}else
		//其他情况下内容块上方必然是beforeContentTag
		if (this.senList.get(this.contEndIndex-1).getName().equals("tag")) {
			beforeContentTag = (TagSentence) this.senList.get(this.contEndIndex-1);
		}else{
			log.error(this.mgr.getMeth().getName()+" - contEndIndex-1 is not tag. Method: "+this.mgr.getMeth().getName());
			return null;
		}
		beforeContentTagLn = beforeContentTag.getLineNum();
		//虚拟内容行lineNum,在最后一个条件if或afterLastIfTag的lineNum后面+0.5
		int contentLineNum = afterLastIfTagSenLn + 1;
		
		TagSentence lastIfTag = null;
		//将最后一个if的tag换成afterLastIfTag,除非在scanWhile中条件有倒置的情况
		if (!isReversedIf && isReverse) {
//			System.out.println("ddd:"+this.mgr.getMeth().getName());
			if (reversedTag != null) {
				reversedTag.setLineNum(beforeContentTagLn);
			}
			
		}else{
			for (int i = endIndex; i >= startIndex; i--) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("if")) {
					IfSentence aIfs = (IfSentence) s;
					TagSentence tagAIfs = aIfs.getCondTag();
					if (tagAIfs.getLineNum() < aIfs.getLineNum()) {
						//倒置if的情况下,肯定为最后tag
						lastIfTag = tagAIfs;
						aIfs.setCondTag(afterLastIfTag);
						aIfs.reverseCompare();
						//无break,因为可能有多个if指向倒置的tag
					}else if(lastIfTag == null){
						//确定最后一个if的tag
						lastIfTag = tagAIfs;
						aIfs.setCondTag(afterLastIfTag);
						aIfs.setReversed(true);
						break;
					}
				}
			}
			
			//交换afterLastIfTag和beforeContentTag的lineNum
			afterLastIfTag.setLineNum(beforeContentTagLn);
			beforeContentTag.setLineNum(afterLastIfTagSenLn);
		}
		//--------------------------------------------------------
		int ifCount = 2;
		IfSentence ifs = null;
		TagSentence tag = null;
		int maxTimes = 1000;
		while (ifCount >= 2) {
			ifCount = 0;
			maxTimes--;
			if (maxTimes<=0) {
				log.error(this.mgr.getMeth().getName()+" - mergeCondsForWhile out of maxTimes.");
				return null;
			}
			for (int i = startIndex; i < endIndex; i++) {
				Sentence s = this.senList.get(i);
				boolean doMerge = false;
				//第一个if(doing状态)
				if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
					ifCount++;
					ifs = (IfSentence)s;
					tag = ifs.getCondTag();
					int tagIndex = this.senList.indexOf(tag);
					if (tagIndex < 0) {
						//倒置情况
						tagIndex = this.contStartIndex+1;
					}else if(tagIndex < startIndex){
						//倒置情况2
						tagIndex = endIndex;
					}
					float tag1LineNum = tag.getLineNum();
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
								if (tag2LineNum > contentLineNum) {
									ifs2.reverseCompare();
									isAnd = true;
								}else if(tag2LineNum < contentLineNum){
									isAnd = false;
								}
							}
						}
						// 合并|| 或 &&
						if (isAnd) {
							ifs.reverseCompare();
						}
						ifs.mergeIf(isAnd, ifs2);
						//是否要加括号保护
						if (tag1LineNum != tag2LineNum) {
							ifs.addCondProtect();
						}
//							//最后一个if
//						else if(ifs2.getLineNum() == lastIfLineNum){
//								ifs.addCondProtect();
//							}
//						}
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
		//回复位置和行号
//		for (int i = endIndex; i >= startIndex; i--) {
//			Sentence s = this.senList.get(i);
//			if (s.getName().equals("if")) {
//				IfSentence lifs = (IfSentence) s;
//				if (lifs.getCondTag().getLine().equals("")) {
//					lifs.setCondTag(lastIfTag);
//				}
//			}
//		}
		afterLastIfTag.setLineNum(afterLastIfTagSenLn);
		beforeContentTag.setLineNum(beforeContentTagLn);
//		reversedTag.setLineNum(reversedTagLn);
		return ifs;
	}

	
//	private int findSenIndex(int start,String typeName){
//		for (int i = start; i < this.len; i++) {
//			Sentence sen = this.senList.get(i);
//			if (sen.getName().equals(typeName)) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
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
			Sentence s = this.senList.get(i);
			if (s.getName().equals("return")) {
				//直接返回最后一行，即return行
				//return this.senList.get(this.len-1).getLineNum();
				return s.getLineNum();
			}
			//真正有输出的语句
			if(s.getName().equals("goto") && s.getState() == Sentence.STATE_DOING){
				i = this.senList.indexOf(((GotoSentence)s).getTargetSen());
			} 
//			if (s.getType() == Sentence.TYPE_LINE || s.getName().equals("if")|| s.getName().equals("return")) {
			else if (!isStructSen(s)) {
				return s.getLineNum();
			} 
		}
		return -1;
	}
	
	
	/**
	 * 设置if的toReturn为true
	 */
	private void setIfToRetrun(int conStart,IfSentence ifs){
		for (int i = conStart+1; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
				if(((TagSentence)s).isReturn()){
					ifs.setToReturn(true);
					return;
				}
				continue;
			}else if(s.getName().equals("return")){
				ifs.setToReturn(true);
				return;
			}else {
				return;
			}
		}
	}

	/**
	 * 初始化cond和ifsen以及goto的对应关系
	 */
	private boolean init(){
		this.maxLineNum = this.senList.get(len-1).getLineNum();
		//先扫描两次将cond和if对应上
		boolean returnScope = false;
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if ((s.getName().equals("tag") || s.getName().equals("gotoTag"))) {
				TagSentence ts = (TagSentence)s;
				condMap.put(ts.getTag(), ts);
				if (returnScope) {
					ts.setReturn(true);
				}
			}else if(s.getName().equals("return")){
				this.returnLineNum = s.getLineNum();
				this.returnSentence = (ReturnSentence) s;
				returnScope = true;
			}else{
				returnScope = false;
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
					log.error(this.mgr.getMeth().getName()+" - ifs cond not found: "+ifs.getCond());
				}
			}else if(s.getName().equals("goto")){
				GotoSentence gs = (GotoSentence)s;
				TagSentence ts = condMap.get(gs.getTarget());
				if (ts != null) {
					ts.setIfSen(gs);
					gs.setTargetSen(ts);
					((GotoTagSentence)ts).addGotoTimes();
				}else{
					log.error(this.mgr.getMeth().getName()+" - goto target not found: "+gs.getTarget());
				}
			}
		}
		lastSenLineNum = this.senList.get(this.len-1).getLineNum();
		return true;
	}
	private int lastSenLineNum = -1;
	/*
	/**
	 * 将return前方的tag和gotoTag移动到方法最末尾
	 
	private void backReturnSens(){
		
		//是否需要将return后置,0表示未处理或不处理,1:不需要,2:需要,3处理中
		int returnToEnd = 0;
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if(returnToEnd==0){
				if (s.getType() == Sentence.TYPE_LINE) {
					returnToEnd = 2;
				}else if(s.getName().equals("return")){
					returnToEnd = 1;
				}
			}
		}
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
//			if(s.getName().equals("return")){
//				this.returnIndex = i;
//			}
			if(returnToEnd == 2){
				if (returnIndex >= 0) {
					returnToEnd = 3;
				}
			}else if(returnToEnd == 3){
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					returnIndex = i;//this.senList.indexOf(s);
				}else{
					//returnToEnd = 0;//不再处理
					break;
				}
			}
		}
		//return 后置
		if (returnIndex > -1) {
			ArrayList<Sentence> temp = new ArrayList<Sentence>();
			while (true) {
				Sentence s1 = this.senList.get(returnIndex);
				if (s1.getName().equals("return")) {
					//return 语句本身不后置
					break;
				}else if (s1.getName().equals("if") || s1.getName().equals("goto")) {
					break;
				}else{
					if (s1.getName().equals("tag")) {
						((TagSentence)s1).setReturn(true);
					}else if(s1.getName().equals("gotoTag")){
						GotoTagSentence gts = (GotoTagSentence)s1;
						gts.setReturn(true);
						((GotoSentence)gts.getIfSen()).setReturn(true);
					}
					s1.setLineNum(lastSenLineNum+1);
					temp.add(s1);
					this.senList.remove(s1);
					lastSenLineNum++;
					continue;
				}
			}
			if (!temp.isEmpty()) {
				this.senList.addAll(temp);
			}
		}
	}*/
	
}
