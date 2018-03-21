DROP TABLE IF EXISTS `fenxiangtoken` 
CREATE TABLE `fenxiangtoken` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Sweibo_Token` varchar(255) DEFAULT NULL,
  `UserId` bigint(20) DEFAULT NULL,
  `openid` varchar(255) DEFAULT NULL,
  `openkey` varchar(255) DEFAULT NULL,
  `qq_Token` varchar(255) DEFAULT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8