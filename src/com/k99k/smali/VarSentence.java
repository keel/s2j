/**
 * 
 */
package com.k99k.smali;

import java.util.HashMap;


/**
 * 声明语句,注意此类将作为静态类在mgr中使用,不可在内部访问mgr
 * @author keel
 *
 */
public class VarSentence extends Sentence {

	/**
	 * @param mgr
	 * @param srcLines
	 */
	public VarSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		//始终是结束状态,不需要再处理 
		this.over();
	}
	
	private static HashMap<String,String> varMap  = new HashMap<String, String>();
	
	static{
		varMap.put("const", "int");
		varMap.put("const/4", "int");
		varMap.put("const/16", "int");
		varMap.put("const/high16", "float");
		varMap.put("const-wide/16", "long");
		varMap.put("const-wide/32", "long");
		varMap.put("const-wide", "long");
		varMap.put("const-wide/high16", "double");
		varMap.put("const-string", "String");
		varMap.put("const-string-jumbo", "String");
		varMap.put("const-class", "Class");
	}

	/** 
	 * 处理变量声明
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//解析
		this.doComm(this.line);
		
		String[] ws = this.line.split(" ");
		if (ws.length<3) {
			this.out.append("exec var error. line:").append(this.line);
			this.mgr.err(this);
			System.err.println(this.out);
			return false;
		}
		
		//生成Var
		Var v = new Var(this);
		v.setKey(ws[0]);
		String type = varMap.get(ws[0]);
		v.setClassName(type);
		String vName = ws[1].replaceAll(",", "");
		v.setName(vName);
		String value = ws[2];
		if (type.equals("String")) {
			v.setValue(value.replaceAll("\"", ""));
		}else if(type.equals("int")){
			v.setValue(Integer.decode(value));
		}else if(type.equals("long")){
			v.setValue(Long.decode(value));
		}else if(type.equals("float")){
			v.setValue(Float.valueOf(value));
		}else if(type.equals("double")){
			v.setValue(Double.valueOf(value));
		}else if(type.equals("Class")){
			//TODO 无法确定Class值 ,暂存String
			v.setValue(value);
		}
		//仅输出value
		v.setOut(value);
		
		//加入到SentenceMgr的Var集合
		SentenceMgr.setVar(v);
		this.over();
		return true;
	}

	/** 
	 * 返回SentenceMgr内static的VarSentence,注意本类中this.mgr为null
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.util.ArrayList)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,String line) {
		Sentence s = SentenceMgr.getVarSen();
		s.setLine(line);
		return s;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_NOT_LINE;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "var";
	}
	static final String[] KEYS = new String[]{
		"const", 
		"const/4",
		"const/16",
		"const/high16",
		"const-wide/16",
		"const-wide/32",
		"const-wide",
		"const-wide/high16", 
		"const-string",
		"const-string-jumbo",
		"const-class"
	};
}
