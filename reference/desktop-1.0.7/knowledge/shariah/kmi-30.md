---
title: KMI-30 index
kind: shariah
tags: [kmi-30, index, shariah, benchmark, psx]
updated: 2026-10-10
sources:
  - https://www.psx.com.pk/psx/product-and-services/indices
  - https://en.wikipedia.org/wiki/KMI_30_Index
  - https://dps.psx.com.pk/
---

# KMI-30

## What it is
- The **KMI-30** tracks the **30 most liquid Shariah-compliant companies** listed on PSX.
- It was launched on 2 September 2008 (base period 30 June 2008), jointly by the then Karachi Stock Exchange and Al Meezan Investment Management / Meezan Bank.
- **Weighting:** free-float market capitalisation, with a **cap** on any single company's weight (12% in the published methodology; check PSX for the current rule).
- **Universe:** companies on the PSX-KMI All Share list, which pass the business and financial-ratio screens (see shariah/screening).
- **Recomposition:** periodic, per the PSX index methodology. Follow PSX notices on dps.psx.com.pk. Membership also changes when a company fails a Shariah review.

## Related Islamic indices
| Index | Content |
|---|---|
| PSX-KMI All Share | All Shariah-compliant listed companies |
| MZNPI (Meezan Pakistan Index) | A narrow basket of top KMI-30 stocks; tracked by a Meezan ETF |
| MII30 (Mahaana Islamic Index) | 30 liquid stocks from KMI All Share; tracked by a Mahaana ETF |

Index rules (basket size, liquidity filters, rebalancing frequency) are set in each index's methodology and can change. Check the PSX indices page.

## How to check membership
1. PSX Data Portal (dps.psx.com.pk), Indices section: select KMI30 to see constituents and weights. Igent can read index data with query_index.
2. PSX notices on index recomposition and on the periodic "Shariah compliant companies" review, linked from PSX's indices page (query_announcements).
3. Islamic fund FMRs list holdings, which must be compliant.

Igent should treat any cached member list as stale after a PSX recomposition or Shariah review notice.

## Typical composition (qualitative)
Historically heavy in oil and gas exploration, fertilizer, cement, power, oil marketing and Islamic banking. It contains **no conventional banks**, which is a key difference from KSE-100. Read current members and weights live from PSX.

## Using KMI-30 in analysis
- It is the correct benchmark for Shariah equity funds and Shariah stock portfolios.
- Performance diverges from KSE-100 when conventional banks move sharply. Bank rallies lift KSE-100 more. Energy and commodity moves affect KMI-30 more.
- The weight cap and a large energy weight make KMI-30 sensitive to circular-debt news, oil prices and gas pricing decisions.
- Index-level moves of KSE-30, not KMI-30, trigger the PSX market halt.

## Pitfalls
- A stock's Shariah status can change between reviews, for example when a company takes conventional loans.
- KMI-30 membership is for liquid names only. A compliant small cap can be in KMI All Share but not KMI-30.
