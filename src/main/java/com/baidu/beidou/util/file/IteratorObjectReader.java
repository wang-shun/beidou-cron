/**
 * 2009-4-28 下午04:02:28
 */
package com.baidu.beidou.util.file;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class IteratorObjectReader<T> {

	private FileInputStream fis = null;
	private ObjectInputStream ois = null;
	public IteratorObjectReader(String fileName) throws FileNotFoundException, IOException{
		fis = new FileInputStream(fileName);
		ois = new ObjectInputStream(fis);
	}
	/**
	 * 存储记录
	 * @author zengyunfeng
	 * @param list
	 * @param cacheFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void persistentCache(final List<T> list, final ObjectOutputStream output)
			throws IOException {
		if (list == null || output == null) {
			return;
		}

		for (T bo : list) {
			output.writeObject(bo);
		}

	}

	/**
	 * 读取一条记录
	 * 
	 * @author zengyunfeng
	 * @param input
	 * @return 返回null表示读到文件结尾
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public T next() throws IOException,
			ClassNotFoundException {
		try {
			return (T) ois.readObject();
		} catch (EOFException e) {
			return null;
		}
	}
	
	/**
	 * 是否到文件结尾
	 * @author zengyunfeng
	 * @return
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException{
		return fis.available()>0;
	}
	

	public void close() throws IOException{
		ois.close();
	}
}
