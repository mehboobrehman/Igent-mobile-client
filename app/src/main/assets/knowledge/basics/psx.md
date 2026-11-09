---
title: How the Pakistan Stock Exchange (PSX) works
kind: basics
tags: [psx, trading-hours, settlement, circuit-breakers, indices, cdc, nccpl]
updated: 2026-10-10
sources:
  - https://www.psx.com.pk/psx/exchange/general/trading-hours
  - https://www.psx.com.pk/psx/product-and-services/indices
  - https://dps.psx.com.pk/
  - https://www.cdcpakistan.com/
  - https://www.nccpl.com.pk/
---

# How PSX works

PSX is Pakistan's only stock exchange (the Karachi, Lahore and Islamabad exchanges merged in 2016). It is regulated by SECP. Trading is fully electronic through brokers (Trading Rights Entitlement Certificate holders, "TREC holders").

## Trading hours (general pattern)
- Trading runs Monday to Friday, with a **pre-open session** (orders collected and matched at one opening price), then **continuous trading**, then a short **post-close** session.
- Friday has a different schedule with a break for Jumma prayers.
- Hours change in Ramadan and on special notices. The market is closed on public holidays.
- Orders placed in pre-open cannot be cancelled or modified until the matching ends.
- Check the PSX trading-hours page for current timings.

## Settlement
- Ready-market trades moved from T+2 to **T+1** on **9 February 2026** (SECP/PSX). T+1 means settlement one business day after the trade. Check PSX for the current cycle.
- Buyers must fund by settlement. Sellers must have the shares in their CDC account.

## Price limits and market halts
- **Stock-level circuit breaker:** a stock's price can move only a set % from the previous close in one day. PSX raised this band from 7.5% to 10% in steps, completed in July 2024. A stock at the limit is "locked" (upper or lower circuit). Check PSX for the current limit.
- **Market-wide halt:** if the **KSE-30** index moves by a set % (5% under the long-standing rule) and stays there for a few minutes, all trading halts, then resumes through a pre-open. Check PSX for the current rule.

## Lot size
- The regular market moved to a **one-share marketable lot** in 2024, so you can buy 1 share.
- Some products (for example some ETFs, deliverable futures and IPOs) may still use larger lots. Check the symbol's lot size.

## Markets
- **Ready (regular) market:** cash trading.
- **Futures:** Deliverable Futures Contracts (monthly, physically settled), cash-settled futures and index futures. Leverage and margin calls make these unsuitable for most retail investors.
- **Margin products:** Margin Trading System (MTS) and margin financing let investors borrow. Financing costs track KIBOR.
- Also odd-lot, negotiated deals, bills and bonds, and the GEM board.

## Main indices
| Index | What it is |
|---|---|
| KSE-100 | Benchmark. Largest companies by free-float market cap, with sector representation. Recomposed periodically (follow PSX notices). |
| KSE-30 | 30 most liquid companies, free-float weighted. Drives the market-halt rule. |
| KMI-30 | 30 most liquid Shariah-compliant stocks, free-float weighted with a per-stock cap. See shariah/kmi-30. |
| PSX-KMI All Share | All Shariah-compliant listed companies. |
| All Share (ALLSHR) | All listed companies. |

There are also sector indices (e.g. banks, oil and gas), dividend indices and indices that ETFs track. Use query_index for live levels.

## Infrastructure
- **CDC (Central Depository Company):** holds shares electronically in investor sub-accounts (under a broker) or Investor Accounts (directly with CDC). Check holdings through CDC's online access and SMS alerts to detect broker misuse.
- **NCCPL (National Clearing Company):** clears and settles trades, nets obligations, manages risk and margins, issues the UIN (unique investor number) and **computes and collects capital gains tax** on listed securities.

## Pitfalls for analysis
- Index points are not returns. Compare % changes and include dividends (total return).
- KSE-100 is concentrated in a few sectors (historically banks, oil and gas exploration, fertilizer, cement, power). It is not "the economy".
- Locked stocks (at the circuit limit) can be impossible to exit the same day.
