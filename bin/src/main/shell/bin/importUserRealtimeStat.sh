#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importUserRealtimeStat.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

function getQuarter(){
	date=$1
	hour=$2
	executeTimestamp=$3
		
	minute=0
	beginTimeStamp=`date -d "${date} ${hour}:${minute}" +%s`
	while((${beginTimeStamp} <= ${executeTimestamp}))
	do
		beginTimeStamp=$((${beginTimeStamp} + 900))
		minute=$((${minute} + 15))
	done

	if(${minute} > 0);then
		minute=$((${minute} - 15))
	fi
    echo "`date -d \"${date} ${hour}:${minute}\" +"%Y%m%d %H%M"`" >> ${LOG_FILE}
    echo "`date -d \"${date} ${hour}:${minute}\" +%s`"
}

function importQuarterStat(){
	
	# 下载数据
	nowDate="$1"
	nowHour="$2"
	nowMinute="$3"

	fileName="${FILETYPE}.${nowDate}.${nowHour}${nowMinute}"

	wget -r ftp://${REMOTE_HOST}${REMOTE_DATA_PATH}/${fileName} -O ${fileName} -nd -nH --limit-rate=20M
	#wget -r ftp://${REMOTE_HOST}${REMOTE_DATA_PATH}/${fileName}.md5 -O ${fileName}.md5 -nd -nH --limit-rate=20M

    #md5sum ${fileName} -c ${fileName}.md5
	#if(($? != 0)) ; then
	#	echo "${fileName} check md5 failed!" >> ${LOG_FILE}
	#	return;
	#fi

	# 修改格式及重命名数据文件
	awk '{print $2","$20}' ${fileName} > ${fileName}.tmp
	rm ${fileName} 
    #${fileName}.md5
	mv ${fileName}.tmp ${fileName}
}

function importData(){
	echo "to execute import" >> ${LOG_FILE}
	importStartTimeStamp="$1"
	importEndTimeStamp="$2"
	echo "importStartTimeStamp is ${importStartTimeStamp}" >> ${LOG_FILE}
	echo "importEndTimeStamp is ${importEndTimeStamp}" >> ${LOG_FILE}
	
	todayDate=`date +%Y%m%d`
	todayTimeStamp=`date -d "${todayDate} 00:00:00" +%s`
	importNowTimeStamp=${importStartTimeStamp}
	while((${importNowTimeStamp} <= ${importEndTimeStamp} && ${importNowTimeStamp} >= ${todayTimeStamp}))
	do
		eval $(date -d "1970-01-01 UTC ${importNowTimeStamp} seconds" +"%Y%m%d %H %M" | awk '{printf("nowDate=%s;nowHour=%s;nowMinute=%s;",$1,$2,$3); }' )
	    
		if [ ! -f ${FILETYPE}.${nowDate}.${nowHour}${nowMinute} ] ; then
			echo "importNowTimeStamp is ${importNowTimeStamp}" >> ${LOG_FILE}
	
			# 抓取此刻钟数据
			importQuarterStat ${nowDate} ${nowHour} ${nowMinute}
		fi
	
		# 进行下一刻钟的import
		importNowTimeStamp=$((${importNowTimeStamp} + 900))
	done
}

## 程序开始
mkdir -p ${DATA_PATH}
cd ${DATA_PATH}
# 每次运行的时候都清除上一次的下载的数据
echo "rm all" >> ${LOG_FILE}
rm -rf *

echo -e "======================\n`date +\"%F %T\"`\n" >> ${LOG_FILE}
nowh=`date +%H`
nowm=`date +%M`

#因为08，09 以0开始，会当做八进制对待而报错，所以需要去除前面的0
if [ "${nowh}" = "08" ]
then
    nowh="8"
elif [ "${nowh}" = "09" ]
then
    nowh="9"
fi
#因为实时抓取脚本是从2点才开始抓到数据
if [ $((${nowh})) -lt 2 ]
then
	echo "return for ${nowh} < 2" >> ${LOG_FILE}
	return;
elif [ $((${nowh})) -eq 2 ]
then
	if [ $((${nowm})) -lt 25 ]
	then
		echo "return for ${nowm} < 25" >> ${LOG_FILE}
		return;
	fi
	#清空头天的数据
	echo "delete all from db" >> ${LOG_FILE}
	runsql_xdb "truncate table beidoureport.realtime_stat_user"
	alert_return $? "删除用户层级的昨日实时统计数据失败"
		
	#在2:30的时候把前面的数据全部导入
	startTimeStamp=`getQuarter \`date -d "-150 min " +"%Y%m%d %H"\` \`date -d "-150 min " +%s\` `
	endTimeStamp=`getQuarter \`date -d "-74 min " +"%Y%m%d %H"\` \`date -d "-74 min " +%s\` `
	importData ${startTimeStamp} ${endTimeStamp}
elif [ $((${nowh})) -gt 2 ]
then
	startTimeStamp=`getQuarter \`date -d "-90 min " +"%Y%m%d %H"\` \`date -d "-90 min " +%s\` `
	endTimeStamp=`getQuarter \`date -d "-74 min " +"%Y%m%d %H"\` \`date -d "-74 min " +%s\` `
	importData ${startTimeStamp} ${endTimeStamp}
fi

#调用java代码导入数据
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cache.BeidouCache userRealtime
