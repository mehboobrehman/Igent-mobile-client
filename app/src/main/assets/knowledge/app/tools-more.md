---
title: More Tools (Stock Screener, Fair Value, Trade Cost, Pay Debt or Invest, Rebalance, Dividend Income)
kind: app
tags: [igent, tools, screener, fair-value, valuation, trade-cost, break-even, brokerage, debt, rebalance, dividend, income, how-to]
updated: 2026-10-14
sources: []
---

# More Tools

Six tools on the **Tools** page. Each sits in the group where it fits. All use Igent's stored data and your My Money entries. They are analysis, not advice. The rates you type (commission, tax) are examples: use your own.

## Decide group

### Stock Screener
Pick what to look for, then **Find**: biggest fallers or risers (over a number of days), lowest P/E, highest dividend yield, near the 52-week low or high, or volume spikes. Filters: only KSE-100, KMI-30 or Shariah (KMI All Share) shares, a sector word (for example bank), a **Min Avg Volume** to skip thin shares, and how many to show (up to 30).
- The table shows close, change, volume against its 30-day average, distance from the 52-week low and high, P/E and dividend yield. A **Shariah** badge marks KMI All Share names.
- **Value** opens Fair Value Check and **Buy?** opens Should I Buy? for that share.
- P/E and dividend rankings only include companies whose results or dividends Igent has read (watchlist, holdings, companies looked up before). A screen only finds names to study. The agent uses the same screen (screen_stocks).

### Fair Value Check
Type a PSX symbol, then **Check**. Two simple tests give a rough range:
- **Earnings value**: profit per share divided by the yearly return you want. It assumes no growth, so it is a floor.
- **Dividend growth value**: next dividend divided by (return you want minus growth).
- **Return You Want** is empty by default: the 3-month T-bill plus 5 points. **Dividend Growth** is empty by default: past profit growth, capped at 8%. Type your own to change either.
- The middle value is compared with the price: **cheap** (price more than 15% below), **about fair**, or **expensive** (more than 15% above). It also shows P/E, profit yield against the T-bill and the notes. It reads the company page first if Igent's copy is old.
- Without results Igent cannot value it; a loss-making company gets no earnings value. It is a rough guide: use it with **Should I Buy?**. The agent can call it (fair_value).

### Trade Cost and Break-Even
Type the buy price and number of shares; optionally a sell price. Set the broker commission, other charges (CDC, exchange, taxes on the trade) and capital gains tax (from My Money if you saved one).
- It shows what you pay, the **break-even sell price** (what you must sell at to get your money back after selling costs) and, with a sell price, costs both ways, the gain after costs, the tax and what you keep.
- Tax is on the gain after costs and zero on a loss. It is an estimate. Check rates with your broker and the current rules. The calculate tool has the same op (trade_cost).

## Plan Ahead group

### Pay Debt or Invest?
Type the money you have, the debt's yearly cost, the return you hope for, the tax on it and the years. Debt cost starts from your Safety Net.
- Paying a debt off saves its cost for certain. Investing is a guess. Investing must beat the debt by more than 3 points a year to win; otherwise pay the debt first. A close call is marked.
- It shows what each choice is worth after the years. If your safety money is under 3 months it warns you to build that first. The calculate tool has the same op (debt_vs_invest).

### Rebalance Helper
Set a target for **Safe** (cash, savings, money market and income funds), **Growth funds**, **Shares** and **Gold**. The four must add up to 100%. The **Cautious**, **Balanced** and **Growth** buttons are example mixes. If your Safety Net has your age and retirement age, the start mix follows the years left.
- The table shows each part now, its target and what to buy or sell. A part within 5 points of its target is "on target".
- Type **New Money to Add** to see how to split it over the short parts without selling. Selling can cost brokerage and tax, so new money goes first. The agent can call it (rebalance_check).

### Dividend Income
Shows the cash dividends your held shares paid over the last 12 months, per share and in total, before tax. Type your tax rate to see after tax and per month.
- It repeats the past. Companies can cut or skip dividends. A share with no dividend found may have paid none, or Igent has not read its notices yet.
- Funds' payouts are not included. The agent can call it (dividend_income).
