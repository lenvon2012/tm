DROP TABLE IF EXISTS `category_props`;
CREATE TABLE `category_props` (
  `cid` bigint(20) NOT NULL,
  `created` bigint(20) DEFAULT NULL,
  `props` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`cid`),
  KEY `cid` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8