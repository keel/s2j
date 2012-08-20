/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

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
	
	private SentenceMgr mgr;
	
	private ArrayList<Sentence> senList;
	
	private int len;
	
	@SuppressWarnings("unchecked")
	public void scan(){
		for (int i = 0; i < this.len; i++) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("switch")) {
				SwitchSentence ss = (SwitchSentence)s;
				String key = ss.getKey();
				if (key.equals("packed-switch") || key.equals("sparse-switch")) {
					//定位到switch的起始句,结束位置
					String tag = ss.getDataTag();
					ArrayList<String> cases = (ArrayList<String>) this.mgr.getVar(tag).getValue();
					if (cases == null) {
						System.err.println("switch cases not found:"+ss.getLine());
						continue;
					}
					Sentence se = this.senList.get(i+1);
					String end = null;
					if (!se.getLine().startsWith(":goto_")) {
						System.err.println("switch end not found:"+se.getLine());
						continue;
					}
					end = se.getLine();
					se.setOut("} //end of switch");
					se.over();
					//加入位置
					int addIndex = i+1;
					
					//找到对应的case集合,准备移动
					ArrayList<Sentence> caseLs = this.findCases(cases, addIndex+1, end);
					
					this.senList.addAll(addIndex,caseLs);
					
				}
				
			}
		}
		
		
	}
	
	private ArrayList<Sentence> findCases(ArrayList<String> caseData,int startIndex,String endGoto){
		HashMap<String,String> cases = new HashMap<String, String>();
		for (int i = 0; i < caseData.size(); i++) {
			String[] ss = caseData.get(i).split(",");
			cases.put(ss[0], ss[1]);
		}
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		for (int i = 0; i < caseData.size(); i++) {
			String[] ss = caseData.get(i).split(",");
			String cond = ss[0];
			for (int j = startIndex; j < this.senList.size(); ) {
				Sentence s = this.senList.get(j);
				if (s.getLine().equals(cond)) {
					//case 句
					s.setOut(ss[1]+":");
					s.over();
					ls.add(s);
					this.senList.remove(j);
					//case块
					ls.addAll(this.caseOne(endGoto, j,cases));
				}else{
					j++;
				}
			}
		}
		
		return ls;
	}
	
	private ArrayList<Sentence> caseOne(String endGoto,int startIndex,HashMap<String,String> cases){
		ArrayList<Sentence> ls = new ArrayList<Sentence>();
		for (int i = startIndex; i < this.senList.size();) {
			Sentence s = this.senList.get(i);
			if (s.getName().equals("goto")) {
				GotoSentence gt = (GotoSentence)s;
				if (gt.getTarget().equals(endGoto)) {
					gt.setOut("break;");
					gt.over();
					ls.add(gt);
					this.senList.remove(i);
					break;
				}
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
	
	
}
