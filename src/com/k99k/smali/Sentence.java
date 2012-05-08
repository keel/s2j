/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import com.k99k.tools.StringUtil;

/**
 * 语句单元 
 * @author keel
 *
 */
public abstract class Sentence {

	public Sentence(SentenceMgr mgr,ArrayList<String> srcLines) {
		this.srcLines = srcLines;
		this.mgr = mgr;
	}
	
	/**
	 * 未处理
	 */
	public static final int STATE_INIT = 0;
	/**
	 * 处理完成
	 */
	public static final int STATE_DONE =1;
	/**
	 * 结束
	 */
	public static final int STATE_OVER =2;
	
	/**
	 * 处理未完成
	 */
	public static final int STATE_DOING =3;
	
	/**
	 * 未知类型
	 */
	public static final int TYPE_UNKOWN = 0;
	/**
	 * 变量设置,不独立成句
	 */
	public static final int TYPE_VAR = 1;
	/**
	 * 获取某值,一般不独立成句
	 */
	public static final int TYPE_GET = 2;
	/**
	 * 执行操作,可能会独立成句
	 */
	public static final int TYPE_ACT = 3;
	/**
	 * 运算操作,一般不独立成句
	 */
	public static final int TYPE_COMPUTE = 4;
	/**
	 * 结构
	 */
	public static final int TYPE_STRUCT = 5;
	
	/**
	 * 源SentenceMgr
	 */
	SentenceMgr mgr;
	
	/**
	 * 状态
	 */
	int state = STATE_INIT;
	
	
	/**
	 * 行号
	 */
	int lineNum;
	
	/**
	 * 原始语句
	 */
	ArrayList<String> srcLines;
	
	/**
	 * 输出
	 */
	ArrayList<String> outLines = new ArrayList<String>();
	

	
	/**
	 * 处理语句，返回是否处理成功
	 * @param srcLines
	 * @return
	 */
	public abstract boolean exec();
	
	/**
	 * 继续处理未完成的部分,返回是否处理成功
	 * @return
	 */
	public boolean execNext(){
		return true;
	}
	
	
	/**
	 * 新实例
	 * @return
	 */
	public abstract Sentence newOne(SentenceMgr mgr,ArrayList<String> srcLines);
	
	
	/**
	 * 处理行号
	 * @param line
	 */
	public boolean lineNum(String line){
		String[] words = line.split(" ");
		if (words[0].equals(StaticUtil.TYPE_LINE) && words.length >= 2 && StringUtil.isDigits(words[1])) {
			this.lineNum = Integer.parseInt(words[1]);
			return true;
		}
		return false;
	}
	
	/**
	 * 处理失败时返回可能能处理的其他Sentence的key
	 * @return String key of other Sentence
	 */
	public String maybeSentence(){
		return "";
	}


	/**
	 * @return the type
	 */
	public abstract int getType() ;


	/**
	 * @return the lineNum
	 */
	public final int getLineNum() {
		return lineNum;
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
