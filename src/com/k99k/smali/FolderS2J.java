/**
 * 
 */
package com.k99k.smali;

import java.io.File;

import org.apache.log4j.Logger;

import com.k99k.tools.IOInterface;

/**
 * 按目录批量转换的转换器
 * @author keel
 *
 */
public class FolderS2J implements IOInterface {

	/**
	 * 
	 */
	public FolderS2J() {
	}
	
	static final Logger log = Logger.getLogger(FolderS2J.class);


	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#doFileContent(java.lang.String)
	 */
	@Override
	public String doFileContent(String fileContent) {
		S2J s =S2J.getS2J();
		return s.exec(fileContent);
	}

	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#doFilter(java.lang.String)
	 */
	@Override
	public boolean doFilter(String fileName) {
		String last = fileName.substring(fileName.lastIndexOf("."));
		if (last.equals(".smali")) {
			log.info("-----------["+fileName+"]----------");
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.k99k.tools.IOInterface#newFile(java.io.File)
	 */
	@Override
	public File newFile(File fileTo) {
		String path = fileTo.getAbsolutePath();
		String to = path.substring(0,path.lastIndexOf("."))+".java";
//		fileTo.renameTo(new File(to));
		return new File(to);
	}

}
