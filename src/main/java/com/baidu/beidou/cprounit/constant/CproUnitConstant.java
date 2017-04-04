/**
 * 
 */
package com.baidu.beidou.cprounit.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class CproUnitConstant {
	/**
	 * 广告行业分类的无效值（缺省）
	 */
	public static final int UNIT_TRADE_INVALID = 0;
	
	/**
	 *  推广单元提交类型：用户提交
	 */
	public static final int UNIT_SUBMITTYPE_USER = 0;
	
	/**
	 * 推广单元提交类型：代理提交
	 */
	public static final int UNIT_SUBMITTYPE_PROXY = 1;
	
	
	public static final String[] UNIT_SUBMITTYPE_NAME = new String[]{"用户提交","代理提交"};
	
	/**
	 * 推广单元状态：有效
	 */
	public static final int UNIT_STATE_NORMAL = 0;
	
	/**
	 * 推广单元状态：暂停
	 */
	public static final int UNIT_STATE_PAUSE = 1;
	
	/**
	 * 推广单元状态：删除
	 */
	public static final int UNIT_STATE_DELETE = 2;
	
	/**
	 * 推广单元状态：审核中
	 */
	public static final int UNIT_STATE_AUDITING = 3;
	
	/**
	 * 推广单元状态：审核拒绝
	 */
	public static final int UNIT_STATE_REFUSE = 4;
	
	/**
	 * 推广创意是否已从DRMC同步至UBMC：0表示未同步，1表示已同步
	 */
	public static final int UBMC_SYNC_FLAG_NO	= 0; // 未同步
	public static final int UBMC_SYNC_FLAG_YES	= 1; // 已同步
	
	/**
	 * 推广创意是否已从UBMC同步至DRMC：0表示未同步，1表示已同步
	 */
	public static final int DRMC_SYNC_FLAG_NO	= 0; // 未同步
	public static final int DRMC_SYNC_FLAG_YES	= 1; // 已同步
	
	/**
	 * 推广单元状态的显示名称
	 * added by zhuqian 2009-03-18
	 */
	public static final String[] UNIT_STATE_NAME = new String[]{"有效","暂停","删除","审核中","审核拒绝"};
	
	/**
	 * 推广单元是否已同步：已同步
	 */
	public static final int UNIT_SYN = 1;
	
	/**
	 * 推广单元是否已同步：未同步
	 */
	public static final int UNIT_NOT_SYN = 0;
	
	/**
	 * 推广单元文字物料是否允许只出标题：允许
	 */
	public static final int UNIT_IFTITLE = 1;
	
	/**
	 * 推广单元文字物料是否允许只出标题：不允许
	 */
	public static final int UNIT_NOT_IFTITLE = 0;
	
	/**
	 * flash版本的要求 
	 */
	public static final int FLASH_VERSION = 7;
	
	
	
	/**
	 * 推广单元物料类型：文字
	 */
	public static final int MATERIAL_TYPE_LITERAL = 1;
	
	
	/**
	 * 图文物料默认尺寸
	 */
	public static final int LITERAL_WITH_ICON_DEFAULT_WIDTH = 60;
	public static final int LITERAL_WITH_ICON_DEFAULT_HEIGHT = 60;
	/**
	 * 图标物料默认的最大尺寸
	 */
	public static final int LITERAL_WITH_ICON_DEFAULT_SIZE = 56320;
	
	
	/**
	 * 推广单元物料类型：图片
	 */
	public static final int MATERIAL_TYPE_PICTURE = 2;
	
	/**
	 * 推广单元物料类型：flash
	 */
	public static final int MATERIAL_TYPE_FLASH = 3;
	
	/**
	 * 推广单元物料类型：图文混排
	 */
	public static final int MATERIAL_TYPE_LITERAL_WITH_ICON = 5;
	
	/**
	 * 推广单元物料类型：智能创意
	 */
	public static final int MATERIAL_TYPE_SMART_IDEA = 9;
	
	/**
	 * 智能创意标志
	 */
	public static final int IS_SMART_TRUE = 1;
	public static final int IS_SMART_FALSE = 0;

    /**
     * 霓裳物料类型：霓裳入门版制作的图片
     */
    public static final int MATERIAL_TYPE_NICHANG = 96;
    
	/**
	 * 北斗预览工具图片类型：预览工具上传的图片或者flash等
	 */
	public static final int MATERIAL_TYPE_PREVIEW_IMAGE = 97;
	
	/**
	 * admaker物料类型：admaker制作的图片或者flash
	 */
	public static final int MATERIAL_TYPE_ADMAKER = 98;
	
	/**
	 * 推广单元物料类型：图标（业务端使用，存储于drmc，以便解析处理）
	 */
	public static final int MATERIAL_TYPE_ICON = 99;
	
	/**
	 * 物料类型的显示名称
	 * added by zhuqian 2009-03-05
	 */
	public static final String[] WULIAO_TYPE_NAME = new String[]{"","文字","图片","Flash","","图文"};
	
	/**
	 * MC的物料展现请求url
	 */
	public static String MC_MATERIAL_URL = "";
	
	/**
	 * MC的物料下载请求url
	 */
	public static String MC_DOWNLOAD_MATERIAL_URL = "";
	
	/**
	 * MC的删除物料的请求url
	 */
	public static List<String> MC_DEL_MATERIAL_URL = new ArrayList<String>(4);
	
	/**
	 * 后台提交物料的请求url
	 */
	public static List<String> MC_BAK_SUBMIT_MATERIAL_URL = new ArrayList<String>(4);
	
	/**
	 * MC的拷贝物料的请求url
	 */
	public static List<String> MC_COPY_MATERIAL_URL = new ArrayList<String>(4);
	
	/**
	 * MC的物料提交页面的url
	 */
	public static String MC_SUBMIT_MATERIAL_URL = "";
	
	/**
	 * EIV地址，数据库中存储的物料地址加上该url,才是完整的物料地址
	 */
	public static String MC_EIV_URL = "";
	
	/**
	 * MC的物料同步请求的url
	 */
	public static final List<String> MC_SYN_REQUEST_URL = new ArrayList<String>(4);
	
	
	public static String MC_BEIDOU_SYSID = "5";
	
	public static int MC_TIMEOUT_MATERIAL = 0;
	
	
	public static int MC_TIMEOUT_SYN_QUERY = 0;
	
	public static final char MC_CODE_REPLACE_SLASH= '-';
	
	
	/**
	 * 页面的缩略图的最大宽度
	 */
	public static final int PAGE_IMG_WIDTH = 240;
	
	/**
	 * 页面缩略图的最大高度
	 */
	public static final int PAGE_IMG_HEIGTH = 100;
	
	/**
	 * 同一个推广组下的最多推广单元数
	 */
	public static int MAX_UNIT_NUMBER =10000;
	
	/**
	 * 物料同步单次最大发送个数
	 */
	public static int MAX_UNSYN_MATERIAL_SIZE = 100;
	
	/**
	 * 物料提交单次最大发送个数
	 */
	public static int MAX_SUBMIT_MATERIAL_SIZE = 1000;
	
	/**
	 * 物料复制单次最大发送个数
	 */
	public static int MAX_COPY_MATERIAL_SIZE = 1000;
	
	public static final String MC_MATERIAL_NAME= "material";
	
	/**
	 * 列表数据的返回json格式的名称
	 */
	public static final String DATA_KEY = "dataList";
	
	/**
	 * 起始时间的返回json格式的名称
	 */
	public static final String DATE_START_KEY = "dateStart";
	
	/**
	 * 结束时间的返回json格式的名称
	 */
	public static final String DATE_END_KEY = "dateEnd";
	
	/**
	 * 已经进入过审核员列表中的状态辅助位,辅助位为1的推广单元不能进行修改
	 */
	public static final int UNIT_AUDITING= 1;	
	
	/**
	 * 已经进入过审核员列表中的状态辅助位,辅助位为1的推广单元不能进行修改
	 */
	public static final int UNIT_MAN_CATEGORY= 2;	
	
	/********************drmc constant****************************/
	/**
	 * drmc成功状态值
	 */
	public static final int DRMC_STATUS_OK = 0;

	/**
	 * drmc删除物料失败
	 */
	public static final int DRMC_DEL_FAIL = 1;
	
	/**
	 * drmc备份物料失败
	 */
	public static final int DRMC_BACKUP_FAIL = -1;
	
	/**
	 * drmc拷贝物料失败
	 */
	public static final int DRMC_COPY_FAIL = -1;
	
	/**
	 * drmc提供的beidou文字物料类型值
	 */
	public static final int DRMC_MATTYPE_LITERAL = 2022;//30;
	
	/**
	 * drmc提供的beidou图片物料类型值
	 */
	public static final int DRMC_MATTYPE_IMAGE = 2023;//31;
	
	/**
	 * drmc提供的beidou图文物料类型值
	 */
	public static final int DRMC_MATTYPE_LITERAL_WITH_ICON = 2021;//2004;
	
	/**
	 * drmc提供的beidou图标物料类型值
	 */
	public static final int DRMC_MATTYPE_ICON = 2003;
	
	/**
	 * DRMC的临时物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_TMP=0;
	
	/**
	 * DRMC的正式物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_FORMAL=1;
	
	/**
	 * DRMC的历史物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_HISTORY=2;	
	
	/**
	 * 验证码物料图片宽度，固定为250
	 */
	public static final int DRMC_CHECKCODEMAT_WIDTH = 250;
	
	/**
	 * 验证码物料图片高度，固定为185
	 */
	public static final int DRMC_CHECKCODEMAT_HEIGHT = 185;

	/**
	 * 验证码相关异常错误
	 */
	public static interface DRMC_CHECKCODEMAT_ERRORINFO {
		public static final String ERROR_READINGCSV = "reading cvs line data failed";
		public static final String ERROR_UNITERROR = "the unit is null or its state is not validate";
		public static final String ERROR_DRMCACTIVE = "drmc tmp active response is empty";
		public static final String ERROR_DRMCTMP = "drmc tmp insert response is empty";
	}
	
	/**
	 * unit表的分表数
	 */
	public static final int CPROUNIT_TABLE_NUMBER = 8;

	/**
	 * 北斗接入外部api的adx类型—google
	 */
	public static final int ADX_TYPE_GOOGLE = 1;

	/**
	 *  google adx 创意审核状态 
	 * 
	 * @author kanghongwei
	 */
	public static enum GOOGLE_AUDIT_STATE {

		APPROVED(0, "APPROVED"), NOT_CHECKED(1, "NOT_CHECKED"), DISAPPROVED(2, "DISAPPROVED");

		private String stateName;
		private int stateValue;

		private GOOGLE_AUDIT_STATE(int stateValue, String stateName) {
			this.stateValue = stateValue;
			this.stateName = stateName;
		}

		public String getStateName() {
			return stateName;
		}

		public int getStateValue() {
			return stateValue;
		}

		public static List<String> getAllAuditStates() {
			List<String> stateList = new ArrayList<String>();
			stateList.add(APPROVED.getStateName());
			stateList.add(NOT_CHECKED.getStateName());
			stateList.add(DISAPPROVED.getStateName());
			return stateList;
		}

		public static Map<String, Integer> getAuditStatesMap() {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(APPROVED.getStateName(), APPROVED.getStateValue());
			map.put(NOT_CHECKED.getStateName(), NOT_CHECKED.getStateValue());
			map.put(DISAPPROVED.getStateName(), DISAPPROVED.getStateValue());
			return map;
		}

		public static boolean isAuditStateValie(String state) {
			if (StringUtils.isEmpty(state)) {
				return false;
			}
			List<String> stateList = getAllAuditStates();
			for (String s : stateList) {
				if (s.equalsIgnoreCase(state)) {
					return true;
				}
			}
			return false;
		}

		public static int getStateValue(String state) {
			if (StringUtils.isEmpty(state)) {
				return DISAPPROVED.getStateValue();
			}
			Map<String, Integer> stateMap = getAuditStatesMap();
			for (String s : stateMap.keySet()) {
				if (s.equalsIgnoreCase(state)) {
					return stateMap.get(state.toUpperCase());
				}
			}
			return DISAPPROVED.getStateValue();
		}

	}
	
	/**
	 *
	 * google adx的截图，针对特定尺寸，需要切割白边，如下为需要切割白边的尺寸
	 * 
	 * @author kanghongwei
	 * @dateTime 2013-10-30 下午1:22:28
	 */
	public static enum GOOGLE_SNAPSHOT_DEAL_BLANK_SIZES {

		SIZE_120X600(120, 600), SIZE_160X600(160, 600), SIZE_200X200(200, 200);

		private int width;
		private int height;

		private GOOGLE_SNAPSHOT_DEAL_BLANK_SIZES(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public static boolean isBlankSizeValid(int width, int height) {
			if (width == SIZE_120X600.getWidth() && height == SIZE_120X600.getHeight()) {
				return true;
			}
			if (width == SIZE_160X600.getWidth() && height == SIZE_160X600.getHeight()) {
				return true;
			}
			if (width == SIZE_200X200.getWidth() && height == SIZE_200X200.getHeight()) {
				return true;
			}
			return false;
		}

	}

	/**
	 * 投放google的swf物料截图状态—无需截图
	 */
	public static final int GOOGLE_SNAPSHOT_STATE_NO_NEED = 0;

	/**
	 * 投放google的swf物料截图状态—暂未截图
	 */
	public static final int GOOGLE_SNAPSHOT_STATE_NO_START = 1;

	/**
	 * 投放google的swf物料截图状态—截图成功
	 */
	public static final int GOOGLE_SNAPSHOT_STATE_SUCCESS = 2;

	/**
	 * 投放google的swf物料截图状态—截图失败
	 */
	public static final int GOOGLE_SNAPSHOT_STATE_FAILED = 3;

}
