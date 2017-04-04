#!/bin/bash

#filename: new_adtrade_generate_dicjs.sh
#@auther: xuxiaohu
#@date: 2013-06-20
#@version: 1.0.0.0
#@brief: generate new adtrade dic js

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_adtrade_generate_dicjs.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_path()
{
    if ! [ -d $TRADEDIC_INPUT_PATH ]
    then  
        mkdir -p $TRADEDIC_INPUT_PATH
    fi  
   
    if ! [ -d $TRADEDIC_OUTPUT_PATH ]
    then
        mkdir -p $TRADEDIC_OUTPUT_PATH
                                                            
    fi                                                               
    return 0
}

function backup_last_data()
{
    backup_file=$1

    if [ -f $backup_file ]
    then
        if [ -f ${backup_file}.last ]
        then
             rm ${backup_file}.last
        fi
        
        mv ${backup_file} ${backup_file}.last
    fi
    
    return 0
}

function download_data()
{
    cd $TRADEDIC_INPUT_PATH
    backup_last_data ${REMOTE_TRADEDIC_LABEL1}
    backup_last_data ${REMOTE_TRADEDIC_LABEL2}
    backup_last_data ${REMOTE_TRADEDIC_LABEL3}
    
    wget ftp://${REMOTE_TRADEDIC_HOST}${REMOTE_TRADEDIC_PATH}${REMOTE_TRADEDIC_LABEL1} ||  alert $? "Fetch[\
        new trade dic label 1 errror!"
    wget ftp://${REMOTE_TRADEDIC_HOST}${REMOTE_TRADEDIC_PATH}${REMOTE_TRADEDIC_LABEL2} ||  alert $? "Fetch[\
        new trade dic label 2 errror!"
    wget ftp://${REMOTE_TRADEDIC_HOST}${REMOTE_TRADEDIC_PATH}${REMOTE_TRADEDIC_LABEL3} ||  alert $? "Fetch[\
        new trade dic label 3 errror!"
    
    if [ -e ${REMOTE_TRADEDIC_LABEL1} -a -e ${REMOTE_TRADEDIC_LABEL1}.last ] && [ "`diff ${REMOTE_TRADEDIC_LABEL1} ${REMOTE_TRADEDIC_LABEL1}.last`" != "" ]; 
    then
        alert $? "New trade label 1 dic is different from last dic!"
    fi

    if [ -e ${REMOTE_TRADEDIC_LABEL2} -a -e ${REMOTE_TRADEDIC_LABEL2}.last ] && [ "`diff ${REMOTE_TRADEDIC_LABEL2} ${REMOTE_TRADEDIC_LABEL2}.last`" != "" ]; 
    then
        alert $? "New trade label 2 dic is different from last dic!"
    fi
    
    if [ -e ${REMOTE_TRADEDIC_LABEL3} -a -e ${REMOTE_TRADEDIC_LABEL3}.last ] && [ "`diff ${REMOTE_TRADEDIC_LABEL3} ${REMOTE_TRADEDIC_LABEL3}.last`" != "" ]; 
    then
        alert $? "New trade label 3 dic is different from last dic!"
    fi
}

function gen_js()
{
    cd $TRADEDIC_OUTPUT_PATH
    echo "var TRADE_DATA = {" >> $TRADEDIC_OUTPUT_FILENAME.tmp
    tradeFile1=${TRADEDIC_INPUT_PATH}${REMOTE_TRADEDIC_LABEL1}
    tradeFile2=${TRADEDIC_INPUT_PATH}${REMOTE_TRADEDIC_LABEL2}
    tradeFile3=${TRADEDIC_INPUT_PATH}${REMOTE_TRADEDIC_LABEL3}
    
    echo -e "\t'firstTrade': [" >> $TRADEDIC_OUTPUT_FILENAME.tmp
    len1=`wc -l $tradeFile1 | awk '{print $1}'`
    awk -F"\t" -vlen=$len1 '{print "\t\t{";print "\t\t\tvalue:"$1","; print "\t\t\tname:'\''"$2"'\'',"; print\
        "\t\t\tparentId:null"; if ( FNR == len ){ print "\t\t}"; } else print "\t\t},"; }' $tradeFile1 >>\
        $TRADEDIC_OUTPUT_FILENAME.tmp
    echo -e "\t],\n" >> $TRADEDIC_OUTPUT_FILENAME.tmp 
    
    echo -e "\t'secondTrade': [" >> $TRADEDIC_OUTPUT_FILENAME.tmp
    len2=`wc -l $tradeFile2 | awk '{print $1}'`
    awk -F"\t" -vlen=$len2 '{print "\t\t{";print "\t\t\tvalue:"$1","; print "\t\t\tname:'\''"$2"'\'',"; print\
        "\t\t\tparentId:"int($1/100); if ( FNR == len){ print "\t\t}"; } else print "\t\t},"; }' $tradeFile2 >>\
        $TRADEDIC_OUTPUT_FILENAME.tmp
    echo -e "\t]," >> $TRADEDIC_OUTPUT_FILENAME.tmp 
    
    echo -e "\t'thirdTrade': [" >> $TRADEDIC_OUTPUT_FILENAME.tmp
    len3=`wc -l $tradeFile3 | awk '{print $1}'`
    awk -F"\t" -vlen=$len3 '{print "\t\t{";print "\t\t\tvalue:"$1","; print "\t\t\tname:'\''"$2"'\'',"; print\
        "\t\t\tparentId:"int($1/100); if ( FNR == len ){ print "\t\t}"; } else print "\t\t},"; }' $tradeFile3 >>\
        $TRADEDIC_OUTPUT_FILENAME.tmp
    echo -e "\t]" >> $TRADEDIC_OUTPUT_FILENAME.tmp
    echo "};" >> $TRADEDIC_OUTPUT_FILENAME.tmp

    iconv -f GB2312 -t UTF-8 $TRADEDIC_OUTPUT_FILENAME.tmp > $TRADEDIC_OUTPUT_FILENAME
    rm $TRADEDIC_OUTPUT_FILENAME.tmp
    cat  $TRADEDIC_OUTPUT_FILENAME | tr -d "\n" | tr -d "\t" > $TRADEDIC_OUTPUT_JSFILE
}

check_path
download_data
gen_js

exit $?
