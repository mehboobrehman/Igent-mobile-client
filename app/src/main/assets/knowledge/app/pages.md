---
title: Igent pages tour
kind: app
tags: [igent, pages, dashboard, chat, idea-cards, watchlist, my-money, learn, tools, weekly-summary, wiki, sources, tray, how-to]
updated: 2026-10-10
sources: []
---

# A tour of Igent's pages

The sidebar on the left lists: **Dashboard**, **Chat**, **My Money**, **Idea Cards**, **Watchlist**, **Tools**, **Learn**, **Glossary**, **Wiki**, **Sources**, **About** (version, updates: app/about-and-updates), and **Settings** at the bottom. A red dot and number on **Dashboard** or **Idea Cards** means something new (app/alerts-and-agent).

## Dashboard
The overview page.
- **Header line**: "Sources: N ok", plus "N failing" (a link to Sources) when any fail, and the number of news items in the last 24 h.
- **Tiles** (top row): KSE-100, US Dollar (Open Market, Sell), Gold 24K per Tola, KIBOR 6M (Offer), SBP Policy Rate, CPI Inflation (YoY). Each shows the latest value, the change (green up, red down; rates show "pts"), a small trend line, "as of <date>" and its refresh time (Pakistan time): KSE-100 weekdays 17:10, Dollar and Gold every 2 h, KIBOR and Policy Rate weekdays 12:30, CPI weekdays 11:00 (new value about monthly). "waiting for first fetch" means no data yet.
- **Agent** card: the last run (time, trigger, status badge, summary). **Run now** starts a run. While it runs the card shows "Working: <step>" and a **Stop** button. Up to 3 open idea cards are listed; click one to open it.
- **PSX Breadth**: advancers (▲), decliners (▼) and unchanged (●) from the daily market report, with a bar. Before data arrives it says it comes after the PSX close report (17:20).
- **Big Moves**: recent big moves detected (see app/alerts-and-agent). "No big moves detected." when none.
- **My Money**: total (cash + holdings), cash, holdings value with gain since cost, today's change in rupees, and what your idle cash could earn a month before tax in the best simple safe option (best money market fund, else the 3-month T-bill). **Open** goes to My Money.
- **My Stocks Today** (top row, right of My Money): your held and watched shares sorted by today's move; "held" marks holdings and an **alert** badge shows a move above your alert level.
- **Coming Up** (second row, first slot): book closures (dividend cut-offs) in the next 3 weeks and dividends, bonus or right issues declared recently for your holdings and watchlist, plus your reminders due in the next 2 weeks.
- **Market Movers**: top 5 KSE-100 gainers (up), losers (down) and volume leaders of the last full trading day.
- **Safe Rates**: T-bill yields by tenor (for example T-bill 3M) and National Savings scheme rates, plus the best money market funds by annualised return once a week of fund prices exists.
- **Weekly Summary →** (top right) opens the Friday weekly summary (see app/money-and-tools).
- **Latest News**: newest items with source, age and the AI one-liner. The dropdown filters by category: All, Business, Politics, World, Commodities, YouTube. Click a headline to open it in your browser.

## Chat
Ask Igent anything about investing in Pakistan.
- **New chat** starts a conversation. Past chats are listed on the left with their age.
- Hover a chat to see ✎ (**Rename**) and ✕ (**Delete**, asks first).
- The top right shows the **chat** model slot: pick the provider and model for chat here.
- Type in the box at the bottom (it grows as you type). **Enter** sends, **Shift+Enter** adds a new line. The round arrow button sends; while Igent answers it becomes a square **Stop** button.
- The **microphone** button opens Windows voice typing (same as pressing **Win+H**): speak and Windows types into the box. Windows may first ask you to allow the microphone (Settings → Privacy & security → Microphone). Press the button or Win+H again to stop.
- Answers can show a collapsible **Reasoning** block (only when "Show Model Reasoning in Chat" is on, Anthropic models) and **Tool** blocks you can expand to see what Igent looked up (marked "done" or "failed").
- Under the latest answer, **Related** topic buttons and 2–3 follow-up questions appear; click one to ask it.
- Under each answer: the model id, tokens in/out and cost when known.
- For big questions Igent can run **deep research**: it searches many sites, reads up to 15 pages and writes a cited report (takes 1–3 minutes; shows as the deep_research tool).
- Chat can read your money, watchlist, cards, news, market data, the wiki, the knowledge pack and the web. It cannot send alerts or write the wiki; only the agent does that.

## Glossary
An A–Z list of about 900 investing, banking, tax, economy and Islamic-finance terms in plain words, with examples.
- Search by term, abbreviation (e.g. KIBOR, NAV) or words in the meaning; filter by category; jump with the A–Z letters.
- Hover a term and click **Ask Igent** to start a chat that explains it with a Pakistani example.
- Chat and the agent use the same glossary (define_term), so explanations match.
- In Chat answers and Learn lessons, glossary words have a dotted underline. Hover one (or press Tab to reach it) to see its meaning. Each word is marked once per answer or lesson.

## Idea Cards
Ideas the agent found.
- **Header**: before any card is scored it reads "Ideas the agent found. Each is scored against the safe rate after 7 days." Later it shows the track record: number scored, hit rate, average points vs the safe rate.
- **Score now** scores open cards right away (normally done every Friday at 18:00).
- **Filters**: open, watching, closed, all.
- **List**: each card shows title, confidence badge, kind and symbol, date, status, "acted" and the score label.
- **Detail**: Why (thesis), Risks, Entry level, Safe alternative, Scorecard, and an outcome note. Every card ends with "This is analysis, not a promise."
- **Buttons**: **Mark acted** / **Unmark acted** (record whether you followed it), **Watching**, **Close**, and **Reopen** for closed cards.
- Empty list: quiet days are normal; a card is written only when an idea beats the safe rate.

## Watchlist
Stocks and funds Igent checks every day.
- **Add**: choose **Type** (**PSX stock** or **Mutual fund**), type a symbol or fund name (suggestions appear after 2 letters; fund search uses MUFAP names), set **Alert at ±%** (default 5, from 0.5 to 50), press **Add** or Enter.
- **Watching** table: Name, Last, Change, Trend (30), Alert, **Remove**. Edit the alert % in the table; it saves when you click away.
- A move bigger than the alert % counts as a big move.
- **Alert Below** / **Alert Above**: price alerts. Igent toasts once when the close or NAV reaches the price and marks the row **target hit** (see app/money-and-tools).

## My Money
Your cash and holdings; it stays on this PC. Tabs:
- **Holdings**: Update Cash on Hand, **Record a Buy** (Stock, Fund, Gold, Savings; repeat buys join at the average cost), and per holding **Sell** / **Encash**, **Dividend** and **Remove**. **Gains This Tax Year** shows realised gains, dividends, tax taken and a capital gains tax estimate with your own rate. **History** lists every entry.
- **Goals**: targets such as Hajj or a house, with the monthly amount needed and on track / behind.
- **Safety Net**: optional bank money, expenses, debt, gold, silver, property (app/safety-net).
- **Zakat**: zakat on My Money plus what you add (silver or gold nisab).
Every button is described in app/money-and-tools.

## Learn
The beginner-to-expert course, the Practice tab (pretend money at real prices) and the Quiz tab, including **Review My Mistakes**. Every button is described in app/learn.

## Tools
Seventeen tools in a side menu, in four groups: Decide, Plan Ahead, Review, Stay Safe. See app/money-and-tools and app/tools-more.

## Wiki
Three tabs:
- **Learned by Igent (N)**: pages the agent writes and keeps current: macro/situation, company, sector and fund pages, **product** pages (new funds, REITs, IPOs, schemes, government securities) and **rule** pages (laws, tax, regulator rules). Weekly, a run also checks the SECP, SBP, MUFAP and National Savings sites for new items. No person checks these pages. Empty until the first run.
- **Knowledge pack (N)**: the notes bundled with Igent (basics, tax, funds, savings, sectors, companies, Shariah, and these app notes).
- **Added by You (N)**: knowledge you add yourself. Igent uses it in Chat and agent runs, and says when an answer uses it.
- Use **Filter** to search titles. Pages are grouped by kind. Each page shows when it was updated.

### Add Knowledge
The **+ Add Knowledge** button (top right) opens a window:
- **Drop files** on it, or click **Choose Files**. Igent reads PDF, DOCX, TXT and MD files up to 25 MB each. A scanned PDF (pictures of pages) and old .doc files cannot be read.
- **Or Add a Link**: paste a web page, a PDF link or a single YouTube video, then click **Add Link** (or press Enter). PSX pages are refused (PSX terms). YouTube channels and playlists are refused.
- **YouTube videos** need yt-dlp.exe on PATH or in `%APPDATA%\Igent\tools`. Igent reads the captions and the description only; it never downloads the video. A video without captions gives only its description.
- **Local video and audio files** (MP4, MP3 and so on) are not supported, because Igent cannot turn speech into text.
- Items are added one at a time, up to about a minute each. The AI reads the first 60,000 characters and writes a page: a title, a category (Company, Sector, Fund, Product, Rule, Tax, Banking, Macro, Strategy, Personal or Other) and a summary with key facts. Igent keeps 200,000 characters of the text for search.
- Adding the same file name or link again updates the old item instead of making a copy. You can keep up to 500 items.
- Without an AI model set up, the item is still saved with its original text, but without a summary.

Each added item shows a **Yours** badge, where it came from, and buttons:
- **Edit**: change the title, category and page text, then **Save**.
- **Delete**: click it, then **Confirm Delete**.
- **Show Original Text**: the start of the text Igent read from your source.

## Sources
Where Igent gets its data.
- Groups: **Market Data** (SBP, PSX indices, PSX daily market report, PSX company pages, MUFAP NAVs, gold, dollar, National Savings, PBS CPI), **News Feeds**, **YouTube**.
- **PSX daily market report** also reads PSX's daily closing rate summary, so Igent keeps a daily price history (open, high, low, close, volume) for every listed company from the day it was installed.
- **PSX company pages** (watchlist and held stocks, daily) also store each company's profile: market cap, shares, free float, business description, 4 years and 4 quarters of sales, profit and EPS, and margins. Other companies the agent or chat looks at are read too (once a day each). For watched and held companies Igent also reads each new results notice (PDF) once, to record dividends declared, bonus and right issues, and book closure dates. XD, XB and XR (ex-dividend, bonus, rights) days come from the daily closing rate summary.
- Each row: an **Enabled** toggle, the name, a status badge (ok, failing, waiting, off), the category, the schedule, "last ok", items in 24 h, and the last error when failing.
- **Fetch now** fetches one source immediately.
- **Remove** appears only for feeds you added.
- **Tag news now** (top right) asks the cheap model to write one-liners for untagged news now.
- **Add an RSS Feed**: Name, Feed URL (RSS, Atom, Google News or YouTube Feed), Category, then **Add**.

## Settings
Tabs: General (opens first), Providers, Models, Search, Agent, Notifications, Backups, Logs. See app/settings.

## Tray icon
Click or double-click to open Igent. Right-click menu: **Open Igent**, **Run agent now**, **Pause alerts 2 h**, the last run line, **Quit Igent**. Closing the window keeps Igent running in the tray (unless you turn that off).
