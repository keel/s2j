/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.k99k.tools.RandomUtil;
import com.k99k.tools.StringUtil;

/**
 * @author keel
 *
 */
public class PutSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public PutSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(PutSentence.class);
	
	/**
	 * 如果是数组赋值,在此保存数组对应的源name，如: v1
	 */
	private String arrSourceVar = null;

	/**
	 * 等号右边的值
	 */
	private String rightValue = null;
	
	/**
	 * 等号左边的表达式
	 */
	private String left = "";
	
	/**
	 * 对于数组，在left的左边还需要加上arrLeft
	 */
	private String arrLeft = "";
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		//解析
		this.doComm(this.line);
		//去除,号
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws.length<3) {
			this.out.append("exec putSentence error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		String key = ws[0];
		//第一个表示其类型:array,instance,static
		char type = key.charAt(0);
//		Var v = new Var(this);
		v.setKey(key);
		if (type == 'i') {
			Var v1 = this.mgr.getVar(ws[2]);
			Var v2 = this.mgr.getVar(ws[1]);
			int p = ws[3].indexOf(':');
			String name = ws[3].substring(ws[3].indexOf("->")+2,p);
//			v.setName(ws[2]);
			String obj = Tool.parseObject(ws[3].substring(p+1));
//			v.setClassName(obj);
//			v.setOut(v1.getOut()+"."+name);
			this.left = v1.getOut()+"."+name;
			//需要确认右边的输出
			String right = Var.varOut(obj, v2.getOut());
//			if (String.valueOf(v2.getValue()).equals("0")){
//				right = Var.checkIout(obj, "0") +" /* " + v2.getOut() +" */";
//			}else{
//				right = Var.checkIout(obj, v2.getOut());
//			}
//			if (obj.equals("int") && (!v2.getClassName().equals("int")) && StringUtil.isDigits(v2.getValue())) {
//				right = String.valueOf(v2.getValue());
//			}
			if (left.equals(right)) {
				this.out.append("//");
			}
//			this.out.append(left).append(" = ").append(right);
			this.rightValue = right;
			
//			v.setValue(right);
			v = v2;
			//对于v1引用的语句，如果不成行则可去除
			Sentence s = v1.getSen();
			if (s != null && s.getState()>=Sentence.STATE_DONE && s.getType() == Sentence.TYPE_NOT_LINE) {
				s.over();
				this.mgr.removeSentence(s);
			}
		}else if(type == 's'){
			Var v2 = this.mgr.getVar(ws[1]);
			int p = ws[2].indexOf(':');
			int p2 = ws[2].indexOf('>');
			String name = ws[2].substring(p2+1,p);
			String v1 = Tool.parseObject(ws[2].substring(0,p2-1));
			this.left = v1+"."+name;
			//this.rightValue = v2.getOut();
			
			//需要确认右边的输出
			String obj = Tool.parseObject(ws[2].substring(p+1));
//			String right = null;
//			if (String.valueOf(v2.getValue()).equals("0")){
//				right = Var.checkIout(obj, "0") +" /* " + v2.getOut() +" */";
//			}else{
//				right = Var.checkIout(obj, v2.getOut());
//			}
//			if (obj.equals("int") && (!v2.getClassName().equals("int")) && StringUtil.isDigits(v2.getValue())) {
//				right = String.valueOf(v2.getValue());
//			}
			this.rightValue = Var.varOut(obj, v2.getOut());
			
			v = v2;
			if (v2.getKey().equals("new-array")) {
				//多维数组赋值 
				NewSentence v2Sen = (NewSentence) v2.getSen();
				if (v2Sen.getArrName() != null) {
					this.setRightValue(v2Sen.getArrName());
				}
			}
		}else if(type == 'a'){
			Var v1 = this.mgr.getVar(ws[2]);
			Var v2 = this.mgr.getVar(ws[3]);
			Var v3 = this.mgr.getVar(ws[1]);
			this.arrSourceVar = ws[2];
//			String name = ws[1];
//			v.setName(name);
//			v.setClassName(v1.getClassName());
//			v.setOut(v1.getOut()+"["+v2.getValue()+"]");
			String index = (v2.getValue() == null) ? v2.getOut() :v2.getValue().toString();
			this.left = "["+index+"]";
			this.arrLeft = v1.getOut();
			this.rightValue = v3.getOut();
//			if (this.mgr.getMeth().isConstructor() || this.mgr.getMeth().isStaticConstructor()) {
				if (v3.getKey().equals("new-array")) {
					//多维数组赋值 
					NewSentence v3Sen = (NewSentence) v3.getSen();
					String v3Out = v3.getOut();
					if (v3Out.startsWith("new ")) {
						String le = v3.getValue().toString();
						String vv = v3.getName()+v2.getName()+RandomUtil.getRandomInt(1, 99);
						if (!v3Sen.isFilled()) {
							v3Sen.setOut(le.trim()+" "+vv+" = "+v3Out);
						}
						v3Sen.setArrName(vv);
						this.setRightValue(vv);
					}
				}else if(v1.getKey().equals("new-array")){
					NewSentence v1Sen = (NewSentence) v1.getSen();
					this.arrNewSen = v1Sen;
					if (v1Sen.getArrName() == null) {
						Var nv = v1Sen.getVar();
						String vv = v1.getName()+v2.getName()+RandomUtil.getRandomInt(1, 99);
						if (!v1Sen.isFilled()) {
							v1Sen.setOut(nv.getValue()+" "+vv+" = "+nv.getOut());
						}
						v1Sen.setArrName(vv);
					}
				}
//			}
				else{
					//需要确认右边的输出
					String obj = v1.getClassName().replaceAll("\\[\\]", "");
//					String right = null;
//					if (String.valueOf(v3.getValue()).equals("0")){
//						right = Var.checkIout(obj, "0") +" /* " + v3.getOut() +" */";
//					}else{
//						right = Var.checkIout(obj, v3.getOut());
//					}
//					if (obj.equals("int") && (!v3.getClassName().equals("int")) && StringUtil.isDigits(v3.getValue())) {
//						right = String.valueOf(v3.getValue());
//					}
					this.rightValue = Var.varOut(obj, v3.getOut());
				}
			
			//TODO 对于数组中的索引对象,如果是VarSentence,暂时先不removeSentence,仅标为over,可能会有其他地方用到
			//其他情况不进行处理
			Sentence s = v2.getSen();
			if (s != null && (s.getName().equals("var") || s.getName().equals("get"))) {
				s.type = Sentence.TYPE_NOT_LINE;
				s.over();
			}
//			s = v1.getSen();
//			if (s != null && (s.getName().equals("var"))) {
//				s.type = Sentence.TYPE_NOT_LINE;
//				s.over();
//			}
			
			v = v1;
//			this.out.append(left).append(" = ").append(rightValue);
		}
		
		
//		this.mgr.setVar(v);
		this.over();
		
		return true;
	}
	
	
	/**
	 * aput所引用到的arrNewSen
	 */
	private NewSentence arrNewSen;
	
	
	/**
	 * @return the arrNewSen
	 */
	public final NewSentence getArrNewSen() {
		return arrNewSen;
	}




	/**
	 * @param arrNewSen the arrNewSen to set
	 */
	public final void setArrNewSen(NewSentence arrNewSen) {
		this.arrNewSen = arrNewSen;
	}
	private Var v = new Var(this);
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.v;
	}
	
	


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getOut()
	 */
	@Override
	public String getOut() {
//		this.out = new StringBuilder();
		if (this.arrNewSen != null) {
			this.out.append(this.arrNewSen.getArrName());
		}else{
			this.out.append(this.arrLeft);
		}
		this.out.append(this.left).append(" = ").append(this.rightValue);
		return super.getOut();
	}


	/**
	 * @param rightValue the rightValue to set
	 */
	public final void setRightValue(String rightValue) {
		this.rightValue = rightValue;
	}


	/**
	 * @return the arrLeft
	 */
	public final String getArrLeft() {
		return arrLeft;
	}

	/**
	 * @param arrLeft the arrLeft to set
	 */
	public final void setArrLeft(String arrLeft) {
		this.arrLeft = arrLeft;
	}




	/**
	 * @param left the left to set
	 */
	public final void setLeft(String left) {
		this.left = left;
	}




	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new PutSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "put";
	}
	
	
	/**
	 * @return the arrSourceVar
	 */
	public final String getArrSourceVar() {
		return arrSourceVar;
	}

	/**
	 * @return the arrVal
	 */
	public final String getRightValue() {
		return rightValue;
	}
	

	/**
	 * @return the left
	 */
	public final String getLeft() {
		return left;
	}
	static final String[] KEYS = new String[]{
		"aput",
		"aput-wide",
		"aput-object",
		"aput-boolean",
		"aput-byte",
		"aput-char",
		"aput-short",
		"iput",
		"iput-wide",
		"iput-object",
		"iput-boolean",
		"iput-byte",
		"iput-char",
		"iput-short",
		"sput",
		"sput-wide",
		"sput-object",
		"sput-boolean",
		"sput-byte",
		"sput-char",
		"sput-short"
	};
}
