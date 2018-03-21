DROP TABLE IF EXISTS `activity0`;
CREATE TABLE `activity0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity1`;
CREATE TABLE `activity1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity2`;
CREATE TABLE `activity2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity3`;
CREATE TABLE `activity3` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity4`;
CREATE TABLE `activity4` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity5`;
CREATE TABLE `activity5` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity6`;
CREATE TABLE `activity6` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity7`;
CREATE TABLE `activity7` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity8`;
CREATE TABLE `activity8` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity9`;
CREATE TABLE `activity9` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity10`;
CREATE TABLE `activity10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity11`;
CREATE TABLE `activity11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity12`;
CREATE TABLE `activity12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity13`;
CREATE TABLE `activity13` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity14`;
CREATE TABLE `activity14` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `activity15`;
CREATE TABLE `activity15` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityDescription` varchar(255) DEFAULT NULL,
  `activityEndTime` bigint(20) DEFAULT NULL,
  `activityStartTime` bigint(20) DEFAULT NULL,
  `activityTitle` varchar(255) DEFAULT NULL,
  `activityType` int(11) DEFAULT '0',
  `buyLimit` int(11) DEFAULT '0',
  `createTime` bigint(20) DEFAULT NULL,
  `itemCount` int(11) DEFAULT '0',
  `items` varchar(2045) DEFAULT NULL,
  `mjsActivityId` bigint(20) DEFAULT '0',
  `mjsParams` varchar(4095) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tmActivityStatus` int(11) DEFAULT '0',
  `updateTs` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT NULL,
  `tmplHtml` varchar(4095) DEFAULT NULL,
  `remark` varchar(127) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

