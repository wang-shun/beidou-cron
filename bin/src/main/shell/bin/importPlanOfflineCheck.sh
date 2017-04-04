#!/bin/sh
#探测今天的下线文件，对于缺少的重新抓取

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/planOffline.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importPlanOffline.sh
reader_list=chenlu

LOG_FILE=${LOG_PATH}/importPlanOffline.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

day=`date +%Y%m%d`

if [ $# -ge 1 ]; then
   day=$1
fi

echo "start $day" >> $LOG_FILE

#get yesterday
h=23
dayBeforeTime=$((`date +%s`-24*3600))
dayBefore=`date +%Y%m%d -d "1970-01-01 UTC ${dayBeforeTime} seconds"`

   for ((time=0;time<46;time+=15))
   do
       t=`printf "%02d" $time`
	   
	   ts=`date +%s -d "$dayBefore $h:$t:00"`
	   
	   if [[ $((ts+25*60)) -lt "`date +%s`" ]]; then
           if [[ ! -f ${PLAN_OFFLINE_DATA_PATH}/$dayBefore/"bdbudget."$dayBefore"-"$h$t".log" ]]
	   then
	       sh importPlanOffline.sh $dayBefore $h$t
	   fi
       fi
   done
   
#CIRCLE CHECK
for hour in {0..23}
do
   h=`printf "%02d" $hour`
   for ((time=0;time<46;time+=15))
   do
       t=`printf "%02d" $time`
	   
	   ts=`date +%s -d "$day $((10#$hour)):$t:00"`
	   
	   if [[ $((ts+25*60)) -lt "`date +%s`" ]]; then
           if [[ ! -f ${PLAN_OFFLINE_DATA_PATH}/$day/"bdbudget."$day"-"$h$t".log" ]]
		   then
			   sh importPlanOffline.sh $day $h$t
		   fi
	   fi
   done
done

echo "end "$day >> $LOG_FILE
