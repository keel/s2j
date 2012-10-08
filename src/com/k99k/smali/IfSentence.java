/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;


/**
 * @author keel
 *
 */
public class IfSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public IfSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}
	static final Logger log = Logger.getLogger(IfSentence.class);
	
//	/**
//	 * 准备输出的行
//	 */
//	private ArrayList<String> outLines = new ArrayList<String>();
//	
	static HashMap<String,String> compareMap = new HashMap<String, String>();
	static{
		compareMap.put("if-eq"," == ");
		compareMap.put("if-ne"," != ");
		compareMap.put("if-lt"," < ");
		compareMap.put("if-ge"," >= ");
		compareMap.put("if-gt"," > ");
		compareMap.put("if-le"," <= ");
		compareMap.put("if-eqz"," == ");
		compareMap.put("if-nez"," != ");
		compareMap.put("if-ltz"," < ");
		compareMap.put("if-gez"," >= ");
		compareMap.put("if-gtz"," > ");
		compareMap.put("if-lez"," <= ");
	}
	
	/**
	 * 用于保存比较方法
	 */
	private String comp = "";
	
	/**
	 * srcLine中的key
	 */
	private String key;

	
	private String left;
	
	private String right;
	
	private boolean isBoolean = false;
	
	private String ifCond = "if";
	
	private String boolVal = "";
	
	private boolean isReversed = false;
	
	/**
	 * 自定义输出,因为重写了getOut方法,当此值为true时，恢复重写前的getOut方法
	 */
	private boolean customOut = false;
	
	/**
	 * if 指向的cond_x的Sentence
	 */
	private TagSentence condTag;
	
	/**
	 * if内部结束位置指到的Sen所在的lineNum
	 */
	private int endSenLineNum;
	
	/**
	 * if 指向的cond_x
	 */
	private String cond;
	
	/**
	 * 是否是doWhile
	 */
	private boolean isDoWhile = false;
	
	/**
	 * 是否是倒置cond形成的while
	 */
	private boolean isReversedWhile = false;
	/**
	 * 是否是指向
	 */
	private boolean isToReturn = false;
	
	/**
	 * 是否是特定的，用于mergeWhile
	 */
	private boolean isSpecial = false;
	
	
	
	/**
	 * @return the isSpecial
	 */
	public final boolean isSpecial() {
		return isSpecial;
	}



	/**
	 * @param isSpecial the isSpecial to set
	 */
	public final void setSpecial(boolean isSpecial) {
		this.isSpecial = isSpecial;
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.mgr.setHasIF(true);
		this.line= this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws.length<3) {
			this.out.append("exec IfSentence error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		this.key = ws[0];
		
		//先生成条件行
		this.comp = compareMap.get(key);
		if (key.indexOf('z') >= 0) {
			//与0比较
			Var v = this.mgr.getVar(ws[1]);
			this.left = v.getOut();
			if (v.getClassName().equals("boolean")) {
				this.isBoolean = true;
				if (key.equals("if-eqz")) {
					this.boolVal = "!";
				}
			}
			this.right = Var.checkIout(v, "0");
			this.cond = ws[2];
			Sentence s1 = v.getSen();
			if (s1 != null && !s1.getName().equals("local")) {
				this.mgr.removeSentence(s1);
			}
		}else{
			//两对象比较
			Var v1 = this.mgr.getVar(ws[1]);
			Var v2 = this.mgr.getVar(ws[2]);
			this.left = v1.getOut();
			this.right = Var.checkIout(v1, v2.getOut());
			this.cond = ws[3];
			Sentence s1 = v1.getSen();
			Sentence s2 = v2.getSen();
			if (s1 != null && !s1.getName().equals("local")) {
				this.mgr.removeSentence(s1);
			}
			if (s2 != null && !s2.getName().equals("local")) {
				this.mgr.removeSentence(s2);
			}
		}
		
		
		this.setState(STATE_DOING);
		//this.over();
		return true;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#debug()
	 */
	@Override
	public boolean debug() {
		this.isDebug = true;
		this.exec();
		this.over();
		return true;
	}

	private boolean isDebug = false;


	/**
	 * 输出
	 */
	private void render(){
		if(isClosePre){
			this.out.append("} ");
		}else if (isElse) {
			this.out.append("else ");
		}else if(this.isDoWhile){
			this.out.append("} ");
		}
		this.out.append(this.ifCond);
		this.out.append(" (");
		this.out.append(getCondOut());
		this.out.append(")");
		if (this.isClosePre || this.isDoWhile) {
			this.out.append(";");
		}else{
			this.out.append(" {");
		}
	}
	
	/**
	 * 条件输出,不包含最外部的()
	 * @return
	 */
	String getCondOut(){
		StringBuilder sb = new StringBuilder();
		boolean isSelf = true;
		if (!this.addIfs.isEmpty()) {
			for (Iterator<IfSentence> it = this.addIfs.iterator(); it.hasNext();) {
				IfSentence ifs = it.next();
				if (ifs.getLineNum() < 0) {
					sb.append(ifs.getLine());
					continue;
				}
				if (isSelf) {
					if (isBoolean) {
						sb.append(this.boolVal);
					}
					sb.append(this.left);
					if (!isBoolean) {
						sb.append(comp).append(this.right);
					}
					isSelf = false;
				}
				sb.append(" ").append(ifs.getAddIfLogic()).append(" ");
				sb.append(ifs.getCondOut());
			}
		}else{
			if (isBoolean) {
				sb.append(this.boolVal);
			}
			sb.append(this.left);
			if (!isBoolean) {
				sb.append(comp).append(this.right);
			}
		}
		return sb.toString();
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getOut()
	 */
	@Override
	public String getOut() {
		if (customOut) {
			return super.getOut();
		}
		this.out = new StringBuilder();
		if (isDebug) {
			String t = StringUtil.intToLetter((this.cond.charAt(this.cond.indexOf("_")+1)-48));
			this.out = new StringBuilder("if-").append(t).append(" - ");
		}
		this.render();
		return super.getOut();
	}

	
	
	/**
	 * @return the isToReturn
	 */
	public final boolean isToReturn() {
		return isToReturn;
	}

	/**
	 * @param isToReturn the isToReturn to set
	 */
	public final void setToReturn(boolean isToReturn) {
		this.isToReturn = isToReturn;
	}

	/**
	 * 在条件中加上括号保护
	 */
	void addCondProtect(){
//		condProtect = true;
		IfSentence prePro = new IfSentence(this.mgr, "(");
		IfSentence afterPro = new IfSentence(this.mgr, ")");
		this.addIfs.add(0, prePro);
		this.addIfs.add(afterPro);
	}
	
	/**
	 * 合并if条件
	 * @param logicAnd true为&&,false为||
	 * @param ifs 后面的IfSentence
	 */
	public void mergeIf(boolean logicAnd,IfSentence ifs){
		if (this.getLineNum() == ifs.getLineNum()) {
			log.error(this.mgr.getMeth().getName()+" - mergeIf error! lineNum is same:"+ifs.getLineNum());
			return;
		}
		if (logicAnd) {
			ifs.setAddIfLogic("&&");
		}else{
			ifs.setAddIfLogic("||");
		}
		ifs.setType(TYPE_NOT_LINE);
		ifs.over();
		this.isReversed = true;
		this.addIfs.add(ifs);
	}
	
	
	public void setWhile(){
		this.ifCond = "while";
		
	}
	
	public void setDoWhile(){
		this.ifCond = "while";
		this.isDoWhile = true;
	}
	
	public void setAsIf(){
		this.ifCond = "if";
		this.isReversedWhile = false;
		this.isDoWhile = false;
		
	}

	public boolean isWhile(){
		if (this.ifCond.equals("while")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 多条件时附加上的if条件语句
	 */
	private ArrayList<IfSentence> addIfs = new ArrayList<IfSentence>();
	
	/**
	 * 作为增加的条件时，与原条件的逻辑关系(只能是 && 或 ||)
	 */
	private String addIfLogic  = "";
	
	/**
	 * 是否为else if
	 */
	private boolean isElse = false;
	
	/**
	 * 是否需要在前面添加上一语句的关闭}号,如为do while的条件或} else if时
	 */
	private boolean isClosePre = false;
	
	/**
	 * @return the isElse
	 */
	public final boolean isElse() {
		return isElse;
	}

	/**
	 * @param isElse the isElse to set
	 */
	public final void setElse(boolean isElse) {
		this.isElse = isElse;
//		if (isElse) {
//			this.out = new StringBuilder();
//		}
	}
	
	/**
	 * @return the addIfComp
	 */
	public final String getAddIfLogic() {
		return addIfLogic;
	}

	/**
	 * @param addIfComp the addIfComp to set
	 */
	public final void setAddIfLogic(String addIfLogic) {
		this.addIfLogic = addIfLogic;
	}

	/**
	 * 比较条件反向,只能有效反一次
	 */
	public void reverseCompare(){
		if (isReversed) {
			return;
		}
		isReversed = true;
		if (isBoolean) {
			if (this.boolVal.equals("!")) {
				this.boolVal = "";
			}else{
				this.boolVal = "!";
			}
		}else if (this.comp.equals(" == ")) {
			this.comp = " != ";
		}else if(this.comp.equals(" > ")){
			this.comp = " <= ";
		}else if(this.comp.equals(" < ")){
			this.comp = " >= ";
		}else if(this.comp.equals(" >= ")){
			this.comp = " < ";
		}else if(this.comp.equals(" <= ")){
			this.comp = " > ";
		}else if(this.comp.equals(" != ")){
			this.comp = " == ";
		}
		//this.out = new StringBuilder();
		//this.render();
	}


	/**
	 * @return the isClosePre
	 */
	public final boolean isClosePre() {
		return isClosePre;
	}
	
	/**
	 * @param set isClosePre
	 */
	public final void setClosePre(boolean isClosePre) {
		this.isClosePre = isClosePre;
	}

	/**
	 * @param isDoWhile the isDoWhile to set
	 */
	public final void setDoWhile(boolean isDoWhile) {
		if (isDoWhile) {
			this.isClosePre = isDoWhile;
			this.ifCond = "while";
			this.isReversed = true;
		}else{
			this.isClosePre = false;
			if (this.isWhile()) {
				this.ifCond = "while";
			}else{
				this.ifCond = "if";
			}
		}
	}

	
	/**
	 * @return the customOut
	 */
	public final boolean isCustomOut() {
		return customOut;
	}



	/**
	 * @param customOut the customOut to set
	 */
	public final void setCustomOut(boolean customOut) {
		this.customOut = customOut;
	}



	/**
	 * @return the isReversedWhile
	 */
	public final boolean isReversedWhile() {
		return isReversedWhile;
	}



	/**
	 * 
	 */
	public final void setReversedWhile() {
		this.isReversedWhile = true;
		this.ifCond = "while";
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new IfSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "if";
	}
	

	/**
	 * @return the endSenLineNum
	 */
	public final int getEndSenLineNum() {
		return endSenLineNum;
	}

	/**
	 * @param endSenLineNum the endSenLineNum to set
	 */
	public final void setEndSenLineNum(int endSenLineNum) {
		this.endSenLineNum = endSenLineNum;
	}

	/**
	 * @return the condTag
	 */
	public final TagSentence getCondTag() {
		return condTag;
	}

	/**
	 * @param condTag the condTag to set
	 */
	public final void setCondTag(TagSentence condTag) {
		this.condTag = condTag;
	}
	

	/**
	 * @param isReversed the isReversed to set
	 */
	public final void setReversed(boolean isReversed) {
		this.isReversed = isReversed;
	}

	/**
	 * @return the addIfs
	 */
	public final ArrayList<IfSentence> getAddIfs() {
		return addIfs;
	}

	/**
	 * @return the cond
	 */
	public final String getCond() {
		return cond;
	}

	/**
	 * @param cond the cond to set
	 */
	public final void setCond(String cond) {
		this.cond = cond;
	}

	/**
	 * @return the isReversed
	 */
	public final boolean isReversed() {
		return isReversed;
	}

	static final String[] KEYS = new String[]{
		"if-eq", 
		"if-ne", 
		"if-lt", 
		"if-ge", 
		"if-gt", 
		"if-le", 
		"if-eqz", 
		"if-nez", 
		"if-ltz", 
		"if-gez", 
		"if-gtz", 
		"if-lez"
	};
}
