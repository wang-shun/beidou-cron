package com.baidu.beidou.tool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;



public class CommonTest {

	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();
		String json="{\"name\" : { \"first\" : \"Joe\", \"last\" : \"Sixpack\" },\"gender\" : \"MALE\",\"verified\" : false,\"userImage\" : \"Rm9vYmFyIQ==\"}";
		try {
			Map o = mapper.readValue(json, HashMap.class);
			System.out.println(o);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String site = "http://asfa.com.cn/aaa?ds";
		Pattern p = Pattern.compile("^http://([^/]+).*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(site);
		matcher.find();
		System.out.println(matcher.group(1));
		System.out.println("**");
	}

}
