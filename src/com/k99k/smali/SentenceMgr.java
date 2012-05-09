/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;

import com.k99k.tools.StringUtil;

/**
 * 语句管理者
 * @author keel
 *
 */
public class SentenceMgr {

	public SentenceMgr(ArrayList<String> srcLines,Methods meth) {
		this.srcLines = srcLines;
		outLines = new ArrayList<String>();
		vars = new HashMap<String, Var>();
		sentenceList = new ArrayList<Sentence>();
		this.meth = meth;
		this.setP0();
	}
	
	/**
	 * sentenceMap
	 */
	private static final HashMap<String,Sentence> sentenceMap = new HashMap<String, Sentence>();
	
	private static VarSentence varSen = new VarSentence(null, null);
	
	static{
		GetSentence g = new GetSentence(null, null);
		for (int i = 0; i < GetSentence.KEYS.length; i++) {
			sentenceMap.put(GetSentence.KEYS[i], g);
		}
		
		for (int i = 0; i < VarSentence.KEYS.length; i++) {
			sentenceMap.put(VarSentence.KEYS[i], varSen);
		}
		CommSentence c = new CommSentence(null, null);
		sentenceMap.put(StaticUtil.COMM, c);
		
	}
	
	public final Sentence createSentence(String key,String line){
		return sentenceMap.get(key).newOne(this, line);
	}
	
	/**
	 * 方法参数集
	 */
	private Methods meth;
	
	
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
	private ArrayList<String> outLines;
	
	/**
	 * 原始行
	 */
	private ArrayList<String> srcLines;
	
	/**
	 * 变量
	 */
	private static HashMap<String,Var> vars;
	
	/**
	 * 匹配上的Sentence集合,按顺序排
	 */
	private ArrayList<Sentence> sentenceList;

	
	/**
	 * 获取变量 
	 * @param key
	 * @return
	 */
	public final static Var getVar(String key){
		return vars.get(key);
	}
	
	/**
	 * 移除某个已处理的Sentence
	 * @param sen
	 */
	public final void removeSentence(Sentence sen){
		this.sentenceList.remove(sen);
	}
	/**
	 * 获取p0的Var，根据是否静态方法有所不同
	 * @param sen Sentence
	 * @param key 用于设置Var的key
	 * @return Var
	 */
	public final void setP0(){
		if (this.isStatic()) {
			String vs = this.getMeth().getMethodProp(0);
			String[] ws = vs.split(" ");
			Var v = new Var(null);
			v.setClassName(ws[0]);
			v.setKey("");
			v.setName(ws[1]);
			v.setValue(ws[1]);
			v.setOut(ws[1]);
			vars.put("p0", v);
		}else{
			Var v = new Var(null);
			String c = this.getMeth().getClassName();
			v.setClassName(c);
			v.setKey("");
			v.setName("this");
			v.setOut("this");
			v.setValue("this");
			vars.put("p0", v);
		}
	}
	
	/**
	 * 设置变量
	 * @param key
	 * @param line
	 */
	public static final void setVar(String key,Var var){
		vars.put(key, var);
	}
	
	
	/**
	 * 处理原始语句集
	 */
	public void execLines(){
		
		this.parse();
		
		this.render();
		
		//逐行读取，当读到Sendtence的key时，创建Sentence处理,并将此段内容传入处理
		//如果处理失败,则创建另一可能处理的Sentence处理
		//处理成功,则将Sentence加入已处理的lines,注意处理后的状态是完成还是完成中
		//如果是完成则继续
		//如果是处理中则将此Sentence加入待完成的
		
		
		
	}
	
	
	/**
	 * 从sentenceList生成输出的outLines
	 */
	public void render(){
		//处理sentenceList,将处于非over状态的sentence进行处理,直到全部处理完成
		
		//全部完成后按顺序输出到outLines
	}
	
	/**
	 * 处理每行，并加入到sentenceList
	 */
	public void parse(){
		maxNum = this.srcLines.size();
		while (cNum < maxNum) {
			String l = this.srcLines.get(cNum);
			String key = Tool.getKey(l);
			int javaLine = this.javaLineNum(l);
			Sentence s = this.createSentence(key, l);
			//最多尝试5次切换Sentence
			int i = 0;
			while (i<=5) {
				
				if (s.exec()) {
					//成功处理语句后设置行号等，并加入语句列表
					s.setLineNum(cNum);
					if (javaLine > -1) {
						s.setJavaLineNum(javaLine);
					}
					//只有能成行输出的操作加入到sentenceList
					if (s.getType()>Sentence.TYPE_NOT_LINE) {
						this.sentenceList.add(s);
					}
					break;
				}else{
					key = s.maybeSentence();
					if (key.equals("")) {
						this.sentenceList.add(new CommSentence(this, "//ERR: unknown sententce. line"+l));
						break;
					}else{
						s = this.createSentence(key, l);
					}
				}
				i++;
			}
			cNum++;
		}
	}
	
	/**
	 * 处理行号
	 * @param line
	 */
	public int javaLineNum(String line){
		String[] words = line.split(" ");
		if (words[0].equals(StaticUtil.TYPE_LINE) && words.length >= 2 && StringUtil.isDigits(words[1])) {
			return Integer.parseInt(words[1]);
		}
		return -1;
	}
	
	/**
	 * @return Methods
	 */
	public final Methods getMeth(){
		return this.meth;
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

	/**
	 * @return the varSen
	 */
	public final VarSentence getVarSen() {
		return varSen;
	}

	
}
