#!/bin/bash

#@file: vtpeople_export.sh
#@author: lvzichan
#@date: 2013-07-08
#@version: 1.0.0.0
#@brief: export pid,hpid from beidou.vtpeople where type in(2,3) 

DEBUG_MOD=0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/vtpeople_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

if [ $? -ne 0 ]
then
	echo "load basic libfile failed"
	exit 1;
fi

function check_conf()
{
	if ! [ $EXPORT_PATH ]
	then
		echo "Conf[$EXPORT_PATH] is empty or its value is invalid"
		return 1
	fi

	if ! [ $EXPORT_FILE_PID2HPID ]
	then
		echo "Conf[$EXPORT_FILE_PID2HPID] is empty or its value is invalid"
		return 1
	fi
	
	return 0
}

function check_path()
{
	if ! [ -w $EXPORT_PATH ]
	then
		if ! [ -e $EXPORT_PATH ]
		then
			mkdir $EXPORT_PATH
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$EXPORT_PATH]!"
				return 1
			fi
		else
			log "FATAL" "Path[$EXPORT_PATH] is not writable!"
			return 1
		fi
	fi
	return 0	
}

function vtpeople_export()
{
	#check conf and path
	check_conf
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	check_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	#export required data to file
	DATE=`date +%Y%m%d -d"1 days ago"`
	outFile_pid2hpid=${EXPORT_FILE_PID2HPID}_${DATE}
	
	cd $EXPORT_PATH
	SQL="select pid, hpid from beidou.vtpeople where [userid] and type in (2,3)";
	runsql_sharding_read "$SQL" $outFile_pid2hpid
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_pid2hpid] Export Error."
		alert 1 "[$outFile_pid2hpid] Export Error."
		return 1
	fi
	
	md5sum ${outFile_pid2hpid} > ${outFile_pid2hpid}.md5
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to generate md5 for [$outFile_pid2hpid]."
		return 1
	fi
	
    #regist file to dts
	msg="regist DTS for ${VTPEOPLE_EXPORT_PID2HPID} failed."
	md5=`getMd5FileMd5 $EXPORT_PATH/$outFile_pid2hpid.md5`
	noahdt add ${VTPEOPLE_EXPORT_PID2HPID} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${outFile_pid2hpid}
	if [ $? -ne 0 ]
	then
		log "FATAL" "${msg}"
		alert 1 "${msg}"
		return 1
	fi
	
	return 0
}

vtpeople_export

exit $?