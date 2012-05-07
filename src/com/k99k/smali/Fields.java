/**
 * 
 */
package com.k99k.smali;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Fields extends Context {

	public Fields(S2J s2j, Context superContext) {
		super(s2j, superContext);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#getKey()
	 */
	@Override
	public String getKey() {
		return StaticUtil.TYPE_FIELD;
	}
	
	private String objType;
	
	private String name;
	
	private String scope = "";

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#out()
	 */
	@Override
	public boolean out() {
		if (StringUtil.isStringWithLen(this.name, 1) 
			&& StringUtil.isStringWithLen(this.objType, 1)) {
			if (StringUtil.isStringWithLen(this.scope, 1)) {
				this.out.append(this.scope);
			}
			
			this.out.append(this.objType).append(" ").append(this.name).append(";").append(StaticUtil.NEWLINE);
			return true;
		}else{
			this.err = "//ERR: some prop missed. props:"+this.objType+","+this.name;
			return false;
		}
		
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#parse()
	 */
	@Override
	public boolean parse() {
		String l = this.lines.remove(0);
		String[] words = l.split(" ");
		int len = words.length;
		for (int i = 1; i < len-1; i++) {
			this.scope += words[i]+" ";
		}
		String[] objstr = words[len-1].split(":");
		if (objstr.length != 2) {
			this.err = "//ERR: fields objstr error. line:"+l;
			return false;
		}
		this.name = objstr[0];
		this.objType = Tool.parseObject(objstr[1]);
		return true;
	}

	@Override
	public Context newOne(S2J s2j,Context superContext) {
		return new Fields(s2j,superContext);
	}

}
