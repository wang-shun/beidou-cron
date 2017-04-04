/**
 * 
 */
package com.baidu.beidou.unionsite.online;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.unionsite.bo.UnionSiteIndex;

/**
 * @author Administrator
 * 
 */
public class ObjectWriter implements Serializable {

	private static final long serialVersionUID = 8257264486247287710L;

	private int id = 0;

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("obj.dat"));
		// ObjectWriter obj = new ObjectWriter();
		// obj.setId(3);
		// os.writeObject(obj);
		// // obj = new ObjectWriter();
		// os.flush();
		// os.close();
		// os = new ObjectOutputStream(new FileOutputStream("obj.dat", true));
		// obj.setId(4);
		// os.writeObject(obj);
		// os.flush();
		// os.close();
		List<UnionSiteIndex> list = new ArrayList<UnionSiteIndex>(3);
		UnionSiteIndex index = null;
		index = new UnionSiteIndex();
		index.setDomain("sina.com");
		index.setCname("gpr");
		index.setDomainFlag((byte) 0);
		index.setStart(0);
		index.setLength(100);
		list.add(index);
		os.writeObject(index);

		index = new UnionSiteIndex();
		index.setDomain("sohu.com");
		index.setCname("gpr2");
		index.setDomainFlag((byte) 0);
		index.setStart(100);
		index.setLength(300);
		list.add(index);
		os.writeObject(index);

		os.close();

	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

}
