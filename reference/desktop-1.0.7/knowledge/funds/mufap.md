---
title: MUFAP - what it publishes and how Igent uses it
kind: funds
tags: [mufap, nav, data, aum, returns]
updated: 2026-10-10
sources:
  - https://www.mufap.com.pk/
---

# MUFAP

The **Mutual Funds Association of Pakistan (MUFAP)** is the industry body of SECP-licensed AMCs. It is not the regulator. SECP regulates. MUFAP collects and publishes data and sets industry standards (for example on performance reporting and fund categorisation) in coordination with SECP.

## What MUFAP publishes (website sections)
- **Daily NAVs:** NAV, offer price, redemption price and front/back-end load for every open-end fund, VPS sub-fund and ETF, by AMC and category.
- **Performance / returns:** fund returns over standard periods (MTD, FYTD, 30/90/365-day, etc.). Money market and income funds are shown annualised, equity funds as absolute returns.
- **Payouts:** dividend distributions per unit with ex-dates.
- **Industry statistics (monthly):** assets under management (AUM) by category, by Shariah/conventional and by AMC, plus the industry's asset allocation.
- **Fund directory:** fund names, category, AMC, launch date, risk profile, trustee and Shariah status.
- **VPS and dedicated plans** data, and regulatory or industry notices.

Industry size (total AUM) grows and shifts over time. Read the latest figure from MUFAP's monthly statistics instead of quoting a remembered number.

## How Igent uses MUFAP data
Igent reads MUFAP NAVs and returns through the query_navs tool.
1. **Safe-rate benchmark:** the average or top-quartile money market fund annualised return (conventional and Islamic) feeds the "safe rate" in savings/safe-rates.
2. **Fund screening:** rank funds within the same category and Shariah type on 90-day, 1-year and 3-year returns, then filter out tiny funds (low AUM) and high-load funds.
3. **NAV tracking:** value the user's fund holdings daily (units × redemption price).
4. **Category flows:** shifts of AUM from money market to equity funds are a sentiment indicator for PSX.

## Data handling rules
- MUFAP returns are **net of fund expenses, before loads and the investor's taxes**. Igent must deduct both for take-home comparisons.
- Check the "as of" date. NAVs are published after market hours, and holidays create gaps.
- Annualised short-period returns (e.g. 7- or 30-day) are noisy. Prefer longer windows.
- Fund names change after mergers or rebrands. Match on fund name plus AMC, and keep history.
- If MUFAP data is missing or stale, say so instead of guessing. The AMC's Fund Manager Report (FMR) is the secondary source.
