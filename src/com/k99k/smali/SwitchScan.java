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
	 * 是否有default语句
	 */
	private boolean hasDefault = false;
	
	@SuppressWarnings("unchecked")
	public void scan(){
		for (int i = 0; i < this.len; i++) {
			String end = null;
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch") && s.getState() == Sentence.STATE_DOING) {
				SwitchSentence ss = (SwitchSentence)s;
				String key = ss.getKey();
				if (key.equals("packed-switch") || key.equals("sparse-switch")) {
					//定位到switch的起始句,结束位置
					String tag = ss.getDataTag();
					ArrayList<String> cases = (ArrayList<String>) this.mgr.getVar(tag).getValue();
					if (cases == null || cases.size() <= 0) {
//						Logger.getLogger(name)
						log.error(this.mgr.getMeth().getName()+" - switch cases not found:"+ss.getLine());
						continue;
					}
					Sentence se = null;
					//如果i+1并不是goto或gotoTag语句，则肯定有default
					hasDefault = false;
					int insertPo = i+1;
					for (int j = insertPo; j < this.senList.size(); j++) {
						se = this.senList.get(j);
						if (se.getName().equals("gotoTag")) {
							end = se.getLine();
							break;
						}else if(se.getName().equals("goto")){
							GotoSentence gt = (GotoSentence)se;
							end = gt.getTarget();
							break;
						}else if(se.getName().equals("return")){
							//log.error(this.mgr.getMeth().getName()+" - switch end not found:"+se.getLine());
							break;
						}else if(se.getName().equals("tag")){
							continue;
						}
						else{
							hasDefault = true;
							continue;
						}
					}
					se.appendOut(StaticUtil.NEWLINE+StaticUtil.TABS[se.level]+"} //end of switch");
					se.over();
					//加入位置
					int startIndex = insertPo;
					
					//找到对应的case集合,准备移动
					ArrayList<Sentence> caseLs = this.findCases(cases, startIndex, end);
					
					this.senList.addAll(startIndex,caseLs);
					s.over();
				}
				
			}
		}
		
	}
	
	/**
	 * 取出所有的case块的集合
	 * @param caseData
	 * @param startIndex
	 * @param endGoto
	 * @return
	 */
	private ArrayList<Sentence> findCases(ArrayList<String> caseData,int startIndex,String endGoto){
		//cases用于防止被方法内的其他switch干扰
		HashMap<String,String> cases = new HashMap<String, String>();
		for (int i = 0; i < caseData.size(); i++) {
			String[] ss = caseData.get(i).split(",");
			if (!cases.containsKey(ss[0])) {
				cases.put(ss[0], ss[1]);
			}
		}
		
//		Collections.sort(caseData, comp); //按实际出现的次序
		//caseLs:所有case语句块集合
		HashMap<String,ArrayList<Sentence>> caseLs = new HashMap<String, ArrayList<Sentence>>();
		//加入default
		if (this.hasDefault) {
			Sentence fs = null;
			ArrayList<Sentence> dls = new ArrayList<Sentence>();
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
					if (fs == null) {
						fs = s;
					}
					if (s.getName().equals("switch") && (s.getLine().startsWith(":pswitch_") || s.getLine().startsWith(":sswitch_"))) {
						//default块前方引用的:pswitch_0 或:sswitch_0
						s.over();
					}
					dls.add(s);
					this.senList.remove(i);
					i--;
				}
			}
			//添加default到第一句
			OtherSentence sws = new OtherSentence(mgr, "default:");
			sws.setOut("default:");
			sws.setType(Sentence.TYPE_STRUCT);
			sws.over();
			dls.add(0, sws);
			//fs.setOut("default:"+StaticUtil.NEWLINE+StaticUtil.TABS[fs.level]+fs.getOut());
			caseLs.put("default", dls);
			startIndex++;
		}
		//其他cases
		for (int i = startIndex; i < this.senList.size(); i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch") && cases.containsKey(s.getLine())) {
				String caseName = cases.get(s.getLine());
				//case块开始的case句
				s.setOut(caseName+":");
				s.over();
//				ls.add(s);
				this.senList.remove(i);
				//case块
				ArrayList<Sentence> cls = this.caseOne(endGoto, i,cases);
				cls.add(0, s);
				caseLs.put(caseName, cls);
				//ls.addAll(this.caseOne(endGoto, i,cases));
				//继续从原位置向后
				i--;
			}
		}
		
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		for (int i = 0; i < caseData.size(); i++) {
			String[] ss = caseData.get(i).split(",");
			if (caseLs.containsKey(ss[1])) {
				ls.addAll(caseLs.get(ss[1]));
			}else{
				//未找到的case可能是其他case重复的内容,从cases中找到原来的
				if (cases.containsKey(ss[0])) {
					String cName = cases.get(ss[0]);
					ArrayList<Sentence> cls = caseLs.get(cName);
					OtherSentence sws = new OtherSentence(mgr, ss[0]);
					sws.setOut(ss[1]+":");
					sws.setType(Sentence.TYPE_STRUCT);
					sws.over();
					ls.add(sws);
					for (int j = 1; j < cls.size(); j++) {
						ls.add(cls.get(j));
					}
				}else{
					log.error(this.mgr.getMeth().getName()+" case not found:"+ss[1]+","+ss[0]);
				}
			}
		}
		if (hasDefault) {
			ls.addAll(caseLs.get("default"));
		}
		return ls;
	}
	
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
					gt.setOut("break;");
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
	
	
	private final CompareCase comp = new CompareCase();
	/**
	 * case比较器
	 * @author keel
	 *
	 */
	class CompareCase implements Comparator<String>{

		@Override
		public int compare(String s1, String s2) {
			String n1 = s1.split(",")[2];//s1.substring(s1.indexOf("_")+1,s1.indexOf(","));
			String n2 = s2.split(",")[2];//s2.substring(s2.indexOf("_")+1,s2.indexOf(","));
			if (StringUtil.isDigits(n1) && StringUtil.isDigits(n2)) {
				return Integer.parseInt(n1)-Integer.parseInt(n2);
			}
			return 0;
		}
		
	}
}
