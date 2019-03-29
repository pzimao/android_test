/*
 Navicat Premium Data Transfer

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 50723
 Source Host           : localhost:3306
 Source Schema         : app_db

 Target Server Type    : MySQL
 Target Server Version : 50723
 File Encoding         : 65001

 Date: 29/03/2019 14:26:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `app_name` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `provided_pkg_name` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `actual_pkg_name` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `dl_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `dl_state` int(10) NOT NULL DEFAULT 0 COMMENT 'APK文件下载状态:\r\n0 未下载；\r\n-1 下载出错；\r\n1 下载完成；\r\n2 跳过大文件；',
  `desc` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '通过包名分析出的信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 190556 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app_domain
-- ----------------------------
DROP TABLE IF EXISTS `app_domain`;
CREATE TABLE `app_domain`  (
  `app_id` int(10) UNSIGNED NOT NULL,
  `domain` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `label` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`app_id`, `domain`) USING BTREE,
  INDEX `FK2`(`domain`) USING BTREE,
  CONSTRAINT `FK1` FOREIGN KEY (`app_id`) REFERENCES `app` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK2` FOREIGN KEY (`domain`) REFERENCES `domain` (`domain`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app_info
-- ----------------------------
DROP TABLE IF EXISTS `app_info`;
CREATE TABLE `app_info`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `jid` int(11) NOT NULL,
  `app_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '存储app_name',
  `icon_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dl_url` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dl_num` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `shop_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `size` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `permission` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `abstract` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `updatetime` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `packagename` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `appID` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46576 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for domain
-- ----------------------------
DROP TABLE IF EXISTS `domain`;
CREATE TABLE `domain`  (
  `domain` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `domain_desc` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备案网站查到的信息',
  `domain_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '人工标注的信息',
  PRIMARY KEY (`domain`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for domain_cname
-- ----------------------------
DROP TABLE IF EXISTS `domain_cname`;
CREATE TABLE `domain_cname`  (
  `domain` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `cname` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`domain`, `cname`) USING BTREE,
  CONSTRAINT `domain_cname_ibfk_1` FOREIGN KEY (`domain`) REFERENCES `domain` (`domain`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for domain_ip
-- ----------------------------
DROP TABLE IF EXISTS `domain_ip`;
CREATE TABLE `domain_ip`  (
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`domain`, `ip`) USING BTREE,
  CONSTRAINT `domain_ip_ibfk_1` FOREIGN KEY (`domain`) REFERENCES `domain` (`domain`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for 不属于app的域名
-- ----------------------------
DROP VIEW IF EXISTS `不属于app的域名`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `不属于app的域名` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`actual_pkg_name` AS `pkg_name`,`domain`.`domain` AS `pub_domain`,`domain`.`domain_desc` AS `domain_desc` from ((`app` join `app_domain`) join `domain`) where ((`app`.`actual_pkg_name` = `app`.`provided_pkg_name`) and (`app`.`id` = `app_domain`.`app_id`) and (`domain`.`domain` = `app_domain`.`domain`) and (`app_domain`.`label` = -(1))) order by `app`.`id`;

-- ----------------------------
-- View structure for 属于app的域名
-- ----------------------------
DROP VIEW IF EXISTS `属于app的域名`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `属于app的域名` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`actual_pkg_name` AS `pkg_name`,`domain`.`domain` AS `domain`,`domain`.`domain_desc` AS `domain_desc` from ((`app` join `app_domain`) join `domain`) where ((`app`.`actual_pkg_name` = `app`.`provided_pkg_name`) and (`app`.`id` = `app_domain`.`app_id`) and (`domain`.`domain` = `app_domain`.`domain`) and (`app_domain`.`label` = 1)) order by `app`.`id`;

-- ----------------------------
-- View structure for 视图1_所有域名
-- ----------------------------
DROP VIEW IF EXISTS `视图1_所有域名`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `视图1_所有域名` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`provided_pkg_name` AS `package_name`,`domain`.`domain` AS `domain`,`domain`.`domain_desc` AS `domain_desc`,`domain`.`domain_info` AS `domain_info` from ((`app` join `domain`) join `app_domain`) where ((`app`.`id` = `app_domain`.`app_id`) and (`app_domain`.`domain` = `domain`.`domain`) and (`app`.`provided_pkg_name` = `app`.`actual_pkg_name`)) order by `app`.`id`;

-- ----------------------------
-- View structure for 视图2_api或者sdk域名
-- ----------------------------
DROP VIEW IF EXISTS `视图2_api或者sdk域名`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `视图2_api或者sdk域名` AS select `domain`.`domain` AS `domain` from `domain` where ((`domain`.`domain` like '%api.%') or (`domain`.`domain` like '%sdk.%') or (`domain`.`domain` like '%sc-troy%') or (`domain`.`domain` like '%umeng.%') or (`domain`.`domain` like '%mumu%') or (`domain`.`domain` like '%appjiagu%'));

-- ----------------------------
-- View structure for 视图2_app引用api或者sdk域名
-- ----------------------------
DROP VIEW IF EXISTS `视图2_app引用api或者sdk域名`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `视图2_app引用api或者sdk域名` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`actual_pkg_name` AS `actual_pkg_name`,`app_domain`.`domain` AS `domain`,`domain`.`domain_desc` AS `domain_desc` from ((`app` join `app_domain`) join `domain`) where ((not((`domain`.`domain` like '%api%'))) and (not((`domain`.`domain` like '%sdk%'))) and (not((`domain`.`domain` like '%sc-troy%'))) and (not((`domain`.`domain` like '%umeng.%'))) and (not((`domain`.`domain` like '%mumu%'))) and (not((`domain`.`domain` like '%appjiagu%'))) and (`app`.`id` = `app_domain`.`app_id`) and (`app_domain`.`domain` = `domain`.`domain`)) order by `app`.`id`;

-- ----------------------------
-- View structure for 视图2_域名按出现次数排序
-- ----------------------------
DROP VIEW IF EXISTS `视图2_域名按出现次数排序`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `视图2_域名按出现次数排序` AS select `app_domain`.`domain` AS `domain`,`domain`.`domain_desc` AS `domain_desc`,count(0) AS `出现次数` from (`app_domain` join `domain`) where (`app_domain`.`domain` = `domain`.`domain`) group by `app_domain`.`domain` order by count(0) desc;

-- ----------------------------
-- Triggers structure for table app_info
-- ----------------------------
DROP TRIGGER IF EXISTS `synAppInfo`;
delimiter ;;
CREATE TRIGGER `synAppInfo` AFTER INSERT ON `app_info` FOR EACH ROW begin
		set @var := new.app_name;
		set @var = replace(@var,'.apk','');
		set @var = replace(@var,'\','#');
		set @var = replace(@var,'/','#');
		set @var = replace(@var,':','#');
		set @var = replace(@var,'*','#');
		set @var = replace(@var,'?','#');
		set @var = replace(@var,'"','#');
		set @var = replace(@var,'<','#');
		set @var = replace(@var,'>','#');
		set @var = replace(@var,'|','#');
    insert into app (id, app.app_name, app.provided_pkg_name, app.dl_url) values (new.id, @var, new.packagename, new.dl_url);
end
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
