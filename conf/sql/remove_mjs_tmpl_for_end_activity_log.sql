 DROP TABLE IF EXISTS `remove_mjs_tmpl_for_end_activity_log`;
 CREATE TABLE `remove_mjs_tmpl_for_end_activity_log` (
  `activityId` bigint(20) NOT NULL,
  `activityType` int(11) NOT NULL,
  `jobTs` bigint(20) DEFAULT NULL,
  `numIids` varchar(2045) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`activityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;