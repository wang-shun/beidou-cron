#!/bin/bash

#@file: google_adx_unit_import.sh
#@author: kanghongwei
#@intention: import units that can launch to google's adx to beidou.cprounitadx[0-7]

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/google_adx_unit_import.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${DATA_PATH}

program=google_adx_unit_import.sh

ADX_UNIT_TO_ADD_FILE=""

if [ $# -ne 1 ]
then
	alert 1 "adx unit to add file can't be null."
else
	ADX_UNIT_TO_ADD_FILE=${GOOGLE_WORK_PATH}/$1
fi


function dealFiles(){
	
	rm -f ${ADX_UNIT_TO_ADD_PICTURE_FILE}
	rm -f ${ADX_UNIT_TO_ADD_FLASH_FILE}
	
	#根据“物料类型”分类
	if [ ! -s ${ADX_UNIT_TO_ADD_FILE} ]
	then
		touch ${ADX_UNIT_TO_ADD_PICTURE_FILE}
		touch ${ADX_UNIT_TO_ADD_FLASH_FILE}
	else
		awk '{if($3==2){print $0}}' ${ADX_UNIT_TO_ADD_FILE} > ${ADX_UNIT_TO_ADD_PICTURE_FILE}
		awk '{if($3==3){print $0}}' ${ADX_UNIT_TO_ADD_FILE} > ${ADX_UNIT_TO_ADD_FLASH_FILE}
	fi
	
}


function filderAdmakerFlash(){
	
	rm -f ${ADX_UNIT_TO_ADD_FLASH_ADMAKER_FILE}
	touch ${ADX_UNIT_TO_ADD_FLASH_ADMAKER_FILE}
	
	if [  -s ${ADX_UNIT_TO_ADD_FLASH_FILE}  ]
	then
		java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.service.google.GoogleAdxUnitImporter ${ADX_UNIT_TO_ADD_FLASH_FILE}  ${ADX_UNIT_TO_ADD_FLASH_ADMAKER_FILE} > ${LOG_PATH}/${API_LOG_NAME} 2>&1
		if [ $? -gt 0 ]
		then
			log "FATAL" "call com.baidu.beidou.cprounit.service.google.GoogleAdxUnitImporter api failed."
			close_log 1
			return 1
		fi
	fi
		
}


function main()
{
	open_log
	
	log "TRACE" "start at `date +%F\ %T`"
	
	# 筛选“待添加”和“待删除”数据
	dealFiles
	
	# 去除“非admaker物料”
	filderAdmakerFlash
	
	log "TRACE" "end at `date +%F\ %T`"
	
	close_log 0
}

main

exit $?

