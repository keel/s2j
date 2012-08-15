/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class PutSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public PutSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}
	
	/**
	 * 如果是数组赋值,在此保存数组对应的源name，如: v1
	 */
	private String arrSourceVar = null;

	/**
	 * 如果是数组赋值,在此保存所赋的某一位置的值
	 */
	private String arrVal = null;

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
			this.out.append("exec putSentence error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		String key = ws[0];
		//第一个表示其类型:array,instance,static
		char type = key.charAt(0);
		Var v = new Var(this);
		v.setKey(key);
		if (type == 'i') {
			Var v1 = this.mgr.getVar(ws[2]);
			Var v2 = this.mgr.getVar(ws[1]);
			int p = ws[3].indexOf(':');
			String name = ws[3].substring(ws[3].indexOf("->")+2,p);
			v.setName(ws[1]);
			String obj = Tool.parseObject(ws[3].substring(p+1));
			v.setClassName(obj);
			v.setOut(v1.getOut()+"."+name);
			
			this.out.append(v.getOut()).append(" = ").append(v2.getOut());
			
			//对于v1引用的语句，如果不成行则可去除
			Sentence s = v1.getSen();
			if (s != null && s.getState()>=Sentence.STATE_DONE && s.getType() == Sentence.TYPE_NOT_LINE) {
				s.over();
				this.mgr.removeSentence(s);
			}
		}else if(type == 's'){
			Var v2 = this.mgr.getVar(ws[1]);
			int p = ws[2].indexOf(':');
			int p2 = ws[2].indexOf('>');
			String name = ws[2].substring(p2+1,p);
			v.setName(ws[1]);
			String obj = Tool.parseObject(ws[2].substring(p+1));
			v.setClassName(obj);
			String v1 = Tool.parseObject(ws[2].substring(0,p2-1));
			v.setOut(v1+"."+name);
			
			this.out.append(v.getOut()).append(" = ").append(v2.getOut());
			
		}else if(type == 'a'){
			Var v1 = this.mgr.getVar(ws[2]);
			Var v2 = this.mgr.getVar(ws[3]);
			Var v3 = this.mgr.getVar(ws[1]);
			this.arrSourceVar = ws[2];
			String name = ws[1];
			v.setName(name);
			v.setClassName(v1.getClassName());
			v.setOut(v1.getOut()+"["+v2.getOut()+"]");
			//TODO 对于数组中的索引对象,如果是VarSentence,暂时先不removeSentence,仅标为over,可能会有其他地方用到
			//其他情况不进行处理
			Sentence s = v2.getSen();
			if (s != null && s.getName().equals("var")) {
				s.over();
			}
			this.arrVal = v3.getOut();
			this.out.append(v.getOut()).append(" = ").append(arrVal);
		}
		
		
		//不处理value
		//v.setValue(value);
		this.mgr.setVar(v);
		this.over();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new PutSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "put";
	}
	
	
	/**
	 * @return the arrSourceVar
	 */
	public final String getArrSourceVar() {
		return arrSourceVar;
	}

	/**
	 * @return the arrVal
	 */
	public final String getArrVal() {
		return arrVal;
	}

	static final String[] KEYS = new String[]{
		"aput",
		"aput-wide",
		"aput-object",
		"aput-boolean",
		"aput-byte",
		"aput-char",
		"aput-short",
		"iput",
		"iput-wide",
		"iput-object",
		"iput-boolean",
		"iput-byte",
		"iput-char",
		"iput-short",
		"sput",
		"sput-wide",
		"sput-object",
		"sput-boolean",
		"sput-byte",
		"sput-char",
		"sput-short"
	};
}
