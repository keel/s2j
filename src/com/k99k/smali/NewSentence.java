/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class NewSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public NewSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		//可能会被其他语句需要，所以这里为可输出状态
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(NewSentence.class);
	
	private Var v = new Var(this);
	
	private String[] arrRang = null;
	
	/**
	 * 数组变量名
	 */
	private String arrName;
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//自身不设置输出，仅生成一个Var 
		
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		int len = ws.length;
		if (len < 3) {
			this.out.append("exec newSentence error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		String obj = "";
		if (ws[0].equals("new-instance")) {
			obj = Tool.parseObject(ws[2]);
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-instance");
			v.setOut("new "+obj);
		}else if(ws[0].equals("new-array")){
			obj = Tool.parseObject(ws[3]);
			//加入初始化数量
			StringBuilder sb = new StringBuilder(obj);
			int po = sb.indexOf("[");
			String size = this.mgr.getVar(ws[2]).getOut();
			sb.insert(po+1, size);
			String a = ws[3];
//			String aa = a.substring(a.indexOf("["),a.lastIndexOf("[")+1);
			String aa = a.substring(0,a.lastIndexOf("[")+1);
			int w = aa.length();
			v.setValue(obj);
			obj = sb.toString();
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-array");
			v.setOut("new "+obj);
//			//arrValue准备保存数组值
//			if (StringUtil.isDigits(size)) {
//				Object[] arrValue = new Object[Integer.parseInt(size)];
//				v.setValue(arrValue);
//			}
		}else if(ws[0].equals("filled-new-array")){
			int rangStart = this.line.indexOf("{")+1;
			int rangEnd = this.line.indexOf("}");
			
			String ss = this.line.substring(rangStart,rangEnd);
			//确定变量集
			String[] rang = ss.split(" ");
			this.arrRang = new String[rang.length];
			obj = Tool.parseObject(ws[ws.length-1]);
			StringBuilder sb = new StringBuilder(obj);
			//sb.delete(sb.indexOf("["), sb.length());
			sb.append("{");
			int delPo = sb.length();
			for (int i = 0; i < rang.length; i++) {
				String sout = this.mgr.getVar(rang[i]).getOut();
				this.arrRang[i] = sout;
				sb.append(",").append(sout);
			}
			sb.deleteCharAt(delPo).append("}");
			obj = sb.toString();
			
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("filled-new-array");
			v.setOut("new "+obj);
		}else if(ws[0].equals("filled-new-array-range")){
			int rangStart = this.line.indexOf("{")+1;
			int rangEnd = this.line.indexOf("}");
			
			String ss = this.line.substring(rangStart,rangEnd);
			//确定变量集
			String[] rang = ss.split(" ");
			if (rang.length == 3 && rang[1].equals("..")) {
				int start = Integer.parseInt(rang[0].substring(1));
				int end = Integer.parseInt(rang[2].substring(1));
				int rlen = end-start+1;
				char headChar = rang[0].charAt(0);
				rang = new String[rlen];
				for (int i = 0; i < rlen; i++) {
					rang[i] = headChar + String.valueOf(start);
					start++;
				}
			}
			this.arrRang = new String[rang.length];
			obj = Tool.parseObject(ws[ws.length-1]);
			StringBuilder sb = new StringBuilder(obj);
			//sb.delete(sb.indexOf("["), sb.length());
			sb.append("{");
			int delPo = sb.length();
			for (int i = 0; i < rang.length; i++) {
				String sout = this.mgr.getVar(rang[i]).getOut();
				this.arrRang[i] = sout;
				sb.append(",").append(sout);
			}
			sb.deleteCharAt(delPo).append("}");
			obj = sb.toString();
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("filled-new-array-range");
			v.setOut("new "+obj);
		}
		this.mgr.setVar(v);
		this.over();
		return true;
	}
	
	/**
	 * 是否被fill-array-data填充过
	 */
	private boolean isFilled = false;
	
	/**
	 * @return the isFilled
	 */
	public final boolean isFilled() {
		return isFilled;
	}

	/**
	 * @param isFilled the isFilled to set
	 */
	public final void setFilled(boolean isFilled) {
		this.isFilled = isFilled;
	}

	/**
	 * @return the arrName
	 */
	public final String getArrName() {
		return arrName;
	}


	/**
	 * @param arrName the arrName to set
	 */
	public final void setArrName(String arrName) {
		this.arrName = arrName;
	}



	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.v;
	}



	/**
	 * @return the arrRang
	 */
	public final String[] getArrRang() {
		return arrRang;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new NewSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "new";
	}
	
	static final String[] KEYS = new String[]{
		"new-instance", 
		"new-array", 
		"filled-new-array", 
		"filled-new-array-range"
	};

}
