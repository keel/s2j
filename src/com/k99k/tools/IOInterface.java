package com.k99k.tools;

import java.io.File;

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
	
	/**
	 * 处理目标文件,比如更新目标文件扩展名等
	 * @param fileTo
	 * @return
	 */
	public File newFile(File fileTo);

}
