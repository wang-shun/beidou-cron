alter table `cprounitadx0` drop column `mtime`;
alter table `cprounitadx0` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx0` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx0` drop column `audit_adx_type`;
alter table `cprounitadx0` drop KEY `IN_CPROUNITADX0_AUDIT_ADX_TYPE`;

alter table `cprounitadx1` drop column `mtime`;
alter table `cprounitadx1` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx1` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx1` drop column `audit_adx_type`;
alter table `cprounitadx1` drop KEY `IN_CPROUNITADX1_AUDIT_ADX_TYPE`;

alter table `cprounitadx2` drop column `mtime`;
alter table `cprounitadx2` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx2` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx2` drop column `audit_adx_type`;
alter table `cprounitadx2` drop KEY `IN_CPROUNITADX2_AUDIT_ADX_TYPE`;

alter table `cprounitadx3` drop column `mtime`;
alter table `cprounitadx3` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx3` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx3` drop column `audit_adx_type`;
alter table `cprounitadx3` drop KEY `IN_CPROUNITADX3_AUDIT_ADX_TYPE`;

alter table `cprounitadx4` drop column `mtime`;
alter table `cprounitadx4` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx4` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx4` drop column `audit_adx_type`;
alter table `cprounitadx4` drop KEY `IN_CPROUNITADX4_AUDIT_ADX_TYPE`;

alter table `cprounitadx5` drop column `mtime`;
alter table `cprounitadx5` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx5` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx5` drop column `audit_adx_type`;
alter table `cprounitadx5` drop KEY `IN_CPROUNITADX5_AUDIT_ADX_TYPE`;

alter table `cprounitadx6` drop column `mtime`;
alter table `cprounitadx6` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx6` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx6` drop column `audit_adx_type`;
alter table `cprounitadx6` drop KEY `IN_CPROUNITADX6_AUDIT_ADX_TYPE`;

alter table `cprounitadx7` drop column `mtime`;
alter table `cprounitadx7` modify column `adx_type` int(10) NOT NULL DEFAULT '0' COMMENT '投放的类型(1：google)';
alter table `cprounitadx7` change `audit_state_0` `google_audit_state` int(20) NOT NULL DEFAULT '0' COMMENT 'google审核状态(0：已批准，1：审核中，2：已拒登)';
alter table `cprounitadx7` drop column `audit_adx_type`;
alter table `cprounitadx7` drop KEY `IN_CPROUNITADX7_AUDIT_ADX_TYPE`;