---
title: Bank Savings and Term Deposits in Pakistan
kind: savings
tags: [banks, deposits, mdr, dpc, islamic-banking, withholding-tax]
updated: 2026-10-10
sources:
  - https://www.sbp.org.pk/bprd/2013/c7.htm
  - https://www.sbp.org.pk/
  - https://dpc.org.pk/
---

# Bank Savings and Term Deposits

## Product types
- **Current account**: no profit.
- **Savings / PLS account**: profit on balance (monthly-average or minimum-balance basis), paid monthly, quarterly or half-yearly depending on the bank. Fully liquid.
- **Term deposit (TDR / certificates)**: fixed tenor (1 month to 5+ years), fixed or floating rate. Breaking early usually costs profit.
- **Islamic banks** (full Islamic banks and Islamic windows of conventional banks): profit-sharing (mudarabah) pools. Rates are "expected" and declared monthly, not guaranteed.

## Minimum Profit Rate rule (conventional banks)
- SBP sets a minimum profit rate on PKR savings deposits at conventional banks, linked to the **SBP floor rate** (the bottom of the interest rate corridor) minus a margin. The rule started with BPRD Circular 07 of 2013.
- When the floor rate changes, the minimum changes from the start of the following month.
- SBP has changed who is covered over time (for example, excluding deposits of financial institutions, public sector enterprises and public limited companies, and limiting coverage by depositor type or balance). Check the latest SBP circular for the current margin and coverage.
- Term deposits are generally excluded; banks price them freely.
- To estimate the current minimum: get the policy rate with query_rates, find the floor rate (policy rate minus the corridor spread, per SBP), then apply the margin in the current SBP rule.

## Islamic banks
SBP requires Islamic banks to pay depositors on PKR savings deposits at least a set share of the weighted-average gross yield of their pools. There is no direct link to the floor rate. Check SBP's Islamic banking circulars for the current requirement.

## Deposit protection
- The Deposit Protection Corporation (DPC, an SBP subsidiary) insures deposits **up to a set amount per depositor per bank**. The limit has been raised over time. Check dpc.org.pk for the current limit.
- Payment happens only if SBP declares a member bank failed.
- Spreading large amounts across banks increases cover. Joint and individual accounts are treated under DPC rules.

## Tax
- Banks withhold tax on profit. The rate is higher for non-filers (often about double). Rates change with each Finance Act; see basics/tax and check current FBR rates or budget news.
- The bank-profit rate has often differed from the rate on National Savings profit, which matters when comparing.

## How to use in analysis
- Bank savings rates track the floor rate with about a one-month lag. Term-deposit rates track KIBOR and T-bill yields (query_rates).
- Money market funds usually beat bank savings rates before tax. After tax, mutual fund treatment differs (see basics/tax).
- Real return = after-tax rate minus inflation (CPI from query_rates).

Example (made-up numbers): savings rate 10%, filer tax 20%, non-filer 40%. Filer nets 8.0%, non-filer nets 6.0%. If CPI inflation is 9%, the filer's real return is about −1% and the non-filer's about −3%.

## Pitfalls
- Advertised "up to" rates often need high minimum balances.
- Islamic expected rates can be revised monthly.
- Balances above the DPC limit at one bank carry uninsured credit risk (small, but real for weaker banks).
