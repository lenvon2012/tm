DROP TABLE IF EXISTS `user_cdn_pic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_cdn_pic` (
  `picId` bigint(20) NOT NULL,
  `cdnPath` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  PRIMARY KEY (`picId`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;