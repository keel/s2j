/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;


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
		this.type = Sentence.TYPE_NOT_LINE;
	}
	static final Logger log = Logger.getLogger(VarSentence.class);
	
//	private static HashMap<String,String> varMap  = new HashMap<String, String>();
//	
//	static{
//		varMap.put("const", "int");
//		varMap.put("const/4", "int");
//		varMap.put("const/16", "int");
//		varMap.put("const/high16", "float");
//		varMap.put("const-wide/16", "long");
//		varMap.put("const-wide/32", "long");
//		varMap.put("const-wide", "long");
//		varMap.put("const-wide/high16", "double");
//		varMap.put("const-string", "String");
//		varMap.put("const-string-jumbo", "String");
//		varMap.put("const-class", "Class");
//	}
	private Var v = new Var(this);
	/** 
	 * 处理变量声明
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//解析
		this.doComm(this.line);
		//this.line = this.line.replaceAll(",", ""); //String 可能包含空格
		int p1 = this.line.indexOf(" ");
		int p2 = this.line.indexOf(",");
		if (p1 == -1 || p2 == -1 || p2>=this.line.length()) {
			this.out.append("exec var error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		String[] ws = new String[3];
		ws[0] = this.line.substring(0,p1);
		ws[1] = this.line.substring(p1+1,p2);
		ws[2] = this.line.substring(p2+2).trim();
		
		//生成Var
		v.setKey(ws[0]);
		boolean isString = ws[0].contains("string");
		boolean isClass = ws[0].contains("class");
		String type = (isString ? "String" : (isClass ? "Class" : "number"));
		v.setClassName(type);
		String vName = ws[1];
		v.setName(vName);
		String value = ws[2].trim();
		if (isString) {
			v.setValue(value);
		}else if(isClass){
			v.setValue(value);
		}else{
			//直接用修正后16进制数,因为无法实际区分float,double
			value = fixNum(ws[0], value);
			v.setValue(value);
		}
		
		//仅输出value
		v.setOut(value);
		
		//加入到SentenceMgr的Var集合,此时需要判断mgr中是否已存在及v的输出状态
		if (this.mgr.getVar(v.getName()) == null) {
//			v.setOutVar(false);
			this.mgr.setVar(v);
		}else{
			//此时很可能是赋值,变为可输出状态
			Var v1 = this.mgr.getVar(v.getName());
			
			
			//是否输出的判断
			boolean isSet = !ComputeSentence.isLocalVar(v1, this.mgr.isStatic());

			if(isSet){
				this.mgr.setVar(v);
			}else{
//				if (type.equals("int")) {
//					String right = null;
//					if (String.valueOf(value).equals("0")){
//						right = Var.checkIout(v1.getClassName(), "0") +" /* " + value +" */";
//					}else{
//						right = Var.checkIout(v1.getClassName(), value);
//					}
//					if (v1.getClassName().equals("int") && (!v1.getClassName().equals("int")) && StringUtil.isDigits(v1.getValue())) {
//						right = String.valueOf(value);
//					}
//					v.setOut(right);
//				}
				v.setOut(Var.varOut(v1.getClassName(), value));
				this.out.append(v1.getOut()).append(" = ").append(v.getOut());
				this.type = Sentence.TYPE_LINE;
				v1.setValue(v.getValue());
			}
		}
		this.over();
		return true;
	}
	
	/**
	 * 修正后的16进制数,注意float和double值有不能直接用本16进制数赋值,需要float16to10,double16to10进行转换
	 * @param key
	 * @param numVal
	 * @return
	 */
	static final String fixNum(String key,String numVal){
		if (numVal.endsWith("L")) {
			return numVal;
		}
		if (key.equals("const/high16")) {
			return fixHight16(numVal);
		}
		if (key.equals("const-wide/high16")) {
			return fixWideHight16(numVal);
		}
		return numVal;
	}
	
	static final String fixHight16(String numVal){
		return numVal+"0000";
	}
	static final String fixWideHight16(String numVal){
		StringBuilder sb = new StringBuilder();
		sb.append(numVal);
		//要算上0x两位,16+2=18位
		for (int i = numVal.length(); i < 18; i++) {
			sb.append("0");
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.v;
	}

	/** 
	 * 返回SentenceMgr内static的VarSentence,注意本类中this.mgr为null
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.util.ArrayList)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr,String line) {
		return new VarSentence(mgr, line);
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
