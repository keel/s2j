/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class NewSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public NewSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}
	
	private Var v = new Var(this);

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//自身不设置输出，仅生成一个Var 
		
		//FIXME filled-new-array 等 未完成
		
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		int len = ws.length;
		if (len < 3) {
			this.out.append("exec newSentence error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		String obj = "";
		if (ws[0].equals("new-instance")) {
			obj = Tool.parseObject(ws[2]);
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-instance");
			v.setOut(obj);
		}else if(ws[0].equals("new-array")){
			obj = Tool.parseObject(ws[3]);
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-array");
			v.setOut(obj);
		}
		this.mgr.setVar(v);
		this.over();
		return true;
	}
	
	

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.v;
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new NewSentence(mgr, line);
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
		return "new";
	}
	
	static final String[] KEYS = new String[]{
		"new-instance", 
		"new-array", 
		"filled-new-array", 
		"filled-new-array-range"
		
	};

}
