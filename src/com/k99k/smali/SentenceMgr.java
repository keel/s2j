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
	}
	
	/**
	 * sentenceMap,用于定位不同的Sentence
	 */
	private static final HashMap<String,Sentence> sentenceMap = new HashMap<String, Sentence>();
	
	
	/**
	 * if结构相关语句name
	 */
	private static final HashMap<String,String> ifMap = new HashMap<String, String>();

	
	static{
		CommSentence c = new CommSentence(null, null);
		sentenceMap.put(StaticUtil.COMM, c);
		sentenceMap.put("nop", c);
		ErrSentence e = new ErrSentence(null, null);
		sentenceMap.put(ErrSentence.KEY, e);
		LocalSentence l = new LocalSentence(null,null);
		sentenceMap.put(LocalSentence.KEY, l);
		GotoSentence gt = new GotoSentence(null, null);
		sentenceMap.put(GotoSentence.KEY, gt);
		CastSentence cast = new CastSentence(null, null);
		sentenceMap.put(CastSentence.KEY, cast);
		
		
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
		IfSentence ifs = new IfSentence(null,null);
		for (int i = 0; i < IfSentence.KEYS.length; i++) {
			sentenceMap.put(IfSentence.KEYS[i], ifs);
		}
		TagSentence t = new TagSentence(null,null);
		for (int i = 0; i < TagSentence.KEYS.length; i++) {
			sentenceMap.put(TagSentence.KEYS[i], t);
		}
		GotoTagSentence gotag = new GotoTagSentence(null,null);
		for (int i = 0; i < GotoTagSentence.KEYS.length; i++) {
			sentenceMap.put(GotoTagSentence.KEYS[i], gotag);
		}
		
		VarSentence v = new VarSentence(null, null);
		for (int i = 0; i < VarSentence.KEYS.length; i++) {
			sentenceMap.put(VarSentence.KEYS[i], v);
		}
		
		ArraySentence ar = new ArraySentence(null, null);
		for (int i = 0; i < ArraySentence.KEYS.length; i++) {
			sentenceMap.put(ArraySentence.KEYS[i], ar);
		}
		SwitchSentence ss = new SwitchSentence(null, null);
		for (int i = 0; i < SwitchSentence.KEYS.length; i++) {
			sentenceMap.put(SwitchSentence.KEYS[i], ss);
		}
		//-------------------
		//if结构语句name
		ifMap.put("if", "if");
		ifMap.put("tag", "tag");
		ifMap.put("goto", "goto");
		ifMap.put("return", "return");
		
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
	private HashMap<String,Var> vars;
	
	/**
	 * 匹配上的Sentence集合,按顺序排
	 */
	private ArrayList<Sentence> sentenceList;

	/**
	 * 结构语句中的标记集合,用于快递定位指定的tag
	 */
	private HashMap<String,Sentence> tags;
	
	/**
	 * 是否包含if结构
	 */
	private boolean hasIF = false;;
	
	private boolean hasSwitch = false;
	/**
	 * 处理原始语句集
	 */
	public void execLines(){
		
		this.setProps();
		
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
				}
				else if(s.getType() == Sentence.TYPE_STRUCT){
					String ostr = StaticUtil.TABS[s.getLevel()]+s.getOut();
					this.outLines.add(ostr);
				}
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
			//对数组赋值行特殊处理
			if (key.equals(".array-data")) {
				cNum++;
				l = this.srcLines.get(cNum);
				while(!l.startsWith(".end")){
					ArraySentence as = (ArraySentence)s;
					as.addToArrMatrix(l);
					cNum++;
					l = this.srcLines.get(cNum);
				}
				s.exec();
			}
			//对switch行特殊处理
			else if (key.equals(".packed-switch") || key.equals(".sparse-switch")) {
				cNum++;
				hasSwitch = true;
				l = this.srcLines.get(cNum);
				while(!l.startsWith(".end")){
					SwitchSentence as = (SwitchSentence)s;
					as.addSwitchKey(l);
					cNum++;
					l = this.srcLines.get(cNum);
				}
				s.exec();
			}
			//其他语句
			else if (s.exec()) {
				//成功处理语句后加入语句列表
				//只有能成行输出的操作加入到sentenceList
				if (s.getType()>Sentence.TYPE_NOT_LINE) {
					this.sentenceList.add(s);
				}
			}else{
				Sentence e = new CommSentence(this, "#ERR: unknown sententce. line:"+l);
				e.exec();
				this.sentenceList.add(e);
				//后面语句不处理了
				break;
			}
			cNum++;
		}
		if (hasSwitch) {
			SwitchScan ss = new SwitchScan(this, this.sentenceList);
			ss.scan();
		}
		//处理IFScan
		if (hasIF) {
			IFStructScan ifs = new IFStructScan(this,this.sentenceList);
			ifs.scan();
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
	
	public final void addTag(TagSentence tsen){
		tags.put(tsen.getTag(), tsen);
	}
	
	public final TagSentence getTag(String tag){
		return (TagSentence) tags.get(tag);
	}
	
	public final int indexOfSentence(Sentence sen){
		return this.sentenceList.indexOf(sen);
	}
	
	/**
	 * 根据行号查找在sentenceList中的index
	 * @param lineNum
	 * @return
	 */
	public final int findSentenceIndexByLineNum(int lineNum){
		int len = this.sentenceList.size();
		if (len<1) {
			return -1;
		}
		for (int i = len-1; i >= 0; i--) {
			Sentence s = this.sentenceList.get(i);
			if (s.getLineNum() == lineNum) {
				return this.sentenceList.indexOf(s);
			}
		}
		return -1;
	}
	
	public final Sentence findSentenceByIndex(int index){
		return this.sentenceList.get(index);
	}
	
	public final String getSrcline(int index){
		return this.srcLines.get(index);
	}
	
	/**
	 * 返回指定tag在sentenceList中的位置
	 * @param tag String
	 * @return 未找到则返回-1
	 */
	public final int findTagIndex(String tag){
		if (!tags.containsKey(tag)) {
			return -1;
		}
		return this.sentenceList.indexOf(tags.get(tag));
	}
	
	/**
	 * 获取SentenceList中的最后一个Sentence
	 * @return 
	 */
	public final Sentence getLastSentence(){
		int len = this.sentenceList.size();
		if (len<1) {
			return null;
		}
		return this.sentenceList.get(len -1);
	}
	
	/**
	 * 查找上一个匹配 name的Sentence
	 * @param senName
	 * @return
	 */
	public final Sentence findLastSentence(String senName){
		int len = this.sentenceList.size();
		if (len<1) {
			return null;
		}
		for (int i = len-1; i >= 0; i--) {
			Sentence s = this.sentenceList.get(i);
			if (s.getName().equals(senName)) {
				return s;
			}
		}
		return null;
	}
	
//	/**
//	 * 查找上一个匹配 name的Sentence
//	 * @param senName
//	 * @param fromIndex 从某一个index向上找
//	 * @return
//	 */
//	public final Sentence findLastSentence(String senName,int fromIndex){
//		int len = this.sentenceList.size();
//		if (len<1 || fromIndex>=len) {
//			return null;
//		}
//		for (int i = fromIndex; i >= 0; i--) {
//			Sentence s = this.sentenceList.get(i);
//			if (s.getName().equals(senName)) {
//				return s;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 向前查找标签
	 * @param tagName
	 * @param lineNum
	 * @return
	 */
	public final Sentence findLastTag(String tagName,int lineNum){
		int len = this.sentenceList.size();
		if (len<1|| lineNum<=0 || lineNum>=len) {
			return null;
		}
		for (int i = lineNum-1; i >= 0; i--) {
			Sentence s = this.sentenceList.get(i);
			if (s.getName().equals("tag")) {
				TagSentence tag = (TagSentence)s;
				if (tag.getTag().equals(tagName)) {
					return s;
				}
			}
		}
		return null;
	}
	
	/**
	 * 查找上一个匹配 name的Sentence
	 * @param senName
	 * @param fromIndex 从某一个index向上找
	 * @return
	 */
	public final Sentence findLastSentence(String senName,int fromIndex){
		int len = this.sentenceList.size();
		if (len<1 || fromIndex<=0 || fromIndex>=len) {
			return null;
		}
		for (int i = fromIndex-1; i >= 0; i--) {
			Sentence s = this.sentenceList.get(i);
			if (s.getName().equals(senName)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * 从某一位置查找IF结构语句
	 * @param isBack 查找方向是否是反向
	 * @param lineNum 从哪个行号开始
	 * @return Sentence
	 */
	public final Sentence findIFSentence(boolean isBack,int lineNum){
		int len = this.sentenceList.size();
		if (len<1 || lineNum<=0 || lineNum>=len) {
			return null;
		}
		if (isBack) {
			for (int i = lineNum-1; i >= 0; i--) {
				Sentence s = this.sentenceList.get(i);
				if (s.getLineNum() < lineNum && ifMap.containsKey(s.getName())) {
					return s;
				}
			}
		}else{
			for (int i = lineNum+1; i < len-1; i++) {
				Sentence s = this.sentenceList.get(i);
				if (s.getLineNum() > lineNum && ifMap.containsKey(s.getName())) {
					return s;
				}
			}
		}
		return null;
	}
	
	/**
	 * 是否为if结构语句
	 * @param senName
	 * @return
	 */
	public static final boolean isIFS(String senName){
		return ifMap.containsKey(senName);
	}

	/**
	 * 获取变量 
	 * @param key
	 * @return
	 */
	public final Var getVar(String key){
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
	public final void setVar(Var var){
		vars.put(var.getName(), var);
	}
	
	
	/**
	 * @return the hasIF
	 */
	public final boolean isHasIF() {
		return hasIF;
	}





	/**
	 * @param hasIF the hasIF to set
	 */
	public final void setHasIF(boolean hasIF) {
		this.hasIF = hasIF;
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
	 * @return the level
	 */
	public final int getLevel() {
		return level;
	}

	
}
