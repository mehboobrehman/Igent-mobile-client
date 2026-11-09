---
title: Gold Prices in Pakistan
kind: macro
tags: [gold, tola, apgjsa, pmex, gold-fund, commodities]
updated: 2026-10-10
sources:
  - https://www.pmex.com.pk
  - https://www.mufap.com.pk
  - https://www.fbr.gov.pk
---

# Gold Prices in Pakistan

Live local gold rates and the dollar rate come from query_rates. Gold fund NAVs come from query_navs.

## Units
- **1 tola = 11.6638 grams**. 1 troy ounce = 31.1035 g, so 1 tola = about 0.375 oz.
- Prices are quoted per tola and per 10 grams, usually for 24-karat. The 22K price is about 22/24 of the 24K price; jewellery adds making charges.

## How the local price is set
- The **All Pakistan Gems and Jewellers Sarafa Association (APGJSA)** announces daily rates, which the press reports.
- Approximate formula: **local price per tola = international USD/oz x 0.375 x PKR/USD + local premium (or discount)**.
- Example (made-up numbers): gold at $2,000/oz and PKR at Rs 300 per USD gives 2,000 x 0.375 x 300 = Rs 225,000 per tola. If the sarafa rate is Rs 228,000, the local premium is Rs 3,000 (about 1.3%).
- The premium rises when import restrictions bind, the PKR is under pressure or local demand spikes (wedding season, crisis hoarding). It can turn into a discount when demand is weak. City rates differ.
- Gold import and export rules change from time to time. Check current policy before assuming free flows.

## Ways to invest
- **Physical bars, coins, jewellery:** jewellery carries making charges and buy-back deductions. Bars have smaller spreads. There is storage and theft risk, and purity must be verified.
- **Gold funds (open-end commodity funds):** they invest mainly through PMEX deliverable gold contracts (e.g. Meezan Gold Fund, Shariah-compliant). Loads and expense ratios apply. Check available funds and NAVs with query_navs.
- **PMEX gold contracts** through PMEX-registered brokers: futures carry leverage and margin calls. Not suitable for most retail savers.
- Check `funds/etfs` for whether any gold ETF is listed on PSX.

## Taxes (brief)
- Sales tax applies to jewellery at rates that change in budgets.
- Gains on selling gold are, in principle, taxable as capital gains. Rates and holding-period rules change, so check the latest Finance Act. Gold fund redemptions follow mutual fund tax rules (see `basics/tax`).
- Filers must declare gold holdings in their wealth statements.

## How to use in analysis
- Pakistani gold return = USD gold return + PKR depreciation + change in local premium.
- Gold hedges against PKR crises and inflation, but pays no income. Compare it with the after-tax safe rate (`savings/safe-rates`).
- Suggest gold as a modest diversifier (a portfolio slice), not a core income holding.

## Pitfalls
- Using the 22K jewellery price as if it were investment-grade 24K.
- Ignoring the buy/sell spread and making charges, which can be several percent.
- Treating daily APGJSA rates as tradable prices. Dealer quotes differ.
