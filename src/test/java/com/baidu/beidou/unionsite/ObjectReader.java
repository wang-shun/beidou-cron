/**
 * 2009-4-30 上午11:28:09
 */
package com.baidu.beidou.unionsite;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.QValue;
import com.baidu.beidou.unionsite.bo.UnionSiteBo;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;
import com.baidu.beidou.util.file.ObjectAccessUtil;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ObjectReader {

	public static void randomObjectReader() throws IOException, InternalException, ClassNotFoundException {
		RandomAccessFile file = new RandomAccessFile("unionsite/unionsitecache.dat", "r");
		long pos;
		int length;
		pos = 19071011;
		length = 898;
		UnionSiteBo bo = (UnionSiteBo) ObjectAccessUtil.readObject(file, pos, length);
		System.out.println(bo.getSiteUrl() + '\t' + bo.getFirstTradeId() + '\t' + bo.getSencondTradeId());

		pos = 5462548;
		length = 507;
		bo = (UnionSiteBo) ObjectAccessUtil.readObject(file, pos, length);
		System.out.println(bo.getSiteUrl() + '\t' + bo.getFirstTradeId() + '\t' + bo.getSencondTradeId());
	}

	public static void readUnionSiteIndex() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream("unionsite\\unionsiteindex.dat"));
		int size = 0;
		int nulsize = 0;
		while (true) {
			try {
				UnionSiteIndex index = (UnionSiteIndex) is.readObject();
				if (index.getDomain().equals("868.00968.com") || index.getDomain().equals("00968.com")) {
					System.out.println(index.getDomain() + "\t" + index.getStart() + "\t" + index.getLength());
					nulsize++;
				}
				size++;
			} catch (EOFException e) {
				System.out.println("end");
				break;
			}
		}
		System.out.println(size + "\t" + nulsize);
		is.close();
	}

	public static void readUnionSiteIndexBug() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream("D:\\My Documents\\download\\unionsiteindex.dat"));
		int size = 0;
		int nulsize = 0;
		while (true) {
			try {
				UnionSiteIndex index = (UnionSiteIndex) is.readObject();
				if (index.getDomain() == null) {
					System.out.println(index);
					nulsize++;
				}
				if (index.getDomain().endsWith("hao91.cn")) {
					System.out.println(index.getCname());
				}
				size++;
			} catch (EOFException e) {
				System.out.println("end");
				break;
			}
		}
		System.out.println(size + "\t" + nulsize);
		is.close();
	}

	public static void readQValue() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream("unionsite\\q_200904220257"));
		int size = 0;
		int nulsize = 0;
		while (true) {
			try {
				QValue index = (QValue) is.readObject();
				if (index.getDomain().endsWith("52donghua.com")) {
					System.out.println("fu:" + index.getQ1() + '\t' + index.getQ2());
				}
				size++;
			} catch (EOFException e) {
				System.out.println("end");
				break;
			}
		}
		System.out.println(size + "\t" + nulsize);
		is.close();
	}

	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InternalException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, InternalException {
		// TODO Auto-generated method stub
		// readUnionSiteIndex();
		// readSiteStatBug();
		// readSiteStatBug();
		// String siteList=
		// "10081|10145|6585|10130|6101|10093|9538|6592|6156|6171|6594|9749|6582|6068|6340|6532|9382|9761|9178|6503|9171|6074|9747|7675|10080|9798|6192|6140|7285|9042|6548|6173|6544|9175|6076|6158|6465|9864|9342|9335|9609|9196|6184|6561|7812|9833|7272|6557|8649|9949|6213|6513|6706|8684|9356|9164|9969|6517|6204|9119|6510|7464|6600|6356|6565|7073|9850|8250|6507|6571|7626|6352|9442|6188|7880|6346|6099|6202|6358|10089|8489|8276|8593|8131|6478|6095|8555|8633|6206|8494|8800|6497|6084|7415|7403|7881|8938|6624|6296|9090|6154|10008|7946|6389|6411|6276|6431|6106|7882|6409|6626|6152|6423|6415|6273|6299|6634|6108|8005|9750|8657|9118|6302|9136|6421|6417|10012|6602|9711|6439|6476|9662|6658|6598|6361|6256|6308|6647|6147|6403|6433|6396|6636|9288|9363|6260|6446|6315|7856|7203|6448|8211|9466|7407|8429|6264|6643|6701|6367|9266|6381|6042|9322|6131|8432|7292|6452|9073|6051|6363|6693|10036|8998|6038|6138|8335|9986|6311|6684|6687|6125|6123|8946|6034|9907|6114|6550|6662|7085|6474|6247|6472|6670|6534|7972|10073|10151|6552|6379|6682|6249|10152|9726|6536|6055|6375|9733|6470|9659|6377|8136|6680|";
		// String [] sites = siteList.split("\\"+"|");
		// boolean result = ArrayUtils.contains(sites, String.valueOf(6624));
		// System.out.println(result);;
	}
}
