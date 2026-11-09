---
title: Shariah screening of PSX stocks (KMI criteria) and dividend purification
kind: shariah
tags: [shariah, kmi, screening, purification, islamic]
updated: 2026-10-10
sources:
  - https://www.psx.com.pk/psx/product-and-services/indices
  - https://www.secp.gov.pk/
---

# Shariah screening (PSX-KMI)

PSX publishes a list of Shariah-compliant companies, which forms the **PSX-KMI All Share Index**. The **KMI-30** is drawn from that list. The screening methodology needs SECP approval under the Shariah Governance Regulations, 2023. SECP approved it in May 2024 and approved a revision in February 2026. It can be revised again, so check PSX and SECP for the current version. The list is reviewed periodically; check PSX for the latest review.

## Step 1: business activity screen
The company's core business must be permissible. This excludes, for example, conventional banking, insurance and leasing (interest-based finance), alcohol, pork, gambling, adult entertainment, and interest-based financial services. Conventional banks are never compliant. Islamic banks can be.

## Step 2: financial ratio screens
A company with a permissible business must also pass ratio tests. The types of test are:

| Test | Idea |
|---|---|
| Interest-bearing debt / total assets | Must be below a maximum. Limits borrowing from conventional lenders. |
| Non-compliant investments / total assets | Must be below a maximum. Limits holdings of interest-bearing or non-compliant assets. |
| Non-compliant income / total revenue | Must be below a small maximum. Covers interest income, gambling or gharar-based income, late-payment penalties, etc. |
| Illiquid assets / total assets | Must be above a minimum. Fixed assets, inventory, stores and spares, WIP. |
| Market price per share vs net liquid assets per share | Price must be at least net liquid assets per share. Stops a share being a disguised claim on cash or receivables. |

The exact % limits are set by the PSX / Al Meezan KMI methodology and have been tightened in the past. Check the current methodology on PSX before quoting a number.

## Other methodology features
Recent versions of the methodology have added features such as a compliance rating for qualifying companies, an objection window when the list is published, and interim inclusion of newly listed companies after screening. Check PSX for current details.

## Purification of dividends
- Even compliant companies may earn a small amount of non-compliant income. Scholars require investors to give that share of their **dividends** to charity (not as Zakat).
- Typical formula: purification amount = dividend received × (non-compliant income ÷ total revenue) of that company. Some Islamic funds publish purification ratios per stock in their FMRs.
- Islamic mutual funds and ETFs purify inside the fund, so the investor normally does nothing extra.
- Capital gains: many scholars do not require purification of capital gains on compliant shares. Views differ, so let the user follow their own scholar.

Example (made-up numbers): you receive a Rs 10,000 dividend, and the company's non-compliant income is 2% of revenue. Purification = 10,000 × 0.02 = Rs 200 given to charity.

## How Igent should use it
- When the user wants halal investments, restrict stock ideas to the current KMI All Share list. Use KMI-30 as the benchmark. Use Islamic funds, sukuk and Islamic bank deposits for fixed income.
- Compliance can change at each review, for example when debt rises. Re-check membership (query_index, query_announcements) and do not cache it indefinitely.
- Do not label a stock compliant on its business alone. Ratios matter.
- Igent is not a Shariah authority. Present PSX/KMI status as fact and leave fiqh views to the user's scholar.
