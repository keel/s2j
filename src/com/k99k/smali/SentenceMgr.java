/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 语句管理者
 * @author keel
 *
 */
public class SentenceMgr {

	public SentenceMgr(ArrayList<String> srcLines) {
		this.srcLines = srcLines;
	}
	
	/**
	 * sentenceMap
	 */
	private static final HashMap<String,Sentence> sentenceMap = new HashMap<String, Sentence>();
	
	static{
		GetSentence g = new GetSentence(null, null);
		for (int i = 0; i < GetSentence.KEYS.length; i++) {
			sentenceMap.put(GetSentence.KEYS[i], g);
		}
		
	}
	
	public final Sentence createSentence(String key,ArrayList<String> srcLines){
		return sentenceMap.get(key).newOne(this, srcLines);
	}
	
	/**
	 * 当前处理行数
	 */
	private int cNum = 0;
	
	/**
	 * 最大行数
	 */
	private int maxNum = 0;
	
	/**
	 * 缩进
	 */
	private int level = 0;
	
	/**
	 * 是否静态方法
	 */
	private boolean isStatic = false;
	
	/**
	 * 准备输出的行
	 */
	private ArrayList<String> outLines = new ArrayList<String>();
	
	/**
	 * 当前处理的几行
	 */
	private ArrayList<String> cLines = new ArrayList<String>();

	/**
	 * 原始行
	 */
	private ArrayList<String> srcLines;
	
	/**
	 * 变量
	 */
	private HashMap<String,Var> vars = new HashMap<String, Var>();
	
	/**
	 * 匹配上的Sentence集合,按顺序排
	 */
	private ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();

	
	/**
	 * 获取变量 
	 * @param key
	 * @return
	 */
	public Object getVar(String key){
		return this.vars.get(key);
	}
	
	/**
	 * 设置变量
	 * @param key
	 * @param line
	 */
	public void setVar(String key,String line){
		//声明
		
		//move
		
		
		this.vars.put(key, var);
	}
	
	
	/**
	 * 处理原始语句集
	 */
	public void execLines(){
		maxNum = this.srcLines.size();
		while (cNum < maxNum) {
			String l = this.srcLines.get(cNum);
			Sentence s = this.createSentence(Tool.getKey(l), srcLines);
			//最多尝试5次切换Sentence
			int i = 0;
			while (i<=5) {
				if (s.exec()) {
					break;
				}else{
					String key = s.maybeSentence();
					if (key.equals("")) {
						this.outLines.add("//ERR: unknown sententce. line"+l);
						break;
					}else{
						s = this.createSentence(key, srcLines);
					}
				}
				i++;
			}
			
			
			
			
			
			cNum++;
		}
		//逐行读取，当读到Sendtence的key时，创建Sentence处理,并将此段内容传入处理
		//如果处理失败,则创建另一可能处理的Sentence处理
		//处理成功,则将Sentence加入已处理的lines,注意处理后的状态是完成还是完成中
		//如果是完成则继续
		//如果是处理中则将此Sentence加入待完成的
		
		
		
	}
	

	/**
	 * 增加或减少缩进
	 * @param add
	 */
	public final void addLevel(int add){
		this.level = this.level + add;
	}

	/**
	 * 增加或减少当前行
	 * @param add
	 */
	public final void addCNum(int add){
		this.cNum = this.cNum + add;
	}
	
	/**
	 * @return the outLines
	 */
	public final ArrayList<String> getOutLines() {
		return outLines;
	}

	/**
	 * @return the cNum
	 */
	public final int getcNum() {
		return cNum;
	}
	

	/**
	 * @return the maxNum
	 */
	public final int getMaxNum() {
		return maxNum;
	}

	/**
	 * @return the isStatic
	 */
	public final boolean isStatic() {
		return isStatic;
	}

	/**
	 * @param isStatic the isStatic to set
	 */
	public final void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	
}
