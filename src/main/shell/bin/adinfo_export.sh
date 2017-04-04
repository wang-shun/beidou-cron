#!/bin/bash

#@file: adinfo_export.sh
#@author: yanjie
#@date: 2009-07-07
#@version: 1.0.0.0
#@modify:guojichun since 2.0.0.0
#@modify:wangchongjie since 2012.12.10 for cpweb525
#@brief: [ipfilter/sitefilter/tradeprice/siteprice/sitetargeturl] to cpro

DEBUG_MOD=0
WORK_PATH="/home/work/beidou-cron"

CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/adinfo_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

cd $WORK_PATH

function check_conf()
{
	if ! [[ $MAIL_LIST ]]
	then
		echo "Conf[MAIL_LIST] is empty or its value is invalid"
		return 1
	fi

	if ! [ $EXPORT_FILE_IP ]
	then
		echo "Conf[EXPORT_FILE_IP] is empty or its value is invalid"
		return 1
	fi

	if ! [ $EXPORT_FILE_SITE ]
	then
		echo "Conf[EXPORT_FILE_SITE] is empty or its value is invalid"
		return 1
	fi
	
	if ! [ $EXPORT_FILE_TRADEPRICE ]
	then
		echo "Conf[EXPORT_FILE_TRADEPRICE] is empty or its value is invalid"
		return 1
	fi

	if ! [ $EXPORT_FILE_SITEPRICE ]
	then
		echo "Conf[EXPORT_FILE_SITEPRICE] is empty or its value is invalid"
		return 1
	fi
	
	if ! [ $EXPORT_FILE_TARGETURL ]
	then
		echo "Conf[EXPORT_FILE_TARGETURL] is empty or its value is invalid"
		return 1
	fi
	
	if ! [ $EXPORT_FILE_HUICHUANG_USER ]
	then
		echo "Conf[EXPORT_FILE_HUICHUANG_USER] is empty or its value is invalid"
		return 1
	fi
	
	return 0
}

function check_path()
{
	if ! [ -w $EXPORT_PATH ]
	then
		if ! [ -e $EXPORT_PATH ]
		then
			mkdir $EXPORT_PATH
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
			mkdir $BACKUP_PATH
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

function adinfo_export()
{
	check_conf
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	open_log
	
	check_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeNow=`date +%H%M`
	
	returnFlag=0
	
	outFile_ip=$EXPORT_PATH/$EXPORT_FILE_IP
	outFile_site=$EXPORT_PATH/$EXPORT_FILE_SITE
	outFile_tradeprice=$EXPORT_PATH/$EXPORT_FILE_TRADEPRICE
	outFile_siteprice=$EXPORT_PATH/$EXPORT_FILE_SITEPRICE
	outFile_targeturl=$EXPORT_PATH/$EXPORT_FILE_TARGETURL
	outFile_huichuan_user=$EXPORT_PATH/$EXPORT_FILE_HUICHUANG_USER
		
	outFile_ip_tmp=${outFile_ip}.tmp
	outFile_site_tmp=${outFile_site}.tmp
	outFile_tradeprice_tmp=${outFile_tradeprice}.tmp
	outFile_siteprice_tmp=${outFile_siteprice}.tmp
	outFile_targeturl_tmp=${outFile_targeturl}.tmp
	outFile_huichuan_user_tmp=${outFile_huichuan_user}.tmp
	
	if [ -f $outFile_ip ]
	then
		cp $outFile_ip $BACKUP_PATH/$EXPORT_FILE_IP.$timeNow
		log "TRACE" "Suc to backup $outFile_ip"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE_IP.$timeNow > $EXPORT_FILE_IP.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_IP.$timeNow]"
			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_IP.$timeNow]." "${MAIL_LIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
		cd $WORK_PATH
	fi

#	该逻辑检索端已不用，由于生成文件太大，导致网卡打满，因此注释掉by zhangpingan
#	if [ -f $outFile_site ]
#	then
#		cp $outFile_site $BACKUP_PATH/$EXPORT_FILE_SITE.$timeNow
#		log "TRACE" "Suc to backup $outFile_site"

#		cd $BACKUP_PATH
#		md5sum $EXPORT_FILE_SITE.$timeNow > $EXPORT_FILE_SITE.$timeNow.md5
#		if [ $? -ne 0 ]
#		then
#			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_SITE.$timeNow]"
#			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_SITE.$timeNow]." "${MAIL_LIST}"
#		else
#			log "TRACE" "Suc to generate md5 for backup"
#		fi
#		cd $WORK_PATH
#	fi

	if [ -f $outFile_tradeprice ]
	then
		cp $outFile_tradeprice $BACKUP_PATH/$EXPORT_FILE_TRADEPRICE.$timeNow
		log "TRACE" "Suc to backup $outFile_tradeprice"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE_TRADEPRICE.$timeNow > $EXPORT_FILE_TRADEPRICE.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_TRADEPRICE.$timeNow]"
			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_TRADEPRICE.$timeNow]." "${MAIL_LIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
		cd $WORK_PATH
	fi
	
	if [ -f $outFile_siteprice ]
	then
		cp $outFile_siteprice $BACKUP_PATH/$EXPORT_FILE_SITEPRICE.$timeNow
		log "TRACE" "Suc to backup $outFile_siteprice"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE_SITEPRICE.$timeNow > $EXPORT_FILE_SITEPRICE.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_SITEPRICE.$timeNow]"
			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_SITEPRICE.$timeNow]." "${MAIL_LIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
		cd $WORK_PATH
	fi
	
	if [ -f $outFile_targeturl ]
	then
		cp $outFile_targeturl $BACKUP_PATH/$EXPORT_FILE_TARGETURL.$timeNow
		log "TRACE" "Suc to backup $outFile_targeturl"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE_TARGETURL.$timeNow > $EXPORT_FILE_TARGETURL.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_TARGETURL.$timeNow]"
			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_TARGETURL.$timeNow]." "${MAIL_LIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
		cd $WORK_PATH
	fi
	
	if [ -f $outFile_huichuan_user ]
	then
		cp $outFile_huichuan_user $BACKUP_PATH/$EXPORT_FILE_HUICHUANG_USER.$timeNow
		log "TRACE" "Suc to backup $outFile_huichuan_user"

		cd $BACKUP_PATH
		md5sum $EXPORT_FILE_HUICHUANG_USER.$timeNow > $EXPORT_FILE_HUICHUANG_USER.$timeNow.md5
		if [ $? -ne 0 ]
		then
			log "WARNING" "Fail to generate md5 for [$EXPORT_FILE_HUICHUANG_USER.$timeNow]"
			SendMail "adinfo: Fail to generate md5 for [$EXPORT_FILE_HUICHUANG_USER.$timeNow]." "${MAIL_LIST}"
		else
			log "TRACE" "Suc to generate md5 for backup"
		fi
		cd $WORK_PATH
	fi
	
	SQL="select groupid, ip from beidou.groupipfilter where [userid]";
	runsql_sharding_read "$SQL" $outFile_ip_tmp
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_ip] Export Error."
		SendMail "adinfo: [$outFile_ip] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
	    sort -k1n $outFile_ip_tmp | uniq > $outFile_ip_tmp".sort"
        mv $outFile_ip_tmp".sort" $outFile_ip 
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $EXPORT_FILE_IP > $EXPORT_FILE_IP.md5.new
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for [$outFile_ip]."
			SendMail "adinfo: Fail to generate md5 for [$outFile_ip]." "${MAIL_LIST}"
			returnFlag=1
		else
			mv $EXPORT_FILE_IP.md5.new $EXPORT_FILE_IP.md5
			rm -f $outFile_ip_tmp
			log "TRACE" "Suc to generate md5."
		fi
		cd $WORK_PATH
	fi
	
	
	SQL="select groupid, site from beidou.groupsitefilter where [userid]";
	#runsql_sharding_read "$SQL" $outFile_site_tmp
	
	rm -f $outFile_site_tmp
	for((i=0;i<${SHARDING_SLICE};i++));
	do
	   runsql_single_read "$SQL" ${outFile_site_tmp}.${i} ${i}
	   if [ $? -ne 0 ]
			then
			log "FATAL" "[$outFile_tradeprice] Export Error."
			SendMail "adinfo: [$outFile_tradeprice] Export Error." "${MAIL_LIST}"
			exit 1
		fi
	    cat ${outFile_site_tmp}.${i} >> $outFile_site_tmp
		rm -f ${outFile_site_tmp}.${i}
	done
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_site] Export Error."
		SendMail "adinfo: [$outFile_site] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
		SQL="select max(groupid) from beidou.cprogroup where [userid]";
		runsql_sharding_read "$SQL" "maxGroupIds.tmp"
		local maxGroupId=`cat maxGroupIds.tmp|awk 'BEGIN{sum=0}{if($1>sum)sum=$1}END{print sum}'`
		
	    cat $outFile_site_tmp | awk -v "maxGroupId=$maxGroupId" '{if ($1 > maxGroupId) {print "invalid line:" $0; exit 1;}}'
		if [ $? -ne 0 ]
		then
			log "FATAL" "Invalid line found in [$outFile_site_tmp]."
			SendMail "adinfo: Invalid line found in [$outFile_site_tmp]." "${MAIL_LIST}"
			returnFlag=1
		else
            mv $outFile_site_tmp $outFile_site
	    	log "TRACE" "Suc to export."
    		cd $EXPORT_PATH
	    	md5sum $EXPORT_FILE_SITE > $EXPORT_FILE_SITE.md5.new
	    	if [ $? -ne 0 ]
	    	then
	    		log "FATAL" "Fail to generate md5 for [$outFile_site]."
	    		SendMail "adinfo: Fail to generate md5 for [$outFile_site]." "${MAIL_LIST}"
	    		returnFlag=1
	    	else
    			mv $EXPORT_FILE_SITE.md5.new $EXPORT_FILE_SITE.md5
	     		rm -f $outFile_site_tmp
    			log "TRACE" "Suc to generate md5."
	    	fi
		fi
		cd $WORK_PATH
	fi

	SQL="select  groupid, tradeid, price from beidou.grouptradeprice where [userid]";
	runsql_sharding_read "$SQL" $outFile_tradeprice_tmp
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_tradeprice] Export Error."
		SendMail "adinfo: [$outFile_tradeprice] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
		sort -k1n $outFile_tradeprice_tmp | uniq > $outFile_tradeprice_tmp".sort"
        mv $outFile_tradeprice_tmp".sort" $outFile_tradeprice
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $EXPORT_FILE_TRADEPRICE > $EXPORT_FILE_TRADEPRICE.md5.new
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for [$outFile_tradeprice]."
			SendMail "adinfo: Fail to generate md5 for [$outFile_tradeprice]." "${MAIL_LIST}"
			returnFlag=1
		else
			mv $EXPORT_FILE_TRADEPRICE.md5.new $EXPORT_FILE_TRADEPRICE.md5
			rm -f $outFile_tradeprice_tmp
			log "TRACE" "Suc to generate md5."
		fi
		cd $WORK_PATH
	fi
	
	SQL="select groupid, siteid, price from beidou.groupsiteprice where price is not null and [userid]";
	runsql_sharding_read "$SQL" $outFile_siteprice_tmp
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_siteprice] Export Error."
		SendMail "adinfo: [$outFile_siteprice] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
		sort -k1n $outFile_siteprice_tmp | uniq > $outFile_siteprice_tmp".sort"
        mv $outFile_siteprice_tmp".sort" $outFile_siteprice
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $EXPORT_FILE_SITEPRICE > $EXPORT_FILE_SITEPRICE.md5.new
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for [$outFile_siteprice]."
			SendMail "adinfo: Fail to generate md5 for [$outFile_siteprice]." "${MAIL_LIST}"
			returnFlag=1
		else
			mv $EXPORT_FILE_SITEPRICE.md5.new $EXPORT_FILE_SITEPRICE.md5
			rm -f $outFile_siteprice_tmp
			log "TRACE" "Suc to generate md5."
		fi
		cd $WORK_PATH
	fi
	
	SQL="select  groupid, siteid, targeturl from groupsiteprice where targeturl is not null and targeturl != 'null' and [userid]";
	runsql_sharding_read "$SQL" $outFile_targeturl_tmp
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_targeturl] Export Error."
		SendMail "adinfo: [$outFile_targeturl] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
		sort -k1n $outFile_targeturl_tmp | uniq > $outFile_targeturl_tmp".sort"
        mv $outFile_targeturl_tmp".sort" $outFile_targeturl
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $EXPORT_FILE_TARGETURL > $EXPORT_FILE_TARGETURL.md5.new
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for [$outFile_targeturl]."
			SendMail "adinfo: Fail to generate md5 for [$outFile_targeturl]." "${MAIL_LIST}"
			returnFlag=1
		else
			mv $EXPORT_FILE_TARGETURL.md5.new $EXPORT_FILE_TARGETURL.md5
			rm -f $outFile_targeturl_tmp
			log "TRACE" "Suc to generate md5."
		fi
		cd $WORK_PATH
	fi
	
	#
	SQL="select userid from beidoucap.useraccount where is_bd_classify = 0";
	runsql_cap_read "$SQL" $outFile_huichuan_user_tmp
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "[$outFile_huichuan_user] Export Error."
		SendMail "adinfo: [$outFile_huichuan_user] Export Error." "${MAIL_LIST}"
		returnFlag=1
	else
		sort -k1n $outFile_huichuan_user_tmp | uniq > $outFile_huichuan_user_tmp".sort"
        mv $outFile_huichuan_user_tmp".sort" $outFile_huichuan_user
		log "TRACE" "Suc to export."
		cd $EXPORT_PATH
		md5sum $EXPORT_FILE_HUICHUANG_USER > $EXPORT_FILE_HUICHUANG_USER.md5.new
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to generate md5 for [$outFile_huichuan_user]."
			SendMail "adinfo: Fail to generate md5 for [$outFile_huichuan_user]." "${MAIL_LIST}"
			returnFlag=1
		else
			mv $EXPORT_FILE_HUICHUANG_USER.md5.new $EXPORT_FILE_HUICHUANG_USER.md5
			rm -f $outFile_huichuan_user_tmp
			log "TRACE" "Suc to generate md5."
		fi
		cd $WORK_PATH
	fi
	
	#########temporally copy to new dir added by zhangxichuan############
	NEW_PATH=/home/work/beidou-cron/data/export/adinfo
	mkdir -p $NEW_PATH
	
	cp $outFile_ip $NEW_PATH/
	cp $outFile_site $NEW_PATH/
	cp $outFile_tradeprice $NEW_PATH/
	cp $outFile_siteprice $NEW_PATH/
	cp $outFile_targeturl $NEW_PATH/
	cp $outFile_huichuan_user $NEW_PATH/
	
	cp $outFile_ip.md5 $NEW_PATH/
	cp $outFile_site.md5 $NEW_PATH/
	cp $outFile_tradeprice.md5 $NEW_PATH/
	cp $outFile_siteprice.md5 $NEW_PATH/
	cp $outFile_targeturl.md5 $NEW_PATH/
	cp $outFile_huichuan_user.md5 $NEW_PATH/
	#########temporally copy to new dir added by zhangxichuan############

	close_log $returnFlag
	
	return $returnFlag
}

adinfo_export

exit $?
