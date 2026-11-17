-- Gig Radar 演出雷達 資料庫結構（由 scripts/export_schema.py 自動產生，請勿手改）
CREATE DATABASE IF NOT EXISTS gig_radar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gig_radar;

CREATE TABLE crawl_sources (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	name VARCHAR(100) NOT NULL, 
	module VARCHAR(100) NOT NULL, 
	base_url VARCHAR(500), 
	expected_min_count INTEGER NOT NULL, 
	is_active BOOL NOT NULL, 
	PRIMARY KEY (id), 
	UNIQUE (name)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	email VARCHAR(255) NOT NULL, 
	password_hash VARCHAR(255) NOT NULL, 
	display_name VARCHAR(100) NOT NULL, 
	`role` ENUM('audience','performer','venue','admin') NOT NULL, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	UNIQUE (email)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commission_rules (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	rule_type ENUM('match','merch') NOT NULL, 
	rate NUMERIC(5, 4) NOT NULL, 
	effective_from DATE NOT NULL, 
	created_by INTEGER, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	FOREIGN KEY(created_by) REFERENCES users (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crawl_runs (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	source_id INTEGER NOT NULL, 
	started_at DATETIME NOT NULL DEFAULT (now()), 
	finished_at DATETIME, 
	fetched_count INTEGER NOT NULL, 
	inserted_count INTEGER NOT NULL, 
	merged_count INTEGER NOT NULL, 
	status ENUM('success','anomaly','failed'), 
	message TEXT, 
	PRIMARY KEY (id), 
	FOREIGN KEY(source_id) REFERENCES crawl_sources (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE performers (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	user_id INTEGER, 
	name VARCHAR(150) NOT NULL, 
	genre VARCHAR(50), 
	bio TEXT, 
	social_url VARCHAR(500), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	UNIQUE (user_id), 
	FOREIGN KEY(user_id) REFERENCES users (id), 
	UNIQUE (name)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_performers_genre ON performers (genre);

CREATE TABLE venues (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	name VARCHAR(150) NOT NULL, 
	district VARCHAR(20) NOT NULL, 
	address VARCHAR(255), 
	capacity INTEGER, 
	official_url VARCHAR(500), 
	owner_user_id INTEGER, 
	claim_status ENUM('unclaimed','pending','approved','rejected') NOT NULL, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	UNIQUE (name), 
	FOREIGN KEY(owner_user_id) REFERENCES users (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_venues_district ON venues (district);

CREATE TABLE events (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	title VARCHAR(255) NOT NULL, 
	venue_id INTEGER NOT NULL, 
	event_date DATE NOT NULL, 
	start_time TIME, 
	genre VARCHAR(50), 
	description TEXT, 
	ticket_url VARCHAR(500), 
	source_type ENUM('crawler','performer','match') NOT NULL, 
	crawl_source_id INTEGER, 
	source_url VARCHAR(500), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	updated_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	CONSTRAINT uq_event_dedup UNIQUE (venue_id, event_date, start_time), 
	FOREIGN KEY(venue_id) REFERENCES venues (id), 
	FOREIGN KEY(crawl_source_id) REFERENCES crawl_sources (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_events_event_date ON events (event_date);

CREATE INDEX ix_events_genre ON events (genre);

CREATE INDEX ix_events_venue_id ON events (venue_id);

CREATE TABLE follows (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	user_id INTEGER NOT NULL, 
	target_type ENUM('performer','venue','genre') NOT NULL, 
	performer_id INTEGER, 
	venue_id INTEGER, 
	genre VARCHAR(50), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	CONSTRAINT ck_follow_target CHECK ((target_type = 'performer' AND performer_id IS NOT NULL) OR (target_type = 'venue' AND venue_id IS NOT NULL) OR (target_type = 'genre' AND genre IS NOT NULL)), 
	FOREIGN KEY(user_id) REFERENCES users (id) ON DELETE CASCADE, 
	FOREIGN KEY(performer_id) REFERENCES performers (id) ON DELETE CASCADE, 
	FOREIGN KEY(venue_id) REFERENCES venues (id) ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_follows_user_id ON follows (user_id);

CREATE TABLE performer_aliases (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	performer_id INTEGER NOT NULL, 
	alias VARCHAR(150) NOT NULL, 
	PRIMARY KEY (id), 
	FOREIGN KEY(performer_id) REFERENCES performers (id) ON DELETE CASCADE, 
	UNIQUE (alias)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	performer_id INTEGER NOT NULL, 
	name VARCHAR(150) NOT NULL, 
	description TEXT, 
	price NUMERIC(10, 0) NOT NULL, 
	stock INTEGER NOT NULL, 
	image_url VARCHAR(500), 
	is_active BOOL NOT NULL, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	FOREIGN KEY(performer_id) REFERENCES performers (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_products_performer_id ON products (performer_id);

CREATE TABLE venue_aliases (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	venue_id INTEGER NOT NULL, 
	alias VARCHAR(150) NOT NULL, 
	PRIMARY KEY (id), 
	FOREIGN KEY(venue_id) REFERENCES venues (id) ON DELETE CASCADE, 
	UNIQUE (alias)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_performers (
	event_id INTEGER NOT NULL, 
	performer_id INTEGER NOT NULL, 
	PRIMARY KEY (event_id, performer_id), 
	FOREIGN KEY(event_id) REFERENCES events (id) ON DELETE CASCADE, 
	FOREIGN KEY(performer_id) REFERENCES performers (id) ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	user_id INTEGER NOT NULL, 
	event_id INTEGER NOT NULL, 
	message VARCHAR(255) NOT NULL, 
	is_read BOOL NOT NULL, 
	emailed_at DATETIME, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	CONSTRAINT uq_notify_once UNIQUE (user_id, event_id), 
	FOREIGN KEY(user_id) REFERENCES users (id) ON DELETE CASCADE, 
	FOREIGN KEY(event_id) REFERENCES events (id) ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_notifications_user_id ON notifications (user_id);

CREATE TABLE orders (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	user_id INTEGER NOT NULL, 
	performer_id INTEGER NOT NULL, 
	event_id INTEGER, 
	fulfillment ENUM('pickup','shipping') NOT NULL, 
	status ENUM('pending_payment','paid','picked_up','cancelled') NOT NULL, 
	total_amount NUMERIC(10, 0) NOT NULL, 
	pickup_code VARCHAR(20), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	picked_up_at DATETIME, 
	PRIMARY KEY (id), 
	FOREIGN KEY(user_id) REFERENCES users (id), 
	FOREIGN KEY(performer_id) REFERENCES performers (id), 
	FOREIGN KEY(event_id) REFERENCES events (id), 
	UNIQUE (pickup_code)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_orders_performer_id ON orders (performer_id);

CREATE INDEX ix_orders_user_id ON orders (user_id);

CREATE TABLE promotions (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	performer_id INTEGER NOT NULL, 
	event_id INTEGER, 
	fee NUMERIC(10, 0) NOT NULL, 
	start_date DATE NOT NULL, 
	end_date DATE NOT NULL, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	FOREIGN KEY(performer_id) REFERENCES performers (id), 
	FOREIGN KEY(event_id) REFERENCES events (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_promotions_performer_id ON promotions (performer_id);

CREATE TABLE venue_slots (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	venue_id INTEGER NOT NULL, 
	slot_date DATE NOT NULL, 
	start_time TIME NOT NULL, 
	end_time TIME NOT NULL, 
	rent NUMERIC(10, 0) NOT NULL, 
	last_minute_rent NUMERIC(10, 0), 
	apply_deadline DATETIME, 
	allow_co_performance BOOL NOT NULL, 
	max_performers INTEGER NOT NULL, 
	status ENUM('open','booked','closed') NOT NULL, 
	event_id INTEGER, 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	PRIMARY KEY (id), 
	CONSTRAINT uq_slot UNIQUE (venue_id, slot_date, start_time), 
	FOREIGN KEY(venue_id) REFERENCES venues (id), 
	FOREIGN KEY(event_id) REFERENCES events (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_venue_slots_slot_date ON venue_slots (slot_date);

CREATE INDEX ix_venue_slots_venue_id ON venue_slots (venue_id);

CREATE TABLE match_requests (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	slot_id INTEGER NOT NULL, 
	performer_id INTEGER NOT NULL, 
	message TEXT, 
	status ENUM('pending','accepted','rejected','cancelled') NOT NULL, 
	final_rent NUMERIC(10, 0), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	decided_at DATETIME, 
	PRIMARY KEY (id), 
	CONSTRAINT uq_match_once UNIQUE (slot_id, performer_id), 
	FOREIGN KEY(slot_id) REFERENCES venue_slots (id), 
	FOREIGN KEY(performer_id) REFERENCES performers (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_match_requests_performer_id ON match_requests (performer_id);

CREATE INDEX ix_match_requests_slot_id ON match_requests (slot_id);

CREATE TABLE order_items (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	order_id INTEGER NOT NULL, 
	product_id INTEGER NOT NULL, 
	quantity INTEGER NOT NULL, 
	unit_price NUMERIC(10, 0) NOT NULL, 
	PRIMARY KEY (id), 
	FOREIGN KEY(order_id) REFERENCES orders (id) ON DELETE CASCADE, 
	FOREIGN KEY(product_id) REFERENCES products (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_order_items_order_id ON order_items (order_id);

CREATE TABLE transactions (
	id INTEGER NOT NULL AUTO_INCREMENT, 
	txn_type ENUM('match_commission','merch_commission','promotion') NOT NULL, 
	match_request_id INTEGER, 
	order_id INTEGER, 
	promotion_id INTEGER, 
	gross_amount NUMERIC(10, 0) NOT NULL, 
	commission_rate NUMERIC(5, 4), 
	commission_amount NUMERIC(10, 0) NOT NULL, 
	payment_status ENUM('pending','paid','failed') NOT NULL, 
	payment_ref VARCHAR(100), 
	created_at DATETIME NOT NULL DEFAULT (now()), 
	paid_at DATETIME, 
	PRIMARY KEY (id), 
	FOREIGN KEY(match_request_id) REFERENCES match_requests (id), 
	FOREIGN KEY(order_id) REFERENCES orders (id), 
	FOREIGN KEY(promotion_id) REFERENCES promotions (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_transactions_txn_type ON transactions (txn_type);
