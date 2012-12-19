/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;


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
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(GetSentence.class);
	
	private Var v = new Var(this);
	
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
		if (ws.length<3) {
			this.out.append("exec getSentence error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		String key = ws[0];
		//第一个表示其类型:array,instance,static
		char type = key.charAt(0);
//		Var v = new Var(this);
		v.setKey(key);
		if (type == 'i') {
			Var v1 = this.mgr.getVar(ws[2]);
			int p = ws[3].indexOf(':');
			String name = ws[3].substring(ws[3].indexOf("->")+2,p);
			v.setName(ws[1]);
			String obj = Tool.parseObject(ws[3].substring(p+1));
			v.setClassName(obj);
			v.setOut(v1.getOut()+"."+name);
			
			//对于v1引用的语句，如果不成行则可去除
			Sentence s = v1.getSen();
			if (s != null) {
				if (s.getType() == Sentence.TYPE_NOT_LINE) {
					//暂时先使用removeSentence
					this.mgr.removeSentence(s);
					s.type = Sentence.TYPE_NOT_LINE;
					s.over();
				}else if (s.getName().equals("var") || s.getName().equals("get")) {
					s.type = Sentence.TYPE_NOT_LINE;
					s.over();
				}
				s.over();
			}
			
		}else if(type == 's'){
			int p = ws[2].indexOf(':');
			int p2 = ws[2].indexOf('>');
			String name = ws[2].substring(p2+1,p);
			v.setName(ws[1]);
			String obj = Tool.parseObject(ws[2].substring(p+1));
			v.setClassName(obj);
			String v1 = Tool.parseObject(ws[2].substring(0,p2-1));
			v.setOut(v1+"."+name);
		}else if(type == 'a'){
			Var v1 = this.mgr.getVar(ws[2]);
			Var v2 = this.mgr.getVar(ws[3]);
			String name = ws[1];
			v.setName(name);
			v.setClassName(v1.getClassName());
			v.setOut(v1.getOut()+"["+v2.getOut()+"]");
			//TODO 对于数组中的索引对象,如果是VarSentence,暂时先不removeSentence,仅标为over,可能会有其他地方用到
			//其他情况不进行处理
			Sentence s = v2.getSen();
			if (s != null) {
				if (s.getName().equals("var") || s.getName().equals("get")) {
					s.type = Sentence.TYPE_NOT_LINE;
					s.over();
				}
			}
			s = v1.getSen();
			if (s != null) {
				if (s.getName().equals("var") || s.getName().equals("get")) {
					s.type = Sentence.TYPE_NOT_LINE;
					s.over();
				}
			}
		}
		//不处理value
		//v.setValue(value);
//		v.setOutVar(true);
		this.mgr.setVar(v);
//		this.done();
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
	 * @see com.k99k.smali.Sentence#newOne()
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,String line) {
		return new GetSentence(mgr, line);
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
