---
title: How to judge a REIT on PSX
kind: analysis
tags: [reit, real-estate, nav, discount, distribution-yield, rental-reit, developmental-reit, hybrid-reit, valuation, checklist]
updated: 2026-10-10
sources:
  - https://khalidzafar.com/wp-content/files_mf/1750508368RealEstateInvestmentTrustRegulations2022.pdf
  - https://khalidzafar.com/?p=2736
  - https://dps.psx.com.pk/company/DCR
  - https://www.arifhabibdolmenreit.com/wp-content/uploads/2026/02/Company-Profile-19-2-2026.pdf
---

# How to judge a REIT on PSX

A method for deciding if a listed REIT is a real opportunity. Pull live prices with query_prices and results with query_announcements. General education only, not personal investment advice. Check current rules and data.

For the basic idea of REITs versus owning property, see personal/property-and-alternatives. For general stock ratios, see analysis/stock-valuation.

## 1. What a REIT is

SECP regulates REITs under the **Real Estate Investment Trust Regulations, 2022** (S.R.O. 2067(I)/2022). Check legal/regulators-and-laws for the wider framework.

- A **REIT Scheme** is a **closed-end trust fund** that holds real estate projects. You buy units, not shares of a company.
- Closed-end means you cannot redeem units at NAV with the manager. Listed units trade on PSX, so you sell to another investor at the market price.
- The Regulations require REIT Schemes to list on a stock exchange within a set period. Check the current text for the deadline.

**Who does what**

| Party | Role |
|---|---|
| REIT Management Company (RMC) | A public company licensed by SECP as a non-banking finance company to manage REITs. Runs the scheme, sets distribution and borrowing policy, appoints the valuer. |
| Trustee | Holds title to the REIT assets for unit holders. Checks that the RMC follows the Regulations and the trust deed. Keeps scheme money in highly rated banks. |
| Valuer | Independently values the property to set NAV. |

## 2. Three types of REIT scheme

The 2022 Regulations define:

- **Rental REIT:** invests in property to earn **rental income**. Income is steady if the buildings stay let.
- **Developmental REIT:** invests in projects to **develop, build, refurbish or rehabilitate** property. Income comes when units or projects are sold. It is lumpy. There may be little or no regular dividend for years.
- **Hybrid REIT:** a mix of different REIT types. Read the offering document to see how much is rental and how much is development.

Do not compare a developmental REIT's yield with a rental REIT's yield. They are different products. Example: companies/TPLRF1 (developmental) vs companies/DCR (rental).

## 3. Payout rules

- The Regulations require the RMC to set a **distribution policy** in unit holders' interest. It must fit the tax law and be stated in the offering document.
- Tax law links a REIT's tax exemption to paying out a **minimum share of its income**. So rental REITs usually distribute most of their profit. Check the current rule; do not assume a fixed figure.
- The payout comes from income other than capital gains. **Revaluation gains are not cash.** A big profit from revaluation does not mean a big dividend.
- Tax on REIT dividends is withheld at source and can differ from ordinary dividends. See basics/tax. Do not quote a rate from memory.

## 4. Price vs NAV

**NAV per unit** = (property value + cash + other assets - liabilities) ÷ units.

**Discount** = (NAV per unit - market price) ÷ NAV per unit.

Made-up example: NAV Rs 30, price Rs 21. Discount = 9 ÷ 30 = 30%.

**How NAV is set**
- Under the 2022 Regulations, a valuer values the property at least once a year. For listed, developmental and hybrid REITs it is done half-yearly.
- The valuer must be independent and follow international valuation standards (IVSC) as far as possible.
- A NAV jump from revaluation is an opinion of value, not cash received.

**Why PSX REITs often trade at a discount**
- You cannot redeem at NAV, so nothing forces price up to NAV.
- Low free float and thin trading.
- Doubt about the valuation, or about selling the property at that value.
- High safe rates make a REIT yield look less attractive.
- Fees and related-party dealings with sponsors.

A discount is only an opportunity if the cash income is solid. A permanent discount is common.

## 5. Distribution yield vs the safe rate

**Distribution yield** = last 12 months' distributions per unit ÷ current price.

- Use the yield on **market price**, not on face value. Some reports quote yield on face value (par), which looks higher.
- Use after-tax figures. Compare with the after-tax safe rate (savings/safe-rates).
- A rental REIT has property risk and price risk. It should offer a clear margin over the safe rate, or real growth in rent.
- One-off payouts (for example from a property sale) inflate the yield. Strip them out.

## 6. Quality of rental income

- **Occupancy:** share of leasable area that is let. Falling occupancy cuts income quickly.
- **Tenant mix:** retail, offices, anchors, food. A mix spreads risk.
- **Concentration:** how much rent comes from the top tenants, one building, or one city.
- **Lease length:** long leases give stable income. Short leases mean frequent renewals and risk.
- **Rent escalation:** leases with yearly rent increases help against inflation (macro/inflation). Check if the RMC discloses escalation terms.
- **Operating costs:** maintenance, security, utilities, insurance, management fees. Watch net income, not just gross rent.

## 7. Debt and interest rates

- Read the borrowing policy in the offering document and the debt in the accounts. Borrowing needs RMC board approval and trustee consent under the Regulations.
- Debt raises finance cost when rates rise.
- Even with no debt, REITs are **rate-sensitive**. When the policy rate rises, safe rates rise, and investors demand a higher REIT yield, so prices fall. When rates fall, the opposite. See macro/policy-rate and the SBP decision playbook in analysis/event-playbooks.
- Property cycles matter too: weak consumer spending hurts malls; weak corporate hiring hurts offices; a slow property market hurts developmental REIT sales.

## 8. Liquidity

- Many REITs have low free float and low daily volume.
- Bid-ask spreads can be wide. A market order can move the price against you. Use limit orders (basics/orders).
- Exiting a large holding can take days.

## 9. Shariah status

Some REITs describe themselves as Shariah-compliant, with a Shariah advisor. KMI index membership is a separate test and changes over time. Igent reads it live. See shariah/screening.

## 10. Finding listed REITs

PSX groups REITs in the "Real Estate Investment Trust" sector on the PSX Data Portal (dps.psx.com.pk). Check each RMC's website and SECP disclosures. New schemes register often, so do not rely on a fixed list.

## Checklist before an idea card on a REIT

1. **Type:** rental, developmental or hybrid? Read the RMC's description and offering document.
2. **Latest results:** pull with query_announcements. Split rental income from revaluation gains.
3. **Cash payout:** total distributions over the last 12 months. Remove one-offs.
4. **Yield on price:** after tax (basics/tax).
5. **Safe-rate test:** compare with the after-tax safe rate (savings/safe-rates). No clear margin means no card, unless rent growth is strong and visible.
6. **NAV and discount:** latest NAV per unit, date of last valuation, and discount. Is the discount wider or narrower than its usual range?
7. **Income quality:** occupancy trend, tenant concentration, lease length, escalation, costs.
8. **Debt and rates:** borrowing level and where the policy rate cycle is heading (macro/policy-rate).
9. **Liquidity:** average volume and spread. Use limit orders.
10. **Shariah:** read KMI status live if the user needs it (shariah/screening).
11. **Risks:** concentration, security events, sponsor-related dealings, valuation doubts.
12. **Sizing:** REITs are satellite holdings. Apply the limits in portfolio/risk-and-sizing.
13. **State clearly:** what would make the idea wrong (for example a big tenant leaving, or a rate rise).
