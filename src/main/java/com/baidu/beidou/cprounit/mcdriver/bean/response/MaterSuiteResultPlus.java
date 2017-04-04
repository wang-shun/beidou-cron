package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

public class MaterSuiteResultPlus {

	// 当前页的创意列表
	private List<MaterSuiteResult> list;
	
	// 创意列表总页数
	private int sumpage;
	
	// 创意列表总数量
	private int count;

	public List<MaterSuiteResult> getList() {
		return list;
	}

	public void setList(List<MaterSuiteResult> list) {
		this.list = list;
	}

	public int getSumpage() {
		return sumpage;
	}

	public void setSumpage(int sumpage) {
		this.sumpage = sumpage;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
}
