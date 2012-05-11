/**
 * 
 */
package com.k99k.smali;

/**
 * 错误处理Sentence
 * @author keel
 *
 */
public class ErrSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public ErrSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.out.append("//ERR: ").append(this.line);
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new ErrSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_LINE;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "error";
	}
	
	public static final String KEY = "ERR";

}
