#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importRealtimeStat.conf"
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

	minute=$((${minute} - 15))
	echo "`date -d \"${date} ${hour}:${minute}\" +%s`"
}

function importQuarterStat(){
	
	# 下载数据
	nowDate="$1"
	nowHour="$2"
	nowMinute="$3"

	dateStr="${nowDate}${nowHour}${nowMinute}"
	fileName="${FILETYPE}.${nowDate}.${nowHour}${nowMinute}"

	wget "${DATA_PREFIX}&date=${dateStr}" -O ${FILETYPE}
	wget "${MD5_PREFIX}&date=${dateStr}" -O ${FILETYPE}.md5

	# 验证数据
	size=`ls -l ${FILETYPE} | awk '{print $5}'`
	if(($size < 100 )) ; then
		echo "${fileName} does not ready!" >> ${LOG_FILE}
		return;
	fi

	awk '{print $2"  "$1}' ${FILETYPE}.md5 > tmp && mv tmp ${FILETYPE}.md5
	md5sum -c ${FILETYPE}.md5
	if(($? != 0)) ; then
		echo "${fileName} check md5 failed!" >> ${LOG_FILE}
		return;
	fi

	# 修改格式及重命名数据文件
	awk '{print "\t"$0}' ${FILETYPE} > ${fileName}
	mv ${FILETYPE}.md5 ${fileName}.md5

	# 导入数据到数据库
	runsql_xdb "load data local infile '${DATA_PATH}/${fileName}' into table beidoureport.realtime_stat"
	alert_return $? "导入实时统计数据 ${fileName} 失败"

}

## 程序开始
mkdir -p ${DATA_PATH}
cd ${DATA_PATH}

echo -e "======================\n`date +\"%F %T\"`\n" >> ${LOG_FILE}

paramCnt=$#
if(($paramCnt >= 1)) ; then

	## 修复统计数据，参数形如 "20121108 20:00" ["20121108 20:45"]
	echo "to fix realtime stat" >> ${LOG_FILE}

	fixStartTime="$1"
	fixEndTime="$1"
	if(($paramCnt >= 2)) ; then
		fixEndTime="$2"
	fi

	fixStartTimeStamp=`date -d "${fixStartTime}" +%s`
	fixEndTimeStamp=`date -d "${fixEndTime}" +%s`
	echo "fixStartTimeStamp is ${fixStartTimeStamp}" >> ${LOG_FILE}
	echo "fixEndTimeStamp is ${fixEndTimeStamp}" >> ${LOG_FILE}

	fixNowTimeStamp=${fixStartTimeStamp}
	while((${fixNowTimeStamp} <= ${fixEndTimeStamp}))
	do
		eval $(date -d "1970-01-01 UTC ${fixNowTimeStamp} seconds" +"%Y%m%d %H %M" | awk '{printf("nowDate=%s;nowHour=%s;nowMinute=%s;",$1,$2,$3); }' )
	
		# 删除掉数据库中此刻钟的数据
		runsql_xdb "delete from beidoureport.realtime_stat where time=${fixNowTimeStamp}"	
		alert_return $? "删除实时统计刻钟数据失败 ${nowDate} ${nowHour} ${nowMinute}"

		# 重新抓取此刻钟数据并导入数据库
		importQuarterStat ${nowDate} ${nowHour} ${nowMinute}

		# 进行下一刻钟的fix
		fixNowTimeStamp=$((${fixNowTimeStamp} + 900))

	done
	
else
	## 正常执行


	# 删除7天前的时间戳文件，删除7天前的log平台下载文件，计算今天0时的时间戳，执行时刻的时间戳
	removeDate=`date -d "7 day ago" +%Y%m%d`
	rm -f time.${removeDate}
	rm -f ${FILETYPE}.${removeDate}*
	todayDate=`date +%Y%m%d`
	todayTimeStamp=`date -d "${todayDate} 00:00:00" +%s`


	# 如果没有今天的时间戳文件，则建立今天的时间戳文件，并删除数据库中所有的数据
	if [ ! -f time.${todayDate} ] ; then
		touch time.${todayDate}
		echo "delete all from db" >> ${LOG_FILE}
		runsql_xdb "truncate table beidoureport.realtime_stat"
		alert_return $? "删除昨日实时统计数据失败"
	fi


	# 进入报警逻辑
	echo "to execute warning" >> ${LOG_FILE}

	warnStartTimeStamp=`getQuarter \`date -d "-3 hour " +"%Y%m%d %H"\` \`date -d "-3 hour " +%s\` `
	warnEndTimeStamp=`getQuarter \`date -d "-2 hour " +"%Y%m%d %H"\` \`date -d "-2 hour " +%s\` `
	echo "warnStartTimeStamp is ${warnStartTimeStamp}" >> ${LOG_FILE}
	echo "warnEndTimeStamp is ${warnEndTimeStamp}" >> ${LOG_FILE}

	warnNowTimeStamp=${warnStartTimeStamp}
	while((${warnNowTimeStamp} <= ${warnEndTimeStamp} && ${warnNowTimeStamp} >= ${todayTimeStamp}))
	do
		eval $(date -d "1970-01-01 UTC ${warnNowTimeStamp} seconds" +"%Y%m%d %H %M" | awk '{printf("nowDate=%s;nowHour=%s;nowMinute=%s;",$1,$2,$3); }' )

		if [ ! -f ${FILETYPE}.${nowDate}.${nowHour}${nowMinute} ] ; then
			echo "warnNowTimeStamp is ${warnNowTimeStamp}" >> ${LOG_FILE}
			alert_return 1 "实时统计数据 ${nowDate}.${nowHour}${nowMinute} 抓取失败"
		fi

		# 进行下一刻钟的warn
		warnNowTimeStamp=$((${warnNowTimeStamp} + 900))
	done


	# 进入抓取数据逻辑
	echo "to execute import" >> ${LOG_FILE}
	importStartTimeStamp=`getQuarter \`date -d "-2 hour " +"%Y%m%d %H"\` \`date -d "-2 hour " +%s\` `
	importEndTimeStamp=`getQuarter \`date -d "-50 min " +"%Y%m%d %H"\` \`date -d "-50 min " +%s\` `
	echo "importStartTimeStamp is ${importStartTimeStamp}" >> ${LOG_FILE}
	echo "importEndTimeStamp is ${importEndTimeStamp}" >> ${LOG_FILE}

	importNowTimeStamp=${importStartTimeStamp}
	while((${importNowTimeStamp} <= ${importEndTimeStamp} && ${importNowTimeStamp} >= ${todayTimeStamp}))
	do
		eval $(date -d "1970-01-01 UTC ${importNowTimeStamp} seconds" +"%Y%m%d %H %M" | awk '{printf("nowDate=%s;nowHour=%s;nowMinute=%s;",$1,$2,$3); }' )

		if [ ! -f ${FILETYPE}.${nowDate}.${nowHour}${nowMinute} ] ; then
			echo "importNowTimeStamp is ${importNowTimeStamp}" >> ${LOG_FILE}

			# 抓取此刻钟数据并导入数据库
			importQuarterStat ${nowDate} ${nowHour} ${nowMinute}
		fi

		# 进行下一刻钟的import
		importNowTimeStamp=$((${importNowTimeStamp} + 900))
	done

fi


