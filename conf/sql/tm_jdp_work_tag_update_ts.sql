CREATE TABLE `worktag_update_ts` (
  `worktag` varchar(255) NOT NULL,
  `comment` varchar(2046) DEFAULT NULL,
  `firstUpdateTime` bigint(20) NOT NULL,
  `lastUpdateTime` bigint(20) NOT NULL,
  PRIMARY KEY (`worktag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;