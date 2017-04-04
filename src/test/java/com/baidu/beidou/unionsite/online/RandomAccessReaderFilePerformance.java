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
public class RandomAccessReaderFilePerformance {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		RandomAccessFile file = new RandomAccessFile("random.dat","r");
		long start = System.currentTimeMillis();
		int result =0;
		for(int i=0; i<1000000&&result!=-1; i++){
			byte[] a = new byte[2000];
			result = file.read(a);
		}
		System.out.println("cost=41656"+(System.currentTimeMillis()-start));
		file.close();
	}

}
