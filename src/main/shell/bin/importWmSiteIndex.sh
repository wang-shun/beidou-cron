#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importWmSiteIndex.sh
reader_list=liangshimu

LOG_FILE=${LOG_PATH}/importWmSiteIndex.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="cd ${SITE_DATA_PATH} failed."
cd ${SITE_DATA_PATH}
alert $? "${msg}"

msg="wget ${INDEX_REGION_NAME} failed."
wget -q -t$MAX_RETRY  ${INDEX_FILE_URL}/${INDEX_REGION_NAME}
alert $? "${msg}"

msg="wget ${INDEX_REGION_NAME}.md5 failed."
wget  -q -t$MAX_RETRY ${INDEX_FILE_URL}/${INDEX_REGION_NAME}.md5
alert $? "${msg}"

msg="check ${INDEX_REGION_NAME}.md5 failed."
md5sum -c ${INDEX_REGION_NAME}.md5
alert $? "${msg}"

if [ ! -s ${SITE_DATA_PATH}/${INDEX_REGION_NAME} ];then
    exit 0;
fi

msg="wget ${INDEX_PEOPLE_NAME} failed."
wget -q -t$MAX_RETRY  ${INDEX_FILE_URL}/${INDEX_PEOPLE_NAME}
alert $? "${msg}"

msg="wget ${INDEX_PEOPLE_NAME} failed."
if [ ! -s ${SITE_DATA_PATH}/${INDEX_PEOPLE_NAME} ];then
    alert 1 "${msg}"
fi

msg="wget ${INDEX_PEOPLE_NAME}.md5 failed."
wget  -q -t$MAX_RETRY ${INDEX_FILE_URL}/${INDEX_PEOPLE_NAME}.md5
alert $? "${msg}"

msg="check ${INDEX_PEOPLE_NAME}.md5 failed."
md5sum -c ${INDEX_PEOPLE_NAME}.md5
alert $? "${msg}"

msg="cd ${BIN_PATH} failed."
cd ${BIN_PATH}
alert $? "${msg}"

msg="execute WMSiteIndexImporter failed."
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WMSiteIndexImporter -i ${SITE_DATA_PATH}/${INDEX_REGION_NAME} -i ${SITE_DATA_PATH}/${INDEX_PEOPLE_NAME} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"

msg="delete data from beidouurl.unionsiteindexstat failed."
runsql_xdb "use beidouurl;set charset utf8;delete from beidouurl.unionsiteindexstat;"
alert $? "${msg}"

msg="execute WMSiteIndexImporter failed."
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WMSiteIndexImporter -c 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"

