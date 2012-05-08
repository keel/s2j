/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * @author keel
 *
 */
public abstract class Context {

	public Context(S2J s2j,ArrayList<String> lines,StringBuilder out) {
		this.s2j = s2j;
		this.lines = lines;
		this.out = out;
	}
	
	ArrayList<String> lines;
	
	StringBuilder out;
	
	String err = "//ERR ";
	
	S2J s2j;
	
	
	/**
	 * 处理标识
	 * @return
	 */
	public abstract String getKey();
	
	/**
	 * 输出
	 * @return 输出是否成功
	 */
	public abstract boolean out();
	
	/**
	 * 解析目标行
	 * @return 
	 */
	public abstract boolean parse();

	/**
	 * @return 错误
	 */
	public String getErr(){
		return this.err;
	}

	/**
	 * 处理过程
	 * @return
	 */
	public void render() {
//		if (lines.size() > 0) {
			if (this.parse() && this.out()) {
//				this.next();
//				this.outEnd();
			} else {
				// 输出错误
				this.out.append(this.getErr()).append(StaticUtil.NEWLINE);
//				this.next();
			}
//		}
	}
	
//	/**
//	 * 执行下一个处理
//	 */
//	final void next(){
//		if (this.lines.isEmpty()) {
//			return;
//		}
//		String nextLine = this.lines.get(0);
//		String key = Tool.getKey(nextLine);
//		if (key != null) {
//			Context next = this.s2j.createContext(key,this);
//			if (next!=null) {
//				next.render(this.lines,this.out);
//			}else{
//				this.out.append("//F IXME context not found! key: "+key);
//			}
//		}else{
//			this.out.append("//F IXME key is empty! line: "+nextLine);
//		}
//	}
	
//	/**
//	 * 输出结束字符,有需要的Context重写此方法即可
//	 */
//	public void outEnd(){
//		//this.out.append(StaticUtil.NEWLINE);
//	}
	
	public abstract Context newOne(S2J s2j,ArrayList<String> lines,StringBuilder out);
	
	/**
	 * 处理一行中的注释
	 * @param l
	 * @return
	 */
	public final String doComm(String l){
		int c = l.indexOf(StaticUtil.COMM);
		if (c == -1) {
			return l;
		}
		if (c == 0) {
			this.out.append("//").append(l).append(StaticUtil.NEWLINE);
			return "";
		}
		if (c>0 && l.length()>c+1) {
			this.out.append("//").append(l.substring(c+1)).append(StaticUtil.NEWLINE);
		}
		return l.substring(0,c);
	}
	
//	public boolean insert(String str,int line){
//		int c = -1;
//		int l = 1;
//		while (true) {
//			int n = this.txt.indexOf("\n", c+1);
//			if (l == line) {
//				this.out.insert(n+1, str+StaticUtil.NEWLINE);
//				return true;
//			}
//			if (n == -1) {
//				return false;
//			}
//			c = n;
//			l++;
//		}
//	}
	
	
//	public String out(){
//		return out.toString();
//	}
	
//	/**
//	 * @return the maxLine
//	 */
//	public final int getMaxLine() {
//		return maxLine;
//	}
//
//	/**
//	 * @param maxLine the maxLine to set
//	 */
//	public final void setMaxLine(int maxLine) {
//		this.maxLine = maxLine;
//	}
	
	
	

}
