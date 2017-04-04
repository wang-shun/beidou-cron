package com.baidu.beidou.cprounit.ubmcdriver;

import java.util.List;
import java.util.Map;

import com.baidu.ubmc.bc.RpcBatchResponse;
import com.baidu.ubmc.bc.RpcResponse;
import com.baidu.ubmc.bc.Text;

/**
 * ClassName: UbmcDriver
 * Function: ubmc接口
 * 		依赖ubmc-rpc-j.jar中UbmcBatchService接口
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public interface UbmcDriver {
	
	/**
	 * 物料插入。
	 * <ul>
	 * <li>插入物料，默认使用版本号1</li>
	 * 
	 * <li>参数中的medias数组与value中的空白即不包含id的%%BEGIN_MEDIA%%%%END_MEDIA%%占位符一一对应；
	 * 参数中占位符中的id若为空，表示新增富媒体物料；否则表示已有的富媒体物料</li>
	 * 
	 * </ul>
	 * 
	 * @param texts
	 * 			Text
	 * 			{
	 * 				value:非空，必填,
	 * 				medias: 
	 * 					[ 
	 * 						{ 
	 * 							binaryData：二进制数据 ,
	 * 							attributes:富媒体属性
	 * 						} 
	 * 					]
	 * 			}
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 *			{ 
	 *				mcId: [long],物料中心分配的文本物料mcId,
	 *				versionId:[int]1,
	 *				value:[String]新的物料文本value，包含对原有的空白占位符替换成实际富媒体物料id后的值,
	 *				previewValue:[String] 相关内容被替换成可预览格式的文本value
	 *			}
	 */
	public RpcBatchResponse insert(List<Text> texts);

	/**
	 * 物料更新API。
	 * <ul>
	 * <li>参数中的medias数组与value中的空白即不包含id的%%BEGIN_MEDIA%%%%END_MEDIA%%占位符一一对应；
	 * 参数中占位符中的id若为空，表示新增富媒体物料；否则表示已有的富媒体物料</li>
	 * 
	 * <li>操作产品线必须要有已有富媒体物料的权限才能使用已有富媒体物料。</li>
	 * 
	 * </ul>
	 * 
	 * @param texts
	 * 			Text
	 * 			{
	 * 				value:非空，必填 mcId:非空，必填
	 * 				versionId：版本Id，若为空则为默认版本
	 * 				lock:若之前有对该文本加锁，提供该key才能修改,必须是版本锁。
	 * 				medias: 
	 * 					[
	 * 						{
	 *            				binaryData:富媒体二进制数据,
	 *            				attributes::富媒体属性
	 *            			}
	 *            		]
	 * 			}
	 * @param mediaDelDelay
	 *            对于丢且的富媒体是否采用延迟删除,默认为true
	 * @return RpcBatchResponse,如果成功,Map中的内容
	 * 			{
	 * 				mcId: [long],与请求参数的一致,
	 * 				versionId:[long]与请求参数一致,
	 * 				value:[String]新的物料文本value，包含对原有的空白占位符替换成实际富媒体物料id后的值,
	 * 				previewValue:[String] 相关内容被替换成可预览格式的文本value
	 * 			}
	 */
	public RpcBatchResponse update(List<Text> texts, Boolean mediaDelDelay);

	/**
	 * 删除文本物料引用。
	 * 
	 * @param texts,里面需要设置的参数:
	 * 			Text
	 * 			{
	 * 				mcId:文本物料mcid. 
	 * 				versionId:文本物料版本id,
	 * 				lock:相关锁，如果已经有加锁
	 * 			}
	 * 
	 * @param isDelayed
	 *            是否是延迟删除,true表示是，默认为true
	 * 
	 * @return RpcBatchResponse，无数据值
	 */
	public RpcBatchResponse remove(List<Text> texts, Boolean isDelayed);

	/**
	 * 拷贝物料。
	 * <ul>
	 * <li>拷贝文本物料，对于里面的富媒体物料采用引用的方式</li>
	 * 
	 * <li>仅拷贝物料内容，不拷贝产品线引用信息</li>
	 * </ul>
	 * 
	 * @param texts
	 * 			Text
	 * 			{
	 * 				mcId:非空，必填 
	 * 				versionId:版本Id，若为空则为默认版本
	 * 			}
	 * 
	 * @return RpcBatchResponse,如果成功,Map中的内容：
	 * 			{
	 * 				mcId: [long],物料中心分配的文本物料mcId,
	 *				versionId:[int]1,
	 * 				value:[String]新的物料文本value，包含对原有的空白占位符替换成实际富媒体物料id后的值,
	 *				previewValue:[String] 相关内容被替换成可预览格式的文本value
	 *			}
	 */
	public RpcBatchResponse copy(List<Text> texts);

	/**
	 * 文本物料获取操作。
	 * 
	 * @param texts
	 *			Text
	 * 			{
	 * 				mcId:非空，必填 
	 * 				versionId:版本Id，若为空则为默认版本 
	 * 			}
	 * @param isPreview
	 *            如果为true，则相关占位属性标签会被去除，会按照展现的样式对相关占位符进行替换。
	 *            比如点击URL占位符和富媒体URL（默认按24小时展现时间限制策略生成）； 否则是原始存储样式。默认为false
	 * @return RpcBatchResponse,如果成功,Map中的内容：
	 * 			{ 
	 * 				value:[String]文本物料value
	 * 			}
	 */
	public RpcBatchResponse get(List<Text> texts, Boolean isPreview);

	/**
	 * 物料的版本添加操作。
	 * <ul>
	 * 
	 * <li>2种版本添加方式：基于已有版本以及直接版本插入的方式。</li>
	 * 
	 * <li>基于已有的版本，对于里面的富媒体物料使用引用的方式，如果需要拷贝，则需要业务系统先读取物料内容，然后通过传递参数来新生成一份，
	 * 结果返回不同的mediaId。</li>
	 * </ul>
	 * 
	 * @param texts
	 * 			Text 
	 * 			{
	 * 				mcId：物料id, 
	 * 				versionId:拷贝该版本id对应的内容到新版本，若为空则为默认版本
	 * 				value:value串（若为空，则按照mcId和versionId指定的版本添加物料版本；否则按照value串以及富媒体内容添加相关物料版本。）,
	 * 				medias: 
	 * 					[
	 * 						{
	 * 							binaryData：二进制数据 , 
	 * 							attributes:富媒体属性
	 * 						}
	 * 					]
	 * 			}
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 * 			{
	 * 				mcId: [long],物料中心分配的文本物料mcId,
	 * 				versionId:[int]1,
	 * 				value:[String]新的物料文本value，包含对原有的空白占位符替换成实际富媒体物料id后的值,
	 * 				previewValue:[String] 相关内容被替换成可预览格式的文本value
	 * 			}
	 */
	public RpcBatchResponse addVersion(List<Text> texts);

	/**
	 * 获取物料的所有版本。
	 * <ul>
	 * 
	 * <li>返回的versions数组数据，按照版本添加的递增顺序返回。</li>
	 * 
	 * <li>由于批量数的限制，物料中心默认只对num数100以内的物料返回value值，其余的需要自行调用get接口。</li>
	 * </ul>
	 * 
	 * @param mcId
	 *            文本物料的mcid，必须字段
	 * @param filterNoAuth
	 *            如果为true，则过滤掉没有权限的版本，默认为true
	 * @param endId
	 *            版本范围的结束值（不包括该结束值），若不设置默认为最大范围
	 * @param startId
	 *            版本范围的起始值（包含该起始值），若不设置默认为0
	 * @param num
	 *            最大版本数，如果为空表示不限制。
	 * @param isPreview
	 *            如果为true，则相关占位属性标签会被去除，会按照展现的样式对相关占位符进行替换，比如点击URL占位符和富媒体URL（
	 *            默认按24小时展现时间限制策略生成）；否则是原始存储样式。默认为false
	 * 
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 * 			{ 
	 * 				versions: 
	 * 					[ 
	 * 						{ 
	 * 							versionId:[long]版本id, 
	 * 							hasAuth:[boolean]是否有权限, 
	 * 							value:[String]物料文本value 
	 * 						} 
	 * 					] 
	 * 			}
	 */
	public RpcResponse getVersions(Long mcId, Boolean filterNoAuth, Integer startId, 
			Integer endId, Integer num, Boolean isPreview);

	/**
	 * 富媒体物料的获取接口。
	 * 
	 * @param mediaIds
	 *			富媒体物料Id
	 * @param urlStrategy
	 * 			url生成策略 <li>null或0：表示无限制</li> <li>
	 *			1：时间戳限制，Map中需要包含time参数，类型为long，表示时间限制的秒数，默认为24小时</li>
	 * @param withContent
	 *			如果为true，则返回富媒体的二进制内容以及属性，否则只返回url；默认为false
	 * @param strategyAtt
	 *			根据urlStrategy有不同的取值
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 * 			{ 
	 * 				url: [String]根据生成策略生成的物料中心url
	 *				binaryData:[byte[]]富媒体二进制内容, 
	 *				attributes:[String]用户自定义富媒体属性 
	 *			}
	 */
	public RpcBatchResponse getMedia(List<Long> mediaIds, Integer urlStrategy,
			Map<String, String> strategyAtt, Boolean withContent);

	/**
	 * 物料版本级别权限授予接口。<br>
	 * 
	 * 对于文本物料中重复的富媒体只加锁一次,需要避免这类使用。
	 * 
	 * @param texts
	 * 			Text 
	 * 			{ 
	 * 				mcId:非空，必填, 
	 * 				versionId:版本Id，若为空则为默认版本,
	 * 				lock:如果已经加锁，需要提供锁id 
	 * 			}
	 * @param appId
	 *            产品线Id
	 * @param onlyMedias
	 *            是否只授予里面富媒体的引用权限。默认为true
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 * 			{
	 * 				mediaIds: [List<Long>]文本物料中对应的富媒体物料 
	 * 			}
	 */
	public RpcBatchResponse grantAuthority(List<Text> texts, Integer appId,
			Boolean onlyMedias);

	/**
	 * 文本物料加锁接口。<br>
	 * 
	 * <ul>
	 * 
	 * <li>对文本物料进行加锁，未减少死锁的可能，对于批量的请求，物料中心将按照id递增的顺序进行加锁 。</li>
	 * 
	 * <li>加上的锁如果没有显示调用unlock操作，默认时间后自动失效。</li>
	 * 
	 * <li>该锁为写锁，可防止其他操作对引用的并发写（对除引用外的其他数据不限制），从安全性考虑，该写锁不阻塞读操作</li>
	 * </ul>
	 * 
	 * @param texts
	 * 			Text 
	 * 			{
	 * 				mcId:非空，必填 
	 * 				versionId:版本Id，若为空则为默认版本 
	 * 			}
	 * @param onlyReference
	 *            如果为true，该锁只控制物料引用的增、减改变，默认为true；否则该锁控制整行的数据修改操作，包括引用的增、减。
	 * @return RpcBatchResponse,如果成功,Map中的内容： 
	 * 			{
	 *				lock:[Lock]对文本物料和富媒体物料加上的锁对应的key，用于使用方后续解锁；如果输入参数中有提供非null的lockId，两个值一致。
	 * 			}
	 */
	public RpcBatchResponse lock(List<Text> texts);

	/**
	 * 解锁操作。<br>
	 * 
	 * 解除lock操作加上的锁
	 * 
	 * @param texts
	 * 			Text 
	 * 			{ 
	 * 				mcId:非空，必填, 
	 * 				versionId:版本Id，若为空则为默认版本, 
	 * 				lock:物料锁
	 * 			}
	 * @return RpcBatchResponse 无数据内容。
	 */
	public RpcBatchResponse unlock(List<Text> texts);

    /**
     * Function: 批量操作富媒体物料的封禁状态
     *
     * @param mediaIds 富媒体物料id
     * @param isBlocking true:封禁富媒体， false:解封富媒体
     * @return RpcBatchResponse 结果，包含mcId和versionId
     * 
     *  {
     *      "statusCode":0,
     *      "errorMsg":null,
     *      "responses":[
     *          {
     *              "statusCode":0,
     *              "errorMsg":null,
     *              "result":null
     *          }
     *      ]
     *  }
     */
    public RpcBatchResponse manageBlockStatus(List<Long> mediaIds, boolean isBlocking);

    /**
     * Function: 根据mediaId查询关联文本
     *
     * @param mediaId 富媒体物料ID，非空，必填
     * @return RpcResponse 如果成功，返回结果，包含mcId和versionId
     * {
     *  "statusCode":0,
     *  "errorMsg":null,
     *  "result":{
     *      "data":null,
     *      "binary":null,
     *      "nestedData":[
     *          {
     *              "date":{
     *                  "mcId":"23143",
     *                  "versionId":"1"
     *              },
     *              "binary":null,
     *              "nestedData":null
     *          },
     *          ...
     *      ]
     *  }
     * }
     */
    public RpcResponse getRelatedText(Long mediaId);
}
