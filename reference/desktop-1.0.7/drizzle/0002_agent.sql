CREATE TABLE `wiki_log` (
	`id` text PRIMARY KEY NOT NULL,
	`run_id` text,
	`slug` text NOT NULL,
	`action` text NOT NULL,
	`at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `wiki_log_at_idx` ON `wiki_log` (`at`);--> statement-breakpoint
CREATE TABLE `wiki_pages` (
	`slug` text PRIMARY KEY NOT NULL,
	`title` text NOT NULL,
	`body` text NOT NULL,
	`kind` text NOT NULL,
	`updated_at` text NOT NULL,
	`updated_by_run_id` text
);
--> statement-breakpoint
CREATE INDEX `wiki_pages_kind_idx` ON `wiki_pages` (`kind`);--> statement-breakpoint
CREATE INDEX `wiki_pages_updated_idx` ON `wiki_pages` (`updated_at`);--> statement-breakpoint
CREATE TABLE `agent_runs` (
	`id` text PRIMARY KEY NOT NULL,
	`trigger` text NOT NULL,
	`started_at` text NOT NULL,
	`finished_at` text,
	`status` text NOT NULL,
	`provider_id` text,
	`model_id` text,
	`tokens_in` integer DEFAULT 0 NOT NULL,
	`tokens_out` integer DEFAULT 0 NOT NULL,
	`cost_usd` real DEFAULT 0 NOT NULL,
	`summary` text,
	`error_text` text,
	`steps_json` text,
	`cards_made` integer DEFAULT 0 NOT NULL,
	`notified` integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE INDEX `agent_runs_started_idx` ON `agent_runs` (`started_at`);--> statement-breakpoint
CREATE TABLE `idea_cards` (
	`id` text PRIMARY KEY NOT NULL,
	`run_id` text,
	`title` text NOT NULL,
	`kind` text NOT NULL,
	`symbol_or_name` text NOT NULL,
	`thesis` text NOT NULL,
	`risks` text NOT NULL,
	`confidence` integer NOT NULL,
	`size_pct` real NOT NULL,
	`horizon_days` integer NOT NULL,
	`safe_alternative` text,
	`safe_rate_at_creation` real,
	`status` text DEFAULT 'open' NOT NULL,
	`acted_at` text,
	`created_at` text NOT NULL,
	`scored_at` text,
	`score` integer,
	`return_pct` real,
	`safe_return_pct` real,
	`outcome_note` text,
	`entry_ref` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `idea_cards_status_created_idx` ON `idea_cards` (`status`,`created_at`);--> statement-breakpoint
CREATE INDEX `idea_cards_symbol_idx` ON `idea_cards` (`symbol_or_name`,`status`);--> statement-breakpoint
CREATE TABLE `notifications_log` (
	`id` text PRIMARY KEY NOT NULL,
	`at` text NOT NULL,
	`title` text NOT NULL,
	`body` text NOT NULL,
	`kind` text NOT NULL,
	`card_id` text,
	`delivered` integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE INDEX `notifications_log_at_idx` ON `notifications_log` (`at`);--> statement-breakpoint
CREATE TABLE `reminders` (
	`id` text PRIMARY KEY NOT NULL,
	`due_at` text NOT NULL,
	`title` text NOT NULL,
	`body` text NOT NULL,
	`created_by_run_id` text,
	`fired_at` text
);
--> statement-breakpoint
CREATE INDEX `reminders_due_idx` ON `reminders` (`fired_at`,`due_at`);--> statement-breakpoint
CREATE VIRTUAL TABLE `wiki_fts` USING fts5(title, body, content='wiki_pages', content_rowid='rowid', tokenize='unicode61 remove_diacritics 2');
--> statement-breakpoint
CREATE TRIGGER `wiki_ai` AFTER INSERT ON `wiki_pages` BEGIN
  INSERT INTO wiki_fts(rowid, title, body) VALUES (new.rowid, new.title, new.body);
END;
--> statement-breakpoint
CREATE TRIGGER `wiki_au` AFTER UPDATE ON `wiki_pages` BEGIN
  INSERT INTO wiki_fts(wiki_fts, rowid, title, body) VALUES ('delete', old.rowid, old.title, old.body);
  INSERT INTO wiki_fts(rowid, title, body) VALUES (new.rowid, new.title, new.body);
END;
--> statement-breakpoint
CREATE TRIGGER `wiki_ad` AFTER DELETE ON `wiki_pages` BEGIN
  INSERT INTO wiki_fts(wiki_fts, rowid, title, body) VALUES ('delete', old.rowid, old.title, old.body);
END;
