CREATE TABLE `company_financials` (
	`symbol` text NOT NULL,
	`period_type` text NOT NULL,
	`period` text NOT NULL,
	`sales` real,
	`profit_after_tax` real,
	`eps` real,
	`gross_margin_pct` real,
	`net_margin_pct` real,
	`eps_growth_pct` real,
	`peg` real,
	`updated_at` text NOT NULL,
	PRIMARY KEY(`symbol`, `period_type`, `period`)
);
--> statement-breakpoint
ALTER TABLE `companies` ADD `market_cap` real;--> statement-breakpoint
ALTER TABLE `companies` ADD `shares` real;--> statement-breakpoint
ALTER TABLE `companies` ADD `free_float_shares` real;--> statement-breakpoint
ALTER TABLE `companies` ADD `free_float_pct` real;--> statement-breakpoint
ALTER TABLE `companies` ADD `description` text;--> statement-breakpoint
ALTER TABLE `companies` ADD `profile_updated_at` text;