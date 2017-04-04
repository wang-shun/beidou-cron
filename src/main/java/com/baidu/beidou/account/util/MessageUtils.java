package com.baidu.beidou.account.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
	public static boolean validateMobileFormat(String mobile){
		if(mobile==null){
			return false;
		}
		if((mobile.length()<8)||(mobile.length()>12)){
			return false;
		}
		char[] c = mobile.toCharArray();
		for(int i=0;i<c.length;i++){
			char ctmp = c[i];
			if((ctmp>'9')||(ctmp<'0')){
				return false;
			}
		}
		return true;
	}
	public static boolean validateMailFormat(String mail){
		//Pattern pattern = Pattern.compile("\\w+(\\.\\w+)*@\\w+(\\.\\w+)+");
		Pattern pattern = Pattern.compile("\\w+([-.])*\\w+([-.])*@\\w+([-.])*\\w+([-.])*\\.\\w+([-.])*\\w+([-.])*");
		if(mail.indexOf(",")>0){
			String[] mails = mail.split(",");
			for(int i=0;i<mails.length;i++){
				Matcher matcher = pattern.matcher(mails[i]);
				if(!matcher.matches()){
					return false;
				}
			}
			return true;
		}else{
			Matcher matcher = pattern.matcher(mail);
			if(matcher.matches()){
				return true;
			}else{
				return false;
			}
		}
	}
	public static void main(String[] args) throws Exception{
		String msg = "12345678";
		String msg1 = "1234567";
		String msg2 = "1234567dd";
		String msg3 = "123456789134";
		if(validateMobileFormat(msg)){
			System.out.println("msg0 matches");
		}
		if(validateMobileFormat(msg1)){
			System.out.println("msg1 matches");
		}
		if(validateMobileFormat(msg2)){
			System.out.println("msg2 matches");
		}
		if(validateMobileFormat(msg3)){
			System.out.println("msg3 matches");
		}
	}
}
