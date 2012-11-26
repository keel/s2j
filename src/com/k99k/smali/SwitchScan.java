/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class SwitchScan {

	public SwitchScan(SentenceMgr mgr,ArrayList<Sentence> senList) {
		this.mgr = mgr;
		this.senList = senList;
		this.len = this.senList.size();
	}
	
	static final Logger log = Logger.getLogger(SwitchScan.class);
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
	private int len;
	
	/**
	 * switch块结束的gotoTag
	 */
	private String switchEnd;
	
//	/**
//	 * 是否有default语句
//	 */
//	private boolean hasDefault = false;
	
	@SuppressWarnings("unchecked")
	public void scan(){
		for (int i = 0; i < this.len; i++) {
//			String endTagName = null;
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch") && s.getState() == Sentence.STATE_DOING) {
				SwitchSentence ss = (SwitchSentence)s;
				String key = ss.getKey();
				if (key.equals("packed-switch") || key.equals("sparse-switch")) {
					//定位到switch的起始句,结束位置
					String tag = ss.getDataTag();
					HashMap<String,String> cases = (HashMap<String,String>) this.mgr.getVar(tag).getValue();
					if (cases == null || cases.size() <= 0) {
//						Logger.getLogger(name)
						log.error(this.mgr.getMeth().getName()+" - switch cases not found:"+ss.getLine());
						continue;
					}
					Sentence se = null;
					//如果i+1并不是goto或gotoTag语句，则肯定有default
					boolean hasDefault = false;
					int insertPo = i+1;
					for (int j = insertPo; j < this.senList.size(); j++) {
						se = this.senList.get(j);
						if (se.getName().equals("gotoTag")) {
//							endTagName = se.getLine();
							this.switchEnd =  se.getLine();
							break;
						}else if(se.getName().equals("goto")){
							GotoSentence gt = (GotoSentence)se;
//							endTagName = gt.getTarget();
							this.switchEnd =gt.getTarget();
							break;
						}else if(se.getName().equals("return")){
							log.error(this.mgr.getMeth().getName()+" - switch end not found:"+se.getLine());
							this.switchEnd = se.getLine();
							break;
						}else if(se.getType() == Sentence.TYPE_STRUCT){
							continue;
						}
//						else if(se.getName().equals("switch")){
//							//已经到达case块了
//							continue;
//						}
						else{
							hasDefault = true;
							continue;
						}
					}
//					se.appendOut(StaticUtil.NEWLINE+StaticUtil.TABS[se.level]+"} //end of switch");
//					se.over();
					
					//加入位置
					int startIndex = insertPo;
					
					//找到对应的case集合,准备移动
					ArrayList<Sentence> caseLs = this.findCases(cases, startIndex, this.switchEnd,hasDefault);
					OtherSentence endSwitch = new OtherSentence(mgr, "//end of switch");
					endSwitch.setOut("} //end of switch");
					endSwitch.setLevel(se.getLevel());
					endSwitch.setType(Sentence.TYPE_STRUCT);
					endSwitch.over();
					caseLs.add(endSwitch);
					this.senList.addAll(startIndex,caseLs);
					
					s.over();
				}
				
			}
		}
		
	}
	
	/**
	 * 取出所有的case块的集合
	 * @param cases
	 * @param startIndex
	 * @param endGoto
	 * @return
	 */
	private ArrayList<Sentence> findCases(HashMap<String,String> cases,int startIndex,String endGoto,boolean hasDefault){
		//加入default
		ArrayList<Sentence> dls = new ArrayList<Sentence>();
		if (hasDefault) {
			//添加default到第一句
			OtherSentence sws = new OtherSentence(mgr, "default:");
			sws.setOut("default:");
			sws.setType(Sentence.TYPE_STRUCT);
			sws.over();
			//dls为default case语句集
			for (int i = startIndex; i < this.senList.size(); i++) {
				Sentence s = this.senList.get(i);
				if (s.getName().equals("gotoTag") || s.getName().equals("goto")) {
					break;
				}else if(s.getName().equals("return")){
					//FIXME 此外会造成多一个return
					dls.add(s);
//					this.senList.remove(i);
					break;
				}else{
					if (s.getName().equals("switch") && (s.getLine().startsWith(":pswitch_") || s.getLine().startsWith(":sswitch_"))) {
						//default块前方引用的:pswitch_0 或:sswitch_0
						if (cases.containsKey(s.getLine())) {
							//这里不再是default，而是case
							s.setOut(cases.get(s.getLine()));
//							sws.setOut(cases.get(s.getLine()));
							//直接从cases中去掉不再处理
							cases.remove(s.getLine());
						}
						s.over();
					}
					dls.add(s);
					this.senList.remove(i);
					i--;
				}
			}
			//添加default到第一句
			dls.add(0, sws);
//			startIndex++;
		}
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		
		//其他cases
		for (int i = startIndex; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch") && cases.containsKey(s.getLine())) {
				String caseName = cases.get(s.getLine());
				//case块开始的case句
				s.setOut(caseName);
				s.over();
				//case块
				this.senList.remove(i);
				ArrayList<Sentence> cls = this.caseOne(endGoto, i,cases);
				cls.add(0, s);
				//向上包含tag和gotoTag语句
//				if (!hasReturnCase) {
					for (int j = i-1; j > 0;j-- ) {
						Sentence pres = this.senList.get(j);
						if (pres.getName().equals("tag") || pres.getName().equals("gotoTag")) {
							if (pres.getLine().equals(this.switchEnd)) {
								//这是switch块之后的语句,放弃处理,原取出的块放回原位,cls变成空的
								s.setOut("//"+s.getLine());
								this.senList.addAll(i,cls);
								i = i+cls.size();
								cls = new ArrayList<Sentence>();
								break;
							}else{
								cls.add(0,pres);
								this.senList.remove(j);
								i--;
							}
						}else{
							break;
						}
					}
//				}
				
				ls.addAll(cls);
				//继续从原位置向后
				i--;
			}
		}
		if (hasDefault) {
			ls.addAll(dls);
		}
		return ls;
	}
	
	/**
	 * 
	 */
	private boolean hasReturnCase = false;
	
	/**
	 * 取出某一个case块的语句
	 * @param endGoto
	 * @param startIndex
	 * @param cases
	 * @return
	 */
	private ArrayList<Sentence> caseOne(String endGoto,int startIndex,HashMap<String,String> cases){
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		
		for (int i = startIndex; i < this.senList.size();) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("goto")) {
				GotoSentence gt = (GotoSentence)s;
				if (gt.getTarget().equals(endGoto)) {
					//FIXME 这里暂时用out中是否包含return 判断，未判明return是否是有效java语句
					boolean hasReturn = false;
					if (this.senList.get(i-1).getOut().indexOf("return ")>-1) {
						hasReturn = true;
					}
					if (hasReturn) {
						gt.setOut("//break;");
					}else{
						gt.setOut("break;");
					}
					//此处将goto彻底变为switch语句,不再if处理中受影响
					gt.setSwitch();
					gt.over();
					ls.add(gt);
					this.senList.remove(i);
					break;
				}else{
					//FIXME 应该是return或while内的continue,break，仍需要跳出
					gt.setOut("goto somewhere; //maybe return,continue,break: "+gt.getLine());
					gt.over();
					ls.add(gt);
					this.senList.remove(i);
					break;
				}
			}else if(s.getName().equals("return")){
				//将return的注释去掉
				if (s.getOut().startsWith("//")) {
					s.setOut(s.getOut().substring(2));
				}
				ls.add(s);
				this.senList.remove(i);
				hasReturnCase = true;
				break;
			}else if(s.getName().equals("switch")){
				String t = s.getLine();
				if (cases.containsKey(t)) {
					break;
				}
			}
			
			ls.add(this.senList.remove(i));
		}
		
				
		return ls;
	}
	
	
//	private final CompareCase comp = new CompareCase();
//	/**
//	 * case比较器
//	 * @author keel
//	 *
//	 */
//	class CompareCase implements Comparator<String>{
//
//		@Override
//		public int compare(String s1, String s2) {
//			String n1 = s1.split(",")[2];//s1.substring(s1.indexOf("_")+1,s1.indexOf(","));
//			String n2 = s2.split(",")[2];//s2.substring(s2.indexOf("_")+1,s2.indexOf(","));
//			if (StringUtil.isDigits(n1) && StringUtil.isDigits(n2)) {
//				return Integer.parseInt(n1)-Integer.parseInt(n2);
//			}
//			return 0;
//		}
//		
//	}
}
