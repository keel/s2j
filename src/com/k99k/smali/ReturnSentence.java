/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class ReturnSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public ReturnSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		String[] ws = this.line.split(" ");
		if (ws[0].equals("return-void")) {
			this.out.append("return");
		}else if(ws.length == 2){
			Var v = this.mgr.getVar(ws[1]);
			if (v.getSen() != null) {
				v.getSen().over();
			}
			this.out.append("return ").append(v.getOut());
		}else{
			this.out.append("exec return error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new ReturnSentence(mgr, line);
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
		return "return";
	}
	
	static final String[] KEYS = new String[]{
		"return-void", 
		"return",
		"return-wide",
		"return-object"
	};

}
