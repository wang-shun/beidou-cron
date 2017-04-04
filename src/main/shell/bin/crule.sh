#!/bin/sh
#ͬ��
#
ROOT_PATH=/home/zengyf/work/shell/beidou-cron
CRULE_USER=beidoudb
CRULE_PASSWORD=123456
#CRULE_SERVER=tc-sf-aka00.tc.baidu.com
CRULE_SERVER=jx-veyron00.jx.baidu.com
#CRULE_PATH=/home/work/fc-aka/dict/
CRULE_PATH=/home/beidoudb/fc-aka/fc-aka/dict
CRULE_FILE=wordrule
CRULE_FILE_MD5=wordrule.md5
BD_CRULE_FILE=wordrule
BD_CRULE_FILE_MD5=wordrule.md5
BD_CRULE_PATH=${ROOT_PATH}/dict
LOG_PATH=${ROOT_PATH}/logs
LOG_FILE=${LOG_PATH}/crule.log


type="ERROR"
module=beidou-cron
program=crule.sh
reader_list=zengyunfeng

alert() {

	if [ $# -lt 2 ]
	then
		return
	fi
	ifError $1 "[${type}][${module}]$2@${CURR_DATETIME}" "${program}" \
			"$2" "${reader_list}"
	
}


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${BD_CRULE_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

msg="���빤��Ŀ¼${ROOT_PATH}ʧ��"
cd ${ROOT_PATH}
alert $? ${msg}


CONF_SH=${CRULE_FILE}
[ -f "${CONF_SH}" ] && rm $CONF_SH 
CONF_SH=${CRULE_FILE_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 



#ץȡ�ﳲ�ĺ������ļ������ĳ���ļ�����md5�����ڣ���ֱ�Ӹ���������������ִ�У����md5У�鲻ͨ����Ҳֹͣ
msg="���طﳲ��������md5�ļ�ʧ�ܣ�"
wget -q ftp://${CRULE_USER}:${CRULE_PASSWORD}@${CRULE_SERVER}/${CRULE_PATH}/${CRULE_FILE_MD5}
alert $? ${msg}

msg="���طﳲ�������ļ�ʧ�ܣ�"
wget -q ftp://${CRULE_USER}:${CRULE_PASSWORD}@${CRULE_SERVER}/${CRULE_PATH}/${CRULE_FILE}
alert $? ${msg}

msg="�ﳲ�������ļ���md5У��ʧ��"
md5sum -c ${CRULE_FILE_MD5}
alert $? ${msg}

msg="�ﳲ�������ļ�����ʧ��"
mv ${CRULE_FILE} ${CRULE_FILE}.tmp
alert $? ${msg}

msg="����beidou�������ļ�ʧ��"
#awk '$4==1 {print $0}' ${CRULE_FILE}.tmp  > ${BD_CRULE_FILE}
cp ${CRULE_FILE}.tmp  ${BD_CRULE_FILE}
alert $? ${msg}

msg="����beidou�������ļ�MD5ʧ��"
md5sum ${BD_CRULE_FILE} > ${BD_CRULE_FILE_MD5}
alert $? ${msg}

msg="mv beidou������ʧ��"
mv ${BD_CRULE_FILE} ${BD_CRULE_FILE_MD5} ${BD_CRULE_PATH}
alert $? ${msg}



