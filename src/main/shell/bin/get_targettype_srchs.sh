#!/bin/bash
#@author:zhangpingan
#@data:2012-07-12
#@version: 1.0.0.0
#@brief:download and mapping targetType srchs file

CONF=../conf/get_targettype_srchs.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=../conf/common.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=alert.sh
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"

program=get_targettype_srchs.sh
reader_list=zhangpingan

DATE_TO_DOWNLOAD=`date -d "1 day ago" +%Y%m%d`
DATE_TO_RECORD=`date -d yesterday +%s`
DATE_NOW=`date +%Y%m%d`
if  [ $1 ]
then
	DATE_TO_DOWNLOAD=$1
	DATE_TO_RECORD=`date -d "${DATE_TO_DOWNLOAD}" +%s`
fi

function prepareFiles()
{
	DATA_PATH_INPUT=${DATA_PATH}${TARGETTYPE_FILE_PATH_INPUT}
	DATA_PATH_OUTPUT=${DATA_PATH}${TARGETTYPE_FILE_PATH_OUTPUT}
	LOG_PATH=${LOG_PATH}${TARGETTYPE_LOG_PATH}
	
	LOG_FILE=${LOG_PATH}"/"${TARGETTYPE_LOG_NAME}${DATE_NOW}
	ARCHIVE_FILE=${DATA_PATH_OUTPUT}"/"${TARGETTYPE_FILE_NAME}"."${DATE_TO_DOWNLOAD}"."${ARCHIVE_FILE_SUFFIX}
	ARCHIVE_FILE_MD5=${DATA_PATH_OUTPUT}"/"${TARGETTYPE_FILE_NAME}"."${DATE_TO_DOWNLOAD}"."${ARCHIVE_FILE_SUFFIX}".md5"
	
	DOWNLOAD_FILE=${DATA_PATH_INPUT}"/"${TARGETTYPE_FILE_NAME}"."${DATE_TO_DOWNLOAD}
	DOWNLOAD_FILE_MD5=${DATA_PATH_INPUT}"/"${TARGETTYPE_FILE_NAME}"."${DATE_TO_DOWNLOAD}".md5"
	DOWNLOAD_FILE_MD5_TMP=${DATA_PATH_INPUT}"/"${TARGETTYPE_FILE_NAME}"."${DATE_TO_DOWNLOAD}".md5.tmp"
	
	mkdir -p ${DATA_PATH_INPUT}
	mkdir -p ${DATA_PATH_OUTPUT}
	mkdir -p ${LOG_PATH}
	rm -f ${ARCHIVE_FILE}
	rm -f ${ARCHIVE_FILE_MD5}
	rm -f ${DOWNLOAD_FILE}
	rm -f ${DOWNLOAD_FILE_MD5}
	rm -f ${DOWNLOAD_FILE_MD5_TMP}
	remove_date=`date -d "${KEEP_DATA} days ago ${DATE_TO_DOWNLOAD}" +%Y%m%d`;
    rm -rf ${DATA_PATH_INPUT}/*.${remove_date}*
	rm -rf ${DATA_PATH_OUTPUT}/*.${remove_date}*
}

function wgetFiles(){
	#download log file
	wget -q -c -t3  --limit-rate=5m -O${DOWNLOAD_FILE} ${DOWNLOAD_LOG_REFIX}${DATE_TO_DOWNLOAD}${DOWNLOAD_FILE_SUFFIX}
	if [ $? -ne 0 ]
	then
	   echo "`date +"%Y-%m-%d %H:%M:%S"`,get_targettype_srchs download ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD} failed">>${LOG_FILE}
	   alert 1 "download ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD} failed"
	   return 1
	fi
	
	#download md5 file
	wget -q -c -t3 -O${DOWNLOAD_FILE_MD5_TMP} ${DOWNLOAD_LOG_REFIX}${DATE_TO_DOWNLOAD}${DOWNLOAD_MD5_SUFFIX}
	if [ $? -ne 0 ]
	then
	   echo "`date +"%Y-%m-%d %H:%M:%S"`,get_targettype_srchs download ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed">>${LOG_FILE}
	   alert 1 "download ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed"
	   return 1
	fi
	
	#check md5
	awk -vfname="${TARGETTYPE_FILE_NAME}"".""${DATE_TO_DOWNLOAD}" '{print $2 "  " fname}' ${DOWNLOAD_FILE_MD5_TMP} > ${DOWNLOAD_FILE_MD5}
	rm -f ${DOWNLOAD_FILE_MD5_TMP}
	cd ${DATA_PATH_INPUT}
	md5sum -c ${DOWNLOAD_FILE_MD5} > /dev/null
	if [ $? -ne 0 ]
	then
		echo "`date +"%Y-%m-%d %H:%M:%S"`,get_targettype_srchs check ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed">>${LOG_FILE}
	    alert 1 "check ${TARGETTYPE_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed"
	    return 1
	fi
}

function targetTypeMapping(){
	awk -v mappingStr=${TARGET_TYPE_MAPPING} -v downloadFile=${DOWNLOAD_FILE} -v logFile=${LOG_FILE} -v recordDate=${DATE_TO_RECORD} -v MaxError=${ERROR_THREASHOLD} 'BEGIN{ 
	        split(mappingStr,target_type_array,",");
	        for (i in target_type_array) {
	             split(target_type_array[i],inner_array,":");
	             log_target_type=inner_array[1];
	             beidou_target_type=inner_array[2];
	             inner_type_array[log_target_type]=beidou_target_type;
	             delete inner_array;
	        }
	        OFS="\t";
			ERROR_LINES=0;
	    }
	    {
	        if($4 in inner_type_array){
	            target_type=inner_type_array[$4];
	            $4=target_type;
	        	print $0;
	        }
	        else{
	            	print "target_type is error in file "downloadFile", line : "$0 >> logFile
					ERROR_LINES=ERROR_LINES+1;
					if(ERROR_LINES > MaxError)
					{
					   exit 1;
					}
	        }
	    }' ${DOWNLOAD_FILE} > ${ARCHIVE_FILE}
	    
	    if [ $? -ne 0 ]
	    then
	    	rm -f ${ARCHIVE_FILE}
	    	rm -f ${ARCHIVE_FILE_MD5}
	    	alert 1 "target_type is wrong for ${DOWNLOAD_FILE}"
	   		return 1
	   	else
	   		md5sum ${ARCHIVE_FILE} > ${ARCHIVE_FILE_MD5}
	    fi
}

function checkResult(){
	if  ! [ -f ${ARCHIVE_FILE} ] || ! [ -f ${ARCHIVE_FILE_MD5} ]
    then
		alert 1 "generate targettype_srchs file for ${DATE_TO_DOWNLOAD} failed."
	    return 1
    fi
}

function main(){
	
	#prepare for run
	prepareFiles
	
	startTime=`date +"%s"`
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_targettype_srchs start run.">>${LOG_FILE}
	
	#download file from log platform
    wgetFiles
    if [ $? -ne 0 ];then
	return 1
	fi
	
    #mapping target_type
    targetTypeMapping
    if [ $? -ne 0 ];then
	return 1
	fi
	
    #check file result
    checkResult
    
    endTime=`date +"%s"`
	spendTime=$((endTime-startTime))
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_targettype_srchs run success, consume ${spendTime}s.">>${LOG_FILE}
}

success_flag=1
fail_count=-1
while [ $success_flag -eq 1 ]
do
fail_count=$((fail_count+1))
if [ $fail_count -gt 180 ];then
  exit 1
fi
main
success_flag=$?
sleep 300
done

