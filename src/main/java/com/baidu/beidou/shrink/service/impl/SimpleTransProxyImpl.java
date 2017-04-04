package com.baidu.beidou.shrink.service.impl;

import com.baidu.beidou.shrink.service.SimpleTransProxy;
import com.baidu.beidou.shrink.service.TransTask;

public class SimpleTransProxyImpl implements SimpleTransProxy {

	@Override
	public void commitTask(TransTask task) {
		task.execute();
	}
//	@Override
//	public void commitTask(Integer userId,TransTask task) {
//		task.execute();
//	}
}
