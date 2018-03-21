DROP TABLE IF EXISTS `cat_click_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cat_click_rate` (
  `numIid` bigint(20) NOT NULL,
  `aclick` int(11) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `clickRate` double DEFAULT NULL,
  `impression` int(11) NOT NULL,
  `picUrl` varchar(255) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
