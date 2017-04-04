package com.baidu.beidou.unionsite.constant;

import junit.framework.TestCase;

import com.baidu.beidou.unionsite.constant.SiteConstant;

public class SiteConstantTest extends TestCase {

	public void testGetIPLevelEnumValue() {
		assertEquals(1, SiteConstant.getIPLevelEnumValue(10000));
		assertEquals(2, SiteConstant.getIPLevelEnumValue(50000));
		assertEquals(3, SiteConstant.getIPLevelEnumValue(100000));
		assertEquals(4, SiteConstant.getIPLevelEnumValue(1000001));
	}

	public void testGetUVLevelEnumValue() {

		assertEquals(1, SiteConstant.getUVLevelEnumValue(10000));
		assertEquals(2, SiteConstant.getUVLevelEnumValue(50000));
		assertEquals(3, SiteConstant.getUVLevelEnumValue(100000));
		assertEquals(4, SiteConstant.getIPLevelEnumValue(1000001));
	}
}
