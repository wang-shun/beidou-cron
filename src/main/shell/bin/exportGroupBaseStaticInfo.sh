#!/bin/bash

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/exportGroupBaseStaticInfo.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

program=exportGroupBaseStaticInfo.sh
reader_list=dongying01

LOG_FILE=${LOG_PATH}/exportGroupBaseStaticInfo.log
exportDir="${DATA_PATH}/exportGroupBaseStaticInfo"
#������Ŀ¼�Ƿ����
if [ ! -d ${exportDir} ];then
    mkdir ${exportDir}
fi

#usertrade file get info
USER_TRADEID_HOST="tc-sf-cron07.tc.baidu.com"
USER_TRADEID_PATH="/home/work/var/sf-stat/data"
USER_TRADEID_FILENAME="usertrade.txt"
MAX_RETRY=3
LIMIT_RATE=20m
TIME_OUT=5

#set timestamp
if [ -z $1 ];then
    TIME_YYYYMMDD=`date -d "yesterday" +%Y-%m-%d`
    TIME_YYYYMMDD_FORMAT=`date -d "yesterday" +%Y%m%d`
    TIME_YYYYMM=`date -d "yesterday" +%Y%m`
    result_time=`date -d "yesterday" +%Y%m%d`
else
    TIME_YYYYMMDD=`date -d "$1" +%Y-%m-%d`
    TIME_YYYYMMDD_FORMAT=`date -d "$1" +%Y%m%d`
    TIME_YYYYMM=`date -d "$1" +%Y%m`
    result_time=`date -d "$1" +%Y%m%d`
fi


result_file="groupBasicStaticInfo.${result_time}"

#first get group basic info from beidou db
file1="groupinfo"
msg="��beidou�����ȡ�ƹ�����Ϣʧ��"
runsql_sharding_read "use beidou; select a.groupid,a.userid,a.grouptype,a.targettype,b.reglist,b.price from cprogroup a,cprogroupinfo b where a.groupid=b.groupid and [a.userid]" "${exportDir}/${file1}"
alert $? "${msg}"

#seconde ��stat_ad_final_yyyyMMdd�ļ��л�ȡ���������beidoustat���ѯ(by kanghongwei)
file2="groupinfo_stat"

msg="wget ${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT} failed."
wget -t3  --limit-rate=30M  ftp://${STAT_AD_DATA_SERVER}/${DATA_OUTPUT_PATH}/${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}  -O ${exportDir}/${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}
alert $? "${msg}"

msg="wget ${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}.md5 failed."
wget  -t3  ftp://${STAT_AD_DATA_SERVER}/${DATA_OUTPUT_PATH}/${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}.md5  -O ${exportDir}/${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}.md5
alert $? "${msg}"

cd $exportDir
msg="check ${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}'s md5 failed."
md5sum -c ${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT}.md5
alert $? "${msg}"

msg="��ȡ�ƹ���չ�ֵ��������Ϣʧ��" 
cat ${FILE_STAT_AD_FINAL}${TIME_YYYYMMDD_FORMAT} | awk 'BEGIN{OFS="\t"}{a[$7] += $2;b[$7] += $3; c[$7] += $4}END{for(i in a) print i, a[i], b[i],c[i]}' | sort -k1 -n > ${exportDir}/${file2}
alert $? "${msg}"

# ץȡ�ļ���md5
cd $exportDir
getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME}.md5 -O ${USER_TRADEID_FILENAME}.md5"
clear_command="if [[ -f ${USER_TRADEID_FILENAME}.md5 ]]; then rm ${USER_TRADEID_FILENAME}.md5; fi"
msg="Failed to get user_trade md5 for exportGroupBaseStaticInfo"
getfile "$getfile_command" "$clear_command"
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi

# ץȡ�ļ�
getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME} -O ${USER_TRADEID_FILENAME}"
clear_command="if [[ -f $USER_TRADEID_FILENAME ]]; then rm $USER_TRADEID_FILENAME; fi"
msg="Failed to get user_trade for exportGroupBaseStaticInfo"
getfile "$getfile_command" "$clear_command"
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi

### У��md5
md5sum -c ${USER_TRADEID_FILENAME}.md5 > /dev/null
alert $? "Failed to check user_trade md5"

sort -nk1  $file1 > $file1.sort
sort -nk1  $file2 > $file2.sort

awk 'BEGIN{FS="\t"}{
    if(NR==FNR){
        stat[$1]=$0
    }else{
        if(stat[$1]){
                print stat[$1]"\t"$0
        }
    }
}' "$file2.sort" "$file1.sort" > merge.tmp

awk 'BEGIN{FS="\t"}{
    if(NR==FNR){
        trade[$1]=$2
    }else{
        if(trade[$6]){
            print $1"\t"trade[$6]"\t"$7"\t"$8"\t"$9"\t"$10"\t"$4"\t"$2"\t"$3
        }
    }
}' ${USER_TRADEID_FILENAME} merge.tmp > result.tmp

msg="��beidoucap���л�ȡ������Ϣʧ��"
runsql_cap_read "select firstregid,secondregid from beidoucap.reginfo"  "reginfo"
alert $? "${msg}"

#����reginfo����һ������idlist
awk 'BEGIN{FS="\t"}{print $1}' reginfo|sort -n|uniq > reginfo.first
allRegionList=`awk '{if(firstStr==""){firstStr=$0}else{firstStr=firstStr"|"$0}}END{print firstStr}' reginfo.first`

#���ļ��е�����ϢΪnull��תΪȫһ������list������������鲢��һ������ȥ��
awk 'BEGIN{FS="\t"}{
    if(NR==FNR){
        if($2==0)
        {
            regmap[$1]=$1
        }else{
            regmap[$2]=$1
        }
    }else{
        if($5=="NULL")
        {
            print $1"\t"$2"\t"$3"\t"$4"\t'"$allRegionList"'\t"$6"\t"$7"\t"$8"\t"$9
        }else{
            reglist=""
            
            split($5,regArray,"|")
            for (i in regArray) {
                if(regmap[regArray[i]]){
                    firstRegId=regmap[regArray[i]]
                    if(!resultMap[firstRegId]){
                        resultMap[firstRegId]=1
                    }
                }
            }
            for (i in resultMap){
                if(reglist==""){
                    reglist=i
                }else{
                    reglist=reglist"|"i
                }
            }
            print $1"\t"$2"\t"$3"\t"$4"\t"reglist"\t"$6"\t"$7"\t"$8"\t"$9
            delete resultMap
        }
    }
}' reginfo result.tmp > $result_file

md5sum $result_file > $result_file.md5

#check result file data num,if less then 10 alert
num=`wc -l $result_file|awk '{print $1}'`
msg="���ɵ�$result_file�ļ�����С��10�����ע"
if [ $num -lt 10 ]; then 
    alert 1 "$msg"
fi

