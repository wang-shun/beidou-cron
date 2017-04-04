#!/bin/bash
#@author:zhangpingan
#@data:2012-07-04
#@version: 1.0.0.0
#@brief:download kt srch data(ct,qt,hct)

CONF=../conf/get_kt_clk.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=../conf/common.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=alert.sh
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"

program=get_kt_clk.sh
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
	DATA_PATH_INPUT=${DATA_PATH}${KT_FILE_PATH_INPUT}
	DATA_PATH_OUTPUT=${DATA_PATH}${KT_FILE_PATH_OUTPUT}
	LOG_PATH=${LOG_PATH}${KT_LOG_PATH}
	
	LOG_FILE=${LOG_PATH}"/"${KT_LOG_NAME}${DATE_NOW}
	ARCHIVE_FILE=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}"."${ARCHIVE_FILE_SUFFIX}
	ARCHIVE_FILE_MD5=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}"."${ARCHIVE_FILE_SUFFIX}".md5"
	
	mkdir -p ${DATA_PATH_INPUT}
	mkdir -p ${DATA_PATH_OUTPUT}
	mkdir -p ${LOG_PATH}
	rm -f ${ARCHIVE_FILE}
	rm -f ${ARCHIVE_FILE_MD5}
	remove_date=`date -d "${KEEP_DATA} days ago ${DATE_TO_DOWNLOAD}" +%Y%m%d`;
	rm -f ${DATA_PATH_INPUT}/*.${remove_date}*
	rm -f ${DATA_PATH_OUTPUT}/*.${remove_date}*
}

function wgetFiles(){
	#download log file
	
	for((i=0;i<24;i++))
	do
	    times=${i}"0000"
	    if [ $i -lt 10 ];then
		   times="0"${i}"0000"
		fi
		
		#首先检查文件是否已经下载完毕，下载完毕则跳过
		CHECK_FILE=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}
		CHECK_FILE_MD5=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}".md5"
		if [ -f ${CHECK_FILE} ];then
		    cd ${DATA_PATH_OUTPUT}
			md5sum -c ${CHECK_FILE_MD5} > /dev/null
			if [ $? -ne 0 ]
			then
				echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk check ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5(output) failed">>${LOG_FILE}
				rm -f ${CHECK_FILE}
				rm -f ${CHECK_FILE_MD5}
				cd ${BIN_PATH}
			else
			    sleep 1;
				cd ${BIN_PATH}
			    continue;
			fi
			
		fi		
		
	    DOWNLOAD_FILE=${DATA_PATH_INPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}
		DOWNLOAD_FILE_MD5=${DATA_PATH_INPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}".md5"
		DOWNLOAD_FILE_MD5_TMP=${DATA_PATH_INPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}".md5.tmp"
		rm -f ${DOWNLOAD_FILE}
	    rm -f ${DOWNLOAD_FILE_MD5}
		rm -f ${DOWNLOAD_FILE_MD5_TMP}
		wget -c -t3  --limit-rate=30m -O${DOWNLOAD_FILE} ${DOWNLOAD_LOG_REFIX}${DATE_TO_DOWNLOAD}${times}${DOWNLOAD_FILE_SUFFIX}
		if [ $? -ne 0 ]
		then
		echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk download ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD} failed">>${LOG_FILE}
		alert 1 "download ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD} failed"
		return 1
		fi
	
	#download md5 file
	wget -c -t3 -O${DOWNLOAD_FILE_MD5_TMP} ${DOWNLOAD_LOG_REFIX}${DATE_TO_DOWNLOAD}${times}${DOWNLOAD_MD5_SUFFIX}
	if [ $? -ne 0 ]
	then
	   echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk download ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed">>${LOG_FILE}
	   alert 1 "download ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed"
	   return 1
	fi
	
	#check md5
	awk -vfname="${KT_FILE_NAME}"".""${DATE_TO_DOWNLOAD}${times}" '{print $2 "  " fname}' ${DOWNLOAD_FILE_MD5_TMP} > ${DOWNLOAD_FILE_MD5}
	rm -f ${DOWNLOAD_FILE_MD5_TMP}
	cd ${DATA_PATH_INPUT}
	md5sum -c ${DOWNLOAD_FILE_MD5} > /dev/null
	if [ $? -ne 0 ]
	then
		echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk check ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed">>${LOG_FILE}
	    alert 1 "check ${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}.md5 failed"
	    return 1
	fi
	
	#成功之后mv文件到ouput目录
	
	mv ${DOWNLOAD_FILE} ${DATA_PATH_OUTPUT}/
	mv ${DOWNLOAD_FILE_MD5} ${DATA_PATH_OUTPUT}/
	done
}


function mergeFiles()
{
    cd ${DATA_PATH_OUTPUT}
	for((i=0;i<24;i++))
	do
	    times=${i}"0000"
	    if [ $i -lt 10 ];then
		   times="0"${i}"0000"
		fi
		
		#首先检查文件是否齐全
		CHECK_FILE=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}
		CHECK_FILE_MD5=${DATA_PATH_OUTPUT}"/"${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}".md5"
		if [ -f ${CHECK_FILE} ];then
			md5sum -c ${CHECK_FILE_MD5} > /dev/null
			if [ $? -ne 0 ]
			then
				echo "`date +"%Y-%m-%d %H:%M:%S"`,${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}(output) missing">>${LOG_FILE}
				alert 1 "${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}(output) missing"
				return 1
			fi
		fi
		
    #处理后的字段列为 keywordid, targettype, clk, cost, userid, planid, groupid, wordid
	awk -F'\t' -v logFile=${LOG_FILE}  -v v='#' '{
		curUserId=$2;
        curPlanId=$3;
        curGroupId=$4;
        curKeywordId=$5;
		curWordId=$6;
		curTargetType=$7;
        curClick=$9;
		curCost=$10;
        
		keyword=$4v$5v$6v$7;
		if(keyword in KTMAP){
		    KTMAP_CLICK[keyword]+=$9
			KTMAP_COST[keyword]+=$10
		} else {
		    KTMAP_CLICK[keyword]=$9
			KTMAP_COST[keyword]=$10
		    KTMAP[keyword]=$2v$3
		}
	}END{for(keyword in KTMAP){printf("%s#%s#%s#%s\n",keyword,KTMAP_CLICK[keyword],KTMAP_COST[keyword],KTMAP[keyword])}}' ${CHECK_FILE} > ${CHECK_FILE}.merge.tmp
    awk -F'#' '{printf("%s#%s#%s#%s#%s#%s#%s#%s\n",$2,$4,$5,$6,$7,$8,$1,$3)}' ${CHECK_FILE}.merge.tmp > ${CHECK_FILE}.merge
	rm -f ${CHECK_FILE}
		
		if [ $? -ne 0 ]
			then
				echo "`date +"%Y-%m-%d %H:%M:%S"`,${KT_FILE_NAME}.${DATE_TO_DOWNLOAD} Error">>${LOG_FILE}
				alert 1 "${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}(output) Merge Error"
				return 1
		fi
	done
	

}


function mergeFilesAll()
{
    cd ${DATA_PATH_OUTPUT}
    rm -f ${ARCHIVE_FILE}.tmp
	touch ${ARCHIVE_FILE}.tmp
	for((i=0;i<24;i++))
	do
	    times=${i}"0000"
	    if [ $i -lt 10 ];then
		   times="0"${i}"0000"
		fi
		#首先检查文件是否齐全
		CHECK_FILE=${KT_FILE_NAME}"."${DATE_TO_DOWNLOAD}${times}.merge
		if [ ! -f ${CHECK_FILE} ];then
				echo "`date +"%Y-%m-%d %H:%M:%S"`,${KT_FILE_NAME}.${DATE_TO_DOWNLOAD}(output).merge missing">>${LOG_FILE}
				alert 1 "${CHECK_FILE} missing"
				return 1
		fi
   	    cat ${CHECK_FILE} >> ${ARCHIVE_FILE}.tmp
	done
	
	
	#${ARCHIVE_FILE}.tmp字段列为 keywordid, targettype, click, cost, userid, planid, groupid, wordid
	awk -F'#' -v logFile=${LOG_FILE}  -v v='#' '{
		curUserId=$5;
        curPlanId=$6;
        curGroupId=$7;
        curKeywordId=$1;
		curWordId=$8;
		curTargetType=$2;
        curClick=$3;
		curCost=$4;
        
		keyword=$1v$2v$7v$8;
		if(keyword in KTMAP){
		    KTMAP_CLICK[keyword]+=$3
			KTMAP_COST[keyword]+=$4
		} else {
		    KTMAP_CLICK[keyword]=$3
			KTMAP_COST[keyword]=$4
		    KTMAP[keyword]=$5v$6
		}
	}END{for(keyword in KTMAP){printf("%s#%s#%s#%s\n",keyword,KTMAP_CLICK[keyword],KTMAP_COST[keyword],KTMAP[keyword])}}' ${ARCHIVE_FILE}.tmp > ${ARCHIVE_FILE}.merge.tmp
	
	#keywordid, targettype, click, cost, userid, planid, groupid, wordid
	
	
	awk -F'#' '{printf("%s#%s#%s#%s#%s#%s#%s#%s\n",$1,$2,$5,$6,$7,$8,$3,$4)}' ${ARCHIVE_FILE}.merge.tmp > ${ARCHIVE_FILE}.merge
	rm -f ${ARCHIVE_FILE}.merge.tmp
	
	rm -f ${ARCHIVE_FILE}.QT
	rm -f ${ARCHIVE_FILE}.CT
	rm -f ${ARCHIVE_FILE}.HCT
	
	#keywordid, wordid, userid, planid, groupid, srchs, click, cost
	
	#778063919#8#3#270#374574#6207#11083#31634
	awk -F'#' '{
	    if($2==8){
			printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$5,$6,$7,$1,$8,0,$3,$4);
		}
	}' ${ARCHIVE_FILE}.merge > ${ARCHIVE_FILE}.QT
	
	awk -F'#' '{
	    if($2==64){
			printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$5,$6,$7,$1,$8,0,$3,$4);
		}
	}' ${ARCHIVE_FILE}.merge > ${ARCHIVE_FILE}.CT
	
	awk -F'#' '{
	    if($2==1024){
			printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$5,$6,$7,$1,$8,0,$3,$4);
		}
	}' ${ARCHIVE_FILE}.merge > ${ARCHIVE_FILE}.HCT
}

function main(){
	
	#prepare for run/
	prepareFiles
	
	startTime=`date +"%s"`
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk start run.">>${LOG_FILE}
	
	#download file from log platform
    wgetFiles
	if [ $? -ne 0 ];then
	return 1
	fi
    
    endTime=`date +"%s"`
	spendTime=$((endTime-startTime))
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk run success, consume ${spendTime}s.">>${LOG_FILE}
	
	startTime=`date +"%s"`
	echo "`date +"%Y-%m-%d %H:%M:%S"`,kt_clk merge run.">>${LOG_FILE}
	mergeFiles
	if [ $? -ne 0 ];then
	return 1
	fi
	endTime=`date +"%s"`
	spendTime=$((endTime-startTime))
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk run success, consume ${spendTime}s.">>${LOG_FILE}
	
	startTime=`date +"%s"`
	echo "`date +"%Y-%m-%d %H:%M:%S"`,kt_clk merge run.">>${LOG_FILE}
	mergeFilesAll
	endTime=`date +"%s"`
	spendTime=$((endTime-startTime))
	echo "`date +"%Y-%m-%d %H:%M:%S"`,get_kt_clk run success, consume ${spendTime}s.">>${LOG_FILE}
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


