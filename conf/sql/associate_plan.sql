DROP TABLE IF EXISTS `associate_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `associate_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityNameChinese` varchar(255) DEFAULT NULL,
  `activityNameEnglish` varchar(255) DEFAULT NULL,
  `activityPrice` double NOT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `borderColor` varchar(255) DEFAULT NULL,
  `counterPrice` double NOT NULL,
  `fontColor` varchar(255) DEFAULT NULL,
  `modelId` bigint(20) DEFAULT NULL,
  `numIids` varchar(255) DEFAULT NULL,
  `originalPrice` double NOT NULL,
  `planName` varchar(255) DEFAULT NULL,
  `planWidth` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `backgroundColor` varchar(255) DEFAULT NULL,
  `days` int(11) NOT NULL,
  `hours` int(11) NOT NULL,
  `minutes` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

