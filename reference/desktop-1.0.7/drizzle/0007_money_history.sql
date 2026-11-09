CREATE TABLE `goals` (
	`id` text PRIMARY KEY NOT NULL,
	`name` text NOT NULL,
	`target_amount` real NOT NULL,
	`target_date` text NOT NULL,
	`saved` real DEFAULT 0 NOT NULL,
	`monthly` real DEFAULT 0 NOT NULL,
	`return_pct` real NOT NULL,
	`inflation_pct` real NOT NULL,
	`created_at` text NOT NULL,
	`updated_at` text NOT NULL
);
--> statement-breakpoint
CREATE TABLE `money_transactions` (
	`id` text PRIMARY KEY NOT NULL,
	`kind` text NOT NULL,
	`symbol_or_name` text NOT NULL,
	`type` text NOT NULL,
	`qty` real NOT NULL,
	`price` real NOT NULL,
	`amount` real NOT NULL,
	`fees` real DEFAULT 0 NOT NULL,
	`tax` real DEFAULT 0 NOT NULL,
	`lot_id` text,
	`at` text NOT NULL,
	`note` text,
	`created_at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `money_tx_subject_idx` ON `money_transactions` (`kind`,`symbol_or_name`);--> statement-breakpoint
CREATE INDEX `money_tx_at_idx` ON `money_transactions` (`at`);--> statement-breakpoint
CREATE TABLE `weekly_summaries` (
	`id` text PRIMARY KEY NOT NULL,
	`week_end` text NOT NULL,
	`data` text NOT NULL,
	`note` text,
	`model` text,
	`created_at` text NOT NULL
);
--> statement-breakpoint
CREATE UNIQUE INDEX `weekly_summaries_week_u` ON `weekly_summaries` (`week_end`);--> statement-breakpoint
ALTER TABLE `watchlist` ADD `target_below` real;--> statement-breakpoint
ALTER TABLE `watchlist` ADD `target_above` real;--> statement-breakpoint
ALTER TABLE `watchlist` ADD `target_hit_at` text;--> statement-breakpoint
ALTER TABLE `watchlist` ADD `target_hit_detail` text;--> statement-breakpoint
-- Existing holdings become opening buys, so the history starts complete.
INSERT INTO `money_transactions` (`id`, `kind`, `symbol_or_name`, `type`, `qty`, `price`, `amount`, `fees`, `tax`, `lot_id`, `at`, `note`, `created_at`)
SELECT `id`, `kind`, `symbol_or_name`, 'buy', `qty`, `unit_cost`, `qty` * `unit_cost`, 0, 0, NULL, substr(`bought_at`, 1, 10), `note`, `bought_at` FROM `holdings`;
