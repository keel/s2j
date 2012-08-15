/**
 * 
 */
package com.k99k.smali;

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
	 * 创建本Var的Sentence
	 */
	private Sentence sen;
	
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
	 * @return the name
	 */
	public final String getName() {
		return name;
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
