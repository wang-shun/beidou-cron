#!/bin/bash
#@file: dts_service.sh
#@author: kanghongwei
#@version: 1.0.0
#@intention: DTS helper script

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/dts.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

DTS_INFO_FILE="./DTSInfo.dat.$$"

#function: download DTS file (by Date optional) 
#$1:DTS service name 
#$2:target path and file
#$3:date[optional]
function downloadDTSFile(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ -z $2 ]]; then
	    alert 1 "No target path and file specified."
	else
		target_path_file=$2 
	fi
	
	if [[ $target_path_file == .* ]];then
		target_path_file=`pwd``echo $target_path_file|sed 's/.//'`
	fi
	
	file_date=$3
	host_name=`hostname`
	host_name_lite=`hostname|sed 's/\.baidu\.com//'`
	
	dts_uri=`getDTSUri $dts_name $file_date`
	if [[ $dts_uri == $host_name":"$target_path_file ]] || [[ $dts_uri == $host_name_lite":"$target_path_file ]];then
		if [[ -f $target_path_file ]];then
			return 0
		else
			return 1
		fi
	fi
	
	dts_version=`getDTSVersion $dts_name $file_date`
	noahdt download -n $dts_version $dts_name $target_path_file
}

#function: download DTS file (by Date optional) 
#$1:DTS service name 
#$2:target path and file
#$3:partition id
#$4:date[optional]
function downloadDTSFileByShard(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ -z $2 ]]; then
	    alert 1 "No target path and file specified."
	else
		target_path_file=$2 
	fi
	
	if [[ -z $3 ]]; then
	    alert 1 "No partition specified."
	else
		file_partition=$3 
	fi
	
	if [[ $target_path_file == .* ]];then
		target_path_file=`pwd``echo $target_path_file|sed 's/.//'`
	fi
	
	file_date=$4
	host_name=`hostname`
	host_name_lite=`hostname|sed 's/\.baidu\.com//'`
	
	dts_uri=`getDTSUriByShard $dts_name $file_partition $file_date`
	if [[ $dts_uri == $host_name":"$target_path_file ]] || [[ $dts_uri == $host_name_lite":"$target_path_file ]];then
		if [[ -f $target_path_file ]];then
			return 0
		else
			return 1
		fi
	fi
	
	dts_version=`getDTSVersionByShard $dts_name $file_partition $file_date`
	noahdt download -n $dts_version $dts_name $target_path_file
}

#return DTS name's uri
#$1:DTS service name 
#$2:date[optional]
function getDTSUri(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ ! -z $2 ]]; then
	    local file_date=$2
	 	noahdt  list  ${dts_name} -i date=${file_date} | grep "\[uri\]" | awk  '{print $3}'| sed 's/.*@//' | tr -d '[]'
	else
		noahdt  list  ${dts_name} | grep "\[uri\]" | awk  '{print $3}'| sed 's/.*@//' | tr -d '[]'
	fi
}

#return DTS name's uri
#$1:DTS service name 
#$2:partition id
#$3:date[optional]
function getDTSUriByShard(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
		
	if [[ -z $2 ]]; then
	    alert 1 "No partition specified."
	else
		file_partition=$2
	fi
	
	if [[ ! -z $3 ]]; then
	    local file_date=$3
	 	noahdt  list  ${dts_name} -i partition=${file_partition} -i date=${file_date} | grep "\[uri\]" | awk  '{print $3}'| sed 's/.*@//' | tr -d '[]'
	else
		noahdt  list  ${dts_name} -i partition=${file_partition} | grep "\[uri\]" | awk  '{print $3}'| sed 's/.*@//' | tr -d '[]'
	fi
}

#return DTS name's newest version
#$1:DTS service name 
#$2:date[optional]
function getDTSVersion(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ ! -z $2 ]]; then
	    local file_date=$2
	 	noahdt  list  ${dts_name} -i date=${file_date} | grep "\[version\]" | awk -F":" '{print $2}' | tr -d '[]'
	else
		noahdt  list  ${dts_name} | grep "\[version\]" | awk -F":" '{print $2}' |  tr -d '[]'
	fi
}

#return DTS name's newest version
#$1:DTS service name 
#$2:partition id
#$3:date[optional]
function getDTSVersionByShard(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ -z $2 ]]; then
	    alert 1 "No partition specified."
	else
		file_partition=$2
	fi
	
	if [[ ! -z $3 ]]; then
	    local file_date=$3
	 	noahdt  list  ${dts_name}  -i partition=${file_partition} -i date=${file_date} | grep "\[version\]" | awk -F":" '{print $2}' | tr -d '[]'
	else
		noahdt  list  ${dts_name}  -i partition=${file_partition} | grep "\[version\]" | awk -F":" '{print $2}' |  tr -d '[]'
	fi
}

#return DTS name's item
#$1:DTS service name
#$2:DTS info.itemName
#$3:date[optional]
function getDTSInfoItem(){
	if [[ -z $1 ]]; then
	    alert 1 "No DTS service name specified."
	else    
    	local dts_name=$1
	fi
	
	if [[ -z $2 ]]; then
	    alert 1 "No DTS's info itemName specified."
	else    
    	local item_name=$2
	fi
	
	if [[ ! -z $3 ]]; then
	    local file_date=$3
	 	noahdt  list  ${dts_name} -i date=${file_date} > ${DTS_INFO_FILE}
	else
        noahdt  list  ${dts_name} > ${DTS_INFO_FILE}
	fi
    
	cat ${DTS_INFO_FILE} |  
		grep "\[info\]" | 
			awk -F":" '{print $2}' | 
                tr -d '[]' | 
                    awk -v item=$item_name '{split($0,array,","); 
                        for(i in array){
                            isIn=index(array[i],item)
                            if(isIn > 0) {
                                gsub(/ /,"",array[i]);
                                itemIndex=length("item=");
                                print substr(array[i],itemIndex);
                            }
                        }
                    }'
    rm -f ${DTS_INFO_FILE}
}

#check file's md5 that download from DTS
#$1:source file Name
#$2:DTS service name 
#$3:DTS info.itemName
#$4:date[optional]
function checkMD5ForDTS(){
    FILEMD5=`getFileMd5 $1`
    DTSMD5=`getDTSInfoItem $2 $3 $4`
    if [[ ${FILEMD5}"X" != ${DTSMD5}"X" ]];then
        return 1
    else
        return 0
    fi
}

#return file's md5
#$1:source file Name
function getFileMd5(){
	if [[ ! -f $1 ]]; then
	    alert 1 "file not exist: ${1}."
	fi
	md5sum ${1} | awk '{print $1}' 
}

#return file's md5
#$1:md5 file name
function getMd5FileMd5(){
	if [[ ! -f $1 ]]; then
	    alert 1 "md5 file not exist: ${1}."
	fi
	cat ${1} | awk '{print $1}' 
}
