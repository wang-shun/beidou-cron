package com.baidu.beidou.bes.user.bean;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.baidu.beidou.util.JsonUtils;

/**
 * 推送用户后api返回信息bean 反射解析json封装成bean
 * 
 * @author caichao
 */
public class PushUserResultBean extends BaseApiResultBean {
	private Map<Integer ,List<String>> ret_msg;


	
	public static void main(String[] args) throws JSONException {

		//PushUserResultBean bean = (PushUserResultBean) JsonUtils.json2Object("{\"ret_code\": 1,\"ret_msg\": {\"517\": [\"广州创思信息技术有限公司11\",\"北京中公未来教育咨询有限公司11\"]},\"error_code\": 102}", PushUserResultBean.class);
		//BaseApiResultBean bean = (BaseApiResultBean)JsonUtils.json2Object("{\"ret_code\": 1,\"ret_msg\": {\"505\": [\"广州创思信息技术有限公司1112\"],\"511\": [\"北京中公未来教育咨询有限公司1132121\"]},\"error_code\": 102}",BaseApiResultBean.class);
		PushUserResultBean bean = (PushUserResultBean) JsonUtils.json2Object("{\"ret_code\":1,\"ret_msg\":{\"303\":[\"token\"]},\"error_code\":102}", PushUserResultBean.class);
		System.out.println(bean.getRet_code());
		System.out.println(bean.getError_code());
		System.out.println(bean.getRet_msg());
		for (Map.Entry<Integer, List<String>> entry : bean.getRet_msg().entrySet()) {
			System.out.println(entry.getKey());
			for (String value : entry.getValue()) {
				System.out.println(value);
			}
		}
		
		//Map<String,String> map = toMap("{\"ret_code\": 1,\"ret_msg\": \"{1234}\",\"error_code\": 102}");
//		for (Map.Entry<String, String> entry : map.entrySet()) {
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue());
//		}
	}


	public Map<Integer, List<String>> getRet_msg() {
		return ret_msg;
	}


	public void setRet_msg(Map<Integer, List<String>> ret_msg) {
		this.ret_msg = ret_msg;
	}



	
}
