/**
 * 
 */
package com.k99k.smali;

import java.util.ArrayList;

/**
 * 语句管理者
 * @author keel
 *
 */
public class SentenceMgr {

	/**
	 * 
	 */
	public SentenceMgr() {
	}
	
	/**
	 * 当前处理的几行
	 */
	private ArrayList<String> curLines = new ArrayList<String>();
	
	/**
	 * 处理原始语句集
	 * @param srcLines
	 */
	public void execLines(ArrayList<String> srcLines){
		//逐一读取，当读到Sendtence的key时，查找Sentence处理,并将此段内容传入处理
		//如果处理失败,则查找另一可能处理的Sentence处理
		//处理成功,则将Sentence加入已处理的lines,注意处理后的状态是完成还是完成中
		//如果是完成则继续
		//如果是处理中则将此Sentence加入待完成的
	}
	
	/**
	 * 定位语句
	 * @param key
	 * @return
	 */
	public Sentence findSentence(String key){
		
		
		return null;
	}

}
