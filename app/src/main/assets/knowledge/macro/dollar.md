---
title: PKR vs US Dollar
kind: macro
tags: [pkr, usd, exchange-rate, interbank, open-market, reserves]
updated: 2026-10-10
sources:
  - https://www.sbp.org.pk
  - https://www.brecorder.com/trends/interbank-closing-rates-for-dollar
---

# PKR vs US Dollar

Live interbank and open-market rates and SBP reserves come from query_rates. News on flows and policy comes from search_news / read_news.

## Two markets
- **Interbank:** banks trade with each other and with importers and exporters. SBP publishes a daily weighted-average closing/revaluation rate after market close. This is the "official" rate used for trade and for valuing foreign-currency assets.
- **Open market:** licensed exchange companies sell cash dollars to individuals (travel, remittances, savings). Their buy/sell quotes are usually slightly above interbank.
- A **wide gap** (more than about 1-2%) between open market and interbank is a stress signal. It suggests dollar shortages, administrative controls or grey-market (hawala/hundi) demand. In past crises the gap widened sharply until the authorities cracked down on exchange companies and smuggling.

## History in brief (patterns, not levels)
- The PKR has gone through long periods of tight management followed by sharp step devaluations, for example in 2018-19 and 2022-23.
- After the 2023 crisis, an administrative crackdown and an IMF programme brought a long period of stability. Stability can end quickly when reserves fall or external financing dries up.

## What moves the PKR
- **Current account:** oil prices (Pakistan is a large oil importer), import demand, exports (textiles, IT) and **workers' remittances** (a key support).
- **Financial flows:** IMF disbursements, Eurobonds, bilateral rollovers (e.g. China, Saudi Arabia, UAE), FDI and debt repayments.
- **SBP reserves and intervention:** SBP may buy dollars in the interbank market to build reserves, which limits appreciation. Reserves are usually judged in months of import cover.
- **Interest rate differential:** positive real rates attract portfolio inflows into T-bills.
- **Politics and security:** instability raises dollar demand and hoarding.
- **IMF stance:** IMF programmes require a market-determined exchange rate. Artificial pegging has tended to end in sharp adjustments.

## How to use in analysis
- Exporters (textiles, IT, some chemicals) gain from PKR weakness. Importers and companies with USD debt (OMCs, autos, some power, airlines) lose.
- E&P revenues are USD-linked, so they benefit from a weaker PKR.
- A stable PKR with falling inflation weakens the case for holding cash dollars as a hedge, but dollar savings remain a diversifier against crises.
- Track the open-market premium and SBP reserves weekly. SBP releases reserves data weekly.
- Example (made-up numbers): a rupee fund returns 12% in a year while the PKR falls 5% against the USD. In dollar terms the return is about 1.12 / 1.05 - 1 = 6.7%.

## Pitfalls
- Assuming a period of stability is permanent.
- Comparing rupee returns with dollar returns without adjusting for expected depreciation.
- Quoting an old exchange rate. Always pull the live rate with query_rates.
