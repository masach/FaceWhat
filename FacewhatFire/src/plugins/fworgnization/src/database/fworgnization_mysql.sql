
-- 纯粹的企业通讯录。如果需要开发group，则另行开发。。
-- 或者和of一样，新建一个   fwxxxprop表，进行扩展

-- 默认公司的id=0.这里只会有一个公司。。。我程序里面只设计了一个公司的情况。
-- 这个组织结构不涉及权限问题，所有在这个组织架构中的人都是平等的，这样是不大合理
-- 比如一个基础部门的员工不应该给公司总裁发送消息。。虽然说我们可以通过现实中进行控制。
-- 但我这个暂时不做，同理，如果需要改进，可以在新建表 fwxxxprop在其中新增 权限字段。

CREATE TABLE IF NOT EXISTS fwgroup
(
groupid int auto_increment unique, -- uuid?自增？ 
groupname varchar(64), -- 
groupfatherid int,
groupfathername varchar(64),
creationdate varchar(15), -- 创建日期，毫秒形式
isorgnization varchar(1), -- 是不是组织架构，0不是，1是。
-- description text -- 不要描述字段了。
-- foreign key(groupfatherid) references fwgroup(groupid)
displayname varchar(64)
);
-- insert into fwgroup(groupname, groupfatherid, creationdate, isorgnization)
-- values('')


-- 在组织结构中的人，是否是
CREATE TABLE IF NOT EXISTS fwgroupuser
(
groupid int, 
groupname varchar(64),
username varchar(64), -- 与ofUser表关联用户
usernickname varchar(64), -- 用户在这个group中的名称。组织架构中用户自己不允许改动这个。 
fullpinyin varchar(64), -- 用户姓名全拼音，暂时不考虑一字多音，如 行-xing-hang。按照用户姓名的读法存入。如林兴洋 linxingyang
shortpinyin varchar(64) -- 用户姓名首字母，如 林兴洋 lxy
-- role // 用户在这个群组中的角色。可自定义，在组织架构中就 member。
);

CREATE TABLE IF NOT EXISTS fwGroupMessageHistory
(
msgid int auto_increment primary key,
groupname varchar(64),
username varchar(64),
sentDate char(15),
body text
);
