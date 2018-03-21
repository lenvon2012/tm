 DROP TABLE IF EXISTS `shop_min_discount_get_log`;
 CREATE TABLE `shop_min_discount_get_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apiStatus` varchar(255) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `deleteTimes` int(11) DEFAULT '0',
  `numIid` bigint(20) DEFAULT NULL,
  `promotionId` bigint(20) DEFAULT NULL,
  `tmStatus` varchar(255) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `usedTime` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;