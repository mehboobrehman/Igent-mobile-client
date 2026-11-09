---
title: How Igent's agent and alerts work
kind: app
tags: [igent, agent, schedule, alerts, notifications, quiet-hours, big-moves, price-alert, weekly-summary, reminders, digest, red-dot, token-budget, web-search]
updated: 2026-10-11
sources: []
---

# How the agent and alerts work

## Two kinds of background work
- **Data fetching** runs all the time while Igent is open (also when the window is closed to the tray). Each source has its own schedule, shown on the Sources page. No AI is used for this.
- **The agent** is the AI. It runs only at set times or when you ask, so cost stays low.

## When the agent runs
- **Evening Run**: weekdays (Monday to Friday) at the evening time, default 18:00 Pakistan time, after the PSX close.
- **Morning Run**: Monday to Saturday at the morning time, default 10:00.
- Both are on by default. Each has an on/off switch in Settings → Agent; a run that is off never starts by itself.
- **Manual**: **Run now** on the Dashboard's Agent card, or **Run agent now** in the tray menu. Also **Run the agent now** at the end of the welcome flow.
- **Urgent**: only if **Urgent Wake** is on (Settings → Agent, default off). A big move can then start an extra run, at most one every 3 hours.
- Change the times in **Settings → Agent**. Times are Pakistan time.
- Only one run at a time. Asking again while it runs does nothing.
- Runs happen only while Igent is running. If the PC is off or Igent is closed at run time, that run is missed; use Run now.
- **Stop** on the Dashboard cancels a running run (status "cancelled").

## What a run does
1. Reads what changed since the last run: rates and prices, KSE-100, your watchlist, money market funds, your cash and holdings, open cards, its own scorecard, big moves, company announcements and news.
2. Updates its wiki (the big-picture page macro/situation, plus company, sector and fund pages).
3. Decides whether there is an opportunity or a risk worth your attention. Most runs there is none; silence is normal.
4. If there is, writes or updates an idea card. At most 3 new cards per run. It prefers updating an existing card.
5. Alerts you only when it is strong (rules below).
6. Ends with a short summary, shown on the Dashboard Agent card and in Settings → Agent → Agent Activity.
- Ideas are always compared with the safe alternative (T-bills, money market funds, National Savings) after tax. Your tax status, Shariah setting and "Largest single idea" limit are applied.
- A typical run uses 8 to 20 tool calls; the hard limit is 25 steps.
- The tray menu shows the last run, e.g. "Last agent run 17:42: 1 card(s), 0 alert(s)", or "failed", "cancelled", "skipped (budget)".

## When the agent notifies you
The agent may send an alert only for:
- an **opportunity** card with confidence 55 or more (Igent blocks opportunity alerts below 55),
- a **serious risk** to your holdings,
- a **major event**, such as a policy rate change.
It does not alert just to say nothing happened. Clicking an alert opens the related card or page.

## Other alerts
- **Big Moves** (Settings → Notifications → "Tell Me About Big Moves Right Away", default off). Sent right away when new data shows:
  - KSE-100 up or down 3% or more in a day,
  - the open-market dollar up or down 1.5% or more,
  - gold 24K up or down 2% or more,
  - an SBP policy rate change,
  - a watchlist stock or fund moving more than its own "Alert at ±%" (default 5%).
  Big Moves are always listed on the Dashboard's **Big Moves** card and given to the next agent run, even when this alert is off.
- **Price alerts** (Watchlist → **Alert Below** / **Alert Above**): you set a price for a watched stock or fund. After each price update (stocks after the PSX close, funds in the evening), Igent checks it. When the close or NAV reaches your price you get one toast, "Price alert: SYMBOL", even if Big Moves is off, and the row shows **target hit**. It does not repeat; type a new price to arm it again. Prices are end-of-day, so these alerts never come during trading hours.
- **Weekly Summary**: every Friday at 18:30 a toast "Your weekly summary is ready" (click it to open the page). If Igent was off then, it is made and shown at the next start from Friday evening to Sunday. **Make Now** on the Weekly Summary page builds one at any time.
- **Reminders**: the agent can set a reminder for a date and time (e.g. before a results announcement or a savings maturity). It pops up as "Reminder: …". See and delete them in Settings → Notifications → Reminders.
- **Daily digest** (default off): after a completed evening run that sent no alert, a toast "Igent daily digest" with the run summary.
- **Budget used up**: one toast a day, "Igent paused for today", when the token budget runs out.

## Quiet Hours and pausing
- **Quiet Hours** (default on, 22:00 to 08:00 Pakistan time): alerts are held, not lost. When quiet hours end they are shown. Several held alerts arrive as one toast: "Igent: N updates while you were away".
- To stop alerts at night: keep Quiet Hours on and set the times in Settings → Notifications.
- **Pause alerts 2 h** (tray menu): holds alerts for 2 hours. The menu then shows "Alerts paused until HH:MM". Held alerts appear when the pause ends.
- Quiet Hours and pauses only hold pop-ups. Data fetching and agent runs continue.

## Red dots and badges
- A **red dot** on the taskbar button and on the tray icon means something new you have not seen. The tray tooltip says what, e.g. "2 new on Idea Cards, 1 new alert".
- The sidebar shows a red dot and a count next to:
  - **Idea Cards**: new cards and alerts linked to a card. Reminders do not add a dot: they show a Windows notification (list: Settings → Agent).
  - **Dashboard**: big-move alerts, risk and info alerts not tied to a card, daily digests and system messages.
- The taskbar button flashes once when a new item arrives while the window is not in focus.
- **How they clear**: open that page (Dashboard or Idea Cards) while the Igent window is in focus. The dot goes away when both are seen.

## Daily Token Budget
- Background AI work (agent runs and news one-liners) counts against a daily token budget. Default 500,000 tokens; change it in Settings → Agent.
- Chat is not counted.
- When it is used up, scheduled runs are skipped ("Skipped: daily token budget used up.") until the next day (Pakistan date).
- Settings → Agent → Agent Activity shows today's use.

## Web Search limits
- The agent and chat can use `web_search`, `web_fetch` (read a page or PDF) and `http_request` (read-only data APIs).
- Limits: 10 searches and 15 page fetches per agent run, and the same per chat reply. After that the AI works with what it has.
- Pages are cached for 15 minutes. Long pages can be read by the cheap model to save tokens.
- PSX website pages are never fetched by these tools (PSX terms). PSX data comes from Igent's own daily fetches.
- Search engines: Exa, then Tavily (if you added keys in Settings → Search), then DuckDuckGo (no key).
- Web text is treated as outside data, never as instructions.

## Scoring
- Every Friday at 18:00 Igent scores open and watching cards that are at least 7 days old, against the 3M T-bill rate at creation. Cards past their horizon are closed. **Score now** on Idea Cards does it at once.
- The agent sees its own scorecard each run and is told to learn from misses.
