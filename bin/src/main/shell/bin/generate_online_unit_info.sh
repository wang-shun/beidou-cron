#!/bin/sh
#filename: generate_online_unit_info.sh
#@auther:  genglei
#@fuction: generate unit info into file from db
#@date:    2015-06-23
#@version: 1.0.0

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/online_unit_info.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=generate_online_unit_info.sh
reader_list=genglei01

mkdir -p ${ONLINE_UNIT_INFO_PATH}
mkdir -p ${UNIT_OUTPUT_PATH}
mkdir -p ${UNIT_INPUT_PATH}
mkdir -p ${UNIT_LOG_PATH}

LOG_NAME=generate_online_unit_info
LOG_INFO_FILE=${UNIT_LOG_PATH}/${LOG_NAME}.log

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $LOG_INFO_FILE
}

PRINT_LOG "begin to get all unit info from db"

TODAY_DATE=`date +%Y%m%d`

#get image/flash/icon unit info from db
UNIT_FILE=${UNIT_INFO_OUTPUT_FILE}.${TODAY_DATE}
SELECT_SQL="set names utf8; select o.id, o.userid, mc_id, mc_version_id, p.planid, p.planname, g.groupid, g.groupname, \
	wuliao_type, width, height, show_url, target_url, wireless_show_url, wireless_target_url, new_adtradeid, \
	tag_mask, o.modtime, o.am_template_id, o.title, o.description1, o.description2 \
	from beidou.online_unit o join beidou.cproplan p on o.planid=p.planid \
	join beidou.cprogroup g on o.groupid=g.groupid where wuliao_type in (2,3,5) and o.is_smart=0;"

for slice in `seq 0 7`; do
	db_file=${UNIT_FILE}.${slice}
	#execute sql
	PRINT_LOG "start to get image/flash/icon unit info from db, slice="$slice
	runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
	if [ $? -ne 0 ]; then
		msg="[ERROR]run sql {"${SELECT_SQL}"} failed"
		PRINT_LOG "$msg"
		alert 1 "$msg"
	fi
	PRINT_LOG "end to get image/flash/icon unit info from db, slice="$slice
done

PRINT_LOG "end to get all unit info from db"

cat ${UNIT_FILE}.[0-7] > ${UNIT_FILE}
rm -rf ${UNIT_FILE}.[0-7]
cd ${UNIT_OUTPUT_PATH}
md5sum ${UNIT_OUTPUT_FILE_NAME}.${TODAY_DATE} > ${UNIT_OUTPUT_FILE_NAME}.${TODAY_DATE}.md5

if [ $? -ne 0 ]; then
	msg="[ERROR]generate md5 for {"${UNIT_FILE}"} failed"
	PRINT_LOG "$msg"
	alert 1 "$msg"
fi

#get text unit info from db
SELECT_SQL="set names utf8; select o.id, o.userid, mc_id, mc_version_id, p.planid, p.planname, g.groupid, g.groupname, \
	wuliao_type, show_url, target_url, wireless_show_url, wireless_target_url, new_adtradeid, \
	tag_mask, o.modtime, o.title, o.description1, o.description2 \
	from beidou.online_unit o join beidou.cproplan p on o.planid=p.planid \
	join beidou.cprogroup g on o.groupid=g.groupid where wuliao_type=1 and o.is_smart=0;"
UNIT_FILE=${TEXT_UNIT_INFO_OUTPUT_FILE}.${TODAY_DATE}

for slice in `seq 0 7`; do
	db_file=${UNIT_FILE}.${slice}
	#execute sql
	PRINT_LOG "start to get text unit info from db, slice="$slice
	runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
	if [ $? -ne 0 ]; then
		msg="[ERROR]run sql {"${SELECT_SQL}"} failed"
		PRINT_LOG "$msg"
		alert 1 "$msg"
	fi
	PRINT_LOG "end to get text unit info from db, slice="$slice
done
cat ${UNIT_FILE}.[0-7] > ${UNIT_FILE}
rm -rf ${UNIT_FILE}.[0-7]

PRINT_LOG "end to get all unit info from db"

CURR_DATETIME=`date +%F\ %T`
PRINT_LOG "end normally"
