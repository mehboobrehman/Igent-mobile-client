---
id: i03-orders-and-costs
level: intermediate
order: 3
title: Orders, Prices and Trading Costs
minutes: 10
summary: Read a bid/ask quote, place a sensible limit order, and work out what a round trip of buying and selling really costs you.
practice: [trading-cost]
notes: [basics/orders, basics/accounts, basics/psx, basics/tax]
---

## The Big Idea

A share does not have one price. There is a price buyers will pay and a price sellers will accept, and your order type decides which one you get. Every trade also has costs: commission, taxes and fees. They look tiny on one trade, but trade often and they quietly eat a big part of your return.

## Explain It Like I'm New

### Bid, ask and spread

Think of the Anarkali bazaar. A shopkeeper buys a bag at one price and sells it at a slightly higher one. Shares work the same way.

| Term | Meaning |
|---|---|
| **Bid** | The highest price a buyer is offering right now. |
| **Ask** (or offer) | The lowest price a seller is accepting right now. |
| **Spread** | The gap between the two. |
| **Last price** | The price of the most recent trade. |

Buy immediately and you usually pay the ask; sell immediately and you get the bid. The spread is tiny in busy stocks but can be several percent in quiet ones, a hidden cost.

The **order book** shows how many shares wait at each price. Thin books mean big orders move the price.

### Market orders and limit orders

| Order type | What it does | Good for |
|---|---|---|
| **Market order** | Buys or sells right away at the best prices available. | Rarely, and only in very liquid stocks. |
| **Limit order** | Buys or sells only at your price or better. If no one matches, it waits. | Almost always, especially for beginners. |

A market order says "I'll take whatever price there is", which in a thin stock can mean a nasty surprise. A limit order says "Not a rupee more than this." You may miss a trade, but you never overpay by accident.

Avoid **pre-open** orders until you understand them. They are matched at a single opening price and cannot be cancelled or changed until the matching ends.

### Lots and quantity

A **lot** is the minimum number of shares you can trade in one go. PSX has moved to very small lots in the regular market in recent years, which makes it easy to start small. Some products, such as certain ETFs, may still use bigger lots. Your broker's app shows the lot size for each symbol.

### Circuit breakers

A **circuit breaker** limits how far a share's price can move from the previous day's close in one session. When a stock hits the top of its band it is "upper locked"; at the bottom, "lower locked". A locked stock may have no sellers or no buyers, so you might not be able to get in or out that day. There is also a market-wide halt if the overall market moves too far too fast. PSX sets and changes these limits, so check PSX for the current rules.

### What a trade really costs

Every buy and every sell has charges. The exact amounts differ by broker and change over time, so always read your broker's fee schedule.

| Charge | How it works |
|---|---|
| **Brokerage commission** | The broker's fee, usually a % of trade value or a per-share amount, with a minimum. |
| **Sales tax on commission** | A provincial tax charged on the commission. |
| **CDC, PSX, SECP and NCCPL fees** | Small fees per trade. |
| **Capital gains tax (CGT)** | Tax on profit when you sell, worked out and collected by NCCPL. Rates depend on filer status and the law at the time. |

After each trade your broker sends a **contract note**, a receipt listing every charge. Add up the buy and sell charges to get your **round-trip cost**. For small retail accounts this can easily come to around 1% of the trade value or more, before CGT.

### Why frequent trading eats returns

Suppose a full round trip costs you 1% (an example figure). Trade the same money in and out 20 times a year and you have paid about 20% of it in costs. To just break even, your trades must earn that 20% back first. A money market fund earns its return with almost no effort. Most frequent traders end up with less than a patient investor.

## A Pakistani Example

Bilal wants 100 shares of a large company, say Engro Fertilizers. The screen shows (example numbers only):

- Bid Rs 199.50, Ask Rs 200.00, last price Rs 199.80.

He places a **limit buy** at Rs 200.00 for 100 shares. Trade value: 100 × 200 = Rs 20,000.

Let's say his total charges are 0.5% each way (an illustrative figure; his broker's rates will differ).

| Step | Amount |
|---|---|
| Buy value | Rs 20,000 |
| Buy charges (0.5%) | Rs 100 |
| Sell value a month later at Rs 204 | Rs 20,400 |
| Sell charges (0.5%) | Rs 102 |
| Profit before costs | Rs 400 |
| Profit after costs | Rs 400 − 100 − 102 = **Rs 198** |

Half of his 2% price gain went to costs, before any CGT. Now he knows why his cousin, who flips shares every few days, is always busy but rarely ahead.

## Try It

Open the **trading-cost** calculator below. Enter a Rs 20,000 trade, an example cost rate, and then try 2, 10 and 30 round trips a year to see how costs pile up. Then, in Igent's Practice tab, buy 100 shares of a KSE-100 company with a **limit** price just below the current ask and see whether it fills, noting the estimated costs Igent shows.

## Common Mistakes

- Using market orders in thin stocks and getting filled far from the last price.
- Ignoring the spread. A wide spread is a cost you pay on the way in and out.
- Making many tiny trades that hit minimum commissions.
- Chasing a stock that is upper locked and then being unable to sell when it reverses.

## Key Takeaways

- You buy at the ask and sell at the bid. The spread is a hidden cost.
- Limit orders protect you from bad prices. Use them by default.
- Circuit breakers can lock a stock for the day. Check PSX for current limits.
- Add up every charge on your contract notes to know your round-trip cost.
- The more often you trade, the higher the hurdle your returns must clear.

```quiz
[{"q":"A stock shows Bid Rs 99.50 and Ask Rs 100.00. If you buy straight away, what price do you usually pay?","options":["Rs 99.50","Rs 99.75","Rs 100.00","Whatever the last trade was"],"answer":2,"why":"An immediate buy matches the lowest seller, which is the ask price of Rs 100.00."},{"q":"Which order type is the safer default for a beginner?","options":["Market order","Limit order","Pre-open order","Any order, they are all the same"],"answer":1,"why":"A limit order only fills at your price or better, so you never overpay by accident."},{"q":"You buy shares worth Rs 50,000. Charges are 0.6% when buying and 0.6% when selling (example rates), and you sell for Rs 50,000. What are your total costs?","options":["Rs 300","Rs 600","Rs 1,200","Rs 6,000"],"answer":1,"why":"0.6% of 50,000 is Rs 300 per side. Buying plus selling = 300 + 300 = Rs 600."},{"q":"Your friend says he trades the same money in and out every week to make quick profits. What is the main hidden problem?","options":["PSX does not allow weekly trades","Costs on every round trip add up and can eat most of the gains","He will receive too many dividends","His shares will expire"],"answer":1,"why":"Each round trip has commission, fees and spread, plus CGT on gains. Frequent trading needs big gains just to break even."}]
```
