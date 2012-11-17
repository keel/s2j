/**
 * 
 */
package com.k99k.smali;

import java.util.HashMap;

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
//		Var v = new Var(this);
		v.setKey(ws[0]);
		String type = varMap.get(ws[0]);
		v.setClassName(type);
		String vName = ws[1];
		v.setName(vName);
		String value = ws[2].trim();
		if (type.equals("String")) {
			v.setValue(value.replaceAll("\"", ""));
		}else if(type.equals("int")){
			v.setValue(Integer.decode(value));
			value = String.valueOf(v.getValue());
		}else if(type.equals("long")){
			v.setValue(Long.decode(value.replace("L", "")));
			value = String.valueOf(v.getValue());
		}else if(type.equals("float")){
			StringBuilder sb = new StringBuilder(value);
			//去掉0x,不足8位补到8位,注意负数保留负号
			if (sb.charAt(0) == '-') {
				sb.delete(1, 3);
			}else{
				sb.delete(0, 2);
			}
			for (int i = sb.length(); i < 8; i++) {
				sb.append("0");
			}
			String flag = "F";
			try {
				float fv = Float.intBitsToFloat(Integer.parseInt(sb.toString(),16));
				v.setValue(fv);
			} catch (NumberFormatException e) {
				log.error(this.mgr.getMeth().getName()+" float16to10 ERR,change to int:"+e.getCause()+" line:"+this.line);
//				e.printStackTrace();
				int fv = Integer.parseInt(StringUtil.a16to10(value));
				v.setValue(fv);
				flag = "";
			}
			value = String.valueOf(v.getValue())+flag+" /*"+value+"*/";
		}else if(type.equals("double")){
			StringBuilder sb = new StringBuilder(value);
			//去掉0x,不足8位补到8位
			if (sb.charAt(0) == '-') {
				sb.delete(1, 3);
			}else{
				sb.delete(0, 2);
			}
			for (int i = sb.length(); i < 16; i++) {
				sb.append("0");
			}
			String flag = "D";
			try {
				double lv = Double.longBitsToDouble(Long.parseLong(sb.toString(),16));
				v.setValue(lv);
			} catch (NumberFormatException e) {
				log.error(this.mgr.getMeth().getName()+" double16to10 ERR,change to int:"+e.getCause()+" line:"+this.line);
//				e.printStackTrace();
				int fv = Integer.parseInt(StringUtil.a16to10(value));
				v.setValue(fv);
				flag = "";
			}
			value = String.valueOf(v.getValue())+flag+" /*"+value+"*/";
		}else if(type.equals("Class")){
			//TODO 无法确定Class值 ,暂存String
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
				if (type.equals("int")) {
					String right = null;
					if (String.valueOf(value).equals("0")){
						right = Var.checkIout(v1.getClassName(), "0") +" /* " + value +" */";
					}else{
						right = Var.checkIout(v1.getClassName(), value);
					}
					if (v1.getClassName().equals("int") && (!v1.getClassName().equals("int")) && StringUtil.isDigits(v1.getValue())) {
						right = String.valueOf(value);
					}
					v.setOut(right);
				}
				this.out.append(v1.getOut()).append(" = ").append(v.getOut());
				this.type = Sentence.TYPE_LINE;
				v1.setValue(v.getValue());
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
