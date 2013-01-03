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
	/**
	 * 可能是do while,用于标记第一次do while
	 */
	private boolean maybeDoWhile = false;
	/**
	 * 可能是while,用于do while处理
	 */
	private boolean maybeWhile = false;
	
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
	 * if条件数组:[条件块真正结束的index,最后一个if的index,整个if块对应的cond的index]
	 */
	private int[] ifArea; 
	
	/**
	 * 是否执行了if块扫描
	 */
	private boolean scanIfBlock = false;
	
	/**
	 * 被外部If的tag打断cond扫描
	 */
	private boolean stopByOutIf = false;
	
	/**
	 * 内部的if块到达了cond位置, 需要作为普通if处理
	 */
	private boolean innerIfReachCondTag = false;

	
	/**
	 * 初始化IfScaner
	 */
	private void init(int ifsPo){
		this.senList = ifScan.getSenList();
		ifArea = this.defineIfBlock(ifsPo);
		this.cond = (TagSentence)this.senList.get(this.ifArea[2]);
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
	 * cond块到达return
	 */
	private boolean condToReturn = false;
	/**
	 * 扫描
	 */
	public void scan(){
		
		int condPo = this.senList.indexOf(cond);
		//扫描cond区
		this.condToReturn  = this.scanCondBlock(condPo);
		if (isWhile) {
			//移动块
			int moveStart = this.senList.indexOf(this.condLink.get(0));
			Sentence endLink = this.condLink.get(this.condLink.size()-1);
			ArrayList<Sentence> ls = new ArrayList<Sentence>();
			TagSentence cond = (TagSentence) this.senList.remove(moveStart);
			while (moveStart < this.senList.size()) {
				Sentence s = this.senList.remove(moveStart);
				ls.add(s);
				if (s == endLink) {
					break;
				}
			}
			cond.setEndStruct().over();
			ls.add(cond);
			//插入
			this.ifs.setWhile();
			this.senList.addAll(this.ifArea[0]+1, ls);
			this.reInit();
			this.ifScan.mergeWhileConds(this.ifPo, this.ifArea);
			this.ifs.over();
			if (this.lastGotoInCond != null) {
				this.lastGotoInCond.over();
			}
			clearWhileTags();
			return;
		}else if(isDoWhile){
			doWhile();
//			clearWhileTags();
			return;
		}else {
			//扫描if区
			this.scanIfBlock(this.ifArea[0]+1);
			
		}
		
		if (this.scanIfBlock) {
			this.ifScan.removeIfScanTag(this.cond.getLineNum());
		}
	}
	
	private boolean doWhile(){
		if (this.isDoWhile || (!this.isWhile && this.maybeDoWhile)) {
			this.isDoWhile = true;
			this.ifs.setDoWhile();
			this.cond.setOut("do {");
			this.ifScan.mergeConds(this.ifPo, ifArea, this.senList.get(ifArea[0]).getLineNum()+1F);
			this.cond.over();
			this.ifs.over();
			return true;
		}
		return false;
	}
	
	/**
	 * 扫描if区
	 * @param start
	 * @return 是否完成处理
	 */
	private boolean scanIfBlock(int start){
		boolean gotoTurn = false;
		int lastSenIndex = start;
		//将if的cond作为scan结束的停止句
		this.ifScan.addIfScanTag(this.cond.getLineNum(),this.senList.indexOf(this.cond), ifs);
		this.scanIfBlock = true;
		//gtMap用于防止同一goto反复循环
		HashMap<Integer,Sentence> gtMap = new HashMap<Integer, Sentence>();
		//stopByOutIf时,if块的goto句的index
		int ifGotoIndex = -1;
		int i = start;
		for (; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getLineNum() == this.cond.getLineNum() || (firstSenAfterCond!= null && s.getLineNum() == this.firstSenAfterCond.getLineNum())) {
				//普通if(无else)
				this.maybeDoWhile = false;
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
					this.makeEnd(addPo);
					this.reInit();
					this.cond.over();
					this.lastGotoInIf.over();
				}
				this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
				this.ifs.over();
				return true;
			}else if(s.getName().equals("if") && s.getState() != Sentence.STATE_OVER){
				IfSentence ifsen = (IfSentence)s;
				//创建新ifScaner进行处理
				IfScaner scaner = new IfScaner(ifsen, ifScan, i,methName);
				scaner.scan();
				i = this.senList.indexOf(s)-1;
				this.reInit();
				continue;
			}else if(s.getName().equals("goto")){
				GotoSentence gt = (GotoSentence)s;
				if (s.getState() != Sentence.STATE_OVER) {
					GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
					int gtTagIndex = this.senList.indexOf(gtTag);
					//先查找上方tag,判断是否是普通if块
					for (int j = gtTagIndex-1; j >=0; j--) {
						Sentence ss2 = this.senList.get(j);
						if (ss2.getName().equals("tag")) {
							if (ss2 == this.cond) {
								//普通if块
								this.maybeDoWhile = false;
								int addPo = i-1;
								this.makeEnd(addPo);
								this.reInit();
								this.cond.over();
								this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
								this.ifs.over();
								s.over();
								return true;
							}
						}else if (ss2.getName().equals("gotoTag")) {
							continue;
						}else{
							break;
						}
					}
					//else处理
					if (!gotoTurn && this.lastGotoInCond !=null && this.lastGotoInCond.getTargetSen().getLineNum() == gt.getTargetSen().getLineNum()) {
						//else块插入
						elseInsert(i,s);
						this.checkWhileContinue(gt);
						return true;
					}else if(this.stopByOutIf){
						ifGotoIndex = i;
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
				}
				this.checkWhileContinue(gt);
			}else if(s.getName().equals("gotoTag")){
				if (this.lastGotoInCond != null && this.lastGotoInCond.getTargetSen().getLineNum() == s.getLineNum()) {
					if (this.innerIfReachCondTag) {
						//内部if碰到了condTag，应判断为普通if,但需要移动
						int moveStart = this.senList.indexOf(this.condLink.get(0));
						Sentence endLink = this.condLink.get(this.condLink.size()-1);
						ArrayList<Sentence> ls = new ArrayList<Sentence>();
						while (moveStart < this.senList.size()) {
							Sentence s1 = this.senList.remove(moveStart);
							ls.add(s1);
							if (s1 == endLink) {
								break;
							}
						}
						this.cond.setEndStruct().over();
						this.senList.addAll(i,ls);
						this.reInit();
						this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
						this.ifs.over();
						if (lastGotoInCond != null) {
							this.lastGotoInCond.over();
						}
					}else{
						//else块插入
						elseInsert(i,s);
					}
					return true;
				}
				
			}else if(s.getName().equals("return")){
				if(this.doWhile()){
					return true;
				}
				break;
			}
			if (!gotoTurn) {
				lastSenIndex = i;
			}
			
		}
		

		//maybeDoWhile为true时，如果在if块中未再次处理到，则确认为do while
		if(this.doWhile()){
			return true;
		}
		
		//stopByOutIf后无法在if块内处理,作为else块插入
		if(this.stopByOutIf){
			if (ifGotoIndex > -1) {
				//else块插入
				elseInsert(ifGotoIndex,this.senList.get(ifGotoIndex));
				return true;
			}else if(this.condToReturn){
				//else块插入
				for (int j = i; j>= 0; j--) {
					Sentence sj = this.senList.get(j);
					if (sj.getName().equals("return") || sj.getName().equals("gotoTag")) {
						continue;
					}else{
						elseInsert(j+1,this.senList.get(j+1));
						return true;
					}
				}
				
			}
		}
		
		//cond块到达return,if块无接应的情况
		if (this.condToReturn) {
			this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(ifArea[0]).getLineNum()+1F);
			this.ifs.over();
			this.makeEnd(this.senList.size()-1);
			this.cond.over();
			return true;
		}
		if (gotoTurn && this.lastGotoInIf !=null) {
			//else块插入
			int po = this.senList.indexOf(this.lastGotoInIf);
			elseInsert(po,lastGotoInIf);
			this.checkWhileContinue(lastGotoInIf);
			return true;
		}
		
		log.error(this.methName+"-scanIfBlock failed. can't find else insert point!");
		//作为普通if处理
		this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(ifArea[0]).getLineNum()+1F);
		this.ifs.over();
		this.makeEnd(this.senList.size()-1);
		this.cond.over();
		return false;
	}
	
	
	/**
	 * 扫描cond区
	 * @param start
	 * @return 是否到达return
	 */
	private boolean scanCondBlock(int start){
		//是否跳转，用于判断别do-while和while
		boolean gotoTurn = false;//
		boolean ifTurn = (this.senList.indexOf(this.cond) < this.ifPo);
		boolean toReturn = true;
		//gtMap用于防止同一goto反复循环
		HashMap<Integer,Sentence> gtMap = new HashMap<Integer, Sentence>();
		for (int i = start; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if(this.ifScan.isInIfScanTag(s.getLineNum())){
				//处理到了外部if块的cond，需要结束
				this.stopByOutIf = true;
				this.ifScan.getIfScanTag(s.getLineNum()).getIfScaner().setInnerIfReachCondTag(true);
				break;
			}else if(s.getName().equals("if") && s.getState() != Sentence.STATE_OVER){
				IfSentence ifsen = (IfSentence)s;
				if (this.ifScan.getIfsLink().contains(ifsen)) {
					//在if链中找到,处理成while或doWhile
					if (gotoTurn) {
						ifsen.getIfScaner().setWhile(true);
						if (this.lastGotoInCond != null) {
							ifsen.getIfScaner().putTopTag(this.lastGotoInCond);
						}
					}else if(ifTurn){
//						this.setDoWhile(true);
						ifsen.getIfScaner().setMaybeWhile(true);
						this.maybeDoWhile = true;
					}else{
						log.error(this.methName+ " maybe it's a while");
					}
					if (firstSenAfterCond == null) {
						firstSenAfterCond = s;
					}
					toReturn = false;
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
				GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
				int gtTagLineNum = gtTag.getLineNum();
				if (s.getState() != Sentence.STATE_OVER) {
					if (gtTag.isReturn()) {
						//判断return;
						toReturn = true;
						this.condLink.add(s);
						if (!gotoTurn) {
							this.lastGotoInCond = gt;
							gotoTurn = true;
						}
//						gt.over();
						break;
					}else if(this.ifScan.isInWhileStartTag(gtTagLineNum)){
						//判断continue
						this.checkWhileContinue(gt);
						gt.over();
						gotoTurn = true;
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
						gotoTurn = true;
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
					if (!gotoTurn) {
						this.lastGotoInCond = gt;
					}
					gotoTurn = true;
					this.condLink.add(s);
					continue;
				}else{
					//已经处理过的goto加入lastGotoInCond
					if (!gotoTurn) {
						this.lastGotoInCond = gt;
					}
					//判断while中的continue或end
					if(this.isWhile && (this.removeTopTag(s.getLineNum())!=null)){
						if (this.isTopTagEmpty()) {
							//end
							gotoTurn = true;
							this.condLink.add(s);
							break;
						}else{
							//continue
							gt.setContinue(null, "");
						}
					} else if (this.maybeWhile) {
						int gotoTagIndex = this.senList.indexOf(gtTag);
						boolean sureWhile = false;
						for (int j = gotoTagIndex+1; j < this.senList.size(); j++) {
							Sentence ss1 = this.senList.get(j);
							if (ss1.getName().equals("tag") || ss1.getName().equals("gotoTag")) {
								continue;
							}else{
								if (ss1 == this.ifs) {
									sureWhile = true;
								}
								break;
							}
						}
						if (sureWhile) {
							this.setWhile(true);
							if (this.lastGotoInCond != null) {
								this.putTopTag(this.lastGotoInCond);
							}
							this.condLink.add(s);
							break;
						}
								
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
	 * @param i 插入位置,如果此位置语句为goto，则将插入位置向后跳过goto句
	 * @param s
	 */
	private void elseInsert(int i,Sentence s){
		this.maybeDoWhile = false;
		if (this.condLink.size() == 0) {
			//因为到达外部if的cond可能导致condLink为空,此时按普通if处理
			this.makeEnd(i);
			this.reInit();
			this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
			this.ifs.over();
			if (lastGotoInCond != null) {
				this.lastGotoInCond.over();
			}
			s.over();
			return;
		}
		int moveStart = this.senList.indexOf(this.condLink.get(0));
		Sentence endLink = this.condLink.get(this.condLink.size()-1);
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		while (moveStart < this.senList.size()) {
			Sentence s1 = this.senList.remove(moveStart);
			ls.add(s1);
			if (s1 == endLink) {
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
			this.makeEnd(ls.size()-1, ls);
		}
		this.cond.over();
		if (s.getName().equals("goto")) {
			this.senList.addAll(i+1,ls);
		}else{
			//判断上方是否有外部cond结束
			int addPo = i;
			for (int j = i-1; j >= 0; j--) {
				Sentence s2 = this.senList.get(j);
				if (s2.getName().equals("tag")) {
					if (this.ifScan.isInIfScanTag(s2.getLineNum())) {
						addPo = j;
					}
				}else if(s2.getName().equals("gotoTag")){
					continue;
				}else{
					break;
				}
			}
			this.senList.addAll(addPo,ls);
		}
		this.reInit();
		this.ifScan.mergeConds(this.ifPo, this.ifArea, this.senList.get(this.ifArea[0]).getLineNum()+1F);
		this.ifs.over();
		if (lastGotoInCond != null) {
			this.lastGotoInCond.over();
		}
		s.over();
	}
	
	
	/**
	 * 确认goto是否continue或while的end
	 * @param gt
	 */
	private void checkWhileContinue(GotoSentence gt){
		GotoTagSentence gtTag = (GotoTagSentence) gt.getTargetSen();
		int gtTagLineNum = gtTag.getLineNum();
		if(this.ifScan.isInWhileStartTag(gtTagLineNum)){
			IfSentence whileSen = this.ifScan.getWhileFromWhileStartTags(gtTagLineNum);
			IfSentence currentWhile = this.ifScan.getCurrentWhile();
			if (currentWhile != null && whileSen.getLineNum() == currentWhile.getLineNum()) {
//				gt.setContinue(null, "");
				whileSen.getIfScaner().putTopTag(gt);
			}else{
				//其他层次的continue
				gt.setContinue("someWhile", whileSen.getOut());
			}
		}
	}

	/**
	 * 有可能是while的continue或end
	 */
	private HashMap<Integer,Sentence> whileTopTags;
	
	void putTopTag(Sentence s){
		if (this.whileTopTags == null) {
			this.whileTopTags = new HashMap<Integer, Sentence>();
		}
		this.whileTopTags.put(s.getLineNum(), s);
	}
	
	boolean isTopTagEmpty(){
		if (this.whileTopTags == null) {
			return true;
		}
		return this.whileTopTags.isEmpty();
	}
	
	Sentence removeTopTag(int lineNum){
		if (this.whileTopTags == null) {
			return null;
		}
		return this.whileTopTags.remove(lineNum);
	}
	
	Sentence getTopTag(int lineNum){
		if (this.whileTopTags == null) {
			return null;
		}
		return this.whileTopTags.get(lineNum);
	}
	
	/**
	 * 创建一个OtherSentence作为结束句,并插入到指定位置向上的非goto句位置
	 */
	private final void makeEnd(int po){
		makeEnd(po,this.senList);
	}
	
	private final void makeEnd(int po,ArrayList<Sentence> ls){
		Sentence endCond = makeAnEnd();
		for (int i = po; i >= 0; i--) {
			Sentence s = ls.get(i);
			if (s.getName().equals("goto")) {
				continue;
			}else{
				ls.add(i+1, endCond);
				break;
			}
		}
		
	}
	
	private final Sentence makeAnEnd(){
		OtherSentence endCond = new OtherSentence(this.ifs.mgr, "} //end of if");
		endCond.setOut(StaticUtil.TABS[this.ifs.getLevel()]+"}");
		endCond.setType(Sentence.TYPE_STRUCT);
		endCond.over();
		return endCond;
	}
	
	/**
	 * 定位if条件块,去除if条件之外的if
	 * @param beginIndex 开始if的index,包含
	 * @return 数组:[条件块真正结束的index,最后一个if的index,最后一个if对应的tag的Index(即整个if块的cond)]
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
		HashMap<Integer,Sentence> condMap = new HashMap<Integer, Sentence>();
		for (int i = beginIndex; i <= endIndex ; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence cond = ifs.getCondTag();
				int condIndex = this.senList.indexOf(cond);
				condMap.put(cond.getLineNum(), cond);
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
				if (condMap.containsKey(s.getLineNum()) && s == condMap.get(s.getLineNum())) {
					realEndIndex = i;
					continue;
				}else{
					break;
				}
			}
		}
		return new int[]{realEndIndex,lastIfIndex,lastCondIndex};
	}

	/**
	 * @return the isWhile
	 */
	final boolean isWhile() {
		return isWhile;
	}

	/**
	 * 记录本类的whileStartTags
	 */
	private ArrayList<Integer> whileTags;
	/**
	 * 记录本类的whileEndTags
	 */
	private ArrayList<Integer> whileTags2;
	
	private void clearWhileTags(){
		int len = this.whileTags.size();
		for (int i = 0; i < len; i++) {
			this.ifScan.removeWhileStartTag(this.whileTags.get(i));
		}
		len = this.whileTags2.size();
		for (int i = 0; i < len; i++) {
			this.ifScan.removeWhileEndTag(this.whileTags2.get(i));
		}
	}
	
	/**
	 * 设置while并处理whileStartTags,whileEndTags,currentWhile
	 * @param isWhile the isWhile to set
	 */
	final void setWhile(boolean isWhile) {
		this.isWhile = isWhile;
		if (this.isWhile) {
			this.maybeDoWhile = false;
			whileTags = new ArrayList<Integer>();
			whileTags2 = new ArrayList<Integer>();
			for (int i = this.ifPo-1; i >= 0; i--) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					this.ifScan.addWhileStartTag(s.getLineNum(),this.ifs);
					whileTags.add(s.getLineNum());
				}else{
					break;
				}
			}
			this.ifScan.setCurrentWhile(this.ifs);
			for (int i = this.ifArea[0]+1; i < this.senList.size(); i++) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("tag") || s.getName().equals("gotoTag")) {
					this.ifScan.addWhileEndTag(s.getLineNum(),this.ifs);
					whileTags2.add(s.getLineNum());
				}else{
					break;
				}
			}
		}
		//FIXME do while也有break
	}
	
	

	/**
	 * @return the maybeWhile
	 */
	final boolean isMaybeWhile() {
		return maybeWhile;
	}

	/**
	 * @param maybeWhile the maybeWhile to set
	 */
	final void setMaybeWhile(boolean maybeWhile) {
		this.maybeWhile = maybeWhile;
	}

	/**
	 * @return the innerIfReachCondTag
	 */
	final boolean isInnerIfReachCondTag() {
		return innerIfReachCondTag;
	}

	/**
	 * @param innerIfReachCondTag the innerIfReachCondTag to set
	 */
	final void setInnerIfReachCondTag(boolean innerIfReachCondTag) {
		this.innerIfReachCondTag = innerIfReachCondTag;
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
