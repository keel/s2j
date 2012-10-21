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
		this.type = Sentence.TYPE_NOT_LINE;
	}
	private String name = "other";
	
	private Var var;
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.TagSentence#exec()
	 */
	@Override
	public boolean exec() {
		//this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws[0].equals(".restart")) {
			if (ws[1].equals("local")) {
				this.var = this.mgr.restartVar(ws[2]);
			}else{
				this.var = this.mgr.getVar(ws[2]);
			}
//		}else if(ws[0].equals(".end")){
//			if (ws[1].equals("local")) {
//				this.mgr.removeVar(ws[2]);
//			}
		}else if(ws[0].equals(".end")){
			if (ws[1].equals("local")) {
				this.mgr.endVar(ws[2]);
			}
			this.out.append("//[OTHER] ").append(this.line.substring(1));
		}
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
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.var;
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
