
export const sqlite = () => ({
  prepare: () => ({
    all: () => [],
    get: () => null,
    run: () => ({ changes: 0 })
  }),
  exec: () => {}
});
export const getRobustSeedStocks = () => [];
export const fundUniverse = () => [];
export const fundsByName = () => new Map();
export const targetFor = (text, symbol) => {
    const re = new RegExp(symbol + "[^\\n]{0,80}?(?:target|tp|tgt)\\s*(?:price)?\\s*(?:rs\\.?|pkr)?\\s*:?\\s*(\\d+(?:\\.\\d+)?)", "i");
    const m = re.exec(text) ?? /(?:target|tp|tgt)\s*(?:price)?\s*(?:rs\.?|pkr)?\s*:?\s*(\d+(?:\.\d+)?)/i.exec(text);
    return m ? Number(m[1]) : null;
};
export const clean = (s) => (s ?? "").replace(/\s+/g, " ").trim();
export const pktToday$2 = () => new Date(Date.now() + 5 * 36e5).toISOString().slice(0, 10);
export const pktMinutes = (d = new Date()) => {
  const utcMin = d.getUTCHours() * 60 + d.getUTCMinutes();
  return (utcMin + 5 * 60) % (24 * 60);
};
export const RULES = [
  {
    re: /advance fee|upfront fee|processing fee|membership fee|registration fee|hidden charges/i,
    flag: "Upfront fee or charges",
    why: "Legitimate investments don't ask for fees before you see any return."
  },
  {
    re: /guaranteed return|fixed return|sure profit|no risk|risk-free/i,
    flag: "Guaranteed or fixed returns",
    why: "All investments carry risk. Anyone promising fixed returns with no risk is lying."
  },
  {
    re: /referral bonus|downline|binary plan|matrix plan|level income/i,
    flag: "Multi-level marketing (MLM) or pyramid scheme",
    why: "MLM schemes primarily make money from recruiting new members, not from selling products. These are often illegal pyramid schemes."
  },
  {
    re: /earn passively|easy money|work from home|no effort/i,
    flag: "Easy money / no effort required",
    why: "Legitimate earnings require work, skill, or capital. Be skeptical of claims that promise high returns for little to no effort."
  },
  {
    re: /unregulated|unregistered|no license|offshore/i,
    flag: "Unregulated or unlicensed entity",
    why: "Invest only with entities regulated by the SECP or SBP. Unregulated entities offer no investor protection."
  },
  {
    re: /secret formula|exclusive access|insider tip/i,
    flag: "Secret or exclusive investment",
    why: "Scammers often create a sense of urgency and exclusivity to pressure you into investing without due diligence."
  },
  {
    re: /bitcoin|crypto|forex|commodities\s+trading\s+bot/i,
    flag: "High-risk, complex product with promised software returns",
    why: "Automated trading bots for highly volatile assets like crypto or forex often make unrealistic promises. Understand the underlying asset and trading strategy thoroughly."
  },
  {
    re: /committee|kameti|bc (scheme|group)|gold (scheme|committee)|plot (scheme|booking) .{0,30}(profit|return)/i,
    flag: "Informal committee or scheme",
    why: "Committees and informal gold or plot schemes have no legal protection if the organiser disappears."
  }
];
export const SECTORS = [
  [/bank/i, "banks"],
  [/oil|gas|petrol/i, "oil-gas-exploration"],
  [/cement/i, "cement"],
  [/tech|it|software/i, "technology"],
  [/fertilizer/i, "fertilizer"],
  [/power|electric/i, "power"],
  [/auto|car|tractor/i, "autos"],
  [/steel/i, "steel"],
  [/refiner/i, "refinery"],
  [/pharma|medicine/i, "pharmaceuticals"],
  [/food|beverage/i, "food"],
  [/textile/i, "textiles"],
  [/sugar/i, "sugar"],
  [/reit/i, "reit"],
  [/telecom/i, "telecom"]
];
export const SECTOR_BETA = { banks: 1, "oil-gas-exploration": 1.1, cement: 1.3, technology: 1.3, fertilizer: 0.8, power: 0.8, "oil-marketing": 1.2, autos: 1.2, steel: 1.3, refinery: 1.4, pharmaceuticals: 0.9, food: 0.8, textiles: 1 };
export const SECTOR_USD = { "oil-gas-exploration": 0.3, technology: 0.4, textiles: 0.3, autos: -0.3, "oil-marketing": -0.2, pharmaceuticals: -0.2, steel: -0.2 };


export function highReturnFlag(text) {
  const re = /(\d+(?:\.\d+)?)\s?%\s*(?:profit|return|munafa|interest)?\s*(?:per|a|an|every|each|\/|har)?\s*(day|daily|week|weekly|month|monthly|mahina|mahine)/gi;
  for (const m of text.matchAll(re)) {
    const pct2 = Number(m[1]);
    const unit = m[2].toLowerCase();
    const monthly = /^(month|monthly|mahina|mahine)$/.test(unit);
    if (monthly && pct2 >= 3 || !monthly && pct2 > 0)
      return {
        flag: `Very high return claimed (${m[0].trim()})`,
        why: monthly ? `${pct2}% a month is about ${Math.round(((1 + pct2 / 100) ** 12 - 1) * 100)}% a year, far above what safe options or even good shares pay. Returns like this usually come from new members' money.` : "Daily or weekly profit figures are a classic sign of a Ponzi or trading scam."
      };
  }
  return null;
}

export function ruleFlags(text) {
  const out = [];
  const high = highReturnFlag(text);
  if (high) out.push(high);
  for (const r of RULES) if (r.re.test(text)) out.push({ flag: r.flag, why: r.why });
  return out;
}

export function ruleVerdict(flags) {
  const strong = new Set(RULES.filter((r) => r.strong).map((r) => r.flag));
  const strongCount = flags.filter((f) => strong.has(f.flag) || f.flag.startsWith("Very high return")).length;
  if (strongCount >= 1 && flags.length >= 2) return "likely_scam";
  if (strongCount >= 1 || flags.length >= 2) return "suspicious";
  return flags.length ? "suspicious" : "low_risk";
}

export function scoreFromExcess(excess) {
  if (excess >= 5) return 2;
  if (excess >= 1) return 1;
  if (excess > -1) return 0;
  if (excess > -5) return -1;
  return -2;
}

export function cardReturn(card, current2, now2 = Date.now()) {
  const days2 = Math.max(0, (now2 - Date.parse(card.createdAt)) / DAY$3);
  if (card.kind === "savings" || card.kind === "cash") {
    const rate = card.entryRef.rate ?? current2?.value;
    return rate === void 0 ? null : rate * days2 / 365;
  }
  const entry = card.entryRef.price ?? card.entryRef.nav;
  if (entry === void 0 || !current2 || entry <= 0) return null;
  const change = (current2.value - entry) / entry * 100;
  return card.kind === "avoid" ? -change : change;
}

export function goalPlan(g, today2) {
  const n2 = monthsBetween(today2, g.targetDate);
  const futureTarget = g.targetAmount * (1 + g.inflationPct / 100) ** (n2 / 12);
  const r = g.returnPct / 100 / 12;
  const growth = (1 + r) ** n2;
  const annuity = r === 0 ? n2 : (growth - 1) / r;
  const fromSaved = g.saved * growth;
  const projected = fromSaved + g.monthly * annuity;
  const gap = futureTarget - fromSaved;
  const neededMonthly = gap <= 0 ? 0 : n2 === 0 ? gap : gap / annuity;
  return {
    months: n2,
    futureTarget,
    projected,
    neededMonthly,
    onTrack: projected >= futureTarget - 0.5,
    progressPct: futureTarget > 0 ? Math.min(100, projected / futureTarget * 100) : 100
  };
}

export function monthsBetween(today2, date) {
  const [y1, m1, d1] = today2.slice(0, 10).split("-").map(Number);
  const [y2, m2, d2] = date.slice(0, 10).split("-").map(Number);
  return Math.max(0, (y2 - y1) * 12 + (m2 - m1) - (d2 < d1 ? 1 : 0));
}

export function safetyReport(n2, cash, holdingsValue, goldPerTola2) {
  const months = n2.monthlyExpenses > 0 ? n2.bankSavings / n2.monthlyExpenses : null;
  const level = months === null ? "unknown" : months < SAFETY_LOW_MONTHS ? "low" : months < SAFETY_STRONG_MONTHS ? "ok" : "strong";
  const known = n2.monthlyIncome > 0 && n2.monthlyExpenses > 0;
  const surplus = known ? n2.monthlyIncome - n2.monthlyExpenses : null;
  const parts = { cash, holdings: holdingsValue, bank: n2.bankSavings, gold: n2.goldTola * (goldPerTola2 ?? 0), property: n2.propertyValue };
  const gross = parts.cash + parts.holdings + parts.bank + parts.gold + parts.property;
  const pct2 = (v) => gross > 0 ? Math.round(v / gross * 1e3) / 10 : 0;
  return {
    months: months === null ? null : Math.round(months * 10) / 10,
    level,
    surplus,
    savingsRatePct: surplus === null ? null : Math.round(surplus / n2.monthlyIncome * 1e3) / 10,
    yearsToRetire: yearsToRetire(n2.age, n2.retireAge),
    parts,
    gross,
    net: gross - n2.debt,
    sharePct: { cash: pct2(parts.cash), holdings: pct2(parts.holdings), bank: pct2(parts.bank), gold: pct2(parts.gold), property: pct2(parts.property) }
  };
}

export function replay(rows = allTx()) {
  const pos = /* @__PURE__ */ new Map();
  const tx = [];
  for (const r of rows) {
    const key2 = r.kind === "savings" ? `savings:${r.type === "buy" ? r.id : r.lotId ?? ""}` : `${r.kind}:${r.symbolOrName}`;
    if (r.type === "buy") {
      const p2 = r.kind === "savings" ? void 0 : pos.get(key2);
      if (p2) {
        p2.qty += r.qty;
        p2.cost += r.amount + r.fees;
      } else pos.set(key2, { id: r.id, kind: r.kind, symbolOrName: r.symbolOrName, qty: r.qty, cost: r.amount + r.fees, boughtAt: r.at });
      tx.push({ ...r, realized: null, costBasis: null });
      continue;
    }
    if (r.type === "dividend") {
      tx.push({ ...r, realized: null, costBasis: null });
      continue;
    }
    let p = pos.get(key2);
    if (r.kind === "savings" && !p) p = [...pos.values()].find((x) => x.kind === "savings" && x.symbolOrName === r.symbolOrName);
    if (!p || p.qty <= EPS) {
      tx.push({ ...r, realized: null, costBasis: null });
      continue;
    }
    const qty = Math.min(r.qty, p.qty);
    const basis = p.cost * (qty / p.qty);
    const proceeds = r.amount * (qty / r.qty);
    p.qty -= qty;
    p.cost -= basis;
    tx.push({ ...r, realized: proceeds - r.fees - basis, costBasis: basis });
    if (p.qty <= EPS) {
      for (const [k, v] of pos) if (v === p) pos.delete(k);
    }
  }
  return { positions: [...pos.values()], tx };
}

export function fundScore(f, group) {
  const ret = f.annualPct ?? -999;
  if (group === "safe" || group === "income" || f.dailyVolPct === null) return ret;
  return ret - 0.5 * f.dailyVolPct * Math.sqrt(250);
}

export function futureValue(principal, ratePct, years, monthly = 0) {
  const r = ratePct / 100 / 12;
  const n2 = Math.round(years * 12);
  const growth = (1 + r) ** n2;
  return principal * growth + (r === 0 ? monthly * n2 : monthly * ((growth - 1) / r));
}

export function monthlySavingFor(target, ratePct, years, start = 0) {
  const r = ratePct / 100 / 12;
  const n2 = Math.round(years * 12);
  const remaining = target - start * (1 + r) ** n2;
  if (remaining <= 0) return 0;
  return r === 0 ? remaining / n2 : remaining * r / ((1 + r) ** n2 - 1);
}

export function realReturnPct(nominalPct, inflationPct) {
  return ((1 + nominalPct / 100) / (1 + inflationPct / 100) - 1) * 100;
}

export function cagrPct(start, end, years) {
  return ((end / start) ** (1 / years) - 1) * 100;
}

export function sectorKey(sector) {
  if (!sector) return "unknown";
  for (const [re, key2] of SECTORS) if (re.test(sector)) return key2;
  return sector.toLowerCase().replace(/companies|company/g, "").replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "") || "unknown";
}

export function stockFlags(f, target) {
  const out = [];
  if (f.marketCap !== null && f.marketCap < 5e9) out.push({ flag: `Small company (market cap about Rs ${(f.marketCap / 1e9).toFixed(1)} bn)`, why: "Small companies need little money to push the price up, which is why pump groups like them." });
  if (f.freeFloatPct !== null && f.freeFloatPct < 15) out.push({ flag: `Few shares trade freely (free float ${f.freeFloatPct.toFixed(0)}%)`, why: "With few shares available, a small group can move the price a lot, both up and down." });
  if (f.change1mPct !== null && f.change1mPct > 30) out.push({ flag: `Already up ${f.change1mPct.toFixed(0)}% in a month`, why: "Tips often arrive after the rise, when early buyers want someone to sell to." });
  if (f.close !== null && f.w52High !== null && f.w52Low !== null && f.w52High > f.w52Low && f.close >= f.w52Low + 0.95 * (f.w52High - f.w52Low))
    out.push({ flag: "Price is near its 52-week high", why: "Buying at the top of a sharp run is how tip followers usually lose." });
  const base = f.avgVolume60 ?? f.avgVolume20;
  if (f.lastVolume !== null && base && f.sessions >= 10 && f.lastVolume > 3 * base) out.push({ flag: `Volume spike (${(f.lastVolume / base).toFixed(1)}× normal)`, why: "A sudden jump in trading with no news is a classic sign of a pump." });
  if (f.eps !== null && f.eps < 0) out.push({ flag: "The company made a loss last year", why: "A rising price with losses has to be explained by news. If there is none, be very careful." });
  if (f.pe !== null && f.pe > 40) out.push({ flag: `Very expensive (P/E ${f.pe.toFixed(0)})`, why: "Investors are paying many years of profit; that needs a strong, checkable story." });
  if (!f.kse100) out.push({ flag: "Not in the KSE-100", why: "Outside the biggest 100 companies, there is less research and trading, so hype moves prices more." });
  if (target !== null && f.close !== null && target > f.close * 1.3) out.push({ flag: `Big target (Rs ${target}, ${((target - f.close) / f.close * 100).toFixed(0)}% above the price)`, why: "A large target with no valuation behind it is a hook. Ask how it was worked out." });
  return out;
}

export function portfolioCheckup() {
  const p = getPortfolio();
  const s = getSettings();
  const total = p.total;
  const findings = [];
  const pctOf = (v) => total ? v / total * 100 : 0;
  const funds = fundsByName();
  const fundGroupOf = (h) => fundGroup(funds.get(h.symbolOrName)?.category ?? "") ?? "unknown";
  const sum = (pred) => p.holdings.filter(pred).reduce((a, h) => a + h.value, 0);
  const stocks = p.holdings.filter((h) => h.kind === "stock");
  const equityFunds = sum((h) => h.kind === "fund" && ["equity", "mixed"].includes(fundGroupOf(h)));
  const safeFunds = sum((h) => h.kind === "fund" && ["safe", "income"].includes(fundGroupOf(h)));
  const otherFunds = sum((h) => h.kind === "fund") - equityFunds - safeFunds;
  const mix = [
    { label: "Cash", value: p.cash },
    { label: "Savings schemes", value: sum((h) => h.kind === "savings") },
    { label: "Money market and income funds", value: safeFunds },
    { label: "Equity and mixed funds", value: equityFunds },
    { label: "Other funds", value: otherFunds },
    { label: "Shares", value: sum((h) => h.kind === "stock") },
    { label: "Gold", value: sum((h) => h.kind === "gold") }
  ].filter((m) => m.value > 0).map((m) => ({ ...m, pct: pctOf(m.value) }));
  const facts = stockFactsMany(stocks.map((h) => h.symbolOrName));
  const bySector = /* @__PURE__ */ new Map();
  for (const h of stocks) {
    const f = facts.get(h.symbolOrName);
    const key2 = f?.sectorKey ?? "unknown";
    const label = niceSector(f?.sector);
    const cur = bySector.get(key2) ?? { label, value: 0, count: 0 };
    bySector.set(key2, { label: cur.label, value: cur.value + h.value, count: cur.count + 1 });
  }
  const sectorRows = [...bySector.values()].sort((a, b) => b.value - a.value);
  const sectors = sectorRows.map((x) => ({ label: x.label, value: x.value, pct: pctOf(x.value) }));
  if (!total) {
    return { score: 0, total: 0, mix: [], sectors: [], findings: [{ severity: "medium", title: "Nothing entered yet", detail: "Igent cannot check an empty portfolio.", fix: "Enter your cash and holdings in My Money." }], topFixes: ["Enter your cash and holdings in My Money."] };
  }
  for (const h of stocks) {
    const w = pctOf(h.value);
    if (w > s.maxSinglePositionPct)
      findings.push({ severity: "high", title: `${h.symbolOrName} is ${w.toFixed(0)}% of your money`, detail: `Your own limit is ${s.maxSinglePositionPct}% per share. One bad result could hurt a lot.`, fix: `Trim ${h.symbolOrName} by about ${rs$1(h.value - total * s.maxSinglePositionPct / 100)}, or add money elsewhere (Tools → Should I Sell?).` });
  }
  const top = sectorRows[0] && sectorRows[0].count > 1 ? sectors[0] : void 0;
  if (top && top.pct > 35) findings.push({ severity: "high", title: `${top.pct.toFixed(0)}% in one sector (${top.label})`, detail: "Shares in one sector tend to fall together on the same news.", fix: "Spread new money into other sectors or an equity fund." });
  else if (top && top.pct > 25) findings.push({ severity: "medium", title: `${top.pct.toFixed(0)}% in ${top.label}`, detail: "A large part of your money depends on one sector.", fix: "Lean new share buys towards other sectors." });
  if (stocks.length > 0 && stocks.length <= 2 && pctOf(sum((h) => h.kind === "stock")) > 30)
    findings.push({ severity: "medium", title: `Only ${stocks.length} share(s), but ${pctOf(sum((h) => h.kind === "stock")).toFixed(0)}% of your money`, detail: "With so few shares, one company's news decides your result.", fix: "Use an equity fund for most of your share exposure, or hold 5 or more shares from different sectors." });
  const tbill = latestRate$1("tbill_3m");
  const cashPct = pctOf(p.cash);
  if (cashPct > 30 && p.cash > 5e4) {
    const month = tbill ? p.cash * tbill.value / 100 / 12 : null;
    findings.push({
      severity: "medium",
      title: `${cashPct.toFixed(0)}% sits in cash`,
      detail: month ? `At the 3-month T-bill rate (${tbill.value.toFixed(1)}%) this could earn about ${rs$1(month)} a month before tax.` : "Idle cash loses value to inflation.",
      fix: "Keep your emergency fund, and move the rest to a money market fund (Tools → Fund Picker)."
    });
  }
  if (p.cashUpdatedAt && Date.now() - Date.parse(p.cashUpdatedAt) > 60 * 864e5)
    findings.push({ severity: "low", title: "Cash on hand is out of date", detail: `Last updated ${p.cashUpdatedAt.slice(0, 10)}.`, fix: "Update Cash on Hand in My Money so the checks are right." });
  const goals2 = listGoals();
  const behind = goals2.filter((g) => !g.plan.onTrack && g.plan.months > 0);
  if (behind.length) findings.push({ severity: "medium", title: `${behind.length} goal(s) behind`, detail: behind.map((g) => `${g.name}: add about ${rs$1(g.plan.neededMonthly - g.monthly)} a month`).join("; "), fix: "Raise the monthly amount or move the date (My Money → Goals)." });
  const riskyPct = pctOf(sum((h) => h.kind === "stock") + equityFunds);
  const soon = goals2.filter((g) => g.plan.months > 0 && g.plan.months <= 24);
  if (soon.length && riskyPct > 50)
    findings.push({ severity: "high", title: "A goal is close, but most money is in shares", detail: `${soon.map((g) => g.name).join(", ")} within 2 years, and ${riskyPct.toFixed(0)}% of your money is in shares or equity funds.`, fix: "Move the money for near goals to safe options as the date gets close." });
  if (s.shariahOnly) {
    const bad = [...stocks.filter((h) => facts.get(h.symbolOrName) && !facts.get(h.symbolOrName).shariah).map((h) => h.symbolOrName), ...p.holdings.filter((h) => h.kind === "fund" && funds.get(h.symbolOrName) && !isShariahFund(funds.get(h.symbolOrName).category)).map((h) => h.symbolOrName)];
    if (bad.length) findings.push({ severity: "high", title: "Not Shariah-compliant", detail: `${bad.join(", ")} ${bad.length === 1 ? "is" : "are"} not on the Shariah lists, but your settings say Shariah only.`, fix: "Check with your scholar, and consider switching to a Shariah-compliant share or fund." });
  }
  const goldPct = pctOf(sum((h) => h.kind === "gold"));
  if (goldPct > 30) findings.push({ severity: "medium", title: `${goldPct.toFixed(0)}% in gold`, detail: "Gold protects against a falling rupee but pays no income and can stay flat for years.", fix: "Keep gold to about 10–20% and put new money into income-producing options." });
  const losers = p.holdings.filter((h) => (h.gainPct ?? 0) < -25);
  if (losers.length) findings.push({ severity: "low", title: `${losers.length} holding(s) down more than 25%`, detail: losers.map((h) => `${h.symbolOrName} ${h.gainPct.toFixed(0)}%`).join(", "), fix: "Check whether the reason you bought still holds (Tools → Should I Sell?). The price you paid does not matter to the market." });
  const noPrice = p.holdings.filter((h) => h.kind !== "savings" && h.unitValue === null);
  if (noPrice.length) findings.push({ severity: "low", title: "Some holdings have no price yet", detail: noPrice.map((h) => h.symbolOrName).join(", "), fix: "Check the name or symbol; funds need the exact MUFAP name." });
  if (!findings.some((f) => f.title.includes("% of your money")) && stocks.length >= 5) findings.push({ severity: "good", title: "No single share is too big", detail: `You hold ${stocks.length} shares, each within your ${s.maxSinglePositionPct}% limit.`, fix: null });
  if (cashPct <= 30 && cashPct >= 3) findings.push({ severity: "good", title: "Cash level looks sensible", detail: `${cashPct.toFixed(0)}% in cash.`, fix: null });
  if (goals2.length && !behind.length) findings.push({ severity: "good", title: "Goals on track", detail: `${goals2.length} goal(s) on track.`, fix: null });
  const order = { high: 0, medium: 1, low: 2, good: 3 };
  findings.sort((a, b) => order[a.severity] - order[b.severity]);
  const score = Math.max(0, 100 - findings.reduce((s2, f) => s2 + SEV_COST[f.severity], 0));
  return { score, total, mix, sectors, findings, topFixes: findings.filter((f) => f.fix && f.severity !== "good").slice(0, 3).map((f) => f.fix) };
}

export function taxYearStart(date) {
  const y = Number(date.slice(0, 4));
  const m = Number(date.slice(5, 7));
  return `${m >= 7 ? y : y - 1}-07-01`;
}

export function isQuietTime(s, d = /* @__PURE__ */ new Date()) {
  if (!s.quietHoursEnabled) return false;
  const now2 = pktMinutes(d);
  const start = toMin(s.quietStart);
  const end = toMin(s.quietEnd);
  if (start === end) return false;
  return start < end ? now2 >= start && now2 < end : now2 >= start || now2 < end;
}

export function inMarketHours(d) {
  const { day, minutes } = pktParts(d);
  return day >= 1 && day <= 5 && minutes >= 9 * 60 + 15 && minutes <= 15 * 60 + 45;
}

export function betaFromHistory(symbol) {
  const rows = sqlite().prepare(
    `SELECT p.date, p.close, i.level FROM prices_daily p JOIN index_levels i ON i.index_code = 'KSE100' AND i.date = p.date
			 WHERE p.symbol = ? AND p.date >= date('now', '-365 days') ORDER BY p.date`
  ).all(symbol);
  if (rows.length < 40) return null;
  const rs2 = rows.slice(1).map((r, i) => [r.close / rows[i].close - 1, r.level / rows[i].level - 1]);
  const mx = rs2.reduce((a, r) => a + r[1], 0) / rs2.length;
  const ms = rs2.reduce((a, r) => a + r[0], 0) / rs2.length;
  let cov = 0;
  let varx = 0;
  for (const [s, x] of rs2) {
    cov += (s - ms) * (x - mx);
    varx += (x - mx) ** 2;
  }
  return varx ? Math.min(2.5, Math.max(0.2, cov / varx)) : null;
}