#!/bin/bash
#@file: clearReadFlag.sh
#@author: genglei
#@date: 2013-02-25
#@version: 1.0.0.0
#@brief: clear read flag in table beidouext.user_info_remind 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=clearReadFlag.sh
reader_list=genglei

function clearReadFlag()
{
	runsql_xdb "use beidouext; update user_info_remind set read_flag=0;"
	alert $? "clearReadFlag: fail to update the read flag in table beidouext.user_info_remind"
	return 0;
}

clearReadFlag
alert $? "clearReadFlag: error occurs during running"
exit 0
