/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
		this.setProps();
	}
	
	/**
	 * sentenceMap
	 */
	private static final HashMap<String,Sentence> sentenceMap = new HashMap<String, Sentence>();
	
	private static VarSentence varSen = new VarSentence(null, null);
	
	static{
		CommSentence c = new CommSentence(null, null);
		sentenceMap.put(StaticUtil.COMM, c);
		ErrSentence e = new ErrSentence(null, null);
		sentenceMap.put(ErrSentence.KEY, e);
		LocalSentence l = new LocalSentence(null,null);
		sentenceMap.put(LocalSentence.KEY, l);
		
		
		GetSentence g = new GetSentence(null, null);
		for (int i = 0; i < GetSentence.KEYS.length; i++) {
			sentenceMap.put(GetSentence.KEYS[i], g);
		}
		InvokeSentence inv = new InvokeSentence(null,null);
		for (int i = 0; i < InvokeSentence.KEYS.length; i++) {
			sentenceMap.put(InvokeSentence.KEYS[i], inv);
		}
		ReturnSentence r = new ReturnSentence(null,null);
		for (int i = 0; i < ReturnSentence.KEYS.length; i++) {
			sentenceMap.put(ReturnSentence.KEYS[i], r);
		}
		PutSentence p = new PutSentence(null,null);
		for (int i = 0; i < PutSentence.KEYS.length; i++) {
			sentenceMap.put(PutSentence.KEYS[i], p);
		}
		ComputSentence com = new ComputSentence(null,null);
		for (int i = 0; i < ComputSentence.KEYS.length; i++) {
			sentenceMap.put(ComputSentence.KEYS[i], com);
		}
		NewSentence n = new NewSentence(null,null);
		for (int i = 0; i < NewSentence.KEYS.length; i++) {
			sentenceMap.put(NewSentence.KEYS[i], n);
		}
		MoveSentence m = new MoveSentence(null,null);
		for (int i = 0; i < MoveSentence.KEYS.length; i++) {
			sentenceMap.put(MoveSentence.KEYS[i], m);
		}
		
		for (int i = 0; i < VarSentence.KEYS.length; i++) {
			sentenceMap.put(VarSentence.KEYS[i], varSen);
		}
	}
	
	public final Sentence createSentence(String key,String line){
		if (!sentenceMap.containsKey(key)) {
			return null;
		}
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
		
		//输出时可控制缩进以及检查末尾的;号
		Iterator<Sentence> it = this.sentenceList.iterator();
		while (it.hasNext()) {
			Sentence s = it.next();
			if (s.state == Sentence.STATE_OVER) {
				if (s.getType() == Sentence.TYPE_LINE) {
					this.outLines.add(StaticUtil.TABS[s.getLevel()]+s.getOut()+";");
				}else if(s.getType() == Sentence.TYPE_STRUCT){
					ArrayList<String> l = s.getOutLines();
					for (Iterator<String> itt = l.iterator(); itt.hasNext();) {
						String o = itt.next();
						this.outLines.add(StaticUtil.TABS[s.getLevel()]+o+";");
					}
				}
			}else if(s.state == Sentence.STATE_DOING){
				//对于STATE_DOING状态的Sentence，执行一次execNext
				if (s.execNext()) {
					if (s.getType() == Sentence.TYPE_LINE) {
						this.outLines.add(StaticUtil.TABS[s.getLevel()]+s.getOut());
					}else if(s.getType() == Sentence.TYPE_STRUCT){
						ArrayList<String> l = s.getOutLines();
						for (Iterator<String> itt = l.iterator(); itt.hasNext();) {
							String o = itt.next();
							this.outLines.add(StaticUtil.TABS[s.getLevel()]+o+";");
						}
					}
				}else{
					this.outLines.add("//ERR: execNext() failed. line:"+s.line);
				}
			}else{
				this.outLines.add("//ERR: sentence not over. line:"+s.line);
			}
		}
	}

	/**
	 * 处理每行，并加入到sentenceList
	 */
	public void parse(){
		maxNum = this.srcLines.size();
		while (cNum < maxNum) {
			String l = this.srcLines.get(cNum);
			int javaLine = this.javaLineNum(l);
			if (javaLine >= 0) {
				cNum++;
				l = this.srcLines.get(cNum);
			}
			String key = Tool.getKey(l);
			Sentence s = this.createSentence(key, l);
			if (s == null) {
				//未知key的处理
				Sentence e = new CommSentence(this, "#ERR: unknown sententce. line:"+l);
				e.exec();
				this.sentenceList.add(e);
				cNum++;
				continue;
			}
			s.setLevel(this.level);
			s.setLineNum(cNum);
			if (javaLine > -1) {
				s.setJavaLineNum(javaLine);
			}
			//最多尝试5次切换Sentence
			int i = 0;
			while (i<=5) {
				if (s.exec()) {
					//成功处理语句后加入语句列表
					//只有能成行输出的操作加入到sentenceList
					if (s.getType()>Sentence.TYPE_NOT_LINE) {
						this.sentenceList.add(s);
					}
					break;
				}else{
					key = s.maybeSentence();
					if (key.equals("")) {
						Sentence e = new CommSentence(this, "#ERR: unknown sententce. line:"+l);
						e.exec();
						this.sentenceList.add(e);
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
	 * Sentence出错时的处理
	 * @param srcSen 原Sentence
	 */
	public final void err(Sentence srcSen ){
		Sentence s = this.createSentence(ErrSentence.KEY, srcSen.getOut());
		s.setLineNum(srcSen.getLineNum());
		s.setJavaLineNum(srcSen.getJavaLineNum());
		s.setLevel(srcSen.getLevel());
		s.exec();
		this.sentenceList.add(s);
	}
	
//	/**
//	 * 获取SentenceList中的Sentence
//	 * @param index 索引
//	 * @return
//	 */
//	public final Sentence getSentence(int index){
//		return this.sentenceList.get(index);
//	}
	
	/**
	 * 获取SentenceList中的最后一个Sentence
	 * @return 
	 */
	public final Sentence getLastSentence(){
		int len = this.sentenceList.size();
		return this.sentenceList.get(len -1);
	}

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
	 * 设置p0的Var，根据是否静态方法有所不同
	 * @param sen Sentence
	 * @param key 用于设置Var的key
	 * @return Var
	 */
	public final void setProps(){
		ArrayList<String> props = this.meth.getMethProps();
		int len = props.size();
		int start = 1;
		if (isStatic) {
			start = 0;
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
		
		for (int i = 0; i < len; i++) {
			String vs = props.get(i);
			String[] ws = vs.split(" ");
			Var v = new Var(null);
			v.setClassName(ws[0]);
			v.setKey("");
			v.setName(ws[1]);
			v.setValue(ws[1]);
			v.setOut(ws[1]);
			vars.put("p"+start, v);
			start++;
		}
		
	}
	
	/**
	 * 设置变量
	 * @param key
	 * @param line
	 */
	public static final void setVar(Var var){
		vars.put(var.getName(), var);
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
	public static final VarSentence getVarSen() {
		return varSen;
	}
	
	
	/**
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	
}
