/**
 * 
 */
package com.k99k.smali;


/**
 * 处理get类语句
 * @author keel
 *
 */
public class GetSentence extends Sentence {

	/**
	 * @param mgr
	 * @param srcLines
	 */
	public GetSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		
		
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne()
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,String line) {
		return new GetSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_NOT_LINE;
	}
	
	
	static final String[] KEYS = new String[]{
		"aget", //array
		"aget-wide",
		"aget-object",
		"aget-boolean",
		"aget-byte",
		"aget-char",
		"aget-short",
		"iget",  //instance
		"iget-wide",
		"iget-object",
		"iget-boolean",
		"iget-byte",
		"iget-char",
		"iget-short",
		"sget", //static
		"sget-wide",
		"sget-object",
		"sget-boolean",
		"sget-byte",
		"sget-char",
		"sget-short"
	};


	@Override
	public String getName() {
		return "get";
	}


}
