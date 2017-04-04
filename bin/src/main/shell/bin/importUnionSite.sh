#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=importUnionSite.sh
reader_list=zengyunfeng,zhuqian

LOG_FILE=${LOG_PATH}/importunionsite.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="��������Ŀ¼${SITE_DATA_PATH}ʧ��"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#����unionվ���ļ�
CONF_SH=${UNION_FILE}
[ -f "${CONF_SH}" ] && mv $CONF_SH ${UNION_FILE_BAK}
CONF_SH=${UNION_FILE_NEW_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 

#ץȡ�ļ�����֤MD5
msg="wget�ļ�${UNION_FILE}ʧ��"
wget -q  ${UNION_URL}${UNION_FILE_NEW} 
alert $? "${msg}"

msg="wget�ļ�${UNION_FILE_MD5}ʧ��"
wget  -q ${UNION_URL}${UNION_FILE_NEW_MD5} 
alert $? "${msg}"

msg="${UNION_FILE}�ļ���md5У��ʧ��"
md5sum -c ${UNION_FILE_NEW_MD5}
alert $? "${msg}"

mv ${UNION_FILE_NEW} ${UNION_FILE}




msg="׷�Ӱٶ����������ļ�${CENTRAL_SITE_WHITE_LIST_FILE}ʧ��"
if [ -f "${CENTRAL_SITE_WHITE_LIST_FILE}" ]
then
###modify by liangshimu@cpweb-250, ȡǰ14�У�ͬʱ׷������1�������У������unionһ���������ϲ�
    #cat ${CENTRAL_SITE_WHITE_LIST_FILE} >> ${UNION_FILE}
    awk -F"\t" 'BEGIN{ORS="\n";OFS="\t"}{line="";for(i=1;i<=14;i++){line=line$i"\t"};line=line"1\t1";print line}' ${CENTRAL_SITE_WHITE_LIST_FILE} >> ${UNION_FILE}
    alert $? "${msg}"
fi

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

msg="����unionվ�㷢���쳣"
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.UnionSiteImporter >> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? "${msg}"


