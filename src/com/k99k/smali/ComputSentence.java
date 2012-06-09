/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class ComputSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public ComputSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new ComputSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_NOT_LINE;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "compute";
	}

	static final String[] KEYS = new String[]{
		"neg-int",
		"not-int",
		"neg-long",
		"not-long",
		"neg-float",
		"neg-double",
		"int-to-long",
		"int-to-float",
		"int-to-double",
		"long-to-int",
		"long-to-float",
		"long-to-double",
		"float-to-int",
		"float-to-long",
		"float-to-double",
		"double-to-int",
		"double-to-long",
		"double-to-float",
		"int-to-byte",
		"int-to-char",
		"int-to-short",
		"add-int",
		"sub-int",
		"mul-int",
		"div-int",
		"rem-int",
		"and-int",
		"or-int",
		"xor-int",
		"shl-int",
		"shr-int",
		"ushr-int",
		"add-long",
		"sub-long",
		"mul-long",
		"div-long",
		"rem-long",
		"and-long",
		"or-long",
		"xor-long",
		"shl-long",
		"shr-long",
		"ushr-long",
		"add-float",
		"sub-float",
		"mul-float",
		"div-float",
		"rem-float",
		"add-double",
		"sub-double",
		"mul-double",
		"div-double",
		"rem-double",
		"add-int/2addr",
		"sub-int/2addr",
		"mul-int/2addr",
		"div-int/2addr",
		"rem-int/2addr",
		"and-int/2addr",
		"or-int/2addr",
		"xor-int/2addr",
		"shl-int/2addr",
		"shr-int/2addr",
		"ushr-int/2addr",
		"add-long/2addr",
		"sub-long/2addr",
		"mul-long/2addr",
		"div-long/2addr",
		"rem-long/2addr",
		"and-long/2addr",
		"or-long/2addr",
		"xor-long/2addr",
		"shl-long/2addr",
		"shr-long/2addr",
		"ushr-long/2addr",
		"add-float/2addr",
		"sub-float/2addr",
		"mul-float/2addr",
		"div-float/2addr",
		"rem-float/2addr",
		"add-double/2addr",
		"sub-double/2addr",
		"mul-double/2addr",
		"div-double/2addr",
		"rem-double/2addr",
		"add-int/lit16",
		"rsub-int",
		"mul-int/lit16",
		"div-int/lit16",
		"rem-int/lit16",
		"and-int/lit16",
		"or-int/lit16",
		"xor-int/lit16",
		"add-int/lit8",
		"rsub-int/lit8",
		"mul-int/lit8",
		"div-int/lit8",
		"rem-int/lit8",
		"and-int/lit8",
		"or-int/lit8",
		"xor-int/lit8",
		"shl-int/lit8",
		"shr-int/lit8",
		"ushr-int/lit8"
	};
}
