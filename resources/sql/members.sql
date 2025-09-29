
create table regist (
    user_id         CHAR(8) ,
    user_active     CHAR(1) ,
	teMail          VARCHAR(50) PRIMARY KEY,
    szDB            VARCHAR(30) DEFAULT '',
    passwd          VARCHAR(20) ,
    teName          VARCHAR(20) ,
    hurigana        VARCHAR(30) ,
    shozoku         VARCHAR(50) ,
    bunnya          VARCHAR(100) ,
    url_t           VARCHAR(150) DEFAULT 'http://mail-and-work.net/',
    url_s           VARCHAR(150) DEFAULT 'http://mail-and-work.net/login/',
	address			VARCHAR(150) ,
    homedir         VARCHAR(100) ,
	note            VARCHAR(100) DEFAULT '5$0$1'
);

create table members (
    user_mail       VARCHAR(50) PRIMARY KEY,
    user_id         CHAR(8)  ,
    user_passwd     VARCHAR(20) ,
    user_active     CHAR(1) ,
    user_name       VARCHAR(20) ,
    user_hurigana   VARCHAR(30) ,
    user_birthday   DATE ,
    user_yubin1     CHAR(3) ,
    user_yubin2     CHAR(4) ,
    user_addr1      CHAR(2) ,
    user_addr2      VARCHAR(100) ,
    user_date_reg   DATE ,
    user_occupation CHAR(1) ,
    note            VARCHAR(100)  DEFAULT '#',
    user_db         VARCHAR(30) DEFAULT '',
    user_url_t      VARCHAR(150)  DEFAULT ''
);

create table membersInfo (
    user_id         CHAR(8)   PRIMARY KEY,
    user_mail       VARCHAR(50),
    user_passwd     VARCHAR(20) ,
    user_active     CHAR(1) ,
    user_name       VARCHAR(20) ,
    user_hurigana   VARCHAR(30) ,
    note            VARCHAR(100)  DEFAULT '#',
    user_db         VARCHAR(30)   DEFAULT '',
    user_url_t      VARCHAR(150)  DEFAULT ''
);
