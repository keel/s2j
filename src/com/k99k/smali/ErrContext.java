/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * 输出错误消息的Context
 * @author keel
 *
 */
public class ErrContext extends Context {

	/**
	 * @param s2j
	 * @param lines
	 */
	public ErrContext(S2J s2j, ArrayList<String> lines) {
		super(s2j, lines);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#getKey()
	 */
	@Override
	public String getKey() {
		return "err";
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#out()
	 */
	@Override
	public boolean out() {
		this.out.append(this.err).append(StaticUtil.NEWLINE);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#parse()
	 */
	@Override
	public boolean parse() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#newOne(com.k99k.smali.S2J, java.util.ArrayList)
	 */
	@Override
	public Context newOne(S2J s2j, ArrayList<String> lines) {
		return new ErrContext(s2j, lines);
	}

}
