#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importWmSiteVisitorIndex.sh
reader_list=zhangxu

LOG_FILE=${LOG_PATH}/importWmSiteVisitorIndex.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="��������Ŀ¼${SITE_DATA_PATH}ʧ��"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#ץȡ�ļ�
msg="wget�ļ�${INDEX_VISITOR_NAME}ʧ��"
wget -q  ${INDEX_VISITOR_URL}/${INDEX_VISITOR_NAME}
alert $? "${msg}"

msg="wget�ļ�${INDEX_VISITOR_NAME}.md5ʧ��"
wget  -q ${INDEX_VISITOR_URL}/${INDEX_VISITOR_NAME}.md5
alert $? "${msg}"

msg="${INDEX_VISITOR_NAME}�ļ���md5У��ʧ��"
md5sum -c ${INDEX_VISITOR_NAME}.md5
alert $? "${msg}"

msg="�ÿ����������ļ�Ϊ��"
if [ ! -s ${INDEX_VISITOR_NAME} ];then
    alert 1 "${msg}"
fi

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

msg="����ÿ��������ݷ����쳣��������ֶ��ָ�"
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WMSiteVisitorIndexImporter -i ${SITE_DATA_PATH}/${INDEX_VISITOR_NAME} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"

exit 0
