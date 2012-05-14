/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class InvokeSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public InvokeSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
	}

	private Var var = new Var(this);
	
	private int type = Sentence.TYPE_LINE;
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		String[] arr = this.line.split(" ");
		if (arr.length<3) {
			this.out.append("exec invoke error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		int alen = arr.length;
		String key = arr[0];
		String opStr = arr[alen-1];
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < alen-1; i++) {
			sb.append(",");
			sb.append(arr[i].replaceAll("\\{", "").replaceAll(",", "").replaceAll("\\}", ""));
		}
		sb.deleteCharAt(0);
		//确定变量集
		String[] rang = sb.toString().split(",");
		if (rang.length == 3 && rang[1].equals("..")) {
			int start = Integer.parseInt(rang[0].substring(1));
			int end = Integer.parseInt(rang[2].substring(1));
			int len = end-start+1;
			char headChar = rang[0].charAt(0);
			rang = new String[len];
			for (int i = 0; i < len; i++) {
				rang[i] = headChar + String.valueOf(start);
				start++;
			}
		}
		//处理方法各个String
		int p1 = opStr.indexOf("->");
		String src = Tool.parseObject(opStr.substring(0,p1));
		int p2 = opStr.indexOf("(");
		String methName = opStr.substring(p1+2,p2);
		int p3 = opStr.indexOf(")");
		String propStr = (p3-p2 == 1)?"":opStr.substring(p2,p3+1);
		String re = Tool.parseObject(opStr.substring(p3+1));
		boolean isInit = methName.equals("<init>");
		boolean isConstr = this.mgr.getMeth().isConstructor();
		//输出构造方法
		if (isInit) {
			if (isConstr) {
				//构造方法
				String str = "super";
				if(this.mgr.getMeth().getClassName().equals(src.substring(src.lastIndexOf('.')+1))){
					str = "this";
				}
				this.out.append(str).append("(");
				//this.out.append(this.mgr.getVar(rang[0]).getOut()).append(".");
			}else{
				//new 方法的构造方法
				this.out.append("new ").append(src).append("(");
			}
			
		}
		//输出静态方法
		else if(key.indexOf("static")>0){
			this.out.append(src).append(".").append(methName).append("(");
		}
		//输出一般方法
		else{
			String val = "";
			Sentence s1 = this.mgr.getVar(rang[0]).getSen();
			
			
//FIXME 对于前一个语句的处理依据，应该结合状态考虑
			
			if (s1 != null && s1.getJavaLineNum() == this.getJavaLineNum() && s1.getOut().length()>0) {
				s1.over();
				val = s1.getOut();
			}else{
				val = this.mgr.getVar(rang[0]).getOut();
				if (val.equals("this") && key.indexOf("super")>0) {
					val = "super";
				}
			}
			if (s1 != null && !s1.getName().equals("local")) {
				this.mgr.removeSentence(s1);
			}
			this.out.append(val).append(".").append(methName).append("(");
		}
		//输出参数并结束
		if (!propStr.equals("")) {
			StringBuilder sb2 = new StringBuilder();
			int start = (key.indexOf("static") >= 0) ? 0 : 1;
			for (int i = start; i < rang.length; i++) {
				sb2.append(",");
				Var v2 = this.mgr.getVar(rang[i]);
				sb2.append(v2.getOut());
			}
			sb2.deleteCharAt(0);
			this.out.append(sb2);
		}
		this.out.append(")");
		//设置Var
		this.var.setClassName(re);
		this.var.setKey(key);
		this.var.setOut(this.out.toString());
		//this.var.setValue(re);
		
		
		//处理引用的相关Sentence
		if (isInit && (!isConstr)) {
			Sentence ns = this.mgr.findLastSentence("new");
			if (ns != null) {
				ns.getVar().setOut(this.out.toString());
				this.type = Sentence.TYPE_NOT_LINE;
			}
		}
		
		if (!propStr.equals("")) {
			for (int i = 1; i < rang.length; i++) {
				Sentence s = this.mgr.getVar(rang[i]).getSen();
				if (s != null) {
					if (s.getType() == Sentence.TYPE_NOT_LINE) {
						s.over();
					}else if(s.getName().equals(this.getName())){
						s.over();
						//是方法的直接从SentenceList中去除
						this.mgr.removeSentence(s);
					}
				}
				
			}
		}
		this.over();
		return true;
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
		return new InvokeSentence(mgr, line);
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
		return "invoke";
	}
	
	static final String[] KEYS = new String[]{
		"invoke-virtual", 
		"invoke-super",
		"invoke-direct",
		"invoke-static",
		"invoke-interface",
		"invoke-virtual/range",
		"invoke-super/range",
		"invoke-direct/range",
		"invoke-static/range",
		"invoke-interface/range"
		//"invoke-direct-empty"
	};

}
