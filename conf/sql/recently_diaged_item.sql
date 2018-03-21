
CREATE TABLE `recently_diaged_item` (
  `numIid` bigint(20) NOT NULL,
  `diagTime` bigint(20) NOT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  PRIMARY KEY (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;