/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.k99k.tools.StringUtil;

/**
 * 数组处理
 * @author keel
 *
 */
public class ArraySentence extends Sentence {

	/**
	 * @param mgr
	 * @param line
	 */
	public ArraySentence(SentenceMgr mgr, String line) {
		super(mgr, line);
		this.type = Sentence.TYPE_LINE;
	}
	static final Logger log = Logger.getLogger(ArraySentence.class);
	
	private ArrayList<String> arrMatrix;
	
	private String[] arrData;

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#exec()
	 */
	@Override
	public boolean exec() {
		this.doComm(this.line);
		this.line = this.line.replaceAll(",", "");
		String[] ws = this.line.split(" ");
		String key = ws[0];
		if (key.equals("fill-array-data")) {
			//向var集合中保存
			//Var的sen定位于fill-array-data的前一句,即:new-array
			Var v = new Var(this);
			v.setKey(key);
			v.setClassName("array");
			v.setName(ws[2]);//以ws[2]为key
			v.setValue(ws[1]);//value为变量，如：v0
			this.mgr.setVar(v);
			Var v1 = this.mgr.getVar(ws[1]);
			if (v1 != null) {
				Sentence newArrSen = v1.getSen();
				if (newArrSen != null && newArrSen.getLine().startsWith("new-array")) {
					newArrSen.setOut("");
					((NewSentence)newArrSen).setFilled(true);
				}
			}
		}else if(key.equals(".array-data")){
			if (this.arrMatrix == null) {
				this.out.append("arrMatrix is null. line:").append(this.line);
				this.mgr.err(this);
				log.error(this.out);
				return false;
			}
			//处理填入的数组数据
//			int arrLen = Integer.parseInt(StringUtil.a16to10(ws[1]))+1;
			int alen = this.arrMatrix.size();
			String[] arrVals = new String[alen];
			StringBuilder asb = new StringBuilder();
			for (int i = 0; i < alen; i++) {
				String[] ls = this.arrMatrix.get(i).split(" ");
				StringBuilder sb = new StringBuilder("0x");
				for (int j = ls.length-1; j >= 0 ; j--) {
					String str = ls[j].substring(2,ls[j].length()-1);
					if (str.length() == 1) {
						sb.append("0");
					}
					sb.append(str);
				}
				if (ls.length>=8) {
					sb.append("L");
				}
				arrVals[i] = sb.toString();
				asb.append(",").append(arrVals[i]);
			}
			this.arrData = arrVals;
			//查找标识,在上一句中
			String vkey = this.mgr.getLastSentence().getLine();
			//补充到fill-array-data中
			Var v = this.mgr.getVar(vkey);
			//Var arrDef = v.getSen().getVar();
			asb.delete(0, 1);
			asb.insert(0, "{");
			//asb.insert(0, this.mgr.getVar(v.getValue().toString()).getOut());
			asb.append("}");
			v.getSen().setType(Sentence.TYPE_NOT_LINE);
			int ii = this.mgr.findSentenceIndexByLineNum(v.getSen().getLineNum())+1;
			Sentence sen = this.mgr.findSentenceByIndex(ii);
			if (sen.getName().equals("put")) {
				PutSentence sarr = (PutSentence)sen;
				sarr.setRightValue(asb.toString());
			}else{
				String s = sen.getOut();
				sen.setOut(s.split("=")[0]+" = "+asb.toString());
			}
			if (sen.getType() == Sentence.TYPE_NOT_LINE) {
				sen.setType(TYPE_LINE);
			}
//			StringBuilder sb = new StringBuilder(sarr.getOut());
//			sb.delete(sb.indexOf("=")+1, sb.length());
//			sb.append(asb);
//			sarr.setOut(sb.toString());
			//this.type = Sentence.TYPE_NOT_LINE;
		}else if(key.equals("array-length")){
			log.debug(this.mgr.getMeth().getName()+" - array-length:"+this.line);
		}else{
			//this.type = Sentence.TYPE_NOT_LINE;
		}
		
		this.over();
		return true;
	}
	
	public void addToArrMatrix(String line){
		if (this.arrMatrix == null) {
			this.arrMatrix = new ArrayList<String>();
		}
		this.arrMatrix.add(line);
	}

	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#newOne(com.k99k.smali.SentenceMgr, java.lang.String)
	 */
	@Override
	public Sentence newOne(SentenceMgr mgr, String line) {
		return new ArraySentence(mgr, line);
	}


	/* (non-Javadoc)
	 * @see com.k99k.smali.Sentence#getName()
	 */
	@Override
	public String getName() {
		return "array";
	}

	
	/**
	 * @return the arrData
	 */
	public final String[] getArrData() {
		return arrData;
	}

	static final String[] KEYS = new String[]{
		"array-length",
		".array-data",
		"fill-array-data",
		":array_0",
		":array_1",
		":array_2",
		":array_3",
		":array_4",
		":array_5",
		":array_6",
		":array_7",
		":array_8",
		":array_9",
		":array_a",
		":array_b",
		":array_c",
		":array_d",
		":array_e",
		":array_f",
		":array_10",
		":array_11",
		":array_12",
		":array_13",
		":array_14",
		":array_15",
		":array_16",
		":array_17",
		":array_18",
		":array_19",
		":array_1a",
		":array_1b",
		":array_1c",
		":array_1d",
		":array_1e",
		":array_1f",
		":array_20",
		":array_21",
		":array_22",
		":array_23",
		":array_24",
		":array_25",
		":array_26",
		":array_27",
		":array_28",
		":array_29",
		":array_2a",
		":array_2b",
		":array_2c",
		":array_2d",
		":array_2e",
		":array_2f",
		":array_30",
		":array_31",
		":array_32",
		":array_33",
		":array_34",
		":array_35",
		":array_36",
		":array_37",
		":array_38",
		":array_39",
		":array_3a",
		":array_3b",
		":array_3c",
		":array_3d",
		":array_3e",
		":array_3f",
		":array_40",
		":array_41",
		":array_42",
		":array_43",
		":array_44",
		":array_45",
		":array_46",
		":array_47",
		":array_48",
		":array_49",
		":array_4a",
		":array_4b",
		":array_4c",
		":array_4d",
		":array_4e",
		":array_4f",
		":array_50"
	};
	
	
}
