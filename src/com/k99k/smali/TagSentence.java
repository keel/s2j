/**
 * 
 */
package com.k99k.smali;

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
	}
	
	private String tag = "";
	
	private int type = TYPE_STRUCT;
	
	private Sentence ifSen;
	
	private boolean isGoto = false;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//用于占用一个Sentence行的位置,便于结构语句插入，本行将输出一个注释
		this.doComm(this.line);
		this.tag = this.line.split(" ")[0];
		if (this.tag.indexOf(":goto")>=0) {
			this.isGoto = true;
		}
		this.out.append("// ").append(this.line);
		
		
//		if (this.mgr.getLevel()>0) {
//			this.mgr.addLevel(-1);
//		}
		
		this.over();
		return true;
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

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return this.type;
	}
	
	

	/**
	 * @param type the type to set
	 */
	public final void setType(int type) {
		this.type = type;
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
	 * @return the isGoto
	 */
	public final boolean isGoto() {
		return isGoto;
	}

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
		":cond_50",
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
		":goto_50"
	};
}
