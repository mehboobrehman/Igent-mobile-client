---
title: Placing orders on PSX, liquidity and short-term trading pitfalls
kind: basics
tags: [orders, limit, market, liquidity, spread, volume, trading]
updated: 2026-10-10
sources:
  - https://www.psx.com.pk/psx/exchange/general/trading-hours
  - https://dps.psx.com.pk/
---

# Orders, liquidity and trading pitfalls

## Order types (as offered by most PSX broker terminals and apps)
- **Limit order:** buy or sell at your price or better. This is the default and safest choice for retail investors. An unfilled order rests in the order book.
- **Market order:** executes immediately against the best available prices. In thin stocks it can fill far from the last price. Avoid it in illiquid names.
- **Pre-open orders:** entered in a short pre-open session before continuous trading and matched at a single opening price. They **cannot be cancelled or changed** until the matching ends. Check PSX for current session timings.
- Broker apps may offer stop-loss or "good-till" features. Their availability and behaviour vary by broker, so confirm before relying on them.
- Quantity can be **1 share** in the regular market (PSX moved to a one-share lot in 2024). Some products still use larger lots. Price ticks are small fixed increments.
- Price cannot go beyond the daily **circuit band** around the previous close (±10% since July 2024; check PSX for the current rule). Orders outside the band are rejected.

## Reading the screen
- **Bid/ask (best buy and sell) and spread:** in the largest, most liquid stocks the spread is usually a fraction of a percent. In small caps it can be several percent.
- **Order book depth:** quantity at each price level. Thin depth means your order moves the price.
- **Volume:** shares traded. Use **traded value** (Rs) and compare it with the stock's own 1–3 month average.
  - Rising price on rising value suggests real demand.
  - Big volume in a very low-priced (penny) stock is often speculative churn.
- **Free float:** a low free float plus a sudden volume spike raises manipulation risk.
- **LDCP:** last day closing price, the reference for the circuit band.

## Liquidity rules of thumb for advice
- A position should be small relative to average daily traded value, for example under 5–10% of one day's value, so it can be exited in a day.
- Prefer KSE-100, and especially KSE-30, stocks for core holdings.
- Upper or lower locked stocks may have no sellers or no buyers. You may be stuck for days.

## Short-term trading pitfalls
- Round-trip costs (commission, sales tax, fees) plus capital gains tax eat into every trade. Frequent trading needs a big edge to beat a money market fund. See basics/accounts and basics/tax.
- **Settlement:** PSX moved to T+1 on 9 February 2026 (check PSX for the current cycle). You must pay for buys by settlement. Selling shares you do not hold ("blank selling") is prohibited outside regulated short-sale rules.
- Margin (MTS, margin financing) and futures amplify losses and can trigger forced liquidation.
- **Market halts:** a large KSE-30 move triggers a market-wide halt (see basics/psx). Gaps after news (budget, IMF, MPC, geopolitics) are common.
- Rumour-driven "tips" in WhatsApp and Telegram groups are a classic pump-and-dump channel. SECP has warned about unlicensed advisers.
- Ex-dividend dates: the price drops by about the dividend. Buying just before the ex-date only to "get the dividend" gains nothing after tax.

## Checklist before any order
1. Is the stock liquid enough? Check value traded and spread (query_prices).
2. Use a limit order near the current bid/ask.
3. Size the position against portfolio rules.
4. Know the thesis and the exit (target or stop) beforehand.
5. Compare the expected return after costs and tax with the safe rate (savings/safe-rates).
