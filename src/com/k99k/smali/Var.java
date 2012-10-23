/**
 * 
 */
package com.k99k.smali;

import com.k99k.tools.StringUtil;

/**
 * 表示某方法体中的变量
 * @author keel
 *
 */
public class Var {

	/**
	 * @param sen Sentence
	 */
	public Var(Sentence sen) {
		this.sen = sen;
	}
	
	/**
	 * 最近一次在smali行中的原始key,如: const/4
	 */
	private String key;
	
	/**
	 * 对象类名
	 */
	private String className;
	
	/**
	 * 真实值,一般在VarSentence中使用
	 */
	private Object value;
	
	/**
	 * 变量名,如:v1
	 */
	private String name;
	
	/**
	 * 输出String
	 */
	private String out = "";
	
	
	/**
	 * 是否在用过状态，随时会被新Var替换
	 */
	private boolean isUsed = false;
	
	/**
	 * 创建本Var的Sentence
	 */
	private Sentence sen;
	
//	/**
//	 * 是否是可以直接输出out的Var,LocalSentence,GetSentence,PutSentence,move,invoke生成的Var都为true,只有VarSentence,ComputeSentence生成的Var为false
//	 */
//	private boolean isOutVar = true;
	
	/**
	 * 取反
	 */
	public void negVal(){
		if (this.value == null) {
			return;
		}
		String type = this.getClassName();
		boolean isNum = false;
		if(type.equals("int")){
			int v = (Integer)this.value;
			this.value = -v;
			isNum = true;
		}else if(type.equals("long")){
			long v = (Long)this.value;
			this.value = -v;
			isNum = true;
		}else if(type.equals("float")){
			float v = (Float)this.value;
			this.value = -v;
			isNum = true;
		}else if(type.equals("double")){
			double v = (Double)this.value;
			this.value = -v;
			isNum = true;
		}
		if (isNum) {
			if (this.out.startsWith("-")) {
				this.out = this.out.substring(1);
			}else{
				this.out = "-"+this.out;
			}
		}
	}

	/**
	 * 复制一个Var
	 * @return Var
	 */
	public Var cloneVar(){
		Var v = new Var(this.sen);
		v.setClassName(this.className);
		v.setKey(this.key);
		v.setName(this.name);
		v.setOut(this.out);
		v.setValue(this.value);
		return v;
	}

	/**
	 * iput或if-eqz比较时，确定后面的0是否需要输出为boolean或null
	 * @param v1 原Var
	 * @param v2Out 可能getOut=="0"的被比较Var的输出
	 * @return v2真正的输出String
	 */
	public static final String checkIout(Var v1,String v2Out){
		if(ComputSentence.isNum(v1.getClassName())){
			return v2Out;
		}else if (v1.getClassName().equals("boolean") && StringUtil.isDigits(v2Out)) {
			return (v2Out.equals("0")) ? "false" : "true";
		}else {
			if (v2Out.equals("0")) {
				return "null";
			}else{
				return v2Out;
			}
		}
	}
	
	
	
//	/**
//	 * @return the isOutVar
//	 */
//	public final boolean isOutVar() {
//		return isOutVar;
//	}
//
//	/**
//	 * @param isOutVar the isOutVar to set
//	 */
//	public final void setOutVar(boolean isOutVar) {
//		this.isOutVar = isOutVar;
//	}
	
	

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return the isUsed
	 */
	public final boolean isUsed() {
		return isUsed;
	}

	/**
	 * @param isUsed the isUsed to set
	 */
	public final void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the key
	 */
	public final String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public final void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the className
	 */
	public final String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public final void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the value
	 */
	public final Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public final void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the out
	 */
	public final String getOut() {
		return out;
	}

	/**
	 * @param out the out to set
	 */
	public final void setOut(String out) {
		this.out = out;
	}

	/**
	 * @return the sen
	 */
	public final Sentence getSen() {
		return sen;
	}

	/**
	 * @param sen the sen to set
	 */
	public final void setSen(Sentence sen) {
		this.sen = sen;
	}
	
	
}
