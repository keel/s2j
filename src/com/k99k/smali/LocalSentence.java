/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * 局部变量
 * @author keel
 *
 */
public class LocalSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public LocalSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(LocalSentence.class);
	
	private Var v;
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		int len = ws.length;
		if (len<3) {
			this.out.append("exec var error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.out);
			return false;
		}
		String[] tar = ws[2].split(":");
		String name = tar[0];
		String obj = Tool.parseObject(tar[1]);
		this.v = new Var(this);
		v.setClassName(obj);
		v.setKey(KEY);
		v.setName(ws[1]);
		v.setOut(name);
//		v.setOutVar(true);
		
//		String val = "";
		Var ov = this.mgr.getVar(ws[1]);
		// null可能是exception
		if (ov == null) {
			if (obj.toLowerCase().matches(".*exception.*")) {
				this.mgr.setVar(v);
				this.over();
				return true;
			}else if(obj.toLowerCase().equals("throwable")){
				this.mgr.setVar(v);
				this.over();
				return true;
			}else{
				//判断是否是参数p0之类的因为end local从vars中去掉了,此时可以找回来
				if (ws[1].startsWith("p")) {
					ov = this.mgr.getVarFromEndVars(ws[1]);
					this.v = ov;
					this.mgr.setVar(v);
					this.type = Sentence.TYPE_NOT_LINE;
					return true;
				}
				log.error(this.mgr.getMeth().getName()+" local can't find:"+ws[1]+ " mLine:"+this.line);
				return false;
			}
		}
		String val = ov.getOut();
		if (obj.equals("float")) {
//			val = StringUtil.float16to10(val)+"F";
			if (val.matches("(0x)?[\\d|a-f]+[F]?")) {
				val = String.valueOf(StringUtil.float16to10(val))+"F";
			}
		}else if(obj.equals("double")){
//			val = StringUtil.double16to10(val)+"D";
			if (val.matches("(0x)?[\\d|a-f]+[D]?")) {
				val = String.valueOf(StringUtil.double16to10(val))+"D";
			}
		}else if(obj.equals("boolean")){
			if (val.equals("0")) {
				val = "false";
			}else{
				val = "true";
			}
		}else{
			String right = null;
			if (val.equals("0")){
				right = Var.checkIout(obj, "0");
			}else{
				right = Var.checkIout(obj, val);
			}
			if (obj.equals("int") && (!ov.getClassName().equals("int")) && StringUtil.isDigits(ov.getValue())) {
				right = String.valueOf(ov.getValue());
			}
			val = right;
		}
		
		
//		Sentence s1 = this.mgr.getLastSentence();
//		if (s1 != null && s1.getOut().length()>0) {
//			s1.over();
//			this.mgr.removeSentence(s1);
//			val = s1.getOut();
//		}else{
//			val = this.mgr.getVar(ws[1]).getOut();
//		}
		
		
		this.out.append(obj).append(" ").append(name).append(" = ").append(val);
		this.mgr.setVar(v);
		if (ov.getSen() != null) {
			Sentence s1 = ov.getSen();
			if (s1.getName().equals("get") || s1.getName().equals("var") ) {
				s1.type = Sentence.TYPE_NOT_LINE;
			}
		}
		if (ov.getOut().equals("[exception]")) {
			this.type = TYPE_NOT_LINE;
		}
		//数组的处理
		if (obj.indexOf("[]")>0) {
			String pre = null;
			for (int i = this.lineNum-1; i >= 0; i--) {
				pre = this.mgr.getSrcline(i);
				if (pre.charAt(0) != '.') {
					break;
				}
			}
			
			//Sentence preSen = this.mgr.getLastSentence();
			if (pre.startsWith("check-cast")) {
				//处理多维数组定义
				CastSentence ca = new CastSentence(mgr, pre);
				ca.exec();
				if (StringUtil.isStringWithLen(ca.getArrVal(),1)) {
					//向上找到filled-new-array语句
					Sentence fs = this.mgr.findLastSentence("new");
					if (fs.getLine().indexOf("filled-new-array") > -1) {
						NewSentence ns = (NewSentence)fs;
						String[] r = ns.getArrRang();
						if (r != null && r.length>0) {
							this.out = new StringBuilder();
							this.out.append(obj).append(" ").append(name).append(" = ");
							this.out.append(ns.getVar().getOut());
							//计算总数组维数
							int arrM = obj.split("\\[").length - 1;
							for (int i = 0; i < r.length; i++) {
								this.out.append("[");
								this.out.append(r[i]);
								this.out.append("]");
								arrM--;
							}
							//追加最后的空[]
							for (int i = 0; i < arrM; i++) {
								this.out.append("[]");
							}
						}
					}
				}
			}else if(pre.startsWith("aput")){
				//处理数组在定义时初始化的情况，如String[] arr2 = new String[]{"aaa","bbb","ccc"};
				Sentence preSen = this.mgr.getLastSentence();
				int indexMe = this.mgr.findSentenceIndexByLineNum(preSen.getLineNum());
				if (indexMe>0) {
					int start = -1,from = indexMe;
					while(start == -1){
						Sentence ns = this.mgr.findLastSentence("new",from);
						if (ns.getLine().indexOf("new-array") >= 0) {
							//确定上方第一个包含new-array的语句为起始点
							start = this.mgr.findSentenceIndexByLineNum(ns.getLineNum());
							break;
						}else{
							from = this.mgr.findSentenceIndexByLineNum(ns.getLineNum());
						}
					}
					if (start > -1) {
						StringBuilder sb = new StringBuilder();
						for (int i = start; i <= indexMe; i++) {
							Sentence s = this.mgr.findSentenceByIndex(i);
							if (s.getName().equals("put") && s.getLine().startsWith("aput")) {
								PutSentence put = (PutSentence)s;
								put.setType(Sentence.TYPE_NOT_LINE);
								if (put.getArrSourceVar().equals(ws[1])) {
									sb.append(",").append(put.getRightValue());
								}
							}
						}
						if (sb.length()>1) {
							//删除第一个,号
							sb.delete(0, 1);
							//添加初始的数组定义
							this.out.delete(this.out.indexOf("=")+1, this.out.length());
							this.out.append("{").append(sb).append("}");
						}
					}
				}
			}
			
		}
		
		this.over();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new LocalSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.v;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "local";
	}

	static final String KEY = StaticUtil.TYPE_LOCAL;
}
