#!/bin/bash
#@file: generateSiteurl4Sinan.sh
#@author: zhangxu04
#@date: 2011-05-28
#@version: 1.0.0.0
#@brief: download last 3 month's wm123 visit access log, anlyze logs to generate siteurl list file sort by top landing detail page count and site srchs

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/wm123siteurl4sn.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=generateWm123Siteurl.sh
reader_list=zhangxu04

WM123_GEN_SITEURL_BASE=${DATA_PATH}/wm123siteurl4sn/
WM123_GEN_SITEURL_VISITLOG=${WM123_GEN_SITEURL_BASE}visitlog
WM123_GEN_SITEURL_DATA=${WM123_GEN_SITEURL_BASE}data
WM123_GEN_SITEURL_TEMP=${WM123_GEN_SITEURL_BASE}temp
SITEID_FILE=$WM123_GEN_SITEURL_DATA/"siteid.txt"
WM123_SITE=$WM123_GEN_SITEURL_DATA/"wm123site.txt"
SINAN_SITE_ORI=$WM123_GEN_SITEURL_DATA/"sinansite_ori.txt"
SINAN_SITE=$WM123_GEN_SITEURL_DATA/"sinansite.txt"
LOG_FILE=${LOG_PATH}/generateSiteurl4Sinan.log

# 昨天的日期，格式为YYmmdd
YESTERDAY=`date -d "1 day ago" +%Y%m%d`

#--------------- function  --------------

# env setup to clear data and temp file
function env_setup()
{
	if [ ! -e "$WM123_GEN_SITEURL_BASE" ];then
		mkdir -p $WM123_GEN_SITEURL_BASE
	fi
	
	cd $WM123_GEN_SITEURL_VISITLOG && rm -rf $WM123_GEN_SITEURL_VISITLOG
	cd $WM123_GEN_SITEURL_TEMP && rm -rf $WM123_GEN_SITEURL_TEMP
	cd $WM123_GEN_SITEURL_DATA && rm -f $WM123_SITE && rm -f $SITEID_FILE && rm -f $SINAN_SITE_ORI
	
	mkdir -p $WM123_GEN_SITEURL_VISITLOG
	mkdir -p $WM123_GEN_SITEURL_TEMP
	mkdir -p $WM123_GEN_SITEURL_DATA
	
	if [ ! -e "$LOG_PATH" ];then
		mkdir -p $LOG_PATH
	fi
	
	return 0
}

# Download WM123 visitaccess logs
# $1: absolute output path - make sure output path exist, e.g. /home/work/data/wm123log
# $2: date - make sure date is valid, e.g. 20110312
function dowloadWm123VisitAccessLog()
{
    cd $1
    param_date=`date +"%Y%m%d" --date=$2`
    format_date="."${param_date}
   
    wm123_visit_suffix=$format_date
    ftp_path=${wm123_log_path}${wm123_visit_prefix}${wm123_visit_suffix}
    log_name=`echo $ftp_path | awk -F"/" '{print $NF}'`
    
    msg="download ${DTS_VISITACCESS_ITEM}${format_date} from dts failed!"
    noahdt download ${DTS_VISITACCESS_ITEM}${format_date}  ./${log_name} --tag=${param_date}
    alert $? "${msg}"
	
}

# Download WM123 visitaccess logs in specific month
# $1: absolute output path - make sure output path exist, e.g. /home/work/data/wm123log
# $2: year
# $3: month
# $4: total_day_num_in_a_month
function downloadMonthlyLogs()
{
    start_day=1
    outputdir=$1
    year=$2
    month=$3
    total_day_num=$4
    while [ $start_day -le $total_day_num ] 
    do
        if [ $start_day -lt 10 ];then
			echo_msg "Download ${year}${month}0${start_day} log"
            dowloadWm123VisitAccessLog ${outputdir} ${year}${month}0${start_day}
        else
			echo_msg "Download ${year}${month}${start_day} log"
            dowloadWm123VisitAccessLog ${outputdir} ${year}${month}${start_day}
        fi
        start_day=`expr $start_day + 1`
    done
}



# Download all wm123 logs
# $1 - absolute output path - make sure output path exist, e.g. /home/work/data/wm123log
function dowloadAllWm123VisitAccessLog()
{
    # 能够符合计算的log月份，之前的log系统未升级
    MOST_BEFORE_YEAR_MONTH="201101"
    outputdir=$1
    n=1
    LAST_YEAR_MONTH=`date -d '1 months ago' +%Y%m`
    while [ $LAST_YEAR_MONTH -gt $MOST_BEFORE_YEAR_MONTH ] 
    do
        LAST_YEAR_MONTH=`date -d ''$n' months ago' +%Y%m`
        LAST_YEAR=`date -d ''$n' months ago' +%Y`
        LAST_MONTH=`date -d ''$n' months ago' +%m`
        LAST_DAY_NUM=`cal $LAST_MONTH $LAST_YEAR | xargs | awk '{print $NF}'`
		echo_msg "Start to download ${LAST_YEAR}${LAST_MONTH} all logs"
        downloadMonthlyLogs ${outputdir} ${LAST_YEAR} ${LAST_MONTH} ${LAST_DAY_NUM}
		echo_msg "End download ${LAST_YEAR}${LAST_MONTH} all logs"
        n=`expr $n + 1`
    done
    NOW_YEAR_MONTH=`date +%Y%m`
    NOW_YEAR=`date +%Y`
    NOW_MONTH=`date +%m`
    NOW_DAY_NUM=`date -d "1 day ago" +%d`
    echo_msg "Start to download this month's all logs"
    downloadMonthlyLogs ${outputdir} ${NOW_YEAR} ${NOW_MONTH} ${NOW_DAY_NUM}
	echo_msg "End download this month's all logs"
}

# Download last 3 month's wm123 logs
# $1 - absolute output path - make sure output path exist, e.g. /home/work/data/wm123log
function dowloadLast3MonthWm123VisitAccessLog()
{
    outputdir=$1
    n=1
	LAST_3_YEAR_MONTH=`date -d '3 months ago' +%Y%m`
    LAST_YEAR_MONTH=`date -d '1 months ago' +%Y%m`
    while [ $LAST_YEAR_MONTH -gt $LAST_3_YEAR_MONTH ] 
    do
        LAST_YEAR_MONTH=`date -d ''$n' months ago' +%Y%m`
        LAST_YEAR=`date -d ''$n' months ago' +%Y`
        LAST_MONTH=`date -d ''$n' months ago' +%m`
        LAST_DAY_NUM=`cal $LAST_MONTH $LAST_YEAR | xargs | awk '{print $NF}'`
		echo_msg "Start to download ${LAST_YEAR}${LAST_MONTH} all logs"
        downloadMonthlyLogs ${outputdir} ${LAST_YEAR} ${LAST_MONTH} ${LAST_DAY_NUM}
		echo_msg "End download ${LAST_YEAR}${LAST_MONTH} all logs"
        n=`expr $n + 1`
    done
}

function genDetailPageSiteid()
{
    cat $WM123_GEN_SITEURL_VISITLOG/* | grep "url=\[http:\/\/wm123.baidu.com\/site\/detail.action" | awk 'BEGIN {
        OFS="\t";
        preLen=length("url=[http://wm123.baidu.com/site/detail.action?siteId=");
    }
	
	{ 
	    if($7 ~ /url=\[http:\/\/wm123.baidu.com\/site\/detail.action/)
		{
		    siteIdStr=substr($7,preLen + 1);
            sIdx=index(siteIdStr,"]");
            siteid=substr(siteIdStr,1,sIdx-1);
            sIdx=index(siteid,"&");
            if (sIdx > 0 ) {
                siteid=substr(siteid,1,sIdx-1); 
            }
			SITE_IDS[siteid]++;
		}
		else if($8 ~ /url=\[http:\/\/wm123.baidu.com\/site\/detail.action/)
		{
		    siteIdStr=substr($8,preLen + 1);
            sIdx=index(siteIdStr,"]");
            siteid=substr(siteIdStr,1,sIdx-1);
            sIdx=index(siteid,"&");
            if (sIdx > 0 ) {
                siteid=substr(siteid,1,sIdx-1); 
            }
			SITE_IDS[siteid]++;
		}
		else
		{
		    echo_error_msg $0
		}
	}
	
	END{ for(i in SITE_IDS) print i "\t" SITE_IDS[i] ; }'  | sort +1nr > $SITEID_FILE
} 

function outputFinalData()
{
	awk 'BEGIN {
        OFS="\t";
    }
	
	{ 
	    if(NR==FNR)
		{
		    a[$1]=$0;
		} 
		else if(NR>FNR)
		{
			print a[$1]"\t"$2
		}
	} ' $WM123_SITE $SITEID_FILE > $WM123_GEN_SITEURL_TEMP/siteid_unsort.tmp
	
	cat $WM123_GEN_SITEURL_TEMP/siteid_unsort.tmp | awk -F"\t" '{if(NF==8) print $0}' | sort  +7nr +3nr +2nr +4nr +5nr > $WM123_GEN_SITEURL_TEMP/siteid_sort.tmp

	cat $WM123_GEN_SITEURL_TEMP/siteid_sort.tmp | awk -F"\t" '{print $2}' > $SINAN_SITE_ORI
}


# print message
# $1 - message
echo_msg(){
   echo -e "$1" | tee -a $LOG_FILE
}

# usage help
function usage()
{
        echo "生成WM123近三个月来详情页访问量倒序排的siteurl列表文件（没有访问的按检索量排序）"
        echo "Version : 1.0.0  (build 20110528) "
        echo "write by zhangxu04@baidu.com"
        echo ""
        echo "Usage:"
        echo "      generateSiteurl4Sinan.sh"
        echo "Function:"
		echo "      下载前三个月的所有日志分析并统计生成siteurl列表"
}

#--------------- main --------------

msg="清理文件失败"
env_setup
alert $? "${msg}"
CUR_DATE=`date +%Y%m%d`
echo_msg "********** Execute @$CUR_DATE************"
echo_msg "Logs will be saved under $WM123_GEN_SITEURL_VISITLOG"
dowloadLast3MonthWm123VisitAccessLog $WM123_GEN_SITEURL_VISITLOG
echo_msg "Finishe download logs"

# 根据详情页访问的次数生成siteid
echo_msg "Generate siteurl list file sort by top landing detail page"
genDetailPageSiteid
echo_msg "Finish generate siteurl list file sort by top landing detail page"

#导出WM123站点信息
echo_msg "Generate siteurl list file sort by srchs"
EXEC_SQL="SELECT a.siteid, a.siteurl, b.certification, c.srchs, c.ips, c.cookies, b.sitelink FROM beidouext.unionsite a JOIN beidouext.unionsiteinfos b on a.siteid=b.siteid JOIN beidouext.unionsitestat c ON a.siteid=c.siteid JOIN beidouext.unionsitebdstat d ON a.siteid=d.siteid  WHERE a.valid !=0  UNION  SELECT a.siteid, a.siteurl,  b.certification, c.srchs, c.ips, c.cookies, b.sitelink FROM beidouext.unionsite a JOIN beidouext.unionsiteinfos b on a.siteid=b.siteid  JOIN beidouext.unionsitestat c ON a.siteid=c.siteid JOIN beidouext.unionsitebdstat d ON a.siteid=d.siteid  WHERE a.valid !=0 and a.firsttradeid=6;"
runsql_xdb_read "${EXEC_SQL}" "${WM123_SITE}"
echo_msg "Finish generate siteurl list file sort by srchs"

# merge有详情页访问的，且在WM123站点集合中的站点并且，按照详情页访问次数>srchs>大联盟认证>ip>cookies的依次排序，输出siteurl list
echo_msg "Merge siteurl list from db and log"
outputFinalData
echo_msg "Finished merge siteurl list from db and log"

echo_msg "Add www. prefix for main domain url"
msg="根据统计后的siteurl列表，对于一级域名加入www.前缀失败"
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WM123SiteurlGenerator -i ${SINAN_SITE_ORI} -i ${SINAN_SITE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"

#regist DTS
msg="regist DTS for ${GENERATEWM123SITEURL_SINANSITE_TXT} failed."
noahdt add ${GENERATEWM123SITEURL_SINANSITE_TXT} bscp://${SINAN_SITE}
alert $? "${msg}"

echo_msg "Finished add www. prefix for main domain url"
echo_msg "Script finished."

exit 0
