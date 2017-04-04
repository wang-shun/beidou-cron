/**
 * 2009-8-20 下午02:10:43
 * @author zengyunfeng
 */
package com.baidu.beidou.cprounit.mcdriver;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcActiveBatchResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchBackupResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchCopyResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchRemoveResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcMediaResultBean;

public interface RawMcDriverProxy {
	
	DrmcActiveBatchResultBean tmpActiveBatch(List<Map<String, Long>> arrTmpActIds,
			boolean bolDelTmp, boolean bolErrorExit, Map<String, String> params);

	/**
	 * 批量获取正式物料
	 * @param ids
	 * @param querytype 简单查询，即只需返回key：value形式，设为0
	 * @param ismaster 若是在刚刚提交之后立即获取，则设为true
	 * @return
	 */
	DrmcBatchResultBean getElements(long[] ids, int querytype, boolean ismaster, Map<String, String> params);
	/**
	 * 批量获取临时物料
	 * 参数设置同getElements
	 */
	DrmcBatchResultBean getTmpElements(long[] ids, int querytype, boolean ismaster, Map<String, String> params);
	/**
	 * 批量获取历史物料
	 * 参数设置同getElements
	 */
	DrmcBatchResultBean getHisElements(long[] ids, int querytype, boolean ismaster, Map<String, String> params);
	/**
	 * 批量提交临时物料
	 * @param ads InsertLiteralRequestBean/InsertImageRequestBean/InsertImageRequestBean2组成的list
	 * @param errorExit 处理过程中若出错是否终止
	 * @return
	 */
	DrmcBatchResultBean tmpInsertBatch(List ads, boolean errorExit, Map<String, String> params);
	/**
	 * 更新文字临时物料，允许指定不同的新旧类型
	 * @param ads UpdateLiteralRequestBean/UpdateImageRequestBean/UpdateImageRequestBean2组成的list
	 * @param errorExit 处理过程中若出错是否终止
	 * @return
	 */
	DrmcBatchResultBean tmpUpdateBatch(List ads, boolean errorExit, Map<String, String> params);
	/**
	 * 批量拷贝正式物料
	 * @param ids
	 * @param copytype 0：复制后物料为正式；1：复制后物料为临时
	 * @param errorExit 处理过程中若出错是否终止
	 * @return
	 */
	DrmcBatchCopyResultBean copy(long[] ids, int copytype, boolean errorExit, Map<String, String> params);
	/**
	 * 批量拷贝临时物料
	 * @param tmpids 
	 * @param errorExit 处理过程中若出错是否终止
	 * @return
	 */
	DrmcBatchCopyResultBean tmpCopy(long[] tmpids, boolean errorExit, Map<String, String> params);
	/**
	 * 备份正式物料
	 */
	DrmcBatchBackupResultBean backup(long[] ids, Map<String, String> params);
	/**
	 * 备份临时物料
	 */
	DrmcBatchBackupResultBean tmpBackup(long[] ids, Map<String, String> params);
	/**
	 * 删除正式物料
	 */
	DrmcBatchRemoveResultBean remove(long[] ids, Map<String, String> params);
	/**
	 * 删除临时物料
	 */
	DrmcBatchRemoveResultBean tmpRemove(long[] tmpids, Map<String, String> params);
	/**
	 * 删除历史物料
	 */
	DrmcBatchRemoveResultBean hisRemove(long[] hisids, Map<String, String> params);
	/**
	 * 获取多媒体物料二进制数据
	 */
	DrmcMediaResultBean getMedia(String url, Map<String, String> params);
}
