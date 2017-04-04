#!/bin/bash

#@file:whiteListUserUpdate.sh
#@author:wangxiongjie
#@date:2013-04-09
#@version:1.0.0.0
#@brief:update beidouext.whitelist,use new user list file,you should update WHITE_TYPE and ADD_TYPE in whiteListUserUpdate.conf first
################## @usage: sh whiteListUserUpdate.sh  useridFile
################## useridFile is a file dir with userids in this file

program=whiteListUserUpdate.sh
reader_list=wangxiongjie

source whiteListUserUpdate.conf;

# delete whiteuserid by whitetype
function deleteOldWhiteListUser()
{
    delsql=" delete from beidoucap.whitelist where type = ${WHITE_TYPE} ;"
    ${DB_CLIENT} -u${DB_USER} -p${DB_PWD} -h${HOST} beidoucap -P${DB_PORT} --default-character-set=gbk --skip-column-names -e "${delsql}"
    echo "delete old whitelist user done"
}

# backup whiteuserid by whitetype
function backupOldWhiteListUser()
{
    selsql="select id from beidoucap.whitelist where type = ${WHITE_TYPE} ;"
    ${DB_CLIENT} -u${DB_USER} -p${DB_PWD} -h${HOST} beidoucap -P${DB_PORT} --default-character-set=gbk --skip-column-names -e "${selsql}" > WhiteListUserBackup`date +'%y%m%d_%H%M%S'`.txt
}

# insert record into beidouext.whitelist
# arguments 1: useridFile
function importNewWhiteListUser()
{
    while read line
    do
        insertsql="insert into beidoucap.whitelist (type, id) values (${WHITE_TYPE}, ${line}) ;"
        ${DB_CLIENT} -u${DB_USER} -p${DB_PWD} -h${HOST} beidoucap -P${DB_PORT}  --default-character-set=gbk --skip-column-names -e "${insertsql}"
    done < ${1}
}

if [ $# -ne 1 ]
then
    echo "Wrong arguments input!"
    echo "Please read script usage!"
    exit 1
fi

backupOldWhiteListUser

if [ ${ADD_TYPE} = "REPLACE" ]
then
    deleteOldWhiteListUser
fi

importNewWhiteListUser ${1}

echo "update white list done!" 
