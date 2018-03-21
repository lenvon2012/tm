DROP TABLE IF EXISTS `searchkey_`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `searchkey_` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cid` int(11) DEFAULT '-1',
  `click` int(11) DEFAULT '-1',
  `competition` int(11) DEFAULT '-1',
  `ctr` int(11) DEFAULT '-1',
  `lastINWordUpdate` bigint(20) DEFAULT '-1',
  `price` int(11) DEFAULT '-1',
  `pv` int(11) DEFAULT '-1',
  `score` int(11) DEFAULT '-1',
  `scount` int(11) DEFAULT '-1',
  `searchFocus` int(11) DEFAULT '-1',
  `status` int(11) DEFAULT '0',
  `strikeFocus` int(11) DEFAULT '-1',
  `totalPayed` int(11) DEFAULT '-1',
  `word` varchar(127) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `word` (`word`)
) ENGINE=InnoDB AUTO_INCREMENT=4590 DEFAULT CHARSET=utf8