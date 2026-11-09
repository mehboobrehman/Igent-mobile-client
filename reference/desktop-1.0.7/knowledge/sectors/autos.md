---
title: Automobile Assemblers & Parts (PSX sector)
kind: sector
tags: [autos, cars, tractors, motorcycles, tariffs, interest-rates]
updated: 2026-10-10
sources:
  - https://www.pama.org.pk
  - https://dps.psx.com.pk
  - https://www.sbp.org.pk
---

# Automobile Assemblers & Parts

Autos are highly cyclical. Demand depends on car financing rates, consumer incomes, the PKR (imported CKD kits) and government tariff and tax policy.

## Example listed companies (tickers)
- **Cars:** e.g. **INDU** (Indus Motor, Toyota), **HCAR** (Honda Atlas Cars), **SAZEW** (Sazgar Engineering), **GHNI** (Ghandhara).
- **Tractors:** e.g. **MTL** (Millat Tractors), **AGTL** (Al-Ghazi).
- **Motorcycles:** e.g. **ATLH** (Atlas Honda).
- **Parts and tyres:** e.g. **THALL** (Thal Ltd), **GTYR** (General Tyre), **EXIDE**.
- Some auto exposure sits inside other stocks. For example, Lucky Cement (LUCK) owns Kia assembly through a subsidiary.
- Companies can delist (for example through a sponsor buyback). Confirm a symbol is still listed with query_prices before analysing it.

## Demand data
- **PAMA** (Pakistan Automotive Manufacturers Association) publishes monthly unit sales for cars, LCVs, tractors, two- and three-wheelers, usually around mid-month.
- Read both month-on-month and year-on-year changes. Single months are noisy (holidays, plant shutdowns, price announcements).
- Igent finds sales news with search_news / read_news and company results with query_announcements.

## Key drivers
- **Interest rates:** a large share of car sales is bank-financed. Rate cuts lift demand with a lag; hikes cut it. Check the live policy rate and KIBOR with query_rates.
- **PKR/USD and JPY:** CKD kits and parts are imported. Depreciation forces price increases, which hurts volumes and margins.
- **Taxes:** GST, federal excise on large engines, withholding tax on registration and provincial token taxes make up a large part of a car's price. Budgets often change them, including concessions for EVs and hybrids.
- **Tariff policy (structural risk):** local assemblers are protected by customs and regulatory duties on imported new and used cars. Government policy has been moving toward lower protection over time. Cheaper or easier used-car imports compete directly with local assemblers.
- **Auto policy:** multi-year auto development policies set incentives and rules. Gaps or uncertainty between policies can hold back launches and sales.
- **Farm economy:** tractor sales follow crop prices, farm incomes and any tractor subsidy schemes.

## What to watch
- Monthly PAMA sales and any new auto or NEV policy.
- Used-car import rules and duty SROs.
- Price increase announcements and PKR/JPY moves.
- Quarterly gross margins of the assemblers.

## Pitfalls
- Peak-cycle earnings rarely last. Volumes have collapsed in past cycles after rate hikes and import curbs.
- Assemblers hold large customer advances. Interest income on that cash can be a big share of profit when rates are high and shrinks when rates fall.
- Policy changes (tariffs, used-car rules, taxes) can re-rate the whole sector quickly.
