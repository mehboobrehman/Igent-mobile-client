CREATE TABLE `learn_progress` (
	`lesson_id` text PRIMARY KEY NOT NULL,
	`status` text NOT NULL,
	`check_score` integer,
	`check_total` integer,
	`opened_at` text NOT NULL,
	`completed_at` text
);
--> statement-breakpoint
CREATE TABLE `learn_quiz_results` (
	`id` text PRIMARY KEY NOT NULL,
	`level` text NOT NULL,
	`total` integer NOT NULL,
	`correct` integer NOT NULL,
	`source` text NOT NULL,
	`detail` text NOT NULL,
	`at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `learn_quiz_results_at_idx` ON `learn_quiz_results` (`at`);--> statement-breakpoint
CREATE TABLE `practice_trades` (
	`id` text PRIMARY KEY NOT NULL,
	`kind` text NOT NULL,
	`symbol` text NOT NULL,
	`name` text,
	`side` text NOT NULL,
	`qty` real NOT NULL,
	`price` real NOT NULL,
	`price_date` text NOT NULL,
	`fees` real NOT NULL,
	`note` text,
	`at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `practice_trades_symbol_idx` ON `practice_trades` (`kind`,`symbol`);--> statement-breakpoint
CREATE INDEX `practice_trades_at_idx` ON `practice_trades` (`at`);