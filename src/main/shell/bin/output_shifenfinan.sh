#!/bin/sh

source statconf.conf
source costconf.conf
source classpath_recommend.conf

if [ ! -f ${SHIFEN_FINAN_FILE_PATH} ]; then
	mkdir -p ${SHIFEN_FINAN_FILE_PATH}
fi

DATEHOUR=`date +"%Y%m%d%H"`
FILENAME=${SHIFEN_FINAN_FILE_NAME}${DATEHOUR}

cd ${HOME_PATH}

java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.OutputShifenFinanFile >> ${LOG_FILE} 2>&1
ifError $? "导出用户财务数据文件失败"

cd ${SHIFEN_FINAN_FILE_PATH}



if [ -f ${FILENAME} ]; then
	echo SHIFEN_FINAN_FILE_PATH
	md5sum ${FILENAME} > ${FILENAME}".md5"
fi