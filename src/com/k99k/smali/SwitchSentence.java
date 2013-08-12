/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class SwitchSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public SwitchSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}
	static final Logger log = Logger.getLogger(SwitchSentence.class);
	
	private ArrayList<String> switchKey;
	
	private String dataTag;
	
	private int caseNum;
	
//	private ArrayList<String> cases;
	
	private String key;
	
	@Override
	public boolean debug() {
		this.exec();
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		this.key = ws[0];
		HashMap<String,String> cases = new HashMap<String, String>();
		if (key.equals("packed-switch") || key.equals("sparse-switch")) {
			//switch
			Var v = new Var(this);
			v.setClassName("switch");
			v.setKey(key);
			//name设置成:pswitch_data_0,作为此switch的唯一标识
			v.setName(ws[2]);
			//v.setValue(ws[1]); //value用作存储case数据集用
			this.dataTag = ws[2];
			this.mgr.setVar(v);
			this.out.append("switch(");
			Var v1 = this.mgr.getVar(ws[1]);
			this.out.append(v1.getOut());
			this.out.append("){");
			//将引用的key所在的sen进行over
			Sentence s = v1.getSen();
			if (s != null && (s.getName().equals("get") || s.getName().equals("var"))) {
				v1.getSen().over();
			}
			this.state = Sentence.STATE_DOING;
		}else if(key.startsWith(":pswitch_data") || key.startsWith(":sswitch_data")){
			//data
			this.over();
		}else if(key.startsWith(":pswitch_") || key.startsWith(":sswitch_")){
			//case
			Var v = new Var(this);
			v.setClassName("case");
			v.setKey(key);
			v.setName(this.line);
			this.mgr.setVar(v);
			this.out.append("//"+this.line);
		}else if(key.equals(".packed-switch") || key.equals(".sparse-switch")){
			if (this.switchKey == null) {
				this.out.append("switchKey is null. line:").append(this.line);
				this.mgr.err(this);
				log.error(this.out);
				return false;
			}
			//计算caseNum
			if (key.startsWith(".packed-switch")) {
				//顺序型的case条件集
				int start = Integer.parseInt(StringUtil.int16to10(ws[1]));
				int slen = this.switchKey.size();
				
				
				for (int i = 0; i < slen; i++) {
					int cii = start+i;
					String caseStr = this.switchKey.get(i);
					if (cases.containsKey(caseStr)) {
						cases.put(caseStr, cases.get(caseStr)+" case "+cii+": ");
					}else{
						cases.put(caseStr, "case "+cii+": ");
					}
				}
				
				
			}else{
				//.sparse-switch ,非顺序型的case条件集
				int slen = this.switchKey.size();
				for (int i = 0; i < slen; i++) {
					String[] ss = this.switchKey.get(i).split(" -> ");
					String caseval = "case " + StringUtil.int16to10(ss[0]);
					String caseStr = ss[1];
					if (cases.containsKey(caseStr)) {
						cases.put(caseStr, cases.get(caseStr)+caseval+": ");
					}else{
						cases.put(caseStr, caseval+": ");
					}
				}
			}
			String tag = this.mgr.getLastSentence().getLine();
			Var sv = this.mgr.getVar(tag);
			//加入到原switch开始句的Var的value中
			sv.setValue(cases);
			this.type = Sentence.TYPE_NOT_LINE;
		}else{
			//this.type = Sentence.TYPE_NOT_LINE;
		}
		
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new SwitchSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "switch";
	}
	
	
	
	/**
	 * @return the dataTag
	 */
	public final String getDataTag() {
		return dataTag;
	}
	
	
	/**
	 * @return the caseNum
	 */
	public final int getCaseNum() {
		return caseNum;
	}
	
	

	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}

	public final void addSwitchKey(String key) {
		if (this.switchKey == null) {
			this.switchKey = new ArrayList<String>();
		}
		switchKey.add(key);
	}

	static final String[] KEYS = new String[]{
		"packed-switch",
		"sparse-switch",
		".packed-switch",
		".sparse-switch",
		":sswitch_data_0",
		":sswitch_data_1",
		":sswitch_data_2",
		":sswitch_data_3",
		":sswitch_data_4",
		":sswitch_data_5",
		":sswitch_data_6",
		":sswitch_data_7",
		":sswitch_data_8",
		":sswitch_data_9",
		":sswitch_data_a",
		":sswitch_data_b",
		":sswitch_data_c",
		":sswitch_data_d",
		":sswitch_data_e",
		":sswitch_data_f",
		":sswitch_data_10",
		":sswitch_data_11",
		":sswitch_data_12",
		":sswitch_data_13",
		":sswitch_data_14",
		":sswitch_data_15",
		":sswitch_data_16",
		":sswitch_data_17",
		":sswitch_data_18",
		":sswitch_data_19",
		":sswitch_data_1a",
		":sswitch_data_1b",
		":sswitch_data_1c",
		":sswitch_data_1d",
		":sswitch_data_1e",
		":sswitch_data_1f",
		":sswitch_data_20",
		":sswitch_data_21",
		":sswitch_data_22",
		":sswitch_data_23",
		":sswitch_data_24",
		":sswitch_data_25",
		":sswitch_data_26",
		":sswitch_data_27",
		":sswitch_data_28",
		":sswitch_data_29",
		":sswitch_data_2a",
		":sswitch_data_2b",
		":sswitch_data_2c",
		":sswitch_data_2d",
		":sswitch_data_2e",
		":sswitch_data_2f",
		":sswitch_data_30",
		":sswitch_data_31",
		":sswitch_data_32",
		":sswitch_data_33",
		":sswitch_data_34",
		":sswitch_data_35",
		":sswitch_data_36",
		":sswitch_data_37",
		":sswitch_data_38",
		":sswitch_data_39",
		":sswitch_data_3a",
		":sswitch_data_3b",
		":sswitch_data_3c",
		":sswitch_data_3d",
		":sswitch_data_3e",
		":sswitch_data_3f",
		":sswitch_data_40",
		":sswitch_data_41",
		":sswitch_data_42",
		":sswitch_data_43",
		":sswitch_data_44",
		":sswitch_data_45",
		":sswitch_data_46",
		":sswitch_data_47",
		":sswitch_data_48",
		":sswitch_data_49",
		":sswitch_data_4a",
		":sswitch_data_4b",
		":sswitch_data_4c",
		":sswitch_data_4d",
		":sswitch_data_4e",
		":sswitch_data_4f",
		":sswitch_data_50",
		":sswitch_0",
		":sswitch_1",
		":sswitch_2",
		":sswitch_3",
		":sswitch_4",
		":sswitch_5",
		":sswitch_6",
		":sswitch_7",
		":sswitch_8",
		":sswitch_9",
		":sswitch_a",
		":sswitch_b",
		":sswitch_c",
		":sswitch_d",
		":sswitch_e",
		":sswitch_f",
		":sswitch_10",
		":sswitch_11",
		":sswitch_12",
		":sswitch_13",
		":sswitch_14",
		":sswitch_15",
		":sswitch_16",
		":sswitch_17",
		":sswitch_18",
		":sswitch_19",
		":sswitch_1a",
		":sswitch_1b",
		":sswitch_1c",
		":sswitch_1d",
		":sswitch_1e",
		":sswitch_1f",
		":sswitch_20",
		":sswitch_21",
		":sswitch_22",
		":sswitch_23",
		":sswitch_24",
		":sswitch_25",
		":sswitch_26",
		":sswitch_27",
		":sswitch_28",
		":sswitch_29",
		":sswitch_2a",
		":sswitch_2b",
		":sswitch_2c",
		":sswitch_2d",
		":sswitch_2e",
		":sswitch_2f",
		":sswitch_30",
		":sswitch_31",
		":sswitch_32",
		":sswitch_33",
		":sswitch_34",
		":sswitch_35",
		":sswitch_36",
		":sswitch_37",
		":sswitch_38",
		":sswitch_39",
		":sswitch_3a",
		":sswitch_3b",
		":sswitch_3c",
		":sswitch_3d",
		":sswitch_3e",
		":sswitch_3f",
		":sswitch_40",
		":sswitch_41",
		":sswitch_42",
		":sswitch_43",
		":sswitch_44",
		":sswitch_45",
		":sswitch_46",
		":sswitch_47",
		":sswitch_48",
		":sswitch_49",
		":sswitch_4a",
		":sswitch_4b",
		":sswitch_4c",
		":sswitch_4d",
		":sswitch_4e",
		":sswitch_4f",
		":sswitch_50",
		":pswitch_0",
		":pswitch_1",
		":pswitch_2",
		":pswitch_3",
		":pswitch_4",
		":pswitch_5",
		":pswitch_6",
		":pswitch_7",
		":pswitch_8",
		":pswitch_9",
		":pswitch_a",
		":pswitch_b",
		":pswitch_c",
		":pswitch_d",
		":pswitch_e",
		":pswitch_f",
		":pswitch_10",
		":pswitch_11",
		":pswitch_12",
		":pswitch_13",
		":pswitch_14",
		":pswitch_15",
		":pswitch_16",
		":pswitch_17",
		":pswitch_18",
		":pswitch_19",
		":pswitch_1a",
		":pswitch_1b",
		":pswitch_1c",
		":pswitch_1d",
		":pswitch_1e",
		":pswitch_1f",
		":pswitch_20",
		":pswitch_21",
		":pswitch_22",
		":pswitch_23",
		":pswitch_24",
		":pswitch_25",
		":pswitch_26",
		":pswitch_27",
		":pswitch_28",
		":pswitch_29",
		":pswitch_2a",
		":pswitch_2b",
		":pswitch_2c",
		":pswitch_2d",
		":pswitch_2e",
		":pswitch_2f",
		":pswitch_30",
		":pswitch_31",
		":pswitch_32",
		":pswitch_33",
		":pswitch_34",
		":pswitch_35",
		":pswitch_36",
		":pswitch_37",
		":pswitch_38",
		":pswitch_39",
		":pswitch_3a",
		":pswitch_3b",
		":pswitch_3c",
		":pswitch_3d",
		":pswitch_3e",
		":pswitch_3f",
		":pswitch_40",
		":pswitch_41",
		":pswitch_42",
		":pswitch_43",
		":pswitch_44",
		":pswitch_45",
		":pswitch_46",
		":pswitch_47",
		":pswitch_48",
		":pswitch_49",
		":pswitch_4a",
		":pswitch_4b",
		":pswitch_4c",
		":pswitch_4d",
		":pswitch_4e",
		":pswitch_4f",
		":pswitch_50",
		":pswitch_data_0",
		":pswitch_data_1",
		":pswitch_data_2",
		":pswitch_data_3",
		":pswitch_data_4",
		":pswitch_data_5",
		":pswitch_data_6",
		":pswitch_data_7",
		":pswitch_data_8",
		":pswitch_data_9",
		":pswitch_data_a",
		":pswitch_data_b",
		":pswitch_data_c",
		":pswitch_data_d",
		":pswitch_data_e",
		":pswitch_data_f",
		":pswitch_data_10",
		":pswitch_data_11",
		":pswitch_data_12",
		":pswitch_data_13",
		":pswitch_data_14",
		":pswitch_data_15",
		":pswitch_data_16",
		":pswitch_data_17",
		":pswitch_data_18",
		":pswitch_data_19",
		":pswitch_data_1a",
		":pswitch_data_1b",
		":pswitch_data_1c",
		":pswitch_data_1d",
		":pswitch_data_1e",
		":pswitch_data_1f",
		":pswitch_data_20",
		":pswitch_data_21",
		":pswitch_data_22",
		":pswitch_data_23",
		":pswitch_data_24",
		":pswitch_data_25",
		":pswitch_data_26",
		":pswitch_data_27",
		":pswitch_data_28",
		":pswitch_data_29",
		":pswitch_data_2a",
		":pswitch_data_2b",
		":pswitch_data_2c",
		":pswitch_data_2d",
		":pswitch_data_2e",
		":pswitch_data_2f",
		":pswitch_data_30",
		":pswitch_data_31",
		":pswitch_data_32",
		":pswitch_data_33",
		":pswitch_data_34",
		":pswitch_data_35",
		":pswitch_data_36",
		":pswitch_data_37",
		":pswitch_data_38",
		":pswitch_data_39",
		":pswitch_data_3a",
		":pswitch_data_3b",
		":pswitch_data_3c",
		":pswitch_data_3d",
		":pswitch_data_3e",
		":pswitch_data_3f",
		":pswitch_data_40",
		":pswitch_data_41",
		":pswitch_data_42",
		":pswitch_data_43",
		":pswitch_data_44",
		":pswitch_data_45",
		":pswitch_data_46",
		":pswitch_data_47",
		":pswitch_data_48",
		":pswitch_data_49",
		":pswitch_data_4a",
		":pswitch_data_4b",
		":pswitch_data_4c",
		":pswitch_data_4d",
		":pswitch_data_4e",
		":pswitch_data_4f",
		":pswitch_data_50"
	};
}
