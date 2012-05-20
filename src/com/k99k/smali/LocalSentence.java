/**
 * 
 */
package com.k99k.smali;

/**
 * 局部变量
 * @author keel
 *
 */
public class LocalSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public LocalSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		int len = ws.length;
		if (len<3) {
			this.out.append("exec var error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		String[] tar = ws[2].split(":");
		String name = tar[0];
		String obj = Tool.parseObject(tar[1]);
		Var v = new Var(this);
		v.setClassName(obj);
		v.setKey(KEY);
		v.setName(ws[1]);
		v.setOut(name);
		
//		String val = "";
		String val = this.mgr.getVar(ws[1]).getOut();
//		Sentence s1 = this.mgr.getLastSentence();
//		if (s1 != null && s1.getOut().length()>0) {
//			s1.over();
//			this.mgr.removeSentence(s1);
//			val = s1.getOut();
//		}else{
//			val = this.mgr.getVar(ws[1]).getOut();
//		}
		this.out.append(obj).append(" ").append(name).append(" = ").append(val);
		
		this.mgr.setVar(v);
		
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new LocalSentence(mgr, line);
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
		return "local";
	}

	static final String KEY = StaticUtil.TYPE_LOCAL;
}
