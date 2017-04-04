/**
 * beidou-cron-640#com.baidu.beidou.util.PageUtil.java
 * 上午10:56:08 created by kanghongwei
 */
package com.baidu.beidou.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * @author kanghongwei
 * @fileName PageUtil.java
 * @dateTime 2013-10-23 上午10:56:08
 */

public class PageUtil {

	/**
	 * see {@link com.baidu.beidou.util.page.DataPage}
	 * @param adList
	 * @param pageSize
	 * @return
	 */
	@Deprecated
	public static <A extends Object> List<List<A>> pageAds(List<A> adList, int pageSize) {
		if (CollectionUtils.isEmpty(adList)) {
			return Collections.emptyList();
		}

		int pageNum = (adList.size() / pageSize) + 1;
		if ((adList.size() % pageSize) == 0) {
			pageNum -= 1;
		}

		List<List<A>> result = new ArrayList<List<A>>(pageNum);
		for (int i = 0; i < pageNum; i++) {
			int from = i * pageSize;
			int to = (i + 1) * pageSize;
			if (to > adList.size()) {
				to = adList.size();
			}
			List<A> item = adList.subList(from, to);
			result.add(item);
		}

		return result;
	}

}
