---
title: Reading NAV, loads, expense ratios and fund returns
kind: funds
tags: [nav, loads, ter, returns, annualised, mufap]
updated: 2026-10-10
sources:
  - https://www.mufap.com.pk/
  - https://www.secp.gov.pk/document/circular-no-06-of-2022-requirements-for-assessing-suitability-and-risk-categorization-of-collective-investment-schemes-cis/
---

# Reading NAV and comparing funds

## Key terms
- **NAV per unit:** (fund assets − liabilities) ÷ units outstanding. It is calculated every business day, net of fees. Open-end funds use *forward pricing*: you get the NAV of the day your request is accepted (before the cut-off time), not yesterday's.
- **Offer price:** what you pay = NAV + front-end load (+ any transaction cost charge).
- **Repurchase (redemption) price:** what you receive = NAV − back-end load (if any).
- **Front-end load:** a one-time % charged on purchase, mostly on equity and balanced funds. It is often zero for money market funds and for online or direct investing. It reduces the effective return.
- **Back-end / contingent load:** charged on early exit from some plans (e.g. fixed-term, capital-protected).
- **TER (Total Expense Ratio):** annual cost including management fee, sales tax on the fee, SECP fee, trustee and other costs. It is shown in the Fund Manager Report (FMR) as % of average net assets. Returns published by MUFAP and AMCs are already **net of TER** but **before loads and your taxes**. SECP sets TER caps by category; check SECP for the current caps.
- **Dividend payout vs growth:** a cash dividend lowers NAV by the payout amount. For returns, use NAV adjusted for distributions (MUFAP returns account for this).

## How returns are reported (convention)
- **Money market and income funds:** usually **annualised** returns (e.g. "YTD x% p.a.", "30-day annualised"). This lets them be compared with deposit and T-bill rates. A 30-day annualised figure can swing, so prefer 90-day or 1-year figures.
- **Equity, balanced and asset allocation funds:** usually **absolute** (cumulative) returns for MTD, FYTD, 1-year, 3-year and since inception.
- Pakistan's fiscal year runs **July–June**, so "YTD" in Pakistani fund reports usually means FYTD from 1 July, not calendar YTD.

## Comparing correctly
1. Same category and same Shariah/conventional type, compared over the same period.
2. Convert to the same basis. For annualised to period return, use (1+r)^(days/365)−1. CAGR for multi-year = (End/Start)^(1/years) − 1.
3. Subtract loads you will actually pay, given the expected holding period.
4. Apply your tax: CGT on redemption gains and withholding on dividends. Rates depend on filer status and change each budget; see basics/tax.
5. Check risk: fund size (AUM), credit quality and weighted average maturity (debt funds), concentration and drawdowns (equity funds).
6. Compare against the benchmark in the FMR. For equity funds that is usually KSE-100 or KMI-30. For money market funds it is usually a mix of short-term government yields and deposit rates.

## Example
Example (made-up numbers): equity fund A shows 1-year +40% and has a 3% front load. Fund B shows +37% with no load.
- On Rs 100,000, A gives 97,000 × 1.40 = Rs 135,800.
- B gives Rs 137,000. B wins despite the lower headline number.

## Where data comes from
- **MUFAP** publishes daily NAVs, offer/redemption prices and returns for all funds, plus monthly industry AUM (see funds/mufap). Igent reads these with query_navs.
- The AMC's monthly **FMR** lists holdings, TER, asset allocation, risk profile and benchmark.

## Pitfalls
- Annualised money market yields are not guaranteed. They fall soon after policy rate cuts.
- Sudden NAV jumps in income funds can come from one-off reversals of provisions or bond revaluations, which won't repeat.
- New funds' "since inception" returns over short periods are misleading.
