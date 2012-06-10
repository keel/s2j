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
		//处理while结构
		//this.scanWhileStruct(0);
		
		//-------------------------------------------------------------------------
		//if结构中包含return时只处理一次
		boolean isReturned = true;
		//从后向前扫描
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if (s.getName().equals("if")) {
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
				this.doStruct();
				i = this.contEndIndex;
			}
			
		}
	}
	
	/**
	 * 分析并处理某一内容块的if结构
	 * @return 返回是否所包含语句全部处理完
	 */
	private boolean doStruct(){
//		if (this.ifls.isEmpty()) {
//			return true;
//		}
//		int le = this.ifls.size();
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
		ifs = this.mergeConds(condStartIndex, this.contEndIndex);
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
		IfSentence ifs2 = null;
		TagSentence tag = null;
		TagSentence tag2 = null; 
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
						//外部if正指向内容块后,为&&
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
		/*
		//确定contEndIndex后面紧跟的tag: endtag，这是if结束的标记
		//定位endtag所指向的最上面的if(可能有多个): startIf,暂定为内容块if开始的标记
		//向后扫描startIf后的tag，如tag所对应的if位置在startIf之上，则将此if更新为startIf
		//扫描到line为止,最终确定if开始位置为startIf
		
		//确定内容块前后tag的index
		int beforeConLineNum = -1;
		int afterConLineNum = -1;
		Sentence sen = this.senList.get(contEndIndex-1);
		if (sen != null && sen.getName().equals("tag")) {
			beforeConLineNum = sen.getLineNum();
		}
		sen = this.senList.get(contStartIndex+1);
		if (sen != null && sen.getName().equals("tag")) {
			afterConLineNum = sen.getLineNum();
		}
		//此内容块对应的if最开始的位置
		int ifStartIndex = 0;
		if (afterConLineNum != -1) {
			//最上面一个对应afterConTag的if位置可能为此内容块最外围的开始位置,注意不一定是
			//有可能其包含的一个tag对应的if
			TagSentence t = (TagSentence) this.senList.get(afterConLineNum);
			ifStartIndex = this.senList.indexOf(t.getIfSen());
		}
		//进一步确认if最上方的位置ifStartIndex
		for (int i = this.contEndIndex+1; i >= ifStartIndex; i--) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag")) {
				TagSentence tag = (TagSentence)s;
				int ti = this.senList.indexOf(tag.getIfSen());
				if (ti < ifStartIndex) {
					ifStartIndex = ti;
				}
			}
		}
		
		//反向扫描：
		ArrayList<IfSentence> conds = new ArrayList<IfSentence>();
		boolean scanAgain = true;
		while(scanAgain){
			//扫描处理可合并的if
			for (int i = contEndIndex-1; i >= ifStartIndex; i--) {
				sen = this.senList.get(i);
				if (sen.getName().equals("if") && sen.getState()== Sentence.STATE_DOING) {
					IfSentence ifs = (IfSentence)sen;
					TagSentence tag = ifs.getCondTag();
					if (tag.getLineNum()>= afterConLineNum) {
						ifs.setAddIfLogic("&&");
					}else if(tag.getLineNum() == beforeConLineNum){
						ifs.setAddIfLogic("||");
					}else {
						//if与其tag中间包含if的情况
						int tIndex = this.senList.indexOf(tag);
						for (int j = i+1; j < tIndex; j++) {
							Sentence innerSen = this.senList.get(j);
							if (innerSen.getName().equals("if")) {
								//包含处理
								
								
							}
						}
					}
				}
			}
			//是否所有的if都处理完
			boolean moreScan = false;
			for (int i = contEndIndex-1; i >= ifStartIndex; i--) {
				sen = this.senList.get(i);
				if (sen.getName().equals("if") && sen.getState() != Sentence.STATE_OVER) {
					moreScan = true;
				}
			}
			scanAgain = moreScan;
		}
		
		
		for (int i = contEndIndex-1; i >= ifStartIndex; i--) {
			sen = this.senList.get(i);
			if (sen.getName().equals("if") && sen.getState()== Sentence.STATE_DOING) {
				IfSentence ifs = (IfSentence)sen;
				
				conds.add(ifs);
				TagSentence tag = ifs.getCondTag();
				if (tag.getLineNum() == afterConLineNum) {
					ifs.reverseCompare();
				}
				
				
				if (!conds.isEmpty()) {
					//尝试合并
					//紧跟的if进行合并
					IfSentence last = conds.get(conds.size()-1);
					//
					if (tag.getLineNum() == last.getLineNum()) {
						//紧跟的if
						conds.remove(last);
						if (tag.getLineNum() > this.contEndIndex) {
							last.mergeIf(true, ifs);
						}else if(tag.getLineNum() < contStartIndex){
							last.mergeIf(false, ifs);
						}
						//更新contStartIndex,contEndIndex
					}
					if (tag.getLineNum() == afterConLineNum) {
						
					}else if(tag.getLineNum() == beforeConLineNum){
						
					}
				}
			}
		}
		*/
		
		//从contStartIndext向上找，如果上面一条是tag,则此tag为起始tag,一定有两个||条件存在,先跳过直到if出现
		
		//将连续的if(无任何非if语句，无tag),先条件合并
		
		//再向上碰到tag,注意此tag对应的if是否
		
		//如if上面为tag，则将tag对应的最近的if与上一步连续的if合并后的条件再次合并(||关系)
		
		//ifs.over();
	}


	/**
	 * 扫描并处理while结构(不包含do while)
	 */
	private void scanWhileStruct(int index){
		
		while (index<this.len && this.findSenIndex(index,"gotoTag",null) > index ) {
			String gotoTag = ((TagSentence)this.senList.get(index)).getTag();
			//goto tag后紧跟if,有可能为while
			Sentence sen = this.senList.get(index+1);
			if (sen.getName().equals("if")) {
				//确认if目标后有goto指向goto tag
				index++;
				IfSentence ifs = (IfSentence)sen;
				TagSentence tag = ifs.getCondTag();
				int start = this.senList.indexOf(tag);
				int gotoS = this.findSenIndex(start, "goto",null);
				while (gotoS > start) {
					GotoSentence gs = (GotoSentence)this.senList.get(gotoS);
					if (gs.getTarget().equals(gotoTag)) {
						//确认为while
						ifs.setWhile();
						//扫描多条件
						//找到紧跟的if,碰到非tag和if时终止多条件 
						IfSentence lastCond = ifs;
						for (int i = index; i < this.len; i++) {
							Sentence s = this.senList.get(i);
							if (s.getName().equals("gotoTag")) {
									break;
							}else if(s.getName().equals("if")){
								lastCond = (IfSentence)s;
								//ifs.addIF(lastCond);
							}else {
								break;
							}
						}
						tag = lastCond.getCondTag();
						start = this.senList.indexOf(tag);
						gotoS = this.findSenIndex(start, "goto",null);
						this.contStart = start;
						this.contEnd = gotoS;
						                                              
						//this.mergeConds(ifs);
						//将while的内容块移动
						for (int i = this.contStart; i < this.contEnd; i++) {
							Sentence s1 = this.senList.remove(i);
							this.senList.add(index, s1);
							//移动index
							index++;
						}
						ifs.over();
					}
					gotoS++;
					gotoS = this.findSenIndex(gotoS, "goto",null);
				}
			}
			
		}
	}
	
	
//	private int findGotoTag(int start){
//		int tagIndex = start;
//		while (tagIndex < this.len) {
//			tagIndex = this.findSenIndex(tagIndex, "tag",null);
//			if (tagIndex == -1) {
//				return start;
//			}
//			TagSentence sen = (TagSentence)this.senList.get(tagIndex);
//			if (sen.isGoto()) {
//				return tagIndex;
//			}
//			tagIndex++;
//		}
//		return -1;
//	}
	
	private int findSenIndex(int start,String typeName,String[] exceptNames){
		for (int i = start; i < this.len; i++) {
			Sentence sen = this.senList.get(i);
			if (exceptNames != null) {
				for (int j = 0; j < exceptNames.length; j++) {
					if (sen.getName().equals(exceptNames[j])) {
						return -1;
					}
				}
			}
			if (sen.getName().equals(typeName)) {
				return i;
			}
		}
		return -1;
	}
	
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
			}else if(returnToEnd >= 2){
				if (returnLine == -1 && s.getName().equals("return")) {
					returnLine = this.senList.indexOf(s);
					returnToEnd = 3;
				}else if(returnToEnd == 3){
					if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
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
					s1.setLineNum(last+1);
					temp.add(s1);
					this.senList.remove(s1);
					last++;
					continue;
				}
			}
			this.senList.addAll(this.senList.size(), temp);
		}
		
		return true;
	}
	
}
