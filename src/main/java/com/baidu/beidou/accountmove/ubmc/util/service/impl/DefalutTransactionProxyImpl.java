package com.baidu.beidou.accountmove.ubmc.util.service.impl;

import com.baidu.beidou.accountmove.ubmc.util.service.TransactionProxy;

public class DefalutTransactionProxyImpl implements TransactionProxy {

	@Override
	public void commitTask(TransTask task) {
		task.execute();
	}
}
