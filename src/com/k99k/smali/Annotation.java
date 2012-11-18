/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * @author keel
 *
 */
public class Annotation extends Context {

	/**
	 * @param s2j
	 * @param lines
	 * @param out
	 */
	public Annotation(S2J s2j, ArrayList<String> lines) {
		super(s2j, lines);
	}
	
	private ArrayList<String> mLines = new ArrayList<String>();

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#getKey()
	 */
	@Override
	public String getKey() {
		return StaticUtil.TYPE_ANNOTATION;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#out()
	 */
	@Override
	public boolean out() {
		
		for (int i = 0; i < this.mLines.size(); i++) {
			this.out.append("// ");
			this.out.append(this.mLines.get(i));
			this.out.append(StaticUtil.NEWLINE);
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#parse()
	 */
	@Override
	public boolean parse() {
		//先获取第一行
		String l = this.lines.remove(0);
		//处理注释
		l = this.doComm(l);
		//再导入此方法内的所有行
		String ss = "";
		while(!(ss = this.lines.remove(0)).equals(StaticUtil.TYPE_ANNOTATION_END)){
			this.mLines.add(ss);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#newOne(com.k99k.smali.S2J, java.util.ArrayList, java.lang.StringBuilder)
	 */
	@Override
	public Context newOne(S2J s2j, ArrayList<String> lines) {
		return new Annotation(s2j, lines);
	}

}
