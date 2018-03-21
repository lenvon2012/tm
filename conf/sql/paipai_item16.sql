-- MySQL dump 10.13  Distrib 5.5.31, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: tm
-- ------------------------------------------------------
-- Server version	5.5.31-0ubuntu0.12.04.2

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
-- Table structure for table `paipai_item_0`
--

DROP TABLE IF EXISTS `paipai_item_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_0`
--

LOCK TABLES `paipai_item_0` WRITE;
/*!40000 ALTER TABLE `paipai_item_0` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_0` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_1`
--

DROP TABLE IF EXISTS `paipai_item_1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_1`
--

LOCK TABLES `paipai_item_1` WRITE;
/*!40000 ALTER TABLE `paipai_item_1` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_1` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_2`
--

DROP TABLE IF EXISTS `paipai_item_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_2`
--

LOCK TABLES `paipai_item_2` WRITE;
/*!40000 ALTER TABLE `paipai_item_2` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_3`
--

DROP TABLE IF EXISTS `paipai_item_3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_3` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_3`
--

LOCK TABLES `paipai_item_3` WRITE;
/*!40000 ALTER TABLE `paipai_item_3` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_3` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_4`
--

DROP TABLE IF EXISTS `paipai_item_4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_4` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_4`
--

LOCK TABLES `paipai_item_4` WRITE;
/*!40000 ALTER TABLE `paipai_item_4` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_4` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_5`
--

DROP TABLE IF EXISTS `paipai_item_5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_5` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_5`
--

LOCK TABLES `paipai_item_5` WRITE;
/*!40000 ALTER TABLE `paipai_item_5` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_5` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_6`
--

DROP TABLE IF EXISTS `paipai_item_6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_6` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_6`
--

LOCK TABLES `paipai_item_6` WRITE;
/*!40000 ALTER TABLE `paipai_item_6` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_6` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_7`
--

DROP TABLE IF EXISTS `paipai_item_7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_7` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_7`
--

LOCK TABLES `paipai_item_7` WRITE;
/*!40000 ALTER TABLE `paipai_item_7` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_7` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_8`
--

DROP TABLE IF EXISTS `paipai_item_8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_8` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_8`
--

LOCK TABLES `paipai_item_8` WRITE;
/*!40000 ALTER TABLE `paipai_item_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_8` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_9`
--

DROP TABLE IF EXISTS `paipai_item_9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_9` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_9`
--

LOCK TABLES `paipai_item_9` WRITE;
/*!40000 ALTER TABLE `paipai_item_9` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_9` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_10`
--

DROP TABLE IF EXISTS `paipai_item_10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_10`
--

LOCK TABLES `paipai_item_10` WRITE;
/*!40000 ALTER TABLE `paipai_item_10` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_10` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_11`
--

DROP TABLE IF EXISTS `paipai_item_11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_11`
--

LOCK TABLES `paipai_item_11` WRITE;
/*!40000 ALTER TABLE `paipai_item_11` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_11` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_12`
--

DROP TABLE IF EXISTS `paipai_item_12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_12`
--

LOCK TABLES `paipai_item_12` WRITE;
/*!40000 ALTER TABLE `paipai_item_12` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_12` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_13`
--

DROP TABLE IF EXISTS `paipai_item_13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_13` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_13`
--

LOCK TABLES `paipai_item_13` WRITE;
/*!40000 ALTER TABLE `paipai_item_13` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_13` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_14`
--

DROP TABLE IF EXISTS `paipai_item_14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_14` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_14`
--

LOCK TABLES `paipai_item_14` WRITE;
/*!40000 ALTER TABLE `paipai_item_14` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_14` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `paipai_item_15`
--

DROP TABLE IF EXISTS `paipai_item_15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paipai_item_15` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryId` int(11) NOT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `itemCode` varchar(255) DEFAULT NULL,
  `itemName` varchar(255) DEFAULT NULL,
  `itemPrice` int(11) NOT NULL,
  `itemState` varchar(255) DEFAULT NULL,
  `picLink` varchar(255) DEFAULT NULL,
  `sellerUin` bigint(20) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `visitCount` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sellerUin` (`sellerUin`),
  KEY `itemCode` (`itemCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paipai_item_15`
--

LOCK TABLES `paipai_item_15` WRITE;
/*!40000 ALTER TABLE `paipai_item_15` DISABLE KEYS */;
/*!40000 ALTER TABLE `paipai_item_15` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-07-24 11:47:45
