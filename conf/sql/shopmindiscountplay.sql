 DROP TABLE IF EXISTS `shop_min_discount_play`;
 CREATE TABLE `shop_min_discount_play` (
  `userId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `minDiscountRate` int(11) DEFAULT '0',
  `updateTs` bigint(20) NOT NULL,
  `userNick` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  KEY `userNick` (`userNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;