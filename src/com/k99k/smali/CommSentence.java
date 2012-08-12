/**
 * 
 */
package com.k99k.smali;

/**
 * 注释行
 * @author keel
 *
 */
public class CommSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public CommSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.out.append("// ").append(this.line.substring(1));
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new CommSentence(mgr, line);
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "#";
	}

}
