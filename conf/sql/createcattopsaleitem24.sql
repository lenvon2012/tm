DROP TABLE IF EXISTS `cat_top_sale_item_2014_05`;
CREATE TABLE `cat_top_sale_item_2014_05` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_06`;
CREATE TABLE `cat_top_sale_item_2014_06` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_07`;
CREATE TABLE `cat_top_sale_item_2014_07` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_08`;
CREATE TABLE `cat_top_sale_item_2014_08` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_09`;
CREATE TABLE `cat_top_sale_item_2014_09` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_10`;
CREATE TABLE `cat_top_sale_item_2014_10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_11`;
CREATE TABLE `cat_top_sale_item_2014_11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2014_12`;
CREATE TABLE `cat_top_sale_item_2014_12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_01`;
CREATE TABLE `cat_top_sale_item_2015_01` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_02`;
CREATE TABLE `cat_top_sale_item_2015_02` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_03`;
CREATE TABLE `cat_top_sale_item_2015_03` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_04`;
CREATE TABLE `cat_top_sale_item_2015_04` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_05`;
CREATE TABLE `cat_top_sale_item_2015_05` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_06`;
CREATE TABLE `cat_top_sale_item_2015_06` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_07`;
CREATE TABLE `cat_top_sale_item_2015_07` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_08`;
CREATE TABLE `cat_top_sale_item_2015_08` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_09`;
CREATE TABLE `cat_top_sale_item_2015_09` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_10`;
CREATE TABLE `cat_top_sale_item_2015_10` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_11`;
CREATE TABLE `cat_top_sale_item_2015_11` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2015_12`;
CREATE TABLE `cat_top_sale_item_2015_12` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2016_01`;
CREATE TABLE `cat_top_sale_item_2016_01` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2016_02`;
CREATE TABLE `cat_top_sale_item_2016_02` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2016_03`;
CREATE TABLE `cat_top_sale_item_2016_03` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cat_top_sale_item_2016_04`;
CREATE TABLE `cat_top_sale_item_2016_04` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `backCid` bigint(20) DEFAULT NULL,
  `delistTime` bigint(20) DEFAULT NULL,
  `frontcid` bigint(20) NOT NULL,
  `listTime` bigint(20) DEFAULT NULL,
  `month` bigint(20) NOT NULL,
  `numIid` bigint(20) DEFAULT NULL,
  `picPath` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `props` text,
  `sellerId` bigint(20) DEFAULT NULL,
  `sellerNick` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tradeNum` int(11) NOT NULL,
  `ts` bigint(20) DEFAULT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  `year` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `numIid` (`numIid`),
  KEY `indexNum` (`backCid`,`tradeNum`),
  KEY `sellerNick` (`sellerNick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

