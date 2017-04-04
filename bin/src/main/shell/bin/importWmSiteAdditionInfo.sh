#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importWmSiteAdditionInfo.sh
reader_list=liangshimu

LOG_FILE=${LOG_PATH}/importWmSiteAdditionInfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

msg="����ip,uv,siteHeat�����쳣��������ֶ��ָ�"
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WMSiteIndexImporter -s 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

# if the relt of "java" is wrong then send error message
alert $? "${msg}"

#add by zhangxu since 2014-05
#������unionsite��ر�ϲ�
COMPOSITE_DATA_FILE="${TMPDATA_PATH}/load_data_beidouext_composite_unionsite_tmp.sql"

msg="drop composite_unionsite��ʱ��ʧ��"
runsql_xdb "drop table if exists beidouext.composite_unionsite_tmp;"
alert $? "${msg}"

db_sql="use beidouext; create table composite_unionsite_tmp like composite_unionsite;"
msg="ִ���½�composite_unionsite��ʱ��ʧ��"
runsql_xdb "$db_sql" 
alert $? "${msg}"

msg="���ɵ���ۺ���վ������ʧ��"
runsql_xdb_read "use beidouext;SELECT u.siteid, u.siteurl, u.firsttradeid, u.secondtradeid, u.isdomain, u.parentid, u.valid, u.invalidtime, u.jointime, u.currentvalid, us.srchs, us.adviews, us.ips, us.cookies, us.clks, us.cost, us.suporttype, (((us.film_srchs>=50)<<2)|((us.flow_srchs>=50)<<1)|(us.fixed_srchs>=50))&us.displaytype AS displaytype, us.size, us.adblockthruput, us.fixed_srchs, us.fixed_adviews, us.fixed_clks, us.fixed_cost, us.flow_srchs, us.flow_adviews, us.flow_clks, us.flow_cost, us.film_srchs, us.film_adviews, us.film_clks, us.film_cost, ui.sitename, ui.sitedesc, ui.filter, ui.certification, ui.finanobj, ui.credit, ui.direct, ui.channel, ui.cheats, ui.sitelink, ui.snapshot, ui.site_source,  ubs.scale, ubs.ratecmp, ubs.scorecmp, ubs.cmplevel, ubs.q1, ubs.q2, ubs.thruputtype, ubs.sizethruput, ubs.score FROM beidouext.unionsite u JOIN beidouext.unionsiteinfos ui ON u.siteid = ui.siteid JOIN beidouext.unionsitestat us ON u.siteid = us.siteid JOIN beidouext.unionsitebdstat ubs ON u.siteid = ubs.siteid;" "${COMPOSITE_DATA_FILE}"
alert $? "${msg}"

msg="������վ�ۺϱ�ʧ��"
runsql_xdb "load data local infile '${COMPOSITE_DATA_FILE}' into table beidouext.composite_unionsite_tmp character set gbk"
alert $? "${msg}"

msg="��������վ�ۺ���Ϣ��ʱ��ʧ��"
runsql_xdb "drop table if exists beidouext.composite_unionsite; rename table beidouext.composite_unionsite_tmp to beidouext.composite_unionsite"
alert $? "${msg}"


