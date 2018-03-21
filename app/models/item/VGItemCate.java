
package models.item;

import javax.persistence.Column;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.GenericModel;

//@Entity(name = "")
public class VGItemCate extends GenericModel {

    @Id
    int id;

    @Column(columnDefinition = "varchar(50) NOT NULL")
    String name = StringUtils.EMPTY;

    @Column(columnDefinition = "varchar(255) NOT NULL")
    String img = StringUtils.EMPTY;;

    @Column(columnDefinition = " smallint(4) NOT NULL DEFAULT '0'")
    int pid;

    @Column(columnDefinition = " int(10) NOT NULL DEFAULT '0'")
    int item_nums;

    @Column(columnDefinition = " int(11) NOT NULL")
    int item_likes;

    @Column(columnDefinition = " smallint(4) NOT NULL DEFAULT '0'")
    int ordid;

    @Column(columnDefinition = "varchar(50) NOT NULL")
    String tags = StringUtils.EMPTY;

    @Column(columnDefinition = " tinyint(1) NOT NULL DEFAULT '0'")
    int is_hots;

    @Column(columnDefinition = " tinyint(1) NOT NULL DEFAULT '1'")
    int status;

    @Column(columnDefinition = " int(1) NOT NULL DEFAULT '0' COMMENT '0表示不推荐，1表示推荐'")
    int recommend;

    @Column(columnDefinition = " int(1) NOT NULL DEFAULT '1'")
    int import_status;

    /*
    `seo_title` varchar(255) NOT NULL,
    `seo_keys` varchar(255) NOT NULL,
    `color` varchar(255) DEFAULT NULL,
    `seo_desc` text NOT NULL,
    `matching_title` varchar(2000) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `index_is_hots` (`is_hots`),
    KEY `ordid` (`ordid`),
    KEY `index_pid` (`pid`,`recommend`,`status`),
    KEY `status` (`status`)
    ) ENGINE=Innodb AUTO_INCREMENT=1008 DEFAULT CHARSET=utf8
     */
}
