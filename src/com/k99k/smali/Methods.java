/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;
import java.util.Iterator;

import com.k99k.tools.StringUtil;


/**
 * @author keel
 *
 */
public class Methods extends Context {

	public Methods(S2J s2j, ArrayList<String> lines, StringBuilder out) {
		super(s2j, lines, out);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#getKey()
	 */
	@Override
	public String getKey() {
		return StaticUtil.TYPE_METHOD;
	}
	
	private ArrayList<String> mLines = new ArrayList<String>();
//	private boolean isConstructor = false;
	private boolean isStaticConstructor = false;
	private String scope = "";
	private String name;
	/**
	 * 方法参数
	 */
	private ArrayList<String> props = new ArrayList<String>();
	/**
	 * 方法返回
	 */
	private String returnStr = "";
	
	/**
	 * 准备输出的行
	 */
	private ArrayList<String> outLines = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Context#out()
	 */
	@Override
	public boolean out() {
		if (StringUtil.isStringWithLen(this.name, 1) ) {
			
			//最后全部输出
			for (Iterator<String> it = this.outLines.iterator(); it.hasNext();) {
				String s = it.next();
				this.out.append(s).append(StaticUtil.NEWLINE);
			}
			this.out.append(StaticUtil.NEWLINE).append("}").append(StaticUtil.NEWLINE);
			return true;
		}else{
			this.err = "//ERR: Method,some prop missed. props:"+this.returnStr+","+this.name;
			return false;
		}
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
		while(!(ss = this.lines.remove(0)).equals(StaticUtil.TYPE_END_METHOD)){
			this.mLines.add(ss);
		}
		try {
			
			//处理方法首行
			this.parseFn(l);
			
			//方法内的部分
			this.parseInner();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			this.err = "//ERR: parse method failed! mline:"+l;
			return false;
		}
		
		return true;
	}
	
	/**
	 * 处理方法内的部分
	 */
	private void parseInner(){
		if (!this.mLines.isEmpty()) {
			SentenceMgr sMgr = new SentenceMgr(this.mLines);
			if (this.scope.indexOf("static")>=0) {
				sMgr.setStatic(true);
			}
			sMgr.execLines();
			this.outLines.addAll(sMgr.getOutLines());
		}
	}
	
	/**
	 * 处理方法首行
	 * @param l
	 */
	private void parseFn(String l){
		// static方法
		if (l.indexOf(StaticUtil.SCOPE_STATIC) > -1
				&& l.indexOf(StaticUtil.SCOPE_CONSTRUCTOR) > -1) {
			this.isStaticConstructor = true;
			this.name = StaticUtil.SCOPE_STATIC;
			this.scope = "";
			this.returnStr = "";
			l = l.replaceAll(" " + StaticUtil.SCOPE_STATIC, "").replaceAll(
					" " + StaticUtil.SCOPE_CONSTRUCTOR, "");
		}
		// 构造方法
		else if (l.indexOf(StaticUtil.SCOPE_CONSTRUCTOR) > -1) {
//			this.isConstructor = true;
			this.name = this.s2j.className;
			this.returnStr = "";
			l = l.replaceAll(" " + StaticUtil.SCOPE_CONSTRUCTOR, "");
		}
		// 普通方法
		else {
			this.returnStr = Tool
					.parseObject(l.substring(l.lastIndexOf(")") + 1));
			this.name = l.substring(l.lastIndexOf(" ") + 1, l.lastIndexOf("("));
		}
		// 处理scope
		String[] words = l.split(" ");
		int len = words.length;
		if (!isStaticConstructor) {
			for (int i = 1; i < len - 1; i++) {
				this.scope += words[i] + " ";
			}
		}
		// 处理参数
		this.props = getMethodProps(l);
		// scope
		StringBuilder sb = new StringBuilder();
		if (StringUtil.isStringWithLen(this.scope, 1)) {
			sb.append(this.scope);
		}
		// return
		if (StringUtil.isStringWithLen(this.returnStr, 1)) {
			sb.append(this.returnStr).append(" ");
		}
		sb.append(this.name);
		if (!isStaticConstructor) {
			sb.append("(");
		}
		
		// 读取接下来几行，处理参数
		String ss = "";
		int prCount = 0;
		while ((ss = this.mLines.remove(0)).indexOf(StaticUtil.TYPE_PROLOGUE) < 0 && ss.indexOf(StaticUtil.TYPE_END_METHOD)==-1) {
			ss = this.doComm(ss);
			String key = Tool.getKey(ss);
			if (key.equals(StaticUtil.TYPE_PARAMETER)) {
				words = ss.split(" ");
				if (words.length == 2) {
					String pr = words[1].replaceAll("\"", "");
					String s = this.props.get(prCount);
					this.props.set(prCount, s.substring(0,s.indexOf(" "))+" "+pr);
					prCount++;
				}
			} 
			//无需处理.locals
//			else if (key.equals(StaticUtil.TYPE_LOCALS)) {
//				words = ss.split(" ");
//				if (words.length >= 2 && StringUtil.isDigits(words[1])) {
//					this.locals = new HashMap<String, Object>(Integer.parseInt(words[1])+5);
//				}
//			}
		}
			
		//输出参数
		StringBuilder sb2 = new StringBuilder("");
		if (!this.props.isEmpty()) {
			//int i = 0;
			for (Iterator<String> it = this.props.iterator(); it.hasNext();) {
				String p = it.next();
				sb2.append(",").append(p);
				//sb2.append(",").append(p).append(" ").append("$$").append(i);
				//i++;
			}
			sb2.deleteCharAt(0);
		}
		sb.append(sb2);
		if (!isStaticConstructor) {
			sb.append(")");
		}
		sb.append("{");
		// 方法首行第一次完成
		this.outLines.add(sb.toString());
	}
	
	/**
	 * 获取行中的参数集
	 * @param line
	 * @return ArrayList props
	 */
	public static final ArrayList<String> getMethodProps(String line){
		ArrayList<String> props = new ArrayList<String>();
		// 处理参数
		int a = line.indexOf("(") + 1;
		int b = line.indexOf(")");
		if (a < b) {
			// 有props
			String p = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
			int plen = p.length(),pp = 0;
			for (int j = 0; j < plen; j++) {
				if (p.charAt(j) == 'L') {
					int e = p.indexOf(";", j);
					String cp = p.substring(j, e + 1);
					// 预先将参数类型加入porps,如: int $$1,java.lang.String $$2
					props.add(Tool.parseObject(cp)+" $$"+pp);
					j = e;
				} else if(p.charAt(j) == '['){
					int e = j;
					while(p.charAt(e) == '['){
						e++;
					}
					if (p.charAt(e) == 'L') {
						e = p.indexOf(";",e);
					}
					String cp = p.substring(j, e + 1);
					props.add(Tool.parseObject(cp)+" $$"+pp);
					j = e;
				}else {
					props.add(Tool.parseObject(String.valueOf(p.charAt(j)))+" $$"+pp);
				}
				pp++;
			}
			
		}
		return props;
	}

	@Override
	public Context newOne(S2J s2j, ArrayList<String> lines, StringBuilder out) {
		return new Methods(s2j, lines, out);
	}
	


}
