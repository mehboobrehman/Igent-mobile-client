CREATE TABLE `sensor_runs` (
	`id` text PRIMARY KEY NOT NULL,
	`source_id` text NOT NULL,
	`started_at` text NOT NULL,
	`finished_at` text,
	`ok` integer DEFAULT 0 NOT NULL,
	`items_new` integer DEFAULT 0 NOT NULL,
	`error` text,
	FOREIGN KEY (`source_id`) REFERENCES `sources`(`id`) ON UPDATE no action ON DELETE cascade
);
--> statement-breakpoint
CREATE INDEX `sensor_runs_source_started_idx` ON `sensor_runs` (`source_id`,`started_at`);--> statement-breakpoint
CREATE TABLE `sources` (
	`id` text PRIMARY KEY NOT NULL,
	`kind` text NOT NULL,
	`name` text NOT NULL,
	`url` text NOT NULL,
	`category` text,
	`enabled` integer DEFAULT 1 NOT NULL,
	`interval_minutes` integer DEFAULT 180 NOT NULL,
	`at_times` text,
	`weekdays` text,
	`market_hours_only` integer DEFAULT 0 NOT NULL,
	`built_in` integer DEFAULT 0 NOT NULL,
	`last_run_at` text,
	`last_ok_at` text,
	`last_error` text,
	`etag` text,
	`last_modified` text
);
--> statement-breakpoint
CREATE INDEX `sources_enabled_idx` ON `sources` (`enabled`);--> statement-breakpoint
CREATE TABLE `news_items` (
	`id` text PRIMARY KEY NOT NULL,
	`source_id` text NOT NULL,
	`external_id` text NOT NULL,
	`url` text NOT NULL,
	`title` text NOT NULL,
	`summary` text,
	`content` text,
	`published_at` text NOT NULL,
	`fetched_at` text NOT NULL,
	`lang` text,
	`category` text,
	`one_liner` text,
	`entities_json` text,
	FOREIGN KEY (`source_id`) REFERENCES `sources`(`id`) ON UPDATE no action ON DELETE cascade
);
--> statement-breakpoint
CREATE UNIQUE INDEX `news_source_external_u` ON `news_items` (`source_id`,`external_id`);--> statement-breakpoint
CREATE INDEX `news_published_idx` ON `news_items` (`published_at`);--> statement-breakpoint
CREATE INDEX `news_fetched_idx` ON `news_items` (`fetched_at`);--> statement-breakpoint
CREATE INDEX `news_oneliner_pending_idx` ON `news_items` (`one_liner`,`fetched_at`);--> statement-breakpoint
CREATE TABLE `transcripts` (
	`video_id` text PRIMARY KEY NOT NULL,
	`lang` text NOT NULL,
	`text` text NOT NULL,
	`fetched_at` text NOT NULL,
	FOREIGN KEY (`video_id`) REFERENCES `youtube_videos`(`video_id`) ON UPDATE no action ON DELETE cascade
);
--> statement-breakpoint
CREATE TABLE `youtube_videos` (
	`id` text PRIMARY KEY NOT NULL,
	`channel_id` text NOT NULL,
	`channel_name` text,
	`video_id` text NOT NULL,
	`title` text NOT NULL,
	`published_at` text NOT NULL,
	`description` text,
	`has_transcript` integer DEFAULT 0 NOT NULL,
	`transcript_tried_at` text
);
--> statement-breakpoint
CREATE UNIQUE INDEX `youtube_video_u` ON `youtube_videos` (`video_id`);--> statement-breakpoint
CREATE INDEX `youtube_published_idx` ON `youtube_videos` (`published_at`);--> statement-breakpoint
CREATE TABLE `announcements` (
	`id` text PRIMARY KEY NOT NULL,
	`symbol` text NOT NULL,
	`date` text NOT NULL,
	`title` text NOT NULL,
	`kind` text,
	`url` text NOT NULL,
	`fetched_at` text NOT NULL
);
--> statement-breakpoint
CREATE UNIQUE INDEX `announcements_url_u` ON `announcements` (`url`);--> statement-breakpoint
CREATE INDEX `announcements_symbol_date_idx` ON `announcements` (`symbol`,`date`);--> statement-breakpoint
CREATE INDEX `announcements_fetched_idx` ON `announcements` (`fetched_at`);--> statement-breakpoint
CREATE TABLE `companies` (
	`symbol` text PRIMARY KEY NOT NULL,
	`name` text NOT NULL,
	`sector` text,
	`shariah_kmi30` integer DEFAULT 0 NOT NULL,
	`kse100` integer DEFAULT 0 NOT NULL,
	`kmi30` integer DEFAULT 0 NOT NULL,
	`week52_high` real,
	`week52_low` real,
	`updated_at` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `fund_navs` (
	`fund_name` text NOT NULL,
	`date` text NOT NULL,
	`amc` text NOT NULL,
	`category` text NOT NULL,
	`sector` text,
	`nav` real NOT NULL,
	`offer` real,
	`repurchase` real,
	`validity_date` text,
	PRIMARY KEY(`fund_name`, `date`)
);
--> statement-breakpoint
CREATE INDEX `fund_navs_category_date_idx` ON `fund_navs` (`category`,`date`);--> statement-breakpoint
CREATE INDEX `fund_navs_date_idx` ON `fund_navs` (`date`);--> statement-breakpoint
CREATE TABLE `index_levels` (
	`index_code` text NOT NULL,
	`date` text NOT NULL,
	`level` real NOT NULL,
	`high` real,
	`low` real,
	`change_abs` real,
	`change_pct` real,
	PRIMARY KEY(`index_code`, `date`)
);
--> statement-breakpoint
CREATE INDEX `index_levels_date_idx` ON `index_levels` (`date`);--> statement-breakpoint
CREATE TABLE `prices_daily` (
	`symbol` text NOT NULL,
	`date` text NOT NULL,
	`open` real,
	`high` real,
	`low` real,
	`close` real NOT NULL,
	`volume` real,
	`ldcp` real,
	`source` text NOT NULL,
	PRIMARY KEY(`symbol`, `date`)
);
--> statement-breakpoint
CREATE INDEX `prices_date_idx` ON `prices_daily` (`date`);--> statement-breakpoint
CREATE TABLE `rates` (
	`kind` text NOT NULL,
	`date` text NOT NULL,
	`value` real NOT NULL,
	`meta` text,
	`updated_at` text NOT NULL,
	PRIMARY KEY(`kind`, `date`)
);
--> statement-breakpoint
CREATE INDEX `rates_date_idx` ON `rates` (`date`);--> statement-breakpoint
CREATE TABLE `threshold_hits` (
	`id` text PRIMARY KEY NOT NULL,
	`at` text NOT NULL,
	`date` text NOT NULL,
	`kind` text NOT NULL,
	`subject` text NOT NULL,
	`pct` real,
	`detail` text NOT NULL,
	`notified` integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE UNIQUE INDEX `threshold_hits_u` ON `threshold_hits` (`kind`,`subject`,`date`);--> statement-breakpoint
CREATE INDEX `threshold_hits_at_idx` ON `threshold_hits` (`at`);--> statement-breakpoint
CREATE TABLE `cash_ledger` (
	`id` text PRIMARY KEY NOT NULL,
	`at` text NOT NULL,
	`amount` real NOT NULL,
	`note` text
);
--> statement-breakpoint
CREATE INDEX `cash_ledger_at_idx` ON `cash_ledger` (`at`);--> statement-breakpoint
CREATE TABLE `holdings` (
	`id` text PRIMARY KEY NOT NULL,
	`kind` text NOT NULL,
	`symbol_or_name` text NOT NULL,
	`qty` real NOT NULL,
	`unit_cost` real NOT NULL,
	`bought_at` text NOT NULL,
	`note` text
);
--> statement-breakpoint
CREATE INDEX `holdings_kind_idx` ON `holdings` (`kind`,`symbol_or_name`);--> statement-breakpoint
CREATE TABLE `watchlist` (
	`id` text PRIMARY KEY NOT NULL,
	`kind` text NOT NULL,
	`symbol_or_name` text NOT NULL,
	`note` text,
	`alert_pct` real DEFAULT 5 NOT NULL,
	`created_at` text NOT NULL
);
--> statement-breakpoint
CREATE UNIQUE INDEX `watchlist_kind_symbol_u` ON `watchlist` (`kind`,`symbol_or_name`);--> statement-breakpoint
CREATE VIRTUAL TABLE `news_fts` USING fts5(title, summary, content, one_liner, content='news_items', content_rowid='rowid', tokenize='unicode61 remove_diacritics 2');
--> statement-breakpoint
CREATE TRIGGER `news_ai` AFTER INSERT ON `news_items` BEGIN
  INSERT INTO news_fts(rowid, title, summary, content, one_liner) VALUES (new.rowid, new.title, new.summary, new.content, new.one_liner);
END;
--> statement-breakpoint
CREATE TRIGGER `news_au` AFTER UPDATE ON `news_items` BEGIN
  INSERT INTO news_fts(news_fts, rowid, title, summary, content, one_liner) VALUES ('delete', old.rowid, old.title, old.summary, old.content, old.one_liner);
  INSERT INTO news_fts(rowid, title, summary, content, one_liner) VALUES (new.rowid, new.title, new.summary, new.content, new.one_liner);
END;
--> statement-breakpoint
CREATE TRIGGER `news_ad` AFTER DELETE ON `news_items` BEGIN
  INSERT INTO news_fts(news_fts, rowid, title, summary, content, one_liner) VALUES ('delete', old.rowid, old.title, old.summary, old.content, old.one_liner);
END;
-- Seed: built-in sources (PLAN.md 10.4)
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-sbp', 'sbp', 'State Bank of Pakistan (policy rate, KIBOR, T-bills, reserves)', 'https://www.sbp.org.pk/ecodata/kibor_index.asp', NULL, 1440, '12:30', '1-5', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-psx-indices', 'psx-indices', 'PSX indices', 'https://dps.psx.com.pk/indices', NULL, 1440, '17:10', '1-5', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-psx-eod', 'psx-eod', 'PSX daily market report (PDF)', 'https://dps.psx.com.pk/download/market_report/dailystockmkt.pdf', NULL, 1440, '17:20', '1-5', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-psx-quotes', 'psx-quotes', 'PSX company pages (watchlist and holdings)', 'https://dps.psx.com.pk/company/', NULL, 1440, '17:30', '1-5', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-mufap', 'mufap', 'MUFAP daily fund NAVs', 'https://www.mufap.com.pk/Industry/IndustryStatDaily?tab=3', NULL, 1440, '19:00', NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gold', 'gold', 'Gold rate (gold.pk, Karachi)', 'https://gold.pk/', NULL, 120, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-dollar', 'dollar', 'US dollar open market (forex.pk)', 'https://www.forex.pk/open_market_rates.asp', NULL, 120, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-savings', 'savings', 'National Savings profit rates', 'https://savings.gov.pk/latest-profit-rates/', NULL, 10080, '10:00', '1', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-cpi', 'cpi', 'PBS monthly inflation (CPI)', 'https://www.pbs.gov.pk/', NULL, 10080, '11:00', '1-5', 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-propakistani', 'rss', 'ProPakistani', 'https://propakistani.pk/feed/', 'business', 120, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-brecorder', 'rss', 'Business Recorder', 'https://www.brecorder.com/feeds/latest-news', 'business', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-dawn-business', 'rss', 'Dawn Business', 'https://www.dawn.com/feeds/business', 'business', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-dawn-home', 'rss', 'Dawn (Pakistan news and politics)', 'https://www.dawn.com/feeds/home', 'politics', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-tribune-business', 'rss', 'Express Tribune Business', 'https://tribune.com.pk/feed/business', 'business', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gn-mettis', 'google-news', 'Mettis Global (via Google News)', 'https://news.google.com/rss/search?q=site%3Amettisglobal.news&hl=en-PK&gl=PK&ceid=PK:en', 'business', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gn-profit', 'google-news', 'Profit (via Google News)', 'https://news.google.com/rss/search?q=site%3Aprofit.pakistantoday.com.pk&hl=en-PK&gl=PK&ceid=PK:en', 'business', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gn-psx', 'google-news', 'PSX KSE-100 news (Google News)', 'https://news.google.com/rss/search?q=PSX+KSE-100&hl=en-PK&gl=PK&ceid=PK:en', 'business', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gn-imf', 'google-news', 'IMF Pakistan (Google News)', 'https://news.google.com/rss/search?q=IMF+Pakistan&hl=en-PK&gl=PK&ceid=PK:en', 'politics', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-gn-reuters', 'google-news', 'Reuters Pakistan (Google News)', 'https://news.google.com/rss/search?q=Pakistan+site%3Areuters.com&hl=en-PK&gl=PK&ceid=PK:en', 'world', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-bbc-world', 'rss', 'BBC World', 'https://feeds.bbci.co.uk/news/world/rss.xml', 'world', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-bbc-business', 'rss', 'BBC Business', 'https://feeds.bbci.co.uk/news/business/rss.xml', 'world', 180, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-oilprice', 'rss', 'OilPrice.com', 'https://oilprice.com/rss/main', 'commodities', 240, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-yt-najam', 'youtube-feed', 'YouTube: Abdul Rehman Najam', 'https://www.youtube.com/feeds/videos.xml?channel_id=UCU_s2lRotb65VMvAtFVnDuw', 'youtube', 360, NULL, NULL, 1);
--> statement-breakpoint
INSERT INTO `sources` (id, kind, name, url, category, interval_minutes, at_times, weekdays, built_in) VALUES ('src-yt-ammar', 'youtube-feed', 'YouTube: Ammar Siddiqui', 'https://www.youtube.com/feeds/videos.xml?channel_id=UCpeMUQz9lAKlO4Bd4T1UM9g', 'youtube', 360, NULL, NULL, 1);
