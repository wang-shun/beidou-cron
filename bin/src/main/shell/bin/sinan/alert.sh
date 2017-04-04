#!/bin/sh

#报警接收人
MAILLIST="zhangxu04@baidu.com"
NOTIFY_MAILLIST="zhangxu04@baidu.com"
MOBILELIST="13581757961"

type="ERROR"
module=beidou-cron-sinan
program=formateSinanData.sh
reader_list=zhangxu

#${title}=[${type}][${module}]${msg}@${timestamp}
#${body1}=${program}:${msg}
#${body2}=${error_msg}

#ifError function
# $1 – 上一个命令的返回值，对应$?
# $2 - ${title}字段，标题信息
# $3 - ${body1}字段，简要错误信息
# $4 - ${body2}字段，补充错误信息
# $5 - ${reader-list}，关注用户列表
ifError() {
    if [ $# -lt 5 ]	
    then
        return
    fi
    if [ $1 -ne 0 ]
    then
        hit "$2" "$3" "$4" "$5" 
    fi
}

ifError_return() {
    if [ $# -lt 5 ]	
    then
        return
    fi
    if [ $1 -ne 0 ]
    then
        hit_return "$2" "$3" "$4" "$5" 
    fi
}

ifNotify_return() {
    if [ $# -lt 5 ]	
    then
        return
    fi
    if [ $1 -ne 0 ]
    then
        notify_return "$2" "$3" "$4" "$5" 
    fi
}

#hit function
# $1 - ${title}字段，标题信息
# $2 - ${body1}字段，简要错误信息
# $3 - ${body2}字段，补充错误信息
# $4 - ${reader-list}，关注用户列表
hit() {
    echo -e $1 
    echo -e "$2 | $3 | $4"|/bin/mail -s "$1" $MAILLIST
    for mobile in $MOBILELIST
    do
        /bin/gsmsend -s emp01.baidu.com:15003 -s emp02.baidu.com:15003 $mobile@"$1 | $2 | `hostname`"
    done
    exit 1
}

hit_return() {
    echo -e $1 
    echo -e "$2 | $3 | $4"|/bin/mail -s "$1" $MAILLIST
    for mobile in $MOBILELIST
    do
        /bin/gsmsend -s emp01.baidu.com:15003 -s emp02.baidu.com:15003 $mobile@"$1 | $2 | `hostname`"
    done
    return 1
}

notify_return(){
    echo -e $1 
    echo -e "$2 | $3 | $4"|/bin/mail -s "$1" $NOTIFY_MAILLIST
    return 1
}

alert() {
    if [ $# -lt 2 ]
    then
        return
    fi
    ifError $1 "[${type}][${module}]$2@`date +%F\ %T`" "${program}" \
            "$2" "${reader_list}"
}

alert_return() {
    if [ $# -lt 2 ]
    then
        return
    fi
    ifError_return $1 "[${type}][${module}]$2@`date +%F\ %T`" "${program}" \
            "$2" "${reader_list}"
}

notify() {
    ifNotify_return 1 "[MSG][WM123-SINAN]$1@`date +%F\ %T`" "${program}" \
            "$1" "${reader_list}"
}

