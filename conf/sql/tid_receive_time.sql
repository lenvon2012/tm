 CREATE TABLE `tid_receive_time` (
  `tid` bigint(20) NOT NULL,
  `buyerNick` varchar(255) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `receiveTime` bigint(20) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8