/**
 * 
 */
package com.baidu.beidou.unionsite.online;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Administrator
 *
 */
public class RandomAccessFilePerformance {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		RandomAccessFile file = new RandomAccessFile("random_rw.dat","rw");
		long start = System.currentTimeMillis();
		for(int i=0; i<1000000; i++){
			byte[] a = new byte[2000];
			file.write(a);
		}
		System.out.println("rw cost=42813ms"+(System.currentTimeMillis()-start));
//		System.out.println("rws cost=622735ms"+(System.currentTimeMillis()-start));
		file.close();
	}

}
