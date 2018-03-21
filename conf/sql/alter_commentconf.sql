alter table comment_conf add column commentType bigint(20) default 0;
alter table comment_conf add column commentTime bigint(20) default 0;
alter table comment_conf add column commentRate bigint(20) default 0;
alter table comment_conf add column badCommentMsg varchar(255);