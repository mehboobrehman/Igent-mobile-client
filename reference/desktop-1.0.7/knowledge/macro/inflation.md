---
title: Inflation in Pakistan (CPI, SPI, Core)
kind: macro
tags: [inflation, cpi, spi, core-inflation, pbs, real-rates]
updated: 2026-10-10
sources:
  - https://www.pbs.gov.pk
  - https://www.sbp.org.pk
---

# Inflation in Pakistan

Live CPI readings come from query_rates. Commentary and the latest PBS releases come from search_news / read_news.

## Measures (published by Pakistan Bureau of Statistics, PBS)
- **CPI (Consumer Price Index):** the headline measure. Separate urban and rural indices are combined into the national CPI. PBS rebases it periodically; check the base year in the latest release. Released monthly, usually on the first working day of the following month.
- **SPI (Sensitive Price Indicator):** a weekly index of essential items (food, fuel, utilities) across major cities, usually released on Fridays. It is volatile but a useful early signal for food and energy pressure.
- **WPI (Wholesale Price Index):** producer and wholesale prices. It often leads CPI.
- **Core inflation:** PBS publishes non-food non-energy (NFNE) and trimmed-mean core for urban and rural. SBP watches core to judge underlying demand pressure.

SBP sets a medium-term CPI inflation target range. Check sbp.org.pk for the current target.

## How to read the numbers
- **Year-on-year (y/y):** price level versus the same month last year. This is the headline figure.
- **Month-on-month (m/m):** change from last month. Shows current momentum.
- **Period average:** e.g. the fiscal-year average (July-June). Press headlines often mix this up with the latest monthly y/y figure.
- **Base effects:** y/y figures swing when last year's month was unusually low or high, even if prices are calm now.
- Pakistan has seen both very high inflation (e.g. the 2023 crisis) and fast disinflation. Do not assume any level persists.

## Drivers to watch
- Global oil prices and the PKR (fuel, transport, electricity via fuel cost adjustments).
- Administered prices: electricity and gas tariff revisions (often IMF-linked) and periodic petrol price setting.
- Food: wheat, sugar, perishables; floods or crop shocks.
- Budget taxes (GST changes, petroleum levy).

## Real interest rates
- Real rate = nominal rate minus inflation (current or expected).
- Example (made-up numbers): policy rate 12%, CPI 8% y/y, so the real policy rate is about +4%. A saver earning 12% before a 15% tax gets 10.2% after tax, or about +2.2% real.
- Clearly positive real rates favour fixed income and the PKR. Negative real rates push savers toward real assets (equities, property, gold, USD).
- Pull the live policy rate and CPI with query_rates before computing this.

## Pitfalls
- Confusing the fiscal-year average with the latest monthly y/y figure.
- Reading one SPI week as a trend.
- Ignoring the gap between headline and core when forecasting SBP moves.
