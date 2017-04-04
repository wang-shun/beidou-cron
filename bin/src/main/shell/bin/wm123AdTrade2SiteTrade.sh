#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/wm123AdTrade2SiteTrade.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

program=wm123AdTrade2SiteTrade.sh
reader_list=lvzichan

mkdir -p ${WM123_TMPDATA_PATH}
mkdir -p ${WM123_DATA_PATH}


log "TRACE" "START-------------`date +%Y%m%d_%H:%M:%S`"
cd ${WM123_TMPDATA_PATH}

#下载usertrade.txt文件，user~tradeid的对应关系存在SF_Other.usertrade表，上游已经导出为文件
rm ${USER_TRADEID_FILENAME}*
wget -q ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME}
alert $? "Fail to wget usertrade.txt"
wget -q ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME}.md5
alert $? "Fail to wget usertrade.txt.md5"
md5sum -c ${USER_TRADEID_FILENAME}.md5
alert $? "Fail to md5sum -c usertrade.txt.md5"

#从beidoucode.adtrade读取所有广告行业
QUERY='SELECT tradeid,parentid FROM beidoucode.adtrade'
runsql_cap_read "${QUERY}" ${ADTRADE_FILENAME}
alert $? "Fail to read db:beidoucode.adtrade"

#从上两个文件，得到userid~first_adtradeid的对应文件
awk -v'OFS=\t' 'NR==FNR{if($2==0){map[$1]=$1} else{map[$1]=$2}} NR>FNR{if(map[$2]"x"!="x"){print $1,map[$2]}}' ${ADTRADE_FILENAME} ${USER_TRADEID_FILENAME} > ${USER_FIRST_ADTRADEID_FILENAME}

#从beidou.cprogroup读取所有有效、非全网投放、设置了投放行业的推广组
QUERY='SELECT c.groupid,c.userid,cinfo.sitetradelist FROM beidou.cprogroup c JOIN beidou.cprogroupinfo cinfo on c.groupid=cinfo.groupid WHERE c.groupstate=0 AND cinfo.isallsite=0 AND cinfo.sitetradelist!="" AND [c.userid];'
runsql_sharding_read "${QUERY}" ${GROUP_SITETRADELIST_FILENAME}
alert $? "Fail to read db:beidou.cprogroup"

#得到最终的每个推广组的groupid userid adtradeid sitetradelist文件
awk -v'OFS=\t' 'NR==FNR{map[$1]=$2} NR>FNR{if(map[$2]"x"!="x"){print $1,$2,map[$2],$3}}' ${USER_FIRST_ADTRADEID_FILENAME} ${GROUP_SITETRADELIST_FILENAME} > ${SRC_GROUP_FILE}

log "TRACE" "MIDDLE,get group_adtrade_sitetradelist file done---------------:`date +%Y%m%d_%H:%M:%S`"

java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WM123AdTrade2SiteTradeGenerator -i${WM123_TMPDATA_PATH}/${SRC_GROUP_FILE} -i${WM123_DATA_PATH}/${ADTRADE_SITETRADE_FILE}  1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "Fail to execute WM123AdTrade2SiteTradeGenerator!"

log "TRACE" "END---------------:`date +%Y%m%d_%H:%M:%S`"
exit 0