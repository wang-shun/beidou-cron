#!/bin/sh

#@file:importClickUrl.sh
#@author:yangyun
#@date:2010-05-18
#@version:1.0.0.1
#@brief:import cpro-click url data  to table clickMM
#modified by kanghongwei
#modified by wangchongjie for sepreate delete function
#@param YYYYMMDD
CONF_SH=/home/work/.bash_profile
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/importClickUrl.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importClickUrl.sh
reader_list=kanghongwei

TIME_DAY=`date -d "1 day ago" +%Y%m%d`
if  [ $1 ]
then
	TIME_DAY=$1
fi

function prepareFile()
{
	LOG_PATH=${LOG_PATH}${LOG_CLICK}
	LOG_FILE=${LOG_PATH}"/"${DOWNLOAD_FILE_NAME}`date +%Y%m%d`".log"
	DATA_PATH=${DATA_PATH}${DATA_CLICK_PATH}
	DOWNLOAD_FILE=${DATA_PATH}"/"${DOWNLOAD_FILE_NAME}"."${TIME_DAY}
	DOWNLOAD_FILE_MD5=${DATA_PATH}"/"${DOWNLOAD_FILE_NAME}"."${TIME_DAY}".md5"
	DOWNLOAD_FILE_MD5_TMP=${DATA_PATH}"/"${DOWNLOAD_FILE_NAME}"."${TIME_DAY}".md5.tmp"
	DB_INFO=${DATA_PATH}"/"${DB_INFO}
	MERGE_FILE=${DATA_PATH}"/"${MERGE_FILE_NAME}
	
	mkdir -p ${DATA_PATH}
	mkdir -p ${LOG_PATH}
	rm -f ${DOWNLOAD_FILE}
	rm -f ${DOWNLOAD_FILE_MD5}
	rm -f ${DOWNLOAD_FILE_MD5_TMP}
    rm -f ${DB_INFO}
	rm -rf ${MERGE_FILE}*
}

function wgetFiles()
{
	#��logƽ̨���ص��URL�����ļ�
	wget -q -c -t3 -O${DOWNLOAD_FILE} ${DATA_PRE}${TIME_DAY}${DATA_AFTER}
	if [ $? -ne 0 ]
	then
	   echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl download ${DOWNLOAD_FILE_NAME}.${TIME_DAY} failed">>${LOG_FILE}
	   alert 1 "download ${DOWNLOAD_FILE_NAME}.${TIME_DAY} failed"
	   exit 1
	fi
	#��logƽ̨���ص��URL����md5��֤�ļ�
	wget -q -c -t3 -O${DOWNLOAD_FILE_MD5_TMP} ${DATA_PRE}${TIME_DAY}${MD5_AFTER}
	if [ $? -ne 0 ]
	then
	   echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl download ${DOWNLOAD_FILE_NAME}.${TIME_DAY}.md5 failed">>${LOG_FILE}
	   alert 1 "download ${DOWNLOAD_FILE_NAME}.${TIME_DAY}.md5 failed"
	   exit 1
	fi
	#��ȡ����֤md5�ļ�
	awk -vfname="${DOWNLOAD_FILE_NAME}"".""${TIME_DAY}" '{print $2 "  " fname}' ${DOWNLOAD_FILE_MD5_TMP} > ${DOWNLOAD_FILE_MD5}
	rm -f ${DOWNLOAD_FILE_MD5_TMP}
	cd ${DATA_PATH}
	md5sum -c ${DOWNLOAD_FILE_MD5} > /dev/null
	if [ $? -ne 0 ]
	then
		echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl check ${DOWNLOAD_FILE_NAME}.${TIME_DAY}.md5 failed">>${LOG_FILE}
	    alert 1 "check ${DOWNLOAD_FILE_NAME}.${TIME_DAY}.md5 failed"
	    exit 1
	fi
}

function prepareAndCheck()
{
	sql="select id,uid,pid,gid from beidou.cprounitstate0  where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate1 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate2 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate3 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate4 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate5 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate6 where [uid] union all \
		select id,uid,pid,gid from beidou.cprounitstate7 where [uid] "
	runsql_sharding_read "${sql}" "${DB_INFO}"
	if [ $? -ne 0 ]
	then
	    echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl get data from database  failed">>${LOG_FILE}
	    alert 1 "get data from database failed"
	    exit 1
	fi

	java -Xms10240m -Xmx30720m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.ImportClickUrl ${DOWNLOAD_FILE} ${DB_INFO} ${MERGE_FILE} ${TIME_DAY} ${TABLE_COUNT} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf 
	if [ $? -ne 0 ]
	then
	     echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl java  failed">>${LOG_FILE}
	     alert 1 "java failed"
	     exit 1
	fi

	index=0
	count=0
	while [ $index -lt $TABLE_COUNT ]
	do
	  tmp=`wc -l ${MERGE_FILE}${index}|cut -f1 -d " "`
	  count=$(( $count + $tmp ))
	  index=$(($index +1))
	done
	click_count=`wc -l ${DOWNLOAD_FILE}|cut -f1 -d " "`
	ignore_count=$(( $click_count-$count ))
	if [ $ignore_count -gt 0 ]
	then
	    echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl ignore ${ignore_count} lines click data">>${LOG_FILE}
	    if [ $ignore_count -gt $MERGE_WARN_LIMIT ]
	    then
	        alert 1 " ignore ${ignore_count} lines click data"
	        exit 1
	    fi
	fi
}

function importClickData(){
	index=0
	while [ $index -lt $TABLE_COUNT ]
	do
	    runsql_xdb "use beidouurl; delete from ${URL_TABLE}${index} where clickdate ='${TIME_DAY}';"
	    if [ $? -ne 0 ]
	    then 
	    	echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl delete ${TIME_DAY} data from ${URL_TABLE}${index} failed" >> ${LOG_FILE}
	        alert 1 "delete ${TIME_DAY} data from ${URL_TABLE}${index} failed"
	        exit 1
	    fi
	    sleep ${SLEEP_TIME}
	    
	    sql="use beidouurl; load data local infile '${MERGE_FILE}${index}' ignore into table ${URL_TABLE}${index} (site,url,aid,clks,cost,uid,pid,gid,clickdate)"
	    runsql_xdb "${sql}"
	    if [ $? -ne 0 ]
	    then
	        echo "`date +"%Y-%m-%d %H:%M:%S"`,importClickUrl load ${TIME_DAY} data to ${URL_TABLE}${index} table failed" >> ${LOG_FILE}
	        alert 1 "load ${TIME_DAY} data to ${URL_TABLE}${index} table failed"
	        exit 1
	    fi
	    
	    sleep ${INSERT_SLEEP_TIME}
	    index=$(($index +1))
	done
}

#���ָ������ǰ�ġ��������ʡ��͡��û�����شʱ�ġ��鵵�ļ�
function clearArchive()
{
	cd ${DATA_PATH}

	excludeFileNames=""

	if [ ${MAINTAIN_FILE_DAY_COUNT} -gt 1 ]
	then
		clearDay=0
		while [ ${clearDay} -lt ${MAINTAIN_FILE_DAY_COUNT} ]
		do
			dayName=`date -d "-${clearDay} day" "+%Y%m%d"`
			clearDay=$((clearDay+1))
			if [ -z ${excludeFileNames} ]
			then
				excludeFileNames=${1}${dayName}
			else
				excludeFileNames=${excludeFileNames}"|"${1}${dayName}
			fi
		done

		#��ǰ��ѯ���ݿ�ġ��Ĳ�ṹ�ļ�����ɾ��
		excludeFileNames=${excludeFileNames}"|""info"
		#��ǰ�ĺϲ��ļ�(merge0-merge99)��ɾ��
		excludeFileNames=${excludeFileNames}"|"${MERGE_FILE_NAME}
		if [ -n ${excludeFileNames} ]
		then
			rm -f `ls | grep -iv -E ${excludeFileNames}`
		fi
	else
		echo "${MAINTAIN_FILE_DAY_COUNT} should big than 1." >> ${LOG_FILE}
	fi
}

function main()
{
	start=`date +"%s"`
	
	#�����ļ�
	prepareFile
	
	#������־��md5
	wgetFiles
	
	#��ѯ���ݿ⣬��֤����
	prepareAndCheck

	#Ĭ��ɾ��90��֮ǰ������,����������CT������ҵ��ͷ�������
#	deleteClickData
	
	#Ĭ�ϵ�������ĵ������
	importClickData
	
	#ɾ��90��֮ǰ�Ĺ鵵�ļ�
	clearArchive "${DOWNLOAD_FILE_NAME}\."
	
	end=`date +"%s"`
	spendtime=$(($end-$start))
	echo "importClickUrl end at `date +"%Y-%m-%d %H:%M:%S"`,spend time:${spendtime}s" >> ${LOG_FILE}
}

function deleteClickData(){

	LOG_PATH=${LOG_PATH}${LOG_CLICK}
	mkdir -p ${LOG_PATH}
	LOG_FILE=${LOG_PATH}"/"${DOWNLOAD_FILE_NAME}`date +%Y%m%d`".log"
    
	cd ${DATA_PATH}
	index=0
	while [ $index -lt $TABLE_COUNT ]
	do
		delClickDataOfOneTable $index 
		
		sleep ${SLEEP_TIME}
		index=$((${index} + 1))
	done
}

function delClickDataOfOneTable()
{
	cd ${DATA_PATH}
	index=$1
	DELETE_START_DATE=`date -d "-${MAINTAIN_DAY_COUNT} day" "+%Y%m%d"`
	
	rm -rf *_clickx*
	
	runsql_xdb_read "use beidouurl; select id from ${URL_TABLE}${index} where clickdate < '${DELETE_START_DATE}'" "clickIds.tmp"
		
	local TimeStamp=`date +%H%M`
	local File_Preifx=${TimeStamp}_clickx
	local File_Lines=1000

	split -a4 -l ${File_Lines} clickIds.tmp ${File_Preifx}
	totalLines=`wc -l clickIds.tmp| cut -d" " -f1`;
	sliceLines=`cat ${File_Preifx}* | wc -l | cut -d" " -f1`;
	if [ $totalLines -ne $sliceLines ]
	then
		exit 1
	fi

	for file in `ls ${File_Preifx}*`
	do
		retryCount=0
		sucFlag=0
		while [[ $retryCount -lt 3 ]] && [[ $sucFlag -eq 0 ]]
		do
			retryCount=$(($retryCount+1))
			ids=`awk '{printf(",%s ",$0)}' ${file}`
			ids=${ids#,}
			ids=${ids%,}
			
			runsql_xdb "use beidouurl; delete from ${URL_TABLE}${index} where id in (${ids})"
			
			if [ $? -eq 0 ]
			then
				sucFlag=1
			else
				sleep 0.5
			fi
		done
		rm -f ${file}
		sleep 2
	done
	rm -f clickIds.tmp
}

#$1:��������
if [ $# -ne 0 ];then
	if [[ "$1" == "delete" ]];then
		deleteClickData
	fi
fi

#Ĭ��ִ��main����
if [ $# -eq 0 ];then
	main
fi

