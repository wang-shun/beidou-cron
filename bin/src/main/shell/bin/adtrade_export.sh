#!/bin/bash

#@file: adtrade_export.sh
#@author: yanjie
#@date: 2009-04-22
#@version: 1.0.0.0
#@brief: export some ads to sf-herring to be classified
#zero argument:  incremental export
#one or more arguments (whatever): full export
#@modify: wangchongjie since 2012.12.10 for cpweb525

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=adtrade_export.sh
reader_list=zhangpeng

CONF_SH="${CONF_PATH}/adtrade_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!"
	exit 1
fi

function check_path()
{
	if ! [ -w $EXPORT_PATH ]
	then
		if ! [ -e $EXPORT_PATH ]
		then
			mkdir -p $EXPORT_PATH
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$EXPORT_PATH]!"
				return 1
			fi
		else
			log "FATAL" "Path[$EXPORT_PATH] is not writable!"
			return 1
		fi
	fi

	if ! [ -w $BACKUP_PATH ]
	then
		if ! [ -e $BACKUP_PATH ]
		then
			mkdir -p $BACKUP_PATH
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$BACKUP_PATH]!"
				return 1
			fi
		else
			log "FATAL" "Path[$BACKUP_PATH] is not writable!"
			return 1
		fi
	fi
	
	return 0	
}


# arguments 1: the file path ouputed
function alltable_export()
{
		TMPFILE=${EXPORT_PATH}/${TMP_SUBTABLE_FILE}
		
		SQL="select s.id, s.targetUrl, t.gid, t.pid, t.uid, IF(s.title='', 'NULL', s.title), IF(s.description1='', 'NULL', s.description1), IF(s.description2='', 'NULL', s.description2) from beidou.cprounitstate? t, beidou.cprounitmater? s where t.id = s.id and s.adtradeid=0 and [t.uid]";
		runsql_sharding_read "$SQL" $TMPFILE $TAB_UNIT_SLICE
		
		if [ $? -ne 0 ]
		then
			log "FATAL" "adtrade_export.sh : cprounitmater? export ERROR."
			return 1
		fi

		if [[ -s ${TMPFILE} ]]
		then
			awk 'BEGIN{ORS=""; print "update beidou.cprounitstate? s, beidou.cprounitmater? m set s.helpstatus=s.helpstatus|4 where  s.helpstatus<>s.helpstatus|4  and s.id in (";}
			{if(NR>1){print ",";}print $1;}
			END{print ") and s.id=m.id and m.adtradeid=0 and [s.uid]";}' ${TMPFILE} > ${TMPFILE}.sql
			if [ $? -ne 0 ]
			then
				log "FATAL" "adtrade_export.sh : cprounitmater? cut id ERROR."
				return 1
			fi
		
			runfilesql_sharding "${TMPFILE}.sql" $TAB_UNIT_SLICE
			if [ $? -ne 0 ]
			then
				log "FATAL" "adtrade_export.sh : cprounitmater? update helpstatus ERROR."
				return 1
			fi
		
			cat ${TMPFILE} >> $1
			if [ $? -ne 0 ]
			then
				log "FATAL" "adtrade_export.sh : cprounitmater? merge ERROR."
				return 1
			else
				return 0
			fi
		fi
}


# arguments 1: the file path ouputed
function alltable_export_full()
{
		SQL="select s.id, s.targetUrl, t.gid, t.pid, t.uid, IF(s.title='', 'NULL', s.title), IF(s.description1='', 'NULL', s.description1), IF(s.description2='', 'NULL', s.description2) from beidou.cprounitstate? t, beidou.cprounitmater? s where t.id = s.id and t.state<>2 and [t.uid]";
		runsql_sharding_read "$SQL" $1 $TAB_UNIT_SLICE
		
		if [ $? -ne 0 ]
		then
			log "FATAL" "adtrade_export.sh : cprounitmater? export ERROR."
			return 1
		else
			return 0
		fi
}

#incremental export
function adtrade_export()
{
		
	open_log
	
	check_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeNow=`date +%Y%m%d%H%M`
	
	returnFlag=0
	
	outFile=$EXPORT_PATH/$EXPORT_FILE
	
	if [ -f $outFile ]
	then
		# modified by genglei01, fix export & import bug(import all the adtrade)
		cp $outFile $BACKUP_PATH/$EXPORT_FILE.$timeNow
		# modified by kanghongwei for task migrate
		#mv $outFile $outFile.export
		rm ${outFile}
		log "TRACE" "Suc to backup $outFile"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE.$timeNow > $EXPORT_FILE.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for incremental backup"
			SendMail "adtrade: Fail to generate md5 for incremental backup." "${MAILLIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
	fi
	if [ -f $outFile.md5 ]
	then
		rm $outFile.md5
	fi


	cd $BIN_PATH
		
	alltable_export $outFile
	if [ $? -ne 0 ]
	then
		log "FATAL" "Incremental Export Error."
		SendMail "adtrade: Incremental Export Error." "${MAILLIST}"
		returnFlag=1
	else
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		if [ ! -f $EXPORT_FILE ]
		then
			touch $EXPORT_FILE
		fi
		md5sum $EXPORT_FILE > $EXPORT_FILE.md5
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for incremental export."
			SendMail "adtrade: Fail to generate md5 for incremental export." "${MAILLIST}"
			returnFlag=1
		else
			log "TRACE" "Suc to generate md5."
		fi		
	fi
	
	#backup current beidouad.txt for the same period adtrade_import.sh(modified by kanghongwei for task migrate)
	cp $EXPORT_PATH/$EXPORT_FILE $EXPORT_PATH/$EXPORT_FILE.export

	cd $BIN_PATH
	
	close_log $returnFlag
	
	return $returnFlag
}


#full export
function adtrade_export_full()
{
	
	open_log
	
	check_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeNowFull=`date +%Y%m%d%H%M`
	
	returnFlag=0
	
	outFileFull=$EXPORT_PATH/$FULL_EXPORT_FILE
	
	if [ -f $outFileFull ]
	then
		mv $outFileFull $BACKUP_PATH/$FULL_EXPORT_FILE.$timeNowFull
		log "TRACE" "Suc to backup $outFileFull"

		cd $BACKUP_PATH
		md5sum $FULL_EXPORT_FILE.$timeNowFull > $FULL_EXPORT_FILE.$timeNowFull.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for full backup"
			SendMail "adtrade: Fail to generate md5 for full backup." "${MAILLIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
	fi
	if [ -f $outFileFull.md5 ]
	then
		rm $outFileFull.md5
	fi


	cd $BIN_PATH
	
	SQL="update beidou.cprounitstate? s, beidou.cprogroup g, beidou.cproplan p, beidoucap.useraccount u force index(IN_USERACCOUNT_USERID) set s.helpstatus=s.helpstatus|4 where s.helpstatus<>s.helpstatus|4 and s.gid=g.groupid and s.pid=p.planid and s.uid=u.userid and s.state<>2 and g.groupstate<>2 and p.planstate<>2 and u.ustate=0 and u.ushifenstatid in (2,3,6) and [s.uid]";
	runsql_sharding "$SQL" $TAB_UNIT_SLICE

	if [ $? -ne 0 ]
	then
		log "FATAL" "Before Incremental Export, update helpstatus Error."
		SendMail "adtrade: Before Incremental Export, update helpstatus Error." "${MAILLIST}"
		returnFlag=1
	fi
	
	alltable_export_full $outFileFull

	if [ $? -ne 0 ]
	then
		log "FATAL" "Error! Full Export error."
		SendMail "adtrade: Full Export Error." "${MAILLIST}"
		returnFlag=1
	else
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $FULL_EXPORT_FILE > $FULL_EXPORT_FILE.md5
		if [ $? -ne 0 ]
		then
			log "FATAL" "Error! Fail to generate md5 for full export."
			SendMail "adtrade: Fail to generate md5 for full export." "${MAILLIST}"
			returnFlag=1
		else
			log "TRACE" "Suc to generate md5."
		fi		
	fi
	
	cd $BIN_PATH
	
	close_log $returnFlag
	
	return $returnFlag
}

if [ $# -eq 0 ]
then
	adtrade_export
else
	adtrade_export_full
fi

exit $?
