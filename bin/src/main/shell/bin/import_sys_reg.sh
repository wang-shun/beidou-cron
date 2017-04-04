#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/sys_reg.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=import_sys_reg.sh
reader_list=wangyu45

LOG_FILE=${LOG_PATH}/import_sys_reg.log

mkdir -p ${ROOT_PATH}
mkdir -p ${BIN_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${SYS_REG_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

cd ${SYS_REG_DATA_PATH}

if [ -f "${PROV_FILE}" ]; then
		rm ${PROV_FILE}
fi

if [ -f "${PROV_FILE}${MD5_POSTFIX}" ]; then
		rm ${PROV_FILE}${MD5_POSTFIX}
fi

if [ -f "${CITY_FILE}" ]; then
		rm ${CITY_FILE}
fi

if [ -f "${CITY_FILE}${MD5_POSTFIX}" ]; then
		rm ${CITY_FILE}${MD5_POSTFIX}
fi

#抓取文件
msg="wget文件${PROV_FILE}失败"
wget -q -t 5 ${SYS_URL}${PROV_FILE} -O ${PROV_FILE}
alert $? "${msg}"

msg="wget文件${PROV_FILE}${MD5_POSTFIX}失败"
wget -q -t 5 ${SYS_URL}${PROV_FILE}${MD5_POSTFIX} -O ${PROV_FILE}${MD5_POSTFIX}
alert $? "${msg}"

msg="${PROV_FILE}文件的md5校验失败"
md5sum -c ${PROV_FILE}.md5
alert $? "${msg}"

msg="wget文件${CITY_FILE}失败"
wget -q -t 5 ${SYS_URL}${CITY_FILE} -O ${CITY_FILE}
alert $? "${msg}"

msg="wget文件${CITY_FILE}${MD5_POSTFIX}失败"
wget -q -t 5 ${SYS_URL}${CITY_FILE}${MD5_POSTFIX} -O ${CITY_FILE}${MD5_POSTFIX}
alert $? "${msg}"

msg="${CITY_FILE}文件的md5校验失败"
md5sum -c ${CITY_FILE}.md5
alert $? "${msg}"

if [ -f "${PROV_CITY_FILE}" ]; then
		cp ${PROV_CITY_FILE} ${PROV_CITY_FILE}${BACKUP_POSTFIX}
		rm ${PROV_CITY_FILE}
else
		touch ${PROV_CITY_FILE}${BACKUP_POSTFIX}
fi

#拷贝省份ID
awk -F'\t' '{printf("%s\t%s\t%s\t%s\n",$1,'0','1',$2)}'  ${PROV_FILE} > ${PROV_CITY_FILE}${TMP_POSTFIX}

#追加城市ID
awk -F'\t' '{printf("%s\t%s\t%s\t%s\n",$2,$1,'1',$3)}'  ${CITY_FILE} >> ${PROV_CITY_FILE}${TMP_POSTFIX}

iconv -f "gbk" -t "utf8" ${PROV_CITY_FILE}${TMP_POSTFIX} > ${PROV_CITY_FILE}

DELETE_SQL="delete from beidoucap.sysreginfo"

runsql_cap "${DELETE_SQL}"

if [ "$?" -ne "0" ]; then	
 	hit "执行 ${DELETE_SQL} 命令有问题"
fi


LOAD_SQL="load data local infile '${SYS_REG_DATA_PATH}/${PROV_CITY_FILE}' into table beidoucap.sysreginfo(firstregid,secondregid,regtype,regname)"

runsql_cap "${LOAD_SQL}"

if [ "$?" -ne "0" ]; then	
 	hit "执行 SYS地域信息数据导入 命令失败"
fi

echo "SYS地域信息数据数导入成功" >> ${LOG_FILE}

exit 0