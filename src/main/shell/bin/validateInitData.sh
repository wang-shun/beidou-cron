#!/bin/sh
#У����Ӫ�������ݣ�����ʱ�贫����Ӫ��������·��

#CONF_SH="/home/work/.bash_profile"
#[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=./alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/validateInitData.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


DATE_NOW=`date +"%Y%m%d"`
VALIDATE_DATA_PATH_INPUT=${DATA_PATH}${VALIDATE_PATH_INPUT}
VALIDATE_DATA_PATH_OUTPUT=${DATA_PATH}${VALIDATE_PATH_OUTPUT}
LOG_PATH=${LOG_PATH}${VALIDATE_LOG_PATH} 
LOG_FILE=${LOG_PATH}"/"${VALIDATE_LOG_NAME}${DATE_NOW}

TMP_CPROGROUP_DATA=${VALIDATE_DATA_PATH_OUTPUT}"/"${TMP_CPROGROUP_DATA_FILE_NAME}
VALIDATE_PASS_DATA=${VALIDATE_DATA_PATH_OUTPUT}"/"${VALIDATE_PASS_DATA_FILE_NAME}


mkdir -p "${VALIDATE_DATA_PATH_INPUT}"
mkdir -p "${VALIDATE_DATA_PATH_OUTPUT}"
mkdir -p "${LOG_PATH}"


#��ѯ�����е��ƹ���
sql="select userid,planid,groupid from beidou.cprogroup"

runsql_sharding_read "$sql" "$TMP_CPROGROUP_DATA"

#�Ա����ݻὫУ��ͨ���ļ�д����ʱ�ļ�
awk '
	 ARGIND==1{
	 index1=$1$2$3;
	 allgroup[index1]
 	}
	 ARGIND==2{
	 index2=$1$2$3;
	 if(index2 in allgroup){print $0}
 	}
' $TMP_CPROGROUP_DATA $1 > $VALIDATE_PASS_DATA