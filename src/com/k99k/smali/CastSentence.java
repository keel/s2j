/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class CastSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public CastSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_NOT_LINE;
	}
	
	private String arrVal= null;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws.length<3) {
			this.out.append("exec cast error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		//check-cast v0, [[I
		Var v = this.mgr.getVar(ws[1]);
		String castTo = Tool.parseObject(ws[2]);
		v.setOut("("+castTo+")"+v.getOut());
		this.mgr.setVar(v);
		if (ws[2].indexOf("[") > -1) {
			this.arrVal = castTo;
		}
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new CastSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "cast";
	}

	
	/**
	 * @return the arrVal
	 */
	public final String getArrVal() {
		return arrVal;
	}

	public static final String KEY = "check-cast";
}
