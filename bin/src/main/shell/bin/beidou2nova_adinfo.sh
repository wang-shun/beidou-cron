#!/bin/bash
#@file: beidou_blacklist
#@author: zhangpingan
#@date: 2011-12-14
#@version: 1.0.0.0
#@brief: Provide Beidou Adinfo to Nova

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh 
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

TODAY=`date +"%Y%m%d"`
YESTERDAY=`date -d "1 day ago" +"%Y%m%d"`
if [ $# -gt 1 ];
then
TODAY=`date -d "$2" +"%Y%m%d"`
YESTERDAY=`date -d "1 day ago ${TODAY}" +"%Y%m%d"`
fi

CONF_SH=../conf/beidou2nova_adinfo.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=beidou2nova_adinfo.sh
reader_list=zhangpingan



function check_conf()
{
	if ! [[ $MAX_RETRY ]]
	then
		echo "Conf[MAX_RETRY] is empty or its value is invalid"
		return 1
	fi
	
	return 0
}

function check_path()
{
    if ! [ -e $INFO_PATH_OUTPUT ]
	then
         mkdir -p $INFO_PATH_OUTPUT
	     if [ $? -ne 0 ]
	       then
	       echo "Fail to mkdir [$INFO_PATH_OUTPUT]!" >> $LOG_FILE
	       return 1
	     fi
    fi
	if ! [ -e $INFO_PATH_TMP ]
	then
         mkdir -p $INFO_PATH_TMP
	     if [ $? -ne 0 ]
	       then
	       echo "Fail to mkdir [$INFO_PATH_TMP]!" >> $LOG_FILE
	       return 1
	     fi
    fi
    #############temporally added by zhangxichuan#####################
    if ! [ -e $INFO_PATH_OUTPUT_TMP ]
	then
         mkdir -p $INFO_PATH_OUTPUT_TMP
	     if [ $? -ne 0 ]
	       then
	       echo "Fail to mkdir [$INFO_PATH_TMP]!" >> $LOG_FILE
	       return 1
	     fi
    fi
    #############temporally added by zhangxichuan#####################
	return 0	
}

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}


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
        PRINT_LOG "this is $cnt time to do $operation"
        $operation
        if [ $? -eq 0 ];then
            return 0
        fi
        sleep 5
    done
    alert 1 "$message"
}

#�����ݿ��е���ȫ��˲ʱ������ݣ������ɿ�ͨ����
function exportAdDatFromDB()
{
    cd ${INFO_PATH_TMP}
   
    #add by caichao
	runsql_sharding_read "select vt.groupid from beidou.cprogroupvt vt , vtpeople p where vt.pid=p.pid and p.type in (4,5) and vt.userid=p.userid" "${LOCAL_RT2_GROUP}"
	#awk '{if($4==16){print $0}}' ${LOCAL_ALL_GROUP_LIST}  > ${LOCAL_NEED_TO_CHANGE}
	#awk '{if($4!=16){print $0}}' ${LOCAL_ALL_GROUP_LIST}  > ${LOCAL_NOT_NEED_TO_CHANGE}
	
	#############temporally added by zhangxichuan#####################
	cp ${LOCAL_RT2_GROUP} ${INFO_PATH_OUTPUT_TMP}/
	#############temporally added by zhangxichuan#####################
	
   ####################################
   #�����û��б�:userid, ustate, balancestat, ushifenstatid
   PRINT_LOG "��ѯȫ��user�б�����"
   rm -f ${LOCAL_ALL_USER_LIST}
   runsql_cap_read "select u.userid,u.ustate,u.balancestat, u.ushifenstatid from beidoucap.useraccount u"   "${LOCAL_ALL_USER_LIST}"
    if [ $? -ne 0 ];then
        PRINT_LOG "��ѯȫ��user�б�����ʧ��"
        return 1
    fi
	
	rm -f ${BALANCE_FILE}
    wget -t 3 -q --limit-rate=30M ftp://${BALANCE_SERVER}/${BALANCE_PATH}/${BALANCE_FILE}
	if [ $? -ne 0 ];then
        PRINT_LOG "ץȡ�賿�û��������ʧ��"
        return 1
    fi
	#(userid,balance)
	
	
	#������Ч״̬�û��б�: userid (ustate=0 and ushifenstatid in 2,3,6)
	awk 'ARGIND==1{map[$1]=$2}ARGIND==2{if($2==0 && map[$1]>0 && ($4==2 || $4==3 || $4==6)){printf("%s\n",$1)}}' ${BALANCE_FILE} ${LOCAL_ALL_USER_LIST} > ${LOCAL_EFFECT_USER_LIST}
	if [ $? -ne 0 ];then
        PRINT_LOG "������Ч״̬�û��б�ʧ��"
        return 1
    fi
   ####################################
   
	
   ####################################
   #���ɼƻ��б�:userid, planid, planstate
   PRINT_LOG "��ѯȫ��plan�б�����"
   rm -f ${LOCAL_ALL_PLAN_LIST}
   runsql_sharding_read "select planid, userid,  planstate, budgetover, promotion_type, wireless_bid_ratio from beidou.cproplan where [userid]" "${LOCAL_ALL_PLAN_LIST}"
   if [ $? -ne 0 ];then
        PRINT_LOG "��ѯȫ��plan�б�����ʧ��"
        return 1
   fi
   #������Ч״̬�ƻ��б�:planid,userid  (planstate=0)
   awk '{if($3==0) {printf("%s\t%s\n",$1,$2)}}' ${LOCAL_ALL_PLAN_LIST} > ${LOCAL_EFFECT_PLAN_LIST}
   if [ $? -ne 0 ];then
        PRINT_LOG "������Ч״̬�ƻ��б�ʧ��"
        return 1
   fi
   #�����ƻ�ײ��״̬
   ##############################
   PRINT_LOG "��ȡ��ǰ�ƻ�ײ������"
   awk '{if($4==1) {printf("%s\n",$1)}}' ${LOCAL_ALL_PLAN_LIST} > ${PLANOFFLINE_MERGE}
   if [ $? -ne 0 ];then
        PRINT_LOG "��ȡ��ǰ�ƻ�ײ������ʧ��"
        return 1
   fi
   ####################################
   
   
   ####################################
   PRINT_LOG "��ѯȫ��group�б�����"
   #�������б�:groupid,planid,userid,targettype,grouptype,groupstate
   rm -f ${LOCAL_ALL_GROUP_LIST}
   runsql_sharding_read "select groupid,planid,userid,targettype,grouptype,groupstate from beidou.cprogroup where [userid]" "${LOCAL_ALL_GROUP_LIST}"
   if [ $? -ne 0 ];then
	    PRINT_LOG "��ѯȫ��group�б�����ʧ��"
	    return 1
   fi
   


	awk -v change=${LOCAL_NEED_TO_CHANGE} -v not_change=${LOCAL_NOT_NEED_TO_CHANGE} '{
	
		if($4==16||$4==48)
		{
			print $0 > change
		}
		else
		{
			print $0 > not_change
		}
	}' ${LOCAL_ALL_GROUP_LIST}
	
	awk 'ARGIND==1{
			rt2[$1]
		}
		ARGIND==2{
	 		if($1 in rt2){printf("%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,8,$5,$6)}
			else{print $0}
	}' ${LOCAL_RT2_GROUP} ${LOCAL_NEED_TO_CHANGE} > ${LOCAL_NEW_NEED_TO_CHANGE}
	#merge
	cat ${LOCAL_NOT_NEED_TO_CHANGE}  ${LOCAL_NEW_NEED_TO_CHANGE}  >> ${LOCAL_MERGE_CHANGE}
	rm -rf ${LOCAL_ALL_GROUP_LIST}
	#mv ${LOCAL_ALL_GROUP_LIST} ${LOCAL_ALL_GROUP_LIST}.bak
	mv ${LOCAL_MERGE_CHANGE} ${LOCAL_ALL_GROUP_LIST}
	#end 
   
   #������Ч״̬���б�: groupid,planid,userid,targettype,grouptype
   awk '{if($6==0) {printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}}' ${LOCAL_ALL_GROUP_LIST} >  ${LOCAL_EFFECT_GROUP_LIST}
   if [ $? -ne 0 ];then
	    PRINT_LOG "������Ч״̬���б�ʧ��"
	    return 1
   fi
   ####################################
	
	

   ####################################
   PRINT_LOG "��ѯȫ��unit�б�����"
   #���ɴ����б�: id,groupid,planid,userid,wuliaoType,state 
   rm -f ${LOCAL_ALL_UNIT_LIST}
   runsql_sharding_read "select s.id,s.gid,s.pid,s.uid,m.wuliaoType,s.state,m.adtradeid from beidou.cprounitstate? s,beidou.cprounitmater? m where s.id=m.id and [s.uid]" "${LOCAL_ALL_UNIT_LIST}" ${TAB_UNIT_SLICE}
   if [ $? -ne 0 ];then
	        PRINT_LOG "��ѯȫ�ⴴ������ʧ��"
	        return 1
   fi
   
   #������Ч״̬�����б�: id,groupid, planid, userid, wuliaotype, adtradeid
   awk '{if($6==0) {printf("%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5,$7)}}' ${LOCAL_ALL_UNIT_LIST} >  ${LOCAL_EFFECT_UNIT_LIST}
   if [ $? -ne 0 ];then
        PRINT_LOG "������Ч״̬�����б�ʧ��"
        return 1
   fi
   ####################################
   
   
   
   #########################
   #�����ͨ����
   PRINT_LOG "�ϲ���ͨ����"
   awk  '
   BEGIN{ 
        MAP_MT[1]=1;
        MAP_MT[2]=2;
        MAP_MT[3]=3;
        MAP_MT[5]=6;
			  
        MAP_AD[1]=1;
        MAP_AD[2]=2;
		MAP_AD[3]=3;
		MAP_AD[4]=4;
		MAP_AD[5]=5;
		MAP_AD[6]=6;
        MAP_AD[7]=7;}
        ARGIND==1{
           MAP_GROUP[$1]=($1"\t"$4"\t"MAP_AD[$5]);
        }
        ARGIND==2{
           MAP_PLAN[$1]=($5"\t"$6);
        }
        ARGIND==3{
           if(($2 in MAP_GROUP) && ($3 in MAP_PLAN))
	       {
		       #id,gid,pid,uid,m.wuliaoType,    state,adtradeid,  MAP_MT, gid, MAP_TAR, MAP_AD, promotion_type, wireless_bid_ratio
	           printf("%s\t%s\t%s\t%s\n",$0,MAP_MT[$5],MAP_GROUP[$2],MAP_PLAN[$3]);
	       }
       }'  ${LOCAL_ALL_GROUP_LIST} ${LOCAL_ALL_PLAN_LIST} ${LOCAL_ALL_UNIT_LIST} > ${LOCAL_ALL_UNIT_LIST}.merge
       	if [ $? -ne 0 ];then
        PRINT_LOG "AWK Merge��ͨ����ʧ��"
        return 1
        fi
		
	   PRINT_LOG "�����ͨ����"
       awk -F'\t' '{printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$10,$8,$11,$7,$12,$13);}'  ${LOCAL_ALL_UNIT_LIST}.merge >  ${OUTPUT_ALL_DATA}
	   if [ $? -ne 0 ];then
         PRINT_LOG "AWK �����ͨ����ʧ��"
       return 1
       fi
	   rm -f ${LOCAL_ALL_UNIT_LIST}.merge
	#########################
	
	
	##########################
	if [ ! -f ${OUTPUT_ALL_DATA} ]; then 
     PRINT_LOG "${OUTPUT_ALL_DATA}�ļ�������"
	 return 1
    fi;
	cp ${INFO_PATH_TMP}/${OUTPUT_ALL_DATA} ${INFO_PATH_OUTPUT}/${OUTPUT_ALL_DATA_RENAME}
	if [ $? -ne 0 ];then
       PRINT_LOG "�����ͨ�����ļ���outputĿ¼ʧ��"
       return 1
    fi
	cd ${INFO_PATH_OUTPUT}
	md5sum ${OUTPUT_ALL_DATA_RENAME} > ${OUTPUT_ALL_DATA_RENAME}.md5
	if [ $? -ne 0 ];then
       PRINT_LOG "���ɿ�ͨ�����ļ�MD5ʧ��"
       return 1
    fi
        
    #regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_BD_ALL_INFO} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${OUTPUT_ALL_DATA_RENAME}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_BD_ALL_INFO} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${OUTPUT_ALL_DATA_RENAME}
	noahdt add ${BEIDOU2NOVA_ADINFO_BD_ALL_INFO_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${OUTPUT_ALL_DATA_RENAME}.md5
	alert $? "${msg}"
	
	##########################
	
	
   
   
   #########################
   #������Ч���Ĳ㼶����
   cd ${INFO_PATH_TMP}
   PRINT_LOG "������Ч���Ĳ㼶����"
	awk  '
    ARGIND==1{
	    MAP_EFFECT_USER[$1]
	}
	ARGIND==2{
	    MAP_EFFECT_PLAN[$1]
	}
	ARGIND==3{
	    MAP_EFFECT_GROUP[$1]
	}
	ARGIND==4{
	    MAP_EFFECT_UNIT[$1]
	}
	ARGIND==5{
	    if(($1 in MAP_EFFECT_UNIT ) && ($2 in MAP_EFFECT_GROUP) && ($3 in MAP_EFFECT_PLAN) && ($4 in MAP_EFFECT_USER)){
		    printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5,$6,$7,$8);
		}
	}
	' ${LOCAL_EFFECT_USER_LIST} ${LOCAL_EFFECT_PLAN_LIST} ${LOCAL_EFFECT_GROUP_LIST} ${LOCAL_EFFECT_UNIT_LIST} ${OUTPUT_ALL_DATA} > ${OUTPUT_EFFECT_DATA}.tmp
   	if [ $? -ne 0 ];then
      PRINT_LOG "������Ч���Ĳ㼶����ʧ��"
      return 1
    fi
   
    #########################
    #����µ���Ч�Ĳ㼶����
    if [ ! -f ${OUTPUT_ALL_DATA} ]; then 
     PRINT_LOG "${OUTPUT_ALL_DATA}�ļ�������"
	 return 1
    fi;
    #��Ч�����б�
	PRINT_LOG "������Ч�����б�"
	awk '{print $1}' ${OUTPUT_EFFECT_DATA}.tmp | sort -k1n -u > ${LOCAL_EFFECT_UNIT_FILTER}
    if [ $? -ne 0 ];then
        PRINT_LOG "������Ч�����б�ʧ��"
        return 1
    fi
	#��Ч�ƹ���
	PRINT_LOG "������Ч�ƹ����б�"
	awk '{print $2}' ${OUTPUT_EFFECT_DATA}.tmp | sort -k1n -u > ${LOCAL_EFFECT_GROUP_FILTER}
	if [ $? -ne 0 ];then
        PRINT_LOG "������Ч�ƹ����б�ʧ��"
        return 1
    fi

	#��Ч�ƹ�ƻ�
	PRINT_LOG "������Ч�ƹ�ƻ��б�"
	awk '{print $3}' ${OUTPUT_EFFECT_DATA}.tmp | sort -k1n -u > ${LOCAL_EFFECT_PLAN_FILTER}
    if [ $? -ne 0 ];then
        PRINT_LOG "������Ч�ƹ�ƻ��б�ʧ��"
        return 1
    fi

	#��Ч�ƹ��û�
	PRINT_LOG "������Ч�ƹ��û��б�"
	awk '{print $4}' ${OUTPUT_EFFECT_DATA}.tmp | sort -k1n -u > ${LOCAL_EFFECT_USER_FILTER}
	if [ $? -ne 0 ];then
        PRINT_LOG "������Ч�ƹ��û��б�ʧ��"
        return 1
    fi
    awk -F'\t' '{print $4}'  ${OUTPUT_EFFECT_DATA}.tmp  | sort -u > bod_effect_user_${TODAY}.txt
    mv bod_effect_user_${TODAY}.txt ${INFO_PATH_OUTPUT}/
	cd ${INFO_PATH_OUTPUT}
	md5sum bod_effect_user_$TODAY}.txt > bod_effect_user_${TODAY}.txt.md5
}

#����ȫ��QT�ؼ�������
function exportAllQTData()
{
   cd ${INFO_PATH_TMP}
   PRINT_LOG "����ȫ��QT�ؼ�������"
   #userid, groupid, keywordid, wordid
   rm -f ${QT_KEYWORD_TABLE}*
   for((i=0;i<${QT_WORD_NUM_CONF};i++))
   do
       rm -f ${QT_KEYWORD_TABLE}${i}
   	   runsql_sharding_read "select a.userid, a.groupid, a.keywordid, a.wordid ,a.keyword from beidou.cprokeyword${i} a, beidou.cprogroup b where a.groupid=b.groupid and (b.targettype & ${QT_FLAG} =${QT_FLAG}) and [a.userid]" "${QT_KEYWORD_TABLE}${i}"
   done
}

#����ȫ��CT�ؼ�������
function exportAllCTData()
{
   cd ${INFO_PATH_TMP}
   PRINT_LOG "����ȫ��CT�ؼ�������"
   #userid, groupid, keywordid, wordid
   rm -f ${CT_KEYWORD_TABLE}*
   for((i=0;i<${CT_WORD_NUM_CONF};i++))
   do
       rm -f ${CT_KEYWORD_TABLE}${i}
       runsql_sharding_read "select a.userid, a.groupid, a.keywordid, a.wordid ,a.keyword from beidou.cprokeyword${i} a, beidou.cprogroup b where a.groupid=b.groupid and (b.targettype & ${CT_FLAG} =${CT_FLAG}) and [a.userid]" "${CT_KEYWORD_TABLE}${i}"
   done
}

#����ATOM�ʱ�
function exportAtomDictionary()
{
    cd ${INFO_PATH_TMP}
    PRINT_LOG "����ȫ��ؼ����ֵ�"
	
	#�жϿ���ļ��Ƿ���ѵ���
	for((i=0;i<${QT_WORD_NUM_CONF};i++))
    do
		if [ ! -f ${QT_KEYWORD_TABLE}${i} ]; then 
           PRINT_LOG "${QT_KEYWORD_TABLE}${i}�ļ�������"
	       return 1
        fi;
	done
	
	for((i=0;i<${CT_WORD_NUM_CONF};i++))
    do
		if [ ! -f ${CT_KEYWORD_TABLE}${i} ]; then 
           PRINT_LOG "${CT_KEYWORD_TABLE}${i}�ļ�������"
	       return 1
        fi;
	done
	
	
	#���Ⱥϲ�QT�ʱ�
	rm -f ${DICTIONARY_FILENAME}.qt
    touch ${DICTIONARY_FILENAME}.qt

    for((i=0;i<${QT_WORD_NUM_CONF};i++))
    do
	    awk -F'\t' '{if(!($4 in map)){map[$4];printf("%s\t%s\n",$4,$5)}}' ${QT_KEYWORD_TABLE}${i} > tmp.data
			if [ $? -ne 0 ];then
            PRINT_LOG "����${QT_KEYWORD_TABLE}${i}��QT�ؼ����ֵ�ʧ��"
            return 1
            fi
        awk -F'\t' 'ARGIND==1{
               WORD[$1]=$2
			   printf("%s\t%s\n",$1,$2);
           } ARGIND==2 {
               if(!($1 in WORD)){
			       printf("%s\t%s\n",$1,$2);
			   }
           } ' ${DICTIONARY_FILENAME}.qt tmp.data > ${DICTIONARY_FILENAME}.qt.tmp
		  	if [ $? -ne 0 ];then
            PRINT_LOG "����${QT_KEYWORD_TABLE}${i}��QT�ؼ����ֵ�ʧ��"
            return 1
            fi
    mv ${DICTIONARY_FILENAME}.qt.tmp ${DICTIONARY_FILENAME}.qt
    PRINT_LOG "file ${QT_KEYWORD_TABLE}${i} is added to dictionary"
    rm -f ${QT_KEYWORD_TABLE}${i}
   done

   #��κϲ�CT�ʱ�
   rm -f ${DICTIONARY_FILENAME}.ct
   touch ${DICTIONARY_FILENAME}.ct
   for((i=0;i<${CT_WORD_NUM_CONF};i++))
   do
       awk -F'\t' '{if(!($4 in map)){map[$4];printf("%s\t%s\n",$4,$5)}}' ${CT_KEYWORD_TABLE}${i} > tmp.data
			if [ $? -ne 0 ];then
            PRINT_LOG "����${CT_KEYWORD_TABLE}${i}��QT�ؼ����ֵ�ʧ��"
            return 1
            fi
   
       awk -F'\t' 'ARGIND==1{
               WORD[$1]=$2
			   printf("%s\t%s\n",$1,$2);
           } ARGIND==2 {
               if(!($1 in WORD)){
			       printf("%s\t%s\n",$1,$2);
			   }
           } ' ${DICTIONARY_FILENAME}.ct tmp.data > ${DICTIONARY_FILENAME}.ct.tmp
		  	if [ $? -ne 0 ];then
            PRINT_LOG "����${CT_KEYWORD_TABLE}${i}��CT�ؼ����ֵ�ʧ��"
            return 1
            fi
    mv ${DICTIONARY_FILENAME}.ct.tmp ${DICTIONARY_FILENAME}.ct
    PRINT_LOG "file ${CT_KEYWORD_TABLE}${i} is added to dictionary"
    rm -f ${CT_KEYWORD_TABLE}${i}
   done
   
   awk -F'\t' 'ARGIND==1{
           WORD[$1]=$2
		   printf("%s\t%s\n",$1,$2);
       } ARGIND==2 {
           if(!($1 in WORD)){
			  printf("%s\t%s\n",$1,$2);
		   }
	   } ' ${DICTIONARY_FILENAME}.ct ${DICTIONARY_FILENAME}.qt > ${DICTIONARY_FILENAME}
	   	if [ $? -ne 0 ];then
            PRINT_LOG "�����ؼ����ֵ�ʧ��"
            return 1
        fi
	
	rm -f ${DICTIONARY_FILENAME}.qt
	rm -f ${DICTIONARY_FILENAME}.ct
    mv ${INFO_PATH_TMP}/${DICTIONARY_FILENAME} ${INFO_PATH_OUTPUT}/${DICTIONARY_FILENAME}
    if [ $? -ne 0 ];then
       PRINT_LOG "Keyword dictionary export failed"
       return 1
    fi
    cd ${INFO_PATH_OUTPUT}
    md5sum ${DICTIONARY_FILENAME} > ${DICTIONARY_FILENAME}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "����ؼ����ֵ�MD5ʧ��"
       return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_WORD_DICTIONARY} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${DICTIONARY_FILENAME}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_WORD_DICTIONARY} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${DICTIONARY_FILENAME}
	noahdt add ${BEIDOU2NOVA_ADINFO_WORD_DICTIONARY_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${DICTIONARY_FILENAME}.md5
	alert $? "${msg}"
    
}

#��ȡ��ЧCT\QT�ؼ������ݣ������Ҫ�ȵ�7����Ч���������Ժ�
function exportEffectKeyword()
{
    cd ${INFO_PATH_TMP}
    #�ж������ļ��Ƿ����
	if [ ! -f ${LOCAL_EFFECT_GROUP_FINAL} ]; then 
        PRINT_LOG "��Ч�ƹ����ļ�������"
	    return 1
    fi;
	if [ ! -f ${LOCAL_EFFECT_USER_FINAL} ]; then 
        PRINT_LOG "��Ч�û��ļ�������"
	    return 1
    fi;
	PRINT_LOG "��ʼ����QT�ؼ�������"
	for((i=0;i< ${QT_WORD_NUM_CONF}; i++))
	do
	   	if [ ! -f ${QT_KEYWORD_TABLE}${i} ]; then 
        PRINT_LOG "${QT_KEYWORD_TABLE}${i}�ļ�������"
	    return 1
        fi;
		PRINT_LOG "�ӱ�${i}�й���QT�ؼ�������"
		awk ' ARGIND==1{
		        EFFECT_USER[$1];
		    } ARGIND==2{
			    EFFECT_GROUP[$1];
			} ARGIND==3{
			  #userid, groupid, keywordid, wordid
			  if(($1 in EFFECT_USER) && ($2 in EFFECT_GROUP)){
			       printf("%s\n",$0);
			  }
			}' ${LOCAL_EFFECT_USER_FINAL}  ${LOCAL_EFFECT_GROUP_FINAL} ${QT_KEYWORD_TABLE}${i} > ${QT_KEYWORD_EFFECT}${i};
	    if [ $? -ne 0 ];then
          PRINT_LOG "�ӱ�${1}�й���QT�ؼ�������ʧ��"
          return 1
        fi
	done
	
	PRINT_LOG "��ʼ����CT�ؼ�������"
	for((i=0;i< ${CT_WORD_NUM_CONF}; i++))
	do
	   	if [ ! -f ${CT_KEYWORD_TABLE}${i} ]; then 
        PRINT_LOG "${CT_KEYWORD_TABLE}${i}�ļ�������"
	    return 1
        fi;
		PRINT_LOG "�ӱ�${i}�й���CT�ؼ�������"
		awk ' ARGIND==1{
		        EFFECT_USER[$1];
		    } ARGIND==2{
			    EFFECT_GROUP[$1];
			} ARGIND==3{
			  #userid, groupid, keywordid, wordid
			  if(($1 in EFFECT_USER) && ($2 in EFFECT_GROUP)){
			       printf("%s\n",$0);
			  }
			}' ${LOCAL_EFFECT_USER_FINAL}  ${LOCAL_EFFECT_GROUP_FINAL} ${CT_KEYWORD_TABLE}${i} > ${CT_KEYWORD_EFFECT}${i};
	    if [ $? -ne 0 ];then
          PRINT_LOG "�ӱ�${1}�й���CT�ؼ�������ʧ��"
          return 1
        fi
	done
}

#ץȡչ�����ݣ���������Ч����
function mergeValidData()
{
    cd ${INFO_PATH_TMP}
	
	#########################
	PRINT_LOG "�����Ч����"
    if [ ! -f ${OUTPUT_ALL_DATA} ]; then 
     PRINT_LOG "${OUTPUT_ALL_DATA}�ļ�������"
	 return 1
    fi;
	awk  '
    ARGIND==1{
	    MAP_EFFECT_USER[$1]
	}
	ARGIND==2{
	    MAP_EFFECT_PLAN[$1]
	}
	ARGIND==3{
	    MAP_EFFECT_GROUP[$1]
	}
	ARGIND==4{
	    MAP_EFFECT_UNIT[$1]
	}
	ARGIND==5{
	    if(($1 in MAP_EFFECT_UNIT ) && ($2 in MAP_EFFECT_GROUP) && ($3 in MAP_EFFECT_PLAN) && ($4 in MAP_EFFECT_USER)){
		print $0
		}
	}
	' ${LOCAL_EFFECT_USER_FILTER} ${LOCAL_EFFECT_PLAN_FILTER} ${LOCAL_EFFECT_GROUP_FILTER} ${LOCAL_EFFECT_UNIT_FILTER} ${OUTPUT_ALL_DATA} > ${OUTPUT_EFFECT_DATA}
	if [ $? -ne 0 ];then
      PRINT_LOG "AWK �����Ч����ʧ��"
      return 1
    fi
	
	
	#########################
	cp ${INFO_PATH_TMP}/${OUTPUT_EFFECT_DATA} ${INFO_PATH_OUTPUT}/${OUTPUT_EFFECT_DATA_RENAME}
	if [ $? -ne 0 ];then
       PRINT_LOG "�����Ч�����ļ���outputĿ¼ʧ��"
       return 1
    fi
	cd ${INFO_PATH_OUTPUT}
	md5sum ${OUTPUT_EFFECT_DATA_RENAME} > ${OUTPUT_EFFECT_DATA_RENAME}.md5
	if [ $? -ne 0 ];then
       PRINT_LOG "������Ч�����ļ�MD5ʧ��"
       return 1
    fi
    
	#regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_BD_EFFECT_INFO} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${OUTPUT_EFFECT_DATA_RENAME}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_BD_EFFECT_INFO} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${OUTPUT_EFFECT_DATA_RENAME}
	noahdt add ${BEIDOU2NOVA_ADINFO_BD_EFFECT_INFO_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${OUTPUT_EFFECT_DATA_RENAME}.md5
	alert $? "${msg}"
	
	
	
	#########################
	
	
	#########################
	cd ${INFO_PATH_TMP}
    #���������Ч�ƻ����
	if [ ! -f ${OUTPUT_EFFECT_DATA} ]; then 
	PRINT_LOG "${OUTPUT_EFFECT_DATA}�ļ��޷��ҵ�"; 
	return 1;
	fi
	awk '{printf("%s\t%s\n",$3,$4)}' ${OUTPUT_EFFECT_DATA} | sort -k1n -u > ${VALID_PLAN_LIST}.tmp
	if [ $? -ne 0 ];then
         PRINT_LOG "�������ʹ�õ���Ч�ƻ��б�ʧ��"
    return 1
    fi
	#cat ${VALID_PLAN_LIST}.tmp ${VALID_PLAN_LIST_YESTERDAY} | sort -k1n -u > ${VALID_PLAN_LIST}
	cat ${VALID_PLAN_LIST}.tmp | sort -k1n -u > ${VALID_PLAN_LIST}
	if [ $? -ne 0 ];then
         PRINT_LOG "Merge�õ�����ʹ�õ���Ч�ƻ��б�ʧ��"
    return 1
    fi
	rm -f ${VALID_PLAN_LIST}.tmp
	#########################
}


#��ȡ������ЧԤ��������ļ�
function exportValidBudgetFiles()
{
   ################################
   cd ${INFO_PATH_TMP}
   if [ ! -f ${VALID_PLAN_LIST} ]; then 
     PRINT_LOG "${VALID_PLAN_LIST}�ļ�������"
	 return 1
   fi;
   ################################
   
   
   ################################
   #��ѯȫ��ƻ�����Ԥ��
   PRINT_LOG "��ѯȫ��ƻ�����Ԥ��"
   runsql_sharding_read "select planid,userid,budget from beidou.cproplan where [userid]" "${ALL_PLAN_BUDGET}"
   if [ $? -ne 0 ];then
        PRINT_LOG "��ѯ��Ч�ƻ�����Ԥ��ʧ��"
        return 1
   fi
   #���ɽ�����Ч�ƻ�DBԤ��
   PRINT_LOG "���ɽ�����Ч�ƻ�DBԤ��"
   awk 'ARGIND==1{MAP[$1]} ARGIND==2{if($1 in MAP) {print $0}}'  ${VALID_PLAN_LIST} ${ALL_PLAN_BUDGET} | sort -k1n -u > ${VALID_PLAN_BUDGET}
   if [ $? -ne 0 ];then
        PRINT_LOG "���ɽ�����Ч�ƻ�DBԤ��ʧ��"
        return 1
   fi

   ################################
   
   
   ################################
   #��ȡ�����û��������
    PRINT_LOG "��ȡ�����û��������"
      #�賿�û��������
    rm -f ${BALANCE_FILE}
    wget -t 3 -q --limit-rate=30M ftp://${BALANCE_SERVER}/${BALANCE_PATH}/${BALANCE_FILE}
	if [ $? -ne 0 ];then
        PRINT_LOG "ץȡ�賿�û��������ʧ��"
        return 1
    fi
	#(userid,balance)
	awk '
	ARGIND==1{
	   USER_BALANCE[$1]=$2/100
	} ARGIND==2{
	   printf("%s\t%s\t%s\n",$1,$2,USER_BALANCE[$2]);
	}' ${BALANCE_FILE} ${VALID_PLAN_BUDGET}> ${VALID_PLAN_BALANCE}
	if [ $? -ne 0 ];then
        PRINT_LOG "���ɼƻ�-�û��������ʧ��"
        return 1
    fi
    ##############################
	
	
	
	##############################
	#��ȡ�ƻ���������ֵ�� planid,  ���ѣ� ����
    PRINT_LOG "��ȡ�ƻ���������ֵ"
    runsql_clk_read "select planid,sum(price) from beidoufinan.cost_${YESTERDAY} group by planid" "${COST_PLAN}.tmp"
	    
    if [ $? -ne 0 ];then
        PRINT_LOG "��ȡ�ƻ���������ֵʧ��"
        return 1
    fi
    sort -k1n -u ${COST_PLAN}.tmp > ${COST_PLAN}
    rm -f ${COST_PLAN}.tmp
	##############################
	
	
	##############################
    #��ȡ�ƻ�����ײ������
    PRINT_LOG "��ȡ�ƻ�����ײ������"
        rm -f ${PLANOFFLINE_YESTERDAY}.tmp
    wget -t 3 -q --limit-rate=50M ftp://${PLANOFFLINE_SERVER}/${PLANOFFLINE_PATH}/${PLANOFFLINE_FILE}
    if [ $? -ne 0 ];then
        PRINT_LOG "ץȡ���ռƻ�ײ������ʧ��"
        return 1
    fi

    cat ${PLANOFFLINE_FILE} | sort -k1n >> ${PLANOFFLINE_YESTERDAY}.tmp
    if [ $? -ne 0 ];then
        PRINT_LOG "�ϲ����ռƻ�ײ������ʧ��"
        return 1
    fi
    rm -f ${PLANOFFLINE_FILE}
	#�����ʽ�ļ�
	PRINT_LOG "Merge����ƻ�ײ������"
	awk -F'\t' ' ARGIND==1{
	  MAP_PLAN_USER[$1]=$2;
	  MAP_PLAN_BUDGET[$1]=$3;
	  MAP_PLAN_OFFTIME[$1]="0000-00-00 00:00:00";
	  MAP_PLAN_OFFFLAG[$1]=0;
	  MAP_PLAN_COST[$1]=0;
	  MAP_PLAN_STATE[$1]=1;
	} ARGIND==2 {
	  MAP_PLAN_STATE[$1]=0;
	} ARGIND==3 {
	  MAP_PLAN_COST[$1]=$2;
	} ARGIND==4 {
	  MAP_PLAN_OFFFLAG[$1]=1;
	  MAP_PLAN_BUDGET[$1]=$3;
	  MAP_PLAN_OFFTIME[$1]=$4;
	  printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", $1, MAP_PLAN_USER[$1] , MAP_PLAN_BUDGET[$1], MAP_PLAN_STATE[$1] , MAP_PLAN_OFFFLAG[$1],MAP_PLAN_OFFTIME[$1],MAP_PLAN_COST[$1]);
	} ARGIND==5 {
      if (MAP_PLAN_OFFFLAG[$1]==0){
	  printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", $1, MAP_PLAN_USER[$1] , MAP_PLAN_BUDGET[$1], MAP_PLAN_STATE[$1], MAP_PLAN_OFFFLAG[$1],MAP_PLAN_OFFTIME[$1],MAP_PLAN_COST[$1]);
	  }
	} ' ${ALL_PLAN_BUDGET} ${VALID_PLAN_BUDGET} ${COST_PLAN}  ${PLANOFFLINE_YESTERDAY}.tmp  ${ALL_PLAN_BUDGET}| sort -k1n > ${PLANOFFLINE_YESTERDAY}
	if [ $? -ne 0 ];then
        PRINT_LOG "�ϲ����ռƻ�ײ������ʧ��"
        return 1
    fi
	
    cp ${INFO_PATH_TMP}/${PLANOFFLINE_YESTERDAY} ${INFO_PATH_OUTPUT}/${PLANOFFLINE_YESTERDAY_RENAME}
    if [ $? -ne 0 ];then
       PRINT_LOG "������ռƻ�ײ�������ļ���outputĿ¼ʧ��"
       return 1
    fi
    cd ${INFO_PATH_OUTPUT}
    md5sum ${PLANOFFLINE_YESTERDAY_RENAME} > ${PLANOFFLINE_YESTERDAY_RENAME}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "���ռƻ�ײ�������ļ�MD5ʧ��"
       return 1
    fi
    
	#regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_CPRO_PLAN_OFFLINE} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${PLANOFFLINE_YESTERDAY_RENAME}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_CPRO_PLAN_OFFLINE} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${PLANOFFLINE_YESTERDAY_RENAME}
	noahdt add ${BEIDOU2NOVA_ADINFO_CPRO_PLAN_OFFLINE_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${PLANOFFLINE_YESTERDAY_RENAME}.md5
	alert $? "${msg}"
	
   ##############################
   ##################################
   #ɾ��һЩ��ʱ�ļ�
   cd ${INFO_PATH_TMP}
   rm -f  ${LOCAL_EFFECT_USER_LIST}
   rm -f  ${LOCAL_EFFECT_PLAN_LIST}
   rm -f  ${LOCAL_EFFECT_GROUP_LIST}
   rm -f  ${LOCAL_EFFECT_UNIT_LIST}
   rm -f ${LOCAL_ALL_USER_LIST}
   rm -f ${LOCAL_ALL_PLAN_LIST}
   rm -f ${LOCAL_ALL_GROUP_LIST}
   rm -f ${LOCAL_ALL_UNIT_LIST}
   rm -f ${ALL_PLAN_BUDGET}
   ################################################################################################
}


#�����û���Ϣ�����û��Ƿ�ΪVIP��
function exportUserVipInfo()
{
    cd ${INFO_PATH_TMP}
    PRINT_LOG "����shifen���û��ļ�"
	rm -f ${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat*
    wget -q ftp://${ULEVEL_FILE_FTP_NAME}:${ULEVEL_FILE_FTP_PWD}@${ULEVEL_FILE_SERVER}/${ULEVEL_FILE_PATH}/${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat
	if [ $? -ne 0 ];then
       PRINT_LOG "����shifen���û��ļ�ʧ��"
       return 1
    fi
	wget -q ftp://${ULEVEL_FILE_FTP_NAME}:${ULEVEL_FILE_FTP_PWD}@${ULEVEL_FILE_SERVER}/${ULEVEL_FILE_PATH}/${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat.md5
	if [ $? -ne 0 ];then
       PRINT_LOG "����shifen���û��ļ�MD5ʧ��"
       return 1
    fi
    ULEVEL_MD5=`md5sum ${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat | cut -d' ' -f1`
    if [ "${ULEVEL_MD5}" != `cat ${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat.md5` ] ;then
        PRINT_LOG "У��shifen���û��ļ�MD5ʧ��"
		return 1
    fi
	
	#����ulevel�ļ�
	rm -f ${UVIP_FILE}
	awk -F"\t" 'BEGIN{OFS="\t"} {if($2==10101){print $1,$3,0} else if($2==10104) {print $1,$3,1}}' ${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat > ${UVIP_FILE}
	if [ $? -ne 0 ];then
       PRINT_LOG "�����û���Ϣ�����û��Ƿ�ΪVIP��ʧ��"
       return 1
    fi
	#ɾ����ʱ�ļ�
	rm -f ${ULEVEL_FILE_NAME_PRE}${YESTERDAY}.dat*
	
    #�����ʽ�ļ�
    mv ${INFO_PATH_TMP}/${UVIP_FILE} ${INFO_PATH_OUTPUT}/${UVIP_FILE}
    if [ $? -ne 0 ];then
       PRINT_LOG "�����û���Ϣ�����û��Ƿ�ΪVIP��ʧ��"
       return 1
    fi
    cd ${INFO_PATH_OUTPUT}
    md5sum ${UVIP_FILE} > ${UVIP_FILE}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "�����û���Ϣ�����û��Ƿ�ΪVIP��ʧ��"
       return 1
    fi
    #regist file to dts
    msg="regist DTS for ${BEIDOU2NOVA_ADINFO_USERVIP} failed."
    md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${UVIP_FILE}.md5`
    noahdt add ${BEIDOU2NOVA_ADINFO_USERVIP} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${UVIP_FILE}
    noahdt add ${BEIDOU2NOVA_ADINFO_USERVIP_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${UVIP_FILE}.md5
    alert $? "${msg}"
}


#����QT��CT�ؼ���չ�ֵ������
function exportQTCTData()
{
    cd ${INFO_PATH_TMP}
	#����QT���չ������
	rm -f ${QT_FILE_NAME}
	PRINT_LOG "��ʼ����QT�ؼ���չ�ֵ������"
	#userid, planid, groupid, kewordid, wordid, srchs, clks, cost
    wget -t 3 -q --limit-rate=30M ftp://${QT_FILE_SERVER}/${QT_FILE_PATH}/${QT_FILE_NAME}
	if [ $? -ne 0 ];then
        PRINT_LOG "����QT�ؼ���չ�ֵ������ʧ��"
        return 1
    fi
	
	
    cd ${INFO_PATH_TMP}
	#����CT���չ������
	rm -f ${CT_FILE_NAME}
	PRINT_LOG "��ʼ����CT�ؼ���չ�ֵ������"
	#userid, planid, groupid, kewordid, wordid, srchs, clks, cost
    wget -t 3 -q --limit-rate=30M ftp://${CT_FILE_SERVER}/${CT_FILE_PATH}/${CT_FILE_NAME}
	if [ $? -ne 0 ];then
        PRINT_LOG "����CT�ؼ���չ�ֵ������ʧ��"
        return 1
    fi

    #�����ʽ�ļ�
    cp ${INFO_PATH_TMP}/${QT_FILE_NAME} ${INFO_PATH_OUTPUT}/${QT_FILE_NAME}
    if [ $? -ne 0 ];then
       PRINT_LOG "����QT�ؼ���չ�ֵ������ʧ��"
       return 1
    fi
	cp ${INFO_PATH_TMP}/${CT_FILE_NAME} ${INFO_PATH_OUTPUT}/${CT_FILE_NAME}
    if [ $? -ne 0 ];then
       PRINT_LOG "����CT�ؼ���չ�ֵ������ʧ��"
       return 1
    fi
	
    cd ${INFO_PATH_OUTPUT}
    md5sum ${QT_FILE_NAME} > ${QT_FILE_NAME}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "����QT�ؼ���չ�ֵ������ʧ��"
       return 1
    fi
	md5sum ${CT_FILE_NAME} > ${CT_FILE_NAME}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "����CT�ؼ���չ�ֵ������ʧ��"
       return 1
    fi
}


#��QT��CT�ؼ���չ�����ݷ�ɢ��64�����ļ���
function devideSrchData()
{
   cd ${INFO_PATH_TMP}
   PRINT_LOG "��ѯȫ��ı���id"
   runsql_cap_read "select userid, userid mod 64 from beidoucap.useraccount"  "${BEIDOU_ID}"
   if [ $? -ne 0 ];then
        PRINT_LOG "��ѯȫ��ı���id�б�ʧ��"
        return 1
   fi
   
   
   PRINT_LOG "��CT�ؼ���չ�����ݷ�ɢ��64�����ļ���"
   rm -f ${CT_KEYWORD_SRCH}*
   awk -vprefix=${CT_KEYWORD_SRCH} 'ARGIND==1{
	   CT_INDEX[$1]=$2;
   } ARGIND==2{
       #userid planid groupid, keywordid,wordid, srch,clk,cost
       printf("%s\t%s\t%s\t%s\n",$1,$3,$4,$5) >> prefix CT_INDEX[$1];
   }' ${BEIDOU_ID}   ${CT_FILE_NAME}
   
   
   
   PRINT_LOG "��QT�ؼ���չ�����ݷ�ɢ��64�����ļ���"
   rm -f ${QT_KEYWORD_SRCH}*
   awk -vprefix=${QT_KEYWORD_SRCH} 'ARGIND==1{
	   QT_INDEX[$1]=$2;
   } ARGIND==2{
       #userid planid groupid, keywordid,wordid, srch,clk,cost
       printf("%s\t%s\t%s\t%s\n",$1,$3,$4,$5) >> prefix QT_INDEX[$1];
   }' ${BEIDOU_ID}   ${QT_FILE_NAME}
}


#�ϲ�QT�ؼ���չ�����ݵ���Ч����
function mergeToEffectQTKeywordData()
{
    cd ${INFO_PATH_TMP}
    rm -f ${QT_WORD_NUM}
    for((i=0;i<${QT_WORD_NUM_CONF}; i++))
	do
	touch ${QT_KEYWORD_SRCH}${i}
	touch ${QT_KEYWORD_EFFECT}${i}
	##userid  groupid keywordid,wordid
	PRINT_LOG "�����û�ά��QT�ؼ��������ؼ��������ļ�${i}"
    awk  -v v='#' 'ARGIND==1{
			key1=$1v$3v$4
			if(!(key1 in USER_KEYWORD)){
			    USER_KEYWORD[key1]
				COUNT_KEYWORD[$1]+=1;
			}
			key2=$1v$4
			if(!(key2 in USER_WORD)){
			    USER_WORD[key2]
				COUNT_WORD[$1]+=1;
			}
	    } ARGIND==2{
			key1=$1v$3v$4
			if(!(key1 in USER_KEYWORD)){
			    USER_KEYWORD[key1]
				COUNT_KEYWORD[$1]+=1;
			}
			key2=$1v$4
			if(!(key2 in USER_WORD)){
			    USER_WORD[key2]
				COUNT_WORD[$1]+=1;
			}
	    } END {
	      for(user in COUNT_KEYWORD){
		     printf("%s\t%s\t%s\n",user, COUNT_KEYWORD[user], COUNT_WORD[user]);
		  }
	    }' ${QT_KEYWORD_EFFECT}${i} ${QT_KEYWORD_SRCH}${i} >> ${QT_WORD_NUM}
	done;
}

#�ϲ�CT�ؼ���չ�����ݵ���Ч����
function mergeToEffectCTKeywordData()
{
    cd ${INFO_PATH_TMP}
    rm -f ${CT_WORD_NUM}
    for((i=0;i<${CT_WORD_NUM_CONF}; i++))
	do
	touch ${CT_KEYWORD_SRCH}${i}
	touch ${CT_KEYWORD_EFFECT}${i}
	PRINT_LOG "�����û�ά��CT�ؼ��������ؼ��������ļ�${i}"
    awk -v v='#' 'ARGIND==1{
			key1=$1v$3v$4
			if(!(key1 in USER_KEYWORD)){
			    USER_KEYWORD[key1]
				COUNT_KEYWORD[$1]+=1;
			}
			key2=$1v$4
			if(!(key2 in USER_WORD)){
			    USER_WORD[key2]
				COUNT_WORD[$1]+=1;
			}
	    } ARGIND==2{
			key1=$1v$3v$4
			if(!(key1 in USER_KEYWORD)){
			    USER_KEYWORD[key1]
				COUNT_KEYWORD[$1]+=1;
			}
			key2=$1v$4
			if(!(key2 in USER_WORD)){
			    USER_WORD[key2]
				COUNT_WORD[$1]+=1;
			}
	    } END {
	      for(user in COUNT_KEYWORD){
		     printf("%s\t%s\t%s\n",user, COUNT_KEYWORD[user], COUNT_WORD[user]);
		  }
	    }' ${CT_KEYWORD_EFFECT}${i} ${CT_KEYWORD_SRCH}${i} >> ${CT_WORD_NUM}
	done;
}


#�ϲ��ؼ������ݵ��û�ά��
function exportUserKeywordSum()
{
    cd ${INFO_PATH_TMP}   	
	#�����û�ά�ȵ�QT�ؼ���չ�ֵ������
	if [ ! -f ${QT_FILE_NAME} ]; then 
        PRINT_LOG "QT�ؼ���չ�ֵ���ļ�������"
	    return 1
    fi;
	if [ ! -f ${CT_FILE_NAME} ]; then 
        PRINT_LOG "CT�ؼ���չ�ֵ���ļ�������"
	    return 1
    fi;
	if [ ! -f ${LOCAL_EFFECT_USER_FINAL} ]; then 
        PRINT_LOG "��Ч�û��б��ļ�������"
	    return 1
    fi;
	if [ ! -f ${QT_WORD_NUM} ]; then 
        PRINT_LOG "�û�QT�ؼ���ͳ���ļ�������"
	    return 1
    fi;
	if [ ! -f ${CT_WORD_NUM} ]; then 
        PRINT_LOG "�û�CT�ؼ���ͳ���ļ�������"
	    return 1
    fi;
	#userid, planid, groupid, keywordid, wordid,srch,click,cost
	PRINT_LOG "����QT-��չ��-�ؼ�������"
	awk '{if($6>0){print $1"\t"$3"\t"$5}}' ${QT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${QT_USER_COUNT_KEYWORD_SRCH}
	if [ $? -ne 0 ];then
        PRINT_LOG "����QT-��չ��-�ؼ�������ʧ��"
        return 1
    fi
	PRINT_LOG "����QT-��չ��-�ؼ�������"
	awk '{if($6>0){print $1"\t"$5}}' ${QT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${QT_USER_COUNT_WORD_SRCH}
	if [ $? -ne 0 ];then
        PRINT_LOG "����QT-��չ��-�ؼ�������ʧ��"
        return 1
    fi
	PRINT_LOG "����QT-������-�ؼ�������"
	awk '{if($7>0){print $1"\t"$3"\t"$5}}' ${QT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${QT_USER_COUNT_KEYWORD_CLK}
	if [ $? -ne 0 ];then
        PRINT_LOG "����QT-������-�ؼ�������ʧ��"
        return 1
    fi
	awk '{if($7>0){print $1"\t"$5}}' ${QT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${QT_USER_COUNT_WORD_CLK}
	PRINT_LOG "����QT-������-�ؼ�������"
	if [ $? -ne 0 ];then
        PRINT_LOG "����QT-������-�ؼ�������ʧ��"
        return 1
    fi
	
	PRINT_LOG "����CT-��չ��-�ؼ�������"
	awk '{if($6>0){print $1"\t"$3"\t"$5}}' ${CT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${CT_USER_COUNT_KEYWORD_SRCH}
	if [ $? -ne 0 ];then
        PRINT_LOG "����CT-��չ��-�ؼ�������ʧ��"
        return 1
    fi
	PRINT_LOG "����CT-��չ��-�ؼ�������"
	awk '{if($6>0){print $1"\t"$5}}' ${CT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${CT_USER_COUNT_WORD_SRCH}
	if [ $? -ne 0 ];then
        PRINT_LOG "����CT-��չ��-�ؼ�������ʧ��"
        return 1
    fi
	PRINT_LOG "����CT-������-�ؼ�������"
	awk '{if($7>0){print $1"\t"$3"\t"$5}}' ${CT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${CT_USER_COUNT_KEYWORD_CLK}
	if [ $? -ne 0 ];then
        PRINT_LOG "����CT-������-�ؼ�������ʧ��"
        return 1
    fi
	PRINT_LOG "����CT-������-�ؼ�������"
	awk '{if($7>0){print $1"\t"$5}}' ${CT_FILE_NAME} | sort -k1n -k2n -u | awk '{print $1}' | uniq -c | awk '{print $2"\t"$1}' >  ${CT_USER_COUNT_WORD_CLK}
	if [ $? -ne 0 ];then
        PRINT_LOG "����CT-������-�ؼ�������ʧ��"
        return 1
    fi
	
	
	#���ɹؼ������ݣ�Keyword��
	#�������� userid, QT_ALL, QT_չ��, QT_����, CT_ALL, CT_չ�֣�CT_����
	PRINT_LOG "���ɹؼ������ݣ�Keyword��"
	awk 'ARGIND==1{
	    #userid
	    USER[$1];
		USER_QT_ALL[$1]=0;
		USER_QT_SRCH[$1]=0;
		USER_QT_CLKS[$1]=0;
		
		USER_CT_ALL[$1]=0;
		USER_CT_SRCH[$1]=0;
		USER_CT_CLKS[$1]=0;
	} ARGIND==2 {
	    #userid, keyword count, wordid count
	    USER_QT_ALL[$1]=$2;
	} ARGIND==3 {
	    #userid, keyword count
		USER_QT_SRCH[$1]=$2;
	} ARGIND==4 {
	    #userid, keyword count
		USER_QT_CLKS[$1]=$2;
	} ARGIND==5 {
	    #userid, keyword count, wordid count
	    USER_CT_ALL[$1]=$2;
	} ARGIND==6 {
	    #userid, keyword count
		USER_CT_SRCH[$1]=$2;
	} ARGIND==7 {
	    #userid, keyword count
		USER_CT_CLKS[$1]=$2;
	} END {
	    for(user in USER){
		    printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", user, USER_QT_ALL[user], USER_QT_SRCH[user],USER_QT_CLKS[user], USER_CT_ALL[user], USER_CT_SRCH[user], USER_CT_CLKS[user] );
		}
	} ' ${LOCAL_EFFECT_USER_FINAL} ${QT_WORD_NUM}  ${QT_USER_COUNT_KEYWORD_SRCH} ${QT_USER_COUNT_KEYWORD_CLK} ${CT_WORD_NUM} ${CT_USER_COUNT_KEYWORD_SRCH} ${CT_USER_COUNT_KEYWORD_CLK} > ${KEYWORD_FILE1}
	if [ $? -ne 0 ];then
        PRINT_LOG "���ɹؼ������ݣ�Keyword��ʧ��"
        return 1
    fi
	
	
	#���ɹؼ������ݣ�word��
	#�������� userid, QT_ALL, QT_չ��, QT_����, CT_ALL, CT_չ�֣�CT_����
	PRINT_LOG "���ɹؼ������ݣ�word��"
	awk 'ARGIND==1{
	    #userid
	    USER[$1];
		USER_QT_ALL[$1]=0;
		USER_QT_SRCH[$1]=0;
		USER_QT_CLKS[$1]=0;
		
		USER_CT_ALL[$1]=0;
		USER_CT_SRCH[$1]=0;
		USER_CT_CLKS[$1]=0;
	} ARGIND==2 {
	    #userid, keyword count, wordid count
	    USER_QT_ALL[$1]=$3;
	} ARGIND==3 {
	    #userid, keyword count
		USER_QT_SRCH[$1]=$2;
	} ARGIND==4 {
	    #userid, keyword count
		USER_QT_CLKS[$1]=$2;
	} ARGIND==5 {
	    #userid, keyword count, wordid count
	    USER_CT_ALL[$1]=$3;
	} ARGIND==6 {
	    #userid, keyword count
		USER_CT_SRCH[$1]=$2;
	} ARGIND==7 {
	    #userid, keyword count
		USER_CT_CLKS[$1]=$2;
	} END {
	    for(user in USER){
		    printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", user, USER_QT_ALL[user], USER_QT_SRCH[user],USER_QT_CLKS[user], USER_CT_ALL[user], USER_CT_SRCH[user], USER_CT_CLKS[user] );
		}
	} ' ${LOCAL_EFFECT_USER_FINAL} ${QT_WORD_NUM}  ${QT_USER_COUNT_WORD_SRCH} ${QT_USER_COUNT_WORD_CLK} ${CT_WORD_NUM} ${CT_USER_COUNT_WORD_SRCH} ${CT_USER_COUNT_WORD_CLK}  > ${KEYWORD_FILE2}
	if [ $? -ne 0 ];then
        PRINT_LOG "���ɹؼ������ݣ�word��ʧ��"
        return 1
    fi
	
	#�����ʽ�ļ�
    mv ${INFO_PATH_TMP}/${KEYWORD_FILE1} ${INFO_PATH_OUTPUT}/${KEYWORD_FILE1}
    if [ $? -ne 0 ];then
       PRINT_LOG "������Ч�ؼ��������ļ�ʧ��"
       return 1
    fi
	mv ${INFO_PATH_TMP}/${KEYWORD_FILE2} ${INFO_PATH_OUTPUT}/${KEYWORD_FILE2}
    if [ $? -ne 0 ];then
       PRINT_LOG "������Ч�ؼ��������ļ�ʧ��"
       return 1
    fi
    cd ${INFO_PATH_OUTPUT}
    md5sum ${KEYWORD_FILE1} > ${KEYWORD_FILE1}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "������Ч�ؼ��������ļ�ʧ��"
       return 1
    fi
	md5sum ${KEYWORD_FILE2} > ${KEYWORD_FILE2}.md5
    if [ $? -ne 0 ];then
       PRINT_LOG "������Ч�ؼ��������ļ�ʧ��"
       return 1
    fi
    
    #regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_KEYWORD_USER} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${KEYWORD_FILE1}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_KEYWORD_USER} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE1}
	noahdt add ${BEIDOU2NOVA_ADINFO_KEYWORD_USER_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE1}.md5
	alert $? "${msg}"
	
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_WORD_USER} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${KEYWORD_FILE2}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_WORD_USER} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE2}
	noahdt add ${BEIDOU2NOVA_ADINFO_WORD_USER_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE2}.md5
	alert $? "${msg}"
}

#�����ܼ���Ч�ؼ��ʣ��ؼ���
function exportTotalEffectKeywordSum()
{
    cd ${INFO_PATH_TMP}
    rm -f ${KEYWORD_FILE_TOTAL}.tmp
    #������Чqt_keyword, ֱ�Ӵӻ��ܵ�user���������
	awk '{sum+=$2}END{printf("qt_keyword_sum\t%s\n",sum)}' ${QT_WORD_NUM} >> ${KEYWORD_FILE_TOTAL}.tmp
	if [ $? -ne 0 ];then
       PRINT_LOG "����ȫ����ЧQT�ؼ�����Ŀʧ��"
       return 1
    fi
	#������չ��qt_keyword��qt_word, ֱ�Ӵ�չ�����������
	PRINT_LOG "������չ��qt_keyword��qt_word, ֱ�Ӵ�չ�����������"
	awk -v v='#' 'BEGIN{
	     KEYWORD_COUNT_SRCH=0;
		 KEYWORD_COUNT_CLKS=0;
		 WORD_COUNT_SRCH=0;
		 WORD_COUNT_CLKS=0;
	   } ARGIND=1{
	     key=$3v$5
	     if($6>0){
	           if(!(key in KEYWORD_SRCH)){
	               KEYWORD_SRCH[key];
	    	       KEYWORD_COUNT_SRCH+=1;
	           } 
		       if(!($5 in WORD_SRCH)){
	               WORD_SRCH[$5]
		           WORD_COUNT_SRCH+=1;
	           }
		    }
			
		 if($7>0){
	           if(!(key in KEYWORD_CLKS)){
	               KEYWORD_CLKS[key];
	    	       KEYWORD_COUNT_CLKS+=1;
	           } 
		       if(!($5 in WORD_CLKS)){
	               WORD_CLKS[$5]
		           WORD_COUNT_CLKS+=1;
	           }
		    }
	   } END {
	           printf("qt_keyword_srch\t%s\n",KEYWORD_COUNT_SRCH);
			   printf("qt_keyword_clks\t%s\n",KEYWORD_COUNT_CLKS);
		       printf("qt_word_srch\t%s\n",WORD_COUNT_SRCH);
		       printf("qt_word_clks\t%s\n",WORD_COUNT_CLKS);
	   } ' ${QT_FILE_NAME} >> ${KEYWORD_FILE_TOTAL}.tmp
	
	#������Чct_keyword, ֱ�Ӵӻ��ܵ�user���������
	awk '{sum+=$2}END{printf("ct_keysord_sum\t%s\n",sum)}' ${CT_WORD_NUM} >> ${KEYWORD_FILE_TOTAL}.tmp
	if [ $? -ne 0 ];then
       PRINT_LOG "����ȫ����ЧCT�ؼ�����Ŀʧ��"
       return 1
    fi
	#������չ��ct_keyword��ct_word, ֱ�Ӵ�չ�����������
	PRINT_LOG "������չ��ct_keyword��ct_word, ֱ�Ӵ�չ�����������"
	awk 'BEGIN{
	     KEYWORD_COUNT_SRCH=0;
		 KEYWORD_COUNT_CLKS=0;
		 WORD_COUNT_SRCH=0;
		 WORD_COUNT_CLKS=0;
	   } ARGIND=1{
	     key=$3v$5
	     if($6>0){
	           if(!(key in KEYWORD_SRCH)){
	               KEYWORD_SRCH[key];
	    	       KEYWORD_COUNT_SRCH+=1;
	           } 
		       if(!($5 in WORD_SRCH)){
	               WORD_SRCH[$5]
		           WORD_COUNT_SRCH+=1;
	           }
		    }
			
		 if($7>0){
	           if(!(key in KEYWORD_CLKS)){
	               KEYWORD_CLKS[key];
	    	       KEYWORD_COUNT_CLKS+=1;
	           } 
		       if(!($5 in WORD_CLKS)){
	               WORD_CLKS[$5]
		           WORD_COUNT_CLKS+=1;
	           }
		    }
	   } END {
	           printf("ct_keyword_srch\t%s\n",KEYWORD_COUNT_SRCH);
			   printf("ct_keyword_clks\t%s\n",KEYWORD_COUNT_CLKS);
		       printf("ct_word_srch\t%s\n",WORD_COUNT_SRCH);
		       printf("ct_word_clks\t%s\n",WORD_COUNT_CLKS);
	   } ' ${CT_FILE_NAME} >> ${KEYWORD_FILE_TOTAL}.tmp
	   
	   #����QT��Чword��������Ҫ��ȥ�ز���
	   PRINT_LOG "����QT��Чword��������Ҫ��ȥ�ز���"
	   #userid, groupid, keywordid, wordid
	   rm -f all_qt_word.data
	   for((i=0;i<${QT_WORD_NUM_CONF};i++))
	   do
	      awk '{if(!($4 in WORD)){
			      print $4;
				  WORD[$4];
			    }
			  }' ${QT_KEYWORD_EFFECT}${i} >> all_qt_word.data
	   done
	   
	   awk 'BEGIN{
	       WORD_COUNT=0;
	      }{
		      if(!($1 in WORD)){
				  WORD[$1];
				  WORD_COUNT+=1;
			    }
		      } END {
			  printf("qt_word_sum\t%s\n",WORD_COUNT);
		   }' all_qt_word.data >> ${KEYWORD_FILE_TOTAL}.tmp
	   

	   
	   #����CT��Чword��������Ҫ��ȥ�ز���
	   PRINT_LOG "����CT��Чword��������Ҫ��ȥ�ز���"
	   rm -f all_ct_word.data
	   for((i=0;i<${CT_WORD_NUM_CONF};i++))
	   do
	      awk '{if(!($4 in WORD)){
			      print $4;
				  WORD[$4];
			    }
			  }' ${CT_KEYWORD_EFFECT}${i} >> all_ct_word.data
	   done
	   
	   awk 'BEGIN{
	       WORD_COUNT=0;
	      }{
		      if(!($1 in WORD)){
				  WORD[$1];
				  WORD_COUNT+=1;
			    }
		      } END {
			  printf("ct_word_sum\t%s\n",WORD_COUNT);
		  }' all_ct_word.data >> ${KEYWORD_FILE_TOTAL}.tmp
		  
	  #����������ʽ�ļ�
	  awk '{
			  MAP[$1]=$2
		 } END {
		      printf("%s\t%s\t%s\t%s\t%s\t%s\n",MAP["qt_keyword_sum"], MAP["qt_keyword_srch"], MAP["qt_keyword_clks"], MAP["ct_keysord_sum"], MAP["ct_keyword_srch"], MAP["ct_keyword_clks"])
		 }' ${KEYWORD_FILE_TOTAL}.tmp > ${KEYWORD_FILE_TOTAL}
		 
	  awk '{
			  MAP[$1]=$2
		 } END {
		     printf("%s\t%s\t%s\t%s\t%s\t%s\n",MAP["qt_word_sum"], MAP["qt_word_srch"], MAP["qt_word_clks"], MAP["ct_word_sum"], MAP["ct_word_srch"], MAP["ct_word_clks"])
		 }' ${KEYWORD_FILE_TOTAL}.tmp > ${WORD_FILE_TOTAL}
      
	  mv ${INFO_PATH_TMP}/${KEYWORD_FILE_TOTAL} ${INFO_PATH_OUTPUT}/${KEYWORD_FILE_TOTAL}
      if [ $? -ne 0 ];then
       PRINT_LOG "�����ؼ�������Ϣʧ��"
       return 1
      fi
      cd ${INFO_PATH_OUTPUT}
      md5sum ${KEYWORD_FILE_TOTAL} > ${KEYWORD_FILE_TOTAL}.md5
	  
      #regist file to dts
	  msg="regist DTS for ${BEIDOU2NOVA_ADINFO_KEYWORD_ALL} failed."
	  md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${KEYWORD_FILE_TOTAL}.md5`
	  noahdt add ${BEIDOU2NOVA_ADINFO_KEYWORD_ALL} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE_TOTAL}
	  noahdt add ${BEIDOU2NOVA_ADINFO_KEYWORD_ALL_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${KEYWORD_FILE_TOTAL}.md5
	  alert $? "${msg}"
	  
	  mv ${INFO_PATH_TMP}/${WORD_FILE_TOTAL} ${INFO_PATH_OUTPUT}/${WORD_FILE_TOTAL}
      if [ $? -ne 0 ];then
       PRINT_LOG "�����ؼ�������Ϣʧ��"
       return 1
      fi
      cd ${INFO_PATH_OUTPUT}
      md5sum ${WORD_FILE_TOTAL} > ${WORD_FILE_TOTAL}.md5
      
      #regist file to dts
	  msg="regist DTS for ${BEIDOU2NOVA_ADINFO_WORD_ALL} failed."
	  md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/${WORD_FILE_TOTAL}.md5`
	  noahdt add ${BEIDOU2NOVA_ADINFO_WORD_ALL} -m md5=${md5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${WORD_FILE_TOTAL}
	  noahdt add ${BEIDOU2NOVA_ADINFO_WORD_ALL_MD5} -i date=${YESTERDAY} bscp://${INFO_PATH_OUTPUT}/${WORD_FILE_TOTAL}.md5
	  alert $? "${msg}"
}

function exportRealTimeEffPlan()
{
    filename=`date +%Y%m%d%H%M`
	rm -f ${INFO_PATH_TMP}/effPlan.data.tmp1
	for((i=0;i<${TAB_UNIT_SLICE};i++))
	do
	runsql_sharding_read "select a.uid, a.pid, a.gid from beidou.cprounitstate${i} a where [a.uid] and a.state=0 and a.pid in (select planid from beidou.cproplan where planstate=0 and budgetover=0)" ${INFO_PATH_TMP}/effPlan.data.tmp1.${i}
	if [ $? -ne 0 ];then
        PRINT_LOG "��Ч�����ߵļƻ�����ʧ��"
        return 1
    fi
	cat ${INFO_PATH_TMP}/effPlan.data.tmp1.${i} >> ${INFO_PATH_TMP}/effPlan.data.tmp1
	rm -f ${INFO_PATH_TMP}/effPlan.data.tmp1.${i}
	done
	
	
	runsql_sharding_read "select groupid from beidou.cprogroup a where groupstate=0 and [a.userid] and a.userid in (select userid from beidoucap.useraccount where ustate=0 and ushifenstatid in (2,3,6))" ${INFO_PATH_TMP}/effPlan.data.tmp2
	
	cd ${INFO_PATH_TMP}
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($3 in map){printf("%s\t%s\n",$1,$2)}}' effPlan.data.tmp2 effPlan.data.tmp1| sort -u > effPlan_${filename}
	
	mv effPlan_${filename} ${INFO_PATH_OUTPUT}/
	if [ $? -ne 0 ];then
       PRINT_LOG "����effPlanʧ��"
       return 1
      fi
	cd ${INFO_PATH_OUTPUT}
	md5sum effPlan_${filename} > effPlan_${filename}.md5 
	
	#regist file to dts
	msg="regist DTS for ${BEIDOU2NOVA_ADINFO_EFFPLAN} failed."
	md5=`getMd5FileMd5 ${INFO_PATH_OUTPUT}/effPlan_${filename}.md5`
	noahdt add ${BEIDOU2NOVA_ADINFO_EFFPLAN} -m md5=${md5} -i date=${filename} bscp://${INFO_PATH_OUTPUT}/effPlan_${filename}
	noahdt add ${BEIDOU2NOVA_ADINFO_EFFPLAN_MD5} -i date=${filename} bscp://${INFO_PATH_OUTPUT}/effPlan_${filename}.md5
	alert $? "${msg}"
}

check_conf
alert $? "Error Configuration"
check_path
alert $? "Error File Path"
PRINT_LOG "Operation Started"
if [ $# -lt 1 ]; then
    alert 1 "ȱ�ٲ���"
fi
cd ${INFO_PATH_TMP}


#param 1, export ad data and userVip data
if [ $1 -eq 1 ]; then
    PRINT_LOG "start to export ad data"
    exportAdDatFromDB
    PRINT_LOG "export User Vip Info(whether the user is vip)"
    op_with_retry "exportUserVipInfo" "export user info failed" $MAX_RETRY
	
fi



#�ű����ݲ���Ϊ2, ׼����Ч���ݣ�������ЧԤ��
if [ $1 -eq 2 ]; then
    PRINT_LOG "��ʼץȡչ�����ݣ���������Ч�������"
    op_with_retry "mergeValidData" "������Ч�������ʧ��" $MAX_RETRY
	PRINT_LOG "��ȡ������ЧԤ��������ļ�"
	op_with_retry "exportValidBudgetFiles" "��ȡ������ЧԤ��������ļ�ʧ��" $MAX_RETRY

fi


#param 3 prepare keyword data, effected keyword and user info
if [ $1 -eq 3 ]; then
   PRINT_LOG "export QT,CT keyword Data"
   op_with_retry "exportQTCTData" "exportQTCTData failed" $MAX_RETRY
   
   PRINT_LOG "export Effect CT, QT Keyword"
   op_with_retry "exportEffectKeyword" "exportEffectKeyword failed" $MAX_RETRY
   
   PRINT_LOG "device Qt, CT srch data to 64 sub files" 
   op_with_retry "devideSrchData" "devideSrchData failed" $MAX_RETRY
   
   #mergeToEffectQTKeywordData
   op_with_retry "mergeToEffectQTKeywordData" "mergeToEffectQTKeywordData failed" $MAX_RETRY
   
   #mergeToEffectCTKeywordData
   op_with_retry "mergeToEffectCTKeywordData" "mergeToEffectCTKeywordData failed" $MAX_RETRY
   
   PRINT_LOG "exportUserKeywordSum" 
   op_with_retry "exportUserKeywordSum" "exportUserKeywordSum failed" $MAX_RETRY
   
   #export Total Effect Keyword Sum
   op_with_retry "exportTotalEffectKeywordSum" "exportTotalEffectKeywordSum failed" $MAX_RETRY
fi



#param 4, export atom dictionary
if [ $1 -eq 4 ]; then
	exportAllCTData
    exportAllQTData
	exportAtomDictionary
fi

#�ű����ݲ���Ϊ5��׼��ʵʱ��Ч�����ߵļƻ�����
if [ $1 -eq 5 ]; then
	#ʵʱ��Ч�����ߵļƻ�����
	exportRealTimeEffPlan
fi

PRINT_LOG  "Operation Ended"
PRINT_LOG  ""
