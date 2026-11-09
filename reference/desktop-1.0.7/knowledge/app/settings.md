---
title: Igent settings explained
kind: app
tags: [igent, settings, providers, models, search, agent, notifications, general, backups, logs, defaults]
updated: 2026-10-10
sources: []
---

# Igent settings

Open **Settings** at the bottom of the sidebar. Tabs: **Providers**, **Models**, **Search**, **Agent**, **Notifications**, **General**, **Backups**, **Logs**. Changes save at once; there is no Save button except where noted. Defaults below are what a new install uses; you can change all of them.

## Providers
The AI services Igent can use (card "AI Providers").
- **Add provider** opens a form: **Type**, **Name**, **API key** (when needed), **Base URL** (Custom and Ollama only), **Default model**. Press **Add**, then **Save** after edits. **Close** hides the form.
- Types: Anthropic API, Claude Subscription (Claude Code Login), Free (OpenCode Zen), OpenAI, Custom (OpenAI-compatible), Google Gemini, Groq, DeepSeek, xAI Grok, OpenRouter, Ollama (local), Z.AI (GLM).
- **Free (OpenCode Zen)**: free models, no key, rate limited. A good way to start.
- **OpenRouter** has a free way in too. Make a free key at openrouter.ai, add it, and keep the default model `openrouter/free`. It picks a free model for each request, so answers vary and can be slow or rate limited. Free models may log what you send. If the agent's tools fail on a free pick, press Test again or choose another model. Pick any other model from the list to pay for better answers.
- **Claude subscription**: uses your local Claude Code login. No key. The form shows "Claude Code: found at …" or "not found on PATH", and "logged in" or "not logged in (run `claude` once)". Personal use only.
- **Ollama (local)**: default Base URL `http://localhost:11434/v1`.
- **Default model**: Igent loads the provider's model list by itself as soon as the form is complete (the API key is typed, or the Base URL for Custom and Ollama). You do not need to save first. Pick a model from the drop-down; "(provider default)" lets the provider choose. If the list cannot load, a message says why and you can type a model id. **Reload Models** loads the list again.
- **Find Working**: under the form, opens a window that sends one short test message to each model in the list, one after another, and marks each **Working**, **Failed** (with the provider's reason) or **No answer** (a rate limit or network problem, so the model is not judged; **Continue** tries it again). The filter box limits the test to matching names, and **Stop** ends it. **Use** next to a working model fills the Default Model box. Each test uses a few tokens of your own key. Nothing is saved until you press **Add** or **Save**.
- The OpenCode free tier is behind a gate meant for its own app. Igent copies that app's request style, but this can stop working without notice. If Test says "free tier unavailable" or "can only be used from within OpenCode", use another provider.
- API keys are encrypted with Windows DPAPI before saving. Leave the key box empty when editing to keep the saved key.
- Each provider row: **Enabled** toggle, badges (**default**, **working**, **failed**, **not tested**), **Test**, **Make default**, **Edit**, **Remove** (deletes its saved key).
- **Test**: checks the provider works. When it passes, Igent fills any empty model slot from it.
- **Make default**: sets all three model slots (chat, analysis, cheap) from this provider, replacing earlier choices.

## Models
- **Model Slots** card: pick a provider and model for each slot:
  - **Chat**: answers in Chat. You can also switch it at the top of the Chat page.
  - **Analysis**: runs the daily agent and writes idea cards. Use a strong model.
  - **Cheap**: one-line news summaries and reading long web pages. A small model, or the Free provider, is enough.
- If a slot is empty, Igent uses the first working provider (its small model for Cheap).
- **Models** card: choose a provider, **Filter** the list, **Refresh list**. Each model has an **Enabled** toggle (switch off models you never want) and a ★ to mark favourites (listed first).
- When to change: use a stronger Analysis model if cards look weak; a cheaper Cheap model to save tokens.

## Search
Card "Web Search". The agent and chat can search the web.
- With no key Igent uses DuckDuckGo. DuckDuckGo sometimes blocks frequent searches.
- **Exa API key** (exa.ai, tried first) and **Tavily API key** (tavily.com, tried when Exa has no key or fails). Both have a free plan. Paste a key and press **Save**; **Remove** deletes it. Badges show **saved** or **none**.
- **Test search** runs a sample search and reports which engine answered.
- Keys are sealed with Windows and stay on this PC.
- When to change: add an Exa or Tavily key if searches fail or come back empty.

## Agent
Card "Agent":
- **Evening Run**: default **on**, at **18:00**. Weekdays, after the PSX close (Pakistan time). The switch next to the time turns the run off; the time is then greyed out.
- **Morning Run**: default **on**, at **10:00**. Monday to Saturday (Pakistan time). It has its own on/off switch. With both runs off, the agent runs only when you click **Run now** (or Urgent Wake is on).
- **Daily Token Budget**: default **500,000** tokens, minimum 10,000. Agent runs and news one-liners stop for the day once it is used. Chat is not counted. Raise it if runs are skipped; lower it to cut cost.
- **Urgent Wake**: default **off**. Lets a big market move start an extra agent run, at most one every 3 hours.
- **Largest Single Idea (% of Cash)**: default **20**. Cards never suggest more than this share of your cash.
- **Tax Status**: **Filer** (default) or **Non-Filer**. Changes capital gains and dividend tax in the analysis.
- **Shariah-Compliant Only**: default **off**.
- **YouTube Transcripts (Optional)**: default **off**. Uses your own yt-dlp to read captions of the followed YouTube channels, at most 3 videos a day. Needs `yt-dlp.exe` on PATH or in `%APPDATA%\Igent\tools`. Personal use only: YouTube's terms do not allow automated downloads.

Card "Agent Activity": today's token use vs the budget (and cost when known), the next scheduled runs, and a list of past runs with time, trigger, status, model, tokens, cards made and alerts sent. **Steps** shows the tools a run used; **Hide steps** closes it.

## Notifications
Card "Notifications":
- **Quiet Hours**: default **on**. Alerts are held and shown when quiet hours end.
- **Quiet from / to**: default **22:00** to **08:00** (Pakistan time). Can cross midnight.
- **Tell Me About Big Moves Right Away**: default **off**. Toasts for KSE-100, dollar, gold and watchlist moves above their limits, without waiting for the agent.
- **Daily Digest After the Evening Run**: default **off**. A short summary toast even when there is no opportunity.
- Note: opportunity alerts come from the agent itself; it only alerts when it finds something worth your attention.

Card "Reminders": upcoming and past reminders with **due** or **sent** badges and a **Delete** button. The agent sets them.

Card "Recent Notifications": the last 50 alerts with time, kind (info, opportunity, risk, move, reminder, system) and text. "(not shown)" means Windows could not display it.

## General
- **Theme**: **System** (default, follows Windows light or dark mode), **Light**, **Dark**.
- **Keep Running in the Tray When the Window Is Closed**: default **on**. Igent keeps watching markets in the background. When off, closing the window quits Igent and nothing runs.
- **Start with Windows**: default **off**. Starts hidden in the tray. Works in the installed app only.
- **Start Minimized to the Tray**: default **off**. When on, every start (Start menu, Desktop shortcut, after an update) opens to the tray icon only, with no window. Click the tray icon to open Igent. It does not apply until the welcome steps are finished.
- **Show Model Reasoning in Chat**: default **off**. Anthropic models only. Uses more tokens.

## Backups
Card "Database Backups".
- Igent backs up its database every day at 03:30 (or soon after start if the PC was off), and before every upgrade. Each backup is checked for damage. The newest 5 are kept.
- **Back up now** makes a backup and verifies it. **Open folder** opens the backups folder.
- Each backup shows its file name, time, size, reason (daily, manual, pre-migration, pre-reset) and **verified** or **unverified**.
- **Restore** replaces the current database with that backup and restarts Igent. Everything after the backup is lost. It asks first.

Card "Reset Igent".
- **Reset Igent…** makes Igent like a fresh install: it deletes all settings, AI provider and search keys, chats, cash, holdings, watchlist, idea cards, reminders, the agent's wiki, fetched market data, news and logs, then restarts with the welcome screen.
- It asks first. The box "Keep a backup of my data first" is ticked by default: Igent then makes a backup before the reset, so you can undo it with **Restore** on that backup. Untick it to delete the backups too.
- It does not sign you out of Claude Code; that login belongs to Claude Code, not Igent.

## Logs
Card "Logs (today)". Pick a log: **app**, **sensors** (data fetching), **provider errors**, **database errors**. **Refresh** reloads; **Open folder** opens the logs folder. The last 5 days of each log are kept. Useful when something fails.
