DROP TABLE IF EXISTS `title_optimised`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `title_optimised` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isOptimised` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=2652 DEFAULT CHARSET=utf8;