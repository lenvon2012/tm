DROP DATABASE IF EXISTS ecshop;
CREATE DATABASE ecshop default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON ecshop.* TO tm_rw@'localhost' IDENTIFIED BY 'ecshop_rw';
GRANT ALL PRIVILEGES ON ecshop.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'ecshop_pw';
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS tm;
CREATE DATABASE tm default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'58.196.167.16' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.6.151' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.6.208' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.140.55' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.22.172' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.173.215' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.189.250' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.205.125' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.49.38' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.1.209' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.1.58' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.1.83' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.221.75' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.200.5.238' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.46.80' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.47.89' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.47.114' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.47.116' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.47.119' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.135.48.172' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.132.18.75' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.132.18.151' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.241.48.36' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.0.4' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.0.80' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.0.208' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.6.70' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.1.187' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON tm.* TO tm_rw@'10.128.0.133' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;






DROP DATABASE IF EXISTS fanfanle;
CREATE DATABASE fanfanle  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON fanfanle.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON fanfanle.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON fanfanle.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS wuxizhanwai;
CREATE DATABASE wuxizhanwai  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON wuxizhanwai.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON wuxizhanwai.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON wuxizhanwai.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;


DROP DATABASE IF EXISTS qqtuiguang;
CREATE DATABASE qqtuiguang  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON qqtuiguang.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON qqtuiguang.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON qqtuiguang.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;



DROP DATABASE IF EXISTS dazhe;
CREATE DATABASE dazhe  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON dazhe.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dazhe.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dazhe.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS title;
CREATE DATABASE title  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'58.196.167.21' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'58.196.167.21' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.241.140.55' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.241.22.172' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.200.173.215' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.200.189.250' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.200.205.125' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.241.49.38' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.128.1.209' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.128.1.58' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON title.* TO tm_rw@'10.128.1.83' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;


DROP DATABASE IF EXISTS dawei;
CREATE DATABASE dawei  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'localhost' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'127.0.0.1' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'58.196.167.15' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'58.196.167.16' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.241.140.55' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.241.22.172' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.200.231.48' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.200.173.215' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.200.189.250' IDENTIFIED BY 'tm_pw';
GRANT ALL PRIVILEGES ON dawei.* TO tm_rw@'10.200.205.125' IDENTIFIED BY 'tm_pw';
FLUSH PRIVILEGES;



DROP DATABASE IF EXISTS wp;
CREATE DATABASE wp default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON wp.* TO wp_rw@'localhost' IDENTIFIED BY 'wp_pw';
GRANT ALL PRIVILEGES ON wp.* TO wp_rw@'127.0.0.1' IDENTIFIED BY 'wp_pw';


#localhost:3307
#wp_rw
#wp_pw
#/usr/tool/mysql-acookie/bin/mysql -u root -h 127.0.0.1 -P3307 --socket=/usr/tool/mysql-acookie/tmp/mysql.sock7 -p123456  -e 'reset master' &
# /usr/tool/mysql-mainbus/bin/mysql -u root -p123456  -h 127.0.0.1 -P3307  --socket=/usr/tool/mysql-mainbus/tmp/mysql.sock1 -e 'reset master' &
#mysql -u root -h 127.0.0.1 -P3307 -p123456 -e 'reset master' & 
#mysql -u root -h 127.0.0.1 -P3307 -p123456 -e 'reset master' &
 
 
 
DROP DATABASE IF EXISTS invite;
CREATE DATABASE invite default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'localhost' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'127.0.0.1' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'58.196.167.15' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'58.196.167.16' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'10.241.49.38' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'10.128.1.209' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'10.128.1.58' IDENTIFIED BY 'invite_pw';
GRANT ALL PRIVILEGES ON invite.* TO invite_rw@'10.128.1.83' IDENTIFIED BY 'invite_pw';
FLUSH PRIVILEGES;


DROP DATABASE IF EXISTS alweb;
CREATE DATABASE alweb default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'localhost' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'127.0.0.1' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'58.196.167.15' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'58.196.167.16' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'10.241.49.38' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'10.128.1.209' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'10.128.1.58' IDENTIFIED BY 'alweb_pw';
GRANT ALL PRIVILEGES ON alweb.* TO alweb_rw@'10.128.1.83' IDENTIFIED BY 'alweb_pw';
FLUSH PRIVILEGES;


DROP DATABASE IF EXISTS txm;
CREATE DATABASE txm  default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'localhost' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'127.0.0.1' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'58.196.167.15' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'58.196.167.16' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.200.231.48' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.241.140.55' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.241.22.172' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.200.231.48' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.200.173.215' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.200.189.250' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.200.205.125' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.241.49.38' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.128.1.209' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.128.1.58' IDENTIFIED BY 'txm_pw';
GRANT ALL PRIVILEGES ON txm.* TO txm_rw@'10.128.1.83' IDENTIFIED BY 'txm_pw';

FLUSH PRIVILEGES;






delete from 0 where userId in ( select id from user_forward where target = 4); delete from adgroup1 where userId in ( select id from user_forward where item); delete from adgroup2 where userId in ( select id from user_forward where target = 4); delete from adgroup3 where userId in ( select id from user_forward where target = 4); delete from adgroup4 where userId in ( select id from user_forward where target = 4); delete from adgroup5 where userId in ( select id from user_forward where target = 4); delete from adgroup6 where userId in ( select id from user_forward where target = 4); delete from adgroup7 where userId in ( select id from user_forward where target = 4); delete from adgroup8 where userId in ( select id from user_forward where target = 4); delete from adgroup9 where userId in ( select id from user_forward where target = 4); delete from adgroup10 where userId in ( select id from user_forward where target = 4); delete from adgroup11 where userId in ( select id from user_forward where target = 4); delete from adgroup12 where userId in ( select id from user_forward where target = 4); delete from adgroup13 where userId in ( select id from user_forward where target = 4); delete from adgroup14 where userId in ( select id from user_forward where target = 4); delete from adgroup15 where userId in ( select id from user_forward where target = 4)

delete from item0 where userId in ( select id from user_forward where target = 4); delete from item1 where userId in ( select id from user_forward where target = 4); delete from item2 where userId in ( select id from user_forward where target = 4); delete from item3 where userId in ( select id from user_forward where target = 4); delete from item4 where userId in ( select id from user_forward where target = 4); delete from item5 where userId in ( select id from user_forward where target = 4); delete from item6 where userId in ( select id from user_forward where target = 4); delete from item7 where userId in ( select id from user_forward where target = 4); delete from item8 where userId in ( select id from user_forward where target = 4); delete from item9 where userId in ( select id from user_forward where target = 4); delete from item10 where userId in ( select id from user_forward where target = 4); delete from item11 where userId in ( select id from user_forward where target = 4); delete from item12 where userId in ( select id from user_forward where target = 4); delete from item13 where userId in ( select id from user_forward where target = 4); delete from item14 where userId in ( select id from user_forward where target = 4); delete from item15 where userId in ( select id from user_forward where target = 4);

delete from adgroup_max_num where userId in ( select id from user_forward where target = 4);  delete from campaign  where userId in ( select id from user_forward where target = 4);  delete from campaign_create_time   where userId in ( select id from user_forward where target = 4); delete from    campaignmaxprice_  where userId in ( select id from user_forward where target = 4);  delete from  creative  where userId in ( select id from user_forward where target = 4); delete from my_words where userId in ( select id from user_forward where target = 4);   delete from optimizeconfig_ where userId in ( select id from user_forward where target = 4);  delete from qscore_fix_price_config_ where userId in ( select id from user_forward where target = 4);  delete from update_keyword_task_ where userId in ( select id from user_forward where target = 4);   delete from user where id in ( select id from user_forward where target = 4);   delete from user_modify_max_price_   where userId in ( select id from user_forward where target = 4);   delete from user_optimize_setting_ where userId in ( select id from user_forward where target = 4);



adgroup0 adgroup1 adgroup2 adgroup3 adgroup4 adgroup5 adgroup6 adgroup7 adgroup8 adgroup9 adgroup10 adgroup11 adgroup12 adgroup13 adgroup14 adgroup15
item0 item1 item2 item3 item4 item5 item6 item7 item8 item9 item10 item11 item12 item13 item14 item15



alter table keywordplay_0 change  industryCtr  industryCtrInt int(11) default -1;


alter table keywordplay_1 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_2 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_3 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_4 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_5 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_6 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_7 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_8 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_9 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_10 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_11 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_12 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_13 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_14 change  industryCtr  industryCtrInt int(11) default -1;
alter table keywordplay_15 change  industryCtr  industryCtrInt int(11) default -1;

alter table keywordplay_0 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_1 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_2 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_3 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_4 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_5 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_6 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_7 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_8 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_9 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_10 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_11 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_12 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_13 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_14 change  industryClick  industryClick int(11) default -1;
alter table keywordplay_15 change  industryClick  industryClick int(11) default -1;



alter table keywordplay_0 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_1 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_2 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_3 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_4 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_5 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_6 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_7 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_8 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_9 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_10 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_11 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_12 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_13 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_14 change  industryPrice  industryPrice int(11) default -1;
alter table keywordplay_15 change  industryPrice  industryPrice int(11) default -1;





  

insert into activity1 (`id`,`activityDescription`,`activityEndTime`,`activityStartTime`,`activityTitle`,`activityType`,`buyLimit`,`createTime`,`itemCount`,`items`,`mjsActivityId`,`mjsParams`,`status`,`tmActivityStatus`,`updateTs`,`userId`,  `tmplHtml`,  `remark`)
select `id`,`activityDescription`,`activityEndTime`,`activityStartTime`,`activityTitle`,`activityType`,`buyLimit`,`createTime`,`itemCount`,`items`,`mjsActivityId`,`mjsParams`,`status`,`tmActivityStatus`,`updateTs`,`userId`,  `tmplHtml`,  `remark` from activity;

 adgroup_max_num   campaign      campaign_create_time   campaignmaxprice_       my_words    optimizeconfig_  qscore_fix_price_config_  update_keyword_task_    user      user_modify_max_price_    user_optimize_setting_
  
 
 adgroup_max_num   campaign      campaign_create_time   campaignmaxprice_   my_words    optimizeconfig_  qscore_fix_price_config_  update_keyword_task_    user      user_modify_max_price_    user_optimize_setting_
 

DROP DATABASE IF EXISTS mission;
CREATE DATABASE mission default charset utf8 COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON mission.* TO mission_rw@'localhost' IDENTIFIED BY 'mission_pw';
GRANT ALL PRIVILEGES ON mission.* TO mission_rw@'127.0.0.1' IDENTIFIED BY 'mission_pw';

DROP DATABASE IF EXISTS ultrax;
CREATE DATABASE ultrax default charset gbk COLLATE gbk_chinese_ci;
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'localhost' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'127.0.0.1' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.105.76.189' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.131.128.22' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.247.61.248' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.105.1.152' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.131.134.68' IDENTIFIED BY 'ultrax_pw';
GRANT ALL PRIVILEGES ON ultrax.* TO ultrax_rw@'10.105.54.70' IDENTIFIED BY 'ultrax_pw';
FLUSH PRIVILEGES;













 CREATE TABLE `Bak_Task` (
  `id` varchar(255) NOT NULL,
  `completeTaskMachineId` varchar(255) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  `errMsg` varchar(255) DEFAULT NULL,
  `expectStartTime` bigint(20) DEFAULT NULL,
  `isAssigned` bit(1) NOT NULL,
  `isError` bit(1) NOT NULL,
  `isFinished` bit(1) NOT NULL,
  `itemId` bigint(20) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `masterId` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `startTime` bigint(20) DEFAULT NULL,
  `submitMachineId` varchar(255) DEFAULT NULL,
  `submitTime` bigint(20) DEFAULT NULL,
  `taskType` varchar(255) DEFAULT NULL,
  `wangwangId` varchar(255) DEFAULT NULL,
  `workerMachineId` varchar(255) DEFAULT NULL,
  `isArchived` bit(1) NOT NULL,
  `assignedTs` bigint(20) NOT NULL,
  `isDeleted` bit(1) NOT NULL,
  `itemUrl` varchar(1023) DEFAULT NULL,
  `loc` varchar(255) DEFAULT NULL,
  `priceLimitHigh` double NOT NULL,
  `priceLimitLow` double NOT NULL,
  `avgVisitDepth` bigint(20) NOT NULL,
  `firstPageAvgStopTime` bigint(20) NOT NULL,
  `isAllowNightIm` bit(1) NOT NULL,
  `isAllowNightPv` bit(1) NOT NULL,
  `isAllowTaskFailRetry` bit(1) NOT NULL,
  `isTimeout` bit(1) NOT NULL,
  `subPagesAvgStopTime` bigint(20) NOT NULL,
  `maxTimeoutLimit` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


insert into Bak_Task( \
`taskType`,`masterId`,`wangwangId`,`keyword`,`itemId`,`name`,`expectStartTime`,`startTime`,`endTime`,`submitTime`,`submitMachineId`,`workerMachineId`,`completeTaskMachineId`,`isAssigned`,`isFinished`,`isArchived`,`isDeleted`,`loc`,`priceLimitLow`,`priceLimitHigh`,`isError`,`itemUrl`,`errMsg`,`id`,`assignedTs`,`isTimeout`,`isAllowNightPv`,`isAllowNightIm`,`isAllowTaskFailRetry`,`firstPageAvgStopTime`,`subPagesAvgStopTime`,`avgVisitDepth`,`maxTimeoutLimit` \
) select  \
taskType,masterId,wangwangId,keyword,itemId,name,expectStartTime,startTime,endTime,submitTime,submitMachineId,workerMachineId,completeTaskMachineId,isAssigned,isFinished,isArchived,isDeleted,loc,priceLimitLow,priceLimitHigh,itemUrl,isError,errMsg,id,assignedTs,isTimeout,isAllowNightPv,isAllowNightIm,isAllowTaskFailRetry,firstPageAvgStopTime,subPagesAvgStopTime,avgVisitDepth,maxTimeoutLimit  \
from Task where expectStartTime >  1409146431481 limit 1;
  `actionState` longtext,
  `taobaoItemPageUrl` longtext,
  `uzHomepageUrl` longtext,
  `uzItemDetailPageUrl` longtext,
  `uzItemListPageUrl` longtext,
KEY `masterId` (`masterId`),
  KEY `completeTaskMachineId` (`completeTaskMachineId`),
  KEY `submitMachineId` (`submitMachineId`),
  KEY `workerMachineId` (`workerMachineId`),
  KEY `expectStartTime` (`expectStartTime`)
