---
title: Getting started with Igent
kind: app
tags: [igent, setup, first-run, welcome, onboarding, provider, how-to]
updated: 2026-10-10
sources: []
---

# Getting started with Igent

Igent is a Windows app that watches Pakistan's markets from your PC. It lives in the system tray. It fetches market data and news in the background, runs an AI agent at set times, and alerts you only when it finds something worth your attention.

**Igent is not financial advice.** It is a personal research tool. Its ideas can be wrong. Check every idea yourself before you invest.

## The welcome flow (first start)
On first start Igent opens **Welcome to Igent**. It has four steps, shown as badges: **1. AI provider**, **2. Your money**, **3. Watchlist**, **4. Done**. You can skip any step and change everything later in Settings.

### Step 1: AI provider ("Pick the AI that powers Igent")
Igent needs an AI model for chat and for the agent. Two routes:
- **Use your Claude Code login.** Shown only when Claude Code is installed and logged in on this PC. Press **Connect Claude**. Igent tests it and sets it as the default for all three model slots (newest Sonnet for chat and analysis, newest Haiku for background work). Personal use only.
- **Use an API key.** Press **Open provider settings**. Supported: Anthropic, OpenAI, Google Gemini, Groq, DeepSeek, xAI, OpenRouter, Z.AI, a local Ollama, or any OpenAI-compatible server. Settings → Providers also offers **Free (OpenCode Zen)**: free models with no key, rate limited. **OpenRouter** is free to start too: a free key and the model `openrouter/free`.
- Press **Next** (or **Skip for now** if nothing is connected).

### Step 2: Your money
- **Cash You Could Invest (PKR)**: Igent sizes ideas from this. It stays on this PC.
- **Tax Status**: **Filer (on the Active Taxpayer List)** or **Non-Filer**. This changes the tax used in analysis.
- **Shariah-compliant options only**: a toggle.
- Holdings are not entered here. Add them later on the **My Money** page.

### Step 3: Watchlist ("What should Igent watch?")
- Type PSX symbols, comma separated, for example `OGDC, HBL, LUCK`. Optional.
- Each gets the default alert level of ±5%. Igent checks each stock once a day after the market closes.
- Add mutual funds later on the **Watchlist** page.

### Step 4: All set
- **Go to dashboard** finishes setup.
- **Run the agent now** finishes setup and starts a first agent run. It is greyed out until a provider is connected.

## Connecting a provider later
1. Open **Settings → Providers** and press **Add provider**.
2. Pick a **Type**, paste the **API key** if needed, press **Add**.
3. Press **Test**. When the test passes, Igent fills any empty model slot (chat, analysis, cheap) from this provider.
4. **Make default** sets all three slots from that provider.
- For Claude Code: pick **Claude Subscription (Claude Code Login)**. The form shows whether Claude Code was found and whether it is logged in.

## Enter your money and holdings
- Open **My Money**. Type your cash in **Update Cash on Hand** and press **Save**.
- Use **Record a Buy** for each stock, fund, gold or National Savings scheme you own. Fill type, name, quantity, price paid, costs (optional) and date, then press **Add**. Later, record sales and dividends with the **Sell** and **Dividend** buttons, so Igent can show your real gains.
- Add your goals (for example Hajj or a house) in **My Money → Goals**.
- Keep cash up to date. The agent reads it to size ideas.

## What happens on first start
- A progress banner appears at the top: "Getting today's market data and news for the first time…" with a count of sources done (for example `5 / 24`) and the sources in progress ("Now: …").
- When it ends it says "Fresh data is in: X of Y sources worked." If some failed it adds "See Sources for the ones that failed." The banner hides a few seconds later.
- The same banner appears if Igent was closed for more than 3 days.
- Data keeps updating in the background, even when the window is closed to the tray.

## When the agent runs
- **Evening Run**: weekdays after the PSX close, default 18:00 Pakistan time.
- **Morning Run**: Monday to Saturday, default 10:00.
- Both are on by default; each has an on/off switch in Settings → Agent.
- **Manually**: **Run now** on the Dashboard, or **Run agent now** in the tray menu.
- Change the times in **Settings → Agent**.

## What to expect in the first days
- History builds up day by day. Charts, sparklines and changes need at least two days of data. A tile may say "no previous value" or "waiting for first fetch" at first.
- Fund comparisons need about a week of daily NAVs.
- PSX Breadth arrives after the daily market report (17:20 on weekdays).
- Most agent runs make no card. Quiet days are normal. The agent writes a card only when an idea clearly beats the safe rate.
- Idea Cards are scored after 7 days, so the track record takes a few weeks to mean much.

## Good next steps
- **New to investing?** Open **Learn** and start the course at Level 1. It remembers where you stopped. Try the **Practice** tab to trade with pretend money at real prices.
- **Already investing?** Open **Tools → Portfolio Check-Up** for a score and your top 3 fixes, then try **Should I Buy?** or **Should I Sell?** on a share.
- **Got an offer or a share tip on WhatsApp?** Paste it into **Tools → Scam Check** or **Tip Checker** before paying anything.
- Set **Alert Below / Alert Above** prices on the Watchlist for shares you want to buy or watch.
- Every Friday evening, read the **Weekly Summary** (Dashboard → Weekly Summary →).

## Good first questions for Chat
- "What can you do, and where does your data come from?"
- "Compare money market funds with National Savings for Rs 500,000 over one year."
- "What is the difference between a filer and a non-filer for my investments?"
- "Explain the KSE-100 tile on my dashboard."
- "Look at my holdings and tell me the main risks."
- "Which Shariah-compliant options suit a one-year horizon?"
