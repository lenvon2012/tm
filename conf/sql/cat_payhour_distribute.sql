DROP TABLE IF EXISTS `cat_payhour_distribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cat_payhour_distribute` (
  `cid` bigint(20) NOT NULL,
  `clock0` int(11) NOT NULL,
  `clock1` int(11) NOT NULL,
  `clock10` int(11) NOT NULL,
  `clock11` int(11) NOT NULL,
  `clock12` int(11) NOT NULL,
  `clock13` int(11) NOT NULL,
  `clock14` int(11) NOT NULL,
  `clock15` int(11) NOT NULL,
  `clock16` int(11) NOT NULL,
  `clock17` int(11) NOT NULL,
  `clock18` int(11) NOT NULL,
  `clock19` int(11) NOT NULL,
  `clock2` int(11) NOT NULL,
  `clock20` int(11) NOT NULL,
  `clock21` int(11) NOT NULL,
  `clock22` int(11) NOT NULL,
  `clock23` int(11) NOT NULL,
  `clock3` int(11) NOT NULL,
  `clock4` int(11) NOT NULL,
  `clock5` int(11) NOT NULL,
  `clock6` int(11) NOT NULL,
  `clock7` int(11) NOT NULL,
  `clock8` int(11) NOT NULL,
  `clock9` int(11) NOT NULL,
  PRIMARY KEY (`cid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;