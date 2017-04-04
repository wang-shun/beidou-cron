package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: RequestGroup
 * Function: 推广组附加信息
 *
 * @author genglei01
 * @version 
 * @date May 14, 2014
 */
public class RequestGroup extends RequestBaseMaterial {

	private Integer groupId;
	private Long phoneId;
	private String phone;
	private Long msgPhoneId;
	private String msgPhone;
	private String msgContent;
    private String subUrlParam;
    private String subUrlTitle;
    private String subUrlLink;

    /**
     * RequestGroup: 构造函数
     * 
     * @param mcId mcId
     * @param versionId versionId
     * @param groupId groupId
     * @param phoneId phoneId
     * @param phone phone
     * @param msgPhoneId msgPhoneId
     * @param msgPhone msgPhone
     * @param msgContent msgContent
     * @param subUrlParam subUrlParam
     * @param subUrlTitle subUrlTitle
     * @param subUrlLink subUrlLink
     */
    public RequestGroup(Long mcId, Integer versionId, Integer groupId, Long phoneId, 
            String phone, Long msgPhoneId, String msgPhone, String msgContent, 
            String subUrlParam, String subUrlTitle, String subUrlLink) {

        super(mcId, versionId);

        this.groupId = groupId;
        this.phoneId = phoneId;
        this.phone = phone;
        this.msgPhoneId = msgPhoneId;
        this.msgPhone = msgPhone;
        this.msgContent = msgContent;
        this.subUrlParam = subUrlParam;
        this.subUrlTitle = subUrlTitle;
        this.subUrlLink = subUrlLink;
        
    }

	public String tranformToValueString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UbmcConstant.VALUE_ITEM_GROUPID)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getGroupId()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_PHONEID)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getPhoneId()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_PHONE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getPhone()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_MSG_PHONEID)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getMsgPhoneId()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_MSG_PHONE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getMsgPhone()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_MSG_CONTENT)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getMsgContent()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
        sb.append(UbmcConstant.VALUE_ITEM_SUB_URL_PARAM)
                .append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
                .append(getSubUrlParam()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
        sb.append(UbmcConstant.VALUE_ITEM_SUB_URL_TITLE)
                .append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
                .append(getSubUrlTitle()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
        sb.append(UbmcConstant.VALUE_ITEM_SUB_URL_LINK)
                .append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
                .append(getSubUrlLink());

        return sb.toString();
	}

	public Integer getGroupId() {
		if (groupId == null) {
			return 0;
		}
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Long getPhoneId() {
		if (phoneId == null) {
			return 0L;
		}
		return phoneId;
	}

	public void setPhoneId(Long phoneId) {
		this.phoneId = phoneId;
	}

	public String getPhone() {
		if (phone == null) {
			return "";
		}
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getMsgPhoneId() {
		if (msgPhoneId == null) {
			return 0L;
		}
		return msgPhoneId;
	}

	public void setMsgPhoneId(Long msgPhoneId) {
		this.msgPhoneId = msgPhoneId;
	}

	public String getMsgPhone() {
		if (msgPhone == null) {
			return "";
		}
		return msgPhone;
	}

	public void setMsgPhone(String msgPhone) {
		this.msgPhone = msgPhone;
	}

	public String getMsgContent() {
		if (msgContent == null) {
			return "";
		}
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

    public String getSubUrlParam() {
        if (subUrlParam == null) {
            return "";
        }
        return subUrlParam;
    }

    public void setSubUrlParam(String subUrlParam) {
        this.subUrlParam = subUrlParam;
    }

    public String getSubUrlTitle() {
        if (subUrlTitle == null) {
            return "";
        }
        return subUrlTitle;
    }

    public void setSubUrlTitle(String subUrlTitle) {
        this.subUrlTitle = subUrlTitle;
    }

    public String getSubUrlLink() {
        if (subUrlLink == null) {
            return "";
        }
        return subUrlLink;
    }

    public void setSubUrlLink(String subUrlLink) {
        this.subUrlLink = subUrlLink;
    }
    
}
