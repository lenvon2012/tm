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
-- Table structure for table `trade_display_0`
--

DROP TABLE IF EXISTS `trade_display_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_0` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_0`
--

LOCK TABLES `trade_display_0` WRITE;
/*!40000 ALTER TABLE `trade_display_0` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_0` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_1`
--

DROP TABLE IF EXISTS `trade_display_1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_1` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_1`
--

LOCK TABLES `trade_display_1` WRITE;
/*!40000 ALTER TABLE `trade_display_1` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_1` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_2`
--

DROP TABLE IF EXISTS `trade_display_2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_2` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_2`
--

LOCK TABLES `trade_display_2` WRITE;
/*!40000 ALTER TABLE `trade_display_2` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_3`
--

DROP TABLE IF EXISTS `trade_display_3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_3` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_3`
--

LOCK TABLES `trade_display_3` WRITE;
/*!40000 ALTER TABLE `trade_display_3` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_3` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_4`
--

DROP TABLE IF EXISTS `trade_display_4`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_4` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_4`
--

LOCK TABLES `trade_display_4` WRITE;
/*!40000 ALTER TABLE `trade_display_4` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_4` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_5`
--

DROP TABLE IF EXISTS `trade_display_5`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_5` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_5`
--

LOCK TABLES `trade_display_5` WRITE;
/*!40000 ALTER TABLE `trade_display_5` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_5` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_6`
--

DROP TABLE IF EXISTS `trade_display_6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_6` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_6`
--

LOCK TABLES `trade_display_6` WRITE;
/*!40000 ALTER TABLE `trade_display_6` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_6` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_7`
--

DROP TABLE IF EXISTS `trade_display_7`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_7` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_7`
--

LOCK TABLES `trade_display_7` WRITE;
/*!40000 ALTER TABLE `trade_display_7` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_7` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_8`
--

DROP TABLE IF EXISTS `trade_display_8`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_8` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_8`
--

LOCK TABLES `trade_display_8` WRITE;
/*!40000 ALTER TABLE `trade_display_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_8` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_9`
--

DROP TABLE IF EXISTS `trade_display_9`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_9` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_9`
--

LOCK TABLES `trade_display_9` WRITE;
/*!40000 ALTER TABLE `trade_display_9` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_9` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_10`
--

DROP TABLE IF EXISTS `trade_display_10`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_10` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_10`
--

LOCK TABLES `trade_display_10` WRITE;
/*!40000 ALTER TABLE `trade_display_10` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_10` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_11`
--

DROP TABLE IF EXISTS `trade_display_11`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_11` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_11`
--

LOCK TABLES `trade_display_11` WRITE;
/*!40000 ALTER TABLE `trade_display_11` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_11` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_12`
--

DROP TABLE IF EXISTS `trade_display_12`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_12` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_12`
--

LOCK TABLES `trade_display_12` WRITE;
/*!40000 ALTER TABLE `trade_display_12` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_12` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_13`
--

DROP TABLE IF EXISTS `trade_display_13`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_13` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_13`
--

LOCK TABLES `trade_display_13` WRITE;
/*!40000 ALTER TABLE `trade_display_13` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_13` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_14`
--

DROP TABLE IF EXISTS `trade_display_14`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_14` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_14`
--

LOCK TABLES `trade_display_14` WRITE;
/*!40000 ALTER TABLE `trade_display_14` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_14` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


--
-- Table structure for table `trade_display_15`
--

DROP TABLE IF EXISTS `trade_display_15`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trade_display_15` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `consignTime` bigint(20) DEFAULT NULL,
  `consignTimeDay` bigint(20) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `createdDay` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `endTimeDay` bigint(20) DEFAULT NULL,
  `modified` bigint(20) DEFAULT NULL,
  `modifiedDay` bigint(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `payTime` bigint(20) DEFAULT NULL,
  `payTimeDay` bigint(20) DEFAULT NULL,
  `payment` double NOT NULL,
  `postFee` double NOT NULL,
  `price` double NOT NULL,
  `receivedPayment` double NOT NULL,
  `receiverAddress` varchar(255) DEFAULT NULL,
  `receiverMobile` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `totalFee` double NOT NULL,
  `tradeFrom` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `endtime_day` (`endTimeDay`),
  KEY `idx_buyer_nick` (`buyerNick`),
  KEY `idx_tid` (`tid`),
  KEY `created` (`created`),
  KEY `paytime_day` (`payTimeDay`),
  KEY `user_id` (`userId`),
  KEY `created_day` (`createdDay`),
  KEY `mobile` (`receiverMobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trade_display_15`
--

LOCK TABLES `trade_display_15` WRITE;
/*!40000 ALTER TABLE `trade_display_15` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_display_15` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-06-19 10:54:20
