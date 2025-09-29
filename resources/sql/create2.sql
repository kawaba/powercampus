create table forumkey_GROUPNAME (
     ownerkey         VARCHAR(12)    PRIMARY KEY,
     forumkey         CHAR(4)
);

create table threadkey_GROUPNAME (
     owner_forumkey   VARCHAR(17)    PRIMARY KEY,
     threadkey        CHAR(4)
);

create table postkey_GROUPNAME (
     bbskey           VARCHAR(22)   PRIMARY KEY,
     postkey          CHAR(5)
);

create table bbsforum_GROUPNAME (
     ownerkey   VARCHAR(12),
     forumkey   CHAR(4),  
     alive      CHAR(1),
     gpflag     CHAR(1),
     rtflag     CHAR(1),
     hdflag     CHAR(1),
     date       VARCHAR(30),
     subject    VARCHAR(100),
     content    TEXT,
     attachment TEXT,
     relation   TEXT
);
create index bbsforum_GROUPNAME_idx on bbsforum_GROUPNAME (ownerkey, forumkey);

create table bbsthread_GROUPNAME (
     owner_forumkey   VARCHAR(17),
     threadkey        CHAR(4),
     userid           VARCHAR(12),
     date             VARCHAR(30),
     rating           VARCHAR(6),
     subject          VARCHAR(100),
     content          TEXT,
     attachment       TEXT,
     views            VARCHAR(7)     
);
create index bbsthread_GROUPNAME_idx on bbsthread_GROUPNAME (owner_forumkey, threadkey);

create table bbspost_GROUPNAME (
     bbskey          VARCHAR(22),
     postkey         CHAR(5),
     userid          VARCHAR(12),
     date            VARCHAR(30),
     rating          VARCHAR(6),
     subject         VARCHAR(100),
     content         TEXT,
     attachment      TEXT,
     dispname        VARCHAR(30),
     link            TEXT
);
create index bbspost_GROUPNAME_idx on bbspost_GROUPNAME (bbskey, postkey);

create table bbslog_GROUPNAME (
     userid          VARCHAR(12),
     owner_forumkey  VARCHAR(17),
     posts           CHAR(4),
     lastvisit       VARCHAR(30)
);
create index bbslog_GROUPNAME_idx on bbslog_GROUPNAME (userid, owner_forumkey);

create table bbsInfo_GROUPNAME (
    userid            VARCHAR(12)  PRIMARY KEY,
    division          CHAR(1)    DEFAULT '4',
    name              VARCHAR(30),
    handle            VARCHAR(30),
    iconfile          VARCHAR(30),
    signature         TEXT,
    formatStyle       TEXT,
    editor            CHAR(2)
);

create table GROUPNAME (
    id                VARCHAR(12)  PRIMARY KEY ,
    kname             VARCHAR(20) ,
    email             VARCHAR(60) ,
    keitai            VARCHAR(60) ,
    stPasswd          VARCHAR(20) ,
    mailselections    VARCHAR(50) ,
    classInfo         TEXT,
    active_i          VARCHAR(60) default '-',
    active_k          VARCHAR(60) default '-',
    number_i          VARCHAR(3)  default '-',
    number_k          VARCHAR(3)  default '-'
);

create table note_GROUPNAME (
    stNumber        VARCHAR(12)   NOT NULL,
    te_aplec_key    CHAR(12)      NOT NULL,
    sect_key        CHAR(5)       NOT NULL,
    note            text          DEFAULT ''
);
create index note_GROUPNAME_idx on note_GROUPNAME (stNumber,te_aplec_key);

create table kadai_GROUPNAME (
     stNumber           VARCHAR(12)   NOT NULL,
     te_aplec_key       CHAR(12)      NOT NULL,
     kadai_key          CHAR(6)       NOT NULL,
     shubetsu           CHAR(1)       NOT NULL,
     saiten_flag        CHAR(1)       DEFAULT '0',
     date_str           text          DEFAULT '',
     subject            text          DEFAULT '',
     points             VARCHAR(3)    DEFAULT ''
);
create index kadai_GROUPNAME_idx on kadai_GROUPNAME (stNumber,te_aplec_key);
