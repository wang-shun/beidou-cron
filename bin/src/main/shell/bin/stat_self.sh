#!/bin/bash

#@file: stat_self.sh
#@author: qianlei
#@date: 2012-11-22
#@version: 1.0.1
#@brief: self group cost stat


CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DB_SHARDING=../lib/db_sharding.sh
[ -f "${DB_SHARDING}" ] && source $DB_SHARDING || echo "not exist ${DB_SHARDING} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

CONF_SH="../conf/stat_self.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


PRINT_LOG "stat self starting"

#����Ĭ��Ϊ����
DAY=$(date +%Y-%m-%d -d "-1 day")
MONTH=$(date +%Y%m -d "-1 day")
SUFFIX=$(date +%Y%m%d -d "-1 day")

if [ "x$1" != "x" ];then 
    DAY=$1
    MONTH=`date -d "$1" +%Y%m`  
    SUFFIX=`date -d "$1" +%Y%m%d`
    if [ $? -ne 0 ]; then
        PRINT_LOG "error date"
	exit
    fi
fi
PRINT_LOG "stat day is $DAY"
PRINT_LOG "stat month is $MONTH"


#����ļ�

#KA�ͻ���SME�е�VIP�ͻ�ѡ��ȫ��Ͷ�š���ѡ������������ҵ/վ�㡱���ƹ���
FILE_VIP_ALLSELF=${OUT_PATH}/vip_allself.$SUFFIX

#SME�з�VIP�ͻ�ѡ��ȫ��Ͷ�š���ѡ������������ҵ/վ�㡱���ƹ���
FILE_NORMAL_ALLSELF=${OUT_PATH}/normal_allself.$SUFFIX

#KA�ͻ���SME�е�VIP�ͻ���Ͷ�š�����������ҵ/վ�㡱���ƹ���
FILE_VIP_SELF=${OUT_PATH}/vip_self.$SUFFIX

#SME�з�VIP�ͻ���Ͷ�š�����������ҵ/վ�㡱���ƹ���
FILE_NORMAL_SELF=${OUT_PATH}/normal_self.$SUFFIX


#��ʱ�ļ�
FILE_VIP_UCID=${TMP_PATH}/vip_ucid
FILE_HEAVY_UCID=${TMP_PATH}/heavy_ucid

FILE_ALL_GROUP=${TMP_PATH}/all_group
FILE_NOALL_GROUP=${TMP_PATH}/noall_group
FILE_BUDGET=${TMP_PATH}/budget
FILE_COST=${TMP_PATH}/cost

FILE_WL_GROUP=${TMP_PATH}/groupid_wl
FILE_WL_GROUP_UNIQ=${TMP_PATH}/groupid_wl_uniq

FILE_ALL_GROUP_FILTER=${TMP_PATH}/all_group_filter
FILE_NOALL_GROUP_FILTER=${TMP_PATH}/noall_group_filter

FILE_SELF_GROUP=${TMP_PATH}/self_group
FILE_VIP_ALL_GROUP=${TMP_PATH}/vip_all_group
FILE_NORMAL_ALL_GROUP=${TMP_PATH}/normal_all_group
FILE_VIP_SELF_GROUP=${TMP_PATH}/vip_self_group
FILE_NORMAL_SELF_GROUP=${TMP_PATH}/normal_self_group

TMP_VIP_ALLSELF=${TMP_PATH}/tmp_vip_allself
TMP_NORMAL_ALLSELF=${TMP_PATH}/tmp_normal_allself
TMP_VIP_SELF=${TMP_PATH}/tmp_vip_self
TMP_NORMAL_SELF=${TMP_PATH}/tmp_normal_self

rm ${TMP_PATH}/*

function exit_count(){
    count=`cat $1|wc -l`
    PRINT_LOG "file $1 count is $count"
    if [ $count -eq 0 ];then
        PRINT_LOG "file $1 count is $count exit"
        exit;
    fi
}

function print_count(){
    count=`cat $1|wc -l`
    PRINT_LOG "file $1 count is $count"
}


#get userid  of vip_customer:352  heavy_custmer:319
java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.user.SelfStat $FILE_VIP_UCID $FILE_HEAVY_UCID  >> ${LOG_FILE} 2>&1
alert $? "failed to get vip&heavy userid"
exit_count $FILE_VIP_UCID
exit_count $FILE_HEAVY_UCID


#get allsite group
runsql_sharding_read "select g.groupid,g.userid from beidou.cprogroup g join beidou.cprogroupinfo i on g.groupid=i.groupid join beidou.cproplan p on g.planid=p.planid where g.groupstate=0 and p.planstate=0 and i.isallsite=1 and [g.userid]"  $FILE_ALL_GROUP
alert $? "failed to get db allsite group data"	
exit_count $FILE_ALL_GROUP

#get no allsite group
runsql_sharding_read "select g.groupid,g.userid,i.sitetradelist from beidou.cprogroup g join beidou.cprogroupinfo i on g.groupid=i.groupid join beidou.cproplan p on g.planid=p.planid where g.groupstate=0 and p.planstate=0 and i.isallsite=0 and [g.userid]"  $FILE_NOALL_GROUP
alert $? "failed to get db no allsite group data"
exit_count $FILE_NOALL_GROUP

#get group budget
runsql_sharding_read "select g.groupid, p.budget from beidou.cprogroup g, beidou.cproplan p where g.planid = p.planid and [g.userid]"  $FILE_BUDGET
alert $? "failed to get db group budget data"	
exit_count $FILE_BUDGET

#get group cost
STAT_AD_FILE="stat_ad_final_$SUFFIX"
wget -t 3 "$STAT_AD_FTP_PATH/$STAT_AD_FILE" -O $TMP_PATH/$STAT_AD_FILE
wget -t 3 "$STAT_AD_FTP_PATH/$STAT_AD_FILE.md5" -O $TMP_PATH/$STAT_AD_FILE.md5
md5num=`md5sum $TMP_PATH/$STAT_AD_FILE | awk '{print $1}'`
checknum=`awk '{print $1}' $TMP_PATH/$STAT_AD_FILE.md5` 
if [[ $md5num != $checknum ]]
then
	PRINT_LOG "wget file $STAT_AD_FTP_PATH/$STAT_AD_FILE failed!"
    exit 1;
fi
awk -F'\t' '{cost[$7]+=$4}END{for(groupid in cost){print groupid"\t"cost[groupid]}}' $TMP_PATH/$STAT_AD_FILE > $FILE_COST
#runsql_stat_read "select a.groupid,sum(a.cost) from beidoustat.stat_ad_$MONTH a where a.date='$DAY' group by a.groupid" $FILE_COST
alert $? "failed to get db cost data"	
exit_count $FILE_COST

#������ҵ(2903,400,3201,3202,3203,3204,3205,3206,3207,3208,3209,3210,3211,3212,3299,3301,3302,3303,3304,3305,3306,3307,3308,3309,3310,3311,3399,301,302,399) ���˺��groupid
runsql_sharding_read "select u.gid from beidou.cprounitstate? u,beidou.cprounitmater? m where u.state=0 and u.id=m.id and m.adtradeid not in (2903,400,3201,3202,3203,3204,3205,3206,3207,3208,3209,3210,3211,3212,3299,3301,3302,3303,3304,3305,3306,3307,3308,3309,3310,3311,3399,301,302,399) and [u.uid]"  $FILE_WL_GROUP 8
alert $? "failed to get db wuliao group data"	
print_count $FILE_WL_GROUP

sort $FILE_WL_GROUP|uniq >  $FILE_WL_GROUP_UNIQ
print_count $FILE_WL_GROUP_UNIQ


#����������ҵgroupid���ƹ�����й���
awk 'ARGIND==1{ array[$1]=1 }
     ARGIND==2{ heavyArr[$1]=1 }
     ARGIND==3{ if(array[$1] || heavyArr[$2]) { printf "%s\n",$0>> "'$FILE_ALL_GROUP_FILTER'" } }
     ARGIND==4{ if(array[$1] || heavyArr[$2]) { printf "%s\n",$0>> "'$FILE_NOALL_GROUP_FILTER'" }}
' $FILE_WL_GROUP_UNIQ $FILE_HEAVY_UCID $FILE_ALL_GROUP $FILE_NOALL_GROUP 


#����3��KA�ͻ���SME�е�VIP�ͻ���Ͷ�š�����������ҵ/վ�㡱���ƹ���
#����4��SME�з�VIP�ͻ���Ͷ�š�����������ҵ/վ�㡱���ƹ���
#��������ͳ��:  isallsite=0 and tradelist������������id,��������id��ȡ���£�
#select  tradeid,tradename from beidoucode.sitetrade where tradename like '�ٶ�%'
#---------+-----------+
#| tradeid | tradename |
#+---------+-----------+
#|     260 | �ٶ�����  | 
#|     281 | �ٶ�����  | 
#|     282 | �ٶ�ͼƬ  | 
#|     283 | �ٶ���Ƶ  | 
#|     284 | �ٶ�֪��  | 
#|     285 | �ٶ�����  | 


#��������group�ļ�����
#��ѡ���������������������ƹ�����ӵ�ȫ���ļ���
awk '{
    split($3,myarr,"|")
    havaself="0"
    for(item in myarr){
        if(myarr[item]=="260"||myarr[item]=="281"||myarr[item]=="282"||myarr[item]=="283"||myarr[item]=="284"||myarr[item]=="285"){
		havaself="1"
		break
        }
    }
    if(havaself=="1"){
	selfonly="1";
	for(item in myarr){
		if(myarr[item]!="260"&&myarr[item]!="281"&&myarr[item]!="282"&&myarr[item]!="283"&&myarr[item]!="284"&&myarr[item]!="285"&&myarr[item]!=""){
			selfonly="0"
			break
		}
	}
	if(selfonly=="0"){
		 printf "%s\t%s\n",$1,$2 >>  "'$FILE_ALL_GROUP_FILTER'"
	}
	if(selfonly=="1"){
		printf "%s\t%s\n",$1,$2 >>  "'$FILE_SELF_GROUP'"
	}
    }
}' $FILE_NOALL_GROUP_FILTER


if [ -f "$FILE_SELF_GROUP" ]; then
	print_count $FILE_SELF_GROUP

	#������������group��vip����ͨ�û��ֿ�
	awk 'NR==FNR{array[$1]=1} NR>FNR{
	    if(array[$2]){
		printf "%s\t%s\n",$1,$2>> "'$FILE_VIP_SELF_GROUP'"
	    }else{
		printf "%s\t%s\n",$1,$2>> "'$FILE_NORMAL_SELF_GROUP'"
	    }
	}' $FILE_VIP_UCID $FILE_SELF_GROUP

	#��vip��������group��Ԥ������ϲ�
	if [ -f "$FILE_VIP_SELF_GROUP" ] ; then
		print_count $FILE_VIP_SELF_GROUP
		awk 'NR==FNR{array[$1]=$2} NR>FNR{printf "%s\t%s\t%s\n",$1,array[$1]?array[$1]:0,$2 >> "'$TMP_VIP_SELF'" }'  $FILE_BUDGET $FILE_VIP_SELF_GROUP
		sort -k2nr $TMP_VIP_SELF |awk '{printf "%s\t%s\t%s\n",$1,$2,$3}'> $FILE_VIP_SELF
		md5sum  $FILE_VIP_SELF > ${FILE_VIP_SELF}.md5
	fi


	#����ͨ��������group��Ԥ������ϲ�
	if [ -f "$FILE_NORMAL_SELF_GROUP" ]; then
		print_count $FILE_NORMAL_SELF_GROUP
		awk 'NR==FNR{array[$1]=$2} NR>FNR{printf "%s\t%s\t%s\n",$1,array[$1]?array[$1]:0,$2 >> "'$TMP_NORMAL_SELF'" }'  $FILE_BUDGET $FILE_NORMAL_SELF_GROUP
		sort  -k2nr $TMP_NORMAL_SELF |awk '{printf "%s\t%s\t%s\n",$1,$2,$3}'> $FILE_NORMAL_SELF
		md5sum  $FILE_NORMAL_SELF > ${FILE_NORMAL_SELF}.md5
	fi
else
	PRINT_LOG "no have self group"
fi

#����1��KA�ͻ���SME�е�VIP�ͻ�ѡ��ȫ��Ͷ�š���ѡ������������ҵ/վ�㡱���ƹ���
#����2��SME�з�VIP�ͻ�ѡ��ȫ��Ͷ�š���ѡ������������ҵ/վ�㡱���ƹ���

if [ -f "$FILE_ALL_GROUP_FILTER" ]; then
	print_count $FILE_ALL_GROUP_FILTER

	#ȫ������������ͳ�ƣ���ȫ��group��vip����ͨ�û��ֿ�
	awk 'NR==FNR{array[$1]=1} NR>FNR{
	if(array[$2]){
		printf "%s\t%s\n",$1,$2>> "'$FILE_VIP_ALL_GROUP'"
	}else{
		printf "%s\t%s\n",$1,$2>> "'$FILE_NORMAL_ALL_GROUP'"
	}
	}' $FILE_VIP_UCID $FILE_ALL_GROUP_FILTER

	#��vipȫ��group�����ѹ����ϲ�
	if [ -f "$FILE_VIP_ALL_GROUP" ]; then
		print_count $FILE_VIP_ALL_GROUP
		awk 'NR==FNR{array[$1]=$2} NR>FNR{printf "%s\t%s\t%s\n",$1,array[$1]?array[$1]:0,$2 >> "'$TMP_VIP_ALLSELF'" }'  $FILE_COST $FILE_VIP_ALL_GROUP
		sort  -k2nr $TMP_VIP_ALLSELF |awk '{printf "%s\t%s\t%s\n",$1,$2,$3}'> $FILE_VIP_ALLSELF
		md5sum  $FILE_VIP_ALLSELF > ${FILE_VIP_ALLSELF}.md5
		PRINT_LOG "vip allself end"
	fi

	if [ -f "$FILE_NORMAL_ALL_GROUP" ]; then
		print_count $FILE_NORMAL_ALL_GROUP
		awk 'NR==FNR{array[$1]=$2} NR>FNR{printf "%s\t%s\t%s\n",$1,array[$1]?array[$1]:0,$2 >> "'$TMP_NORMAL_ALLSELF'" }'  $FILE_COST $FILE_NORMAL_ALL_GROUP
		sort  -k2nr $TMP_NORMAL_ALLSELF |awk '{printf "%s\t%s\t%s\n",$1,$2,$3}'> $FILE_NORMAL_ALLSELF
		md5sum  $FILE_NORMAL_ALLSELF > ${FILE_NORMAL_ALLSELF}.md5
		PRINT_LOG "normal allself end"
	fi
else
	PRINT_LOG "no have all group"
fi


#ɾ��7��ǰ����ʷ����
find ${OUT_PATH}/  -mtime +7 -type f |  xargs rm -rf

PRINT_LOG "stat self end"
