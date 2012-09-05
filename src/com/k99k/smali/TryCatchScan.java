/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author keel
 *
 */
public class TryCatchScan {
	public TryCatchScan(SentenceMgr mgr,ArrayList<Sentence> senList) {
		this.mgr = mgr;
		this.senList = senList;
//		this.len = this.senList.size();
	}
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
//	private int len;
	
	static final Logger log = Logger.getLogger(TryCatchScan.class);
	/**
	 * 保存已经识别的catch tag，用于去重
	 */
	private HashMap<String,TrySentence> catchTagMap = new HashMap<String, TrySentence>();
	
	public void scan(){
		
		//去除同样tag的.catch和.catchall,留下tryStartNum最小的
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("try") && s.getState()!=Sentence.STATE_OVER) {
				TrySentence ts = (TrySentence)s;
				if (ts.getKey().equals(".catch") ) {
					String ctag = ts.getCatchTag();
					if (this.catchTagMap.containsKey(ctag)) {
						TrySentence pre = this.catchTagMap.get(ctag);
						if (ts.getTryStartNum() < pre.getTryStartNum()) {
							noShowTry(this.senList.indexOf(pre));
							pre.setOut("// alrady show catch:"+ctag);
							pre.over();
							this.catchTagMap.put(ctag, ts);
						}else{
							//已经有过的catchtag,进行上方的try start-end块的不显示处理，本句不显示
							noShowTry(i);
							ts.setOut("// alrady show catch:"+ctag);
							ts.over();
							continue;
						}
					}else{
						this.catchTagMap.put(ctag, ts);
					}
				}else if (ts.getKey().equals(".catchall")) {
					String ctag = ts.getCatchTag();
					if (i+1<this.senList.size() && (!this.senList.get(i+1).getLine().startsWith(".catch "))) {
						noShowTry(i);
						ts.setOut("// alrady show catch:"+ctag);
						ts.over();
					}
				}
				else if (ts.getKey().startsWith(":catchall_")) {
					this.doFinally(i);
				}
			}
		}
		
		/*
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("try") && s.getState()!=Sentence.STATE_OVER) {
				TrySentence ts = (TrySentence)s;
				if (ts.getKey().equals(".catch")) {
					String ctag = ts.getCatchTag();
					if (!this.catchTagMap.containsKey(ctag)) {
						this.catchTagMap.put(ctag, ts);
						//catch块处理
						
					}else{
						//已经有过的catchtag,进行上方的try start-end块的不显示处理，本句不显示
						noShowTry(i);
						ts.setOut("// alrady show catch:"+ctag);
						ts.over();
					}
				}else if (ts.getKey().equals(".catchall")) {
					String ctag = ts.getCatchTag();
					if (!this.catchTagMap.containsKey(ctag)) {
						this.catchTagMap.put(ctag, ts);
						//finally块处理
						
						//ts.over();
					}else{
						//如果下一个没有.catch，则进行不显示处理
						if (i+1<this.senList.size() && (!this.senList.get(i+1).getLine().startsWith(".catch "))) {
							noShowTry(i);
							ts.setOut("// alrady show catch:"+ctag);
						}
						ts.over();
					}
				}else if (ts.getKey().startsWith(":catchall_")) {
					this.doFinally(i);
				}else if (ts.getKey().startsWith(":catch_")) {
					
					//this.doCatch(i);
					//ts.over();
				}
				
			}
		}*/
		//移动catch块
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("try") && s.getState()!=Sentence.STATE_OVER) {
				TrySentence ts = (TrySentence)s;
				if (ts.getKey().startsWith(":catch_") && ts.getState() != Sentence.STATE_OVER) {
					this.doCatch(i,ts);
				}
			}
		}
		//移动finally块
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("try") && s.getState()!=Sentence.STATE_OVER) {
				TrySentence ts = (TrySentence)s;
				if (ts.getKey().startsWith(":catchall_") && ts.getState() != Sentence.STATE_OVER) {
					this.shiftFinally(i,ts);
				}
			}
		}
		log.debug("tryCatchScan end");
	}
	
	/**
	 * 移动并处理finally块
	 * @param start
	 * @param ts
	 */
	private void shiftFinally(int start,TrySentence ts){
		//先取出finally块,将将javaLineNum记入HashMap
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		HashMap<Integer,String> fJavaNum = new HashMap<Integer, String>(); 
		ls.add(this.senList.remove(start));
		for (int i = start; i <this.senList.size(); ) {
			Sentence s = this.senList.get(i);
			ls.add(this.senList.remove(i));
			if(s.getLine().startsWith("throw")){
				break;
			}else{
				s.level++;
				if (s.getJavaLineNum() > 0) {
					fJavaNum.put(s.getJavaLineNum(), s.getLine());
				}
			}
		}
		//清理整个方法体中与取批javaLineNum相同的语句,同时将已经over的.catchall处理不显示
//		boolean isFirst = true;
		int insertPo = 0;
		for (int i = 0; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (fJavaNum.containsKey(s.getJavaLineNum())) {
				s.setType(Sentence.TYPE_NOT_LINE);
				s.over();
			}
			else if (s.getLine().startsWith(".catchall ")) {
				TrySentence trs = (TrySentence)s;
				if (trs.state != Sentence.STATE_OVER) {
					if (trs.getCatchTag().equals(ts.getCatchTag())) {
						insertPo = i;
//						isFirst = false;
						s.over();
					}
				}else if(trs.getCatchTag().equals(ts.getCatchTag())){
					s.setType(Sentence.TYPE_NOT_LINE);
					s.over();
				}
				
			}
		}
		
		//FIXME 需要确定finally的插入位置
		
		//从insertPo再向下找到第一个gotoTag位置，插入
		for (int i = insertPo; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("gotoTag")) {
				this.senList.addAll(i,ls);
				break;
			}
		}
		ts.over();
	}
	
	/**
	 * 初步处理finally块
	 * @param start
	 */
	private void doFinally(int start){
		Sentence s = this.senList.get(start);
		s.setOut("} finally {");
		//s.over();
		for (int i = start+1; i <this.senList.size(); i++) {
			s = this.senList.get(i);
			if(s.getLine().startsWith("throw")){
				s.setOut("} //end of finally");
				s.over();
				return;
			}
		}
	}
	
	/**
	 * 处理catch块的移动
	 * @param start
	 * @param ts
	 */
	private void doCatch(int start,TrySentence ts){
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		String vs = "_E_";
		for (int j = start+1; j < this.senList.size(); j++) {
			Sentence s1 = this.senList.get(j);
			if (s1.getName().equals("local") || s1.getLine().startsWith(".restart")) {
				//找到Exception的变量名
				vs = s1.getVar().getOut();
				this.senList.remove(j);
				s1.level++;
				ls.add(s1);
				j--;
			}else if (s1.getName().equals("goto")) {
				//goto语句为catch块结束标志
				GotoSentence gt = (GotoSentence)s1;
				TrySentence s3 = null;
				int addP = -1;
				for (int i = 0; i < this.senList.size(); i++) {
					Sentence s2 = this.senList.get(i);
					if (s2.getState() == Sentence.STATE_DOING) {
						if (s2.getName().equals("try")) {
							s3 = (TrySentence)s2;
							if (s3.getKey().equals(".catch") && s3.getCatchTag().equals(ts.getCatchTag())) {
								addP = i;
								continue;
							}
						}else if(s2.getName().equals("gotoTag")){
							//将gotoTag置为over
							GotoTagSentence gtt = (GotoTagSentence)s2;
							if (gtt.getTag().equals(gt.getTarget())) {
								gtt.over();
							}
						}
					}
				}
				if (addP == -1) {
					log.error(this.mgr.getMeth().getName()+" - .catch not found.");
					return;
				}
				s3 = (TrySentence) this.senList.get(addP);
				s3.setOut(s3.getOut().replace("_E_", vs));
				gt.setOut("} //end of catch: "+gt.getLine());
				gt.setEndOfCatch(true);
				this.senList.remove(j);
				ls.add(s1);
				j--;
				this.senList.addAll(addP+1, ls);
				gt.over();
				ts.over();
				s3.over();
				return;
//				for (int i = 0; i < this.senList.size(); i++) {
//					Sentence s2 = this.senList.get(i);
//					if (s2.getName().equals("try")) {
//						TrySentence s3 = (TrySentence)s2;
//						//定位到catchTag位置进行插入操作
//						if (s3.getKey().equals(".catch")&& s3.getState()!=Sentence.STATE_OVER && s3.getCatchTag().equals(ts.getCatchTag())) {
//							// 比较{:try_start_1 .. :try_end_1}中间的数值大小，取最小的
//							
//							s3.setOut(s3.getOut().replace("_E_", vs));
//							gt.setOut("} //end of catch: "+gt.getLine());
//							this.senList.remove(j);
//							ls.add(s1);
//							j--;
//							this.senList.addAll(i+1, ls);
//							gt.over();
//							ts.over();
//							s3.over();
//							//return; //还需要进一步将gotoTag置为over
//							continue;
//						}
//					}else if(s2.getName().equals("gotoTag")){
//						//将gotoTag置为over
//						GotoTagSentence gtt = (GotoTagSentence)s2;
//						if (gtt.getTag().equals(gt.getTarget())) {
//							gtt.over();
//							return;
//						}
//					}
//				}
//				log.error(this.mgr.getMeth().getName()+" - catch end error:"+ts.getLine());
//				return;
			}else{
				this.senList.remove(j);
				s1.level++;
				ls.add(s1);
				j--;
			}
		}
	}
	
	
	/**
	 * 向上查找第一个:try_start和:try_end块，不显示try
	 * @param start
	 */
	private void noShowTry(int start){
		for (int i = start; i >=0; i--) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("try")) {
				TrySentence ts = (TrySentence)s;
				if (ts.getKey().startsWith(":try_start_")) {
					ts.setOut("//"+ts.getLine());
					ts.over();
					return;
				}else if(ts.getKey().startsWith(":try_end_")){
					ts.setOut("//"+ts.getLine());
					ts.over();
				}
			}
		}
	}
	

}
