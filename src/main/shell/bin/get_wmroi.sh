#!/bin/bash
#@author:cuihuaizhou
#@data:2011-06-12
#@version: 1.0.0
#@brief:download 8 wmroi files and merge this 8 files into 4 files
#@brief:this shell will be run at 12:00 everyday

#we use "yesterday" to get 8 files,but use "today" to name 4 merged files
#merged file named like this :   bd_trans_plan.${TODAY}.normal.0.0
#CONF_PATH=/home/work/cuihuaizhou  
CONF_PATH=/home/work/beidou-cron/conf
LIB_PATH=/home/work/beidou-cron/bin
LOG_FILE=/home/work/beidou-cron/log/get_wmroi.log

source "${CONF_PATH}/get_wmroi.conf"
source "${LIB_PATH}/beidou_lib.sh"
source "/home/work/.bash_profile"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=get_wmroi.sh
reader_list=zhangpingan

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
    echo "[${timeNow}]${1}" >> ${LOG_FILE}
}

function alarm_func()
{
    #$1:return signal
	#$2:is_alarm
	#$3:msg
	if [ $1 -eq 1 ];then
	   if [ $2 -eq 0 ]; then
           exit 0
       else
	       alert 1 $3
       fi	   
	fi
}

DATE=$1
if [ -z $DATE ]
then
	DATE=`date -d yesterday +%Y%m%d` ###DATE default means yesterday , when we download date from logdata ,we use this DATE 
	TODAY=`date +%Y%m%d`
else
	TODAY=`date -d  " 1 day $DATE" +%Y%m%d`
fi

if ! [ -e $BEIDOU_CRON_PLAN_PATH ]
     then
         mkdir $BEIDOU_CRON_PLAN_PATH
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to mkdir $BEIDOU_CRON_PLAN_PATH!"
                return 1
         fi
fi


if ! [ -e $BEIDOU_CRON_GROUP_PATH ]
     then
         mkdir $BEIDOU_CRON_GROUP_PATH
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to mkdir $BEIDOU_CRON_GROUP_PATH!"
                return 1
         fi
fi

if ! [ -e $BEIDOU_CRON_AD_PATH ]
     then
         mkdir $BEIDOU_CRON_AD_PATH
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to mkdir $BEIDOU_CRON_AD_PATH!"
                return 1
         fi
fi

if ! [ -e $BEIDOU_CRON_GROUP_SITE_PATH ]
     then
         mkdir $BEIDOU_CRON_GROUP_SITE_PATH
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to mkdir $BEIDOU_CRON_GROUP_SITE_PATH!"
                return 1
         fi
fi

if ! [ -e $LOCAL_TEMP ]
     then
         mkdir -p $LOCAL_TEMP
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to mkdir $LOCAL_TEMP!"
                return 1
         fi
fi

if ! [ -f $ERROR_TRACE_FILE ]
     then
         touch $ERROR_TRACE_FILE
         if [ $? -ne 0 ]
            then
                log "FATAL" "Fail to touch $ERROR_TRACE_FILE!"
                return 1
         fi
fi

function download_wmroi_pid_day()
{
	curTime=$1
	cd ${LOCAL_TEMP}
	#==========================>Start wget wmroi_pid_fnum
	wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_pid_fnum" -O "wmroi_pid_fnum.$curTime"
	if [ $? -ne 0 ]
	then
		PRINT_LOG "Fail to wget wmroi_pid_fnum !"
		return 1
	fi
	#cp wmroi_pid_fnum.$curTime wmroi_pid_fnum
	
	awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=5){
	alarmContent="get_wmroi: Invalid line "FNR" in wmroi_pid_fnum!"
        print alarmContent >> ERROR_TRACE_FILE
	exit 1 }
}' wmroi_pid_fnum.$curTime
	if [ $? -ne 0 ]
	then 
		PRINT_LOG "get_wmroi: Invalid line in wmroi_pid_fnum!"
        return 1
	fi	

	#计算线下md5
	offline_md5=`md5sum wmroi_pid_fnum.$curTime | awk '{print $1}'`
	
	#获取线上md5
	wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_pid_fnum&type=md5" -O "wmroi_pid_fnum.$curTime.md5"
	online_md5=`awk '{print $2}' $"wmroi_pid_fnum.$curTime.md5"`
	#md5sum -c "wmroi_pid_fnum.$curTime.md5"
	if [ "${online_md5}" != "${offline_md5}" ]
	then
        #报警，错误处理
	echo "Fail to check wmroi_pid_fnum md5 !"
        PRINT_LOG "get_wmroi: Fail to download wmroi_pid_fnum."
	return 1
	fi
	#<=========================End wget wmroi_pid_fnum successfully

	#==========================>Start wget wmroi_pid
	wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_pid" -O "wmroi_pid.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_pid !"
                return 1
        fi
	
	awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=5){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_pid!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_pid.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_pid!"
        return 1
        fi
	
	#计算线下md5
        offline_md5=`md5sum wmroi_pid.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_pid&type=md5" -O "wmroi_pid.$curTime.md5"
        
        
        online_md5=`awk '{print $2}' $"wmroi_pid.$curTime.md5"`
        
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_pid md5 !"
        return 1
        fi
	#<=========================End wget wmroi_pid successfully

}

function download_wmroi_gid_day()
{
        curTime=$1
        cd ${LOCAL_TEMP}
	#==========================>Start wget wmroi_gid_fnum
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_gid_fnum" -O "wmroi_gid_fnum.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_gid_fnum !"
                return 1
        fi
        #cp wmroi_gid_fnum.$curTime wmroi_gid_fnum
	
        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=6){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_gid_fnum!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_gid_fnum.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_gid_fnum!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_gid_fnum.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_gid_fnum&type=md5" -O "wmroi_gid_fnum.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_gid_fnum.$curTime.md5"`
        #md5sum -c "wmroi_gid_fnum.$curTime.md5"
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_gid_fnum md5 !"
        return 1
        fi
        #<=========================End wget wmroi_gid_fnum successfully


        #==========================>Start wget wmroi_gid
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_gid" -O "wmroi_gid.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_gid !"
                return 1
        fi

        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=6){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_gid!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_gid.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_gid!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_gid.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_gid&type=md5" -O "wmroi_gid.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_gid.$curTime.md5"`
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_gid md5 !"
        return 1
        fi
        #<=========================End wget wmroi_gid successfully

}

function download_wmroi_adid_day()
{
        curTime=$1
	    cd ${LOCAL_TEMP}
        #==========================>Start wget wmroi_adid_fnum
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_adid_fnum" -O "wmroi_adid_fnum.$curTime"
        if [ $? -ne 0 ]
        then
                echo "Fail to wget wmroi_adid_fnum !"
                return 1
        fi
        #cp wmroi_gid_fnum.$curTime wmroi_adid_fnum

        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=7){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_adid_fnum!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_adid_fnum.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_adid_fnum!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_adid_fnum.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_adid_fnum&type=md5" -O "wmroi_adid_fnum.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_adid_fnum.$curTime.md5"`
        #md5sum -c "wmroi_adid_fnum.$curTime.md5"
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wm_adid_fnum md5 !"
        return 1
        fi
        #<=========================End wget wmroi_adid_fnum successfully


        #==========================>Start wget wmroi_adid
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_adid" -O "wmroi_adid.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_adid !"
                return 1
        fi

        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=7){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_adid!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_adid.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_adid!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_adid.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_adid&type=md5" -O "wmroi_adid.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_adid.$curTime.md5"`
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_adid md5 !"
        return 1
        fi
        #<=========================End wget wmroi_adid successfully

}

function download_wmroi_uid_site_day()
{
        curTime=$1
	    cd ${LOCAL_TEMP}
        #==========================>Start wget wmroi_uid_site_fnum
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_uid_site_fnum" -O "wmroi_uid_site_fnum.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_uid_site_fnum !"
                return 1
        fi

        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=8){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_uid_site_fnum!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_uid_site_fnum.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_uid_site_fnum!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_uid_site_fnum.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_uid_site_fnum&type=md5" -O "wmroi_uid_site_fnum.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_uid_site_fnum.$curTime.md5"`
        #md5sum -c "wmroi_adid_fnum.$curTime.md5"
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_uid_site_fnum md5 !"
        return 1
        fi
        #<=========================End wget wmroi_uid_site_fnum successfully


        #==========================>Start wget wmroi_uid_site
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_uid_site" -O "wmroi_uid_site.$curTime"
        if [ $? -ne 0 ]
        then
                PRINT_LOG "Fail to wget wmroi_uid_site !"
                return 1
        fi

        awk -v ERROR_TRACE_FILE=$ERROR_TRACE_FILE '{if(NF !=8){
        alarmContent="get_wmroi: Invalid line "FNR" in wmroi_uid_site!"
        print alarmContent >> ERROR_TRACE_FILE
        exit 1 }
}' wmroi_uid_site.$curTime
        if [ $? -ne 0 ]
        then
        PRINT_LOG "get_wmroi: Invalid line in wmroi_uid_site!"
        return 1
        fi

        #计算线下md5
        offline_md5=`md5sum wmroi_uid_site.$curTime | awk '{print $1}'`

        #获取线上md5
        wget -t 3 "${LOGDATA_PATH}&date=$curTime&item=wmroi_uid_site&type=md5" -O "wmroi_uid_site.$curTime.md5"
        online_md5=`awk '{print $2}' $"wmroi_uid_site.$curTime.md5"`
        if [ "${online_md5}" != "${offline_md5}" ]
        then
        #报警，错误处理
        PRINT_LOG "Fail to check wmroi_uid_site md5 !"
        return 1
        fi
        #<=========================End wget wmroi_uid_site successfully
}


function merge_pid
{	
	cd ${LOCAL_TEMP}
	curTime=$1
	time=`date -d yesterday +%s`
	awk -vtime=$time -F"\t" 'ARGIND==1{
        mapa[$1"\t"$2"\t"$4"\t"$5]=$3;
	}ARGIND==2{
	mapb[$1"\t"$2"\t"$4"\t"$5]=$3;
	}END{
	
	for(m in mapa){
	if(m in mapb)
	{result[m]=mapa[m]"\t"mapb[m];}
	else {result[m]=mapa[m]"\t"0;}
	}

	for(m in mapb){
	if(!(m in mapa))
	{result[m]=0"\t"mapb[m];}
	}

	for(k in result)
	{print time"\t"k"\t"result[k];}
	}' ./wmroi_pid_fnum.$curTime   ./wmroi_pid.$curTime  >${BEIDOU_CRON_PLAN_PATH}/bd_trans_plan.${TODAY}.normal.0.0

	cd ${BEIDOU_CRON_PLAN_PATH}
	md5sum bd_trans_plan.${TODAY}.normal.0.0 >bd_trans_plan.${TODAY}.normal.0.0".md5"
}

function merge_gid
{	
	cd ${LOCAL_TEMP}
	curTime=$1
	time=`date -d yesterday +%s`
	awk -vtime=$time -F"\t" 'ARGIND==1{
        mapa[$1"\t"$2"\t"$4"\t"$5"\t"$6]=$3;
	}ARGIND==2{
	mapb[$1"\t"$2"\t"$4"\t"$5"\t"$6]=$3;
	}END{
	for(m in mapa){
	if(m in mapb)
	{result[m]=mapa[m]"\t"mapb[m];}
	else {result[m]=mapa[m]"\t"0;}
	}

	for(m in mapb){
	if(!(m in mapa))
	{result[m]=0"\t"mapb[m];}
	}

	for(k in result)
	{print time"\t"k"\t"result[k];}
	}' ./wmroi_gid_fnum.$curTime   ./wmroi_gid.$curTime  >${BEIDOU_CRON_GROUP_PATH}/bd_trans_group.${TODAY}.normal.0.0

	cd ${BEIDOU_CRON_GROUP_PATH}
    md5sum bd_trans_group.${TODAY}.normal.0.0 >bd_trans_group.${TODAY}.normal.0.0".md5"
}

function merge_adid
{
	cd ${LOCAL_TEMP}
	curTime=$1
	time=`date -d yesterday +%s`
	awk -vtime=$time -F"\t" 'ARGIND==1{
        mapa[$1"\t"$2"\t"$4"\t"$5"\t"$6"\t"$7]=$3;
	}ARGIND==2{
	mapb[$1"\t"$2"\t"$4"\t"$5"\t"$6"\t"$7]=$3;
	}END{
	for(m in mapa){
	if(m in mapb)
	{result[m]=mapa[m]"\t"mapb[m];}
	else {result[m]=mapa[m]"\t"0;}
	}

	for(m in mapb){
	if(!(m in mapa))
	{result[m]=0"\t"mapb[m];}
	}

	for(k in result)
	{print time"\t"k"\t"result[k];}
	}' ./wmroi_adid_fnum.$curTime   ./wmroi_adid.$curTime  >${BEIDOU_CRON_AD_PATH}/bd_trans_ad.${TODAY}.normal.0.0

	cd ${BEIDOU_CRON_AD_PATH}
        md5sum bd_trans_ad.${TODAY}.normal.0.0 >bd_trans_ad.${TODAY}.normal.0.0".md5"
}

function merge_uid_site
{
	cd ${LOCAL_TEMP}
	curTime=$1
	time=`date -d yesterday +%s`
	awk -vtime=$time -F"\t" 'ARGIND==1{
        mapa[$1"\t"$2"\t"$4"\t"$5"\t"$6"\t"$8"\t"$7]=$3;
	}ARGIND==2{
	mapb[$1"\t"$2"\t"$4"\t"$5"\t"$6"\t"$8"\t"$7]=$3;
	}END{
	for(m in mapa){
	if(m in mapb)
	{result[m]=mapa[m]"\t"mapb[m];}
	else {result[m]=mapa[m]"\t"0;}
	}

	for(m in mapb){
	if(!(m in mapa))
	{result[m]=0"\t"mapb[m];}
	}

	for(k in result)
	{print time"\t"k"\t"result[k];}
	}' ./wmroi_uid_site_fnum.$curTime   ./wmroi_uid_site.$curTime  >${BEIDOU_CRON_GROUP_SITE_PATH}/bd_trans_group_site.${TODAY}.normal.0.0

	cd ${BEIDOU_CRON_GROUP_SITE_PATH}
        md5sum bd_trans_group_site.${TODAY}.normal.0.0 >bd_trans_group_site.${TODAY}.normal.0.0".md5"
}

file1=${BEIDOU_CRON_PLAN_PATH}/bd_trans_plan.${TODAY}.normal.0.0
file2=${BEIDOU_CRON_GROUP_PATH}/bd_trans_group.${TODAY}.normal.0.0 
file3=${BEIDOU_CRON_AD_PATH}/bd_trans_ad.${TODAY}.normal.0.0
file4=${BEIDOU_CRON_GROUP_SITE_PATH}/bd_trans_group_site.${TODAY}.normal.0.0


need_retry=1;
if [ -f "$file1" ] && [ -f "$file2" ] && [ -f "$file3" ] && [ -f "$file4" ];then
    need_retry=0;
fi

is_alarm=1;
current_hour=`date +%H`
if [ $current_hour -le $HOUR_DELAY ];then
    is_alarm=0;
fi

if [ $need_retry -eq 1 ]; then

    download_wmroi_pid_day $DATE
    alarm_func $? $is_alarm "get_wmroi: Fail to download wmroi pid file."

    download_wmroi_gid_day $DATE
    alarm_func $? $is_alarm "get_wmroi: Fail to download wmroi gid file."

    download_wmroi_adid_day $DATE
    alarm_func $? $is_alarm "get_wmroi: Fail to download wmroi adid file."

    download_wmroi_uid_site_day $DATE
    alarm_func $? $is_alarm "get_wmroi: Fail to download wmroi uid site file."

    merge_pid $DATE
    alarm_func $? $is_alarm $? "get_wmroi: Fail to merge wmroi pid file."

    merge_gid $DATE
    alarm_func $? $is_alarm $? "get_wmroi: Fail to merge wmroi gid file."

    merge_adid $DATE
    alarm_func $? $is_alarm $? "get_wmroi: Fail to merge wmroi adid file."

    merge_uid_site $DATE
    alarm_func $? $is_alarm $? "get_wmroi: Fail to merge wmroi uid site file."

fi
