/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * If结构扫描器
 * @author keel
 *
 */
class IfScaner {
	
	
	public IfScaner(IfSentence ifs,IFStructScan ifScan,int ifsPo,String methName) {
		this.ifs = ifs;
		this.methName = methName;
		this.ifScan = ifScan;
		this.ifPo = ifsPo;
		this.init(ifsPo);
	}
	
	static final Logger log = Logger.getLogger(IfScaner.class);
	/**
	 * if语句
	 */
	private IfSentence ifs;
	
	private ArrayList<Sentence> senList;
	
	private IFStructScan ifScan;
	
	private int ifPo;
	
	private String methName;
	/**
	 * if条件块结束句
	 */
//	private Sentence conditionEndSen;
	
	private boolean isWhile = false;
	
	private boolean isDoWhile = false;
	
	private GotoSentence lastGotoInCond;
	private GotoSentence lastGotoInIf;
	
//	private ArrayList<Sentence> ifLink;
	
	private ArrayList<Sentence> condLink = new ArrayList<Sentence>();
	
	private TagSentence cond;
	
	/**
	 * cond之后的第一个有意义句
	 */
	private Sentence firstSenAfterCond;
	
	/**
	 * if块扫描的开始位置
	 */
	private int ifBlockStart = -1;
	
	/**
	 * if条件数组:[条件块真正结束的index,最后一个if的index]
	 */
	private int[] ifArea; 
	/**
	 * 初始化IfScaner
	 */
	private void init(int ifsPo){
		this.senList = ifScan.getSenList();
		this.cond = ifs.getCondTag();
		ifArea = this.defineIfBlock(ifsPo);
		for (int i = ifArea[0]+1; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
				continue;
			}else{
//				conditionEndSen = s;
				ifBlockStart = i;
				break;
			}
		}
		this.ifScan.addToIfsLink(ifs);
		ifs.setIfScaner(this);
	}
	
	/**
	 * 重新初始化,用于在发生移动之后重新定位相关的位置
	 */
	private void reInit(){
		int newIfsIndex = this.senList.indexOf(this.ifs);
		//偏移量
		int plus = newIfsIndex - this.ifPo;
		this.ifPo = newIfsIndex;
		this.ifArea = this.defineIfBlock(ifPo);
		ifBlockStart = ifBlockStart + plus;
	}
	
	/**
	 * 扫描
	 */
	public void scan(){
		
		int condPo = this.senList.indexOf(cond);
		//扫描cond区
		boolean condToReturn  = this.scanCondBlock(condPo);
		if (isWhile) {
			//移动块
			int moveStart = this.senList.indexOf(this.condLink.get(0));
			int endLineNum = this.condLink.get(this.condLink.size()-1).getLineNum();
			ArrayList<Sentence> ls = new ArrayList<Sentence>();
			TagSentence cond = (TagSentence) this.senList.remove(moveStart);
			while (moveStart < this.senList.size()) {
				Sentence s = this.senList.remove(moveStart);
				ls.add(s);
				if (s.getLineNum() == endLineNum) {
					break;
				}
			}
			cond.setEndStruct().over();
			ls.add(cond);
			//插入
			this.ifs.setWhile();
			this.senList.addAll(this.ifArea[0]+1, ls);
			this.ifScan.mergeWhileConds(this.ifPo, this.ifArea);
			this.ifs.over();
			return;
		}else if(isDoWhile){
			this.ifs.setDoWhile();
			this.cond.setOut("do {");
			this.ifScan.mergeConds(this.ifPo, ifArea, this.senList.get(ifArea[0]).getLineNum()+1F);
			this.cond.over();
			return;
		}else {
			//扫描if区
			this.scanIfBlock(this.ifArea[0]+1);
			
			
		}
		
		if (condToReturn) {
			
		}
	}
	
	/**
	 * 扫描if区
	 * @param start
	 * @return 是否完成处理
	 */
	private boolean scanIfBlock(int start){
		boolean gotoTurn = false;
		int lastSenIndex = start;
		//gtMap用于防止同一goto反复循环
		HashMap<Integer,Sentence> gtMap = new HashMap<Integer, Sentence>();
		for (int i = start; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if(s.getName().equals("if") && s.getState() != Sentence.STATE_OVER){
				IfSentence ifsen = (IfSentence)s;
				//创建新ifScaner进行处理
				IfScaner scaner = new IfScaner(ifsen, ifScan, i,methName);
				scaner.scan();
				i = this.senList.indexOf(s)-1;
				this.reInit();
				continue;
			}else if (s.getLineNum() == this.cond.getLineNum() || (firstSenAfterCond!= null && s.getLineNum() == this.firstSenAfterCond.getLineNum())) {
				//普通if(无else)
				
				if (!gotoTurn) {
					this.cond.setEndStruct().over();
				}else{
					int addPo = lastSenIndex;
					for (int j = lastSenIndex-1; j >=0; j--) {
						Sentence s1 = this.senList.get(j);
						if (s1.getName().equals("tag") || s1.getName().equals("gotoTag")) {
							addPo--;
						}else{
							break;
						}
					}
					this.senList.add(addPo, this.makeEnd());
					this.cond.over();
					this.lastGotoInIf.over();
				}
				this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
				this.ifs.over();
				return true;
			}else if(s.getName().equals("goto")){
				GotoSentence gt = (GotoSentence)s;
				if (s.getState() != Sentence.STATE_OVER) {
					GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
					if (!gotoTurn && this.lastGotoInCond !=null && this.lastGotoInCond.getTargetSen().getLineNum() == gt.getTargetSen().getLineNum()) {
						//else块插入
						elseInsert(i,s);
						return true;
					}
					if (gtMap.containsKey(gt.getLineNum())) {
						break;
					}else{
						gtMap.put(gt.getLineNum(), gt);
					}
					i = this.senList.indexOf(gtTag)-1;
					this.lastGotoInIf = gt;
					gotoTurn = true;
					lastSenIndex++;
				}else{
					
				}
			}else if(s.getName().equals("gotoTag")){
				if (this.lastGotoInCond != null && this.lastGotoInCond.getTargetSen().getLineNum() == s.getLineNum()) {
					//else块插入
					elseInsert(i,s);
					return true;
				}
				
			}else if(s.getName().equals("return")){
				break;
			}
			if (!gotoTurn) {
				lastSenIndex = i;
			}
			
		}
		
		log.error(this.methName+"-scanIfBlock failed. can't find else insert point!");
		//作为普通if处理
		this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(ifArea[0]).getLineNum()+1F);
		this.ifs.over();
		this.cond.setEndStruct().over();
		return false;
	}
	
	
	
	
	/**
	 * 扫描cond区
	 * @param start
	 * @return 是否到达return
	 */
	private boolean scanCondBlock(int start){
		//是否跳转，用于判断别do-while和while
		boolean gotoTurn = false;
		boolean toReturn = true;
		//gtMap用于防止同一goto反复循环
		HashMap<Integer,Sentence> gtMap = new HashMap<Integer, Sentence>();
		for (int i = start; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if(s.getName().equals("if") && s.getState() != Sentence.STATE_OVER){
				IfSentence ifsen = (IfSentence)s;
				if (this.ifScan.getIfsLink().contains(ifsen)) {
					//在if链中找到,处理成while或doWhile
					if (gotoTurn) {
						ifsen.getIfScaner().setWhile(true);
					}else{
						ifsen.getIfScaner().setDoWhile(true);
					}
					toReturn = false;
					if (this.lastGotoInCond != null) {
						this.lastGotoInCond.over();
					}
					break;
				}else{
					//创建新ifScaner进行处理
					IfScaner scaner = new IfScaner(ifsen, ifScan, i,methName);
					scaner.scan();
					i = this.senList.indexOf(s)-1;
					this.reInit();
					continue;
				}
			}else if (s.getName().equals("return")) {
				toReturn = true;
				if (firstSenAfterCond == null) {
					firstSenAfterCond = s;
				}
				break;
			}else if(s.getName().equals("goto")){
				GotoSentence gt = (GotoSentence)s;
				if (s.getState() != Sentence.STATE_OVER) {
					GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
					int gtTagLineNum = gtTag.getLineNum();
					
					if (gtTag.isReturn()) {
						//判断return;
						toReturn = true;
						this.condLink.add(s);
						this.lastGotoInCond = gt;
						gt.over();
						break;
					}else if(this.ifScan.isInWhileStartTag(gtTagLineNum)){
						//判断continue
						IfSentence whileSen = this.ifScan.getWhileFromWhileStartTags(gtTagLineNum);
						IfSentence currentWhile = this.ifScan.getCurrentWhile();
						if (currentWhile != null && whileSen.getLineNum() == currentWhile.getLineNum()) {
							gt.setContinue(null, "");
						}else{
							//其他层次的continue
							gt.setContinue("someWhile", whileSen.getOut());
						}
						gt.over();
						this.condLink.add(s);
						break;
					}else if(this.ifScan.isInWhileEndTag(gtTagLineNum)){
						//判断break
						IfSentence whileSen = this.ifScan.getWhileFromWhileEndTags(gtTagLineNum);
						IfSentence currentWhile = this.ifScan.getCurrentWhile();
						if (currentWhile != null && whileSen.getLineNum() == currentWhile.getLineNum()) {
							gt.setBreak(null, "");
						}else{
							//其他层次的continue
							gt.setBreak("someWhile", whileSen.getOut());
						}
						gt.over();
						this.condLink.add(s);
						break;
					}
					//防止反复循环
					if (gtMap.containsKey(gt.getLineNum())) {
						break;
					}else{
						gtMap.put(gt.getLineNum(), gt);
					}
					i = this.senList.indexOf(gtTag);
					this.lastGotoInCond = gt;
					gotoTurn = true;
					this.condLink.add(s);
					continue;
				}else{
					//已经处理过的goto加入lastGotoInCond
					if (!gotoTurn) {
						this.lastGotoInCond = gt;
					}
				}
			}
			//其他语句直接加入cond链,除了turn之后的
			if (!gotoTurn) {
				this.condLink.add(s);
			}
			if (firstSenAfterCond == null && !s.getName().equals("tag") && !s.getName().equals("gotoTag")) {
				firstSenAfterCond = s;
			}
		}
		
		return toReturn;
	}
	
	
	/**
	 * else块插入
	 * @param i
	 * @param s
	 */
	private void elseInsert(int i,Sentence s){
		int moveStart = this.senList.indexOf(this.condLink.get(0));
		int endLineNum = this.condLink.get(this.condLink.size()-1).getLineNum();
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		while (moveStart < this.senList.size()) {
			Sentence s1 = this.senList.remove(moveStart);
			ls.add(s1);
			if (s1.getLineNum() == endLineNum) {
				break;
			}
		}
		//判断else if还是else
		Sentence condNext = ls.get(1);
		boolean isLastElse = true;
		if (condNext.getName().equals("if")) {
			IfSentence ifin = (IfSentence)condNext;
			if (!ifin.isWhile()) {
				ifin.setElse(true);
				ifin.setClosePre(true);
				isLastElse = false;
			}
		}
		if (isLastElse) {
			this.cond.setOut("} else {");
			ls.add(this.makeEnd());
		}
		this.cond.over();
		this.senList.addAll(i,ls);
		this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
		this.ifs.over();
		this.lastGotoInCond.over();
		s.over();
	}
	
	private final Sentence makeEnd(){
		OtherSentence endCond = new OtherSentence(this.ifs.mgr, "} //end of if");
		endCond.setOut(StaticUtil.TABS[this.ifs.getLevel()]+"}");
		endCond.setType(Sentence.TYPE_STRUCT);
		endCond.over();
		return endCond;
	}
	
	/**
	 * 定位if条件块,去除if条件之外的if
	 * @param beginIndex 开始if的index,包含
	 * @return 数组:[条件块真正结束的index,最后一个if的index]
	 */
	private int[] defineIfBlock(int beginIndex){
		int endIndex = beginIndex;
		int lastIfIndex = beginIndex;
		int realEndIndex = endIndex;
		//正向查询,先将连续的if和tag扫描完
		for (int i = beginIndex; i < this.senList.size(); i++) {
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
		boolean over = false;
		//lastCondIndex为唯一指向非if区块的tag语句位置
		int lastCondIndex = -1;
		for (int i = beginIndex; i <= endIndex ; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				int condIndex = this.senList.indexOf(ifs.getCondTag());
				if (condIndex >= beginIndex && condIndex <= endIndex) {
					realEndIndex = i;
					lastIfIndex = i;
					continue;
				}else if(over && condIndex == lastCondIndex){
					realEndIndex = i;
					lastIfIndex = i;
					continue;
				}else{
					if (!over) {
						lastIfIndex = i;
						realEndIndex = i;
						over = true;
						lastCondIndex = condIndex;
						continue;
					}else{
						break;
					}
				}
			}else if(s.getName().equals("tag")){
				realEndIndex = i;
				continue;
			}
		}
		return new int[]{realEndIndex,lastIfIndex};
	}

	/**
	 * @return the isWhile
	 */
	final boolean isWhile() {
		return isWhile;
	}

	/**
	 * 设置while并处理whileStartTags,whileEndTags,currentWhile
	 * @param isWhile the isWhile to set
	 */
	final void setWhile(boolean isWhile) {
		this.isWhile = isWhile;
		if (this.isWhile) {
			for (int i = this.ifPo-1; i < 0; i--) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					this.ifScan.addWhileStartTag(s.getLineNum(),this.ifs);
				}else{
					break;
				}
			}
			this.ifScan.setCurrentWhile(this.ifs);
			for (int i = this.ifArea[0]; i < this.senList.size(); i++) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					this.ifScan.addWhileEndTag(s.getLineNum(),this.ifs);
				}else{
					break;
				}
			}
		}
		//FIXME do while也有break
	}

	/**
	 * @return the isDoWhile
	 */
	final boolean isDoWhile() {
		return isDoWhile;
	}

	/**
	 * @param isDoWhile the isDoWhile to set
	 */
	final void setDoWhile(boolean isDoWhile) {
		this.isDoWhile = isDoWhile;
	}

	/**
	 * @return the ifs
	 */
	final IfSentence getIfs() {
		return ifs;
	}

	
	
}
