
package com.baidu.beidou.unionsite.bo;

import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.unionsite.vo.SiteEntity;


/**
 * WMSiteIndexBo
 * Function: wm需要计算的站点的Index统计信息(计算后的信息)
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  May 22, 2010
 * @since    TODO
 * @version  $Id: WMSiteIndexBo.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 */
public class WMSiteIndexBo extends SiteEntity{

    private static final long serialVersionUID = 8442284857795156007L;

    /** 网站的地域属性，以逗号分隔的二级地域（含四个直辖市和二个特别行政区）regid */
    private List<Integer> cityList = new ArrayList<Integer>(500);
    
    /** 网站的性别属性，以逗号分隔的属性枚举值 */
    private List<Integer> genderList = new ArrayList<Integer>();
    
    /** 网站的年龄属性，以逗号分隔的属性枚举值 */
    private List<Integer> ageList = new ArrayList<Integer>();

    /** 网站的学历属性，以逗号分隔的属性枚举值 */
    private List<Integer> educationList = new ArrayList<Integer>();

    /** 
     * 网站的一级地域（含四个直辖市和二个特别行政区）流量统计值，按流量降序排列，IntegerEntry中的key为地域ID，value为流量值。
     * 如： 5：6234，2：1111，1：999...
     */
    private List<IntegerEntry> provinceStatList = new ArrayList<IntegerEntry>(40);
    

    /** 
     * 网站的二级地域（含四个直辖市和二个特别行政区）流量比值，按流量降序排列，IntegerEntry中的key为地域ID，value为流量比。
     * 如： 5：6234，2：1111，1：999...，共10个
     */
    private List<IntegerEntry> cityValue = new ArrayList<IntegerEntry>(500);
    
    /** 网站的性别属性，IntegerEntry的key为男或女（参见indexmapping表），value为流量比 */
    private List<IntegerEntry> genderValue = new ArrayList<IntegerEntry>();
    
    /** 网站的年龄属性，按ID升序排序，IntegerEntry的key为ID（参见indexmapping表），value为流量比 */
    private List<IntegerEntry> ageValue = new ArrayList<IntegerEntry>();

    /** 网站的学历属性，按ID升序排序，IntegerEntry的key为ID（参见indexmapping表），value为流量比 */
    private List<IntegerEntry> educationValue = new ArrayList<IntegerEntry>();

    public WMSiteIndexBo(){}

    public List<Integer> getCityList() {
        return cityList;
    }

    public void setCityList(List<Integer> cityList) {
        this.cityList = cityList;
    }

    public List<Integer> getGenderList() {
        return genderList;
    }

    public void setGenderList(List<Integer> genderList) {
        this.genderList = genderList;
    }

    public List<Integer> getAgeList() {
        return ageList;
    }

    public void setAgeList(List<Integer> ageList) {
        this.ageList = ageList;
    }

    public List<Integer> getEducationList() {
        return educationList;
    }

    public void setEducationList(List<Integer> educationList) {
        this.educationList = educationList;
    }

    public List<IntegerEntry> getProvinceStatList() {
        return provinceStatList;
    }

    public void setProvinceStatList(List<IntegerEntry> provinceStatList) {
        this.provinceStatList = provinceStatList;
    }

    public List<IntegerEntry> getCityValue() {
        return cityValue;
    }

    public void setCityValue(List<IntegerEntry> cityValue) {
        this.cityValue = cityValue;
    }

    public List<IntegerEntry> getGenderValue() {
        return genderValue;
    }

    public void setGenderValue(List<IntegerEntry> genderValue) {
        this.genderValue = genderValue;
    }

    public List<IntegerEntry> getAgeValue() {
        return ageValue;
    }

    public void setAgeValue(List<IntegerEntry> ageValue) {
        this.ageValue = ageValue;
    }

    public List<IntegerEntry> getEducationValue() {
        return educationValue;
    }

    public void setEducationValue(List<IntegerEntry> educationValue) {
        this.educationValue = educationValue;
    }

}

