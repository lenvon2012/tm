-- MySQL dump 10.13  Distrib 5.5.38, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: tm
-- ------------------------------------------------------
-- Server version	5.5.38-0ubuntu0.12.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chief_staff_detail`
--

DROP TABLE IF EXISTS `chief_staff_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chief_staff_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `QQNum` varchar(255) DEFAULT NULL,
  `companyName` varchar(255) DEFAULT NULL,
  `created` bigint(20) DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `price` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `startNum` int(11) NOT NULL,
  `wangwang` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chief_staff_detail`
--

LOCK TABLES `chief_staff_detail` WRITE;
/*!40000 ALTER TABLE `chief_staff_detail` DISABLE KEYS */;
INSERT INTO `chief_staff_detail` VALUES (1,'','鑫盛网络服务公司',1409735554034,'http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2yYkSaXXXXXXEXXXXXXXXXXXX-1039626382.jpg','1000','10个中差评开始处理  底价10元',20,'流行寂寞1'),(2,'','隆泰评价处理公司',1409735554034,'http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2ky.MaXXXXXcFXXXXXXXXXXXX-1039626382.jpg','1100','11元一条，20条起',20,'rxboy123456:售后经理'),(3,'','鑫弘售后工作室',1409735554034,'http://img02.taobaocdn.com/imgextra/i2/1039626382/TB2LvgOaXXXXXaCXXXXXXXXXXXX-1039626382.png','800','20个起接/8元每条',20,'念520456'),(4,'','怡心电子商务有限公司',1409735554034,'http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2JDIMaXXXXXcxXXXXXXXXXXXX-1039626382.png','1000','处理10个起，10元可议价',10,'chenyan809224973'),(5,'','百合中差评',1409735554034,'http://img01.taobaocdn.com/imgextra/i1/1039626382/TB27dcQaXXXXXbDXXXXXXXXXXXX-1039626382.gif','1500','承接中差评个数按 15元一个 包赔付',10,'shirley4319'),(6,'','悟空电子商务服务公司',1409735554034,'http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2YipkapXXXXa8XpXXXXXXXXXX-1039626382.png','1000','10元起步，1个起接单，量大优惠',1,'love谁love'),(7,'','淘专家删评工作室',1409735554034,'http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2BYlHapXXXXaVXXXXXXXXXXXX-1039626382.jpg','1000','10元每条 8个起接',8,'lucy5202002'),(8,'','帮帮淘网络',1409735554034,'http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2EmNyapXXXXXmXXXXXXXXXXXX-1039626382.jpg','1100','11元一条，可议价，5个起接',5,'zy5201314zt'),(9,'','中差评修改工作室',1409735554034,'http://img04.taobaocdn.com/imgextra/i4/1039626382/TB21ZZNaXXXXXb4XXXXXXXXXXXX-1039626382.png','1000','20个以上起接  优惠价10元起',20,'maoyang365');
/*!40000 ALTER TABLE `chief_staff_detail` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-09-03 17:26:31
