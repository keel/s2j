package com.k99k.tools;

public interface IOInterface {
	
	/**
	 * 处理文件内容
	 * @param fileContent
	 * @return
	 */
	public String doFileContent(String fileContent);
	
	/**
	 * 文件名过滤器,比如过滤掉非文本文件
	 * @param fileName
	 * @return
	 */
	public boolean doFilter(String fileName);

}
