/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

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
	
	/**
	 * 准备输出的行
	 */
	private ArrayList<String> outLines = new ArrayList<String>();

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line= this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		String key = ws[0];
		//先生成条件行
		if (key.indexOf('z') >= 0) {
			//与0比较
			
			
			
		}else{
			//两对象比较
			
			
			
		}
		
		
		//缩进和cNum增加
		this.mgr.addLevel(1);
		this.mgr.addCNum(0);
		//
		String nextLine = this.mgr.getSrcline(this.lineNum+1);
		
		
		//注意else if的情况
		
		//注意判断是否是for或while结构
		this.outLines.add("// if");
		this.over();
		return true;
	}
	
	

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getOutLines()
	 */
	@Override
	public ArrayList<String> getOutLines() {
		return this.outLines;
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
