/**
 * 2009-4-20 下午10:52:44
 */
package com.baidu.beidou.util.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import com.baidu.beidou.exception.InternalException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ObjectAccessUtil {

	/**
	 * 写文件，返回写的数据byte长度
	 * 
	 * @author zengyunfeng
	 * @param outFile
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static int writeObject(final RandomAccessFile outFile,
			final Object data) throws IOException {
		byte[] objBytes = getBytesFromObject(data);
		outFile.write(objBytes);
		return objBytes.length;
	}

	public static Object readObject(final RandomAccessFile outFile,
			final long pos, final int length) throws IOException,
			InternalException, ClassNotFoundException {
		byte[] result = new byte[length];
		outFile.seek(pos);
		int readed = outFile.read(result);
		if (readed != length) {
			throw new InternalException(
					"union site index doesn't match the binary file");
		}
		ByteArrayInputStream bos = new ByteArrayInputStream(result);
		ObjectInputStream oos = new ObjectInputStream(bos);
		

		Object obj = oos.readObject();
		bos.close();
		oos.close();
		return obj;
	}

	/**
	 * 获得对象的字节数组
	 * 
	 * @author zengyunfeng
	 * @param data
	 *            需要是Serializable对象
	 * @return
	 * @throws IOException
	 */
	private static byte[] getBytesFromObject(Object data) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(data);
		objos.flush();
		byte[] result = os.toByteArray();
		os.close();
		objos.close();
		return result;
	}
}
