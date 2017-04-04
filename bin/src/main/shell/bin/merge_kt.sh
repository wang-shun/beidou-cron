#!/bin/bash
#@author:zhangpingan
#@data:2012-07-04
#@version: 1.0.0.0
#@brief:download kt srch data(ct,qt,hct)

CONF=../conf/merge_kt.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=../conf/common.conf
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"
CONF=alert.sh
[ -f "${CONF}" ] && source $CONF || echo "not exist ${CONF}"

program=get_kt_srchs.sh
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
	
	CT_SRCH_FILE=${KT_SRCH_FILE_PATH}${DATE_TO_DOWNLOAD}${CT_SRCH_FILE_SUBFIX}
	QT_SRCH_FILE=${KT_SRCH_FILE_PATH}${DATE_TO_DOWNLOAD}${QT_SRCH_FILE_SUBFIX}
	HCT_SRCH_FILE=${KT_SRCH_FILE_PATH}${DATE_TO_DOWNLOAD}${HCT_SRCH_FILE_SUBFIX}
	
	CT_CLK_FILE=${KT_CLK_FILE_PATH}${DATE_TO_DOWNLOAD}${CT_CLK_FILE_SUBFIX}
	QT_CLK_FILE=${KT_CLK_FILE_PATH}${DATE_TO_DOWNLOAD}${QT_CLK_FILE_SUBFIX}
	HCT_CLK_FILE=${KT_CLK_FILE_PATH}${DATE_TO_DOWNLOAD}${HCT_CLK_FILE_SUBFIX}
	
	mkdir -p ${DATA_PATH_INPUT}
	mkdir -p ${DATA_PATH_OUTPUT}
	mkdir -p ${LOG_PATH}
	rm -f ${DATA_PATH_INPUT}/*
	remove_date=`date -d "${KEEP_DATA} days ago ${DATE_TO_DOWNLOAD}" +%Y%m%d`;
	rm -f ${DATA_PATH_OUTPUT}/*.${remove_date}*
}

function merge_ct()
{
   cd ${DATA_PATH_INPUT}
   #下载CT展现，点击数据
   wget -t 3 ${CT_SRCH_FILE} -O CT.srch
   wget -t 3 ${CT_CLK_FILE} -O CT.clk
   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' CT.srch > k1.list
   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' CT.clk > k2.list

   cat k1.list k2.list | sort -k3n -k5n -u > keyword.list

	awk -F'\t' -v v='#' 'ARGIND==1{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=0
		CLICK[key]=0
	COST[key]=0
	}ARGIND==2{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=$6
	} ARGIND==3{
	    key=$1v$2v$3v$4v$5
		CLICK[key]=$7
		COST[key]=$8
	}END{for(keyword in SRCH){printf("%s#%s#%s#%s\n",keyword,SRCH[keyword],CLICK[keyword],COST[keyword])}}' keyword.list CT.srch CT.clk > CT.data.tmp
	
	if [ $? -ne 0 ];then
	return 1;
	fi
	
	awk -F'#' '{printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5,$6,$7,$8)}' CT.data.tmp > CT.${DATE_TO_DOWNLOAD}
	if [ $? -ne 0 ];then
	return 1;
	fi
	rm -f CT.data.tmp
	mv ${DATA_PATH_INPUT}/CT.${DATE_TO_DOWNLOAD} ${DATA_PATH_OUTPUT}/CT.${DATE_TO_DOWNLOAD}
	cd ${DATA_PATH_OUTPUT}
	md5sum CT.${DATE_TO_DOWNLOAD} > CT.${DATE_TO_DOWNLOAD}.md5
	cd ${BIN_PATH}
}

function merge_qt()
{
   cd ${DATA_PATH_INPUT}
   #下载QT展现，点击数据
   wget -t 3 ${QT_SRCH_FILE} -O QT.srch
   wget -t 3 ${QT_CLK_FILE} -O QT.clk

   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' QT.srch > k1.list
   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' QT.clk > k2.list

   cat k1.list k2.list | sort -k3n -k5n -u > keyword.list

	awk -F'\t' -v v='#' 'ARGIND==1{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=0
		CLICK[key]=0
	COST[key]=0
	}ARGIND==2{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=$6
	} ARGIND==3{
	    key=$1v$2v$3v$4v$5
		CLICK[key]=$7
		COST[key]=$8
	}END{for(keyword in SRCH){printf("%s#%s#%s#%s\n",keyword,SRCH[keyword],CLICK[keyword],COST[keyword])}}' keyword.list QT.srch QT.clk > QT.data.tmp
	if [ $? -ne 0 ];then
	return 1;
	fi

	awk -F'#' '{printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5,$6,$7,$8)}' QT.data.tmp > QT.${DATE_TO_DOWNLOAD}
	if [ $? -ne 0 ];then
	return 1;
	fi
	rm -f QT.data.tmp
	mv ${DATA_PATH_INPUT}/QT.${DATE_TO_DOWNLOAD} ${DATA_PATH_OUTPUT}/QT.${DATE_TO_DOWNLOAD}
	cd ${DATA_PATH_OUTPUT}
	md5sum QT.${DATE_TO_DOWNLOAD} > QT.${DATE_TO_DOWNLOAD}.md5
	cd ${BIN_PATH}
}


function merge_hct()
{
   cd ${DATA_PATH_INPUT}
   #下载HCT展现，点击数据
   wget -t 3 ${HCT_SRCH_FILE} -O HCT.srch
   wget -t 3 ${HCT_CLK_FILE} -O HCT.clk
   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' HCT.srch > k1.list
   awk '{printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}' HCT.clk > k2.list

   cat k1.list k2.list | sort -k3n -k5n -u > keyword.list

	awk -F'\t' -v v='#' 'ARGIND==1{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=0
		CLICK[key]=0
	COST[key]=0
	}ARGIND==2{
	    key=$1v$2v$3v$4v$5
		SRCH[key]=$6
	} ARGIND==3{
	    key=$1v$2v$3v$4v$5
		CLICK[key]=$7
		COST[key]=$8
	}END{for(keyword in SRCH){printf("%s#%s#%s#%s\n",keyword,SRCH[keyword],CLICK[keyword],COST[keyword])}}' keyword.list HCT.srch HCT.clk > HCT.data.tmp
	if [ $? -ne 0 ];then
	return 1;
	fi

	awk -F'#' '{printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5,$6,$7,$8)}' HCT.data.tmp > HCT.${DATE_TO_DOWNLOAD}
	if [ $? -ne 0 ];then
	return 1;
	fi
	rm -f HCT.data.tmp
	mv ${DATA_PATH_INPUT}/HCT.${DATE_TO_DOWNLOAD} ${DATA_PATH_OUTPUT}/HCT.${DATE_TO_DOWNLOAD}
	cd ${DATA_PATH_OUTPUT}
	md5sum HCT.${DATE_TO_DOWNLOAD} > HCT.${DATE_TO_DOWNLOAD}.md5
	cd ${BIN_PATH}
}

function main(){
	#prepare for run/
	prepareFiles
	
	startTime=`date +"%s"`
	merge_hct
	if [ $? -ne 0 ];then
		alert 1 "merge hct error"
	fi
	echo "`date +"%Y-%m-%d %H:%M:%S"`,merge hct.">>${LOG_FILE}
	
	startTime=`date +"%s"`
	merge_ct
	if [ $? -ne 0 ];then
		alert 1 "merge ct error"
	fi
	echo "`date +"%Y-%m-%d %H:%M:%S"`,merge ct.">>${LOG_FILE}
	
	startTime=`date +"%s"`
	merge_qt
	if [ $? -ne 0 ];then
		alert 1 "merge qt error"
	fi
	echo "`date +"%Y-%m-%d %H:%M:%S"`,merge qt.">>${LOG_FILE}
}

main

exit $?

