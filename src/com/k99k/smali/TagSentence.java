/**
 * 
 */
package com.k99k.smali;

import com.k99k.tools.StringUtil;

/**
 * 标记位置的Sentence
 * @author keel
 *
 */
public class TagSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public TagSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}
	
	private String tag = "";
	
	private Sentence ifSen;
	
//	private boolean isGoto = false;
	
	private boolean isReturn = false;
	
	/**
	 * 是否被移动到return语句之前
	 */
	private boolean isShift = false;
	
	/**
	 * 是否为倒置cond形式的while
	 */
	private boolean isReverseWhileStart = false;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//用于占用一个Sentence行的位置,便于结构语句插入，本行将输出一个注释
		this.doComm(this.line);
		this.tag = this.line.split(" ")[0];
//		if (this.tag.indexOf(":goto")>=0) {
//			this.isGoto = true;
//		}
		this.out.append("// ").append(this.line);
		
		
//		if (this.mgr.getLevel()>0) {
//			this.mgr.addLevel(-1);
//		}
		
		this.state = STATE_DOING;
		//this.over();
		this.mgr.addTag(this);
		return true;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#debug()
	 */
	@Override
	public boolean debug() {
		this.tag = this.line.split(" ")[0];
		int nn = this.tag.charAt(this.tag.indexOf("_")+1);
		this.out.append(StringUtil.intToLetter(nn-48));
		this.over();
		return true;
	}

	
	public static void main(String[] args) {
		String s = "A";
		int i = (int)s.charAt(0);
		String n = ""+(char)i;
		System.out.println(i);
		System.out.println(n);
	}

	public int getIndex(){
		return this.mgr.indexOfSentence(this);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new TagSentence(mgr, line);
	}
	
	/**
	 * 作为if结束标记的次数
	 */
	private int endTimes = 0;
	
	/**
	 * 是否为最后一个else块的开头
	 */
	private boolean isLastElseStart = false;
	
	/**
	 * 是否为最后一个else块的结尾
	 */
	private boolean isLastElseEnd = false;
	
	/**
	 * 设置结构的结束符,可多次设置，每次增加一个}号
	 */
	public final TagSentence setEndStruct(){
		this.out.append(StaticUtil.NEWLINE).append(StaticUtil.TABS[this.level]);
		this.out.append("}");
		endTimes++;
		return this;
	}
	
	


	/**
	 * @return the isReverseWhileStart
	 */
	public final boolean isReverseWhileStart() {
		return isReverseWhileStart;
	}



	/**
	 * @param isReverseWhileStart the isReverseWhileStart to set
	 */
	public final void setReverseWhileStart(boolean isReverseWhileStart) {
		this.isReverseWhileStart = isReverseWhileStart;
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getOut()
	 */
	@Override
	public String getOut() {
//		for (int i = 0; i< this.endTimes; i++) {
//			this.out.append(StaticUtil.NEWLINE).append(StaticUtil.TABS[this.level]);
//			this.out.append("}");
//		}
		if (isLastElseStart) {
			StringBuilder sb = new StringBuilder(StaticUtil.NEWLINE).append(StaticUtil.TABS[this.level]);
			if (this.endTimes == 0) {
				sb.append("}");
			}
			sb.append(" else { //else made");
			return this.out.toString()+sb.toString();
		}else{
			return this.out.toString();
		}
	}


	/**
	 * @return the isLastElseEnd
	 */
	public final boolean isLastElseEnd() {
		return isLastElseEnd;
	}



	/**
	 * @param isLastElseEnd the isLastElseEnd to set
	 */
	public final void setLastElseEnd(boolean isLastElseEnd) {
		this.isLastElseEnd = isLastElseEnd;
	}



	/**
	 * @return the isLastElseStart
	 */
	public final boolean isLastElseStart() {
		return isLastElseStart;
	}



	/**
	 * @param isLastElseStart the isLastElseStart to set
	 */
	public final void setLastElseStart(boolean isLastElseStart) {
		this.isLastElseStart = isLastElseStart;
	}



	/**
	 * 作为if结束标记的次数
	 * @return the endTimes
	 */
	public final int getEndTimes() {
		return endTimes;
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "tag";
	}
	
	public String getTag(){
		if (this.tag.equals("")) {
			return this.line.trim();
		}
		return this.tag;
	}
	
	
	
	/**
	 * @return the isShift
	 */
	public final boolean isShift() {
		return isShift;
	}

	/**
	 * @param isShift the isShift to set
	 */
	public final void setShift(boolean isShift) {
		this.isShift = isShift;
	}

	/**
	 * @return the ifSen
	 */
	public final Sentence getIfSen() {
		return ifSen;
	}

	/**
	 * @param ifSen the ifSen to set
	 */
	public final void setIfSen(Sentence ifSen) {
		this.ifSen = ifSen;
	}
	
	

	/**
	 * @return the isReturn
	 */
	public final boolean isReturn() {
		return isReturn;
	}

	/**
	 * @param isReturn the isReturn to set
	 */
	public final void setReturn(boolean isReturn) {
		this.isReturn = isReturn;
		if (isReturn) {
//			this.out.append(StaticUtil.NEWLINE).append(StaticUtil.TABS[this.level]);
//			this.out.append("return;");
		}
	}

//	/**
//	 * @return the isGoto
//	 */
//	public final boolean isGoto() {
//		return isGoto;
//	}

	static final String[] KEYS = new String[]{
		":cond_0",
		":cond_1",
		":cond_2",
		":cond_3",
		":cond_4",
		":cond_5",
		":cond_6",
		":cond_7",
		":cond_8",
		":cond_9",
		":cond_a",
		":cond_b",
		":cond_c",
		":cond_d",
		":cond_e",
		":cond_f",
		":cond_10",
		":cond_11",
		":cond_12",
		":cond_13",
		":cond_14",
		":cond_15",
		":cond_16",
		":cond_17",
		":cond_18",
		":cond_19",
		":cond_1a",
		":cond_1b",
		":cond_1c",
		":cond_1d",
		":cond_1e",
		":cond_1f",
		":cond_20",
		":cond_21",
		":cond_22",
		":cond_23",
		":cond_24",
		":cond_25",
		":cond_26",
		":cond_27",
		":cond_28",
		":cond_29",
		":cond_2a",
		":cond_2b",
		":cond_2c",
		":cond_2d",
		":cond_2e",
		":cond_2f",
		":cond_30",
		":cond_31",
		":cond_32",
		":cond_33",
		":cond_34",
		":cond_35",
		":cond_36",
		":cond_37",
		":cond_38",
		":cond_39",
		":cond_3a",
		":cond_3b",
		":cond_3c",
		":cond_3d",
		":cond_3e",
		":cond_3f",
		":cond_40",
		":cond_41",
		":cond_42",
		":cond_43",
		":cond_44",
		":cond_45",
		":cond_46",
		":cond_47",
		":cond_48",
		":cond_49",
		":cond_4a",
		":cond_4b",
		":cond_4c",
		":cond_4d",
		":cond_4e",
		":cond_4f",
		":cond_50"
	};
}
