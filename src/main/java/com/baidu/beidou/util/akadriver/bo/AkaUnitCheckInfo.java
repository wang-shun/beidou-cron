package com.baidu.beidou.util.akadriver.bo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AkaUnitCheckInfo extends AkaCheckInfo{
	
	private String ideaTitle="";
	
	private String ideaDesc1="";
	
	private String ideaDesc2="";
	
	public String getIdeaDesc1() {
		return ideaDesc1;
	}

	public void setIdeaDesc1(String ideaDesc1) {
		this.ideaDesc1 = ideaDesc1;
	}

	public String getIdeaDesc2() {
		return ideaDesc2;
	}

	public void setIdeaDesc2(String ideaDesc2) {
		this.ideaDesc2 = ideaDesc2;
	}

	public String getIdeaTitle() {
		return ideaTitle;
	}

	public void setIdeaTitle(String ideaTitle) {
		this.ideaTitle = ideaTitle;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
