---
id: e05-beat-the-safe-rate
level: expert
order: 5
title: Does This Idea Beat the Safe Rate?
minutes: 12
summary: You will be able to compare an idea's after-tax expected return with the after-tax safe rate, weigh good and bad outcomes, and decide whether the risk is worth it.
practice: [after-tax, compound]
notes: [savings/safe-rates, basics/tax, macro/inflation, analysis/stock-valuation, portfolio/risk-and-sizing]
---

## The Big Idea

Every rupee you put into a risky idea is a rupee you did not put somewhere safe. So the real question is never "will this make money?" It is "will this make **enough more** than the safe option, after tax, to be worth the risk?" If not, the safe option wins.

## Explain It Like I'm New

### The safe rate is your hurdle

The **safe rate** is what you can earn with almost no risk. In Pakistan that means government paper and government-backed savings: T-bills, National Savings certificates, and money market funds for money you may need any time.

Pick the safe option that matches your **horizon** (how long the money will stay invested). For a one-year idea, compare with a 12-month T-bill or a similar certificate. Rates change often, so check today's figures on Igent's **Dashboard** (the Safe Rates card) or ask in **Chat**. Do not use numbers you remember from last year.

### Always compare after tax

Different products are taxed differently, and non-filers usually pay more. Comparing a pre-tax stock return with a pre-tax certificate rate is comparing mangoes with oranges.

**After-tax return = pre-tax return x (1 - tax rate)**

Example: a 12% safe rate taxed at an assumed 15% gives 12 x 0.85 = **10.2%** after tax. Look up the real rates for your filer status from the FBR rate card or Igent's Chat before deciding.

### Risk premium: getting paid for the bumps

A **risk premium** is the extra return you demand for taking risk. If a stock is only expected to match the safe rate, why accept the chance of a 30% fall? A common rule of thumb is to want several percentage points more than the safe rate for equities, and more for small, illiquid or jumpy stocks. Decide your own minimum and write it down.

### Probability-weighted outcomes, in simple words

The future is not one number. It is a few possible stories. List them, give each a rough chance, and average them.

| Story | Chance | Return after tax and costs |
|---|---|---|
| Good | 30% | +30% |
| Normal | 50% | +10% |
| Bad | 20% | -25% |

**Expected return** = (0.30 x 30) + (0.50 x 10) + (0.20 x -25) = 9 + 5 - 5 = **9%**.

Your chances will never be exact. That is fine. The point is to force yourself to think about the bad story, not just the exciting one. Also look at the bad story on its own: can you live with it? That ties back to position sizing.

### Opportunity cost and compounding

**Opportunity cost** is what you give up by choosing one option over another. A small yearly gap grows big over time because of **compounding** (earning returns on your returns).

| Rs 100,000 for 10 years at (examples) | Ends at about |
|---|---|
| 10.2% a year | Rs 264,000 |
| 12% a year | Rs 311,000 |
| 15% a year | Rs 405,000 |

But the higher numbers are only *expected*. The safe number is close to certain. That is exactly why you need a risk premium.

### Do not forget inflation

**Real return** = after-tax return minus inflation. If inflation is 10% and your safe option gives 10.2% after tax, your buying power barely grows. A risky idea should aim to beat inflation clearly too. Current inflation is on the Dashboard's CPI tile.

### The hurdle worksheet

```text
Horizon: ___   Matching safe option: ______
Safe rate ___% x (1 - tax ___%) = after-tax safe ___%
My risk premium for this idea: ___ points
Hurdle = after-tax safe + premium = ___%

Stories (after tax and costs):
Good ___% chance x ___%  +  Normal ___% x ___%  +  Bad ___% x ___%
Expected return = ___%
Beats hurdle?  yes / no     Can I live with the bad story?  yes / no
```

## A Pakistani Example

*All numbers are made-up round figures for teaching, not real data or advice.*

Nadia is a filer with Rs 300,000 she will not need for at least a year. Her safe option in this example is a 12-month T-bill at 12%. With an assumed 15% tax, that is **10.2% after tax**. She wants a risk premium of 5 points for a large PSX stock, so her **hurdle is 15.2%**.

She looks at **Oil & Gas Development Company (OGDC)**. Example figures: price Rs 150, expected dividend Rs 15 (a 10% yield).

**First look, at Rs 150:**

| Story | Chance | After tax and costs |
|---|---|---|
| Good: oil prices firm, dividend raised | 30% | +30% |
| Normal: steady dividend, small price gain | 50% | +12% |
| Bad: oil slumps, payments to the company delayed | 20% | -25% |

Expected = 9 + 6 - 5 = **10%**. That is *below* her 10.2% safe rate, and far below her 15.2% hurdle. Taking risk to earn less than the safe option makes no sense. She does not buy.

**Second look, after the price falls to Rs 125 on market-wide news** (her thesis is unchanged):

The same dividend is now a 12% yield, and every story improves:

| Story | Chance | After tax and costs |
|---|---|---|
| Good | 30% | +42% |
| Normal | 50% | +18% |
| Bad | 20% | -15% |

Expected = 12.6 + 9 - 3 = **18.6%**. That clears her 15.2% hurdle by 3.4 points. The bad story is a 15% loss, which she can absorb with sensible sizing. She buys a small position and keeps the rest in the T-bill.

Same company, different price, different answer. The price you pay is part of the idea.

## Try It

Use the **after-tax** calculator below to turn a safe rate into its after-tax figure for your filer status. Then use the **compound** calculator to see how the gap between that safe rate and an idea's expected return grows over 10 years. In the **Practice** tab, buy only ideas that pass your hurdle worksheet.

## Common Mistakes

- **Comparing pre-tax with after-tax** numbers, or ignoring your filer status.
- **Using old rates.** Safe rates move. Check the Dashboard or Chat each time.
- **Only imagining the good story.** Always give the bad story a real chance.
- **No risk premium.** Accepting stock risk to barely match a T-bill.
- **Forgetting the price.** A good company can fail the hurdle at a high price.

## Key Takeaways

- Every idea must beat the matching safe rate, after tax, plus a risk premium you chose.
- List good, normal and bad stories with rough chances, and average them.
- Small yearly gaps compound into big differences over time.
- Check real return against inflation.
- The price you pay can turn a bad idea into a good one, or the other way round.

```quiz
[{"q":"A safe option pays 14% a year. Assume it is taxed at 20%. What is the after-tax safe rate?","options":["11.2%","12.0%","14.0%","16.8%"],"answer":0,"why":"14 x (1 - 0.20) = 14 x 0.80 = 11.2%."},{"q":"You estimate an idea has a 50% chance of +20%, a 30% chance of +5% and a 20% chance of -20%, all after tax and costs. What is the probability-weighted expected return?","options":["5.0%","7.5%","10.0%","20.0%"],"answer":1,"why":"(0.5 x 20) + (0.3 x 5) + (0.2 x -20) = 10 + 1.5 - 4 = 7.5%."},{"q":"The after-tax safe rate for your horizon is 10%. A stock's expected after-tax return is 11%, and in a bad year it could fall 30%. What is the most sensible view?","options":["Buy a big position; 11% beats 10%.","Borrow money to buy it because it beats the safe rate.","The 1-point margin is too thin for that risk; wait for a better price or skip it.","Compare it with the pre-tax safe rate instead."],"answer":2,"why":"A 1-point edge is a tiny risk premium for a possible 30% loss. The idea should clear a clear hurdle above the safe rate."},{"q":"A non-filer compares a stock idea with a National Savings certificate using pre-tax numbers for both. What is the main problem?","options":["There is no problem; pre-tax is standard.","Stocks are never taxed.","National Savings certificates are never taxed.","Tax rates differ by product and filer status, so the comparison must be after tax."],"answer":3,"why":"Products are taxed differently and non-filers usually pay higher rates, so only after-tax figures give a fair comparison."}]
```
