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

msg="��������Ŀ¼${PLAN_OFFLINE_DATA_PATH}ʧ��"
cd ${PLAN_OFFLINE_DATA_PATH}/$date
alert $? ${msg}

#ץȡ�ļ�
filename="bdbudget."$date"-"$timestamp".log"

if [ -f $filename ]; then
   rm $filename
fi

msg="��ȡ${PLAN_OFFLINE_URL}/${filename}ʧ��"

wget -t 3 -q ${PLAN_OFFLINE_URL}/$date/$filename

alert $? ${msg}

if [ -f "${PLAN_OFFLINE_DATA_PATH}/$date/$filename" ] 
then
    echo "import "$filename >> ${LOG_FILE}

    if [ -f ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning ]
    then
	rm ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
    fi

    msg="ɾ��3����ǰ�����ݴ���.�������ļ���ɾ�����¸������Զ�����"

    #ɾ��3����ǰ������
    MON_AGO=`date "+%Y-%m-%d %H:%M:%S" -d '91 days ago'`

	runsql_cap "use beidoucap; delete from cproplan_offline where offtime < '${MON_AGO}'; show warnings\G; show errors\G;" >> ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
	if [ $? -ne 0 ]; then
            msg_recover="ɾ��${filename}ʧ�ܣ����ֶ��ָ�"
            echo "failed so remove "$filename >> ${LOG_FILE}
            rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
            alert $? ${msg_recover}
            alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" delete history data done" >> ${LOG_FILE}
    
    msg="��ѯcproplan����planid->userid�Ĺ�ϵ����.�������ļ���ɾ�����¸������Զ�����"

	#�������ڶ�ȡȫ��cproplan����planid->userid�Ĺ�ϵ��ԭ��Ϊ����ͬ���ӳ�

    runsql_sharding_read "use beidou; select planid, userid from cproplan where [userid];" ${PLAN_OFFLINE_DATA_PATH}/temp/cproplan.log

    if [ $? -ne 0 ]; then
        msg_recover="ɾ��${filename}ʧ�ܣ����ֶ��ָ�"
        echo "failed so remove "$filename >> ${LOG_FILE}
        rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
        alert $? ${msg_recover}
        alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" select planid and userid done" >> ${LOG_FILE}
                    
 	msg="����bdbudget.log�ļ�����.�������ļ���ɾ�����¸������Զ�����"	

	#add userid to offline data
	awk 'ARGIND==1{map[$1]=$2}ARGIND==2{if($1 in map){print $0"\t"map[$1];}else{error[$1];print $0"\t0";}}END{for(x in error){print x >> "planNotFound.txt";}}' ${PLAN_OFFLINE_DATA_PATH}/temp/cproplan.log ${PLAN_OFFLINE_DATA_PATH}/$date/$filename > ${PLAN_OFFLINE_DATA_PATH}/temp/bdbudget.log


    if [ $? -ne 0 ]; then
            msg_recover="ɾ��${filename}ʧ�ܣ����ֶ��ָ�"
            echo "failed so remove "$filename >> ${LOG_FILE}
            rm -f ${PLAN_OFFLINE_DATA_PATH}/$date/$filename
            alert $? ${msg_recover}
            alert 1 "${msg}"
    fi
    echo `date +%F\ %T`" generate bdbudget.log done" >> ${LOG_FILE}

	msg="����bdbudget.log�ļ����ݵ�cproplan_offline�����.�������ļ���ɾ�����¸������Զ�����"

    #load offline data
	runsql_cap "use beidoucap; load data local infile '${PLAN_OFFLINE_DATA_PATH}/temp/bdbudget.log' into table cproplan_offline (planid, consume, budget, offtime, userid); show warnings\G; show errors\G;"  >> ${PLAN_OFFLINE_DATA_PATH}/$date/offline.warning
    if [ $? -ne 0 ]; then
            msg_recover="ɾ��${filename}ʧ�ܣ����ֶ��ָ�"
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
	alert 1 "${PLAN_OFFLINE_DATA_PATH}/$date/${filename}��ʱ��û��ץȡ�ɹ�"
    fi
fi

