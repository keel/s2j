/**
 * 
 */
package com.k99k.smali;

/**
 * @author keel
 *
 */
public class Comm extends Context {
	
	public Comm(S2J s2j, Context superContext) {
		super(s2j, superContext);
	}

	private String comm;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#getKey()
	 */
	@Override
	public String getKey() {
		return StaticUtil.COMM;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#out()
	 */
	@Override
	public boolean out() {
		this.out.append("//").append(this.comm).append(StaticUtil.NEWLINE);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#parse()
	 */
	@Override
	public boolean parse() {
		String l = this.lines.remove(0);
		this.comm = l.substring(l.indexOf("#")+1);
		return true;
	}

	@Override
	public Context newOne(S2J s2j,Context superContext) {
		return new Comm(s2j,superContext);
	}

}
