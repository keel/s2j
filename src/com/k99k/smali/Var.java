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
	 * 最近一次在smali行中的原始key
	 */
	private String key;
	
	/**
	 * 对象类名
	 */
	private String className;
	
	/**
	 * 真实值
	 */
	private Object value;
	
	/**
	 * 变量名
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
