
CREATE TABLE `tm_jdp_fail_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` bigint(20) DEFAULT NULL,
  `updated` bigint(20) DEFAULT NULL,
  `failCount` int(11) NOT NULL,
  `msg` varchar(8190) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;