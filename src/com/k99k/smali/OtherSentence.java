/**
 * 
 */
package com.k99k.smali;

/**
 * 其他的一些Sentence，不需要处理，或用于未实现时临时测试
 * @author keel
 *
 */
public class OtherSentence extends TagSentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public OtherSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}
	private String name = "other";
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.TagSentence#exec()
	 */
	@Override
	public boolean exec() {
		this.out.append("//[OTHER] ").append(this.line.substring(1));
		this.over();
		return true;
	}


	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new OtherSentence(mgr, line);
	}
	
	static final String[] KEYS = new String[]{
		".end",
		".restart"
	};

}
