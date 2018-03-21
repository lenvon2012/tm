--
-- Table structure for table `grouped_Items`
--

DROP TABLE IF EXISTS `grouped_Items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grouped_Items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `errorMsg` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `planId` bigint(20) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=173 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
