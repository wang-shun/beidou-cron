#!/bin/bash

#@file: beidou_supportData2js.sh
#@author: acelan
#@date: 2011-01-05
#@version: 1.0.0.0
#@description:getdata from dr-mgr, format to js,dispatch to 8 webserver/webroot/asset/

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/beidou_supportData2js.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=beidou_supportData2js.sh;
reader_list=lanxiaobin;

#wget data from dr-mgr

[ ! -d ${source_path} ] && mkdir ${source_path};

#gen notice
wget ${drmgr_notice_file_path} -O ${source_path}${notice_filename};

if [ -e "${source_path}${notice_filename}" ] ; then
    sort -k4nr -t $'\t' ${source_path}${notice_filename} > ${source_path}tmp${notice_filename};
    mv ${source_path}tmp${notice_filename} ${source_path}${notice_filename};

    awk -F"\t" 'BEGIN{
        cnt=0;
    }
    {
        if($6==0){
            map[cnt]="{title:\""$2"\",href:\""$3"\",blank:"$5",key:\"nt"$1"\",hint:\""$7"\",type:"$8"}";
            cnt++;
        }
    
    }END{   
        print "var TRIONES_SUPPORT_NOTICE_MAP=[";
        cnt=cnt-1;
        for(i=0;i<=cnt;i++){
            if(i==cnt){
                print "\t"map[i];
            }else{
                print "\t"map[i]",";
            }
        }
        print "]";
    }' ${source_path}${notice_filename} > ${source_path}${output_notice_filename}

    iconv -f GBK -t UTF-8 -o ${source_path}utf${output_notice_filename} ${source_path}${output_notice_filename};
    cp ${source_path}utf${output_notice_filename} ${dest_path}${output_notice_filename};
    
    cd ${dest_path};
    md5sum ${output_notice_filename} > ${output_notice_filename}.md5;
    
    # use noah data push
    rm -f ${output_notice_filename}.lzo
    ${lzop_client} ${output_notice_filename} ${output_notice_filename}.md5 -o ${output_notice_filename}.lzo
    bscp --setinfo ${ftp_path}${dest_path}${output_notice_filename}.lzo data:/${notice_file_key}
    
    rm ${source_path}utf${output_notice_filename};
    rm ${source_path}${notice_filename};
else
    alert 1 "wget fail, notice file no exist.";
fi


#gen tip
wget ${drmgr_tip_file_path} -O ${source_path}${tip_filename};

if [ -e "${source_path}${tip_filename}" ] ; then
    sort -k4,4 -t $'\t' ${source_path}${tip_filename} > ${source_path}tmp${tip_filename};
    mv ${source_path}tmp${tip_filename} ${source_path}${tip_filename};

    awk -F"\t" 'BEGIN{  
        key="";
        begin=1;
        print "var TRIONES_SUPPORT_MAP={";
        cnt=0;
        SUBSEP="::";  
    }
    {
        if($7==0){
            if (key!=$4) {
                if(begin!=1){
                    str="\""key"\" : ";
                    printf("%s[\n", str);
                    len=asort(array);
                    for(i=1;i<=len;i++){
                        str1=map[array[i]];
                        if(i==len){
                            printf("\t{%s}\n", str1);
                        }else{
                            printf("\t{%s},\n", str1);
                        }
                    }
                    delete array;
                    cnt++;
                    array[$4,cnt]=cnt;
                    map[cnt]="el:\""$5"\",title:\""$2"\",content:\""$3"\",key:\"tip"$1"\"";
                    print "],";
                }else{
                    begin=0;
                    cnt++;
                    array[$4,cnt]=cnt;
                    map[cnt]="el:\""$5"\",title:\""$2"\",content:\""$3"\",key:\"tip"$1"\"";
                }
                key=$4;
            }else {
                cnt++;
                array[$4,cnt]=cnt;
                map[cnt]="el:\""$5"\",title:\""$2"\",content:\""$3"\",key:\"tip"$1"\"";
            }
        }
    }
    END{
        len=asort(array);
        if(len>0){
            str="\""key"\" : ";
            printf("%s[\n", str);
            for(i=1;i<=len;i++){
                if(i==len){
                    print "\t{"map[array[i]]"}";
                }else{
                    print "\t{"map[array[i]]"},";
                }
            }
        
            print "]";
        }
        print "};";       
    }' ${source_path}${tip_filename} > ${source_path}${output_tip_filename}
    
    iconv -f GBK -t UTF-8 -o ${source_path}utf${output_tip_filename} ${source_path}${output_tip_filename};
    cp ${source_path}utf${output_tip_filename} ${dest_path}${output_tip_filename};
    
    cd ${dest_path};
    md5sum ${output_tip_filename} > ${output_tip_filename}.md5;
    
    # use noah data push
    rm -f ${output_tip_filename}.lzo
    ${lzop_client} ${output_tip_filename} ${output_tip_filename}.md5 -o ${output_tip_filename}.lzo
    bscp --setinfo ${ftp_path}${dest_path}${output_tip_filename}.lzo data:/${tip_file_key}
    
    rm ${source_path}utf${output_tip_filename};
    rm ${source_path}${tip_filename};

else
    alert 1 "wget fail, tip file no exist.";
fi

