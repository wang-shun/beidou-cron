/**
 * 2009-8-20 下午02:10:43
 * @author zengyunfeng
 */
package com.baidu.beidou.cprounit.service;

import java.util.LinkedHashMap;
import java.util.List;

import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;

/**
 * @author zengyunfeng
 *
 */
public interface DrmcService {

	/**
	 * 生效临时物料
	 * @param tmpids 本接口不限制个数，在接口实现中分批完成
	 * @param fmids 后效后的正式物料的id,数组大小必须和tmpids一致；没有正式物料id的可以为0，表示新增正式物料
	 * @param deltmp 生效后是否删除临时物料
	 * @param errorexit 插入失败后是否退出，true为退出，false为继续插入
	 * @return 返回生效后的正式物料信息，失败的元素返回null. 设置的属性有:<br>
	 *  文字:wid, title, description1,description2,showUrl,targetUrl; <br>
	 *  图片:wid, title, showUrl, targetUrl,fileSrc, height, width;
	 */
	BeidouMaterialBase[] tmpActiveBatch(long[] tmpids, long[] fmids, boolean deltmp, boolean errorexit); 
	
	/**
	 * 批量获取正式物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @param ismaster 若是在刚刚提交之后立即获取，则设为true
	 * @return ResponseLiteralMaterial/ResponseImageMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料查询失败，则对应结果项为null
	 */
	List<BeidouMaterialBase> getElements(long[] ids, boolean ismaster);
	/**
	 * 批量获取临时物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @param ismaster 若是在刚刚提交之后立即获取，则设为true
	 * @return ResponseLiteralMaterial/ResponseImageMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料查询失败，则对应结果项为null
	 */
	List<BeidouMaterialBase> getTmpElements(long[] ids, boolean ismaster);
	/**
	 * 批量获取历史物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @param ismaster 若是在刚刚提交之后立即获取，则设为true
	 * @return ResponseLiteralMaterial/ResponseImageMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料查询失败，则对应结果项为null
	 */
	List<BeidouMaterialBase> getHisElements(long[] ids, boolean ismaster);

	/**
	 * 批量提交临时物料
	 * @param ads RequestLiterialMaterial/RequestImageMaterial/RequestImageMaterial2组成的list
	 *            本接口不限制个数，在接口实现中分批完成
	 * @return ResponseLiteralMaterial/ResponseImageMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料插入失败，则对应结果项为null
	 */
	List<BeidouMaterialBase> tmpInsertBatch(List<BeidouMaterialBase> ads, boolean errorExit);

	/**
	 * 批量修改临时物料
	 * @param ads RequestLiterialMaterial/RequestImageMaterial及oldtype组成的map
	 * oldtype为DRMC_MATTYPE_LITERAL/DRMC_MATTYPE_IMAGE
	 *            本接口不限制个数，在接口实现中分批完成
	 * 若只想修改图片/Flash物料的文本信息，则将RequestImageMaterial的data字段设为null，width/height字段保持原值
	 * @return ResponseLiteralMaterial/ResponseImageMaterial组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty list；若某物料插入失败，则对应结果项为null
	 */
	List<BeidouMaterialBase> tmpUpdateBatch(LinkedHashMap<BeidouMaterialBase, Integer> ads, boolean errorExit);
	
	/**
	 * 拷贝正式物料
	 * @param ids  本接口不限制个数，在接口实现中分批完成
	 * @return 拷贝后物料ID组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty array；若某物料拷贝失败，则对应结果项为DRMC_COPY_FAIL
	 */
	long[] copyBatch(long[] ids, boolean errorExit);
	
	/**
	 * 将正式物料拷贝为临时物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 拷贝后物料ID组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty array；若某物料拷贝失败，则对应结果项为DRMC_COPY_FAIL
	 */
	long[] unactiveBatch(long[] ids, boolean errorExit);
	
	/**
	 * 拷贝临时物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 拷贝后物料ID组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty array；若某物料拷贝失败，则对应结果项为DRMC_COPY_FAIL
	 */
	long[] tmpCopyBatch(long[] tmpids, boolean errorExit);
	
	/**
	 * 将正式物料备份为历史物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 备份后物料ID组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty array；
	 * 若某物料备份失败，则对应结果项为DRMC_BACKUP_FAIL
	 */
	long[] backupBatch(long[] ids);
	/**
	 * 将临时物料备份为历史物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 备份后物料ID组成的list
	 * 若dr-mc失败（包括返回结果条数与输入条数不一致）则返回empty array；
	 * 若某物料备份失败，则对应结果项为DRMC_BACKUP_FAIL
	 */
	long[] tmpBackupBatch(long[] ids);
	/**
	 * 删除正式物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 物料删除状态组成的list
	 * DRMC_STATUS_OK：删除成功
	 */
	int[] removeBatch(long[] ids);
	/**
	 * 删除临时物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 物料删除状态组成的list
	 * DRMC_STATUS_OK：删除成功
	 */
	int[] tmpRemoveBatch(long[] tmpids);
	/**
	 * 删除历史物料
	 * @param ids 本接口不限制个数，在接口实现中分批完成
	 * @return 物料删除状态组成的list
	 * DRMC_STATUS_OK：删除成功
	 */
	int[] hisRemoveBatch(long[] hisids);
	
	/**
	 * 获取物料的二进制数据
	 * @param url 物料地址全路径（drmc api将无视前缀，仅从url中解析id）
	 * @return
	 * 若异常则返回null
	 */
	byte[] getMediaContent(String url);
}

