package com.baidu.beidou.bes.user.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.json.JSONException;

import com.baidu.beidou.util.JsonUtils;
/**
 * 审核结果bean 反射解析json封装成bean
 * 
 * @author caichao
 */
public class AuditResultBean extends BaseApiResultBean{
	private Map<String, ResultInfo> ret_msg;



	public Map<String, ResultInfo> getRet_msg() {
		return ret_msg;
	}



	public void setRet_msg(Map<String, ResultInfo> ret_msg) {
		this.ret_msg = ret_msg;
	}
	
	
	public static void main(String[] args) throws UnsupportedEncodingException, JSONException {
		AuditResultBean bean = (AuditResultBean)JsonUtils.json2Object("{\"ret_code\": 1,\"ret_msg\":{\"client_name1\": { \"verify_status\":\"待审核\",\"audit_info\":\"客户新建，等待审核\", \"is_black\":\"Y\"}, \"client_name2\": {\"verify_status\":\"不通过\", \"audit_info\":\"资质文件不符合要求\", \"is_black\":\"N\"}},\"error_code\": 102}", AuditResultBean.class);
		//AuditResultBean bean = (AuditResultBean)JsonUtils.json2Object("{\"ret_code\":0,\"ret_msg\":{\"\u4e2d\u6587569948\":{\"verify_status\":\"\u4e0d\u901a\u8fc7\",\"audit_info\":\"\u4e0d\u901a\u8fc73\",\"is_black\":\"N\",\"type\":\"ADX\",\"vocation\":\"\u7f51\u7edc\u6e38\u620f\u7c7b\"},\"\u4e2d\u6587569978\":{\"verify_status\":\"\u901a\u8fc7\",\"audit_info\":\"\u53c8\u4e00\u4e2a\u901a\u8fc7\",\"is_black\":\"N\",\"type\":\"ADX\",\"vocation\":\"\u4ea4\u901a\u7c7b\"}},\"error_code\":0}", AuditResultBean.class);
		//Map<String,String> test = JsonHelper.toMap("{\"ret_code\":0,\"ret_msg\":[],\"error_code\":0}");
	//	System.out.println(test.get("ret_msg"));
//		System.out.println(bean.getRet_code());
//		System.out.println(bean.getError_code());
//		for (Map.Entry<String, ResultInfo> entry : bean.getRet_msg().entrySet()){
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue().getAudit_info());
//			System.out.println(entry.getValue().getVerify_status());
//			System.out.println(entry.getValue().getIs_black());
//		}
////		ResultInfo info1 = new ResultInfo();
//		info1.setAudit_info("aa");
//		info1.setIs_black("Y");
//		info1.setVerify_status("jj");
//		
//		ResultInfo info2 = new ResultInfo();
//		info2.setAudit_info("bb");
//		info2.setIs_black("N");
//		info2.setVerify_status("tt");
//		
//		Map<String ,ResultInfo> test = new HashMap<String, ResultInfo>();
//		test.put("client_name1", info1);
//		test.put("client_name2", info2);
//		
//		AuditResultBean bean = new AuditResultBean();
//		bean.setRet_code(1);
//		bean.setError_code(102);
//		bean.setRet_msg(test);
//		
//		System.out.println(JsonUtils.toJson(bean));
		
		//System.out.println(URLEncoder.encode("中文","UTF-8"));
		System.out.println(URLDecoder.decode("{\"ret_code\":0,\"ret_msg\":{\"\u5168\u90e8\u6210\u529f\":{\"verify_status\":\"\u4e0d\u901a\u8fc7\",\"audit_info\":\"\u4e0d\u901a\u8fc73\",\"is_black\":\"N\",\"type\":\"ADX\",\"vocation\":\"\u7f51\u7edc\u6e38\u620f\u7c7b\"},\"\u4e2d\u6587569978\":{\"verify_status\":\"\u901a\u8fc7\",\"audit_info\":\"\u53c8\u4e00\u4e2a\u901a\u8fc7\",\"is_black\":\"N\",\"type\":\"ADX\",\"vocation\":\"\u4ea4\u901a\u7c7b\"}},\"error_code\":0}", "UTF-8"));
	}
}
