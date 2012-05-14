/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class GotoSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public GotoSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	
	private int type = TYPE_LINE;
	
	/**
	 * goto的目标
	 */
	private String target = "";
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		String[] ws = this.line.split(" ");
		if (ws.length<2) {
			this.out.append("exec GotoSentence error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		this.target = ws[1];
		this.out.append("// ").append(this.line);
		
		//找到
		
//		if (this.mgr.getLevel()>0) {
//			this.mgr.addLevel(-1);
//		}
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new GotoSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "goto";
	}
	
	
	
	/**
	 * @return the target
	 */
	public final String getTarget() {
		return target;
	}


	static final String KEY = "goto";

}
