DROP TABLE IF EXISTS `item0`;
CREATE TABLE `item0` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS `item1`;
CREATE TABLE `item1` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item2`;
CREATE TABLE `item2` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item3`;
CREATE TABLE `item3` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item4`;
CREATE TABLE `item4` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item5`;
CREATE TABLE `item5` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS `item6`;
CREATE TABLE `item6` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item7`;
CREATE TABLE `item7` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item8`;
CREATE TABLE `item8` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item9`;
CREATE TABLE `item9` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item10`;
CREATE TABLE `item10` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item11`;
CREATE TABLE `item11` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item12`;
CREATE TABLE `item12` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item13`;
CREATE TABLE `item13` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item14`;
CREATE TABLE `item14` (
  `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
    `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;
DROP TABLE IF EXISTS `item15`;
CREATE TABLE `item15` (
   `numIid` bigint(20) NOT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `deListTime` bigint(20) DEFAULT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `picURL` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `sellerCids` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `salesCount` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `propsName` text NOT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`numIid`),
  KEY `ts` (`ts`),
  KEY `user_id` (`userId`)
) ENGINE=Innodb DEFAULT CHARSET=utf8 ;


alter table item0 add column outerId varchar(63) default null;
alter table item1 add column outerId varchar(63) default null;
alter table item2 add column outerId varchar(63) default null;
alter table item3 add column outerId varchar(63) default null;
alter table item4 add column outerId varchar(63) default null;
alter table item5 add column outerId varchar(63) default null;
alter table item6 add column outerId varchar(63) default null;
alter table item7 add column outerId varchar(63) default null;
alter table item8 add column outerId varchar(63) default null;
alter table item9 add column outerId varchar(63) default null;
alter table item10 add column outerId varchar(63) default null;
alter table item11 add column outerId varchar(63) default null;
alter table item12 add column outerId varchar(63) default null;
alter table item13 add column outerId varchar(63) default null;
alter table item14 add column outerId varchar(63) default null;
alter table item15 add column outerId varchar(63) default null;