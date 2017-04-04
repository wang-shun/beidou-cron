package com.baidu.beidou.unionsite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.baidu.beidou.unionsite.vo.SiteElement;
import com.baidu.beidou.unionsite.vo.TradeSiteElement;

/**
 * @author zhuqian
 *
 */
public class SiteTradeXmlParser {
	
	private Document dom;
	private String filepath;
	
	public SiteTradeXmlParser(String filepath){
		this.filepath = filepath;
	}
	
	public void createXmlFile(List<TradeSiteElement> trades){
		
		createDocument();		
		createDOMTree(trades);
		printToFile();
		
	}
	
	/**
	 * This method uses Xerces specific classes prints the XML document to file.
	 */
	private void printToFile() {

		// 可以使用 addDocType() 方法添加文档类型说明
		// 这样就向 XML 文档中增加文档类型说明：
		// <!DOCTYPE catalog SYSTEM "catalog.dtd">
		// document.addDocType("catalog",null,"catalog.dtd");

		XMLWriter xw = null;
		try {
			FileOutputStream fos = new FileOutputStream(filepath);
			// 用于格式化输出
			// OutputFormat of = OutputFormat.createPrettyPrint();
			// 格式化输出的另一个形式，不知这两种有什么区别
			// 第1个参数为格式化输出缩排字符,此处为空格,第2个参数true为换行输出,false为单行输出
			OutputFormat of = new OutputFormat("\t", true);
			// 输出为GBK码解决在windows下某些系统下打开含有中文xml乱码的情况
			of.setEncoding("UTF-8");
			xw = new XMLWriter(fos, of);
			xw.write(dom);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (xw != null) {
					xw.close();
				}
			} catch (IOException e) {
				// ignore
			}

		}
	}
	
	/**
	 * Using JAXP in implementation independent manner create a document object
	 * using which we create a xml tree in memory
	 */
	private void createDocument() {


			dom =  DocumentHelper.createDocument();

	}
	
	/**
	 * Helper method which creates a XML element 
	 * @param b The book for which we need to create an xml representation
	 * @return XML element snippet representing a book
	 */
	private Element createTradeElement(Element rootElement, TradeSiteElement trade){

		Element tradeEle = rootElement.addElement("trade");

		//id
		Element idEle = tradeEle.addElement("id");
		idEle.setText(String.valueOf(trade.getId()));

		//name
		Element nameEle = tradeEle.addElement("name");
		nameEle.setText(trade.getName());

		Element siteList = tradeEle.addElement("sites");
		
		if(!CollectionUtils.isEmpty(trade.getSites())){
			
			for(SiteElement site : trade.getSites()){
				Element siteEle = siteList.addElement("site");
				
				//id
				Element siteIdEle = siteEle.addElement("id");
				siteIdEle.setText(String.valueOf(site.getId()));

				//name
				Element siteNameEle = siteEle.addElement("name");
				siteNameEle.setText(site.getName());
			}
			
		}
		
		return tradeEle;

	}

	/**
	 * The real workhorse which creates the XML structure
	 */
	private void createDOMTree(List<TradeSiteElement> trades){

		//create the root element 
		Element rootEle = dom.addElement("root");
		
		for(TradeSiteElement trade : trades) {
			//For each Book object  create  element and attach it to root
			createTradeElement(rootEle, trade);
		}

	}


}
