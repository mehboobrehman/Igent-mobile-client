CREATE TABLE `user_knowledge` (
	`id` text PRIMARY KEY NOT NULL,
	`title` text NOT NULL,
	`category` text NOT NULL,
	`summary` text NOT NULL,
	`content` text NOT NULL,
	`source_kind` text NOT NULL,
	`origin` text NOT NULL,
	`chars` integer NOT NULL,
	`created_at` text NOT NULL,
	`updated_at` text NOT NULL
);
--> statement-breakpoint
CREATE INDEX `user_knowledge_origin_idx` ON `user_knowledge` (`origin`);--> statement-breakpoint
CREATE INDEX `user_knowledge_category_idx` ON `user_knowledge` (`category`);--> statement-breakpoint
CREATE VIRTUAL TABLE `user_knowledge_fts` USING fts5(title, summary, content, content='user_knowledge', content_rowid='rowid', tokenize='unicode61 remove_diacritics 2');
--> statement-breakpoint
CREATE TRIGGER `user_knowledge_ai` AFTER INSERT ON `user_knowledge` BEGIN
  INSERT INTO user_knowledge_fts(rowid, title, summary, content) VALUES (new.rowid, new.title, new.summary, new.content);
END;
--> statement-breakpoint
CREATE TRIGGER `user_knowledge_au` AFTER UPDATE ON `user_knowledge` BEGIN
  INSERT INTO user_knowledge_fts(user_knowledge_fts, rowid, title, summary, content) VALUES ('delete', old.rowid, old.title, old.summary, old.content);
  INSERT INTO user_knowledge_fts(rowid, title, summary, content) VALUES (new.rowid, new.title, new.summary, new.content);
END;
--> statement-breakpoint
CREATE TRIGGER `user_knowledge_ad` AFTER DELETE ON `user_knowledge` BEGIN
  INSERT INTO user_knowledge_fts(user_knowledge_fts, rowid, title, summary, content) VALUES ('delete', old.rowid, old.title, old.summary, old.content);
END;
