/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * @author keel
 *
 */
public class TrySentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public TrySentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}
	
	private ArrayList<String> tryKey;
	
	/**
	 * 本句的指令词
	 */
	private String key;
	
	/**
	 * .catch或.catchall语句的目标tag
	 */
	private String catchTag;
	
	/**
	 * :try_start_X最后一位数字
	 */
	private int tryStartNum = -1;
	
	/**
	 * 是否是catch和catchall的tag语句
	 */
	private boolean isTag = false;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		this.key = ws[0];
		if (this.key.indexOf(":try_start_")>-1) {
			this.out.append("try {");
			this.mgr.setHasTry(true);
			this.over();
			return true;
		}else if(this.key.indexOf(":try_end_")>-1){
			this.out.append("//end of try");
			this.over();
			return true;
		}else if(this.key.indexOf(":catch_")>-1){
			this.isTag = true;
			this.catchTag = this.key;
		}else if(this.key.indexOf(":catchall_")>-1){
			this.isTag = true;
			this.catchTag = this.key;
		}else if(this.key.equals(".catch")){
			String ex = Tool.parseObject(ws[1]);
			this.catchTag = ws[ws.length-1];
			this.out.append("} catch ("+ex+" _E_) {");
			int p = this.line.indexOf(":try_start_")+11;
			String s = this.line.substring(p,p+1);
			this.tryStartNum = Integer.parseInt(s);
		}else if(this.key.equals(".catchall")){
			this.catchTag = ws[ws.length-1];
			int p = this.line.indexOf(":try_start_")+11;
			String s = this.line.substring(p,p+1);
			this.tryStartNum = Integer.parseInt(s);
		}else if(this.key.equals("throw")){
		}
		this.state = Sentence.STATE_DOING;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new TrySentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "try";
	}
	
	
	
	/**
	 * @return the tryStartNum
	 */
	public final int getTryStartNum() {
		return tryStartNum;
	}

	/**
	 * @return the isTag
	 */
	public final boolean isTag() {
		return isTag;
	}

	/**
	 * @return the catchTag
	 */
	public final String getCatchTag() {
		return catchTag;
	}

	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}

	/**
	 * @return the tryKey
	 */
	public final void addTryKey(String key) {
		if (this.tryKey ==null) {
			this.tryKey = new ArrayList<String>();
		}
		this.tryKey.add(key);
	}

	static final String[] KEYS = new String[]{
		".catch",
		".catchall",
		"throw",
		":catchall_0",
		":catchall_1",
		":catchall_2",
		":catchall_3",
		":catchall_4",
		":catchall_5",
		":catchall_6",
		":catchall_7",
		":catchall_8",
		":catchall_9",
		":catchall_a",
		":catchall_b",
		":catchall_c",
		":catchall_d",
		":catchall_e",
		":catchall_f",
		":catchall_10",
		":catchall_11",
		":catchall_12",
		":catchall_13",
		":catchall_14",
		":catchall_15",
		":catchall_16",
		":catchall_17",
		":catchall_18",
		":catchall_19",
		":catchall_1a",
		":catchall_1b",
		":catchall_1c",
		":catchall_1d",
		":catchall_1e",
		":catchall_1f",
		":catchall_20",
		":catchall_21",
		":catchall_22",
		":catchall_23",
		":catchall_24",
		":catchall_25",
		":catchall_26",
		":catchall_27",
		":catchall_28",
		":catchall_29",
		":catchall_2a",
		":catchall_2b",
		":catchall_2c",
		":catchall_2d",
		":catchall_2e",
		":catchall_2f",
		":catchall_30",
		":catchall_31",
		":catchall_32",
		":catchall_33",
		":catchall_34",
		":catchall_35",
		":catchall_36",
		":catchall_37",
		":catchall_38",
		":catchall_39",
		":catchall_3a",
		":catchall_3b",
		":catchall_3c",
		":catchall_3d",
		":catchall_3e",
		":catchall_3f",
		":catchall_40",
		":catchall_41",
		":catchall_42",
		":catchall_43",
		":catchall_44",
		":catchall_45",
		":catchall_46",
		":catchall_47",
		":catchall_48",
		":catchall_49",
		":catchall_4a",
		":catchall_4b",
		":catchall_4c",
		":catchall_4d",
		":catchall_4e",
		":catchall_4f",
		":catchall_50",
		":catch_0",
		":catch_1",
		":catch_2",
		":catch_3",
		":catch_4",
		":catch_5",
		":catch_6",
		":catch_7",
		":catch_8",
		":catch_9",
		":catch_a",
		":catch_b",
		":catch_c",
		":catch_d",
		":catch_e",
		":catch_f",
		":catch_10",
		":catch_11",
		":catch_12",
		":catch_13",
		":catch_14",
		":catch_15",
		":catch_16",
		":catch_17",
		":catch_18",
		":catch_19",
		":catch_1a",
		":catch_1b",
		":catch_1c",
		":catch_1d",
		":catch_1e",
		":catch_1f",
		":catch_20",
		":catch_21",
		":catch_22",
		":catch_23",
		":catch_24",
		":catch_25",
		":catch_26",
		":catch_27",
		":catch_28",
		":catch_29",
		":catch_2a",
		":catch_2b",
		":catch_2c",
		":catch_2d",
		":catch_2e",
		":catch_2f",
		":catch_30",
		":catch_31",
		":catch_32",
		":catch_33",
		":catch_34",
		":catch_35",
		":catch_36",
		":catch_37",
		":catch_38",
		":catch_39",
		":catch_3a",
		":catch_3b",
		":catch_3c",
		":catch_3d",
		":catch_3e",
		":catch_3f",
		":catch_40",
		":catch_41",
		":catch_42",
		":catch_43",
		":catch_44",
		":catch_45",
		":catch_46",
		":catch_47",
		":catch_48",
		":catch_49",
		":catch_4a",
		":catch_4b",
		":catch_4c",
		":catch_4d",
		":catch_4e",
		":catch_4f",
		":catch_50",
		":try_start_0",
		":try_start_1",
		":try_start_2",
		":try_start_3",
		":try_start_4",
		":try_start_5",
		":try_start_6",
		":try_start_7",
		":try_start_8",
		":try_start_9",
		":try_start_a",
		":try_start_b",
		":try_start_c",
		":try_start_d",
		":try_start_e",
		":try_start_f",
		":try_start_10",
		":try_start_11",
		":try_start_12",
		":try_start_13",
		":try_start_14",
		":try_start_15",
		":try_start_16",
		":try_start_17",
		":try_start_18",
		":try_start_19",
		":try_start_1a",
		":try_start_1b",
		":try_start_1c",
		":try_start_1d",
		":try_start_1e",
		":try_start_1f",
		":try_start_20",
		":try_start_21",
		":try_start_22",
		":try_start_23",
		":try_start_24",
		":try_start_25",
		":try_start_26",
		":try_start_27",
		":try_start_28",
		":try_start_29",
		":try_start_2a",
		":try_start_2b",
		":try_start_2c",
		":try_start_2d",
		":try_start_2e",
		":try_start_2f",
		":try_start_30",
		":try_start_31",
		":try_start_32",
		":try_start_33",
		":try_start_34",
		":try_start_35",
		":try_start_36",
		":try_start_37",
		":try_start_38",
		":try_start_39",
		":try_start_3a",
		":try_start_3b",
		":try_start_3c",
		":try_start_3d",
		":try_start_3e",
		":try_start_3f",
		":try_start_40",
		":try_start_41",
		":try_start_42",
		":try_start_43",
		":try_start_44",
		":try_start_45",
		":try_start_46",
		":try_start_47",
		":try_start_48",
		":try_start_49",
		":try_start_4a",
		":try_start_4b",
		":try_start_4c",
		":try_start_4d",
		":try_start_4e",
		":try_start_4f",
		":try_start_50",
		":try_end_0",
		":try_end_1",
		":try_end_2",
		":try_end_3",
		":try_end_4",
		":try_end_5",
		":try_end_6",
		":try_end_7",
		":try_end_8",
		":try_end_9",
		":try_end_a",
		":try_end_b",
		":try_end_c",
		":try_end_d",
		":try_end_e",
		":try_end_f",
		":try_end_10",
		":try_end_11",
		":try_end_12",
		":try_end_13",
		":try_end_14",
		":try_end_15",
		":try_end_16",
		":try_end_17",
		":try_end_18",
		":try_end_19",
		":try_end_1a",
		":try_end_1b",
		":try_end_1c",
		":try_end_1d",
		":try_end_1e",
		":try_end_1f",
		":try_end_20",
		":try_end_21",
		":try_end_22",
		":try_end_23",
		":try_end_24",
		":try_end_25",
		":try_end_26",
		":try_end_27",
		":try_end_28",
		":try_end_29",
		":try_end_2a",
		":try_end_2b",
		":try_end_2c",
		":try_end_2d",
		":try_end_2e",
		":try_end_2f",
		":try_end_30",
		":try_end_31",
		":try_end_32",
		":try_end_33",
		":try_end_34",
		":try_end_35",
		":try_end_36",
		":try_end_37",
		":try_end_38",
		":try_end_39",
		":try_end_3a",
		":try_end_3b",
		":try_end_3c",
		":try_end_3d",
		":try_end_3e",
		":try_end_3f",
		":try_end_40",
		":try_end_41",
		":try_end_42",
		":try_end_43",
		":try_end_44",
		":try_end_45",
		":try_end_46",
		":try_end_47",
		":try_end_48",
		":try_end_49",
		":try_end_4a",
		":try_end_4b",
		":try_end_4c",
		":try_end_4d",
		":try_end_4e",
		":try_end_4f",
		":try_end_50"
	};
}
