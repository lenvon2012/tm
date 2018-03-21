--
-- Table structure for table `group_Plan`
--

DROP TABLE IF EXISTS `group_Plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_Plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityTitle` varchar(255) DEFAULT NULL,
  `btnName` varchar(255) DEFAULT NULL,
  `currentPriceName` varchar(255) DEFAULT NULL,
  `days` int(11) NOT NULL,
  `hours` int(11) NOT NULL,
  `itemString` varchar(400) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `minutes` int(11) NOT NULL,
  `modelId` bigint(20) DEFAULT NULL,
  `originalPriceName` varchar(255) DEFAULT NULL,
  `planName` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

