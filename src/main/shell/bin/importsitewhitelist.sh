#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importsitewhitelist.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/importsitewhitelist.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${SITE_DATA_PATH}失败"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

if [ -f "${WHITE_SITE_FILE}" ] 
then
	
	runsql_cap "delete from beidoucap.whitelist where type = 1" 
	   
    #逐一添加新的网站ID
    for url in `awk -F '\t' '{print $3}' ${WHITE_SITE_FILE}` 
    do
    	if [ -f ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql" ]
    	then
    		rm -f ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql"
    	fi
    	if [ -f ${SITE_DATA_PATH}/"WHITELIST.sql" ]
    	then
    		rm -f ${SITE_DATA_PATH}/"WHITELIST.sql"
    	fi
    	
    	runsql_xdb_read "select 1, siteid from beidouext.unionsite where siteurl = '${url}'"  ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql"
    	if [ -s ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql" ]
    	then
    		LINE_NUM=`cat ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql" | wc -l`
    		echo "insert into beidoucap.whitelist(type,id) values " >>  ${SITE_DATA_PATH}/"WHITELIST.sql"
    		awk -vnum=${LINE_NUM} '{if(NR != num){print "("$1","$2"),"} else {print "("$1","$2");"}}' ${SITE_DATA_PATH}/"TMP_UNIONSITE.sql" >> ${SITE_DATA_PATH}/"WHITELIST.sql"
    		runsql_cap "`cat ${SITE_DATA_PATH}/"WHITELIST.sql"`"
    	fi
    	
    done
else
    echo "no file to import" >> ${LOG_FILE}
fi

msg="更新网站whitelist失败"
alert $? "${msg}"
