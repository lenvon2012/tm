DROP TABLE IF EXISTS `promotionplay0`;
CREATE TABLE `promotionplay0` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay1`;
CREATE TABLE `promotionplay1` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay2`;
CREATE TABLE `promotionplay2` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay3`;
CREATE TABLE `promotionplay3` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay4`;
CREATE TABLE `promotionplay4` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay5`;
CREATE TABLE `promotionplay5` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay6`;
CREATE TABLE `promotionplay6` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay7`;
CREATE TABLE `promotionplay7` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay8`;
CREATE TABLE `promotionplay8` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay9`;
CREATE TABLE `promotionplay9` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay10`;
CREATE TABLE `promotionplay10` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay11`;
CREATE TABLE `promotionplay11` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay12`;
CREATE TABLE `promotionplay12` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay13`;
CREATE TABLE `promotionplay13` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay14`;
CREATE TABLE `promotionplay14` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promotionplay15`;
CREATE TABLE `promotionplay15` (
  `promotionId` bigint(20) NOT NULL,
  `createTs` bigint(20) NOT NULL,
  `decreaseAmount` bigint(20) NOT NULL,
  `discountRate` bigint(20) NOT NULL,
  `isUserTag` bit(1) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `promotionType` varchar(255) DEFAULT NULL,
  `tmActivityId` bigint(20) DEFAULT NULL,
  `tmStatus` int(11) NOT NULL,
  `updateTs` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `userTagValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`promotionId`),
  KEY `numIid` (`numIid`),
  KEY `userId` (`userId`),
  KEY `tmActivityId` (`tmActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;