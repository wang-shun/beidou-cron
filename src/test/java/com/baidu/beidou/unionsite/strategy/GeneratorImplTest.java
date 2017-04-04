package com.baidu.beidou.unionsite.strategy;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import com.baidu.beidou.unionsite.strategy.impl.GenerateSiteLinkPrefixByClickHistory;
import com.baidu.beidou.unionsite.strategy.impl.GenerateSiteLinkPrefixByDotCount;

public class GeneratorImplTest extends TestCase {

	public void testGenerateSiteLinkPrefixByClickHistory() {
		GenerateSiteLinkPrefixByClickHistory generator = new GenerateSiteLinkPrefixByClickHistory();
		URL in = GeneratorImplTest.class.getClassLoader().getResource("base.txt");
		String file = in.getPath();
		generator.setFile(file);
		generator.setAssitantGenerator(new GenerateSiteLinkPrefixByDotCount());
		// 163.com
		// jiaju.sina.com.cn
		// www.263.com
		try {
			generator.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(GenerateSiteLinkPrefixByClickHistory.PROTOCOL_PREFIX, generator.generatePrefix("163.com"));
		assertEquals(GenerateSiteLinkPrefixByClickHistory.PROTOCOL_PREFIX, generator.generatePrefix("www.263.com"));
		assertEquals(GenerateSiteLinkPrefixByClickHistory.DEFAULT_PREFIX, generator.generatePrefix("363.com"));
		assertEquals(GenerateSiteLinkPrefixByClickHistory.DEFAULT_PREFIX, generator.generatePrefix("adf.363.com"));
		assertEquals(GenerateSiteLinkPrefixByClickHistory.PROTOCOL_PREFIX, generator.generatePrefix("adf.363.com.cn"));
		assertEquals(GenerateSiteLinkPrefixByClickHistory.PROTOCOL_PREFIX, generator.generatePrefix("jiaju.sina.com.cn"));
	}
}
