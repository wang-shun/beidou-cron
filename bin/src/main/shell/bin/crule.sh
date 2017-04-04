#!/bin/sh
#同步
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

msg="进入工作目录${ROOT_PATH}失败"
cd ${ROOT_PATH}
alert $? ${msg}


CONF_SH=${CRULE_FILE}
[ -f "${CONF_SH}" ] && rm $CONF_SH 
CONF_SH=${CRULE_FILE_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 



#抓取凤巢的黑名单文件，如果某个文件或者md5不存在，则直接给出报警，不继续执行，如果md5校验不通过，也停止
msg="下载凤巢黑名单的md5文件失败！"
wget -q ftp://${CRULE_USER}:${CRULE_PASSWORD}@${CRULE_SERVER}/${CRULE_PATH}/${CRULE_FILE_MD5}
alert $? ${msg}

msg="下载凤巢黑名单文件失败！"
wget -q ftp://${CRULE_USER}:${CRULE_PASSWORD}@${CRULE_SERVER}/${CRULE_PATH}/${CRULE_FILE}
alert $? ${msg}

msg="凤巢黑名单文件的md5校验失败"
md5sum -c ${CRULE_FILE_MD5}
alert $? ${msg}

msg="凤巢黑名单文件更名失败"
mv ${CRULE_FILE} ${CRULE_FILE}.tmp
alert $? ${msg}

msg="生成beidou黑名单文件失败"
#awk '$4==1 {print $0}' ${CRULE_FILE}.tmp  > ${BD_CRULE_FILE}
cp ${CRULE_FILE}.tmp  ${BD_CRULE_FILE}
alert $? ${msg}

msg="生成beidou黑名单文件MD5失败"
md5sum ${BD_CRULE_FILE} > ${BD_CRULE_FILE_MD5}
alert $? ${msg}

msg="mv beidou黑名单失败"
mv ${BD_CRULE_FILE} ${BD_CRULE_FILE_MD5} ${BD_CRULE_PATH}
alert $? ${msg}



