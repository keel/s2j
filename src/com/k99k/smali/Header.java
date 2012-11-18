/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import com.k99k.tools.StringUtil;

/**
 * class header
 * @author keel
 *
 */
public class Header extends Context {

	


	public Header(S2J s2j, ArrayList<String> lines) {
		super(s2j, lines);
	}

	private String packageName;
	
	private String superName;
	
	private String source;
	
	private String scope = "";
	
	private String type;
	
	private String name;
	
	private String implementsStr;
	
	
	@Override
	public boolean parse() {
		//第一行
		String l = this.lines.remove(0);
		String[] words = l.split(" ");
		int len = words.length;
		if (len <2) {
			this.err = "//ERR: words lenght error. line: "+l;
			return false;
		}
		if (words[0].equals(StaticUtil.TYPE_CLASS)) {
			this.type = "class";
		}else if(l.indexOf("interface abstract")>-1){
			//interface处理
			this.type = "interface";
			l.replaceAll("interface abstract ", "");
			words = l.split(" ");
			len = words.length;
		}else{
			this.err = "//ERR: unkown class type: "+words[0];
			return false;
		}
		//scope
		if (len > 2 ) {
			for (int i = 1; i < len -1; i++) {
				this.scope+= words[i]+" ";
			}
		}
		//packageName,name
		String last = words[len-1];
		String obj = Tool.parseObject(last);
		int p = obj.lastIndexOf('.');
		if (p == -1) {
			this.err = "//ERR: parse packageName and name failed. line:"+l;
			return false;
		}
		this.packageName = obj.substring(0,p);
		this.name = obj.substring(p+1);
		//className
		this.s2j.className = this.name.substring(this.name.lastIndexOf("\\.")+1);
		this.s2j.packageName = this.packageName;
		
		//第二行,super
		l = this.lines.remove(0);
		words = l.split(" ");
		len = words.length;
		if (Tool.getKey(l).equals(StaticUtil.TYPE_SUPER) && len == 2) {
			this.superName = Tool.parseObject(words[1]);
		}else{
			this.err = "//ERR: parse super failed. line: "+l;
			return false;
		}
		
		//第三行,source
		l = this.lines.remove(0);
		words = l.split(" ");
		len = words.length;
		if (Tool.getKey(l).equals(StaticUtil.TYPE_SOURCE) && len == 2) {
			this.source = words[1].substring(1,len-1);
		}else{
			this.err = "//ERR: parse source failed. line: "+l;
			return false;
		}
		
		//处理implements
		l = this.lines.get(0);
		if (Tool.getKey(l).equals(StaticUtil.COMM) && l.indexOf("interfaces")>-1) {
			this.lines.remove(0);
			l = this.lines.get(0);
			this.implementsStr = "";
			while (Tool.getKey(l).equals(StaticUtil.TYPE_IMPLEMENTS)) {
				l = this.lines.remove(0);
				this.implementsStr += ","+Tool.parseObject(l.split(" ")[1]);
				l = this.lines.get(0);
			}
			this.implementsStr = this.implementsStr.substring(1);
		}
		
		return true;
	}
	
	@Override
	public boolean out(){
		if (StringUtil.isStringWithLen(this.packageName, 1)
			&&StringUtil.isStringWithLen(this.type, 1)
			&&StringUtil.isStringWithLen(this.name, 1)) {
			
			StringBuilder sb = new StringBuilder();
			sb.append("package ");
			sb.append(this.packageName);
			sb.append(";").append(StaticUtil.NEWLINE);
			sb.append(StaticUtil.NEWLINE).append(StaticUtil.NEWLINE);
			sb.append(this.scope);
			sb.append(this.type).append(" ");
			sb.append(this.name).append(" ");
			if (!this.superName.equals("java.lang.Object")) {
				sb.append("extends ").append(this.superName).append(" ");
			}
			if (this.implementsStr !=null) {
				sb.append("implements ").append(this.implementsStr);
			}
			sb.append(" {");
			sb.append(StaticUtil.NEWLINE).append(StaticUtil.NEWLINE);
			this.out.append(sb);
			return true;
		}else{
			this.err = "//ERR: some prop missed. props:"+this.packageName+","+this.name+","+this.type;
			return false;
		}
	}
	
	
	
	/**
	 * @return the source
	 */
	public final String getSource() {
		return source;
	}

	@Override
	public String getKey() {
		return StaticUtil.TYPE_CLASS;
	}

	@Override
	public Context newOne(S2J s2j, ArrayList<String> lines) {
		return new Header(s2j,lines);
	}

}
