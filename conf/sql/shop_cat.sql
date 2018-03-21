-- MySQL dump 10.13  Distrib 5.1.62, for unknown-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: bus
-- ------------------------------------------------------
-- Server version	5.1.62-log

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
-- Table structure for table `shop_cat`
--

DROP TABLE IF EXISTS `shop_cat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shop_cat` (
  `cid` bigint(20) NOT NULL,
  `isParent` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parentCid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_cat`
--

LOCK TABLES `shop_cat` WRITE;
/*!40000 ALTER TABLE `shop_cat` DISABLE KEYS */;
INSERT INTO `shop_cat` VALUES (13,'\0','手机',0),(1043,'\0','笔记本电脑',0),(11,'\0','电脑硬件/台式机/网络设备',0),(1103,'\0','IP卡/网络电话/在线影音充值',0),(12,'\0','MP3/MP4/iPod/录音笔',0),(17,'\0','数码相机/摄像机/图形冲印',0),(1048,'\0','3C数码配件市场',0),(1046,'\0','家用电器/hifi音响/耳机',0),(1041,'\0','移动联通充值中心/IP长途',0),(1105,'\0','闪存卡/U盘/移动存储',0),(37,'\0','男装',0),(1106,'\0','运动鞋',0),(1104,'\0','个人护理/保健/按摩器材',0),(1102,'\0','腾讯QQ专区',0),(14,'\0','女装/流行女装',0),(1056,'\0','女鞋',0),(1055,'\0','女士内衣/男士内衣/家居服',0),(15,'\0','美容护肤/美体/精油',0),(23,'\0','珠宝/钻石/翡翠/黄金',0),(31,'\0','箱包皮具/热销女包/男包',0),(1044,'\0','品牌手表/流行手表',0),(1054,'\0','饰品/流行首饰/时尚饰品',0),(18,'\0','运动/瑜伽/健身/球迷用品',0),(1082,'\0','流行男鞋/皮鞋',0),(1045,'\0','户外/军品/旅游/机票',0),(1040,'\0','ZIPPO/瑞士军刀/饰品/眼镜',0),(22,'\0','汽车/配件/改装/摩托/自行车',0),(24,'\0','居家日用/厨房餐饮/卫浴洗浴',0),(1122,'\0','时尚家饰/工艺品/十字绣',0),(1050,'\0','家具/家具定制/宜家代购',0),(1049,'\0','床上用品/靠垫/窗帘/布艺',0),(21,'\0','办公设备/文具/耗材',0),(36,'\0','网络游戏装备/游戏币/帐号/代练',0),(26,'\0','装潢/灯具/五金/安防/卫浴',0),(1051,'\0','保健品/滋补品',0),(29,'\0','食品/茶叶/零食/特产',0),(1020,'\0','母婴用品/奶粉/孕妇装',0),(16,'\0','电玩/配件/游戏/攻略',0),(30,'\0','玩具/动漫/模型/卡通',0),(35,'\0','网络游戏点卡',0),(34,'\0','书籍/杂志/报纸',0),(33,'\0','音乐/影视/明星/乐器',0),(20,'\0','古董/邮币/字画/收藏',0),(32,'\0','宠物/宠物食品及用品',0),(27,'\0','成人用品/避孕用品/情趣内衣',0),(1042,'\0','网店/网络服务/软件',0),(1053,'\0','演出/吃喝玩乐折扣券',0),(1047,'\0','鲜花速递/蛋糕配送/园艺花艺',0),(1062,'\0','童装/婴儿服/鞋帽',0),(1153,'\0','运动服',0),(1154,'\0','服饰配件/皮带/帽子/围巾',0),(2182,'\0','医药保健',0),(2183,'\0','吃喝玩乐折扣券',0),(2184,'\0','房产',0),(2185,'\0','外卖/外送/订餐服务',0),(2202,'\0','个性定制/设计服务/DIY',0),(2222,'\0','本地化生活服务',0),(2223,'\0','电影/演出/体育赛事',0),(1345,'\0','旅行社',0),(2242,'\0','美发护发/假发',0),(2243,'\0','彩妆/香水/美妆工具',0);
/*!40000 ALTER TABLE `shop_cat` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-11-02 17:43:37
