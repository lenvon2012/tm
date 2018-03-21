-- MySQL dump 10.13  Distrib 5.5.28, for osx10.6 (i386)
--
-- Host: localhost    Database: tm
-- ------------------------------------------------------
-- Server version	5.5.28-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


--
-- Table structure for table `paipai_tradeitem_0`
--

DROP TABLE IF EXISTS `paipai_tradeitem_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_0`
--

LOCK TABLES `paipai_tradeitem_0` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_0` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_0` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_1`
--

DROP TABLE IF EXISTS `paipai_tradeitem_1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_1`
--

LOCK TABLES `paipai_tradeitem_1` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_1` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_1` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_2`
--

DROP TABLE IF EXISTS `paipai_tradeitem_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_2`
--

LOCK TABLES `paipai_tradeitem_2` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_2` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_3`
--

DROP TABLE IF EXISTS `paipai_tradeitem_3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_3` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_3`
--

LOCK TABLES `paipai_tradeitem_3` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_3` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_3` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_4`
--

DROP TABLE IF EXISTS `paipai_tradeitem_4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_4` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_4`
--

LOCK TABLES `paipai_tradeitem_4` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_4` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_4` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_5`
--

DROP TABLE IF EXISTS `paipai_tradeitem_5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_5` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_5`
--

LOCK TABLES `paipai_tradeitem_5` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_5` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_5` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_6`
--

DROP TABLE IF EXISTS `paipai_tradeitem_6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_6` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_6`
--

LOCK TABLES `paipai_tradeitem_6` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_6` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_6` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_7`
--

DROP TABLE IF EXISTS `paipai_tradeitem_7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_7` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_7`
--

LOCK TABLES `paipai_tradeitem_7` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_7` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_7` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_8`
--

DROP TABLE IF EXISTS `paipai_tradeitem_8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_8` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_8`
--

LOCK TABLES `paipai_tradeitem_8` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_8` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_9`
--

DROP TABLE IF EXISTS `paipai_tradeitem_9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_9` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_9`
--

LOCK TABLES `paipai_tradeitem_9` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_9` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_9` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_10`
--

DROP TABLE IF EXISTS `paipai_tradeitem_10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_10`
--

LOCK TABLES `paipai_tradeitem_10` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_10` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_10` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_11`
--

DROP TABLE IF EXISTS `paipai_tradeitem_11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_11`
--

LOCK TABLES `paipai_tradeitem_11` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_11` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_11` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_12`
--

DROP TABLE IF EXISTS `paipai_tradeitem_12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_12`
--

LOCK TABLES `paipai_tradeitem_12` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_12` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_12` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_13`
--

DROP TABLE IF EXISTS `paipai_tradeitem_13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_13` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_13`
--

LOCK TABLES `paipai_tradeitem_13` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_13` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_13` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_14`
--

DROP TABLE IF EXISTS `paipai_tradeitem_14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_14` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_14`
--

LOCK TABLES `paipai_tradeitem_14` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_14` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_14` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `paipai_tradeitem_15`
--

DROP TABLE IF EXISTS `paipai_tradeitem_15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_tradeitem_15` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTime` bigint(20) NOT NULL,
  `dealCode` varchar(255) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemDealCount` int(11) NOT NULL,
  `itemDealPrice` int(11) NOT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dealCode` (`dealCode`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_tradeitem_15`
--

LOCK TABLES `paipai_tradeitem_15` WRITE;
/*!40000 ALTER TABLE `paipai_tradeitem_15` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_tradeitem_15` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-07-10 17:59:41
