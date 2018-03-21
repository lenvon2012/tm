--
-- Table structure for table `group_FavoriteModel`
--

DROP TABLE IF EXISTS `group_FavoriteModel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_FavoriteModel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modelId` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
