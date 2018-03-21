 DROP TABLE IF EXISTS `edit_item_price_log`;
CREATE TABLE `edit_item_price_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTs` bigint(20) NOT NULL,
  `failSkuNum` int(11) DEFAULT '0',
  `newPrice` varchar(255) DEFAULT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `originPrice` varchar(255) DEFAULT NULL,
  `skuPriceJson` longtext,
  `status` int(11) DEFAULT '0',
  `successSkuNum` int(11) DEFAULT '0',
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;