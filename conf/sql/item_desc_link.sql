DROP TABLE IF EXISTS `item_desc_links`;
CREATE TABLE `item_desc_links` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` bigint(20) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `status` bigint(20) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;