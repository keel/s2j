/**
 * 
 */
package com.k99k.smali;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class GotoTagSentence extends TagSentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public GotoTagSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}
	
//	/**
//	 * goto到本句的次数,初始值为0
//	 */
//	private int gotoTimes = 0;
//	
//	/**
//	 * 插入语句的位置,默认值-1
//	 */
//	private int insertPosition = -1;
	
	
//
//
//	/**
//	 * @return the insertPosition
//	 */
//	public final int getInsertPosition() {
//		return insertPosition;
//	}
//
//
//	/**
//	 * @param insertPosition the insertPosition to set
//	 */
//	public final void setInsertPosition(int insertPosition) {
//		this.insertPosition = insertPosition;
//	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "gotoTag";
	}

//
//	/**
//	 * @return the gotoTimes
//	 */
//	public final int getGotoTimes() {
//		return gotoTimes;
//	}
//
//	/**
//	 * gotoTimes加一
//	 */
//	public final void addGotoTimes() {
//		this.gotoTimes++;
//	}
//
//	/**
//	 * gotoTimes减一
//	 */
//	public final void lessGotoTimes() {
//		this.gotoTimes--;
//		if (this.gotoTimes <= 0) {
//			this.state = Sentence.STATE_OVER;
//		}
//	}
	
	
	/**
	 * 保存移动语句块时定位插入位置,以便后面插入语句时作判断,默认为null
	 */
	private Sentence insertPoSen = null;
	
	public void setInserPoSen(Sentence sen){
		this.insertPoSen = sen;
	}
	
	public Sentence getInsertPoSen(){
		return this.insertPoSen;
	}
	

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new GotoTagSentence(mgr, line);
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.TagSentence#debug()
	 */
	@Override
	public boolean debug() {
		String tag = this.line.split(" ")[0];
		int nn = tag.charAt(tag.indexOf("_")+1);
		this.out.append(">>").append(StringUtil.intToLetter(nn-48));
		this.over();
		return true;
	}

	static final String[] KEYS = new String[]{
		":goto_0",
		":goto_1",
		":goto_2",
		":goto_3",
		":goto_4",
		":goto_5",
		":goto_6",
		":goto_7",
		":goto_8",
		":goto_9",
		":goto_a",
		":goto_b",
		":goto_c",
		":goto_d",
		":goto_e",
		":goto_f",
		":goto_10",
		":goto_11",
		":goto_12",
		":goto_13",
		":goto_14",
		":goto_15",
		":goto_16",
		":goto_17",
		":goto_18",
		":goto_19",
		":goto_1a",
		":goto_1b",
		":goto_1c",
		":goto_1d",
		":goto_1e",
		":goto_1f",
		":goto_20",
		":goto_21",
		":goto_22",
		":goto_23",
		":goto_24",
		":goto_25",
		":goto_26",
		":goto_27",
		":goto_28",
		":goto_29",
		":goto_2a",
		":goto_2b",
		":goto_2c",
		":goto_2d",
		":goto_2e",
		":goto_2f",
		":goto_30",
		":goto_31",
		":goto_32",
		":goto_33",
		":goto_34",
		":goto_35",
		":goto_36",
		":goto_37",
		":goto_38",
		":goto_39",
		":goto_3a",
		":goto_3b",
		":goto_3c",
		":goto_3d",
		":goto_3e",
		":goto_3f",
		":goto_40",
		":goto_41",
		":goto_42",
		":goto_43",
		":goto_44",
		":goto_45",
		":goto_46",
		":goto_47",
		":goto_48",
		":goto_49",
		":goto_4a",
		":goto_4b",
		":goto_4c",
		":goto_4d",
		":goto_4e",
		":goto_4f",
		":goto_50"
	};

}
