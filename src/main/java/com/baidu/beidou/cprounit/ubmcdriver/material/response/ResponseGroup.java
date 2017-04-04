package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: ResponseGroup
 * Function: 推广组附加信息
 *
 * @author genglei01
 * @version 
 * @date May 14, 2014
 */
public class ResponseGroup extends ResponseBaseMaterial {
	
	private Integer groupId;
	private Long phoneId;
	private String phone;
	private Long msgPhoneId;
	private String msgPhone;
	private String msgContent;
    private String subUrlParam;
    private String subUrlTitle;
    private String subUrlLink;
    private String subUrlWirelessLink;

    /**
     * ResponseGroup: 构造寒素
     * 
     * @param groupId groupId
     * @param phoneId phoneId
     * @param phone phone
     * @param msgPhoneId msgPhoneId
     * @param msgPhone msgPhone
     * @param msgContent msgContent
     * @param subUrlParam subUrlParam
     * @param subUrlTitle subUrlTitle
     * @param subUrlLink subUrlLink
     * @param subUrlWirelessLink subUrlWirelessLink
     */
    public ResponseGroup(Integer groupId, Long phoneId, String phone, Long msgPhoneId, 
            String msgPhone, String msgContent, String subUrlParam, String subUrlTitle, 
            String subUrlLink, String subUrlWirelessLink) {

        this.groupId = groupId;
        this.phoneId = phoneId;
        this.phone = phone;
        this.msgPhoneId = msgPhoneId;
        this.msgPhone = msgPhone;
        this.msgContent = msgContent;
        this.subUrlParam = subUrlParam;
        this.subUrlTitle = subUrlTitle;
        this.subUrlLink = subUrlLink;
        this.subUrlWirelessLink = subUrlWirelessLink;
        
    }

    /**
     * Function: 将map数据封装为Object
     * 
     * @author genglei01
     * @param valueMap valueMap
     * @return ResponseGroup
     */
    public static ResponseGroup transformToObject(Map<String, String> valueMap) {
        if (valueMap == null || valueMap.isEmpty()) {
            return null;
        }

        try {
            Integer groupId = getInteger(valueMap.get(UbmcConstant.VALUE_ITEM_GROUPID));
            Long phoneId = getLong(valueMap.get(UbmcConstant.VALUE_ITEM_PHONEID));
            String phone = getString(valueMap.get(UbmcConstant.VALUE_ITEM_PHONE));
            Long msgPhoneId = getLong(valueMap.get(UbmcConstant.VALUE_ITEM_MSG_PHONEID));
            String msgPhone = getString(valueMap.get(UbmcConstant.VALUE_ITEM_MSG_PHONE));
            String msgContent = getString(valueMap.get(UbmcConstant.VALUE_ITEM_MSG_CONTENT));
            String subUrlParam = getString(valueMap.get(UbmcConstant.VALUE_ITEM_SUB_URL_PARAM));
            String subUrlTitle = getString(valueMap.get(UbmcConstant.VALUE_ITEM_SUB_URL_TITLE));
            String subUrlLink = getString(valueMap.get(UbmcConstant.VALUE_ITEM_SUB_URL_LINK));
            String subUrlWirelessLink = getString(valueMap.get(UbmcConstant.VALUE_ITEM_SUB_WIRELESS_URL_LINK));

            return new ResponseGroup(groupId, phoneId, phone, msgPhoneId, msgPhone, msgContent, 
                    subUrlParam, subUrlTitle, subUrlLink, subUrlWirelessLink);
        } catch (NumberFormatException e) {
            log.error("failed to get groupId or phoneId or msgPhoneId or subUrlType from the ubmc-value map");
            return null;
        }
    }

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Long getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(Long phoneId) {
		this.phoneId = phoneId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getMsgPhoneId() {
		return msgPhoneId;
	}

	public void setMsgPhoneId(Long msgPhoneId) {
		this.msgPhoneId = msgPhoneId;
	}

	public String getMsgPhone() {
		return msgPhone;
	}

	public void setMsgPhone(String msgPhone) {
		this.msgPhone = msgPhone;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

    public String getSubUrlParam() {
        return subUrlParam;
    }

    public void setSubUrlParam(String subUrlParam) {
        this.subUrlParam = subUrlParam;
    }

    public String getSubUrlTitle() {
        return subUrlTitle;
    }

    public void setSubUrlTitle(String subUrlTitle) {
        this.subUrlTitle = subUrlTitle;
    }

    public String getSubUrlLink() {
        return subUrlLink;
    }

    public void setSubUrlLink(String subUrlLink) {
        this.subUrlLink = subUrlLink;
    }

    public String getSubUrlWirelessLink() {
        return subUrlWirelessLink;
    }

    public void setSubUrlWirelessLink(String subUrlWirelessLink) {
        this.subUrlWirelessLink = subUrlWirelessLink;
    }

}
