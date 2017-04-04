package com.baidu.beidou.util.akadriver.service;

import java.util.List;

import com.baidu.beidou.util.akadriver.exception.InputParamIsNullException;
import com.baidu.beidou.util.akadriver.exception.ObjectClassNotMatchException;

public interface AkaResultParser {
	
	/**
	 * 
	 * @see 将返回的错误信息进行封装，正确不改变，错误封装详细的错误信息 
	 * @param input
	 * @return
	 * @throws InputParamIsNullException
	 * @throws ObjectClassNotMatchException
	 * @author puyuda
	 * @date 2008-7-16
	 * @version 1.0.0
	 */
	public List parseAkaResult(List input) throws InputParamIsNullException,ObjectClassNotMatchException;

}
