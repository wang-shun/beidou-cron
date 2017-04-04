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
	
	# ��������
	nowDate="$1"
	nowHour="$2"
	nowMinute="$3"

	dateStr="${nowDate}${nowHour}${nowMinute}"
	fileName="${FILETYPE}.${nowDate}.${nowHour}${nowMinute}"

	wget "${DATA_PREFIX}&date=${dateStr}" -O ${FILETYPE}
	wget "${MD5_PREFIX}&date=${dateStr}" -O ${FILETYPE}.md5

	# ��֤����
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

	# �޸ĸ�ʽ�������������ļ�
	awk '{print "\t"$0}' ${FILETYPE} > ${fileName}
	mv ${FILETYPE}.md5 ${fileName}.md5

	# �������ݵ����ݿ�
	runsql_xdb "load data local infile '${DATA_PATH}/${fileName}' into table beidoureport.realtime_stat"
	alert_return $? "����ʵʱͳ������ ${fileName} ʧ��"

}

## ����ʼ
mkdir -p ${DATA_PATH}
cd ${DATA_PATH}

echo -e "======================\n`date +\"%F %T\"`\n" >> ${LOG_FILE}

paramCnt=$#
if(($paramCnt >= 1)) ; then

	## �޸�ͳ�����ݣ��������� "20121108 20:00" ["20121108 20:45"]
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
	
		# ɾ�������ݿ��д˿��ӵ�����
		runsql_xdb "delete from beidoureport.realtime_stat where time=${fixNowTimeStamp}"	
		alert_return $? "ɾ��ʵʱͳ�ƿ�������ʧ�� ${nowDate} ${nowHour} ${nowMinute}"

		# ����ץȡ�˿������ݲ��������ݿ�
		importQuarterStat ${nowDate} ${nowHour} ${nowMinute}

		# ������һ���ӵ�fix
		fixNowTimeStamp=$((${fixNowTimeStamp} + 900))

	done
	
else
	## ����ִ��


	# ɾ��7��ǰ��ʱ����ļ���ɾ��7��ǰ��logƽ̨�����ļ����������0ʱ��ʱ�����ִ��ʱ�̵�ʱ���
	removeDate=`date -d "7 day ago" +%Y%m%d`
	rm -f time.${removeDate}
	rm -f ${FILETYPE}.${removeDate}*
	todayDate=`date +%Y%m%d`
	todayTimeStamp=`date -d "${todayDate} 00:00:00" +%s`


	# ���û�н����ʱ����ļ������������ʱ����ļ�����ɾ�����ݿ������е�����
	if [ ! -f time.${todayDate} ] ; then
		touch time.${todayDate}
		echo "delete all from db" >> ${LOG_FILE}
		runsql_xdb "truncate table beidoureport.realtime_stat"
		alert_return $? "ɾ������ʵʱͳ������ʧ��"
	fi


	# ���뱨���߼�
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
			alert_return 1 "ʵʱͳ������ ${nowDate}.${nowHour}${nowMinute} ץȡʧ��"
		fi

		# ������һ���ӵ�warn
		warnNowTimeStamp=$((${warnNowTimeStamp} + 900))
	done


	# ����ץȡ�����߼�
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

			# ץȡ�˿������ݲ��������ݿ�
			importQuarterStat ${nowDate} ${nowHour} ${nowMinute}
		fi

		# ������һ���ӵ�import
		importNowTimeStamp=$((${importNowTimeStamp} + 900))
	done

fi


