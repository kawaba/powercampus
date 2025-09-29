create table shozoku (
    sz_id         VARCHAR(30) PRIMARY KEY,
    sz_active     CHAR(1)       DEFAULT '0',
	sz_name       VARCHAR(50)   DEFAULT '',
	sz_addr       VARCHAR(100)  DEFAULT '',
	sz_tel        VARCHAR(15)   DEFAULT '',
    sz_note       VARCHAR(100)  DEFAULT '',
	sz_url_s      VARCHAR(100)  DEFAULT ''
);
