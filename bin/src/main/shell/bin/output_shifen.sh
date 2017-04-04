#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/output_shifen.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=output_shifen.sh
reader_list=zengyunfeng

if [ ! -f ${COST_PATH} ]; then
	mkdir -p ${COST_PATH}
fi

cd ${COST_PATH}
#如果已经存在对应的list.md5文件，则认为已经执行完成，不继续执行。
if [ -f ${COST_PATH}"/finish.${CURR_DATE}" ]; then
	exit 0;
fi

rm *

echo "ftp://"${COST_FTP_USER}":"${COST_FTP_PASSWORD}"@"${COST_FTP_SERVER}"/"${COST_FTP_PATH}"/dcharge.bd.log.list.md5"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.log.list.md5 -O dcharge.bd.log.list.md5


alert $? "Error:${COST_FTP_USER} download click log,文件不存在"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.log.list -O dcharge.bd.log.list

alert $? "Error:${COST_FTP_USER} download dcahrge.bd.log.list,文件不存在"

#抓取文件

while read line ; do
	DEST_FILE=`echo ${line}`
	wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/${line}.md5 -O ${line}.md5
	alert $? "Error:${COST_FTP_USER} download ${line}.md5,执行wget命令有问题"

	wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/${line} -O ${line}
	alert $? "Error:${COST_FTP_USER} download ${line},执行wget命令有问题"

	md5sum -c ${line}".md5"
	alert $? ${line}"文件md5校验失败"
	
    awk -F "\t" '{row=$1;x=2;while(x<=17){row=row"\t"$x ;x++;}print row;}' ${COST_PATH}/${DEST_FILE} > ${COST_PATH}/${DEST_FILE}.tmp
	alert $? "awk文件${COST_PATH}/${DEST_FILE} 失败"
    mv ${COST_PATH}/${DEST_FILE}.tmp ${COST_PATH}/${DEST_FILE}
	alert $? "mv文件${COST_PATH}/${DEST_FILE}.tmp ${COST_PATH}/${DEST_FILE}失败"

done < dcharge.bd.log.list

#抓取天粒度patch文件，并且merge到dcharge.bd.log.list第一个刻钟粒度文件中，重新生成该刻钟MD5文件
wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.day.${CURR_DATE}.log.md5 -O dcharge.bd.day.${CURR_DATE}.log.md5
alert $? "Error:${COST_FTP_USER} download dcharge.bd.day.${CURR_DATE}.log.md5,执行wget命令有问题"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.day.${CURR_DATE}.log -O dcharge.bd.day.${CURR_DATE}.log
alert $? "Error:${COST_FTP_USER} download dcharge.bd.day.${CURR_DATE}.log,执行wget命令有问题"

MERGE_TARGET_FILE=`head -n1 dcharge.bd.log.list`
awk -F "\t" '{row=$1;x=2;while(x<=17){row=row"\t"$x ;x++;}print row;}' ${COST_PATH}/dcharge.bd.day.${CURR_DATE}.log >> ${COST_PATH}/${MERGE_TARGET_FILE}
alert $? "awk文件${COST_PATH}/dcharge.bd.day.${CURR_DATE}.log,并merge到文件${COST_PATH}/${MERGE_TARGET_FILE} 失败"

md5sum $MERGE_TARGET_FILE > $MERGE_TARGET_FILE.md5
alert $? "md5sum${MERGE_TARGET_FILE}失败"

#计算数据并生成接口文件

cd ${BIN_PATH}
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.OutputShifenData >> ${LOG_FILE} 2>&1
alert $? "导入计费模块数据失败"
#创建每天数据表

cd ${COST_PATH}
#生成md5文件

md5sum ${COST_CHARGE_FILE_NAME} > ${COST_CHARGE_FILE_NAME}".md5"
alert $? "md5sum${COST_CHARGE_FILE_NAME}失败"

#regist file to dts
msg="regist DTS for ${OUTPUT_SHIFEN_BEIDOU_CHARGE} failed."
md5=`getMd5FileMd5 ${COST_PATH}/${COST_CHARGE_FILE_NAME}".md5"`
noahdt add ${OUTPUT_SHIFEN_BEIDOU_CHARGE} -i date=${CURR_DATE} -m md5=${md5} bscp://${COST_PATH}/${COST_CHARGE_FILE_NAME}
alert $? "${msg}"

md5sum ${COST_MA_FILE_NAME} > ${COST_MA_FILE_NAME}".md5"
alert $? "md5sum${COST_MA_FILE_NAME}失败"
md5sum ${COST_UN_FILE_NAME} > ${COST_UN_FILE_NAME}".md5"
alert $? "md5sum${COST_UN_FILE_NAME}失败"
	

#导入点击日志
while read line ; do
	SRC_FILE=${line}
	runsql_clk "use beidoufinan; load data local infile '${COST_PATH}/${SRC_FILE}' into table cost_${CURR_DATE}"
	alert $? "插入计费数据文件${SRC_FILE}到数据库中出错，请检查！"
done < dcharge.bd.log.list

touch finish.${CURR_DATE}
