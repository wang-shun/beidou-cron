#!/bin/sh
#ץȡurlչ������
#$1 : �ɴ��ݾ�������ڲ�������ʽΪ:yyyyMMdd 

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/siteurl.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


program=downloadSiteUrl.sh
reader_list=yang_yun

LOG_FILE=${LOG_PATH}/downloadSiteUrl/downloadSiteUrl.log.`date +%Y%m%d`
TIME1=`date +%s`

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${LOG_PATH}/downloadSiteUrl
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${SITE_DATA_CACHE}

msg="��������Ŀ¼${SITE_DATA_PATH}ʧ��"
cd ${SITE_DATA_PATH}
alert $? "${msg}"
echo "`date +"%Y-%m-%d %H:%M:%S"`,��������Ŀ¼">>${LOG_FILE}

#ץȡ�ļ�
[ -f ${WHITE_FILE} ] && mv ${WHITE_FILE} ${WHITE_FILE}.bak
[ -f ${WHITE_FILE}.md5 ] && mv ${WHITE_FILE}.md5 ${WHITE_FILE}.bak
msg="wget�ļ�${WHITE_FILE_URL}${WHITE_FILE}.md5ʧ��"
wget -q -t$MAX_RETRY  ${WHITE_FILE_URL}/${WHITE_FILE}.md5
alert $? "${msg}"

msg="wget�ļ�${WHITE_FILE_URL}${WHITE_FILE}ʧ��"
wget -q  -t$MAX_RETRY  ${WHITE_FILE_URL}/${WHITE_FILE}
alert $? "${msg}"

msg="${WHITE_FILE_URL}${WHITE_FILE}����md5ʧ��"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

#ץȡչ������
[ -f ${SITE_URL_FILE} ] && rm ${SITE_URL_FILE}
msg="wget urlͳ������${SITE_URL}ʧ��"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}normal${SITE_URL2}" -O ${SITE_URL_FILE}
alert $? "${msg}"

msg="wget urlͳ������${SITE_URL} manifestʧ��"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}midoutfile&file=@manifest${SITE_URL2}" -O ${SITE_URL_FILE}.manifest
alert $? "${msg}"

msg="wget urlͳ������${SITE_URL} manifest.md5ʧ��"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}midoutfile&file=@manifest.md5${SITE_URL2}" -O ${SITE_URL_FILE}.manifest.md5
alert $? "${msg}"

offline_manifest_md5=`md5sum $SITE_URL_FILE".manifest" | awk '{print $1}'` #manifest md5 offline
online_manifest_md5=`awk '{print $1}' $SITE_URL_FILE".manifest.md5"`   #manifest md5 online
if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
then
	alert 1 "${SITE_URL_FILE}-manifest�ļ�md5У��ʧ��"
fi

offline_file_size=`du -b $SITE_URL_FILE|awk '{print $1}'` #line count offline
online_file_size=`awk '{print $3}' $SITE_URL_FILE".manifest"`   #line count online
if [ "${offline_file_size}" != "${online_file_size}" ]
then
	alert 1 "${SITE_URL_FILE}-�ļ���СУ��ʧ��"
fi

#ȥ���ݲ�ʹ�õĵ�һ������
cut -f2- ${SITE_URL_FILE} > ${SITE_DATA_PATH}/tmp.txt
mv ${SITE_DATA_PATH}/tmp.txt ${SITE_URL_FILE}

msg="urlͳ����������Ӧ����10��"
LINE=`head -n 11 ${SITE_URL_FILE} | wc -l | cut -d' ' -f1`
alert $? "${msg}"
if [ "${LINE}" -lt 10 ] ; then
    alert 1 "${msg}"
fi

TIME2=`date +%s`
echo "ץȡURLչ�����ݺ�ʱ`expr ${TIME2} - ${TIME1}`" >>${LOG_FILE}


#��չ��url��������
msg="��չ��url��������ʧ��"
sort -T${SITE_DATA_CACHE} -k3,3 -o"${SITE_URL_FILE}.url.sorted" ${SITE_URL_FILE}
alert $? "${msg}"
rm -f ${SITE_URL_FILE}
rm -f ${SITE_URL_FILE}.md5
TIME3=`date +%s`
echo "��չ��url���������ʱ`expr ${TIME3} - ${TIME2}`" >>${LOG_FILE}


#��show_url���д������ڵ�3��չ��urlһ�µ��У�����ڶ����ۼӣ����׷�ӵ�ÿһ�еĽ�β
msg="��show_url���д������ʧ��"
awk 'BEGIN{ 
        pre_url="";
        pre_count=0;
        i=0;
    }{
        if($3==pre_url){
            pre_count=$2+pre_count;
            pre[i++]=$0;
        }else{
                for(k=0;k<i;k++){
                    print pre[k]"\t"pre_count;
                }
                i=0;
                pre[i++]=$0;
                pre_url=$3;
                pre_count=$2
        }
    }
    END{
        for(k=0;k<i;k++){
            print pre[k]"\t"pre_count;
        }
    }'	${SITE_URL_FILE}.url.sorted >${SITE_URL_FILE}.added
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.url.sorted
TIME4=`date +%s`
echo "��show_url���д��������ʱ`expr ${TIME4} - ${TIME3}`" >>${LOG_FILE}


#��show_url.added����������
msg="��show_url.added����������ʧ��"
sort -s -r -n -T${SITE_DATA_CACHE} -t"	" -k9,9 -o"${SITE_URL_FILE}.retrive.sorted" ${SITE_URL_FILE}.added
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.added
TIME5=`date +%s`
echo "��show_url.added�����������ʱ`expr ${TIME5} - ${TIME4}`" >>${LOG_FILE}


#��show_url.added����������
msg="��show_url.added����������ʧ��"
sort -s -f -T${SITE_DATA_CACHE} -k1,1 -o"${SITE_URL_FILE}.domain.sorted" "${SITE_URL_FILE}.retrive.sorted"
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.retrive.sorted
TIME6=`date +%s`
echo "��show_url.added�����������ʱ`expr ${TIME6} - ${TIME5}`" >>${LOG_FILE}


#�������
msg="�������ʧ��"
awk -F"\t" 'BEGIN{OFS="\t"}{print $3,$4,$9,$5,$6,$7,$8}' "${SITE_URL_FILE}.domain.sorted" > ${SITE_URL_FILE}.${STAT_DATE}
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.domain.sorted
TIME7=`date +%s`
echo "������ݺ�ʱ`expr ${TIME7} - ${TIME6}`" >>${LOG_FILE}


if ! [ -n "$1" ] ;then 
	find ${SITE_DATA_PATH} -type f -mtime +${DATA_STORE_DAYS} -exec  rm -rf {} \;
fi
TIME8=`date +%s`
#total time cost
echo "`date +"%Y-%m-%d %H:%M:%S"`,total time cost `expr ${TIME8} - ${TIME1}`" >>${LOG_FILE}
