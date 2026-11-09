---
title: Igent glossary of terms and labels
kind: app
tags: [igent, glossary, terms, labels, confidence, scorecard, model-slots, badges]
updated: 2026-10-10
sources: []
---

# Igent glossary

Plain meanings of the words and labels you see in Igent, grouped by area.

## Idea Cards
- **Idea card**: an idea the agent wrote: what to do, why, the risks and how sure it is. At most 3 new cards per run, and never two open cards for the same subject.
- **Kind**: what the idea is about: stock, fund, gold, savings, cash or avoid.
- **avoid**: a card that says to stay away from something. It counts as right when the price falls.
- **Confidence** (e.g. "65% confidence"): how sure the agent is, 0–100. Green at 70 or more, amber from 55 to 69, grey below 55. It is a judgement, not a probability of profit. Only cards at 55 or more may raise an opportunity alert.
- **Size % / "size N% of cash"**: the share of your current cash the idea suggests. It is capped by "Largest Single Idea (% of Cash)" in Settings → Agent (default 20%).
- **Horizon / "horizon N days"**: how long the idea is meant to run. The card is closed automatically when the horizon passes.
- **Why**: the thesis, with the data and dates the agent used.
- **Risks**: 2 to 6 things that could make the idea wrong.
- **Entry level**: the price, NAV or rate when the card was made. Taken from Igent's latest stored data if the agent gave none.
- **Safe alternative**: the safe option the idea must beat, such as a T-bill, money market fund or National Savings. The card also shows what the 3M T-bill was at creation.
- **Status**: **open** (active), **watching** (kept an eye on, still scored), **closed** (finished, by you or at the horizon).
- **acted / Mark acted**: your note that you followed the idea. It does not change the score.
- **Scorecard**: the result box on a card. "scored after 7 days" until then.
- **Score labels**: compare the card's return with the safe rate over the same days (the gap is "excess", in percentage points):
  - **very good**: 5 pts or more ahead
  - **good**: 1 to 5 pts ahead
  - **neutral**: within 1 pt
  - **poor**: 1 to 5 pts behind
  - **very poor**: 5 pts or more behind
  - **not scored**: too new, or no price yet.
- **Track record / hit rate**: on the Idea Cards header. Hit rate is the share of scored cards rated good or very good. "Average +N pts vs the safe rate" is the average excess.
- **Outcome note**: a line added when a card closes, e.g. "Closed at horizon: …".
- **Score now**: scores cards older than 7 days right away. It also runs every Friday at 18:00.

## Dashboard
- **KSE-100**: the main PSX index, in points.
- **US Dollar (Open Market, Sell)**: the open-market selling rate of the dollar in rupees.
- **US Dollar (Reference Rate, forex.pk down)**: shown instead when forex.pk has failed for over 3 days. It is the international reference rate (series usd_ref) from a free currency service, usually a few rupees below the open-market rate. It is a backup, not the price at a money changer.
- **Gold 24K per Tola**: the Karachi rate for 24-karat gold per tola.
- **KIBOR 6M (Offer)**: the 6-month Karachi Interbank Offered Rate (offer side). Banks price many loans off it.
- **SBP Policy Rate**: the State Bank's main interest rate.
- **CPI Inflation (YoY)**: consumer price inflation vs the same month last year.
- **pts** (on rate tiles): the change in percentage points.
- **as of <date>**: the date of the latest value. Old dates mean stale data.
- **PSX Breadth**: how many shares rose (▲ advancers), fell (▼ decliners) or stayed flat (● unchanged) on the last trading day.
- **Big Moves**: moves above a limit: KSE-100 ±3% in a day, dollar ±1.5%, gold ±2%, a policy rate change, or a watchlist item beyond its own alert %.
- **Safe Rates**: low-risk yields: T-bills (T-bill 1M, 3M, 6M, 12M as published) and National Savings schemes:
  - **Defence Savings Certificates** (code dsc)
  - **Behbood Savings Certificates (also PBA, SFWA)** (bsc; PBA is the Pensioners' Benefit Account, SFWA the Shuhada Family Welfare Account, listed together)
  - **Regular Income Certificates** (ric)
  - **Savings Account** (sa)
  - **Shuhada Islamic Savings Account** (sisa)
  - **Sarwa Islamic Term Account** (sita)
- **Latest News**: newest news items, filterable by category.
- **One-liner**: a short AI line under a news item saying why it may matter for markets. Irrelevant items get "Not market-relevant." and the agent skips them.

## Agent
- **Agent run**: one working session of the AI agent. It reads what changed, updates the wiki, maybe writes cards and alerts, then writes a summary.
- **Trigger**: why a run started: **evening**, **morning**, **manual** (Run now / tray) or **urgent** (Urgent Wake).
- **Run status**: **running**, **completed**, **failed**, **cancelled** (stopped, or Igent closed mid-run), **skipped** (daily token budget used up).
- **Steps**: the tools a run used, shown in Settings → Agent → Agent Activity.
- **Urgent Wake**: lets a big move start an extra run (at most one every 3 h). Off by default.
- **Token budget**: the daily limit on tokens for background AI work (agent runs and news one-liners). Chat is not counted. Default 500,000.
- **Reminder**: a scheduled notification, usually set by the agent (e.g. before a results announcement).
- **Daily digest**: an optional summary toast after the evening run when no other alert was sent.

## Model Slots and providers
- **Provider**: an AI service Igent uses, e.g. Anthropic API, Claude Subscription (Claude Code Login), Free (OpenCode Zen), OpenAI, Ollama (local).
- **default** (provider badge): the provider whose models fill the slots.
- **working / failed / not tested**: result of the provider's last **Test**.
- **Model slot**: which model does which job. Three slots:
  - **chat**: answers you in Chat.
  - **analysis**: runs the agent and writes idea cards. Use a strong model.
  - **cheap**: background work: news one-liners and reading long web pages for the agent. A small, low-cost model is enough; it can be the Free provider.
- **Favorite (★)**: a starred model, listed first in Settings → Models.

## Sources
- **Source**: a site or feed Igent fetches from your PC.
- **ok**: the last fetch worked.
- **failing**: the last try failed after the last success. The error is shown in red. Igent retries about hourly.
- **waiting**: enabled but no successful fetch yet.
- **off**: you turned the source off.
- **last ok**: when it last worked.
- **Fetch now**: fetch this source right now.
- **Tag news now**: write one-liners for untagged news now, with the cheap model.

## Knowledge
- **Wiki / Learned by Igent**: pages the agent writes and updates itself, like macro/situation, and product/ and rule/ pages about new investments and rule changes it finds. They are not checked by a person; the Knowledge pack is.
- **Learn / Practice / Quiz**: the Learn page's tabs: the beginner-to-expert course, paper trading with pretend money at real saved prices, and AI-written quizzes by level.
- **Review My Mistakes**: a quiz of the questions you missed plus new ones on the lessons behind them.
- **Average cost (Avg Cost)**: in My Money, the total paid for a holding including costs, divided by the units. Repeat buys change it; sales do not.
- **Realised gain**: the profit or loss on units actually sold: sale amount minus costs minus their average cost. **Tax year**: 1 July to 30 June.
- **Tax Taken / Tax Withheld**: tax deducted at source, capital gains tax on a sale or withholding tax on a dividend.
- **Alert Below / Alert Above, target hit**: watchlist price alerts; "target hit" means the close reached your price and Igent has told you.
- **Should I Buy? / Scam Check**: the two tabs of the Tools page. **Weekly Summary**: the Friday page that sums up your week.
- **Added by You**: the Wiki tab for knowledge you add with **+ Add Knowledge** (files, web pages, YouTube videos). Igent writes a page for each one; the facts come from your source, not checked by Igent.
- **Knowledge pack**: notes bundled with Igent that explain how things work (tax, funds, savings, sectors, companies, this app). They hold no live numbers.

## Your settings and money
- **Filer**: on FBR's Active Taxpayer List. Pays lower withholding tax on many investments. **Non-Filer**: not on the list; higher rates. See basics/filer-vs-non-filer.
- **Shariah-only**: when on, Igent suggests only Shariah-compliant options (KMI indices, Islamic funds, Islamic savings).
- **Cash on Hand**: money you could invest. Ideas are sized from it.
- **Holding**: something you already own (stock, fund, gold, savings).
- **Tola**: the unit for gold, about 11.66 grams.

## Alerts and indicators
- **Notification / toast / alert**: a Windows pop-up from Igent. Clicking it opens the related page.
- **Quiet Hours**: a nightly window when alerts are held and shown afterwards. Default on, 22:00 to 08:00 Pakistan time.
- **Pause alerts 2 h**: a tray menu item that holds alerts for 2 hours.
- **Red dots / badges**: a red dot on the taskbar button and tray icon, and a red count next to Dashboard or Idea Cards, mean something new you have not seen. Opening that page clears it.
- **(not shown)**: in Recent Notifications, an alert Windows could not display.
