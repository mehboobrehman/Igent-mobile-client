---
title: SBP Policy Rate and Monetary Policy
kind: macro
tags: [sbp, policy-rate, mpc, kibor, interest-rates, monetary-policy]
updated: 2026-10-10
sources:
  - https://www.sbp.org.pk
---

# SBP Policy Rate and Monetary Policy

The live policy rate, KIBOR and T-bill yields come from query_rates. MPC decisions and statements come from search_news / read_news and sbp.org.pk.

## Framework
- The State Bank of Pakistan's **Monetary Policy Committee (MPC)** sets the policy rate, the target for the overnight repo rate.
- **Interest rate corridor:** a ceiling (reverse repo) above and a floor (repo) below the policy rate, set at a fixed spread. Check SBP for the current spread.
- SBP sets a medium-term CPI inflation target range. Check sbp.org.pk for the current target.
- Each decision comes with a Monetary Policy Statement; minutes follow later. SBP publishes an advance meeting calendar for each fiscal year on its website.

## Rate cycles (patterns)
- Pakistan's policy rate moves in large cycles. It has been raised to very high levels in crises (e.g. 2023) and cut heavily during disinflation.
- Cycles can reverse. An energy or currency shock can bring hikes even after a long easing run.
- Surprise decisions happen. Markets often price in a move that does not come.

## Transmission: how a rate move flows through
- **KIBOR and T-bills:** move within days. T-bill yields above the policy rate signal that the market expects hikes; yields below it signal expected cuts.
- **Banks:** most loans are KIBOR-linked and reprice quickly. Deposits reprice more slowly, and the minimum savings rate is linked to the corridor. Historically, falling rates squeeze bank margins and rising rates help, with a lag.
- **Money market and income funds:** yields follow T-bills. When rates fall, income and government-bond funds get capital gains; when rates rise, they take mark-to-market losses. Check NAVs with query_navs.
- **Bonds (PIBs):** prices move inversely to yields. Long tenors are more sensitive.
- **Equities:** lower rates usually lift PSX valuations (cheaper financing, money rotating out of fixed income) and help leveraged sectors (cement, steel, autos, textiles) most. Hikes do the reverse. Banks are the main exception.
- **National Savings:** CDNS rates follow T-bill and PIB yields with a lag.
- **PKR:** higher real rates support the rupee and portfolio inflows.

## How to use in analysis
- Compute the **real policy rate** = policy rate - expected or current CPI.
- Example (made-up numbers): policy rate 12%, CPI 9%, real rate +3%. SBP usually aims for a clearly positive real rate, so a thin or negative real rate makes cuts less likely.
- Before each MPC meeting, check monthly CPI, oil prices, IMF reviews, reserves and the T-bill curve.
- Brokerage surveys before meetings show market expectations. Surprises move stocks.

## Pitfalls
- Assuming a cutting or hiking cycle continues automatically.
- Confusing the policy rate with what savers actually earn (after tax, after lags).
- Quoting an old rate. Always pull the live value with query_rates.
