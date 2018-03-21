CREATE TABLE `mainsearch_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `numIid` bigint(20) NOT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `sort` varchar(31) NOT NULL DEFAULT '',
  `title` varchar(63) DEFAULT NULL,
  `ts` bigint(20) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `word` varchar(63) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;