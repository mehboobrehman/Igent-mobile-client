---
title: Comparing Ideas Against the Safe Rate
kind: savings
tags: [safe-rate, t-bills, hurdle-rate, after-tax, real-return, comparison]
updated: 2026-10-10
sources:
  - https://www.sbp.org.pk/
  - https://savings.gov.pk/latest-profit-rates/
  - https://www.mufap.com.pk/
  - https://www.pbs.gov.pk/
---

# Comparing an Idea Against the "Safe Rate"

Every investment idea (a stock, an equity fund, property, gold) should be compared with what the investor can earn with almost no risk. In Pakistan the safe options are government paper and government-backed savings. If an idea's expected after-tax return does not clearly beat the safe rate, plus a premium for its risk, it is usually not worth taking.

## Safe-rate candidates and where to get them

| Option | Live source | Notes |
|---|---|---|
| T-bill yields (3, 6, 12 months) | query_rates (SBP auction results) | Best match for a fixed horizon |
| Money market funds | query_navs (MUFAP returns) | Best for money that may be needed any time |
| NSS: STSC, RIC, DSC, etc. | query_rates (CDNS) | Fixed at purchase for certificates |
| NSS: Behbood / PBA | query_rates (CDNS) | Eligible people only; historically tax-free |
| Bank savings minimum rate | query_rates (policy rate) + SBP rule | See savings/bank-deposits |
| SBP policy rate | query_rates | A reference, not a buyable yield |

Tip: when T-bill yields sit above the policy rate, the market expects rates to stay high or rise. When they sit below, it expects cuts.

## Steps
1. **Pick the safe rate that matches the horizon.** For a 1-year idea, use the 12-month T-bill or STSC 12m. For money that may be needed any time, use a money market fund.
2. **Convert to after-tax.** Use the user's filer status and the current withholding rates for that product (bank profit, NSS profit, fund dividends and CGT). Rates change each Finance Act; check basics/tax and recent news.
3. **Estimate the risky idea's after-tax return** (dividend yield + expected price change, minus costs and tax).
4. **Require a risk premium.** For equities a common rule of thumb is several percentage points above the safe rate. Demand more for small, illiquid or volatile stocks.
5. **Check the real return.** Subtract expected inflation (latest CPI from query_rates; SBP's Monetary Policy Statement gives its inflation outlook).

## Worked example
Example (made-up numbers), filer:
- Safe option: a 1-year NSS certificate at 12%. Assume 15% tax. After tax: 12 × 0.85 = **10.2%**.
- Stock idea: price Rs 100, expected dividend Rs 8 (8% yield), expected price Rs 106 in a year.
  - Dividend after an assumed 15% tax: Rs 6.8.
  - Capital gain Rs 6, less an assumed 15% CGT: Rs 5.1.
  - Less trading costs of about Rs 0.3 round trip (assumption).
  - Net about Rs 11.6, or **11.6%**.
- Excess over safe: 11.6 − 10.2 = **1.4 percentage points**. That is thin for equity risk, where a 20–30% drawdown is possible in a bad year. The idea needs a better entry price or higher conviction.
- Real terms: if inflation is 10%, the safe option's real return is only about 0.2%.

## Pitfalls
- Comparing a pre-tax stock return with a pre-tax NSS rate. Tax gaps differ by product and by filer status.
- Using the policy rate instead of a buyable yield.
- Ignoring reinvestment: certificate rates are locked; T-bills and money market funds reprice.
- Ignoring liquidity: early encashment of NSS can wipe out profit.
