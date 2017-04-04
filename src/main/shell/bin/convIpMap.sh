#!/bin/sh
# This program is used to convert ip
source /home/work/beidou-cron/lib/beidou_lib.sh
source /home/work/beidou-cron/conf/alert.conf

# ip.map的输出目录
OUTPUT_DIR=/home/work/beidou-cron/data
# 当前程序目录
BASE_DIR=/home/work/beidou-cron

MAP_FILR=/home/work/beidou-cron/conf/zonemap.conf
DATA_DIR=${BASE_DIR}/data

#抓取总控中心文件
#CT_URL=ftp://10.23.252.149/home/work/var/sf-ct/filesvr/data/764/
#IP_FILE=dict_ipmap_ip.map.all
CT_URL=ftp://colombo:colombo_ftp@ip.baidu.com/data/dr_iplist/
IP_FILE=dict_ipmap_ip.map.all.latest

wget -t 3 -q ${CT_URL}${IP_FILE}.md5 -O ${DATA_DIR}/${IP_FILE}.md5
wget -t 3 -q ${CT_URL}${IP_FILE} -O ${DATA_DIR}/${IP_FILE}

cd ${DATA_DIR}
md5sum -c ${IP_FILE}".md5"
if [ $? -ne 0 ]; then
    SendMail "dict_ip: Fail to check md5 for [${IP_FILE}]." "${MAILLIST}"
    SendMessage "dict_ip: Fail to check md5 for [$IP_FILE]." "${MOBILELIST}"
    exit 1
fi

# 进行ip替换，压平

cd ${BASE_DIR}

#7-jp, transform to 35(beidou region)
awk -F"\t" -vzoneMapFile=conf/zonemap.conf -f bin/convIpMap.awk ${DATA_DIR}/${IP_FILE} > ${OUTPUT_DIR}/ip.map.new

#99-other, 35-japan, 999-other country 
awk -F"\t" '{if($3==37 && $4==0) print "1\t"$1"\t"$2"\t999\t0"}' ${DATA_DIR}/${IP_FILE} >> ${OUTPUT_DIR}/ip.map.new

# 追加网吧ip
cat ${BASE_DIR}/conf/netbar.conf >> ${OUTPUT_DIR}/ip.map.new

cd ${OUTPUT_DIR}

#先按第一列排，再按第二列排
awk -F"\t" '{ gsub(/\./,"\t"); print $0; }' ip.map.new | sort -k 1,1n -k 2,2n -k 3,3n -k 4,4n -k 5,5n | awk -F"\t" '{ print $1"\t"$2"."$3"."$4"."$5"\t"$6"."$7"."$8"."$9"\t"$10"\t"$11"\t"}'>  ip.map.tmp

rm ip.map.new

TIME=`date +"%Y%m%d%H%M"`
if [ -e "${OUTPUT_DIR}/ip.map" ] && [ "`diff ip.map.tmp ip.map`" != "" ]; then
    cp ip.map ip.map.${TIME}
fi

# 生成md5
mv ip.map.tmp ip.map
md5sum ip.map > ip.map.md5.tmp
mv ip.map.md5.tmp ip.map.md5

cp ip.map beidou_ip.map.tmp
# 删除电信、网吧、校园网相关信息 modify by guojichun since beidou2.0.0
awk -F"\t" '{if($1==1) print $0}' beidou_ip.map.tmp > beidou_ip.map.tmp.nature
rm beidou_ip.map.tmp
mv beidou_ip.map.tmp.nature beidou_ip.map
md5sum beidou_ip.map > beidou_ip.map.md5.tmp
mv beidou_ip.map.md5.tmp beidou_ip.map.md5

cd ${BASE_DIR}

# 发现没有对应地域的报警，后续处理
if [ -e "${BASE_DIR}/data/zoneNotFound.txt" ]; then
    while read LINE
    do
        ids=${ids}" "${LINE}
    done < ${BASE_DIR}/data/zoneNotFound.txt

    if [ -n "${ids}" ]; then
       SendMail "dict_ip_lib: zone not supported : ${ids}" "${MAILLIST}"
    fi 

    rm ${BASE_DIR}/data/zoneNotFound.txt
fi
