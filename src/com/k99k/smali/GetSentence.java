/**
 * 
 */
package com.k99k.smali;


/**
 * 处理get类语句
 * @author keel
 *
 */
public class GetSentence extends Sentence {

	/**
	 * @param mgr
	 * @param srcLines
	 */
	public GetSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//解析
		this.doComm(this.line);
		//去除,号
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws.length<4) {
			this.out.append("//ERR: exec getSentence error. line:").append(this.line);
			System.err.println(this.out);
			return false;
		}
		String key = ws[0];
		//第一个表示其类型:array,instance,static
		char type = key.charAt(0);
		Var v = new Var(this);
		v.setName(ws[1]);
		v.setKey(key);
		if (type == 'i') {
			Var v1 = SentenceMgr.getVar(ws[2]);
			Sentence s = v1.getSen();
			if (s != null) {
				s.over();
				//TODO 
				this.mgr.removeSentence(s);
			}
			int p = ws[3].indexOf(':');
			String name = ws[3].substring(ws[3].indexOf("->")+2,p);
			v.setName(name);
			String obj = Tool.parseObject(ws[3].substring(p+1));
			v.setClassName(obj);
			v.setOut(v1.getOut()+"."+name);
		}else if(type == 's'){
			
		}else if(type == 'a'){
			
		}
		
		
		
		this.done();
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne()
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,String line) {
		return new GetSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_NOT_LINE;
	}
	
	
	static final String[] KEYS = new String[]{
		"aget", //array
		"aget-wide",
		"aget-object",
		"aget-boolean",
		"aget-byte",
		"aget-char",
		"aget-short",
		"iget",  //instance
		"iget-wide",
		"iget-object",
		"iget-boolean",
		"iget-byte",
		"iget-char",
		"iget-short",
		"sget", //static
		"sget-wide",
		"sget-object",
		"sget-boolean",
		"sget-byte",
		"sget-char",
		"sget-short"
	};


	@Override
	public String getName() {
		return "get";
	}


}
