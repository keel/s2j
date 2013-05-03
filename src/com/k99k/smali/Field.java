/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class Field extends Context {


	public Field(S2J s2j, ArrayList<String> lines) {
		super(s2j, lines);
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
	
	private boolean isStatic = false;
	
	private boolean isFinal = false;
	
	private String scope = "";
	
	private String defaultValue = "";
	
	private ArrayList<String> annotations;

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
			
			this.out.append(this.objType).append(" ").append(this.name);
			if (!this.defaultValue.equals("")) {
				this.out.append(" = ").append(this.defaultValue);
			}else if(this.isFinal){
				//final但没有defaultValue的成员变量，默认为0
				this.out.append(" = ").append("0");
			}
			this.out.append(";").append(StaticUtil.NEWLINE);
			if (this.annotations != null) {
				for (int i = 0; i < this.annotations.size(); i++) {
					this.out.append("// ");
					this.out.append(this.annotations.get(i));
					this.out.append(StaticUtil.NEWLINE);
				}
			}
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
		int vpo = 0;
		for (int i = 1; i < len; i++) {
			if (words[i].indexOf(":") < 0) {
				this.scope += words[i]+" ";
				if (words[i].equals("static")) {
					this.isStatic = true;
				}else if(words[i].equals("final")){
					this.isFinal = true;
				}
			}else{
				vpo = i;
				break;
			}
		}
		String[] objstr = words[vpo].split(":");
		if (objstr.length != 2) {
			this.err = "//ERR: fields objstr error. line:"+l;
			return false;
		}
		this.name = objstr[0];
		this.objType = Tool.parseObject(objstr[1]);
		if (l.indexOf("=")>0) {
			this.defaultValue = words[words.length-1];
		}
		//加入到s2j
		this.s2j.addField(this.name, this);
		
		//判断下一个是不是.annotation
		l = this.lines.get(0);
		if (l.indexOf(StaticUtil.TYPE_ANNOTATION)>=0) {
			annotations = new ArrayList<String>();
			while(!(l = this.lines.remove(0)).equals(StaticUtil.TYPE_FIELD_END)){
				this.annotations.add(l);
			}
		}
		
		return true;
	}


	
	/**
	 * @return the defaultValue
	 */
	public final String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public final void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the isStatic
	 */
	public final boolean isStatic() {
		return isStatic;
	}

	/**
	 * @return the isFinal
	 */
	public final boolean isFinal() {
		return isFinal;
	}

	
	
	
	@Override
	public Context newOne(S2J s2j, ArrayList<String> lines) {
		return new Field(s2j, lines);
	}

}
