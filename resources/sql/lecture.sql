///////////////////////////////////////////////////////////////////////////////////////
■講義定義データ

///////////////////////////////////////////////////////////////////////////////////////

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

///////////////////////////////////////////////////////////////////////////////////////

■講義実施データ

  lec_key は 講義定義データのサブキー部分 teUid-lec_key で講義定義キーとなる
  
  aplec_key = shubetsu + worder + wdate である。KeyGen クラスで生成する。
  
  meibo はクラスの名簿ファイル名
  
  shubetsu  1 = 一般講義
            2 = 夜間講義
			3 = e-Learning

      3 の場合、worder のみ意味を持つ

　yyyy は年度だが、 2003 とか 平成１５ か　決められないので VARCHAR にしてある
　term も前期、前後期、通年などいろいろな書き方があるので同上

　unit は単位数。1.5 とかもありえる 

  title は lecture のtitleと同じ（表示用の冗長情報）

///////////////////////////////////////////////////////////////////////////////////////
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

///////////////////////////////////////////////////////////////////////////////////////
■講義セクションデータ（講義定義の一部）

　te_lec_key は講義データのキー　teUid + '-' + lec_key を意味する
　
　例示：　kawaba01-001
  
　フルキーの例示： kawaba01-003-00012

///////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>
  
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

　　キーリストの例
　　
　　000001$000001%000013 など６桁の資料キー、または課題キー。
　　デリミッタは ',' 
　　kawaba01-002 のような te_lec キーは、所属セクションのキーと同じだから省略してある


////////////////////////////////////////////////////////////////////////////////////////
■講義セクション実施データ

　te_aplec_key は講義実施データのキー　teUid + '-' + aplec_key を意味する
　
　例示：　kawaba01-003
  
　フルキーの例示： kawaba01-003-00012

  実施にかかる日付と講義メモのデータ

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

///////////////////////////////////////////////////////////////////////////////////////
