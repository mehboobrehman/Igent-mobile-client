---
title: Igent troubleshooting and FAQ
kind: app
tags: [igent, troubleshooting, faq, errors, sources, provider, claude-code, search, backup, restore, privacy, data-folder, uninstall, tools, my-money, zakat, price-alert]
updated: 2026-10-11
sources: []
---

# Troubleshooting and FAQ

## A source shows "failing"
- Open **Sources**. The failing row shows its last error in red.
- Igent retries on its own, about hourly (sooner for sources that run more often). Many failures are temporary: the site was down, slow or changed for a day.
- Press **Fetch now** to try at once.
- PSX, SBP, MUFAP and the CPI source publish on set days and times. Before that time "waiting" or an old date is normal. For example, PSX Breadth comes after the 17:20 market report on weekdays, and MUFAP NAVs in the evening.
- If one source keeps failing for days, the site may have changed its layout. Check **Settings → Logs → sensors** for details. You can switch the source off with its toggle.
- For a feed you added: check the URL is a real RSS, Atom, Google News or YouTube feed.

## "No AI provider" / Chat or the agent does not answer
- Errors such as "No AI provider configured. Add one in Settings → Providers." mean no enabled provider.
- Open **Settings → Providers**, press **Add provider**, then **Test**. **Free (OpenCode Zen)** works with no key.
- If a provider shows **failed**, press **Test** again. "(temporary, try again)" means a short outage or rate limit.
- "Provider … has no default model" means pick a **Default model** in the provider form, or set the slots in **Settings → Models**.
- Check the model slots in Settings → Models point at an enabled provider and an enabled model.
- "Tag news now" says "No AI provider for tagging" for the same reason.

## Claude Code not found or not logged in
- The Claude subscription provider needs Claude Code installed on this PC and logged in.
- The provider form shows "not found on PATH" when Igent cannot find `claude.exe`. Install Claude Code, then restart Igent.
- "not logged in (run `claude` once)": open a terminal, run `claude`, and log in. Then press **Test**.
- The **Use your Claude Code login** option in the welcome flow appears only when Claude Code is found and logged in.
- Or use an API key provider instead.

## Web Search fails or is blocked
- With no key Igent searches through DuckDuckGo, which sometimes blocks frequent searches.
- Fix: get a free key from Exa (exa.ai) or Tavily (tavily.com) and paste it in **Settings → Search**, then press **Test search**.
- "Web Search limit reached" or "Page fetch limit reached" is normal: there are 10 searches and 15 fetches per agent run or chat reply.
- PSX website pages are never fetched by web tools; PSX data comes from Igent's own sources.

## The agent says data is stale
- The agent treats data older than a few days as stale and says so.
- Check **Sources** for failing sources and press **Fetch now**.
- Was Igent closed? Data is fetched only while Igent runs. After a gap of more than 3 days a catch-up banner fetches everything again.
- Some data is slow by nature: CPI is monthly, National Savings rates are checked weekly, the policy rate changes only at SBP meetings.
- Weekends and holidays have no new PSX data.

## Agent runs are skipped or did not happen
- "Skipped: daily token budget used up.": raise **Daily Token Budget** in Settings → Agent, or wait for the next day.
- Runs happen only while Igent is running. Turn on **Start with Windows** and keep **Keep Running in the Tray When the Window Is Closed** on.
- Check the times in Settings → Agent. Evening Runs are weekdays only; morning runs Monday to Saturday.
- A run marked "Interrupted: Igent was closed during this run." was cut off by quitting.
- "The model stopped without calling finish_run." means the model ended early. Try again, or use a stronger Analysis model.

## No idea cards
- Normal. The agent writes a card only when an idea clearly beats the safe rate after risk and tax. Many days have none.

## I get no alerts / alerts arrive late
- Quiet Hours (default 22:00 to 08:00) hold alerts until they end.
- **Pause alerts 2 h** in the tray holds alerts for 2 hours.
- Big-move alerts and the daily digest are off by default; turn them on in Settings → Notifications.
- Check Windows notification settings allow Igent.
- Settings → Notifications → Recent Notifications lists what was sent; "(not shown)" means Windows did not display it.

## Tools, My Money and Learn: common messages
- **"This needs an AI model. Set one in Settings → Models."** (Should I Buy?, Should I Sell?): these tools need a model in the **Analysis** or **Chat** slot. Scam Check, Tip Checker, Compare Two and Fund Picker still work without one, using built-in rules; Portfolio Check-Up, Next Rupees and What If? never need one.
- **Fund Picker shows "No Funds Match"**, or **Next Rupees names no funds**: Igent needs about a week of daily fund prices (it keeps them from the day it was installed). Check back after a few evenings, or try another choice. Fund returns from a few weeks of prices can mislead.
- **"Some checks need more price history"** (Journal Review, Tip Checker run-ups, Should I Buy? 1-year change): Igent keeps share prices from the day it was installed, so older comparisons fill in over time.
- **A holding shows "no price yet"**: for a stock, check the PSX symbol (prices arrive after the PSX close, about 17:30 on weekdays); for a fund, the name must match MUFAP exactly (pick it from the suggestions in Watchlist or Compare Two to see the exact name). Savings schemes show "accrued" instead of a price.
- **"You hold only N of …"** when selling: the sale is larger than what My Money says you hold. Check the History list for a missing or wrong buy.
- **My holdings look wrong after deleting an entry**: holdings are worked out from the History (buys, sells, dividends). Deleting an entry recalculates everything; record the missing buy again with its real date.
- **Capital Gains Tax Still Due shows "–"**: type your own rate in the small box and click **Save** (My Money → Holdings → Gains This Tax Year). Igent does not store tax rates because they change with each budget.
- **Zakat says "enter the price"**: Igent has no silver price feed. Type today's silver price per tola (or choose the gold nisab, which uses Igent's gold rate).
- **A price alert did not fire**: it fires once, after the end-of-day price reaches your level, and quiet hours hold it until morning. If the row already shows **target hit**, type a new price to arm it again.
- **No Weekly Summary**: it is made on Friday at 18:30 if Igent is running, or at the next start from Friday evening to Sunday. Click **Make Now** on the page for one at any time.
- **"Nothing to review yet"** (Learn → Review My Mistakes): take a quiz first; questions you miss are asked again later.

## How to restore a backup
1. Open **Settings → Backups**.
2. Pick a backup marked **verified** and press **Restore**.
3. Confirm. Igent restarts with that backup. Everything after the backup time is lost.
- If the database is ever damaged at start, Igent restores the newest good backup by itself and keeps the damaged file. It shows a message "Igent recovered the database".

## Where your data and logs are
- Everything is in `%APPDATA%\Igent`:
  - `igent.db`: the database (money, holdings, watchlist, cards, chats, news, market data, settings).
  - `backups\`: database backups (newest 5).
  - `logs\`: daily log files (app, sensors, provider-errors, db-error), last 5 days.
  - `tools\`: optional place for `yt-dlp.exe`.
- **Open folder** buttons in Settings → Backups and Settings → Logs open these folders.

## Start Igent with Windows
- Settings → General → **Start with Windows**. Igent starts hidden in the tray. Works in the installed app only.

## Closing vs quitting
- Closing the window keeps Igent in the tray (default). To stop it fully, right-click the tray icon and choose **Quit Igent**.
- If "Keep Running in the Tray When the Window Is Closed" is off, closing the window quits Igent.
- Opening Igent a second time just brings the existing window forward.

## Updates
- See app/about-and-updates for how updates work and what each update message means.

## Uninstall
- Installed copy: uninstall Igent from Windows Settings → Apps. Portable copy: close Igent and delete its folder.
- Your data in `%APPDATA%\Igent` is kept. Delete that folder yourself if you want it gone.

## Privacy: what leaves your PC
- Your money, holdings, chats, cards and settings are stored only on this PC.
- API keys and search keys are encrypted with Windows DPAPI.
- **Sent to your AI provider**: your chat messages, and the data the AI reads to answer or run: market data, news, wiki pages, knowledge you added (files, links, videos), and your cash, holdings, buy and sell history, goals, watchlist and cards. Text you paste into Scam Check or Tip Checker is sent too. With a local Ollama model this stays on your PC.
- **Sent to the web**: Igent fetches public sites (PSX, SBP, MUFAP, news feeds, etc.) from your PC. When the AI searches, its search words go to Exa, Tavily or DuckDuckGo, and pages it reads are fetched from those sites. Your data is not posted to websites; web tools are read-only.
- **Update checks** ask GitHub for the latest Igent release. Nothing about you or your money is sent.
- Igent has no account and no server of its own.
