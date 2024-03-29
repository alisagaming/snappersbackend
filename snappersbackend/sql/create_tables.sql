create table players (
	player_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	facebook_id BIGINT UNIQUE KEY,
	name VARCHAR(20),
	full_name VARCHAR(50),
	email VARCHAR(255) UNIQUE KEY,
	country VARCHAR(255),
	gender VARCHAR(6),
	registration_date DATETIME,
	last_played_date DATETIME,
	xp_level INT default 1,
	xp_count INT default 0,
	hint_count INT default 3,
	gifts_sent_count INT default 0,
	invites_sent_count INT default 0,
	shares_count INT default 0,
	dollars_spent INT default 0,
	gifts TEXT,
	platform VARCHAR(255),
	device VARCHAR(255),
	version VARCHAR(255),
	promo_code_used VARCHAR(255),
	user_defaults TEXT
);

create table promo_codes (
	promo_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	promo_code VARCHAR(255) UNIQUE KEY,
	valid_till DATETIME,
	promo_hints INT default 3
);
