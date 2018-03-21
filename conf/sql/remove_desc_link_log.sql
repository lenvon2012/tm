DROP TABLE IF EXISTS `remove_desc_link_log`;
CREATE TABLE `remove_desc_link_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createTs` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `originDesc` longtext,
  `recoverTs` bigint(20) NOT NULL,
  `removedLinks` longtext,
  `status` int(11) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;