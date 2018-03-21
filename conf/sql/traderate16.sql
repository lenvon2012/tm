-- MySQL dump 10.13  Distrib 5.6.12, for osx10.7 (x86_64)
--
-- Host: localhost    Database: tm
-- ------------------------------------------------------
-- Server version	5.6.12

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
-- Table structure for table `trade_rate_0`
--

DROP TABLE IF EXISTS `trade_rate_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_0` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_0`
--

LOCK TABLES `trade_rate_0` WRITE;
/*!40000 ALTER TABLE `trade_rate_0` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_0` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_1`
--

DROP TABLE IF EXISTS `trade_rate_1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_1` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_1`
--

LOCK TABLES `trade_rate_1` WRITE;
/*!40000 ALTER TABLE `trade_rate_1` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_1` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_2`
--

DROP TABLE IF EXISTS `trade_rate_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_2` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_2`
--

LOCK TABLES `trade_rate_2` WRITE;
/*!40000 ALTER TABLE `trade_rate_2` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_3`
--

DROP TABLE IF EXISTS `trade_rate_3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_3` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_3`
--

LOCK TABLES `trade_rate_3` WRITE;
/*!40000 ALTER TABLE `trade_rate_3` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_3` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_4`
--

DROP TABLE IF EXISTS `trade_rate_4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_4` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_4`
--

LOCK TABLES `trade_rate_4` WRITE;
/*!40000 ALTER TABLE `trade_rate_4` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_4` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_5`
--

DROP TABLE IF EXISTS `trade_rate_5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_5` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_5`
--

LOCK TABLES `trade_rate_5` WRITE;
/*!40000 ALTER TABLE `trade_rate_5` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_5` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_6`
--

DROP TABLE IF EXISTS `trade_rate_6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_6` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_6`
--

LOCK TABLES `trade_rate_6` WRITE;
/*!40000 ALTER TABLE `trade_rate_6` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_6` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_7`
--

DROP TABLE IF EXISTS `trade_rate_7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_7` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_7`
--

LOCK TABLES `trade_rate_7` WRITE;
/*!40000 ALTER TABLE `trade_rate_7` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_7` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_8`
--

DROP TABLE IF EXISTS `trade_rate_8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_8` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_8`
--

LOCK TABLES `trade_rate_8` WRITE;
/*!40000 ALTER TABLE `trade_rate_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_8` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_9`
--

DROP TABLE IF EXISTS `trade_rate_9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_9` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_9`
--

LOCK TABLES `trade_rate_9` WRITE;
/*!40000 ALTER TABLE `trade_rate_9` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_9` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_10`
--

DROP TABLE IF EXISTS `trade_rate_10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_10` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_10`
--

LOCK TABLES `trade_rate_10` WRITE;
/*!40000 ALTER TABLE `trade_rate_10` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_10` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_11`
--

DROP TABLE IF EXISTS `trade_rate_11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_11` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_11`
--

LOCK TABLES `trade_rate_11` WRITE;
/*!40000 ALTER TABLE `trade_rate_11` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_11` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_12`
--

DROP TABLE IF EXISTS `trade_rate_12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_12` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_12`
--

LOCK TABLES `trade_rate_12` WRITE;
/*!40000 ALTER TABLE `trade_rate_12` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_12` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_13`
--

DROP TABLE IF EXISTS `trade_rate_13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_13` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_13`
--

LOCK TABLES `trade_rate_13` WRITE;
/*!40000 ALTER TABLE `trade_rate_13` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_13` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_14`
--

DROP TABLE IF EXISTS `trade_rate_14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_14` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_14`
--

LOCK TABLES `trade_rate_14` WRITE;
/*!40000 ALTER TABLE `trade_rate_14` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_14` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;



--
-- Table structure for table `trade_rate_15`
--

DROP TABLE IF EXISTS `trade_rate_15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_rate_15` (
  `oid` bigint(20) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created` bigint(20) NOT NULL,
  `itemTitle` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `rate` int(11) NOT NULL,
  `reply` varchar(255) DEFAULT NULL,
  `reverse` varchar(255) DEFAULT NULL,
  `roleType` int(11) NOT NULL,
  `sellerRate` int(11) NOT NULL,
  `sellerTs` bigint(20) NOT NULL,
  `tid` bigint(20) NOT NULL,
  `updated` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `validScore` bit(1) DEFAULT NULL,
  PRIMARY KEY (`oid`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `buyernick` (`nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_rate_15`
--

LOCK TABLES `trade_rate_15` WRITE;
/*!40000 ALTER TABLE `trade_rate_15` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_rate_15` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-26  3:10:10
