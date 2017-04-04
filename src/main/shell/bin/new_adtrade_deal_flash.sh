#!/bin/bash

#@filename: new_adtrade_deal_flash.sh
#@auther: xuxiaohu
#@date: 2013-06-14
#@version: 1.0.0.0
#@brief: deal flash url 

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_adtrade_deal_flash.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_path()
{
    if ! [ -d $FLASH_INFO_OUTPUT_PATH ]
    then
        mkdir -p $FLASH_INFO_OUTPUT_PATH
    fi

    if ! [ -d $FLASH_INFO_INPUT_PATH ]
    then
        mkdir -p $FLASH_INFO_INPUT_PATH
    fi
 
    return 0
}

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $FLASH_DEAL_LOG_PATH
}

#1:incr 0:full
function deal_flash_msg()
{
    check_path
    if [ $? -ne 0 ]
    then
        return 1
    fi

    inputFile=${FLASH_INFO_INPUT_PATH}$FLASH_INFO_INPUT_FILE
    outputFile=${FLASH_INFO_OUTPUT_PATH}$FLASH_INFO_OUTPUT_FILE
    
    isincr=$1
	if [ $isincr -eq 0 ]
    then
    	inputFile=${inputFile}.full
    	outputFile=${outputFile}.full
    fi

    #delete old data
    if [ -f ${inputFile} ] 
    then
        rm ${inputFile}
    fi

    #delete old data
    if [ -f ${outputFile} ]
    then
        rm ${outputFile}
    fi
    
    cd ${FLASH_INFO_INPUT_PATH} 
    
	if [ $isincr -eq 0 ]
    then
    	wget ftp://${REMOTE_SERVER_URL}${REMOTE_FULL_OUT_PATH}${REMOTE_FULL_OUT_FILE} -nd -nH  --limit-rate=30M || alert $? "$0-Fetch[${REMOTE_FULL_OUT_FILE}]ERROR!"
    	mv ${REMOTE_FULL_OUT_FILE}  ${FLASH_INFO_INPUT_FILE}.full
	else
    	wget ftp://${REMOTE_SERVER_URL}${REMOTE_OUT_PATH}${REMOTE_OUT_FILE} -nd -nH  --limit-rate=30M || alert $? "$0-Fetch[${REMOTE_OUT_FILE}]ERROR!"
    	mv ${REMOTE_OUT_FILE}  ${FLASH_INFO_INPUT_FILE}
	fi
	
    if [ $? -eq 0 ]
    then
    	if [ $isincr -eq 1 ]
    	then
	        total_incre_ad=`wc -l ${inputFile} | awk '{print $1}'`
	        if (( $total_incre_ad > $DEAL_FLASH_THRESHOLD ))
	        then
	            PRINT_LOG "Total incremental ad count is more the threshold: ${DEAL_FLASH_THRESHOLD}"
	            #alert 1 "Total incremental ad count[${total_incre_ad}] is more the threshold: ${DEAL_FLASH_THRESHOLD}"
	            touch $outputFile
	        else
	        	do_retr 1 $outputFile
	    	fi
		else
			do_retr 0 $outputFile
		fi	
    else
        PRINT_LOG "Fail to get remote flash url data!"
        return 1
    fi

    return $?
}

#$1: 1 for incr, 0 for full
#$2: outputFile
function do_retr(){
	local isincr=$1
	local outputfile=$2
	echo "execute decode flash, isincr[${isincr}]" > $FLASH_DEAL_LOG_PATH 
	
	if [ $isincr -eq 0 ];then
		java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.RetrieveInfoFromFlash 0 >> $FLASH_DEAL_LOG_PATH 2>&1
	else
    	java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.RetrieveInfoFromFlash >> $FLASH_DEAL_LOG_PATH 2>&1
	fi
	
    if [ $? -eq 0 ];then 
    	local CHECKFILE_RETRY_TIMES=10
	   	cnt=0   
		while [[ $cnt -lt $CHECKFILE_RETRY_TIMES ]]; do
			if [ ! -s $outputfile ];then
				sleep 60 
			else
				return 0
			fi
			cnt=$(($cnt+1))
		done
				
		if [ ! -f $outputfile ];then
			alert 1 "$0-$outputfile is not exsited!"
		fi
	else
		alert 1 "$0-Fail to retrieve information from flash url!"
	fi
}

if [ $# -eq 0 ]
then
    deal_flash_msg 1
else
    deal_flash_msg 0
fi
exit $?
