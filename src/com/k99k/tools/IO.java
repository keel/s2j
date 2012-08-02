/**
 * 
 */
package com.k99k.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * IO工具
 * @author keel
 *
 */
public final class IO {

	/**
	 * 读取文本文件
	 * @param txtPath
	 * @param encode
	 * @return String
	 * @throws IOException
	 */
	public static final String readTxt(String txtPath, String encode)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(txtPath), encode));
		String str;
		StringBuilder sb = new StringBuilder();
		while ((str = in.readLine()) != null) {
			sb.append(str).append("\r\n");
		}
		return sb.toString();
	}
	
	/**
	 * 写入String到文本文件
	 * @param txt
	 * @param encode
	 * @param filePah
	 * @throws IOException
	 */
	public static final void writeTxt(String txt,String encode, String filePah) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
	            new FileOutputStream(filePah), encode));
        out.write(txt);
        out.close();
	}
	
	/**
	 * 创建目录,无论目录是否已存在
	 * @param dir
	 * @return 
	 */
	public static final boolean makeDir(File dir){
		return dir.mkdirs();
	}
	
	/**
	 * 删除目录及下面的文件和子目录,失败的则跳过
	 * @param dir 目录 
	 * @return 
	 */
	public final static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				//boolean success = 
				deleteDir(new File(dir, children[i]));
				//if (!success) {
					// 跳过
					// return false;
				//}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
	
	/**
	 * 复制整个文件夹到另一位置
	 * @param from File 文件夹
	 * @param to File 新的文件夹
	 * @throws IOException 
	 */
	public static final void copyFullDir(File from,File to) throws IOException{
		if (from.exists()) {
			if (from.isDirectory()) {
				to.mkdirs();
				String[] children = from.list();
				for (int i = 0; i < children.length; i++) {
					copyFullDir(new File(from, children[i]),new File(to, children[i]));
				}
			}else{
				copy(from,to);
			}
		}
	}
	
	public static final void copyFullDirWithFn(File from,File to,IOInterface iof, String encode) throws IOException{
		if (from.exists()) {
			if (from.isDirectory()) {
				to.mkdirs();
				String[] children = from.list();
				for (int i = 0; i < children.length; i++) {
					copyFullDirWithFn(new File(from, children[i]),new File(to, children[i]),iof,encode);
				}
			}else{
				copyWithFn(from,to,iof,encode);
			}
		}
	}
	
	public static final void copyWithFn(File fileFrom, File fileTo,IOInterface iof, String encode) throws IOException {  
		String from = fileFrom.getAbsolutePath();
		if (iof.doFilter(from)) {
			String org = readTxt(from,encode);
			String target = iof.doFileContent(org);
			writeTxt(target,encode,fileTo.getAbsolutePath());
			System.out.println(from);
		}
    } 
	
	
	/**
	 * 复制单个文件,如原文件存在则直接覆盖
	 * @param fileFrom
	 * @param fileTo
	 * @return
	 * @throws IOException 
	 */
	public static final boolean copy(File fileFrom, File fileTo) throws IOException {  
        FileInputStream in = new FileInputStream(fileFrom);  
        FileOutputStream out = new FileOutputStream(fileTo);  
        byte[] bt = new byte[1024*5];  
        int count;  
        while ((count = in.read(bt)) > 0) {  
            out.write(bt, 0, count);  
        }  
        in.close();  
        out.close();  
        return true;
    } 

	/**
	 * 复制单个文件,如原文件存在则直接覆盖
	 * @param in InputStream
	 * @param out FileOutputStream
	 * @return
	 * @throws IOException 
	 */
	public static final boolean copy(InputStream in, FileOutputStream out) throws IOException {  
        byte[] bt = new byte[1024*5];  
        int count;  
        while ((count = in.read(bt)) > 0) {  
            out.write(bt, 0, count);  
        }  
        in.close();  
        out.close();  
        return true;
    } 
}
