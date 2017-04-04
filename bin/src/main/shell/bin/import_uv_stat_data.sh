#!/bin/bash

#@file: import_uv_stat_data.sh
#@author: genglei
#@date: 2012-09-05
#@version: 1.0.0.0
#@brief: download uv stat data and import into database

CONF_FILE="../conf/import_uv_stat_data.conf"
[ -f "${CONF_FILE}" ] && source ${CONF_FILE} || echo "ERROR: not exist ${CONF_FILE}"

LIB_FILE="./beidou_lib.sh"
[ -f "${LIB_FILE}" ] && source ${LIB_FILE} || echo "ERROR: not exist ${LIB_FILE}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_param()
{
    param=$1
    if ! [[ $param ]]
    then
        echo "Conf[$param] is empty or its value is invalid"
        return 1
    fi
}   

function check_conf()
{
    check_param $GENDER_FILETYPE || return 1
    check_param $INTEREST_FILETYPE || return 1
    check_param $DATA_PREFIX || return 1
    check_param $MD5_PREFIX || return 1
    check_param $MAX_RETRY || return 1
    
    check_param $MAIL_LIST || return 1

    check_param $LOCAL_TEMP || return 1
    check_param $LOCAL_DEST || return 1
    check_param $INTEREST_FILE || return 1
    check_param $GENDER_FILE || return 1

    check_param $LOG_PATH || return 1
    check_param $LOG_NAME || return 1
}

function check_all_path()
{
    if ! [ -w $LOCAL_TEMP ]
    then
        mkdir -p $LOCAL_TEMP
        if [ $? -ne 0 ]
        then
            log "FATAL" "Fail to mkdir [$LOCAL_TEMP]!"
            return 1
        fi
    fi

    if ! [ -w $LOCAL_DEST ]
    then
        mkdir -p $LOCAL_DEST
        if [ $? -ne 0 ]
        then
            log "FATAL" "Fail to mkdir [$LOCAL_DEST]!"
            return 1
        fi
    fi
    
    return 0
}

function download_uv_data()
{
    curDate=$1

    # download interest uv stat data
    statFileName=${INTEREST_FILE}
    statTmpFile=${LOCAL_TEMP}/${statFileName}
    statDestFile=${LOCAL_DEST}/${statFileName}.${curDate}
    wget --limit-rate=30M -t $MAX_RETRY -q "${DATA_PREFIX}&date=${curDate}&item=${INTEREST_FILETYPE}" -O $statTmpFile
    if [ $? -ne 0 ] || ! [ -f $statTmpFile ]
    then
        log "FATAL" "Fail to download statfile for interest[$statTmpFile]!"
        SendMail "import_uv_stat_data: Fail to download uv stat data file for interest." "${MAIL_LIST}"
        return 1
    fi

    wget --limit-rate=30M -t $MAX_RETRY -q "${MD5_PREFIX}&date=${curDate}&item=${INTEREST_FILETYPE}" -O $statTmpFile".md5.tmp"
    if [ $? -ne 0 ] || ! [ -f $statTmpFile".md5.tmp" ]
    then
        log "FATAL" "Fail to download md5file for interest[$statTmpFile".md5"]!"
        SendMail "import_uv_stat_data: Fail to download uv stat md5 file for interest." "${MAIL_LIST}"
        return 1
    fi

    awk -vfname="$statFileName" '{print $2 "  " fname}' $statTmpFile".md5.tmp" > $statTmpFile".md5"
    rm $statTmpFile".md5.tmp"

    cd $LOCAL_TEMP
    md5sum -c $statTmpFile".md5" > /dev/null
    if [ $? -ne 0 ]
    then
        log "FATAL" "Fail to check statfile md5 for interest[$statTmpFile.md5]!"
        SendMail "import_uv_stat_data: Fail to check md5 for statfile[interest]." "${MAIL_LIST}"
        return 1
    fi
    # 1: gender, 2: interest
    awk -F"\t" '{print 2"\t"$0;}' $statTmpFile > $statDestFile

    
    # download gender uv statdata
    statFileName=${GENDER_FILE}
    statTmpFile=${LOCAL_TEMP}/${statFileName}
    statDestFile=${LOCAL_DEST}/${statFileName}.${curDate}
    wget --limit-rate=30M -t $MAX_RETRY -q "${DATA_PREFIX}&date=${curDate}&item=${GENDER_FILETYPE}" -O $statTmpFile
    if [ $? -ne 0 ] || ! [ -f $statTmpFile ]
    then
        log "FATAL" "Fail to download statfile for gender[$statTmpFile]!"
        SendMail "import_uv_stat_data: Fail to download uv stat data file for gender." "${MAIL_LIST}"
        return 1
    fi

    wget --limit-rate=30M -t $MAX_RETRY -q "${MD5_PREFIX}&date=${curDate}&item=${GENDER_FILETYPE}" -O $statTmpFile".md5.tmp"
    if [ $? -ne 0 ] || ! [ -f $statTmpFile".md5.tmp" ]
    then
        log "FATAL" "Fail to download md5file for gender[$statTmpFile".md5"]!"
        SendMail "import_uv_stat_data: Fail to download uv stat md5 file for gender." "${MAIL_LIST}"
        return 1
    fi

    awk -vfname="$statFileName" '{print $2 "  " fname}' $statTmpFile".md5.tmp" > $statTmpFile".md5"
    rm $statTmpFile".md5.tmp"

    cd $LOCAL_TEMP
    md5sum -c $statTmpFile".md5" > /dev/null
    if [ $? -ne 0 ]
    then
        log "FATAL" "Fail to check statfile md5 for gender[$statTmpFile.md5]!"
        SendMail "import_uv_stat_data: Fail to check md5 for statfile[gender]." "${MAIL_LIST}"
        return 1
    fi
    # 1: gender, 2: interest
    awk -F"\t" '{print 1"\t"$0;}' $statTmpFile > $statDestFile

    return 0
}

function loaddata_db()
{
    curDate=$1
    
    interestFile=${LOCAL_DEST}/${INTEREST_FILE}.${curDate}
    genderFile=${LOCAL_DEST}/${GENDER_FILE}.${curDate}

    if ! [ -f $interestFile ] || ! [ -f $interestFile ]
    then
        log "ERROR" "not exist: interest statfile && gender statfile"
        SendMail "import_uv_stat_data: not exist statfile[interest && gender]." "${MAIL_LIST}"
        return 1
    fi
    
    # clear all data
    runsql_xdb "use beidoureport; delete from stat_all_uv;"
    if [ $? -ne 0 ]
    then
        log "ERROR" "Fail to clear interest && gender uv stat data"
        SendMail "import_uv_stat_data: Fail to clear uv stat data[interest && gender]." "${MAIL_LIST}"
        return 1
    fi

    # load gender data into db
    runsql_xdb "use beidoureport; load data local infile '${genderFile}' into table stat_all_uv;"
    if [ $? -ne 0 ]
    then
        log "ERROR" "Fail to load gender uv stat data into database"
        SendMail "import_uv_stat_data: Fail to load gender uv stat data into database." "${MAIL_LIST}"
        return 1
    fi
    

    # load interest data into db
    runsql_xdb "use beidoureport; load data local infile '${interestFile}' into table stat_all_uv"
    
    if [ $? -ne 0 ]
    then
        log "ERROR" "Fail to load interest uv stat data into database"
        SendMail "import_uv_stat_data: Fail to load interest uv stat data into database." "${MAIL_LIST}"
        return 1
    fi

    return 0
}

function main()
{
    check_conf || return 1

    open_log

    check_all_path || return 1

    if [ $1 ];then
        format=`echo $1|grep "[0-9]\{4\}[0,1][0-9][0-3][0-9]"`
        if [ ${#1} -ne 8 ]||[ "${format}" != "$1" ];then
            log "ERROR" "param error: $1 format YYYYMMDD"
            return 1
        fi
        curDate=$1
    else
        curDate=`date -d "1 day ago" +%Y%m%d`
    fi

    download_uv_data ${curDate} || return 1

    loaddata_db ${curDate} || return 1
}


# main process
if [ $# -eq 0 ]
then
    main
    exit $?
else
    if [ $# -eq 1 ]
    then
        main $1
    else
        echo "ERROR: more than 1 params"
    fi
fi

