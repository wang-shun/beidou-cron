#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/similarPeople.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=batchOpenSimilarPeople.sh
reader_list=wangyu45

LOG_FILE=${LOG_PATH}/batchOpenSimilarPeople.log
CURR_DATETIME=`date +%Y%m%d%H`
DATA_PATH=${OPEN_SIMILAR_PEOPLE_PATH}

mkdir -p ${DATA_PATH}
mkdir -p ${LOG_PATH}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

##抓取相似人群文件
wget -q -t$MAX_RETRY ${OPEN_SIMILAR_PEOPLE_URL}${CURR_DATETIME} -O ${CURR_DATETIME}
if [ ! -s "${CURR_DATETIME}" ]
then
	rm -rf ${CURR_DATETIME}
	exit 0
fi
INF "Have similar people open task"


##抓取相似人群md5文件
wget -q -t$MAX_RETRY ${OPEN_SIMILAR_PEOPLE_URL}${CURR_DATETIME}.md5 -O ${CURR_DATETIME}.md5
if [ ! -s "${CURR_DATETIME}" ]
then
	rm -rf ${CURR_DATETIME}
	msg="similar people ${CURR_DATETIME}.md5 is empty"
	alert 1 "${msg}"
fi

msg="${CURR_DATETIME}文件的md5校验失败"
md5sum -c ${CURR_DATETIME}.md5
alert $? "${msg}"

INF ${DATA_PATH}${CURR_DATETIME}
java -Xms4096m -Xmx8192m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprogroup.BatchOpenSimilarPeople ${DATA_PATH}${CURR_DATETIME} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf