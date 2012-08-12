/**
 * 
 */
package com.k99k.smali;

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
		this.type = Sentence.TYPE_LINE;
	}
	
	private Var v = new Var(this);
	
	private String[] arrRang = null;

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
			System.err.println(this.out);
			return false;
		}
		String obj = "";
		if (ws[0].equals("new-instance")) {
			obj = Tool.parseObject(ws[2]);
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-instance");
			v.setOut(obj);
		}else if(ws[0].equals("new-array")){
			obj = Tool.parseObject(ws[3]);
			//加入初始化数量
			StringBuilder sb = new StringBuilder(obj);
			int po = sb.indexOf("[");
			sb.insert(po+1, this.mgr.getVar(ws[2]).getOut());
//			sb.delete(slen-1, slen);
//			sb.append(this.mgr.getVar(ws[2]).getOut()).append("]");
			obj = sb.toString();
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("new-array");
			v.setOut(obj);
		}else if(ws[0].equals("filled-new-array")){
			int rangStart = this.line.indexOf("{")+1;
			int rangEnd = this.line.indexOf("}");
			
			String ss = this.line.substring(rangStart,rangEnd);
			//确定变量集
			String[] rang = ss.split(" ");
			this.arrRang = new String[rang.length];
			obj = Tool.parseObject(ws[ws.length-1]);
			StringBuilder sb = new StringBuilder(obj);
			sb.delete(sb.indexOf("["), sb.length());
			for (int i = 0; i < rang.length; i++) {
				String sout = this.mgr.getVar(rang[i]).getOut();
				this.arrRang[i] = sout;
			}
			obj = sb.toString();
			
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("filled-new-array");
			v.setOut(obj);
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
			sb.delete(sb.indexOf("["), sb.length());
			for (int i = 0; i < rang.length; i++) {
				String sout = this.mgr.getVar(rang[i]).getOut();
				this.arrRang[i] = sout;
			}
			obj = sb.toString();
			v.setClassName(obj);
			v.setName(ws[1]);
			v.setKey("filled-new-array-range");
			v.setOut(obj);
		}
		this.mgr.setVar(v);
		this.over();
		return true;
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
