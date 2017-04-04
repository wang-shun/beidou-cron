alter table `cprounitadx0` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx0` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx0` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx0` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx0` add KEY `IN_CPROUNITADX0_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx1` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx1` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx1` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx1` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx1` add KEY `IN_CPROUNITADX1_AUDIT_ADX_TYPE` (`audit_adx_type`);


alter table `cprounitadx2` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx2` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx2` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx2` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx2` add KEY `IN_CPROUNITADX2_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx3` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx3` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx3` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx3` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx3` add KEY `IN_CPROUNITADX3_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx4` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx4` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx4` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx4` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx4` add KEY `IN_CPROUNITADX4_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx5` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx5` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx5` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx5` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx5` add KEY `IN_CPROUNITADX5_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx6` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx6` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx6` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx6` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx6` add KEY `IN_CPROUNITADX6_AUDIT_ADX_TYPE` (`audit_adx_type`);

alter table `cprounitadx7` add column `mtime` datetime DEFAULT NULL COMMENT ‘最后更新时间’;
alter table `cprounitadx7` modify column `adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '投放的adx类型(按位获取，1：google，2：ifeng，3：sohu)采用default值为无效数据';
alter table `cprounitadx7` change `google_audit_state` `audit_state_0` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx7` add column `audit_adx_type` bigint(20) NOT NULL DEFAULT '0' COMMENT '审核过的adx类型(按位获取，1：google，2：ifeng，3：sohu)';
alter table `cprounitadx7` add KEY `IN_CPROUNITADX7_AUDIT_ADX_TYPE` (`audit_adx_type`);

