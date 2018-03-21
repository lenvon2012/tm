/*
Navicat MySQL Data Transfer

Source Server         : con
Source Server Version : 50611
Source Host           : localhost:3306
Source Database       : tm

Target Server Type    : MYSQL
Target Server Version : 50611
File Encoding         : 65001

Date: 2013-09-25 01:34:36
*/


-- ----------------------------
-- Table structure for `ppstock_0`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_0`;
CREATE TABLE `ppstock_0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_0
-- ----------------------------
LOCK TABLES `ppstock_0` WRITE;
/*!40000 ALTER TABLE `ppstock_0` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_0` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_1`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_1`;
CREATE TABLE `ppstock_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_1
-- ----------------------------
LOCK TABLES `ppstock_1` WRITE;
/*!40000 ALTER TABLE `ppstock_1` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_1` ENABLE KEYS */;
UNLOCK TABLES;

-- ----------------------------
-- Table structure for `ppstock_2`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_2`;
CREATE TABLE `ppstock_2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_2
-- ----------------------------
LOCK TABLES `ppstock_2` WRITE;
/*!40000 ALTER TABLE `ppstock_2` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_2` ENABLE KEYS */;
UNLOCK TABLES;

-- ----------------------------
-- Table structure for `ppstock_3`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_3`;
CREATE TABLE `ppstock_3` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_3
-- ----------------------------
LOCK TABLES `ppstock_3` WRITE;
/*!40000 ALTER TABLE `ppstock_3` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_3` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_4`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_4`;
CREATE TABLE `ppstock_4` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_4
-- ----------------------------
LOCK TABLES `ppstock_4` WRITE;
/*!40000 ALTER TABLE `ppstock_4` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_4` ENABLE KEYS */;
UNLOCK TABLES;

-- ----------------------------
-- Table structure for `ppstock_5`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_5`;
CREATE TABLE `ppstock_5` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_5
-- ----------------------------
LOCK TABLES `ppstock_5` WRITE;
/*!40000 ALTER TABLE `ppstock_5` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_5` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_6`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_6`;
CREATE TABLE `ppstock_6` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_6
-- ----------------------------
LOCK TABLES `ppstock_6` WRITE;
/*!40000 ALTER TABLE `ppstock_6` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_6` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_7`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_7`;
CREATE TABLE `ppstock_7` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_7
-- ----------------------------
LOCK TABLES `ppstock_7` WRITE;
/*!40000 ALTER TABLE `ppstock_7` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_7` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_8`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_8`;
CREATE TABLE `ppstock_8` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_8
-- ----------------------------
LOCK TABLES `ppstock_8` WRITE;
/*!40000 ALTER TABLE `ppstock_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_8` ENABLE KEYS */;
UNLOCK TABLES;


-- ----------------------------
-- Table structure for `ppstock_9`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_9`;
CREATE TABLE `ppstock_9` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_9
-- ----------------------------
LOCK TABLES `ppstock_9` WRITE;
/*!40000 ALTER TABLE `ppstock_9` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_9` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_10`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_10`;
CREATE TABLE `ppstock_10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_10
-- ----------------------------
LOCK TABLES `ppstock_10` WRITE;
/*!40000 ALTER TABLE `ppstock_10` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_10` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_11`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_11`;
CREATE TABLE `ppstock_11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_11
-- ----------------------------
LOCK TABLES `ppstock_11` WRITE;
/*!40000 ALTER TABLE `ppstock_11` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_11` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_12`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_12`;
CREATE TABLE `ppstock_12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_12
-- ----------------------------
LOCK TABLES `ppstock_12` WRITE;
/*!40000 ALTER TABLE `ppstock_12` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_12` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_13`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_13`;
CREATE TABLE `ppstock_13` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_13
-- ----------------------------
LOCK TABLES `ppstock_13` WRITE;
/*!40000 ALTER TABLE `ppstock_13` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_13` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_14`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_14`;
CREATE TABLE `ppstock_14` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_14
-- ----------------------------
LOCK TABLES `ppstock_14` WRITE;
/*!40000 ALTER TABLE `ppstock_14` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_14` ENABLE KEYS */;
UNLOCK TABLES;



-- ----------------------------
-- Table structure for `ppstock_15`
-- ----------------------------
DROP TABLE IF EXISTS `ppstock_15`;
CREATE TABLE `ppstock_15` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(255) DEFAULT NULL,
  `num` bigint(20) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `saleAttr` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `skuId` bigint(20) DEFAULT NULL,
  `soldNum` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `stockAttr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `skuId` (`skuId`),
  KEY `sellerUin` (`sellerUin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ppstock_15
-- ----------------------------
LOCK TABLES `ppstock_15` WRITE;
/*!40000 ALTER TABLE `ppstock_15` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppstock_15` ENABLE KEYS */;
UNLOCK TABLES;

