CREATE TABLE `corporate_events` (
	`id` text PRIMARY KEY NOT NULL,
	`symbol` text NOT NULL,
	`kind` text NOT NULL,
	`date` text NOT NULL,
	`end_date` text,
	`label` text,
	`amount` real,
	`pct` real,
	`period_end` text,
	`detail` text,
	`source_url` text,
	`updated_at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `corporate_events_symbol_date_idx` ON `corporate_events` (`symbol`,`date`);--> statement-breakpoint
CREATE INDEX `corporate_events_date_idx` ON `corporate_events` (`date`);