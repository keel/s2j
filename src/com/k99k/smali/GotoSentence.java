/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class GotoSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public GotoSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_STRUCT;
	}

	static final Logger log = Logger.getLogger(GotoSentence.class);
	
	
	/**
	 * goto的目标
	 */
	private String target = "";
	
	/**
	 * TagSentence
	 */
	private TagSentence targetSen;
	
	private boolean isReturn = false;
	
	
	private boolean isContinue = false;
	
	private boolean isBreak = false;
	
	/**
	 * 是否为catch块的结尾
	 */
	private boolean isEndOfCatch = false;
	
	/**
	 * goto的前一句，用于处理return某个值对象， 这个对象一般会保存在前一句中
	 */
	private Sentence preSen;
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		String[] ws = this.line.split(" ");
		if (ws.length<2) {
			this.out.append("exec GotoSentence error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		this.target = ws[1];
		this.out.append("// ").append(this.line);
		
		//保存前一句，以用于return 某值的情况
		this.preSen = this.mgr.getLastSentence();
//		if (this.mgr.getLevel()>0) {
//			this.mgr.addLevel(-1);
//		}
		this.state = Sentence.STATE_DOING;
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#debug()
	 */
	@Override
	public boolean debug() {
		String tag = this.line.split(" ")[1];
		int nn = tag.charAt(tag.indexOf("_")+1);
		this.out.append("goto >>").append(StringUtil.intToLetter(nn-48));
		this.over();
		return true;
	}


	/**
	 * 设置为break语句
	 * @param label break的目标label，可为null
	 */
	public final void setBreak(String label,String info){
		this.isBreak = true;
		this.out.append(StaticUtil.NEWLINE).append("break ");
		if (StringUtil.isStringWithLen(label, 1)) {
			this.out.append(label);
		}
		this.out.append("; //");
		this.out.append(info);
		this.type = TYPE_STRUCT;
	}
	
	/**
	 * 设置为continue语句
	 * @param label continue的目标label，可为null
	 */
	public final void setContinue(String label,String info){
		this.isContinue = true;
		this.out.append(StaticUtil.NEWLINE).append("continue ");
		if (StringUtil.isStringWithLen(label, 1)) {
			this.out.append(label);
		}
		this.out.append("; //");
		this.out.append(info);
		this.type = TYPE_STRUCT;
	}
	
	
	
	
	/**
	 * @return the isContinue
	 */
	public final boolean isContinue() {
		return isContinue;
	}


	/**
	 * @return the isBreak
	 */
	public final boolean isBreak() {
		return isBreak;
	}


	public final boolean isReturn() {
		return isReturn;
	}
	
	public final void setReturn(boolean isReturn) {
		this.isReturn = isReturn;
		if (isReturn) {
			this.out.append(StaticUtil.NEWLINE).append(StaticUtil.TABS[this.level]);
			this.out.append("return ");
			if (this.mgr.getMeth().getReturnStr().equals("void")) {
				this.out.append(";");
			}else{
				this.out.append(this.preSen.getOut()).append(";");
			}
		}
	}
	
	


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new GotoSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "goto";
	}
	
	
	
	/**
	 * @return the preSen
	 */
	public final Sentence getPreSen() {
		return preSen;
	}


	/**
	 * @return the isEndOfCatch
	 */
	public final boolean isEndOfCatch() {
		return isEndOfCatch;
	}


	/**
	 * @param isEndOfCatch the isEndOfCatch to set
	 */
	public final void setEndOfCatch(boolean isEndOfCatch) {
		this.isEndOfCatch = isEndOfCatch;
	}


	/**
	 * @return the target
	 */
	public final String getTarget() {
		return target;
	}


	/**
	 * @return the targetSen
	 */
	public final TagSentence getTargetSen() {
		return targetSen;
	}

	/**
	 * @param targetSen the targetSen to set
	 */
	public final void setTargetSen(TagSentence targetSen) {
		this.targetSen = targetSen;
	}


	static final String KEY = "goto";

}
