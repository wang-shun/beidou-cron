#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/replacedrmcmater.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=replacedrmcmater.sh
reader_list=liuhao05

mkdir -p ${LOG_PATH}
mkdir -p ${DRMC_INPUT_PATH}
mkdir -p ${DRMC_OUTPUT_PATH}

if [ ! -f ${LOGINFO_FILE} ]; then
    echo "" >> ${LOGINFO_FILE}
fi

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

if [ ! -f ${ADIDS_CSV_FILE} ]; then
    echo "adids.csv file not found" >> ${LOG_FILE}
fi

if [ ! -f ${IMAGES_ZIP_FILE} ]; then
    echo "images.zip file not found" >> ${LOG_FILE}
else
	cd ${DRMC_INPUT_PATH}
	unzip -o ${IMAGES_ZIP_FILE} -d ./images
	cd -
fi

if [ -f ${ADIDS_CSV_FILE} ]&&[ -d ${IMAGE_FILES} ]; then
	msg="invoke java failed"
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.ReplaceDrmcMater ${ADIDS_CSV_FILE} ${IMAGE_FILES} ${LOGINFO_FILE} >> ${LOG_FILE}  2>&1

	# if the relt of "java" is wrong then send error message
	alert $? ${msg}
fi

if [ -f ${ADIDS_CSV_FILE} ]; then
    rm ${ADIDS_CSV_FILE}
fi

if [ -f ${IMAGES_ZIP_FILE} ]; then
    rm ${IMAGES_ZIP_FILE}
fi

if [ -d ${IMAGE_FILES} ]; then
	rm -rf ${IMAGE_FILES}
fi
