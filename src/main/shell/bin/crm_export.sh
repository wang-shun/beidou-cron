#!/bin/bash

#@file: crm_export.sh
#@author: lingbing
#@date: 2011-10-17
#@version: 1.0.0.0
#@brief: export data for crm

#$1:type [1|2|4|8]
#$2:date [yyyyMMdd]

#set DATE
DATE=$2

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/crm_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LOG_FILE=${LOG_PATH}/crm_export.log

program=crm_export.sh
reader_list=lingbing

if [ $? -ne 0 ]
then
        echo "Conf error: Fail to load libfile[$LIB_FILE]!"
        exit 1
fi

function check_path()
{
        if ! [ -w $EXPORT_PATH ]
        then
                if ! [ -e $EXPORT_PATH ]
                then
                        mkdir -p $EXPORT_PATH
                        if [ $? -ne 0 ]
                        then
                                log "FATAL" "Fail to mkdir [$EXPORT_PATH]!"
                                return 1
                        fi
                else
                        log "FATAL" "Path[$EXPORT_PATH] is not writable!"
                        return 1
                fi
        fi

        if ! [ -w $TMP_PATH ]
        then
                if ! [ -e $TMP_PATH ]
                then
                        mkdir -p $TMP_PATH
                        if [ $? -ne 0 ]
                        then
                                log "FATAL" "Fail to mkdir [$TMP_PATH]!"
                                return 1
                        fi
                else
                        log "FATAL" "Path[$TMP_PATH] is not writable!"
                        return 1
                fi
        fi
        return 0
}


#$1:bit number
#$2:bit index
#brief:return 1 if the index of bit number is 1
function check_flag(){
	flag=1
	if [ $2 -ne 0 ];then
		let "flag<<=$2"
	fi

	tmp=$flag	
	let "flag&=$1"
	if [ $flag -eq $tmp ];then
		return 1
	fi

	return 0
}

#$1:operation
#$2:message
#$3:retry count
function op_with_retry(){
    operation=$1
    message=$2
    if [ -z "$operation" ];then
        alert 1 "operation cannot be empty"
    fi
    if [ -z "$message" ];then
        alert 1 "message cannot be empty"
    fi
    retry=$3
    if [ -z $retry ];then
        retry=3
    fi

    cnt=0
    while [[ $cnt -lt $retry ]];do
        cnt=$(($cnt+1))
        log "TRACE" "this is $cnt time to do $operation"
        $operation
        if [ $? -eq 0 ];then
            return 0
        fi
        sleep 1
    done

    alert 1 "$message"
}

function export_userbudget(){
	cd ${EXPORT_PATH}
    if [ $? - ne 0 ];then
        log "ERROR" "cd to ${EXPORT_PATH} failed"
        return 1
    fi

	yesterday=`date +%Y%m%d -d"1 days ago"`
	TABLE_NAME="day_final_budget"

	if [ $yesterday -ne $DATE ];then
		TABLE_NAME="day_final_budget_$DATE"
	fi
    
	runsql_xdb_read "select userid, finalbudget, '${DATE_FIELD}' from beidouext.${TABLE_NAME}"  "${USERBUDGET}"
    if [ $? -ne 0 ];then
        log "ERROR" "export userbudget failed"
        return 1
    fi

	md5sum ${USERBUDGET} > ${USERBUDGET}.md5
    if [ $? -ne 0 ];then
        log "ERROR" "generate userbudget md5 failed"
        return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${CRM_EXPORT_USER_BUDGET} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${USERBUDGET}.md5`
	noahdt add ${CRM_EXPORT_USER_BUDGET} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${USERBUDGET}
	alert $? "${msg}"
}

function export_cost(){
    cd ${TMP_PATH}
    if [ $? -ne 0 ];then
        log "ERROR" "cd to ${TMP_PATH} failed"
        return 1
    fi

    TABLE_NAME="cost_${DATE}"
    runsql_clk_read "use beidoufinan; select userid, sum(price), sum(price*rrate), cmatch, provid, count(1), '${DATE_FIELD}' from ${TABLE_NAME} group by userid, cmatch, provid" "${COST_TEMP}"
    if [ $? -ne 0 ];then
        log "ERROR" "export cost sql failed"
        return 1
    fi
    
    cd ${EXPORT_PATH}
    if [ $? -ne  0 ];then
        log "ERROR" "cd to ${EXPORT_PATH} failed"
        return 1
    fi


	awk -F"\t" '{printf "%d\t%2.2f\t%2.2f\t%d\t%d\t%d\t%s\n",$1,$2,$3,$4,$5,$6,$7}' ${TMP_PATH}/${COST_TEMP} > ${COST}
    if [ $? -ne  0 ];then
        log "ERROR" "export cost awk failed"
        return 1
    fi

	md5sum ${COST} > ${COST}.md5
    if [ $? -ne 0 ];then
        log "ERROR" "generate cost md5 failed"
        return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${CRM_EXPORT_COST} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${COST}.md5`
	noahdt add ${CRM_EXPORT_COST} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${COST}
	alert $? "${msg}"
}

function export_useroffline(){
	rm ${TMP_PATH}/*

	wget -q ${USER_OFFLINE_FTP} -P ${TMP_PATH} --limit-rate=${USER_OFFLINE_FTP_RATE}
    if [ $? -ne 0 ];then
        log "ERROR" "wget balance files from drd failed"
        return 1
    fi

    cd ${EXPORT_PATH}
    if [ $? -ne 0 ];then
        log "ERROR" "cd to ${EXPORT_PATH} failed"
        return 1
    fi

	cat ${TMP_PATH}/* | sort -u > ${USER_OFFLINE}
    if [ $? -ne 0 ];then
        log "ERROR" "export useroffline failed"
        return 1
    fi

	md5sum ${USER_OFFLINE} > ${USER_OFFLINE}.md5
    if [ $? -ne 0 ];then
        log "ERROR" "generate useroffline md5 failed"
        return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${CRM_EXPORT_USER_OFFLINE} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${USER_OFFLINE}.md5`
	noahdt add ${CRM_EXPORT_USER_OFFLINE} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${USER_OFFLINE}
	alert $? "${msg}"
}

function export_planoffline(){
	cd ${EXPORT_PATH}
    if [ $? -ne 0 ];then
        log "ERROR" "cd to ${EXPORT_PATH} failed"
        return 1
    fi

	START_DATE=${DATE}
	END_DATE=`date +%Y%m%d -d"1 days ${DATE}"`
	
	runsql_cap_read "select planid, userid, offtime from beidoucap.cproplan_offline where offtime>='${START_DATE}' and offtime<'${END_DATE}'" "${PLAN_OFFLINE}"
    if [ $? -ne 0 ];then
        log "ERROR" "export planoffline failed"
        return 1
    fi

	md5sum ${PLAN_OFFLINE} > ${PLAN_OFFLINE}.md5
    if [ $? -ne 0 ];then
        log "ERROR" "generate planoffline md5 failed"
        return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${CRM_EXPORT_PLAN_OFFLINE} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${PLAN_OFFLINE}.md5`
	noahdt add ${CRM_EXPORT_PLAN_OFFLINE} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${PLAN_OFFLINE}
	alert $? "${msg}"
}

function export_usersrchs(){
    rm ${TMP_PATH}/*

    wget -q ${USER_SRCHS_FTP} -P ${TMP_PATH} --limit-rate=${USER_SRCHS_RATE}
    if [ $? -ne 0 ];then
        log "ERROR" "wget srchs file from tc-cron failed"
        return 1
    fi

	cd ${EXPORT_PATH}
    if [ $? -ne 0 ];then
        log "ERROR" "cd to ${EXPORT_PATH} failed"
        return 1
    fi

	cat ${TMP_PATH}/* | awk '{print $1"\t"$2}' > ${USER_SRCHS}
    if [ $? -ne 0 ];then
        log "ERROR" "export usersrchs failed"
        return 1
    fi

	md5sum ${USER_SRCHS} > ${USER_SRCHS}.md5
    if [ $? -ne 0 ];then
        log "ERROR" "generate usersrchs md5 failed"
        return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${CRM_EXPORT_USER_SRCHS} failed."
	md5=`getMd5FileMd5 ${EXPORT_PATH}/${USER_SRCHS}.md5`
	noahdt add ${CRM_EXPORT_USER_SRCHS} -m md5=${md5} -i date=${DATE} bscp://${EXPORT_PATH}/${USER_SRCHS}
	alert $? "${msg}"
}

#main

#防止主从同步延迟
sleep 300

check_path
alert $? "check path failed"

TYPE=$1
if [ -z $TYPE ];then
	TYPE=65535
fi



check_flag $TYPE 0
if [ $? -eq 1 ];then
	op_with_retry "export_useroffline" "export user offline result failed" $MAX_RETRY
fi

check_flag $TYPE 1
if [ $? -eq 1 ];then
	op_with_retry "export_planoffline" "export plan offline result failed" $MAX_RETRY
fi

check_flag $TYPE 2
if [ $? -eq 1 ];then
	op_with_retry "export_cost" "export user cost result failed" $MAX_RETRY
fi

check_flag $TYPE 3
if [ $? -eq 1 ];then
	op_with_retry "export_userbudget" "export user budget failed" $MAX_RETRY
fi

check_flag $TYPE 4
if [ $? -eq 1 ];then
	op_with_retry "export_usersrchs" "export user srchs failed" $MAX_RETRY
fi

exit 0
