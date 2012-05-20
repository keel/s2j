/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
	}
	
//	/**
//	 * 准备输出的行
//	 */
//	private ArrayList<String> outLines = new ArrayList<String>();
//	
	static HashMap<String,String> compareMap = new HashMap<String, String>();
	static{
		compareMap.put("if-eq"," = ");
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
	
	private String boolVal = "";
	
	/**
	 * if 指向的cond_x的Sentence
	 */
	private Sentence condTag;
	
	/**
	 * if内部结束位置指到的Sen所在的lineNum
	 */
	private int endSenLineNum;
	
	/**
	 * if 指向的cond_x
	 */
	private String cond;
	
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
			System.err.println(this.out);
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
			this.right = "0";
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
			this.right = v2.getOut();
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
		
		this.render();
		
		//缩进增加
		//this.mgr.addLevel(1);
		//
		
		
		//注意else if的情况
		
		//注意判断是否是for或while结构
		
		
		//FIXME this.setState(STATE_DOING);
		this.over();
		return true;
	}
	
	private void render(){
		if (isElse) {
			this.out.append("else ");
		}
		this.out.append("if (");
		if (isBoolean) {
			this.out.append(this.boolVal);
		}
		this.out.append(this.left);
		if (!isBoolean) {
			this.out.append(comp).append(this.right);
		}
		if (!this.addIfs.isEmpty()) {
			for (Iterator<IfSentence> it = this.addIfs.iterator(); it.hasNext();) {
				IfSentence ifs = it.next();
				String ao = ifs.getOut();
				this.out.append(" ").append(ifs.getAddIfLogic());
				this.out.append(ao.substring(2,ao.length()-2));
			}
		}
		this.out.append(") {");
	}
	
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
		if (isElse) {
			this.out = new StringBuilder();
			this.render();
		}
	}

	public void addIF(IfSentence sen){
		this.addIfs.add(sen);
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
	 * 比较条件反向
	 */
	public void reverseCompare(){
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
		this.out = new StringBuilder();
		this.render();
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new IfSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getType()
	 */
	@Override
	public int getType() {
		return Sentence.TYPE_STRUCT;
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
	public final Sentence getCondTag() {
		return condTag;
	}

	/**
	 * @param condTag the condTag to set
	 */
	public final void setCondTag(Sentence condTag) {
		this.condTag = condTag;
	}
	

	/**
	 * @return the cond
	 */
	public final String getCond() {
		return cond;
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
