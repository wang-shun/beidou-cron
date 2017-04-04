#!/bin/bash
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/beidou_cache.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

YEST_Ymd=`date -d yesterday +%Y%m%d`
YEST_s=`date -d "$YEST_Ymd" +%s`
DAY_IN_SEC=86400

if [ ! -d ${CLICK_LOG_LOCAL_PATH} ]; then
	mkdir -p ${CLICK_LOG_LOCAL_PATH}
fi

if [ ! -d ${CLICK_LOG_TMP_PATH} ]; then
	mkdir -p ${CLICK_LOG_TMP_PATH}
fi

if [ ! -d ${LOG_PATH} ]; then
	mkdir -p ${LOG_PATH}
fi

USAGE="$0 [all|yest]"
LOG_FILE=${LOG_PATH}/beidou_cache.log.$YEST_Ymd

function download() {
	#Try to clean some older click log file
	for i in `ls ${CLICK_LOG_LOCAL_PATH}`
	do
		if [ `expr match $i 'detail[0-9]\{8\}'` -eq 0 ] ; then
                echo "The file $i is not a valid click log file, skip it..." >> ${LOG_FILE}
                continue
        fi
        
		local suffix_Ymd=${i##detail}
		local suffix_s=`date -d "$suffix_Ymd" +%s`
		if [ $(($YEST_s-$suffix_s)) -ge $(($DAY_IN_SEC*$CLICK_LOG_EXPIRE_DAYS)) ]; then
			echo "The click log file $i should be deleted..." >> ${LOG_FILE}
			rm -f ${CLICK_LOG_LOCAL_PATH}/${i}
		fi
	done

	if [ -f ${CLICK_LOG_LOCAL_PATH}/detail${YEST_Ymd} ]; then
	        echo "The click log file "${CLICK_LOG_LOCAL_PATH}"/detail"${YEST_Ymd}" is already existed, skip to download it..." >> ${LOG_FILE}
	else
		
	        #改wget为cp,liangshimu,2011-07-04
	        if [ -f ${CLICK_LOG_SERVER_PATH}/detail${YEST_Ymd} ]; then
				cp ${CLICK_LOG_SERVER_PATH}/detail${YEST_Ymd} ${CLICK_LOG_LOCAL_PATH}
			else
				wget -r ftp://${CLICK_LOG_SERVER}${CLICK_LOG_SERVER_PATH}/detail${YEST_Ymd} -P ${CLICK_LOG_LOCAL_PATH} -nd -nH --limit-rate=30M
			fi
	fi
}

if [ $# -eq 1 ]; then
	if [ $1 = "all" ]; then
		java -cp ${CUR_CLASSPATH} com.baidu.beidou.cache.BeidouCache all >> ${LOG_FILE} 2>&1
	elif [ $1 = "yest" ]; then
		download
		if [ $? -eq 0 ]; then
                java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cache.BeidouCache yest >> ${LOG_FILE} 2>&1
        else
                echo "Failed to download click log file "${CLICK_LOG_SERVER_PATH}/detail${YEST_Ymd} >> ${LOG_FILE}
        fi
    elif [ `expr match $1 '[0-9]\{8\}'` -gt 0 ]; then
                java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cache.BeidouCache all iu ${1} >> ${LOG_FILE} 2>&1
	else
		echo $USAGE >> ${LOG_FILE}
	fi
else
	echo $USAGE >> ${LOG_FILE}
fi
