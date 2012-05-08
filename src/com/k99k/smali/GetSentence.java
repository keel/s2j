/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

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
	public GetSentence(SentenceMgr mgr, ArrayList<String> srcLines) {
		super(mgr, srcLines);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		String line = this.srcLines.remove(0);
		if(this.lineNum(line)){
			line = this.srcLines.remove(0);
		}
		
		
		
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne()
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,ArrayList<String> srcLines) {
		return new GetSentence(mgr, srcLines);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_GET;
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


}
