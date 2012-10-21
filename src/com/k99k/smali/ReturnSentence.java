/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;

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
		this.type = Sentence.TYPE_STRUCT;
	}
	static final Logger log = Logger.getLogger(ReturnSentence.class);
	
	/**
	 * 如果有返回值
	 */
	private String returnKey = "";
	
	private Var var;
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		String[] ws = this.line.split(" ");
		if (ws[0].equals("return-void")) {
			this.out.append("return;");
		}else if(ws.length == 2){
			this.returnKey = ws[1];
			Var v = this.mgr.getVar(ws[1]);
			String s = ws[1];
			if (v != null) {
				s = v.getOut();
				this.var = v;
			}
//			this.out.append("return ").append(v.getOut()).append(";");
			this.out.append("return "+ s + ";");
		}else{
			this.out.append("exec return error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
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
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.var;
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

	/**
	 * @return the returnKey
	 */
	public final String getReturnKey() {
		return returnKey;
	}

}
