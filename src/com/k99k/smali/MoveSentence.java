/**
 * 
 */
package com.k99k.smali;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * Move语句
 * @author keel
 *
 */
public class MoveSentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public MoveSentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_NOT_LINE;
	}
	static final Logger log = Logger.getLogger(MoveSentence.class);
	
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		if (ws.length<2) {
			this.out.append("exec move error. line:").append(this.line);
			this.mgr.err(this);
			log.error(this.mgr.getMeth().getName()+" moveERR:"+this.line);
			return false;
		}
		String key = ws[0];
		if (key.startsWith("move-result")) {
			Sentence last = null;
			//向上定位到非STRUCT句
			for(int i=0;i<this.lineNum;i++){
				last = this.mgr.getLastSentence(i);
				if (last == null) {
					this.out.append("exec move error. last sentence is null. line:").append(this.line);
					this.mgr.err(this);
					log.error(this.mgr.getMeth().getName()+" moveERR:"+this.line);
					return false;
				}else if(last.getType() == Sentence.TYPE_STRUCT){
					continue;
				}
				if (last.getVar()==null ) {
					this.out.append("exec move error. last sentence's var is null. line:").append(this.line);
					this.mgr.err(this);
					log.error(this.mgr.getMeth().getName()+" moveERR:"+this.line);
					return false;
				}else{
					break;
				}
			}
			
			//使用前一个invoke语句生成的Var,改变其name后加入到Var集合
			Var v = last.getVar();
			String name = ws[1];
			v.setName(name);
//			v.setOutVar(true);
			this.mgr.setVar(v);
			this.var = v;
			this.setOut(v.getOut());
			//将last语句显示去掉
			last.setType(Sentence.TYPE_NOT_LINE);
			last.over();
		}else if(key.equals("move-exception")){
			//不处理,已由try catch处理
			Var v1 =  this.mgr.getVar(ws[1]);
			if (v1 == null) {
				v1 = new Var(this);
			}
			v1.setOut("[exception]");
			v1.setClassName(ws[0]);
			v1.setKey("move-exception");
			v1.setSen(this);
			v1.setValue("[exception]");
			this.mgr.setVar(v1);
		}else{
			//move变量
			Var v1 =  this.mgr.getVar(ws[1]);
			Var v2 =  this.mgr.getVar(ws[2]);
			boolean show = false;
			if (v1 == null) {
				v1 = new Var(this);
				v1.setName(ws[1]);
				v1.setKey(ws[0]);
				v1.setClassName(v2.getClassName());
				v1.setOut(v2.getOut());
				show = false;
			}else{
				show = ComputeSentence.isLocalVar(v1, this.mgr.isStatic());
//				v1.setSen(this);
//				if (ws[0].startsWith("move-object") || v1.isUsed() || StringUtil.isDigits(v1.getOut())|| v1.getOut().equals(v2.getOut())) {
//					//引用的变化导致需要输出也要变动,另外move用过的v1,out为数字的v1也需要变动
//					//FIXME move-object引用变化可能有风险
//					v1.setOut(v2.getOut());
//					show = false;
//				}
			}
			//v2.setName(ws[1]);
			v1.setValue(v2.getValue());
			this.mgr.setVar(v1);
			this.var = v1;
			if (show) {
				this.setOut(v1.getOut()+" = "+ v2.getOut());
				this.type = Sentence.TYPE_LINE;
			}else{
				v1.setOut(v2.getOut());
			}
//			//FIXME 用过了,目前仅在move中使用,还有get,put,cast,compute(compute应该不能用),未使用,如果使用需要注意v1==v2的情况,另注意p0,p1的情况
//			if (v2.getSen() != null) {
//				//非参数的设置成used
//				v2.setUsed(true);
//			}
		}
		
		this.over();
		return true;
	}
	
	private Var var = new Var(this);
	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getVar()
	 */
	@Override
	public Var getVar() {
		return this.var;
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new MoveSentence(mgr, line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "move";
	}
	
	static final String[] KEYS = new String[]{
		"move", 
		"move/from16",
		"move/16",
		"move-wide",
		"move-wide/from16",
		"move-wide/16",
		"move-object",
		"move-object/from16", 
		"move-object/16",
		"move-result",
		"move-result-wide",
		"move-result-object",
		"move-exception"
	};
}
