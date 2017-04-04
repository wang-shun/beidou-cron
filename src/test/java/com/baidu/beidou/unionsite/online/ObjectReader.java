/**
 * 
 */
package com.baidu.beidou.unionsite.online;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;

/**
 * @author Administrator
 * 
 */
public class ObjectReader {

	public static void lastSevenDayStat(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(fileName));
		List<SiteStatBo> result = (List<SiteStatBo>) is.readObject();
		System.out.println(result.size());
		is.close();
	}

	public static void readUnionSiteIndex() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream("d:\\My Documents\\download\\unionsiteindex.dat"));
		int size = 0;
		while (true) {
			try {
				UnionSiteIndex index = (UnionSiteIndex) is.readObject();
				if (index.getDomain().equals("tianyabook.com")) {
					System.out.println(index);
				}
				size++;
			} catch (EOFException e) {
				System.out.println("end");
				break;
			}
		}
		System.out.println(size);
		is.close();
	}

	public static void readObject(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream file = new FileInputStream(fileName);
		ObjectInputStream is = new ObjectInputStream(file);
		// System.out.println(file.available());
		// is.readObject();
		// System.out.println(file.available());
		// is.readObject();
		// System.out.println(file.available());
		// is.readObject();
		while (file.available() > 0) {
			System.out.println(is.readObject());
		}
		is.close();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// readObject("obj.dat");
		readUnionSiteIndex();
		// ObjectInputStream is = new ObjectInputStream(new
		// FileInputStream("E:\\Beidou
		// 1.0.0\\beidou-cron\\unionsite\\q_200904220257"));
		// ObjectInputStream is = new ObjectInputStream(new
		// FileInputStream("obj.dat"));
		// ObjectInputStream is = new ObjectInputStream(new
		// FileInputStream("E:\\Beidou
		// 1.0.0\\beidou-cron\\unionsite\\daysitestat.2"));
		// lastSevenDayStat("E:\\Beidou
		// 1.0.0\\beidou-cron\\unionsite\\lastsevensitestat");
		// List<QValue> obj = (List<QValue>)is.readObject();
		// System.out.println(obj.size());

		//
		// obj = (ObjectWriter)is.readObject();
		// System.out.println(obj.getId());
		// Object o = null;
		//
		// int i=0;
		// try {
		// do{
		// o = is.readObject();
		// SiteStatBo bo = (SiteStatBo)o;
		// System.out.println(bo.getDomain()+'\t'+bo.getCntn());
		// i++;
		// }while(o!=null);
		// } catch (java.io.EOFException e) {
		// }
		// System.out.println(i);
		// is.close();

		// is = new ObjectInputStream(new FileInputStream("E:\\Beidou
		// 1.0.0\\beidou-cron\\unionsite\\unionsiteindex.dat.old"));
		//
		// List<UnionSiteIndex> objold = (List<UnionSiteIndex>)is.readObject();
		// System.out.println(objold.size());
		// is.close();

		// RandomAccessFile file = new RandomAccessFile("access.dat", "rw");
		// file.writeChars("hello2");
		// file.close();
	}
}
