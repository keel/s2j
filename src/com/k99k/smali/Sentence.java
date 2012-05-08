/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 语句单元 
 * @author keel
 *
 */
public class Sentence {

	/**
	 * 
	 */
	public Sentence() {
	}
	
	public static final int STATE_INIT = 0;
	public static final int STATE_DONE =1;
	public static final int STATE_DOING =2;
	
	public static final int TYPE_UNKOWN = 0;
	public static final int TYPE_NOT_STRUCT = 1;
	public static final int TYPE_STRUCT = 2;
	
	/**
	 * 标识
	 */
	private String key;
	
	/**
	 * 状态
	 */
	private int state = STATE_INIT;
	
	/**
	 * 语句类型
	 */
	private int type = TYPE_UNKOWN;
	
	/**
	 * 行号
	 */
	private int lineNum;
	
	/**
	 * 原始语句
	 */
	private ArrayList<String> srcLines;
	
	/**
	 * 输出
	 */
	private ArrayList<String> outLines = new ArrayList<String>();
	
	/**
	 * 变量
	 */
	private HashMap<String,Object>	vars = new HashMap<String, Object>();
	
	
	/**
	 * 处理语句，返回是否处理成功
	 * @param srcLines
	 * @return
	 */
	public boolean exec(ArrayList<String> srcLines){
		
		
		return true;
	}
	
	/**
	 * 继续处理未完成的部分,返回是否处理成功
	 * @return
	 */
	public boolean execNext(){
		
		
		return true;
	}
	
	/**
	 * 处理失败时返回可能能处理的其他Sentence的key
	 * @return String key of other Sentence
	 */
	public String maybeSentence(){
		
		return null;
	}



	/**
	 * @return the type
	 */
	public final int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public final void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the lineNum
	 */
	public final int getLineNum() {
		return lineNum;
	}

	/**
	 * @param lineNum the lineNum to set
	 */
	public final void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	/**
	 * @return the srcLines
	 */
	public final ArrayList<String> getSrcLines() {
		return srcLines;
	}

	/**
	 * @return the outLines
	 */
	public final ArrayList<String> getOutLines() {
		return outLines;
	}

	/**
	 * @return the vars
	 */
	public final HashMap<String, Object> getVars() {
		return vars;
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
	 * @return the state
	 */
	public final int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public final void setState(int state) {
		this.state = state;
	}

}
