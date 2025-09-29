///////////////////////////////////////////////////////////////////////////////////////

■課題データ

  講義セクション定義の中で作成するデータ

 teUid       = kawaba01 ------- 8 桁．一意なユーザーキー
 lec_key     = 003    --------- 3 桁．講義の連番
 kadai_key   = 000012 --------- 6 桁．課題の連番
 
 te_lect_key = kawaba01-003 --- 12 桁．　
  
  full key は　==>  kawaba01-003-000012   --- 19 桁

//////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table kadai (
     te_lec_key         CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,
     seq_number         CHAR(3),
     shubetsu           CHAR(1),
     title              VARCHAR(200),
     content            text
);

create index kadai_idx on kadai (te_lec_key,kadai_key);


///////////////////////////////////////////////////////////////////////////////////////

■app_kadai テーブルは、時間割に割り付けた講義の課題についての補足データ

  te_aplec_key で一覧リストを得ることができるが、
  直接キーでの  検索を行う処理が中心になる
  
  saiten_flag  0 = 未採点
               1 = 採点済み


///////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4, 2004.7 変更 >>>

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


