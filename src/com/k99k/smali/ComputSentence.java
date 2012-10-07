/**
 * 
 */
package com.k99k.smali;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author keel
 *
 */
public class ComputSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public ComputSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(ComputSentence.class);
	
	private String comTag = null;
	
	/**
	 * 计算符
	 */
	private static HashMap<String,String> coms = new HashMap<String, String>();
	
	private static String[] coma = new String[]{
		"add-",
		"sub-",
		"mul-",
		"div-",
		"rem-",
		"and-",
		"or-",
		"xor-",
		"shr-",
		"shl-",
		"ushr-"
	};
	static{
		coms.put("add-", "+");
		coms.put("sub-", "-");
		coms.put("mul-", "*");
		coms.put("div-", "/");
		coms.put("rem-", "%");
		coms.put("and-", "&");
		coms.put("or-", "|");
		coms.put("xor-", "^");
		coms.put("shl-", "<<");
		coms.put("shr-", ">>");
		coms.put("ushr-", ">>>");
	}
	
	private static String checkCom(String tag){
		for (int i = 0; i < coma.length; i++) {
			if (tag.indexOf(coma[i]) > -1) {
				return coma[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		//去除,号
		this.line = this.line.replaceAll(",", "");
		String[] arr = this.line.split(" ");
		if (arr.length<3) {
			this.out.append("exec invoke error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
//		int alen = arr.length;
		this.comTag = arr[0];
		String target = arr[1];
		String com = null;;
		if (this.comTag.indexOf("-to-") > -1) {
			//转换
			String[] toa = this.comTag.split("-to-");
			Var org = this.mgr.getVar(arr[2]);
			Var tar = this.mgr.getVar(arr[1]);
			if (tar == null) {
				//tar实次出现
				tar = new Var(this);
				tar.setClassName(org.getClassName());
				tar.setKey(org.getKey());
				tar.setName(arr[1]);
			}
			tar.setOut("("+toa[1]+")"+org.getOut());
			tar.setValue(org.getValue());
			this.mgr.setVar(tar);
			this.type = Sentence.TYPE_NOT_LINE;
		}else if(this.comTag.indexOf("/2addr") > -1){
			//存值 
			com=checkCom(this.comTag);
			if (com == null) {
				log.error(this.mgr.getMeth().getName()+" - /2addr error:"+this.line);
				return false;
			}
			Var org = this.mgr.getVar(target);
			String tar = org.getOut();
			StringBuilder sb = new StringBuilder();
			sb.append(tar);
			sb.append(" ").append(coms.get(com)).append(" ");
			sb.append(this.mgr.getVar(arr[2]).getOut());
			if (org.getSen() != null && (org.getSen().getName().equals("var") || org.getSen().getName().equals("get"))) {
				org.setOut("("+sb.toString()+")");
//				org.setOutVar(false);
				this.out.append(tar).append(" = ");
				this.out.append(sb);
				org.setSen(this);
				this.mgr.setVar(org);
				this.type = TYPE_NOT_LINE;
			}else{
				//p1,p2之类的参数处理
				org.setOut("("+sb.toString()+")");
				this.out.append(tar).append(" = ");
				this.out.append(sb);
				org.setSen(this);
				this.mgr.setVar(org);
			}
			
		}else if(this.comTag.indexOf("neg-") > -1){
			//取反
			Var tov = this.mgr.getVar(arr[1]);
			String org = tov.getOut();
			String to = this.mgr.getVar(arr[2]).getOut();
			this.out.append(org).append(" = ").append("-").append(to);
			tov.negVal();
			this.mgr.setVar(tov);
		}else if((com=checkCom(this.comTag)) != null){
			//赋值计算
			Var org = this.mgr.getVar(target);
			boolean orgSave = false;
			//org是新的或是非输出的Var时
			if (org == null) {
				//还未声明
				org = new Var(this);
				org.setName(target);
				org.setKey(this.comTag);
				org.setClassName("");
				orgSave = true;
//				org.setOutVar(false);
				this.type = Sentence.TYPE_NOT_LINE;
			}else{
				this.out.append(org.getOut()).append(" = ");
			}
			Var v2 = this.mgr.getVar(arr[2]);
			StringBuilder sb = new StringBuilder();
			sb.append(v2.getOut());
			sb.append(" ").append(coms.get(com)).append(" ");
			String sec = null;
			if (arr[3].startsWith("v")) {
				//FIXME 需要处理16进制转换
				sec = this.mgr.getVar(arr[3]).getOut();
			}else{
				sec = arr[3];
			}
			sb.append(sec);
			//是否输出的判断
			if (org.getSen()!=null && (org.getSen().getName().equals("invoke") || org.getSen().getName().equals("compute"))) {
//				v2.setOutVar(false);
				this.type = Sentence.TYPE_NOT_LINE;
				org.setOut(sb.toString());
			}
			if (orgSave) {
				org.setOut(sb.toString());
				this.mgr.setVar(org);
			}
			this.out.append(sb);
		}else if(this.comTag.indexOf("cmp") == 0){
			//比较语句
			this.type = Sentence.TYPE_NOT_LINE;
			Var a1 = this.mgr.getVar(arr[2]);
			Var a2 = this.mgr.getVar(arr[3]);
			Var tar = this.mgr.getVar(arr[1]);
//			if (this.comTag.charAt(3) == 'g') {
//				tar.setOut(a1.getOut()+" - "+a2.getOut());
//			}else if (this.comTag.charAt(3) == 'l') {
//				tar.setOut(a1.getOut()+" - "+a2.getOut());
//			}else if(this.comTag.equals("cmp-long")){
//				tar.setOut(a1.getOut()+" - "+a2.getOut());
//			}
			tar.setOut(a1.getOut()+" - "+a2.getOut());
		}
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new ComputSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "compute";
	}

	static final String[] KEYS = new String[]{
		"neg-int",
//		"not-int",
		"neg-long",
//		"not-long",
		"neg-float",
		"neg-double",
		"int-to-long",
		"int-to-float",
		"int-to-double",
		"long-to-int",
		"long-to-float",
		"long-to-double",
		"float-to-int",
		"float-to-long",
		"float-to-double",
		"double-to-int",
		"double-to-long",
		"double-to-float",
		"int-to-byte",
		"int-to-char",
		"int-to-short",
		"add-int",
		"sub-int",
		"mul-int",
		"div-int",
		"rem-int",
		"and-int",
		"or-int",
		"xor-int",
		"shl-int",
		"shr-int",
		"ushr-int",
		"add-long",
		"sub-long",
		"mul-long",
		"div-long",
		"rem-long",
		"and-long",
		"or-long",
		"xor-long",
		"shl-long",
		"shr-long",
		"ushr-long",
		"add-float",
		"sub-float",
		"mul-float",
		"div-float",
		"rem-float",
		"add-double",
		"sub-double",
		"mul-double",
		"div-double",
		"rem-double",
		"add-int/2addr",
		"sub-int/2addr",
		"mul-int/2addr",
		"div-int/2addr",
		"rem-int/2addr",
		"and-int/2addr",
		"or-int/2addr",
		"xor-int/2addr",
		"shl-int/2addr",
		"shr-int/2addr",
		"ushr-int/2addr",
		"add-long/2addr",
		"sub-long/2addr",
		"mul-long/2addr",
		"div-long/2addr",
		"rem-long/2addr",
		"and-long/2addr",
		"or-long/2addr",
		"xor-long/2addr",
		"shl-long/2addr",
		"shr-long/2addr",
		"ushr-long/2addr",
		"add-float/2addr",
		"sub-float/2addr",
		"mul-float/2addr",
		"div-float/2addr",
		"rem-float/2addr",
		"add-double/2addr",
		"sub-double/2addr",
		"mul-double/2addr",
		"div-double/2addr",
		"rem-double/2addr",
		"add-int/lit16",
//		"rsub-int",
		"mul-int/lit16",
		"div-int/lit16",
		"rem-int/lit16",
		"and-int/lit16",
		"or-int/lit16",
		"xor-int/lit16",
		"add-int/lit8",
//		"rsub-int/lit8",
		"mul-int/lit8",
		"div-int/lit8",
		"rem-int/lit8",
		"and-int/lit8",
		"or-int/lit8",
		"xor-int/lit8",
		"shl-int/lit8",
		"shr-int/lit8",
		"ushr-int/lit8",
		"cmpl-float",
		"cmpg-float",
		"cmpl-double",
		"cmpg-double",
		"cmp-long"
		
	};
	
	
	public static void main(String[] args) {
		String fs = "0x3f00";
		
		//fs = "3";
//		int   intBits   =   Integer.parseInt(fs,   16);     
//        float   f   =   Float.intBitsToFloat(intBits);
		float f = 0.5F;
		int bits = Float.floatToIntBits(f); 
		fs = Integer.toHexString(bits);
		System.out.println(fs); 
		
		//int i = Integer.valueOf(fs, 16);
        float value = Float.intBitsToFloat(Integer.valueOf(fs, 16));
        System.out.println(value);    
        
        double d = 120d;
        long ll = Double.doubleToLongBits(d);
        fs = Long.toHexString(ll);
        System.out.println(fs);
        
        long ls = Long.valueOf(fs, 16);
        double val = Double.longBitsToDouble(ls);
        System.out.println(val);
        
	}
}
