#!dou-db00.tc.baidu.com/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/ireginfo.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=ireginfo.sh
reader_list=zhangpeng

LOG_FILE=${LOG_PATH}/ireginfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${BIN_PATH}
mkdir -p ${LOG_PATHi}
mkdir -p ${DATA_PATH}
mkdir -p ${REGINFO_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

cd ${REGINFO_DATA_PATH}

# backup

if [ -f "${RGN_LOCAL_FILE}" ]; then
		cp ${RGN_LOCAL_FILE} ${RGN_LOCAL_FILE}${RGN_BACKUP_FILE_POSTFIX}
		rm ${RGN_LOCAL_FILE}
else
		touch ${RGN_LOCAL_FILE}${RGN_BACKUP_FILE_POSTFIX}
fi

if [ -f "${RGN_LOCAL_FILE_MD5}" ]; then
		cp ${RGN_LOCAL_FILE_MD5} ${RGN_LOCAL_FILE_MD5}${RGN_BACKUP_FILE_POSTFIX}
		rm ${RGN_LOCAL_FILE_MD5}
else
		touch ${RGN_LOCAL_FILE_MD5}${RGN_BACKUP_FILE_POSTFIX}
fi

			
wget -c -T 20 -t 5 ftp://${RGN_FTP_USERNAME}:${RGN_FTP_PASSWORD}@${RGN_SOURCE_SERVER}/${RGN_SOURCE_PATH}/${RGN_SOURCE_FILE} -O ${RGN_LOCAL_FILE}

if [ "$?" -ne "0" ]; then	
 	hit "从 ${RGN_SOURCE_SERVER} download ${RGN_SOURCE_FILE},执行wget命令有问题"
fi

wget -c -T 20 -t 5 ftp://${RGN_FTP_USERNAME}:${RGN_FTP_PASSWORD}@${RGN_SOURCE_SERVER}/${RGN_SOURCE_PATH}/${RGN_SOURCE_FILE_MD5} -O ${RGN_LOCAL_FILE_MD5}

if [ "$?" -ne "0" ]; then	
 	hit "从 ${RGN_SOURCE_SERVER} download ${RGN_SOURCE_FILE_MD5},执行wget命令有问题"
fi

#check md5
LOCAL_MD5SUM=`md5sum ${RGN_LOCAL_FILE} | awk '{if(NR == 1){print $1}}'`
SOURCE_MD5SUM=`awk '{if(NR == 1){print $1}}' ${RGN_LOCAL_FILE_MD5}`

if [ "${LOCAL_MD5SUM}" != "${SOURCE_MD5SUM}" ]; then
	hit "从 ${RGN_SOURCE_SERVER} 下载的 ${RGN_LOCAL_FILE}、${RGN_SOURCE_FILE_MD5},md5值不一致"
fi

#check with yesterday data
if [ -f "${RGN_LOCAL_FILE_MD5}${RGN_BACKUP_FILE_POSTFIX}" ]; then
		BACKUP_MD5SUM=`awk '{if(NR == 1){print $1}}' ${RGN_LOCAL_FILE_MD5}${RGN_BACKUP_FILE_POSTFIX}`
		
		if [ "${BACKUP_MD5SUM}" == "${SOURCE_MD5SUM}" ]; then
			echo "${RGN_LOCAL_FILE} 和昨天下载的文件无差别，不再导入库" >> ${LOG_FILE}
			exit 0
		fi				
fi
#prim key check
REPEAT_KEYS=`awk '{print $2,$3}' "${RGN_LOCAL_FILE}" | uniq -d`
if [ -n "${REPEAT_KEYS}" ]; then
        hit "${RGN_LOCAL_FILE},一级地域ID、二级地域ID值存在重复"
fi
#check first level region id exist
FIRST_CLASS_USING=`awk '{print $2}' "${RGN_SOURCE_FILE}" | sort -un`
FIRST_CLASS=`awk '{if($3 == 0){print $2}}' "${RGN_SOURCE_FILE}" | sort -un`

if [ "${FIRST_CLASS_USING}" != "${FIRST_CLASS}" ]; then
        hit "${RGN_LOCAL_FILE},某个一级地域ID值不存在"
fi

awk -F'\t' '{printf("%s\t%s\t%s\t%s\n",$2,$3,$1,$4)}'  ${RGN_LOCAL_FILE} > ${RGN_LOCAL_FILE}.tmp

iconv -f "gbk" -t "utf8" ${RGN_LOCAL_FILE}.tmp > ${RGN_LOCAL_FILE}.normal

DELETE_SQL="truncate table beidoucap.reginfo"

runsql_cap "${DELETE_SQL}"

if [ "$?" -ne "0" ]; then	
 	hit "执行 ${DELETE_SQL} 命令有问题"
fi


LOAD_SQL="load data local infile '${REGINFO_DATA_PATH}/${RGN_LOCAL_FILE}.normal' into table beidoucap.reginfo"

runsql_cap "${LOAD_SQL}"

if [ "$?" -ne "0" ]; then	
 	hit "执行 地域信息数据导入 命令失败"
fi


echo "地域信息数据数导入成功" >> ${LOG_FILE}

exit 0
