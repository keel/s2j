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
	/**
	 * 每个if内容块内结束标记map,用于判断是否为else
	 */
	private HashMap<Integer,IfSentence> ifEndMap = new HashMap<Integer, IfSentence>();
	
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
		
		try {
			this.setIfContendEnd();
			
			this.shiftIfBlock();
//		this.setLevels();
			
			this.fixIfBlock(0,this.senList.size()-1);
			
			
//		this.scanReversedIf();
//		
//		this.scanWhileStruct();
//		
//		this.scanIf();
//		
		} catch (Exception e) {
			e.printStackTrace();
			log.error(this.mgr.getMeth().getName()+" ERR:"+e.getMessage());
		}
		
		
		log.debug("IfScan end");
	}
	
	private void setLevels(){
		//flag为0表示无操作,大于0表示增加的level,小于0表示减少的level
		int flag = 0;
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			s.levelUpdate(flag);
			if (s.getName().equals("if")) {
				flag++;
			}else if(s.getName().equals("tag")){
				TagSentence tag = (TagSentence)s;
				if (tag.getEndTimes()>0) {
					flag = flag - tag.getEndTimes();
				}
			}
		}
	}
	
	
	private void fixIfBlock(int startIndex,int endIndex){
		for (int i = startIndex; i < endIndex; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if")) {
				IfSentence ifs = (IfSentence)s;
				TagSentence tag = ifs.getCondTag();
				int tagIndex = this.senList.indexOf(tag);
				// 正常顺序
				if (tagIndex > i && tagIndex <= endIndex) {
					//扫描中间的goto语句
					for (int j = 0; j < tagIndex; j++) {
						Sentence s1 = this.senList.get(j);
//						if (s1.getName().equals("if")) {
//							this.fixIfBlock(j+1,tagIndex);
//						}
//						else 
							if (s1.getName().equals("goto") && s1.getState() == Sentence.STATE_DOING) {
							GotoSentence gt = (GotoSentence)s1;
							if (gt.isReturn()) {
								gt.setOut("return");
								gt.over();
							}else{
								int gtTagIndex = this.senList.indexOf(gt.getTargetSen());
								if (gtTagIndex < j) {
									//倒指的goto,一般为continue
									gt.setOut("continue; //"+gt.getLine()+" -> "+this.senList.get(gtTagIndex+1).getOut());
									gt.over();
									continue;
								}
								//先找到goto句后的非tag句
								int afterGotoContent = j+1;
								for (int k = j+1; k < this.senList.size(); k++) {
									Sentence s2 = this.senList.get(k);
									if (s2.getName().equals("tag")) {
										continue;
									}else if(s2.getName().equals("gotoTag")){
										if(((GotoTagSentence)s2).getTag().equals(gt.getTargetSen().getTag())){
											//goto后面跟着gtTag,直接over
											gt.over();
											break;
										}
									}
									else{
										afterGotoContent = k;
										break;
									}
								}
								if (gt.getState() == Sentence.STATE_DOING) {
									if (gtTagIndex > tagIndex) {
										//这里实际上未比较外部while的结束句
										gt.setOut("break; //"+gt.getLine()+" -> "+this.senList.get(gtTagIndex).getOut());
									}else {
										
									}
								}
							}
						}
					}
				}else if(tagIndex < i){
					//倒置的while
					
					
				}else if(tagIndex > i && tagIndex > endIndex){
					log.error(this.mgr.getMeth().getName()+" tagIndex > i && tagIndex > endIndex. fixIfBlock("+startIndex+","+endIndex+");tagIndex:"+tagIndex+";i:"+i);
				}
			}
		}
	}
	
	
	private void shiftIfBlock(){
		//用于保存ifs之前的gotoTag
//		GotoTagSentence preGotoTag = null;
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
//			if (s.getName().equals("gotoTag")) {
//				preGotoTag = (GotoTagSentence)s;
//				continue;
//			}else
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				IfSentence ifs = (IfSentence)s;
				int[] re = this.defineIfBlock(i);
				//定位ifs之后的gotoTag
				GotoTagSentence afterGotoTag = null;
				for (int j = i+1; j < this.senList.size(); j++) {
					Sentence s2 = this.senList.get(j);
					if (s2.getName().equals("gotoTag")) {
						afterGotoTag = (GotoTagSentence) s2;
						break;
					}else if(s2.getName().equals("return")){
						//return 语句时停止
						break;
					}
				}
				TagSentence tag = ((IfSentence)(this.senList.get(re[1]))).getCondTag();
				int tagIndex = this.senList.indexOf(tag);
				//tag在return之后时需要移动
				if (tagIndex > this.senList.indexOf(this.returnSentence)) {
//					if (tag.getLineNum() > ifs.getLineNum()) {
//						//倒置cond的if,为while语句 //应该是倒置的cond后面的if为while
//						ifs.setWhile();
//					}
					int start = this.senList.indexOf(tag);
					//取出需要移动的块
					ArrayList<Sentence> ls = new ArrayList<Sentence>();
					GotoTagSentence gotoTag = null;
					GotoSentence gts = null;
					int gotoTagIndex = -1;
					for (int j = start; j <this.senList.size(); ) {
						Sentence s1 = this.senList.get(j);
						ls.add(this.senList.remove(j));
						if (s1.getName().equals("goto")) {
							gts = (GotoSentence)s1;
							//goto语句为止
							gotoTag = (GotoTagSentence) gts.getTargetSen();
							gotoTagIndex = this.senList.indexOf(gotoTag);
							if (gotoTagIndex < 0) {
								//gotoTag在被移动的块中时继续向下找
								j++;
								continue;
							}else{
								break;
							}
						}
//							//不可能为return
//							else if(s1.getName().equals("return")){
//								break;
//							}
					}
					//确定插入的位置 ----------------------
					int po = -1;
					//是否需要最后移动（跳过gotoTag之前的tag等）
					boolean lastMove = true;
					//一直到最后句未找到goto语句时,一般为if的最后一个else语句
					if (gotoTag == null) {
						if (ifs.isWhile()) {
							po = re[0]+1;
						}else{
							po = this.senList.indexOf(afterGotoTag);
							gotoTag = afterGotoTag;
						}
					}
					//gotoTag为倒置时插入到if块之后,此时判断是while语句,不再进行最终move
					else if (gotoTagIndex>=0 && gotoTagIndex < i) {
						ifs.setWhile();
						lastMove = false;
						if (gotoTag.getInsertPoSen() == null) {
							po = re[0]+1;
						}else{
							//如果gotoTag所在的while已经有插入的内容块了,则插入点在其之后
							int wEnd = this.senList.indexOf(gotoTag.getInsertPoSen());
							if (gts.getLineNum() < gotoTag.getInsertPoSen().getLineNum()) {
								po = wEnd;
							}else{
								//goto的原始位置在WhileContentEnd之前时,从re[0]向下找到插入位置
								po = re[0]+1;
								for (int j = po; j < wEnd; j++) {
									if (gts.getLineNum() < this.senList.get(j).getLineNum()) {
										po = j+1;
										continue;
									}else{
										break;
									}
								}
							}
						}
					}
					//正常顺序时
					else{
						//gotoTagIndex为-1,应该与gotoTag==null同样，此情况应该不存在
						if (gotoTagIndex == -1) {
							log.error(this.mgr.getMeth().getName()+" gotoTagIndex is -1!");
						}
						//ifs后面没有afterGotoTag,取return的位置
						if(afterGotoTag == null){
							po = this.senList.indexOf(this.returnSentence);
						}else{
							//正常if语句插入位置在ifs下方的第一个gotoTag处  //TODO 为什么不直接是gotoTag
							po = this.senList.indexOf(afterGotoTag);
						}
					}
					//最后移动插入位置（跳过gotoTag之前的tag等）
					if (lastMove) {
						//先跳过tag等
						for (int j = po - 1; j >= 0; j--) {
							Sentence s3 = this.senList.get(j);
							String sn = s3.getName();
							//向上跳过tag和catch部分
							if (sn.equals("gotoTag") || sn.equals("try")) {
								po--;
							} else if(sn.equals("tag")){
//								//TODO 特定的跳出,待验证
//								if (((TagSentence)s3).isReverseWhileStart()) {
//									break;
//								}else{
									
									po--;
//								}
							}else{
								break;
							}
						}
						//前面有过插入的块时
						if (gotoTag != null && gotoTag.getInsertPoSen()!= null) {
							int wEnd = this.senList.indexOf(gotoTag.getInsertPoSen());
							if (gts.getLineNum() < gotoTag.getInsertPoSen().getLineNum()) {
								po = wEnd;
							}else{
								//向后找到插入位置
								int curPo = po;
								for (int j = wEnd+1; j < curPo; j++) {
									if (gts.getLineNum() > this.senList.get(j).getLineNum()) {
										po = j+1;
										continue;
									}else{
										break;
									}
								}
							}
						}
						
					}
					//保存本次移动的po位置sen
					if (gotoTag == null) {
						System.out.println("-------??????--------"+this.mgr.getMeth().getName());
					}
					if (ifs.isWhile()) {
						//while语句将移动块的最后一句保存为insertPoSen
						gotoTag.setInserPoSen(ls.get(ls.size()-1));
						//将开头的tag放到最后
						ls.add(ls.remove(0));
					}else{
						//if语句将移动块的第一句保存为insertPoSen
						gotoTag.setInserPoSen(ls.get(0));
					}
					//插入取出的块 -----------------
					if (po > -1) {
						this.senList.addAll(po, ls);
					}else{
						log.error(this.mgr.getMeth().getName()+" - insert po not found: "+ifs.getOut());
					}
					//合并条件并处理结构--------------------------
					if (ifs.isWhile()) {
						ifs = this.mergeConds(i, re[1]+1,this.senList.get(re[0]).getLineNum()+1);
						ifs.setWhile();
						
					}else{
						ifs = this.mergeConds(i, re[1]+1,this.senList.get(re[0]).getLineNum()+1);
						
						//处理break,continue等
						if (ifs.isToReturn()) {
							ifs.getCondTag().setOut(new StringBuilder("return;").append(StaticUtil.NEWLINE).append(StaticUtil.TABS[ifs.getCondTag().level]).append(ifs.getCondTag().getOut()).toString());
						}else{


						}
					}
				}else{
					//正常if
					ifs = this.mergeConds(i, re[1]+1,this.senList.get(re[0]).getLineNum()+1);
				}
				//处理else等结构
				if (!ifs.isWhile()) {
					if (this.ifEndMap.containsKey(ifs.getEndSenLineNum())) {
						ifs.setElse(true);
					}else{
						this.ifEndMap.put(ifs.getEndSenLineNum(), ifs);
					}
					
					
				}
			}
		}
	}
	
	
	
	
	/**
	 * 在移动语句之前扫描所有if的内容块结束位置
	 */
	private void setIfContendEnd(){
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				IfSentence ifs = (IfSentence)s;
				int[] re = this.defineIfBlock(i);
				int endLN = this.findIfEndLineNum(ifs, re[1]+1);
				//把这个if块内所有的if的EndSenLineNum都指向endLN
				for (int j = i; j <= re[1]; j++) {
					Sentence s1 = this.senList.get(j);
					if (s1.getName().equals("if")) {
						((IfSentence)s1).setEndSenLineNum(endLN);
					}
				}
				//单独处理倒置cond的if
				if (ifs.getCondTag().getLineNum() < ifs.getLineNum()) {
					Sentence afterCondSen = this.senList.get(this.senList.indexOf(ifs.getCondTag())+1);
					if (afterCondSen.getName().equals("if")) {
						((IfSentence)afterCondSen).setWhile();
					}else{
						//TODO 倒置if的cond后面不是if
						log.info(this.mgr.getMeth().getName()+" reverse if's cond ,but after it is not if sen."+ifs.getOut());
//						((TagSentence)afterCondSen).setReverseWhileStart(true);
					}
				}
				i = re[0];
			}
		}
	}
	
	/**
	 * 合并多条件,合并结束后进行over处理
	 * @param startIndex 条件正向开始语句(包含)
	 * @param endIndex 条件正向结束语句(不包含)
	 * @return 合并后的IfSentence
	 */
	private IfSentence mergeConds(int startIndex,int endIndex,int contentLn){
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
							if (tag.getLineNum()>contentLn) {
								ifs.reverseCompare();
							}
							continue;
						}
						//需要合并
						boolean isAnd = true;
						//如果外部if对应的tag指向内容块之前
						if(tag.getLineNum() < contentLn){
							isAnd = true;
							if (tag.getLineNum()<ifs2.getLineNum()) {
								isAnd = false;
							}else{
								if (tag2.getLineNum() > contentLn) {
									ifs2.reverseCompare();
									isAnd = false;
								}else if(tag2.getLineNum() < contentLn){
									isAnd = true;
								}
							}
						}
						//外部if正指向内容块后
						else if(tag.getLineNum() >  contentLn){
							if (tag.getLineNum()<ifs2.getLineNum()) {
								isAnd = false;
							}else{
								if (tag2.getLineNum() > contentLn) {
									ifs2.reverseCompare();
									isAnd = true;
								}else if(tag2.getLineNum() < contentLn){
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
			ifs.getCondTag().setEndStruct().over();
		}
		return ifs;
	}

	private void scanIf(){
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("if") && s.getState() == Sentence.STATE_DOING) {
				//定位条件块
				int[] re = this.defineIfBlock(i);
				IfSentence ifs  = (IfSentence) this.senList.get(re[1]);
				TagSentence tag = ifs.getCondTag();
				int tagIndex = this.senList.indexOf(tag);
				this.contStartIndex = tagIndex-1;
				this.contEndIndex = re[0]+1;
				this.contStart = this.senList.get(this.contStartIndex).getLineNum();
				this.contEnd = this.senList.get(this.contEndIndex).getLineNum();
				
				
				//判断是否是到return的if
				this.setIfToRetrun(contStartIndex, ifs);
				//tag是否在return后
				int returnIndex = this.senList.indexOf(this.returnSentence);
				if (tagIndex>returnIndex && (!tag.isShift())) {
					//可能有else块,定位ifs后的gotoTag
//					GotoTagSentence gts = null;
//					int po = 0;
					int gtTagIndex = 0;
					String elseGtTag = null;
					for (int j = i+1; j < this.senList.size(); j++) {
						Sentence s1 = this.senList.get(j);
						//这里无法判断gotoTag的state，有可能state已经over
						if (s1.getName().equals("gotoTag")) {
							elseGtTag = ((GotoTagSentence)s1).getTag();
							gtTagIndex = j;
							break;
						}
					}
					if (gtTagIndex == 0 || elseGtTag == null) {
						log.error(this.mgr.getMeth().getName()+" - if's tag after returnLineNum,but cannot find gotoTag after if:"+ifs.getLine());
						continue;
					}
					//移动else块
					this.shiftElse(this.contStartIndex+1, tag,elseGtTag,gtTagIndex);
					ifs = this.mergeConds(i, this.contEndIndex);
				}else if(tag.isShift()){
					ifs = this.mergeConds(i, this.contEndIndex);
					ifs.setElse(true);
//					ifs.reverseCompare();
//					tag.setEndStruct();
					tag.over();
				}else{
					ifs = this.mergeConds(i, this.contEndIndex);
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
	
//	private ArrayList<GotoTagSentence> elseEntrys = new ArrayList<GotoTagSentence>();
	
	
	/**
	 * 移动else块，返回移动后的处理位置
	 * @param startTagIndex else块开始的tag的index
	 * @param ts else块开始的tag
	 * @param gtTagName gotoTag的tagName
	 * @param gtTagIndex gotoTag的index
	 */
	private void shiftElse(int startTagIndex,TagSentence ts,String gtTagName,int gtTagIndex){
		//定位到goto语句，并移动语句块
		int elseEnd = startTagIndex;
		GotoSentence gt = null;
		for (int i = startTagIndex; i < this.senList.size();i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("goto") && s.getState() == Sentence.STATE_DOING) {
				gt = (GotoSentence)s;
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
				}
				elseEnd = i;
				gt.over();
				gtTag.over();
				break;
			}
		}
		//找不到goto语句时,说明已被其他else语句移动，将最末句作为elseEnd，同时需要补一个if的结束句
		boolean fixEnd = false;
		if (gt == null || elseEnd == startTagIndex) {
			elseEnd = this.senList.size()-1;
			fixEnd = true;
		}
		Sentence tss = this.senList.get(startTagIndex+1);
		boolean isLastElse = false;
		if (tss.getName().equals("if")) {
			//处理else if
			IfSentence ifs = (IfSentence)tss;
			ifs.setElse(true);
			//ifs.over(); //ifs实际上可能还未over
			ts.appendOut("//if's else will start");
			ts.setEndStruct();
		}else{
			//最后的else块
			ts.setOut("} else { //"+ts.getLine());
			fixEnd = true;
			isLastElse = true;
		}
		//查找插入位置
		int po = gtTagIndex;
//		if (gt != null) {
//			GotoTagSentence gts = (GotoTagSentence) gt.getTargetSen();
//			//判断是否与if下方的gotoTag一致
//			if (gts.getTag().equals(gtTagName)) {
//				po = this.senList.indexOf(gts);
//			}else{
//				po = gtTagIndex;
//			}
//		}
		
		if (isLastElse) {
			for (int j = po+1; j < this.senList.size(); j++) {
				String sn = this.senList.get(j).getName();
				if (sn.equals("tag") || sn.equals("gotoTag")) {
					po++;
				} else {
					break;
				}
			}
			po++;
		}else{
			for (int j = po - 1; j >= 0; j--) {
				String sn = this.senList.get(j).getName();
				if (sn.equals("tag") || sn.equals("gotoTag")) {
					po--;
				} else {
					break;
				}
			}
			//向上跳过catch块部分
			while (po >= 0) {
				Sentence s2 = this.senList.get(po-1);
				if (s2.getName().equals("try")) {
					po--;
				}else{
					break;
				}
			}
		}
		//移动else块
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
		this.senList.addAll(po, ls);
		ts.over();
	}
	
	

	/**
	 * 移动语句块
	 * @param startIndex
	 * @param endIndex
	 * @param po
	 * @return
	 */
	private ArrayList<Sentence> moveBlock(int startIndex,int endIndex,int po){
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		int moveCount = endIndex-startIndex+1;
		for (int i = 0; i < moveCount;i++) {
			Sentence s2 = this.senList.remove(startIndex);
			if (s2.getName().equals("tag")) {
				((TagSentence)s2).setShift(true);
			}
			ls.add(s2);
		}
		this.senList.addAll(po, ls);
		return ls;
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
						
						/* break和continue另外处理
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
								log.error(this.mgr.getMeth().getName()+" scanReversedIf error! outWhileGtagIndex == -1");
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
						*/
					}
					
				}
			}
		}
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
		int[] ifRE = this.defineIfBlock(firstIfIndex);
		lastCondIndex = ifRE[1];
		lastCond = (IfSentence) this.senList.get(lastCondIndex);
		conStart = ifRE[0] + 1;
		TagSentence EndTag = lastCond.getCondTag();
		
		//将EndTag移动到最下面一个if后面,lineNum+1
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
//			if (s2.getName().equals("tag")) {
//				((TagSentence)s2).setShift(true);
//			}
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
		this.senList.addAll(conStart, temp);
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
					int lastCondIndex = i+1;
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
								gs = gsg;break;
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
							if (s1.getName().equals("tag")) {
								((TagSentence)s1).setShift(true);
							}
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
						gtTagSen.over();
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
			for (int i = endIndex-1; i >= startIndex; i--) {
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
				boolean doMerge = true;
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
	 * @param ifs IfSentence
	 * @param start 开始扫描的index(包含)
	 * @return 内容块结束的lineNum
	 */
	private int findIfEndLineNum(IfSentence ifs,int start){
		int ln = -1;
		for (int i = start; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("return")) {
				//直接返回最后一行，即return行
				return s.getLineNum();
			}
			//真正有输出的语句
			else if(s.getName().equals("goto")){
				int n = this.senList.indexOf(((GotoSentence)s).getTargetSen());
				return this.findIfEndLineNum(ifs, n+1);
			} 
//			if (s.getType() == Sentence.TYPE_LINE || s.getName().equals("if")|| s.getName().equals("return")) {
			else if(s.getName().equals("tag") || s.getName().equals("gotoTag")){
				ln = s.getLineNum();
				continue;
			}
			else if (!isStructSen(s)) {
				ln = s.getLineNum();
				continue;
			} else{
				break;
			}
		}
		return ln;
	}
	
	
	/**
	 * 判断并设置if的toReturn为true
	 * @param contStartIndex
	 * @param ifs
	 */
	private void setIfToRetrun(int contStartIndex,IfSentence ifs){
		for (int i = contStartIndex+1; i < this.senList.size(); i++) {
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
		boolean isTagToReturnOK = true;
		for (int i = len-1; i >= 0; i--) {
			Sentence s = senList.get(i);
			if(returnScope && s.getName().equals("if")){
				((IfSentence)s).setToReturn(true);
				returnScope = false;
			}else if(returnScope && s.getName().equals("goto")){
				returnScope = false;
			}
			else if ((s.getName().equals("tag") || s.getName().equals("gotoTag"))) {
				TagSentence ts = (TagSentence)s;
				condMap.put(ts.getTag(), ts);
				if (returnScope && isTagToReturnOK) {
					ts.setReturn(true);
				}
			}else if(s.getName().equals("return")){
				this.returnLineNum = s.getLineNum();
				this.returnSentence = (ReturnSentence) s;
				returnScope = true;
			}else{
				isTagToReturnOK = false;
				continue;
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
//					((GotoTagSentence)ts).addGotoTimes();
				}else{
					log.error(this.mgr.getMeth().getName()+" - goto target not found: "+gs.getTarget());
				}
			}
		}
//		lastSenLineNum = this.senList.get(this.len-1).getLineNum();
		return true;
	}
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
