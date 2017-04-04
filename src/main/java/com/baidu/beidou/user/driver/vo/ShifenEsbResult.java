/**
 * 2009-12-21 下午01:45:09
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver.vo;

/**
 * @author zengyunfeng
 * 
 */
public class ShifenEsbResult<T> {

	/**
	 * $flag参数含义： 正常:const RESULT_FLAG_OK = 0; 
	  * 错误: const RESULT_FLAG_ERROR = 1; 
	 *  部分错误: const RESULT_FLAG_HAS_ERROR = 2; 
	 * 
	 */
	private int flag;
	private T data;
	private ShifenEsbErrorItem[] errors;

	/**
	 * @return the flag
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * @param flag
	 *            the flag to set
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * @return the errors
	 */
	public ShifenEsbErrorItem[] getErrors() {
		return errors;
	}

	/**
	 * @param errors
	 *            the errors to set
	 */
	public void setErrors(ShifenEsbErrorItem[] errors) {
		this.errors = errors;
	}

}
