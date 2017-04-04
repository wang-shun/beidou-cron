#!/bin/bash
#@file: mvRelatedPropsOfPlanOrGroupWithDeleted
#@author: heixufeng
#@date: 2014-02-10
#@version: 1.0.0.0
#@parameter: one parameter, indicate mode, 1 is init, 0 is daily
#@brief: mv keyword,site .. of deleted plan or group to cold table

if [ $# -eq 0 ]
then
   echo "you must input MODE parameter.1 is init, 0 is daily"
   exit 1
fi

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh 
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH="../lib/beidou_lib.sh"

[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH=../conf/mvRelatedPropsOfPlanOrGroupWithDeleted.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=mvRelatedPropsOfPlanOrGroupWithDeleted.sh
reader_list=hexiufeng
MODE=$1
NEXT=
SLEEPS=0

function check_conf()
{
    if ! [[ $PROTECTDAYS ]]
    then
        echo "Conf[PROTECTDAYS] is empty or its value is invalid"
        return 1
    fi
    if ! [[ $DELAYHOURS ]]
    then
        echo "Conf[DELAYHOURS] is empty or its value is invalid"
        return 1
    fi
    return 0
}

function check_path()
{
    if ! [ -e $INFO_PATH_TMP ]
    then
         mkdir -p $INFO_PATH_TMP
         if [ $? -ne 0 ]
           then
           echo "Fail to mkdir [$INFO_PATH_TMP]!" >> $LOG_FILE
           return 1
         fi
    fi
    return 0    
}

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
    
    echo "[${timeNow}]${1}"
    
    echo "[${timeNow}]${1}" >> ${LOG_FILE}
}
# 读取non deleted group of deleted plan to file，输出到group db index file
# params:
#       $1--datelimit, 该日期之前删除的plan
function outallgroup(){
    local dt=$1
    local lastindex=$[$SHARDING_SLICE - 1]
    for((idx=0;idx<=$lastindex;idx++));do
        local pgfile=plangroup${idx}
        local ggfile=groupgroup${idx}
        local groupfile=${GROUPPREFIX}${idx}
        local sql="select g.groupid,g.userid from beidou.cprogroup g inner join beidou.plandelinfo pd on g.planid=pd.planid and g.groupstate!=2 where pd.deltime<='${dt}'"
        
        PRINT_LOG "$sql"
        
        runsql_single_read "$sql" "$pgfile" $idx
        
        sql="select g.groupid,g.userid from beidou.cprogroup g inner join beidou.groupdelinfo gd on g.groupid=gd.groupid  where gd.deltime<='${dt}'"
        
        PRINT_LOG "$sql"
        
        runsql_single_read "$sql" "$ggfile" $idx
        
        cat "$pgfile" "$ggfile" > "$groupfile"
        
        rm -f "$pgfile" 
        rm -f "$ggfile"
    done
}


# 读取所有0-63关键词表中的关键词,输入到kw db index file
#  $1 --db index
#  $2 file name
function outkeyword(){
    local idx=$1
    local filename=$2
    local lasttable=$[$KWSLICE -1]
    for((tidx=0;tidx<=$lasttable;tidx++));do
        PRINT_LOG "select groupid from beidou.cprokeyword${tidx}"
        runsql_single_read "select groupid from beidou.cprokeyword${tidx}" "${filename}${idx}_${tidx}.otmp" $idx
        awk '{group[$1]=group[$1]+1} 
            END {
                for(id in group)
                {
                    print id,group[id]
                }
            }' "${filename}${idx}_${tidx}.otmp" > "${filename}${idx}_${tidx}.slice"
        rm -f "${filename}${idx}_${tidx}.otmp"
    done
    
    cat `ls ${filename}${idx}_*.slice` > "${filename}${idx}"
    
    rm -f `ls ${filename}${idx}_*.slice`
}


# 读取所有0-7 vt表,输入到vt db index file
#  $1 --db index
#  $2 file name
function outvt(){
    local idx=$1
    local filename=$2
  
  
    PRINT_LOG "select groupid from beidou.cprogroupvt"

    runsql_single_read "select groupid from beidou.cprogroupvt" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 读取it表,输入到it db index file
#  $1 --db index
#  $2 file name
function outit(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.cprogroupit"

    runsql_single_read "select groupid from beidou.cprogroupit" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}
# 读取group_pack表group_pack db index file
#  $1 --db index
#  $2 file name
function outgp(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.group_pack"

    runsql_single_read "select groupid from beidou.group_pack" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}
# 读取groupit exclude 表,输入到groupit exclude db index file
#  $1 --db index
#  $2 file name
function outitexcl(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.cprogroupit_exclude"

    runsql_single_read "select groupid from beidou.cprogroupit_exclude" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 读取word pack exclude 表,输入到word pack exclude db index file
#  $1 --db index
#  $2 file name
function outwordpackexcl(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.word_pack_exclude"

    runsql_single_read "select groupid from beidou.word_pack_exclude" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 读取word  exclude 表,输入到word  exclude db index file
#  $1 --db index
#  $2 file name
function outwordexcl(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.word_exclude"

    runsql_single_read "select groupid from beidou.word_exclude" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}
# 读取ip filter 表,输入到ip filter db index file
#  $1 --db index
#  $2 file name
function outipfilter(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.groupipfilter"

    runsql_single_read "select groupid from beidou.groupipfilter" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 读取site filter 表,输入到site filter db index file
#  $1 --db index
#  $2 --file name
function outsitefilter(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.groupsitefilter"

    runsql_single_read "select groupid from beidou.groupsitefilter" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 读取app exclude 表,输入到app exclude db index file
#  $1 --db index
#  $2 file name
function outappexcl(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.app_exclude"

    runsql_single_read "select groupid from beidou.app_exclude" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}
# 读取group info 表,输入到group info db index file
#  $1 --db index
#  $2 file name
function outgroupinfo(){
    local idx=$1
    local filename=$2
    PRINT_LOG "select groupid from beidou.cprogroupinfo where (reglist is not null and reglist !='') or (sitetradelist is not null and sitetradelist!='') or (sitelist is not null and sitelist != '')"

    runsql_single_read "select groupid from beidou.cprogroupinfo where (reglist is not null and reglist !='') or (sitetradelist is not null and sitetradelist!='') or (sitelist is not null and sitelist != '')" "${filename}${idx}.otmp" $idx
    awk '{group[$1]=group[$1]+1} 
        END {
            for(id in group){
                print id,group[id]
            }
        }' "${filename}${idx}.otmp" > "${filename}${idx}"
    rm -f "${filename}${idx}.otmp"
}

# 转移数据函数主体,先调用shell输出数据到文件,再调用java具体执行
#  $1 file name
#  $2 out function name 
#  $3 delay times
#  $4 max count for delay
function process(){
    local filename=$1
    local funname=$2
    local delay=$3
    local maxcount=$4
    
    local lastindex=$[$SHARDING_SLICE - 1]
    for ((idx=0;idx<=$lastindex;idx++));do
        "$funname" $idx "${filename}"
        awk 'ARGIND==1{map[$1]=$2}ARGIND==2{
            if($1 in map)
            {
                print $1,$2,map[$1]
            }
        }' "${GROUPPREFIX}${idx}" "${filename}${idx}" > "${filename}${idx}.tmp"
        
        mv -f "${filename}${idx}.tmp"  "${filename}${idx}"
        # call java 
        java -Xms256m -Xmx1024m -classpath ${CUR_CLASSPATH} -Dshrink.dbindex=$idx -Dshrink.table=$filename com.baidu.beidou.shrink.ShrinkExecutor "${INFO_PATH_TMP}/${filename}${idx}" ${delay} ${maxcount} >/dev/null 
        
        sleep 15s
        #&
        #echo $!>"${filename}${idx}.pid" 
    done
    # wait finish
    #wait
    #rm -f `ls ${filename}*.pid`
    # remove keyword file
    #rm -f `ls ${filename}*`
}


function getSleep()
{
    local lh=16
    local sh=11
    local nft=`date +"%F"`
    local nt=`date +"%s"`
    wt=(`date +"%a %H"`)
    local da=0
    if [ ${wt[0]} = "Fri" -a ${wt[1]} -ge $lh ]
    then
       da=3
    fi
    if [ ${wt[0]} = "Sat" ]
    then
        da=2
    fi
    if [ ${wt[0]} = "Sun" -o ${wt[1]} -ge $lh ]
    then
       da=1
    fi
    if [ $da -eq 0 -a ${wt[1]} -ge $lh ]
    then
       da=1
    fi
    if [ $da -eq 0 -a ${wt[1]} -gt 10 ]
    then
        sh=$[ ${wt[1]} + 2 ]
    fi
   
    NEXT=`date -d"-${da} day ${nft} ${sh}:00:00" +"%Y-%m-%d %H:%M:%S"`
    SLEEPS=$[ `date +"%s" -d"${NEXT}"` - $nt - 300 ]
}

# 处理完成某一类型数据时等待和mail通知
# $1 将处理的数据
function waitAndNotify()
{
   if [ $MODE -eq 0 ]
   then
       sleep 10s
   else
       getSleep
       PRINT_LOG "[${1}] will be processed at [${NEXT}], email to ${MAIL_LIST}"
       SendMail "[${1}] will be processed at [${NEXT}]" "${MAIL_LIST}"
       sleep ${SLEEPS}s
       SendMail "[${1}] will be processed after 5 minutes" "${MAIL_LIST}"
       sleep 300s
   fi
}

# just for debug
function enabledebug(){
    DB_PASSWORD_BD_MAID_READ=beidoudb
    DB_USER_BD_MAID_READ=beidoudb
    DB_HOST_BD_MAID_READ=(10.48.54.19 10.48.54.19 10.26.85.30 10.26.85.30 10.26.85.30 10.48.54.19 10.48.54.19 10.48.54.19)
    DB_PORT_BD_MAID_READ=(3302 3303 3304 3305 3306 3307 3308 3309)
    #SHARDING_SLICE=4
}

# start to run
hours=$[$PROTECTDAYS * 24 + $DELAYHOURS]
CURTIME=`date -d "${hours} hours ago" +"${FMT}"`

check_conf
alert $? "Error Configuration"
check_path
alert $? "Error File Path"
PRINT_LOG "Operation Started"

cd "${INFO_PATH_TMP}"



# for debug
#enabledebug


# read all group from 0-7 db
outallgroup "$CURTIME"

# mv all keywords
process "${KWPREFIX}" "outkeyword" "${KWDELAY}" "${KWMAX}"

waitAndNotify "${SITEFILTERPREFIX}"
# mv sitefilter
process "${SITEFILTERPREFIX}" "outsitefilter" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${GINFOPREFIX}"
# mv cprogroupinfo
process "${GINFOPREFIX}" "outgroupinfo" "${GINFODELAY}" "${GINFOMAX}"

waitAndNotify "${ITPREFIX}"
# mv cprogroupit
process "${ITPREFIX}" "outit" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${VTPREFIX}"
#mv vt
process "${VTPREFIX}" "outvt" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${GPPREFIX}"
# mv group_pack
process "${GPPREFIX}" "outgp" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${ITEXCLPREFIX}"
# mv group it exclude
process "${ITEXCLPREFIX}" "outitexcl" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${WDPACKEXCLPREFIX}"
#mv work pack exclude
process "${WDPACKEXCLPREFIX}" "outwordpackexcl" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${WDEXCLPREFIX}"
# mv word exclude
process "${WDEXCLPREFIX}" "outwordexcl" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${IPFILTERPREFIX}"
# mv ip filter
process "${IPFILTERPREFIX}" "outipfilter" "${DEFDELAY}" "${DEFMAX}"

waitAndNotify "${APPEXCLPREFIX}"
# mv app exclude
process "${APPEXCLPREFIX}" "outappexcl" "${DEFDELAY}" "${DEFMAX}"

#remove group files
#rm -f `ls ${GROUPPREFIX}*`

if [ $MODE -ne 0 ]
then
    SendMail "all data has been inited." "${MAIL_LIST}"
fi

PRINT_LOG "finish"