package com.baidu.beidou.cprounit.mcdriver;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.MaterSuiteResultPlus;
import com.baidu.ubmc.bc.Text;

/**
 * 挂接admaker需要访问的服务接口代理
 * 
 * @author yanjie
 * @version 1.2.3
 */
public interface AmDriverProxy {
	/**
	 * 从信息池获取指定key的info信息
	 * @param key 由drmc/admaker生成，对应于info信息
     * @param params Rpc调用时需要传递的参数列表，通常是消息头。
	 * @return 
	 * beidou传递给drmc/admaker的加密info信息
	 * 异常则返回空串
	 */
	String getInfo(long key, Map<String, String> params);
	/**
	 * 从信息池删除指定key的info信息
	 * @param key 由drmc/admaker生成，对应于info信息
     * @param params Rpc调用时需要传递的参数列表，通常是消息头。
	 * @return
	 * 异常则返回false
	 */
	boolean delInfo(long key, Map<String, String> params);
	
	/**
	 * @param userId  用户的id
	 * @param groupTypes，整形数组。数组中的每个值代表一种展示类型， 
	 * ！！注意这里和北斗不一样，admaker内部的物料类型是：1-固定,2-悬浮,3-贴片。
	 * 数组中的每一个元素都不相同，数组的最小长度为1，最大长度为3。
	 * @param page  获取创意列表的第几页（默认从1开始）
	 * @param pageSize 当前列表每页创意数量
	 * @param params Rpc调用时需要传递的参数列表，通常是消息头。
	 * @return
	 * sumpage:xxx,
	 * count:xxx,
	 * list: [ 
	 *   { amId:XXX,
	 *     amName:XXX,
	 *     abbUrl: XXX, 
	 *     suite:[
	 *             { width: X, 
	 *               height: X, 
	 *               url: X
	 *             },
	 *             {}, {}
	 *           ]
	 *   }, 
	 *   {}, {} 
	 * ]
	 */
	MaterSuiteResultPlus getMixedList (int userId, int[] groupTypes, int page, int pageSize, Map<String, String> params);
	
	/**
	 * grantAuthorityByTextList: 使用Text授权接口
	 * List<Text>	List<Text>	物料Text	必选。
	 * appId	Integer	待授权方appId	必选。
	 * onlyMedias	Boolean	是否只授予富媒体的引用权限	必选。
	 * 
	 * GrantResult格式
	 * 		statusCode:  0 标识成功，其余标识错误。
	 * 		message:  成功返回success。失败返回具体错误信息。
	 * 		mcId: [0-9]+
	 * 		versionId: [0-9]+
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	List<GrantResult> grantAuthorityByTextList(List<Text> list, Integer appId, 
			Boolean onlyMedias, Map<String, String> params);
	
	/**
	 * grantAuthority: 使用Text授权接口
	 * 
	 * descJson: admaker物料中，从二进制文件中解析出的
	 * tpId：admaker物料中的模板id
	 * appId	Integer	待授权方appId	必选。
	 * onlyMedias	Boolean	是否只授予富媒体的引用权限	必选。
	 * 
	 * GrantResult格式
	 * 		statusCode:  0 标识成功，其余标识错误。
	 * 		message:  成功返回success。失败返回具体错误信息。
	 * 		mcId: [0-9]+
	 * 		versionId: [0-9]+
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	GrantResult grantAuthority(String descJson, Long tpId, Integer appId, 
			Boolean onlyMedias, Map<String, String> params);
	
	/**
	 * downloadSwf: 从admaker下载flash图片
	 * 传入Text中的mcId和versionId
	 * 
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	byte[] downloadSwf(String descJson, Long tpId, Map<String, String> params);
	
	/**
	 * downloadDrmcMaterial: 根据descJson和tpId生成drmc中物料
	 * 入参descJson： 通过admaker的jar包，解析出的swf文件描述json
	 * 入参tpid： 通过admaker提供的方法，解析出二进制中的tpid信息
	 * 返回：DRMC URL，任何异常都将返回null
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 24, 2013
	 */
	public String downloadDrmcMaterial(String descJson, Integer tpId, Map<String, String> params);
}
