#!/bin/bash
#@author:zhangpingan
#@data:2012-07-12
#@version: 1.0.0.0
#@brief:processtargettype srch,clk data

CONF=../conf/merge_target_addata.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=../conf/common.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=alert.sh
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"

program=merge_target_addata.sh
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
	
	TARGETTYPE_SRCH_FILE=${TARGETTYPE_SRCH_FILE_PATH}${DATE_TO_DOWNLOAD}${TARGETTYPE_SRCH_FILE_SUBFIX}
	TARGETTYPE_SRCH_FILE_MD5=${TARGETTYPE_SRCH_FILE_PATH}${DATE_TO_DOWNLOAD}${TARGETTYPE_SRCH_FILE_SUBFIX}.md5
	TARGETTYPE_CLK_FILE=${TARGETTYPE_CLK_FILE_PATH}${DATE_TO_DOWNLOAD}${TARGETTYPE_CLK_FILE_SUBFIX}
	TARGETTYPE_CLK_FILE_MD5=${TARGETTYPE_CLK_FILE_PATH}${DATE_TO_DOWNLOAD}${TARGETTYPE_CLK_FILE_SUBFIX}.md5
	
	mkdir -p ${DATA_PATH_INPUT}
	mkdir -p ${DATA_PATH_OUTPUT}
	mkdir -p ${LOG_PATH}
	rm -f ${DATA_PATH_INPUT}/*
}

function merge_targettype()
{
   cd ${DATA_PATH_INPUT}
   #下载展现，点击数据
   wget -t 3 ${TARGETTYPE_SRCH_FILE} -O TARGETTYPE.srch
   wget -t 3 ${TARGETTYPE_SRCH_FILE_MD5} -O TARGETTYPE.srch.md5
   wget -t 3 ${TARGETTYPE_CLK_FILE} -O TARGETTYPE.clk
   wget -t 3 ${TARGETTYPE_CLK_FILE_MD5} -O TARGETTYPE.clk.md5
   md5sum -c TARGETTYPE.srch.md5
   alert $? "check targettype srch data md5 error"
   md5sum -c TARGETTYPE.clk.md5
   alert $? "check targettype clk data md5 error"
   
   TARGET_TYPE_MAPPING=(-1:"error" 0:"pt" 1:"ct" 2:"qt" 4:"hct" 8:"rt" 16:"vt" 32:"it" 128:"atright")
   for target_item in `echo ${TARGET_TYPE_MAPPING[@]}`;
   do
    tar_value=`echo ${target_item} | awk -F':' '{print $1}'`
    tar_desc=`echo ${target_item} | awk -F':' '{print $2}'`
   	awk -v mappingStr=${TARGET_TYPE_MAPPING} -v MaxError=${ERROR_THREASHOLD} -v tar=${tar_value} '{
				if($4==tar){
				     printf("%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$5,$6,$7)
				}
	    }' TARGETTYPE.srch > ${tar_desc}.srch
		
	alert $? "export ${tar_desc} srch data error"	
	awk -v tar=${tar_value} '{
				if($4==tar){
				     printf("%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$5,$6,$7)
				}
	    }' TARGETTYPE.clk > ${tar_desc}.clk
		
	alert $? "export ${tar_desc} clk data error"
	
	awk '{printf("%s\t%s\t%s\n",$1,$2,$3)}' ${tar_desc}.srch > k1.list
	awk '{printf("%s\t%s\t%s\n",$1,$2,$3)}' ${tar_desc}.clk > k2.list
	cat k1.list k2.list | sort -k3n -u > groupid.list
	
	awk -F'\t' 'ARGIND==1{
		SRCH[$3]=0
		CLICK[$3]=0
		COST[$3]=0
	}ARGIND==2{
		SRCH[$3]=$4
	} ARGIND==3{
		CLICK[$3]=$5
		COST[$3]=$6
	}END{for(groupid in SRCH){printf("%s\t%s\t%s\t%s\n",groupid,SRCH[groupid],CLICK[groupid],COST[groupid])}}' groupid.list ${tar_desc}.srch ${tar_desc}.clk > ${tar_desc}.data
	
	if [ $? -ne 0 ];then
	return 1;
	fi

	awk -F'\t' 'ARGIND==1{
		USERID[$3]=$1
		PLANID[$3]=$2
	}ARGIND==2{
		printf("%s\t%s\t%s\t%s\t%s\t%s\n",USERID[$1],PLANID[$1],$1,$2,$3,$4)
	}' groupid.list ${tar_desc}.data > ${tar_desc}.${DATE_TO_DOWNLOAD}
	alert $? "Merge ${tar_desc} Ad Data Error"
	
	mv ${DATA_PATH_INPUT}/${tar_desc}.${DATE_TO_DOWNLOAD} ${DATA_PATH_OUTPUT}/${tar_desc}.${DATE_TO_DOWNLOAD}
	cd ${DATA_PATH_OUTPUT}
	md5sum ${tar_desc}.${DATE_TO_DOWNLOAD} > ${tar_desc}.${DATE_TO_DOWNLOAD}.md5
	cd ${DATA_PATH_INPUT}
	
   done	
   
   remove_date=`date -d "${KEEP_DATA} days ago ${DATE_TO_DOWNLOAD}" +%Y%m%d`;
   rm -rf ${DATA_PATH_INPUT}/*.${remove_date}*
   rm -rf ${DATA_PATH_OUTPUT}/*.${remove_date}*
   
   cd ${BIN_PATH}
}

function main(){
	#prepare for run/
	prepareFiles
	
	startTime=`date +"%s"`
	merge_targettype
	if [ $? -ne 0 ];then
		alert 1 "merge targettype ad data error"
	fi
	echo "`date +"%Y-%m-%d %H:%M:%S"`,merge targettype ad data.">>${LOG_FILE}
}

main

exit $?

