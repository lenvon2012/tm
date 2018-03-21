/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50529
Source Host           : 127.0.0.1:3306
Source Database       : tm

Target Server Type    : MYSQL
Target Server Version : 50529
File Encoding         : 65001

Date: 2017-04-17 13:54:10
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for item_cat_props_new
-- ----------------------------
DROP TABLE IF EXISTS `item_cat_props_new`;
CREATE TABLE `item_cat_props_new` (
  `cid` bigint(20) NOT NULL AUTO_INCREMENT,
  `props` longtext,
  `input_pids` longtext,
  PRIMARY KEY (`cid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB AUTO_INCREMENT=50116003 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of item_cat_props_new
-- ----------------------------
INSERT INTO `item_cat_props_new` VALUES ('162104', '13328588:492838729;20021:28351', null);
INSERT INTO `item_cat_props_new` VALUES ('162205', '122216347:740138901', null);
INSERT INTO `item_cat_props_new` VALUES ('290805', null, null);
INSERT INTO `item_cat_props_new` VALUES ('50008899', '148158672:4467397;20000:1030290041;6861561:112400;122216347:740138901', null);
INSERT INTO `item_cat_props_new` VALUES ('50009211', '20000:3236884;20983:7189740', '148060595,149128818,13790194');
INSERT INTO `item_cat_props_new` VALUES ('50010527', '20000:3236884;24477:20533;122216343:29543', '20000');
INSERT INTO `item_cat_props_new` VALUES ('50010850', '122216347:740138901', null);
INSERT INTO `item_cat_props_new` VALUES ('50011123', '122216507:113060;122216348:29444;122216586:118072222;20663:664054090;20000:3253120;122216515:27454;122216345:3288679;42722636:248572013', null);
INSERT INTO `item_cat_props_new` VALUES ('50012424', '20000:3236884;122216608:42552;24477:20533', '20000');
INSERT INTO `item_cat_props_new` VALUES ('50012579', null, '149128818,20000,20984');
INSERT INTO `item_cat_props_new` VALUES ('50013238', '122216608:20533', '13021751,20000,6103476');
INSERT INTO `item_cat_props_new` VALUES ('50015504', null, null);
INSERT INTO `item_cat_props_new` VALUES ('50016742', '20000:114627328', '20000');
INSERT INTO `item_cat_props_new` VALUES ('50019126', '122216608:20532', '20000,6103476');
INSERT INTO `item_cat_props_new` VALUES ('50114001', null, null);
INSERT INTO `item_cat_props_new` VALUES ('50114004', null, null);
INSERT INTO `item_cat_props_new` VALUES ('50116002', null, null);
