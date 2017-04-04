#!/bin/sh
#get cost data of each day, if there is no params, then query cost data yesterday
#input format:yyyyMMdd

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/outputcrmstat_new.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=get_day_finan_merge.sh
reader_list=zengyunfeng

cd ${ROOT_PATH}
if [ $? -ne 0 ] ; then
	exit 1
fi
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

#function:(only of this shell) 
#merge business.1 and business.2 into one file
if [ -f "${BUSINESS_FILE}.old" ]
then
	rm -rf "${BUSINESS_FILE}.old"
fi
msg="merge business file failed"
for fileNum in `seq ${CRM_TASK_CONCURRENT_NUMBER}`
do
	cat ${BUSINESS_FILE}.${fileNum} >> ${BUSINESS_FILE}.old
	alert $? "${msg}"", current file name is ${BUSINESS_FILE}.${fileNum}"
done

#append two columns into business file in the end

cd ${DATA_PATH}

# get all data of table cproplan_offline yesterday
today=`date +%Y%m%d`
yesterday=`date +%Y%m%d -d"1 days ago"`
if [ -n "$1" ] ;then
	yesterday=`date -d "$1" +%Y%m%d`
	today=`date -d "-1 days ago ${yesterday}" +%Y%m%d`
fi


msg="get plan offline time failed"
runsql_sharding_read "select p.pid, p.uid, p.time, p.flag from (select planid as pid, userid as uid, offtime as time, 1 as flag from beidoucap.cproplan_offline where offtime>=${yesterday} and offtime<${today} and [userid]) p order by p.uid, p.pid, p.time" "plan.time"
alert $? "${msg}"

#plan.time(four columns):
#planid, userid, time, flag(order by userid, planid, time asc)
msg="get latest time of plan offline failed"
awk -F"\t" 'BEGIN{
        pid=0;offtime=0;uid=0;
    }
    {
        if(pid==$1)
        {       
            if($4==1){offtime=$3;}else{offtime=0;}
        }           
        else    
        {       
            if(offtime!=0){print pid"\t"uid"\t"offtime;}
            uid=$2;pid=$1;
            if($4==1){offtime=$3;}else{offtime=0;}
        }           
    }
    END{    
        if(offtime!=0){print pid"\t"uid"\t"offtime;}
}' plan.time | sort -k2n -k3 | awk -F"\t" 'BEGIN{
        uid=0;pid=0; 
    }
    {
        if(uid!=$2){print $1"\t"$2"\t"$3;}
        uid=$2; 
    }' > plan.offtime
alert $? "${msg}"

#get planname from table cproplan
msg="get planname from table cproplan failed"
runsql_sharding_read "select planid, planname from beidou.cproplan where [userid]" "plan.name"
alert $? "${msg}"

#merge planname, output: userid,planname,offtime
msg="get planname into file(user.offtime) failed"
awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{print $2"\t"map[$1]"\t"$3}' plan.name plan.offtime > user.offtime
alert $? "${msg}"

#generate business file, key: userid
msg="generate business file failed"
awk -F"\t" 'ARGIND==1{map[$1]=$2"\t"$3;}ARGIND==2{
    if($1 in map)
    {
        print $0"\t"map[$1];
    }
    else
    {
        print $0"\t\t";
    }
}' user.offtime ${BUSINESS_FILE}.old > ${BUSINESS_FILE}
alert $? "${msg}"


cd ${DATA_PATH}

rm beidou_user.${yesterday}
rm beidou_user.${yesterday}.md5
wget -t 3 ftp://yf-beidou-cron01.yf01.baidu.com:/home/work/beidou-cron/data/beidou_user/beidou_user.${yesterday}
wget -t 3 ftp://yf-beidou-cron01.yf01.baidu.com:/home/work/beidou-cron/data/beidou_user/beidou_user.${yesterday}.md5
msg="failed to download crm user list"
md5sum -c beidou_user.${yesterday}.md5
alert $? "{msg}"
#filter ineffective user
mv business business.bak
awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){print $0}}' beidou_user.${yesterday}  business.bak > business

md5sum business >business.md5
alert $? "{msg}" 

# the module below is added by xiehao on 20110629
#YESTERDAY=`date -d'yesterday' +%Y%m%d`
YESTERDAY=`date -d "${yesterday}" +%Y%m%d`
msg="failed to create business.${YESTERDAY} file"
cp business business.${YESTERDAY}
msg="generate business MD5 file failed"
md5sum business.${YESTERDAY} > business.${YESTERDAY}.md5
alert $? "${msg}"

#regist file to dts
msg="regist DTS for ${GET_DAY_FINAN_MERGE_BUSINESS} failed."
md5=`getMd5FileMd5 ${DATA_PATH}/business.${YESTERDAY}.md5`
noahdt add ${GET_DAY_FINAN_MERGE_BUSINESS} -m md5=${md5} -i date=${YESTERDAY} bscp://${DATA_PATH}/business.${YESTERDAY}
alert $? "${msg}"
