#!/bin/bash

#@file: export_beidou_user.sh
#@author: lingbing
#@date: 2011-05-05
#@version: 1.0.0.0
#@brief: export all beidou userid for nova

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/export_beidou_user.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=export_beidou_user.sh
reader_list=lingbing

function check_path()
{
        if ! [ -w $EXPORT_PATH ]
        then
                if ! [ -e $EXPORT_PATH ]
                then
                        mkdir -p $EXPORT_PATH
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

function export_beidou_user()
{
	check_path
    if [ $? -ne 0 ]
    then
            return 1
    fi

	OUTFILE=${EXPORT_PATH}/${EXPORT_FILE_PREFIX}.${TIME_SUFFIX} 	
	OUTHOURFILE=${EXPORT_PATH}/${EXPORT_FILE_PREFIX}
	HOURFILENAME=${EXPORT_FILE_PREFIX}
		
	runsql_sharding_read "use beidou; select distinct userid from cproplan where [userid]" "${OUTFILE}"
	alert $? "export beidou user for nova/crm error"
	
	cp ${OUTFILE} ${OUTHOURFILE}

#	md5sum ${OUTFILE} > ${OUTFILE}.md5
	FILENAME=${EXPORT_FILE_PREFIX}.${TIME_SUFFIX}
	cd ${EXPORT_PATH}
	md5sum ${FILENAME} > ${FILENAME}.md5
	alert $? "md5sum beidou user for nova/crm error"
	
	md5sum ${HOURFILENAME} > ${HOURFILENAME}.md5
	
	
	#regist file to dts
	msg="regist DTS for ${EXPORT_BEIDOU_USER_BEIDOU_USER} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${FILENAME}.md5`
	noahdt add ${EXPORT_BEIDOU_USER_BEIDOU_USER} -m md5=${md5} -i date=${TIME_SUFFIX} bscp://${EXPORT_PATH}/${FILENAME}
	alert $? "${msg}"

	return 0
}

export_beidou_user
alert $? "export_beidou_user run failed!"
