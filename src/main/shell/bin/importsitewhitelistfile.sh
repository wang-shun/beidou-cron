#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importsitewhitelistfile.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/importsitewhitefile.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="��������Ŀ¼${SITE_DATA_PATH}ʧ��"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#���ݰٶ�����������վ����
CONF_SH=${WHITE_SITE_FILE}
[ -f "${CONF_SH}" ] && mv $CONF_SH ${WHITE_SITE_FILE_BAK}
CONF_SH=${WHITE_SITE_FILE_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 

#ץȡ�ļ�����֤MD5
msg="wget�ļ�${WHITE_SITE_FILE}ʧ��"
wget -q  ${WHITE_SITE_URL}/${WHITE_SITE_FILE}
alert $? "${msg}"

msg="wget�ļ�${WHITE_SITE_FILE_MD5}ʧ��"
wget  -q ${WHITE_SITE_URL}/${WHITE_SITE_FILE_MD5}
alert $? "${msg}"

msg="${WHITE_SITE_FILE}�ļ���md5У��ʧ��"
md5sum -c ${WHITE_SITE_FILE_MD5}
alert $? "${msg}"

msg="${WHITE_SITE_FILE}�ļ�Ϊ�գ���PM���"
size=`cat ${WHITE_SITE_FILE} | wc -w`
if [ ${size} -eq 0 ]; then
    mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error
    alert 1 "${msg}"
fi

msg="${WHITE_SITE_FILE}�ļ�������Ϊ15����PM���"
for nf in `awk -F'\t' '{print NF}' ${WHITE_SITE_FILE}`; do
    if [ $nf -ne 15 ]; then
        mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error    
        alert 1 "${msg}" 
    fi
done

#msg="${WHITE_SITE_FILE}�ļ��д�����ҵ���಻Ϊ260��270�ļ�¼����PM���"
#awk -F'\t' '{print $6}' ${WHITE_SITE_FILE} > tmpfile.$$
#while read trade
#do
#    if [ -z "$trade" ] || ( [ $trade -ne 260 ] && [ $trade -ne 270 ] ); then 
#        mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error
#        alert 1 "${msg}"
#    fi
#done <tmpfile.$$
#rm -rf tmpfile.$$
