//////////////////////////////////////////////////////////////////////////////////////

■資料データ

　講義セクション定義の中で作成するデータ

 teUid       = kawaba01 ------- 8 桁．一意なユーザーキー
 lec_key     = 003    --------- 3 桁．講義の連番
 ref_key     = 000012 --------- 6 桁．資料の連番
 

 te_lec_sect_key = kawaba01-003 ----------- 12 桁．　
 
 full key は　==>  kawaba01-003-000012 ---- 19 桁

  shubetsu:   1 = ウェブ資料
              2 = ビデオ
			  3 = 書籍・雑誌
			  4 = プリント等
			  5 = その他


///////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table reference (
     te_lec_key         CHAR(12)  NOT NULL,
     ref_key            CHAR(6)   NOT NULL,
     seq_number         CHAR(3)   DEFAULT   '',
     shubetsu           CHAR(1)   DEFAULT   '',
     title              VARCHAR(200),
     url                VARCHAR(200)
);

// キーは複数列に設定する
create index reference_idx on reference (te_lec_key,ref_key);

 