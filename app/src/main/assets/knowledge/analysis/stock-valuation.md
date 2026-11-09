---
title: How to judge a PSX stock
kind: analysis
tags: [valuation, ratios, eps, pe, pb, roe, dividend-yield, results, checklist, expected-return]
updated: 2026-10-10
sources:
  - https://dps.psx.com.pk/announcements/companies
---

# How to judge a PSX stock

A practical method for deciding whether a stock is a real opportunity or "cheap for a reason". Pull live prices with query_prices and results with query_announcements. General education only, not personal investment advice. Check current rules and data.

## 1. Read the results first
Listed companies file quarterly and annual results on PSX. They appear as company announcements on the PSX Data Portal (dps.psx.com.pk). They often come with a board meeting notice and any dividend or bonus decision (see analysis/corporate-actions).

What to look at, in order:
- **Revenue (sales):** growth versus the same quarter last year. Split price from volume if the company says.
- **Gross margin** = gross profit / revenue. Shows pricing power and input costs (fuel, raw material, PKR).
- **Finance cost:** interest on debt. Rises fast when rates rise. Compare with operating profit.
- **Other income:** interest on cash, exchange gains, one-off sales. Too much of profit from here is low quality.
- **EPS** (earnings per share), and whether it is quarterly or cumulative for the year so far.
- **One-offs:** asset sales, tax reversals, write-offs, exchange gains, dry wells. Remove them to see "core" EPS.
- **Payout:** dividend per share versus EPS.
- **Tax:** a change in tax rate or super tax can swing EPS without the business changing (see basics/tax).

Compare quarter vs same quarter last year (seasonality), and the year-to-date total. Read the directors' report for reasons.

## 2. Key ratios
| Ratio | Formula | How to read |
|---|---|---|
| EPS | Net profit after tax / shares outstanding | Use core EPS. Adjust past EPS for bonus and rights. |
| P/E | Price / EPS | Trailing uses the last 12 months. Forward uses expected EPS. Low P/E can mean cheap or falling earnings. |
| Book value per share (BVPS) | Shareholders' equity / shares | What the accounts say each share owns. |
| P/B | Price / BVPS | Key for banks and asset-heavy firms. Below 1 means priced under book. Ask why. |
| ROE | Net profit / average equity | Return the business earns on owners' money. Compare with the safe rate. |
| Dividend yield | Dividends per share (last 12 months) / price | Cash return. Check it is sustainable. |
| Payout ratio | Dividends per share / EPS | Very high payout leaves little for growth or bad years. |
| Debt/equity | Total borrowings / equity | High debt hurts when rates rise. |
| Interest cover | Operating profit (EBIT) / finance cost | Low cover (example: under 2x) means profit is fragile. |
| Free cash flow (FCF) | Operating cash flow - capital expenditure | Dividends are paid from cash, not profit. Weak FCF with high profit is a warning. |
| Current ratio | Current assets / current liabilities | Below 1 can mean liquidity strain. |

P/B, ROE and P/E are linked: P/B = P/E x ROE. A high-ROE company deserves a higher P/B.

Example (made-up numbers): price Rs 120, core EPS Rs 15, DPS Rs 9, BVPS Rs 100. P/E = 8x. Dividend yield = 7.5%. Payout = 60%. P/B = 1.2x. ROE about 15%.

## 3. Compare with history and peers
A ratio alone means little.
- **Own history:** is P/E low versus the company's usual range? Was past EPS inflated?
- **Sector peers:** compare like with like.
  - **Banks** (sectors/banks): judge on P/B and ROE. Also NIM (net interest margin = net interest income / earning assets), ADR (advances-to-deposit ratio), CASA share (current and savings deposits / total deposits, cheaper funding), asset quality and capital.
  - **Cyclicals** such as cement, steel, autos (sectors/cement, sectors/autos): use normalised (average-cycle) earnings. P/E looks lowest at peak profits and highest at the bottom.
  - **E&P** (sectors/oil-gas-exploration): reserves life, production trend, oil and PKR sensitivity, and whether receivables let them pay dividends.
  - **Power IPPs** (sectors/power): contract terms and cash collection matter more than P/E.
  - **Fertilizer, OMCs, pharma, tech, textiles:** see sectors/fertilizer, sectors/oil-marketing, sectors/pharmaceuticals, sectors/technology, sectors/textiles.
- **The market:** compare with KSE-100 valuation and with the safe rate (an earnings yield = 1 / P/E).

## 4. Quality checks (red flags)
- **Receivables and circular debt:** trade debts growing faster than sales. Common in power, E&P, OMCs and gas utilities. Profits may never turn into cash.
- **Related-party dealings:** large sales, loans or purchases with group companies or sponsors. Read the related-party note.
- **Audit report:** a qualified opinion, adverse opinion, or "emphasis of matter" on going concern is a serious warning.
- **Free float and liquidity:** low free float and thin daily volume mean wide spreads, easy price moves and hard exits (see basics/orders).
- **Governance:** frequent changes in auditor or CFO, late results, default-counter or non-compliance flags on PSX, sponsor share pledges.
- **Dilution:** repeated rights issues to fund losses.
- **Shariah:** check status if the user needs it (shariah/screening).

## 5. Checklist: cheap for a reason, or real opportunity?
1. Why is it cheap? Write the bear case in one line.
2. Is the problem temporary (a bad quarter, a rate peak, a one-off) or permanent (lost market, broken contract, heavy debt)?
3. Is core EPS stable or rising after removing one-offs?
4. Does FCF support the dividend?
5. Is debt manageable at today's rates (interest cover)?
6. Is the balance sheet clean (receivables, related parties, audit opinion)?
7. Is the valuation low versus its own history and peers, on the right metric for its sector?
8. What is the trigger for rerating (rate cuts, tariff decision, payout increase, debt repayment)?
9. Is it liquid enough for the planned position size?
10. Does the expected return beat the after-tax safe rate by a clear margin (step 6)?

If steps 2, 4 or 6 fail, it is likely a value trap. Prefer a fund (funds/fund-types) or pass.

## 6. Turn it into an expected return
A simple yearly estimate:

**Expected return = dividend yield + earnings growth + rerating effect**

- Dividend yield: use expected dividends, net of dividend tax (basics/tax).
- Earnings growth: a realistic yearly core EPS growth rate.
- Rerating: if P/E moves from current to a fair level over N years, yearly effect is about (fair P/E / current P/E)^(1/N) - 1. Can be negative.

Example (made-up numbers): net dividend yield 6%, EPS growth 8%, P/E rerates from 6x to 7x over 3 years (about +5% a year). Expected return about 19% a year before capital gains tax.

Then compare with the after-tax safe rate (savings/safe-rates). Example (made-up numbers): if the after-tax safe rate is 9%, the stock offers about 10% extra a year for equity risk. Use a lower confidence and smaller size when the gap is thin or the estimate relies mostly on rerating. Always state assumptions in the idea card.

## Pitfalls
- Using reported EPS with one-offs, or not adjusting for bonus shares.
- Judging a cyclical on peak-year P/E.
- Chasing high dividend yield that cash flow cannot support.
- Ignoring liquidity: a cheap stock you cannot sell is not cheap.
- Forgetting tax and costs when comparing with the safe rate.

See also guides/stocks-step-by-step, analysis/event-playbooks, macro/policy-rate.
