package com.baidu.beidou.cprounit.service;

import java.util.List;

import com.baidu.beidou.cprounit.service.bo.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseIconMaterial;

public interface IconService {


	/**
	 * 插入图标物料
	 * @param list RRequestIconMaterial组成的list
	 *            本接口不限制个数，在接口实现中分批完成
	 *            
	 * @return ResponseIconMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料插入失败，则对应结果项为null
	 */
	List<ResponseIconMaterial> insertBatch(List<RequestIconMaterial> list);
}

