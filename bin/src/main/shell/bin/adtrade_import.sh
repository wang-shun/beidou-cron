#!/bin/bash

#@file: adtrade_import.sh
#@author: yanjie
#@date: 2009-04-26
#@version: 1.0.0.0
#@brief: get ad-trade from sf-herring and update DB
#sensitive import ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½Ð¹ï¿½ï¿½ï¿½ï¿½ï¿?
#added by cuihuaizhou 2011-08-01
#@modify: wangchongjie since 2012.12.10 for cpweb525

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/adtrade_import.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=adtrade_import.sh
reader_list=kanghongwei

cd $BIN_PATH

function check_conf()
{
	if ! [[ $SERVER_URL ]]
	then
		echo "Conf[SERVER_URL] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $SERVER_USER ]]
	then
		echo "Conf[SERVER_USER] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $SERVER_PWD ]]
	then
		echo "Conf[SERVER_PWD] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $SERVER_ROOT ]]
	then
		echo "Conf[SERVER_ROOT] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $IMPORT_FILE ]]
	then
		echo "Conf[IMPORT_FILE] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ ${ADTRADE_EXPORT_PATH} ]]
	then
		echo "Conf[ADTRADE_EXPORT_PATH] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ ${ADTRADE_EXPORT_FILE} ]]
	then
		echo "Conf[ADTRADE_EXPORT_FILE] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ ${MAX_PER_PAGE} ]]
	then
		echo "Conf[MAX_PER_PAGE] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MAX_RETRY ]]
	then
		echo "Conf[MAX_RETRY] is empty or its value is invalid"
		return 1
	fi
	return 0
}

function check_path()
{
	if ! [ -w $LOCAL_TEMP ]
	then
		if ! [ -e $LOCAL_TEMP ]
		then
			mkdir -p $LOCAL_TEMP
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_TEMP]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_TEMP] is not writable!"
			return 1
		fi
	fi

	if ! [ -w $LOCAL_BACK ]
	then
		if ! [ -e $LOCAL_BACK ]
		then
			mkdir -p $LOCAL_BACK
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_BACK]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_BACK] is not writable!"
			return 1
		fi
	fi
	
	return 0	
}

#sensitive import ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½Ð¹ï¿½ï¿½ï¿½ï¿½ï¿?
#added by cuihuaizhou 2011-08-01
function sensitivead_import()
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

    timeNow=`date +%Y%m%d%H%M`

    returnFlag=0

  #	MAX_SENS_PER_PAGE=1000
  #	SENSITIVEAD_IMPORT_FILE=suspect_sensitive_adid.txt
	log "TRACE" "download begain"
        wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/$SENSITIVEAD_IMPORT_FILE -P $LOCAL_TEMP

	if [ $? -ne 0 ]
        then
                log "FATAL" "Fail to download [$SENSITIVEAD_IMPORT_FILE]."
                SendMail "adtrade: Fail to download [$SENSITIVEAD_IMPORT_FILE]." "${MAILLIST}"
                returnFlag=1
  	fi

	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/${SENSITIVEAD_IMPORT_FILE}.md5 -P $LOCAL_TEMP

	cd $LOCAL_TEMP
	md5sum -c ${SENSITIVEAD_IMPORT_FILE}.md5 > /dev/null
                if [ $? -ne 0 ]
                then
                        log "FATAL" "Fail to check md5 for [$SENSITIVEAD_IMPORT_FILE]."
                        SendMail "adtrade: Fail to check md5 for [$SENSITIVEAD_IMPORT_FILE]." "${MAILLIST}"
                        returnFlag=1
		fi
	
	if [ -s ${SENSITIVEAD_IMPORT_FILE} ]
	then
		log "${SENSITIVEAD_IMPORT_FILE} is not empty."
	else
		log "${SENSITIVEAD_IMPORT_FILE} is empty."
		return $returnFlag
	fi	

	log "TRACE" "group begin"
	sort -n $SENSITIVEAD_IMPORT_FILE | awk -vmax="$MAX_SENS_PER_PAGE" 'BEGIN{ ORS=""; count=0;}; 
                {
		     if(count<max)
                {       
                        if(count % max==(max-1))
                        {
                        print $1;  
                        count+=1;
                        }
                        else 
                        {
                        print $1",";  
                        count+=1;
                        }
                }       
                    else    
                {       
                        print "\n"$1","; 
                        count=1;
                }        
                };' > $SENSITIVEAD_IMPORT_FILE.tmp
                sed 's/\(.*\),$/\1/g' $SENSITIVEAD_IMPORT_FILE.tmp > $SENSITIVEAD_IMPORT_FILE.group

	log "TRACE" "group end & sql begin"
		
	awk 'BEGIN{ print "use beidou;" }; 
{
print "update beidou.cprounitstate? s set s.helpstatus = s.helpstatus | 8 where s.id in ("$1")  and s.state = 3  and [s.uid];";
}' $SENSITIVEAD_IMPORT_FILE.group > $SENSITIVEAD_IMPORT_FILE.sql
	   
	    log "TRACE" "sql end & import begin"
		
		runfilesql_sharding "${LOCAL_TEMP}/$SENSITIVEAD_IMPORT_FILE.sql" $TAB_UNIT_SLICE
	    if [ $? -ne 0 ]
            then
			 log "FATAL" "Import sensitived id error."
			 SendMail "adtrade: Import sensitived id Error." "${MAILLIST}"
			 returnFlag=1
        fi
	
	mv $LOCAL_TEMP/$SENSITIVEAD_IMPORT_FILE $LOCAL_BACK/$SENSITIVEAD_IMPORT_FILE.$timeNow
 	mv $LOCAL_TEMP/$SENSITIVEAD_IMPORT_FILE.md5 $LOCAL_BACK/$SENSITIVEAD_IMPORT_FILE.md5.$timeNow
    rm $LOCAL_TEMP/*
	return $returnFlag
}


function adtrade_import()
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
	
	outFile=${ADTRADE_EXPORT_PATH}/${ADTRADE_EXPORT_FILE}
	if ! [ -f ${outFile} ]
	then
		log "FATAL" "There is no ${outFile} for adtrade import."
		SendMail "There is no ${outFile} for adtrade import." "${MAILLIST}"
		return 1
	fi
	
	timeNow=`date +%Y%m%d%H%M`
	
	returnFlag=0
	
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/$IMPORT_FILE -P $LOCAL_TEMP
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to download [$IMPORT_FILE]."
		SendMail "adtrade: Fail to download [$IMPORT_FILE]." "${MAILLIST}"
		returnFlag=1
	else
		wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/${IMPORT_FILE}.md5 -P $LOCAL_TEMP
		cd $LOCAL_TEMP
		md5sum -c ${IMPORT_FILE}.md5 > /dev/null
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to check md5 for [$IMPORT_FILE]."			
			SendMail "adtrade: Fail to check md5 for [$IMPORT_FILE]." "${MAILLIST}"
			returnFlag=1
		else
			if [ -s $IMPORT_FILE ]
			then
				sort -k1,1 $IMPORT_FILE > $IMPORT_FILE.sort
				#verify
				if [[ -s $LOCAL_BACK/$IMPORT_FILE.sort ]] && [[ -f $outFile ]]
				then					
					join -a 2 $LOCAL_BACK/$IMPORT_FILE.sort $IMPORT_FILE.sort > $IMPORT_FILE.join
					awk '{if(NF==2){print $0;}else{if($2!=$3){print $1"\t"$3;}}}' $IMPORT_FILE.join > $IMPORT_FILE.join1	
					awk 'ARGIND==1{id[$1]}ARGIND>1{if($1 in id){print $0;}}' $outFile $IMPORT_FILE	> $IMPORT_FILE.join2
					cat $IMPORT_FILE.join1 $IMPORT_FILE.join2 | sort -u > $IMPORT_FILE.result		
					awk -velog="verify.err" '{if (NF != 2) {print now" [field number error] "$0 >> elog;} else if (($1 !~ /[0-9]/)||($1 < 1)) {print now" [adid error] "$0 >> elog;} else if (($2 !~ /[0-9]/)||($2 < 101) || ($2 > 9900)) {print now" [adtradeid error] "$0 >> elog;} else {print $0;}}' $IMPORT_FILE.result > $IMPORT_FILE.verify
				else
					log "FATAL" "There is no [$LOCAL_BACK/$IMPORT_FILE.sort] and [${outFile}] for adtrade import."
					SendMail "There is no [$LOCAL_BACK/$IMPORT_FILE.sort] and [${outFile}] for adtrade import."
					return 1
				fi
				if [ -s "verify.err" ]
				then
					errmsg=`cat verify.err`
					log "WARNING" "$errmsg"
					SendMail "verifying [$IMPORT_FILE] error: $errmsg." "${MAILLIST}"
				fi
				log "TRACE" "verify end & group begin"
				#sort&group
				sort -n -k 2 $IMPORT_FILE.verify | awk -vmax="${MAX_PER_PAGE}" 'BEGIN{ ORS=""; cid=-1; count=0;}; 
                {       
                if(count<max)
                {       
                    if($2==cid) print ","$1;  
                    else if(cid>0) {print "\t"cid"\n"$1; cid=$2} 
                    else {cid=$2; print $1;}
                    count+=1;
                }       
                else    
                {       
                    print "\t"cid"\n"$1; cid=$2; count=1;
                }       
                }; END{ if(cid!=-1){print "\t"cid; } ORS="\n"}' > $IMPORT_FILE.group
				log "TRACE" "group end & sql begin"
				#generate sql
				awk 'BEGIN{ print "use beidou;" }; 
				{
				print "update cprounitmater? m, cprounitstate? s set m.adtradeid="$2" where m.id in ("$1") and m.id = s.id and s.helpstatus & 6 = 4 and [s.uid];";
				}' $IMPORT_FILE.group > $IMPORT_FILE.sql
				log "TRACE" "sql end & import begin"
				
				#run sql
				runfilesql_sharding "${LOCAL_TEMP}/$IMPORT_FILE.sql" $TAB_UNIT_SLICE
				if [ $? -ne 0 ]
				then
					log "FATAL" "Import error."
					SendMail "adtrade: Import Error." "${MAILLIST}"
					returnFlag=1
				fi
				log "TRACE" "import end"
			else
				log "WARNING" "[$IMPORT_FILE] is empty."
				SendMail "[$IMPORT_FILE] is empty." "${MAILLIST}"
				returnFlag=1
			fi
			
			#backup sorted import file for next
			mv $LOCAL_TEMP/$IMPORT_FILE.sort $LOCAL_BACK/$IMPORT_FILE.sort
			
			mv $LOCAL_TEMP/$IMPORT_FILE $LOCAL_BACK/$IMPORT_FILE.$timeNow
			cd $LOCAL_BACK
			md5sum $IMPORT_FILE.$timeNow > $IMPORT_FILE.$timeNow.md5
		fi
	fi

	cd $BIN_PATH
	rm $LOCAL_TEMP/*
	close_log $returnFlag
	
	return $returnFlag
}

function adtrade_import_full()
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
	
	returnFlag=0
	
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/$IMPORT_FILE -P $LOCAL_TEMP
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to download [$IMPORT_FILE]."
		SendMail "adtrade full import: Fail to download [$IMPORT_FILE]." "${MAILLIST}"
		returnFlag=1
	else
		wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/${IMPORT_FILE}.md5 -P $LOCAL_TEMP
		cd $LOCAL_TEMP
		md5sum -c ${IMPORT_FILE}.md5 > /dev/null
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to check md5 for [$IMPORT_FILE]."			
			SendMail "adtrade full import: Fail to check md5 for [$IMPORT_FILE]." "${MAILLIST}"
			returnFlag=1
		else
			if [ -s $IMPORT_FILE ]
			then
				awk -velog="verify.err" '{if (NF != 2) {print now" [field number error] "$0 >> elog;} else if (($1 !~ /[0-9]/)||($1 < 1)) {print now" [adid error] "$0 >> elog;} else if (($2 !~ /[0-9]/)||($2 < 101) || ($2 > 9900)) {print now" [adtradeid error] "$0 >> elog;} else {print $0;}}' $IMPORT_FILE > $IMPORT_FILE.verify.full
				if [ -s "verify.err" ]
				then
					errmsg=`cat verify.err`
					log "WARNING" "$errmsg"
					SendMail "verifying [$IMPORT_FILE] error: $errmsg." "${MAILLIST}"
				fi
				log "TRACE" "verify end & group begin"
				#sort&group
				sort -n -k 2 $IMPORT_FILE.verify.full | awk -vmax="${MAX_PER_PAGE}" 'BEGIN{ ORS=""; cid=-1; count=0;}; 
                {       
                if(count<max)
                {       
                    if($2==cid) print ","$1;  
                    else if(cid>0) {print "\t"cid"\n"$1; cid=$2} 
                    else {cid=$2; print $1;}
                    count+=1;
                }       
                else    
                {       
                    print "\t"cid"\n"$1; cid=$2; count=1;
                }       
                }; END{ if(cid!=-1){print "\t"cid; } ORS="\n"}' > $IMPORT_FILE.group.full
				log "TRACE" "group end & sql begin"
				#generate sql
				awk 'BEGIN{ print "use beidou;" }; 
				{
				print "update cprounitmater? m, cprounitstate? s set m.adtradeid="$2" where m.id in ("$1") and m.id = s.id and s.helpstatus & 6 = 4 and [s.uid];";
				}' $IMPORT_FILE.group.full > $IMPORT_FILE.sql.full
				log "TRACE" "sql end & adtrade full import begin"
				
				#run sql
				runfilesql_sharding "${LOCAL_TEMP}/$IMPORT_FILE.sql.full" $TAB_UNIT_SLICE
				if [ $? -ne 0 ]
				then
					log "FATAL" "adtrade full import error."
					SendMail "adtrade full import error." "${MAILLIST}"
					returnFlag=1
				fi
				log "TRACE" "adtrade full import end"
			else
				log "WARNING" "[$IMPORT_FILE] is empty."
				SendMail "[$IMPORT_FILE] is empty." "${MAILLIST}"
				returnFlag=1
			fi
		fi
	fi

	cd $BIN_PATH
	rm $LOCAL_TEMP/*
	close_log $returnFlag
	
	return $returnFlag
}

if [ $# -eq 0 ]
then
	sensitivead_import
	if [ $? -eq 1 ]
	then
		log "FATAL" "sensitivead_import function exec error."
        SendMail "adtrade: sensitivead_import function Error." "${MAILLIST}"
	fi

	adtrade_import
	if [ $? -eq 1 ]
	then
        log "FATAL" "adtrade_import function exec error."
        SendMail "adtrade: adtrade_import function Error." "${MAILLIST}"
	fi
else
	if [ $1 -eq 1 ]
	then
		sensitivead_import
		if [ $? -eq 1 ]
		then
			log "FATAL" "sensitivead_import function exec error."
			SendMail "adtrade: sensitivead_import function Error." "${MAILLIST}"
		fi
	else
	   if [ $1 -eq 2 ]
	   then
			adtrade_import
			if [ $? -eq 1 ]
			then
				log "FATAL" "adtrade_import function exec error."
				SendMail "adtrade: adtrade_import function Error." "${MAILLIST}"
			fi
	   fi
	   
	   if [ $1 -eq 3 ]
	   then
			adtrade_import_full
			if [ $? -eq 1 ]
			then
				log "FATAL" "adtrade_import_full function exec error."
				SendMail "adtrade: adtrade_import_full function Error." "${MAILLIST}"
			fi
	   fi
	fi
fi

exit $?
