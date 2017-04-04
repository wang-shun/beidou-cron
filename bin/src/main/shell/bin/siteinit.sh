#!/bin/sh
# This program is used to synchonize the bulletin file.

CURR_DATETIME=`date +%F\ %T`
#modify 1
ROOT_PATH=/home/zengyf/work/app/ecom/cpweb/beidou-cron/output
#ROOT_PATH=/home/work/beidou-cron
LOG_PATH=${ROOT_PATH}/logs
LOG_FILE=${LOG_PATH}/importbdsite.log
DATA_PATH=${ROOT_PATH}/data
BEIDOU_DATA_PATH=${ROOT_PATH}/unionsite
STATFILE_PRI=beidousitestat. 
IPSTATFILE_PRI=beidousiteinfo. 
STARTDATE=2009-01-01 
ENDDATE=2009-01-03
STATFILE=${STATFILE_PRI}`date -d 'yesterday' +%Y%m%d` 


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}


type="ERROR"
module=beidou-cron
program=siteinit.sh
reader_list=zengyunfeng

alert() {

	if [ $# -lt 2 ]
	then
		return
	fi
	ifError $1 "[${type}][${module}]$2@${CURR_DATETIME}" "${program}" \
			"$2" "${reader_list}"
	
}

echo $CURR_DATETIME >> ${LOG_FILE}

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=classpath.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

statDate=`echo $(date -d "${STARTDATE}" "+%s") $(date -d "${ENDDATE}" "+%s")| awk '{for(j=$1;j<=$2;j+=24*3600){print strftime("%Y%m%d",j)}}'`

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? ${msg}

for curDate in ${statDate}
do	
	mv ${IPSTATFILE_PRI}${curDate} ${STATFILE_PRI}${curDate}.ipcookie
done

STATFILE=`echo $(date -d "${STARTDATE}" "+%s") $(date -d "${ENDDATE}" "+%s")| awk 'BEGIN{ORS=","} {for(j=$1;j<=$2;j+=24*3600){print "data/beidousitestat."strftime("%Y%m%d",j)}}'`
echo ${STATFILE}

msg="进入工作目录${ROOT_PATH}失败"
cd ${ROOT_PATH}
alert $? ${msg}
# Generate the html bulletin from cpmis.notice_info 
# after the date which stored in beidou.sysnvtab and whose name is MSG_LAST_TIME.
# The html bulletin is in ./message/ directory.
msg="导入beidou站点发生异常,请使用恢复脚本进行恢复"
java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.SiteRecover -q ${STATFILE}>> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? ${msg}

