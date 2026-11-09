---
title: Buying T-bills, PIBs and government Sukuk yourself
kind: savings
tags: [t-bills, mtb, pib, ijara-sukuk, ips-account, investpak, non-competitive-bid, gds, psx, yield, interest-rate-risk]
updated: 2026-10-10
sources:
  - https://www.sbp.org.pk/assets/document/IGGS-PIB.pdf
  - https://www.sbp.org.pk/dmmd/Guidelines/MTB.pdf
  - https://www.psx.com.pk/psx/product-and-services/products/government-debt-securities-primary-market-auction
  - https://propakistani.pk/2026/07/06/pakistan-launches-investpak-to-reduce-govt-reliance-on-banks/
  - https://profit.pakistantoday.com.pk/2024/11/27/new-opportunity-for-individual-investors-to-access-government-debt-securities
---

# Buying T-bills, PIBs and government Sukuk yourself

Government debt is the base of the "safe rate" (see savings/safe-rates). This note explains how an individual can hold it directly, and when a fund is easier. National Savings is covered in savings/national-savings; bank deposits in savings/bank-deposits.

This is general education, not personal financial, tax or legal advice. Rules change.

## The three main instruments

| Instrument | What it is | How you earn |
|---|---|---|
| **MTB (Market Treasury Bill, T-bill)** | Short-term federal government debt. Common tenors are 3, 6 and 12 months; SBP also auctions a 1-month bill. | Bought at a **discount** to face value. You get full face value at maturity. No coupon. |
| **PIB (Pakistan Investment Bond)** | Long-term federal government bond (several years up to 10+ years). | **Fixed-rate PIBs** pay a fixed coupon on face value, semi-annually. **Floating-rate PIBs** pay a coupon that resets with a market benchmark, quarterly or semi-annually depending on the issue. |
| **GoP Ijara Sukuk** | Shariah-compliant government certificates backed by government assets, in fixed-rental and variable-rental versions. | Periodic **rental** payments instead of interest, and face value at maturity. |

All are issued for the Government of Pakistan. Repayment of face value and coupons is guaranteed by the government. They are scripless: you hold them as entries in an account, not paper.

## Primary auctions and the calendar
- SBP runs auctions on behalf of the Ministry of Finance. It publishes an **auction calendar** (a schedule of auction dates and target amounts) ahead of each period.
- Only **Primary Dealers** (banks, DFIs and some other institutions that SBP designates each financial year) bid directly. Everyone else goes through them.
- After each auction SBP publishes the **cut-off yield** for each tenor (or cut-off rental rate/price for sukuk). Some auctions see bids rejected, meaning the government borrowed nothing in that tenor.
- Igent reads recent T-bill and PIB cut-off yields with query_rates. Never quote yields from memory.

## How an individual can buy

### 1. Through a bank: the IPS account
- An **IPS (Investor Portfolio Securities) account** is a custodial account at a bank where your government securities are held. SBP is the ultimate custodian, but you are the legal owner.
- Anyone with a PKR account at a Primary Dealer or a scheduled bank offering the service can open one. Ask the branch or the bank's treasury desk.
- Banks charge fees for IPS accounts as per their **schedule of charges**. Check them; on small amounts fees can eat a noticeable part of the yield.

### 2. Non-competitive bid in the auction
- You tell your bank to place a **non-competitive bid**: you state the amount only, not a price.
- If the auction goes ahead, you get the **weighted-average yield** set in that auction for that tenor.
- SBP sets a per-investor size limit linked to the auction target, and bids can be scaled down if the auction is oversubscribed. Only one bid per tenor per investor.
- A **competitive bid** names a yield and may be rejected if it is above the cut-off. That is mainly for large investors.

### 3. Secondary market through your bank
- You can buy or sell existing T-bills, PIBs and sukuk through your bank at its quoted price.
- Compare the bank's quote with SBP's published secondary market yields. The spread is your hidden cost.

### 4. InvestPak (SBP's online portal)
- SBP has launched **InvestPak**, a digital portal for individuals and companies to invest in government securities.
- Reported features: register with your PKR bank account, request an IPS account through your bank if you do not have one, bid in primary auctions, and get quotes from banks to buy or sell in the secondary market.
- Features and eligibility may change. Check sbp.org.pk for the current process.

### 5. PSX and CDC (capital market route)
- The government amended its rules so government debt securities (GDS) can also be issued, registered and traded through **PSX, NCCPL and CDC**. PSX has hosted GDS auctions, and some GoP Ijara Sukuk and other GDS are listed.
- SECP has said individuals can invest in GDS, including sukuk, through this route. In practice you need a broker and a CDC account (see guides/stocks-step-by-step).
- Which issues are listed, and how liquid they are, varies. Ask your broker before relying on it.
- A listed government-securities ETF is another PSX route (see funds/etfs).

### 6. The easy option: funds
Money market, sovereign income and income funds hold the same paper for you, with daily liquidity and no IPS account. You pay a management fee. See funds/fund-types and guides/mutual-funds-step-by-step.

## Yield, coupon and price

**T-bill discount pricing.** You pay less than face value and get face value back.
Price ≈ Face ÷ (1 + yield × days ÷ 365)

Example (made-up numbers): a 365-day T-bill with face value Rs 100,000 at a 12% yield costs about 100,000 ÷ 1.12 = **Rs 89,286**. Profit at maturity is Rs 10,714.

**Coupon vs yield.** A PIB's coupon is fixed on face value. Its **yield** is the return you actually get at the price you pay. Buy below face value and your yield is above the coupon; buy above face value and it is below.

**Price and yield move in opposite directions.** If market rates rise after you buy a fixed PIB, new bonds pay more, so your bond's price falls. If rates fall, its price rises. Longer bonds move more. This is **interest-rate risk**.

Example (made-up numbers): you hold a fixed PIB with a 12% coupon. Market yields jump to 14%. Its price may fall several percent. If you sell now, you lose. If you hold to maturity, you still get every coupon and full face value.

- **Hold to maturity:** price swings do not matter; your return is the yield you bought at.
- **Sell early:** you get the market price, which can be above or below what you paid.
- **Floating PIBs and short T-bills** have little price risk because their rate resets or they mature soon.
- **Reinvestment risk:** when a T-bill matures you reinvest at whatever the rate is then.

## Tax (concept)
- Profit on T-bills, PIB coupons, sukuk rentals and capital gains on sale are taxable.
- Tax is usually **withheld** at source (for T-bills, typically at maturity). Rates differ for filers and non-filers and change with each Finance Act.
- Use the user's filer status and current rates. See basics/tax.

## Naya Pakistan Certificates (overseas route)
Naya Pakistan Certificates are Government of Pakistan certificates sold to non-resident Pakistanis (and some other eligible investors) mainly through the Roshan Digital Account, in foreign currency and PKR, with conventional and Islamic versions. Rates, tenors, eligibility and tax treatment are set by the government and change. See guides/overseas-pakistanis.

## Comparison (no rates)

| Feature | Direct T-bills (IPS / InvestPak) | Money market fund | NSS certificate | Bank term deposit |
|---|---|---|---|---|
| Credit risk | Government | Mostly government and bank paper | Government | Bank (DPC cover up to a limit) |
| Liquidity | Hold to maturity or sell via bank quote | Redeem any working day | Early encashment can cut profit | Breaking early usually costs profit |
| Minimum | Set by instrument and bank | Usually small | Small | Set by bank |
| Tax | Withholding on profit; filer/non-filer | Fund tax rules (dividend/CGT) | NSS profit rules; some schemes tax-free for eligible people | Bank profit rules |
| Effort | IPS account, bid each time, fees | Very low | Low to medium | Low |
| Price risk | Small for T-bills; real for fixed PIBs if sold early | Small NAV moves | None | None |

Get current yields for each column with query_rates and query_navs, then compare **after tax** as in savings/safe-rates.

## Where to see live results
- **SBP website (sbp.org.pk):** auction calendar, auction results and cut-off yields, secondary market yields, and the investor guides for MTBs, PIBs and sukuk.
- In Igent: query_rates for the latest yields; search_news for auction headlines.

## Pitfalls
- Ignoring IPS fees and bank bid-offer spreads on small amounts.
- Buying a long fixed PIB with money you may need soon.
