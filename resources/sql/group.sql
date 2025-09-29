
/////////////////////////////////////////////////////

  グループごとに作成するキー発生ＤＢ
  
  
  mbcd08910000					ownerkey(12)
  mbcd08910000_0001				owner_forumkey(17)
  mbcd08910000_0001_0002		bbskey(22)	
  mbcd08910000_0001_0002_00013  full key : _ で各部分を連結 28桁

/////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////

   変数スタックデータベース
   
   ある時点のシステムハッシュを全て記録する

    key    dd-[uid]-HHMMSS   2+12+6 +2 = 22
    vars   hashtable をシリアライズしさらにbase64エンコードしたテキスト       
    uid    ユーザーID　
    date   記録日

　レコードはログイン時にクリアする．
　本日以前でそのユーザーのレコードを探すので uid でもインデックスを
　付けておく．
   

//////////////////////////////////////////////////////
create table var_kwc (
  key    VARCHAR(22)     PRIMARY KEY,
  uid    VARCHAR(12),
  date   CHAR(4)
);
create index var_kwc_idx on var_kwc (uid);

/////////////////////////////////////////////////////

　　ファイルキャビネットデータベース

    seqkey       = yyyymmddhhmmss-nnn (18桁)
                   nnn はCBkey テーブルから得る
    
     subject       TEXT　　　　　内容説明
     owner         VARCHAR(10)   登録者
     fname         VARCHAR(256)  ファイル名(英字)
     kubun         VARCHAR(5),   ファイル種別（拡張子） 
     size          VARCHAR(20)   ファイルサイズ
     filepath      VARCHAR(256)  アクセス用PATH
     fileurl       VARCHAR(256)  アクセス用URL
     date_from     CHAR(12)      登録日
     idx           CHAR(3)       インデックスフラグ（000,001,010,011） 


/////////////////////////////////////////////////////
create table cabinet_GROUPNAME (
     seqkey        CHAR(18)       PRIMARY KEY,
     subject       TEXT,
     owner         VARCHAR(10),      
     fname         VARCHAR(256),
     kubun         CHAR(5),  
     size          VARCHAR(20),
     filepath      VARCHAR(256),
     fileurl       VARCHAR(256),
     date_from     CHAR(12),
     idx           CHAR(3)
);

          
/////// id 別のインデックス

    accessid     =  id or "everyone"

///////
create table CBidIndex_GROUPNAME ( 
     id  		VARCHAR(12),
     seqkey     VARCHAR(18)
); 
create index CBidIndex_GROUPNAME_idx on CBidIndex_GROUPNAME (id);
     

//////// class 別のインデックス

    accessclass  =  id-lec-key or "everyone" 

///////
create table CBclassIndex_GROUPNAME ( 
     accessclass  VARCHAR(12),
     seqkey       VARCHAR(18)
); 
create index CBclassIndex_GROUPNAME_idx on CBclassIndex_GROUPNAME (accessclass);

////// 各グループごとのキー発生器
create table cbkey (
    groupkey        VARCHAR(20)   PRIMARY KEY,
    seqkey          CHAR(3)
);


/////////////////////////////////////////////////////

 フォーラムの内容

 alive     活動中か否か 　　0=hidden  1=close  2=active
 gpflag    グループ学習か　 0=no      1=yes     規定値: 0
 rtflag    支持度必須か　   0=no      1=yes     規定値: 0
 hdflag    ハンドル必須か   0=no      1=yes     規定値: 1

/////////////////////////////////////////////////////
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



/////////////////////////////////////////////////////

  スレッドの内容
  
  userid       作成者のID
  date         開設日時
  rating       支持度（評価）
  name         スレッド名
  exp　        内容
  attachment   添付ファイルリスト（CSV形式）

/////////////////////////////////////////////////////
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

///////////////////////////////////////////////////////////////////////////////////////

　　教師・学生のPOSTテーブル（ＢＢＳ）

    ポストしたデータを記録する
    dispName はハンドル名またはユーザー名．メモである．
    link はスレッド順を表す
    例：00003-00021-00033　　00003にリンクしている00021の記事にリンクするレコード．
    　　　　　　　　　　　　　最後の 00033 は自分の postkey 

///////////////////////////////////////////////////////////////////////////////////////

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



///////////////////////////

　　閲覧記録


///////////////////////////
create table bbslog_GROUPNAME (
     userid          VARCHAR(12),
     owner_forumkey  VARCHAR(17),
     posts           CHAR(4),
     lastvisit       VARCHAR(30)
);
create index bbslog_GROUPNAME_idx on bbslog_GROUPNAME (userid, owner_forumkey);




///////////////////////////////////////////////////

    ユーザー追加属性とエディタの規定値
　　BBS以外でも利用
    これを編集する画面が必要になる　　　
    division   1=教師　2=TA  3=学生 4=guest

///////////////////////////////////////////////////
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

///////////////////////////////////////////////////////////////////////////////////////

　　学生名簿テーブル

///////////////////////////////////////////////////////////////////////////////////////

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


///////////////////////////////////////////////////////////////////////////////////////

   学生のノート

///////////////////////////////////////////////////////////////////////////////////////

create table note_GROUPNAME (
stNumber        VARCHAR(12)   NOT NULL,
te_aplec_key    CHAR(12)      NOT NULL,
sect_key        CHAR(5)       NOT NULL,
note            text          DEFAULT ''
);
create index note_GROUPNAME_idx on note_GROUPNAME (stNumber,te_aplec_key);



///////////////////////////////////////////////////////////////////////////////////////

　学生ごとの課題提出状況

　kadai_000000 テーブルは、各学生の課題提出状況をストアする

  saiten_flag  0 = 未採点
               1 = 採点済み

  shubetsu     1(report)  2(file)  3(exam)  4(other)
  date_str     提出日時をCSV形式で記録．最後尾のものが一番新しい提出日

///////////////////////////////////////////////////////////////////////////////////////
  <<< 2004.3.28 追加 >>>

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
