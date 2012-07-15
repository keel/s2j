/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * 语句单元 
 * @author keel
 *
 */
public abstract class Sentence {

	public Sentence(SentenceMgr mgr,String line) {
		this.line = line;
		this.mgr = mgr;
	}
	
	/**
	 * 未处理
	 */
	public static final int STATE_INIT = 0;
	
	/**
	 * 处理未完成
	 */
	public static final int STATE_DOING =1;
	/**
	 * 处理完成
	 */
	public static final int STATE_DONE =2;
	/**
	 * 结束
	 */
	public static final int STATE_OVER =3;
	
	/**
	 * 未知类型
	 */
	public static final int TYPE_UNKOWN = 0;
	/**
	 * 不成行
	 */
	public static final int TYPE_NOT_LINE = 1;
	/**
	 * 单行
	 */
	public static final int TYPE_LINE = 2;
	/**
	 * 结构(多行)
	 */
	public static final int TYPE_STRUCT = 3;
	
	int level;
	
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
	int lineNum = -1;
	
	/**
	 * 对应的java文件行号
	 */
	int javaLineNum;
	
	/**
	 * 原始语句
	 */
	String line;
	
	/**
	 * 相关的Sentence集合
	 */
	ArrayList<Sentence> linkedSentenceList;
	
	/**
	 * 单行或局部输出
	 */
	StringBuilder out = new StringBuilder();
	
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
	public abstract Sentence newOne(SentenceMgr mgr,String line);

	
	/**
	 * 处理失败时返回可能能处理的其他Sentence的key
	 * @return String key of other Sentence
	 */
	public String maybeSentence(){
		return "";
	}
	
	/**
	 * 添加相关的Sentence,仅针对结构类型的Sentence
	 * @param sen Sentence
	 */
	public void addLink(Sentence sen){
		if (this.linkedSentenceList == null) {
			this.linkedSentenceList = new ArrayList<Sentence>();
		}
		this.linkedSentenceList.add(sen);
	}
	
	/**
	 * 对于invoke等类型的Sentence，会生成一个结果Var,由此方法返回
	 * @return Var
	 */
	public Var getVar(){
		return null;
	}
	
	/**
	 * 处理一行中的注释,注意这里与Context中的处理不同
	 * @param l
	 * @return
	 */
	public final void doComm(String l){
		int c = l.indexOf(StaticUtil.COMM);
		if (c == -1) {
			return;
		}
		if (c == 0) {
			this.out.append(" /* ").append(l).append(" */ ").append(StaticUtil.NEWLINE);
			return;
		}
		if (c>0 && l.length()>c+1) {
			this.out.append(" /* ").append(l.substring(c+1)).append(" */ ");
			this.line = l.substring(0,c).trim();
		}
	}


	/**
	 * @return the type
	 */
	public abstract int getType() ;


	/**
	 * 名称，用于查找时匹配特定的Sentence
	 */
	public abstract String getName() ;
	
	/**
	 * @return the lineNum
	 */
	public final int getLineNum() {
		return lineNum;
	}

	/**
	 * @return the srcLines
	 */
	public final String getSrcLine() {
		return this.line;
	}

//	/**
//	 * 多行输出
//	 */
//	public ArrayList<String> getOutLines(){
//		return null;
//	}
	
	/**
	 * 单行输出
	 */
	public String getOut(){
		return this.out.toString();
	}
	
	/**
	 * 设置输出
	 * @param outStr
	 */
	public void setOut(String outStr){
		this.out = new StringBuilder();
		this.out.append(outStr);
	}
	
	public Sentence appendOut(String outStr){
		this.out.append(outStr);
		return this;
	}
	
	public void over(){
		this.state = STATE_OVER;
	}
	
	public void done(){
		this.state = STATE_DONE;
	}


	/**
	 * @return the javaLineNum
	 */
	public final int getJavaLineNum() {
		return javaLineNum;
	}

	/**
	 * @param javaLineNum the javaLineNum to set
	 */
	public final void setJavaLineNum(int javaLineNum) {
		this.javaLineNum = javaLineNum;
	}

	/**
	 * @param lineNum the lineNum to set
	 */
	public final void setLineNum(int lineNum) {
		this.lineNum = lineNum;
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

	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public final void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the line
	 */
	public final String getLine() {
		return line;
	}

	/**
	 * @param line the line to set
	 */
	public final void setLine(String line) {
		this.line = line;
	}
	
	

}
