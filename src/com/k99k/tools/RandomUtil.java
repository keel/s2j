/**
 * 
 */
package com.k99k.tools;

/**
 * 随机相关
 * @author keel
 *
 */
public final class RandomUtil {

	private RandomUtil() {
	}
	
//	/**
//	 * 获取一个长度范围内的随机字条串
//	 * @param lenFrom 
//	 * @param lenTo
//	 * @param chars 字符范围
//	 * @return
//	 */
//	public static final String getRandomString(int lenFrom,int lenTo,char[] chars){
//		
//		return "";
//	}
	
	/**
	 * 获取一个范围内的随机值
	 * @param from 必须大于0
	 * @param to 不含,当小于from时直接返回from值
	 * @return 正整数随机值
	 */
	public static final int getRandomInt(int from,int to){
		if (from < 0) {
			from = 0;
		}
		if (to <= from) {
			return from;
		}
		int re = ((int) (Math.random()*100))%(to-from) + from;
		return re;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(getRandomInt(0, 2));
	}

}
