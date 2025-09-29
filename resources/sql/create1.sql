create table shozoku (
    sz_id         VARCHAR(30) PRIMARY KEY,
    sz_active     CHAR(1)       DEFAULT '0',
	sz_name       VARCHAR(50)   DEFAULT '',
    sz_note       VARCHAR(100)  DEFAULT '',
	sz_url_s      VARCHAR(100)  DEFAULT '',
	sz_domain     VARCHAR(50)   DEFAULT ''
);

create table lecture (
       teuid            CHAR(8) NOT NULL,
       lec_key          CHAR(3) NOT NULL,
       title            text,
       content          text,
       seiseki_hyoka    text,
       keywords         text,
       textbook         text,
       ref_texts        text,
       ref_urls         text,
       note             text,
       bikou            text
);
create index lectur_idx on lecture (teUid,lec_key);

create table app_lecture (
       teUid            CHAR(8) NOT NULL,
       aplec_key        CHAR(3) NOT NULL,
       lec_key          CHAR(3) NOT NULL,
       meibo            VARCHAR(100),
	   shubetsu         CHAR(1),
       wdate            CHAR(1),
       worder           CHAR(1),
       yyyy             VARCHAR(20),
       term             VARCHAR(20),
       unit             VARCHAR(4),
	   title            text
);
create index app_lecture_idx on app_lecture (teUid,aplec_key);

create table sect (
       te_lec_key       CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       seq_number       VARCHAR(2),
       title            text,
       content          text,
       note             text,
       ref_list         VARCHAR(200),
       kadai_list       VARCHAR(200)
);
create index sect_idx on sect (te_lec_key,sect_key);

create table app_sect (
       te_aplec_key     CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       s_mm             CHAR(2),
       s_dd             CHAR(2),
       e_mm             CHAR(2),
       e_dd             CHAR(2),
       memo             text
);
create index app_sect_idx on app_sect (te_aplec_key,sect_key);

create table reference (
     te_lec_key         CHAR(12)  NOT NULL,
     ref_key            CHAR(6)   NOT NULL,
     seq_number         CHAR(3)   DEFAULT   '',
     shubetsu           CHAR(1)   DEFAULT   '',
     title              VARCHAR(200),
     url                VARCHAR(200)
);
create index reference_idx on reference (te_lec_key,ref_key);

create table kadai (
     te_lec_key         CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,
     seq_number         CHAR(3),
     shubetsu           CHAR(1),
     title              VARCHAR(200),
     content            text
);
create index kadai_idx on kadai (te_lec_key,kadai_key);

create table app_kadai (
     te_aplec_key       CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,
     saiten_flag        CHAR(1),
     s_yyyy             CHAR(4),
     s_month            CHAR(2),
     s_day              CHAR(2),
     s_hour             CHAR(2),
     s_minute           CHAR(2),
     e_yyyy             CHAR(4),
     e_month            CHAR(2),
     e_day              CHAR(2),
     e_hour             CHAR(2),
     e_minute           CHAR(2),
     passwd             VARCHAR(20)
);
create index app_kadai_idx on app_kadai (te_aplec_key,kadai_key);

create table FAQ (
       te_lec_key       CHAR(12)       NOT NULL,
	   seq_no           VARCHAR(6)     NOT NULL,
       faq_title        VARCHAR(200)   DEFAULT '-',
       faq_body         text           DEFAULT '-'
);
create index FAQ_idx on  FAQ(te_lec_key,seq_no);
create table FAQmail(
       te_aplec_key     CHAR(12)        NOT NULL,
	   seq_no           CHAR(6)         NOT NULL,
       rvdate           VARCHAR(30)     NOT NULL,
       lec_key          CHAR(3)         NOT NULL,
       stNumber         VARCHAR(12)     NOT NULL,
       ml_title         VARCHAR(200)    DEFAULT  '-',
       ml_body          text            DEFAULT  '-',
       read_flag        VARCHAR(6)      DEFAULT 'OFF',
	   faq_title        VARCHAR(200)    DEFAULT '-',
       faq_flag         VARCHAR(6)      DEFAULT 'OFF'
);
create index FAQmail_idx on FAQmail(te_aplec_key,seq_no);

create table key_gen (
    teUid           CHAR(8)  PRIMARY KEY,
    lec             CHAR(3)  DEFAULT '001',
	aplec           CHAR(3)  DEFAULT '001',
    sect            CHAR(5)  DEFAULT '00001',
    kadai           CHAR(6)  DEFAULT '000001',
    ref             CHAR(6)  DEFAULT '000001',
	faq             CHAR(6)  DEFAULT '000001'
);

create table regist (
    user_id         CHAR(8) ,
    user_active     CHAR(1) ,
    szDB            VARCHAR(30) DEFAULT '',
	teMail          VARCHAR(50) PRIMARY KEY,
    passwd          VARCHAR(20) ,
    teName          VARCHAR(20) ,
    hurigana        VARCHAR(30) ,
    shozoku         VARCHAR(50) ,
    url_t           VARCHAR(150) ,
    url_s           VARCHAR(150) ,
	note            VARCHAR(100),
	domain          VARCHAR(50)
);
create table members (
    user_id         CHAR(8),
    user_active     CHAR(1),
    user_db         VARCHAR(30),
    user_mail       VARCHAR(50) PRIMARY KEY,
    user_passwd     VARCHAR(20),
    user_name       VARCHAR(20),
    user_hurigana   VARCHAR(30),
    user_url_t      VARCHAR(150),
    user_division   CHAR(1),
    note            VARCHAR(100),
	domain          VARCHAR(50)
);
create table membersInfo (
    user_id         CHAR(8)   PRIMARY KEY,
    user_active     CHAR(1),
    user_db         VARCHAR(30),
    user_mail       VARCHAR(50),
    user_passwd     VARCHAR(20),
    user_name       VARCHAR(20),
    user_hurigana   VARCHAR(30),
    user_url_t      VARCHAR(150),
    user_division   CHAR(1),
    note            VARCHAR(100),
	domain          VARCHAR(50)
);

create table history (
    user_mail   VARCHAR(50)      primary key,
    path1       VARCHAR(100),
    path2       VARCHAR(100),
    path3       VARCHAR(100)
);

create table cbkey (
    groupkey       VARCHAR(20)  PRIMARY KEY,
    seqkey         CHAR(3)
);