--
-- Table structure for table `associated_items`
--

DROP TABLE IF EXISTS `associated_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `associated_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `numIid` bigint(20) DEFAULT NULL,
  `planId` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=326 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
