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

 Date: 24/03/2019 20:30:15
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
-- View structure for view_table1
-- ----------------------------
DROP VIEW IF EXISTS `view_table1`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `view_table1` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`actual_pkg_name` AS `pkg_name`,`domain`.`domain` AS `domain`,`domain`.`domain_desc` AS `domain_desc` from ((`app` join `app_domain`) join `domain`) where ((`app`.`actual_pkg_name` = `app`.`provided_pkg_name`) and (`app`.`id` = `app_domain`.`app_id`) and (`domain`.`domain` = `app_domain`.`domain`)) order by `app`.`id`;

-- ----------------------------
-- View structure for view_table2
-- ----------------------------
DROP VIEW IF EXISTS `view_table2`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `view_table2` AS select `app`.`id` AS `id`,`app`.`app_name` AS `app_name`,`app`.`actual_pkg_name` AS `pkg_name`,`domain`.`domain` AS `pub_domain`,`domain`.`domain_desc` AS `domain_desc` from ((`app` join `app_domain`) join `domain`) where ((`app`.`actual_pkg_name` = `app`.`provided_pkg_name`) and (`app`.`id` = `app_domain`.`app_id`) and (`domain`.`domain` = `app_domain`.`domain`) and ((`domain`.`domain` like '%api.%') or '%sdk.%') and (`app_domain`.`label` = -(1))) order by `app`.`id`;

SET FOREIGN_KEY_CHECKS = 1;
