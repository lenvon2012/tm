DROP TABLE IF EXISTS `relationed_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `relationed_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `numIid` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8;