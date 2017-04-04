#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/planOffline.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importPlanOffline.sh
reader_list=chenlu

LOG_FILE=${LOG_PATH}/importPlanOffline.log


date=""
timestamp=""

if [ $# -ge 2 ]; then
   date=$1
   timestamp=$2
else
   echo "no timestamp in parameter" >> ${LOG_FILE}
   exit 1
fi

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${PLAN_OFFLINE_DATA_PATH}/$date

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${PLAN_OFFLINE_DATA_PATH}失败"
cd ${PLAN_OFFLINE_DATA_PATH}/$date
alert $? ${msg}

#抓取文件
filename="bdbudget."$date"-"$timestamp".log"

if [ -f $filename ]; then
   rm $filename
fi

msg="获取${PLAN_OFFLINE_URL}/${filename}失败"

wget -t 3 -q ${PLAN_OFFLINE_URL}/$date/$filename

alert $? ${msg}

if [ -f "${PLAN_OFFLINE_DATA_PATH}/$date/$filename" ] 
then
    echo "import "$filename >> ${LOG_FILE}

    if [ -f ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning ]
    then
	rm ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
    fi

    msg="删除3个月前旧数据错误.本周期文件已删除，下个周期自动回溯"

    #删除3月以前的数据
    MON_AGO=`date "+%Y-%m-%d %H:%M:%S" -d '91 days ago'`

	runsql_cap "use beidoucap; delete from cproplan_offline where offtime < '${MON_AGO}'; show warnings\G; show errors\G;" >> ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
	if [ $? -ne 0 ]; then
            msg_recover="删除${filename}失败，请手动恢复"
            echo "failed so remove "$filename >> ${LOG_FILE}
            rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
            alert $? ${msg_recover}
            alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" delete history data done" >> ${LOG_FILE}
    
    msg="查询cproplan表中planid->userid的关系错误.本周期文件已删除，下个周期自动回溯"

	#从主库内读取全部cproplan表中planid->userid的关系，原因为避免同步延迟

    runsql_sharding_read "use beidou; select planid, userid from cproplan where [userid];" ${PLAN_OFFLINE_DATA_PATH}/temp/cproplan.log

    if [ $? -ne 0 ]; then
        msg_recover="删除${filename}失败，请手动恢复"
        echo "failed so remove "$filename >> ${LOG_FILE}
        rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
        alert $? ${msg_recover}
        alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" select planid and userid done" >> ${LOG_FILE}
                    
 	msg="生成bdbudget.log文件错误.本周期文件已删除，下个周期自动回溯"	

	#add userid to offline data
	awk 'ARGIND==1{map[$1]=$2}ARGIND==2{if($1 in map){print $0"\t"map[$1];}else{error[$1];print $0"\t0";}}END{for(x in error){print x >> "planNotFound.txt";}}' ${PLAN_OFFLINE_DATA_PATH}/temp/cproplan.log ${PLAN_OFFLINE_DATA_PATH}/$date/$filename > ${PLAN_OFFLINE_DATA_PATH}/temp/bdbudget.log


    if [ $? -ne 0 ]; then
            msg_recover="删除${filename}失败，请手动恢复"
            echo "failed so remove "$filename >> ${LOG_FILE}
            rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
            alert $? ${msg_recover}
            alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" generate bdbudget.log done" >> ${LOG_FILE}

	msg="加载bdbudget.log文件内容到cproplan_offline表错误.本周期文件已删除，下个周期自动回溯"

    #load offline data
	runsql_cap "use beidoucap; load data local infile '${PLAN_OFFLINE_DATA_PATH}/temp/bdbudget.log' into table cproplan_offline (planid, consume, budget, offtime, userid); show warnings\G; show errors\G;"  >> ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
    if [ $? -ne 0 ]; then
            msg_recover="删除${filename}失败，请手动恢复"
            echo "failed so remove "$filename >> ${LOG_FILE}
            rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
            alert $? ${msg_recover}
            alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" load data done" >> ${LOG_FILE}

    if [ -s "${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning" ]
    then
        msg=`cat ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning`
    	alert 1 ${PLAN_OFFLINE_DATA_PATH}/$date/$filename":\n import plan offline fail(3): $msg\n"
    fi
else
    if [ $((`printf "%d" $((10#${timestamp:0:2}))`+1)) -lt "`date +%H`" ]; then
	alert 1 "${PLAN_OFFLINE_DATA_PATH}/$date/${filename}长时间没有抓取成功"
    fi
fi

