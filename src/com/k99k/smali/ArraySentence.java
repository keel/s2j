/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

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
	
	private ArrayList<String> arrMatrix;

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
			//填充int数组
			
			
			
		}else if(key.equals(".array-data")) {
			if (this.arrMatrix == null) {
				this.out.append("arrMatrix is null. line:").append(this.line);
				this.mgr.err(this);
				System.err.println(this.out);
				return false;
			}
			//处理填入的数组数据
			//int arrLen = Integer.parseInt(ws[1],16);
			int alen = this.arrMatrix.size();
			for (int i = 0; i < alen; i++) {
				String l = this.arrMatrix.get(i);
				
			}
			
		}else{
			
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

	
	static final String[] KEYS = new String[]{
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
