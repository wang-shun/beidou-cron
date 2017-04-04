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
#����Ѿ����ڶ�Ӧ��list.md5�ļ�������Ϊ�Ѿ�ִ����ɣ�������ִ�С�
if [ -f ${COST_PATH}"/finish.${CURR_DATE}" ]; then
	exit 0;
fi

rm *

echo "ftp://"${COST_FTP_USER}":"${COST_FTP_PASSWORD}"@"${COST_FTP_SERVER}"/"${COST_FTP_PATH}"/dcharge.bd.log.list.md5"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.log.list.md5 -O dcharge.bd.log.list.md5


alert $? "Error:${COST_FTP_USER} download click log,�ļ�������"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.log.list -O dcharge.bd.log.list

alert $? "Error:${COST_FTP_USER} download dcahrge.bd.log.list,�ļ�������"

#ץȡ�ļ�

while read line ; do
	DEST_FILE=`echo ${line}`
	wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/${line}.md5 -O ${line}.md5
	alert $? "Error:${COST_FTP_USER} download ${line}.md5,ִ��wget����������"

	wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/${line} -O ${line}
	alert $? "Error:${COST_FTP_USER} download ${line},ִ��wget����������"

	md5sum -c ${line}".md5"
	alert $? ${line}"�ļ�md5У��ʧ��"
	
    awk -F "\t" '{row=$1;x=2;while(x<=17){row=row"\t"$x ;x++;}print row;}' ${COST_PATH}/${DEST_FILE} > ${COST_PATH}/${DEST_FILE}.tmp
	alert $? "awk�ļ�${COST_PATH}/${DEST_FILE} ʧ��"
    mv ${COST_PATH}/${DEST_FILE}.tmp ${COST_PATH}/${DEST_FILE}
	alert $? "mv�ļ�${COST_PATH}/${DEST_FILE}.tmp ${COST_PATH}/${DEST_FILE}ʧ��"

done < dcharge.bd.log.list

#ץȡ������patch�ļ�������merge��dcharge.bd.log.list��һ�����������ļ��У��������ɸÿ���MD5�ļ�
wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.day.${CURR_DATE}.log.md5 -O dcharge.bd.day.${CURR_DATE}.log.md5
alert $? "Error:${COST_FTP_USER} download dcharge.bd.day.${CURR_DATE}.log.md5,ִ��wget����������"

wget -t 3 -q ftp://${COST_FTP_USER}:${COST_FTP_PASSWORD}@${COST_FTP_SERVER}/${COST_FTP_PATH}/dcharge.bd.day.${CURR_DATE}.log -O dcharge.bd.day.${CURR_DATE}.log
alert $? "Error:${COST_FTP_USER} download dcharge.bd.day.${CURR_DATE}.log,ִ��wget����������"

MERGE_TARGET_FILE=`head -n1 dcharge.bd.log.list`
awk -F "\t" '{row=$1;x=2;while(x<=17){row=row"\t"$x ;x++;}print row;}' ${COST_PATH}/dcharge.bd.day.${CURR_DATE}.log >> ${COST_PATH}/${MERGE_TARGET_FILE}
alert $? "awk�ļ�${COST_PATH}/dcharge.bd.day.${CURR_DATE}.log,��merge���ļ�${COST_PATH}/${MERGE_TARGET_FILE} ʧ��"

md5sum $MERGE_TARGET_FILE > $MERGE_TARGET_FILE.md5
alert $? "md5sum${MERGE_TARGET_FILE}ʧ��"

#�������ݲ����ɽӿ��ļ�

cd ${BIN_PATH}
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.OutputShifenData >> ${LOG_FILE} 2>&1
alert $? "����Ʒ�ģ������ʧ��"
#����ÿ�����ݱ�

cd ${COST_PATH}
#����md5�ļ�

md5sum ${COST_CHARGE_FILE_NAME} > ${COST_CHARGE_FILE_NAME}".md5"
alert $? "md5sum${COST_CHARGE_FILE_NAME}ʧ��"

#regist file to dts
msg="regist DTS for ${OUTPUT_SHIFEN_BEIDOU_CHARGE} failed."
md5=`getMd5FileMd5 ${COST_PATH}/${COST_CHARGE_FILE_NAME}".md5"`
noahdt add ${OUTPUT_SHIFEN_BEIDOU_CHARGE} -i date=${CURR_DATE} -m md5=${md5} bscp://${COST_PATH}/${COST_CHARGE_FILE_NAME}
alert $? "${msg}"

md5sum ${COST_MA_FILE_NAME} > ${COST_MA_FILE_NAME}".md5"
alert $? "md5sum${COST_MA_FILE_NAME}ʧ��"
md5sum ${COST_UN_FILE_NAME} > ${COST_UN_FILE_NAME}".md5"
alert $? "md5sum${COST_UN_FILE_NAME}ʧ��"
	

#��������־
while read line ; do
	SRC_FILE=${line}
	runsql_clk "use beidoufinan; load data local infile '${COST_PATH}/${SRC_FILE}' into table cost_${CURR_DATE}"
	alert $? "����Ʒ������ļ�${SRC_FILE}�����ݿ��г������飡"
done < dcharge.bd.log.list

touch finish.${CURR_DATE}
