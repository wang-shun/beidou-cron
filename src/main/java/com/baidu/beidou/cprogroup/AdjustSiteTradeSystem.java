package com.baidu.beidou.cprogroup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.facade.CproGroupFacade;

/**
 * @author zhuqian
 * 
 */
public class AdjustSiteTradeSystem {

	private static final Log log = LogFactory.getLog(AdjustSiteTradeSystem.class);

	private static final String CPROGROUPSERVICE_BEAN_NAME = "cproGroupFacade";

	private static CproGroupFacade cproGroupFacade;

	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			log.error("Usage: AdjustSiteTradeSystem [mapping_file_path] [all_first_trade_file] [old_all_first_trade_file]");
			throw new Exception("Usage: AdjustSiteTradeSystem [mapping_file_path] [all_first_trade_file] [old_all_first_trade_file]");
		}

		String mappingFilepath = args[0];
		String allFirstTradeFilepath = args[1];
		String oldAllFirstTradeFilepath = args[2];

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" };

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		cproGroupFacade = (CproGroupFacade) ctx.getBean(CPROGROUPSERVICE_BEAN_NAME);

		if (cproGroupFacade == null) {
			log.error("service class cproGroupMgr is null");
			System.exit(1);
		}

		cproGroupFacade.adjustSiteTradeSystem(getMappingFile(mappingFilepath), getAllFirstTradeList(allFirstTradeFilepath), getAllFirstTradeList(oldAllFirstTradeFilepath));
	}

	@SuppressWarnings("null")
	public static Map<Integer, Integer> getMappingFile(String mappingFilepath) throws Exception {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		try {

			DataInputStream in = new DataInputStream(new FileInputStream(mappingFilepath));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				String[] items = strLine.split("\\,");
				if (items == null && items.length != 2) {
					continue;
				}
				try {
					Integer key = new Integer(items[0]);
					Integer value = new Integer(items[1]);
					result.put(key, value);
				} catch (NumberFormatException e) {
					continue;
				}
			}
			// Close the input stream
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			throw e;
		}

		return result;
	}

	public static Set<Integer> getAllFirstTradeList(String allFirstTradeFilepath) throws Exception {

		Set<Integer> result = new HashSet<Integer>();

		try {

			DataInputStream in = new DataInputStream(new FileInputStream(allFirstTradeFilepath));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				try {
					Integer id = new Integer(strLine);
					result.add(id);
				} catch (NumberFormatException e) {
					continue;
				}
			}
			// Close the input stream
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			throw e;
		}

		return result;
	}

}
