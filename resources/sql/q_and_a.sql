/*
　　■Ｑ＆Ａメール用データベース
		
		実施講義単位にまとめ、受け取り時間順に並べる

		seq_no ---- 受付番号 ６桁 KeyGen で発生
		　　　　　　個人単位（講義単位ではない）
		
		内容変更に関してはＦＡＱに対する追加・削除・訂正も連動して行う
		
*/
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

/*
　　■ＦＡＱデータベース
		
		定義講義単位にまとめ、受けつけ番号順に並べる
		
		seq_no ---- 受付番号 ６桁 KeyGen で発生
		　　　　　　個人単位（講義単位ではない）
		
		内容はfaq_mailの変更に伴い連動して追加・削除・訂正される
*/
create table FAQ (
       te_lec_key       CHAR(12)       NOT NULL,
	   seq_no           VARCHAR(6)     NOT NULL,
       faq_title        VARCHAR(200)   DEFAULT '-',
       faq_body         text           DEFAULT '-'
);
create index FAQ_idx on  FAQ(te_lec_key,seq_no);
