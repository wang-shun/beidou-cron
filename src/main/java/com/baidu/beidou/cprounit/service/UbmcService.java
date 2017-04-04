package com.baidu.beidou.cprounit.service;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseLite;

public interface UbmcService {
	
	/**
	 * insert: 向ubmc插入物料
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 3, 2013
	 */
	public List<ResponseBaseMaterial> insert(List<RequestBaseMaterial> units);
	
	/**
	 * update: ubmc更新物料
	 *  mcId和value必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> update(List<RequestBaseMaterial> units);
	
	/**
	 * remove: ubmc移除物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * 返回结果仅包含mcId和versionId
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> remove(List<RequestBaseMaterial> units);
	
	/**
	 * copy: ubmc拷贝物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> copy(List<RequestBaseMaterial> units);
	
	/**
	 * get: ubmc获取物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * isPreview：用来表示是否获取预览URL，true为url，false为mediaId
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> get(List<RequestBaseMaterial> units, Boolean isPreview);
	
	/**
	 * addVersion: ubmc为某一mcId指定版本的物料拷贝一份
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> addVersion(List<RequestBaseMaterial> units);
	
	/**
	 * generateMaterUrl: 获取临时URL 
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jun 12, 2013
	 */
	public Map<RequestBaseMaterial, String> generateMaterUrl(List<RequestBaseMaterial> units);

	/**
	 * getTmpUrl: 获取临时url
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jun 12, 2013
	 */
	public String getTmpUrl(Long mcId, Integer versionId);
	
	/**
	 * getMediaData: 通过ubmc获取某一mcId指定版本的二进制文件
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public byte[] getMediaData(Long mcId, Integer versionId);
	
	/**
	 * getMediaData: 通过ubmc获取某一fileSrc指定版本的二进制文件
	 * fileSrc必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public byte[] getMediaData(String fileSrc);
	
	/**
	 * getMediaIdFromFileSrc: 通过fileSrc解析出mediaId
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 18, 2013
	 */
	public Long getMediaIdFromFileSrc(String fileSrc);
	
	/**
	 * getMedia: 通过mediaId获取图片信息
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 22, 2013
	 */
	public byte[] getMediaData(Long mediaId);
	
	/**
	 * getMedia: 通过mediaIds批量获取图片信息
	 * 		如果某一个mediaId对应的二进制文件获取不到，则将其对应位置的返回null
	 * 		即该接口支持部分获取，传入参数列表的长度和结果保持一致
	 * 
	 * 注意：该接口需要考虑批量的数量，数量太大会占用很大内存
	 * @version cpweb-640
	 * @author genglei01
	 * @date Oct 17, 2013
	 */
	public List<byte[]> getMediaData(List<Long> mediaIds);

    /**
     * Function: 通过mediaId使得ubmc下线检索端的多媒体展示
     * 
     * @author genglei01
     * @param mediaId 多媒体ID
     * @return 是否成功
     */
    public boolean offlineMedia(Long mediaId);

    /**
     * Function: 通过mediaId获取ubmc中引用该多媒体的物料
     * 
     * @author genglei01
     * @param mediaId 多媒体ID
     * @return 返回引用该多媒体ID的mcId和versionId
     */
    public List<ResponseLite> getRelatedText(Long mediaId);
    
}
