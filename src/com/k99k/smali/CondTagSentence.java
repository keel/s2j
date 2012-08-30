/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class CondTagSentence extends TagSentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public CondTagSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "condTag";
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new CondTagSentence(mgr, line);
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
