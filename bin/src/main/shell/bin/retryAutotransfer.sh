#!/bin/sh
# 
# code migration
# @author zhangpingan
# @version 1.0.0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/retryAutotransfer.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "



mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=retryAutotransfer.sh
reader_list=zhangpingan
LOG_FILE=${LOG_PATH}/retryAutotransfer.log

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}

    cd ${DATA_PATH}
    #ץȡ�������ķﳲ����ļ�
	msg="�Ӳ�������ץȡ�ﳲ����ļ�ʧ��"
	retryCount=0
    sucFlag=0
    while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
    do
	   retryCount=$(($retryCount+1))
       rm -f ${FC_BALANCE_NAME}
	   wget -t 3 -q --limit-rate=30M ftp://${FC_BALANCE_SERVER}/${FC_BALANCE_PATH}/${FC_BALANCE_NAME}
	   rm -f ${FC_BALANCE_NAME}.md5
	   wget -t 3 -q --limit-rate=30M ftp://${FC_BALANCE_SERVER}/${FC_BALANCE_PATH}/${FC_BALANCE_NAME}.md5
	   md5sum -c ${FC_BALANCE_NAME}.md5
       if [ $? -eq 0 ]
       then
         sucFlag=1
       else
         sleep 5
       fi
    done
	if [ $sucFlag -eq 0 ]
    then
	   PRINT_LOG "${msg}"
       alert 1 "${msg}"
    fi
	
	msg="���������˻��û�����ļ�ʧ��"
    awk '{printf("%s\t%s\n",$1,$5)}' ${FC_BALANCE_NAME} > ${FC_BALANCE_FILE}
    alert $? "${msg}"

	#��autotransfer����ɾ���Ѿ������Զ�ת�˵��û�
	msg="��db��ɾ���Ѿ����Զ�ת���û��б�ʧ��"
	runsql_xdb "delete from beidouext.autotransfer where userid not in (select userid from beidouext.userfundperday where transfertype=1);"
    alert $? "${msg}"
	
	sleep 10
	
	#��db�в��ҵ�ǰת��ʧ�ܵ���Ч�û��б�
	msg="��db�в��ҵ�ǰת��ʧ�ܵ���Ч�û��б�ʧ��"
	
	runsql_cap_read "select userid from beidoucap.useraccount where ustate=0 and ushifenstatid in (2,3,6)"  ${DATA_PATH}/userid.tmp
	alert $? "��db�в�����Ч�û��б�ʧ��"
	
	runsql_xdb_read "select userid, fund from beidouext.userfundperday where transfertype =1 and userid in (select userid from beidouext.autotransfer where is_success=1)"   ${DATA_PATH}/transfer.tmp
	alert $? "��db�в����û�ת����Ϣʧ��"
	
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){print $0}}' ${DATA_PATH}/userid.tmp  ${DATA_PATH}/transfer.tmp > ${USER_RETRY_LIST}.tmp
	
	# ֻ��һվʽKAС�����û�������
	#ONE_STATION_FILE=${DATA_PATH}/oneStation.exp
	#if [ -f "${ONE_STATION_FILE}" ] ; then
	#	awk -F'\t' 'ARGIND==1{map[$1]} ARGIND==2{if($1 in map){print $1}}' ${ONE_STATION_FILE} ${USER_RETRY_LIST}.tmp > retryAutotransfer.tmp.exp 
	#	mv  retryAutotransfer.tmp.exp ${USER_RETRY_LIST}.tmp
	#fi
	
	#�Ƚ�${USER_RETRY_LIST}.tmp��${FC_BALANCE_FILE}��ɸѡ�����ο��Խ���ת�˵��û��б�
	awk 'ARGIND==1{
	    USER_BALANCE[$1]=$2;
	} ARGIND==2{
	    if(($1 in USER_BALANCE) && (100*USER_BALANCE[$1] > $2)){
		    printf("%s\n",$0);
		}
	}' ${FC_BALANCE_FILE} ${USER_RETRY_LIST}.tmp | sort -k1n -u > ${USER_RETRY_LIST}
	
	
	msg="${USER_RETRY_LIST}�ļ�������"
	if [ ! -f ${USER_RETRY_LIST} ]; then 
     PRINT_LOG "${USER_RETRY_LIST}�ļ�������"
	 alert 1 "${msg}"
    fi;
	#���${USER_RETRY_LIST}����Ϊ0�����������´���
	lines=`cat ${USER_RETRY_LIST} | wc -l`
	PRINT_LOG "User Counts Need To ReAutoTransfer is ${lines}";
	
	if [ ${lines} -eq 0 ]
	then
	  PRINT_LOG "Directly Exit"
	  exit 0
	fi
	
    msg="ÿ���Զ�ת�˲���������ʧ�ܣ���׷��"
    java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.RetryAutotransferPerDay  &> ${LOG_FILE}
    alert $? "${msg}"


