"use strict";
var __create = Object.create;
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __getProtoOf = Object.getPrototypeOf;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
  // If the importer is in node compatibility mode or this is not an ESM
  // file that has been converted to a CommonJS file using a Babel-
  // compatible transform (i.e. "__esModule" has not been set), then set
  // "default" to the CommonJS "module.exports" for node compatibility.
  isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
  mod
));
const electron = require("electron");
const cheerio = require("cheerio");
const Parser = require("rss-parser");
const mammoth = require("mammoth");
const node_module = require("node:module");
const node_url = require("node:url");
const promises$1 = require("node:dns/promises");
const node_net = require("node:net");
const promises = require("node:fs/promises");
const node_path = require("node:path");
const protocol = require("./chunks/protocol-CAnp9dtF.js");
function _interopNamespaceDefault(e) {
  const n = Object.create(null, { [Symbol.toStringTag]: { value: "Module" } });
  if (e) {
    for (const k in e) {
      if (k !== "default") {
        const d = Object.getOwnPropertyDescriptor(e, k);
        Object.defineProperty(n, k, d.get ? d : {
          enumerable: true,
          get: () => e[k]
        });
      }
    }
  }
  n.default = e;
  return Object.freeze(n);
}
const cheerio__namespace = /* @__PURE__ */ _interopNamespaceDefault(cheerio);
const USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36";
const BROWSER_HEADERS = {
  "User-Agent": USER_AGENT,
  Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
  "Accept-Language": "en-PK,en;q=0.9",
  "Upgrade-Insecure-Requests": "1",
  "Sec-Fetch-Dest": "document",
  "Sec-Fetch-Mode": "navigate",
  "Sec-Fetch-Site": "none",
  "Sec-Fetch-User": "?1",
  "sec-ch-ua": '"Chromium";v="148", "Not.A/Brand";v="24"',
  "sec-ch-ua-mobile": "?0",
  "sec-ch-ua-platform": '"Windows"'
};
const MAX_BYTES$1 = 5 * 1024 * 1024;
const TIMEOUT_MS = 3e4;
const RETRY_DELAYS = [2e3, 8e3, 3e4];
const MAX_IN_FLIGHT = 4;
class HttpError extends Error {
  constructor(status, url) {
    super(`HTTP ${status} from ${new URL(url).host}`);
    this.status = status;
    this.name = "HttpError";
  }
  status;
}
function hostGapMs(host) {
  if (host.endsWith("psx.com.pk")) return 2e4;
  if (host.endsWith("sbp.org.pk")) return 1e4;
  return 2e3;
}
let fetchImpl = (...a) => fetch(...a);
function setFetchImpl(f) {
  fetchImpl = f;
}
const netFetch = (...a) => fetchImpl(...a);
const nextAllowed = /* @__PURE__ */ new Map();
let inFlight = 0;
const waiters = [];
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
async function acquire(host) {
  while (inFlight >= MAX_IN_FLIGHT) await new Promise((r) => waiters.push(r));
  inFlight++;
  const now = Date.now();
  const at = Math.max(now, nextAllowed.get(host) ?? 0);
  nextAllowed.set(host, at + hostGapMs(host));
  if (at > now) await sleep(at - now);
}
function release() {
  inFlight--;
  waiters.shift()?.();
}
async function readCapped$1(res, maxBytes = MAX_BYTES$1) {
  if (!res.body) return Buffer.alloc(0);
  const reader = res.body.getReader();
  const chunks = [];
  let total = 0;
  for (; ; ) {
    const { done, value } = await reader.read();
    if (done) break;
    total += value.length;
    if (total > maxBytes) {
      await reader.cancel();
      throw new Error(`response larger than ${maxBytes} bytes`);
    }
    chunks.push(value);
  }
  return Buffer.concat(chunks);
}
function retryable(err) {
  if (err instanceof HttpError) return err.status === 429 || err.status >= 500;
  const msg = String(err?.message ?? err);
  return /ECONNRESET|ETIMEDOUT|ECONNREFUSED|EAI_AGAIN|fetch failed|socket|aborted|timeout|ERR_CONNECTION_(RESET|CLOSED|REFUSED|TIMED_OUT|ABORTED)|ERR_TIMED_OUT|ERR_NETWORK_CHANGED|ERR_INTERNET_DISCONNECTED|ERR_NAME_NOT_RESOLVED|ERR_ADDRESS_UNREACHABLE|ERR_EMPTY_RESPONSE|ERR_HTTP2_PROTOCOL_ERROR|ERR_QUIC_PROTOCOL_ERROR|ERR_SSL_PROTOCOL_ERROR/i.test(msg);
}
async function once(url, opts) {
  const host = new URL(url).host;
  await acquire(host);
  try {
    const headers = { ...BROWSER_HEADERS };
    if (opts.accept) headers.Accept = opts.accept;
    if (opts.etag) headers["If-None-Match"] = opts.etag;
    if (opts.lastModified) headers["If-Modified-Since"] = opts.lastModified;
    const res = await fetchImpl(url, { headers, redirect: opts.redirect ?? "follow", signal: AbortSignal.timeout(TIMEOUT_MS) });
    const location = res.headers.get("location");
    if (res.status === 304 || opts.redirect === "manual" && res.status >= 300 && res.status < 400) {
      await res.body?.cancel();
      return { status: res.status, body: Buffer.alloc(0), etag: res.headers.get("etag"), lastModified: res.headers.get("last-modified"), finalUrl: res.url || url, location };
    }
    if (!res.ok) {
      await res.body?.cancel();
      throw new HttpError(res.status, url);
    }
    const body = await readCapped$1(res, opts.maxBytes);
    return { status: res.status, body, etag: res.headers.get("etag"), lastModified: res.headers.get("last-modified"), finalUrl: res.url || url, location };
  } finally {
    release();
  }
}
async function httpGet(url, opts = {}) {
  let lastErr;
  const delays = opts.noRetry ? [] : RETRY_DELAYS;
  for (let attempt = 0; attempt <= delays.length; attempt++) {
    try {
      return await once(url, opts);
    } catch (err) {
      lastErr = err;
      if (attempt === delays.length || !retryable(err)) break;
      await sleep(delays[attempt]);
    }
  }
  throw lastErr;
}
const MONTHS = {
  jan: 1,
  feb: 2,
  mar: 3,
  apr: 4,
  may: 5,
  jun: 6,
  jul: 7,
  aug: 8,
  sep: 9,
  sept: 9,
  oct: 10,
  nov: 11,
  dec: 12,
  january: 1,
  february: 2,
  march: 3,
  april: 4,
  june: 6,
  july: 7,
  august: 8,
  september: 9,
  october: 10,
  november: 11,
  december: 12
};
function num(s) {
  if (s === null || s === void 0) return null;
  let t = String(s).replace(/[,\s%]|Rs\.?/gi, "").trim();
  if (!t || t === "-" || /^n\/?a$/i.test(t)) return null;
  let neg = false;
  if (/^\(.*\)$/.test(t)) {
    neg = true;
    t = t.slice(1, -1);
  }
  const v = Number(t);
  if (!Number.isFinite(v)) return null;
  return neg ? -v : v;
}
function pad(n) {
  return String(n).padStart(2, "0");
}
function fullYear(y) {
  return y < 100 ? 2e3 + y : y;
}
function parseDate(s) {
  if (!s) return null;
  const t = s.replace(/(\d)(st|nd|rd|th)\b/gi, "$1").replace(/\bof\b/gi, " ").replace(/\s+/g, " ").trim();
  let m = /(\d{4})-(\d{2})-(\d{2})/.exec(t);
  if (m) return `${m[1]}-${m[2]}-${m[3]}`;
  m = /\b([A-Za-z]{3,9})\.? (\d{1,2}),? (\d{4})\b/.exec(t);
  if (m && MONTHS[m[1].toLowerCase()]) return `${m[3]}-${pad(MONTHS[m[1].toLowerCase()])}-${pad(Number(m[2]))}`;
  m = /\b(\d{1,2}) ?[- ] ?([A-Za-z]{3,9}) ?[- ] ?(\d{2,4})\b/.exec(t);
  if (m && MONTHS[m[2].toLowerCase()]) return `${fullYear(Number(m[3]))}-${pad(MONTHS[m[2].toLowerCase()])}-${pad(Number(m[1]))}`;
  m = /\b(\d{1,2})\/(\d{1,2})\/(\d{4})\b/.exec(t);
  if (m) return `${m[3]}-${pad(Number(m[2]))}-${pad(Number(m[1]))}`;
  return null;
}
function pkToday(now = /* @__PURE__ */ new Date()) {
  return new Date(now.getTime() + 5 * 36e5).toISOString().slice(0, 10);
}
function clean(s) {
  return (s ?? "").replace(/\s+/g, " ").trim();
}
function htmlToText$1(html, max = 2e4) {
  if (!html) return "";
  const text2 = html.replace(/<(script|style)[\s\S]*?<\/\1>/gi, " ").replace(/<br\s*\/?>/gi, "\n").replace(/<\/(p|div|li|h\d)>/gi, "\n").replace(/<[^>]+>/g, " ").replace(/&nbsp;/g, " ").replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&#0?39;|&apos;|&#8217;|&#8216;/g, "'").replace(/&#8220;|&#8221;/g, '"').replace(/&#8211;|&#8212;/g, "-").replace(/&#\d+;/g, " ").replace(/[ \t]+/g, " ").replace(/\n\s*\n+/g, "\n").trim();
  return text2.length > max ? text2.slice(0, max) : text2;
}
class ParserMatchedNothing extends Error {
  constructor(what) {
    super(`parser matched nothing (${what})`);
    this.name = "ParserMatchedNothing";
  }
}
function parseSbp(html, today = pkToday()) {
  const $ = cheerio__namespace.load(html);
  const text2 = clean($("body").text());
  const rows = [];
  const pr = /SBP Policy Rate\s+(\d+(?:\.\d+)?)%/i.exec(text2) ?? /policy rate (?:unchanged at|to|at) (\d+(?:\.\d+)?)%/i.exec(text2);
  if (pr) {
    const mpc = /meeting held on (\d{1,2} [A-Za-z]+,? \d{4})/i.exec(text2);
    rows.push({ kind: "policy_rate", date: today, value: Number(pr[1]), meta: { mpcDate: mpc ? parseDate(mpc[1]) : null } });
  }
  const ceiling = /Reverse \(Repo\) Ceiling Rate\s+(\d+(?:\.\d+)?)%/i.exec(text2);
  if (ceiling) rows.push({ kind: "sbp_ceiling_rate", date: today, value: Number(ceiling[1]) });
  const floor = /Reverse \(Floor\) Rate\s+(\d+(?:\.\d+)?)%/i.exec(text2);
  if (floor) rows.push({ kind: "sbp_floor_rate", date: today, value: Number(floor[1]) });
  const kiborDateM = /KIBOR As on ([0-9]{1,2} ?- ?[A-Za-z]{3,9} ?- ?[0-9]{2,4})/i.exec(text2);
  const kiborDate = kiborDateM && parseDate(kiborDateM[1]) || today;
  const tables = $("table.kibor-table").toArray();
  const kiborTable = tables.find((t) => /BID/i.test($(t).text()) && /Offer/i.test($(t).text()));
  if (kiborTable) {
    $(kiborTable).find("tr").each((_, tr) => {
      const cells = $(tr).find("td").map((__, td) => clean($(td).text())).get();
      const tenor = /^(\d+)-M$/i.exec(cells[0] ?? "");
      const bid = num(cells[1]);
      const offer = num(cells[2]);
      if (tenor && offer !== null) rows.push({ kind: `kibor_${tenor[1]}m`, date: kiborDate, value: offer, meta: { bid } });
    });
  }
  let tbillSeen = false;
  let pibSeen = false;
  for (const t of tables) {
    if (t === kiborTable) continue;
    const head = clean($(t).find("tr").first().text());
    if (!/Cut-off Yield/i.test(head)) continue;
    const parentText = clean($(t).parent().text());
    const asOn = /\(as on ([A-Za-z]{3,9} \d{1,2}, \d{4})/i.exec(parentText);
    const date = asOn && parseDate(asOn[1]) || today;
    const tenors = [];
    $(t).find("tr").each((_, tr) => {
      const cells = $(tr).find("td").map((__, td) => clean($(td).text())).get();
      const m = /^(\d+)-(M|Y)$/i.exec(cells[0] ?? "");
      const v = num(cells[1]);
      if (m && v !== null) tenors.push([`${m[1]}${m[2].toLowerCase()}`, v]);
    });
    if (!tenors.length) continue;
    const isTbill = tenors.every(([k]) => k.endsWith("m"));
    if (isTbill && !tbillSeen) {
      tbillSeen = true;
      for (const [k, v] of tenors) rows.push({ kind: `tbill_${k}`, date, value: v });
    } else if (!isTbill && !pibSeen) {
      pibSeen = true;
      for (const [k, v] of tenors) rows.push({ kind: `pib_${k}`, date, value: v });
    }
  }
  const res = /Liquid Foreign Exchange Reserves \(USD million\) As on ([0-9]{1,2} ?- ?[A-Za-z]+ ?- ?[0-9]{4}).*?Total Reserves ([\d,.]+)/i.exec(text2);
  if (res) {
    const sbpRes = /SBP.s Reserves ([\d,.]+)/i.exec(text2);
    rows.push({ kind: "fx_reserves_total", date: parseDate(res[1]) || today, value: num(res[2]), meta: { sbp: num(sbpRes?.[1]) } });
  }
  const repo = /overnight repo rate As on ([0-9]{1,2}-[A-Za-z]{3}-[0-9]{2}) (\d+(?:\.\d+)?)%/i.exec(text2);
  if (repo) rows.push({ kind: "overnight_repo", date: parseDate(repo[1]) || today, value: Number(repo[2]) });
  if (!rows.some((r) => r.kind === "policy_rate" || r.kind.startsWith("kibor_"))) throw new ParserMatchedNothing("SBP policy rate / KIBOR");
  return rows;
}
function parsePsxIndices(html) {
  const $ = cheerio__namespace.load(html);
  const asOf = /As of\s+([A-Za-z]{3,9} \d{1,2}, \d{4})/i.exec(clean($("body").text()));
  const date = asOf && parseDate(asOf[1]) || pkToday();
  const rows = [];
  $("table").each((_, t) => {
    const head = clean($(t).find("tr").first().text());
    if (!/High/i.test(head) || !/Current/i.test(head)) return;
    $(t).find("tr").slice(1).each((__, tr) => {
      const c = $(tr).find("td").map((___, td) => clean($(td).text())).get();
      const code = (c[0] ?? "").split(" ")[0].toUpperCase();
      const level = num(c[3]);
      if (!code || level === null) return;
      rows.push({ indexCode: code, date, level, high: num(c[1]), low: num(c[2]), changeAbs: num(c[4]), changePct: num(c[5]) });
    });
  });
  if (!rows.length) throw new ParserMatchedNothing("PSX indices table");
  return rows;
}
const MEMBER_INDICES = [
  { code: "KSE100", url: "https://dps.psx.com.pk/indices/KSE100", min: 90 },
  { code: "KMI30", url: "https://dps.psx.com.pk/indices/KMI30", min: 25 },
  { code: "KMIALLSHR", url: "https://dps.psx.com.pk/indices/KMIALLSHR", min: 100 }
];
function parsePsxIndexMembers(html, indexCode, min = 1) {
  const $ = cheerio__namespace.load(html);
  const seen = /* @__PURE__ */ new Set();
  const members = [];
  $("a.tbl__symbol").each((_, a) => {
    const symbol = clean($(a).text()).toUpperCase();
    if (!/^[A-Z0-9-]{1,15}$/.test(symbol) || seen.has(symbol)) return;
    seen.add(symbol);
    const name = clean($(a).attr("data-title") ?? $(a).closest("tr").find("td").eq(1).text()) || symbol;
    members.push({ symbol, name });
  });
  if (members.length < min) throw new ParserMatchedNothing(`${indexCode} constituents (${members.length} rows)`);
  return { indexCode, members };
}
function parsePsxEodText(text2) {
  const dateM = /Market Position as of\s*\|?\s*([A-Za-z]{3,9} \d{1,2}, \d{4})/i.exec(text2);
  const date = dateM ? parseDate(dateM[1]) : null;
  if (!date) throw new ParserMatchedNothing("PSX EOD date");
  const lines = text2.split(/\r?\n/).map((l) => l.trim());
  const indexLevels = [];
  const hi = lines.findIndex((l) => l === "INDICES");
  if (hi >= 0) {
    const codes = (lines[hi + 1] ?? "").split(/\s+/).filter(Boolean);
    const get = (label) => {
      const l = lines.slice(hi + 2, hi + 10).find((x) => x.startsWith(label + " "));
      return l ? l.slice(label.length).trim().split(/\s+/).map(num) : [];
    };
    const cur = get("Current");
    const high = get("High");
    const low = get("Low");
    const chg = get("Change");
    const pct = get("Percentage");
    codes.forEach((code, i) => {
      const level = cur[i];
      if (level === null || level === void 0) return;
      const indexCode = code === "KSEALLSHR" ? "ALLSHR" : code;
      indexLevels.push({ indexCode, date, level, high: high[i] ?? null, low: low[i] ?? null, changeAbs: chg[i] ?? null, changePct: pct[i] ?? null });
    });
  }
  const rates = [];
  const ri = lines.findIndex((l) => /^Ready Market Companies Position/i.test(l));
  if (ri >= 0) {
    const nums = (lines[ri + 2] ?? "").split(/\s+/).map(num);
    if (nums.length >= 4 && nums.every((n) => n !== null)) {
      rates.push({ kind: "psx_advancers", date, value: nums[0] });
      rates.push({ kind: "psx_decliners", date, value: nums[1] });
      rates.push({ kind: "psx_unchanged", date, value: nums[2] });
    }
  }
  const reg = lines.find((l) => /^REG \d/.test(l));
  if (reg) {
    const n = reg.split(/\s+/).slice(1).map(num);
    if (n[0] !== null && n[0] !== void 0) rates.push({ kind: "psx_volume_reg", date, value: n[0], meta: { previous: n[1] } });
    if (n[2] !== null && n[2] !== void 0) rates.push({ kind: "psx_traded_value_reg", date, value: n[2], meta: { previous: n[3] } });
    if (n[4] !== null && n[4] !== void 0) rates.push({ kind: "psx_market_cap", date, value: n[4], meta: { previous: n[5] } });
  }
  const ti = lines.findIndex((l) => /^READY MARKET$/i.test(l));
  if (ti >= 0) {
    const top = [];
    for (const l of lines.slice(ti + 3, ti + 20)) {
      const m = /^(.+?) ([\d,.]+) ([\d,.]+) ([\d,.]+) ([\d,.]+) ([\d,.]+) ([\d,]+)$/.exec(l);
      if (!m) {
        if (top.length) break;
        continue;
      }
      top.push({ company: m[1], prev: num(m[2]), close: num(m[4]), volume: num(m[7]) });
    }
    if (top.length) rates.push({ kind: "psx_top_volume", date, value: top.length, meta: { top } });
  }
  if (!indexLevels.length) throw new ParserMatchedNothing("PSX EOD index block");
  return { date, indexLevels, rates };
}
function closingRatesUrl(date) {
  return `https://dps.psx.com.pk/download/closing_rates/${date}.pdf`;
}
const ROW = /^([A-Z0-9][A-Z0-9]{0,11})\s+(.+?)\s+(\d+)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?|-)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)$/;
const HEADER = /^(Pakistan Stock Exchange|CLOSING RATE SUMMARY|From :|Flu No|P\. Vol|C\. Vol|Total:|Company Name Turnover)|PageNo:/;
const EX_KIND = { XD: "ex_dividend", XB: "ex_bonus", XR: "ex_rights" };
function parsePsxClosingSummary(text2) {
  const lines = text2.split(/\r?\n/).map((l) => l.trim()).filter(Boolean);
  const dateLine = lines.find((l) => /PageNo:/.test(l));
  const dm = dateLine ? /([A-Za-z]+ \d{1,2}),\s*(\d{4})/.exec(dateLine) : null;
  const date = dm ? parseDate(`${dm[1]}, ${dm[2]}`) : null;
  if (!date) throw new ParserMatchedNothing("PSX closing rates date");
  const out = /* @__PURE__ */ new Map();
  const events = [];
  let buf = "";
  let skip = false;
  for (const line of lines) {
    if (/^\*{3,}\s*(FUTURE CONTRACTS|BONDS|STOCK INDEX FUTURE)/i.test(line)) skip = true;
    else if (/^\*{3,}\s*DEFAULTER/i.test(line)) skip = false;
    if (HEADER.test(line)) continue;
    if (line.startsWith("***")) {
      buf = "";
      continue;
    }
    if (skip) continue;
    const whole = ROW.exec(line);
    buf = whole ? line : buf ? `${buf} ${line}` : line;
    const m = whole ?? ROW.exec(buf);
    if (!m) {
      if (buf.length > 200) buf = "";
      continue;
    }
    buf = "";
    const tags = /((?:X[DBR])+)$/.exec(m[2].replace(/\s+/g, ""))?.[1] ?? "";
    for (const tag of tags.match(/X[DBR]/g) ?? []) events.push({ symbol: m[1], kind: EX_KIND[tag], date, label: tag, detail: `PSX marked ${tag}` });
    const volume = num(m[3]);
    const traded = !!volume;
    out.set(m[1], {
      symbol: m[1],
      date,
      ldcp: num(m[4]),
      open: traded ? num(m[5]) : null,
      high: traded ? num(m[6]) : null,
      low: traded ? num(m[7]) : null,
      close: num(m[8]),
      volume: volume ?? 0
    });
  }
  if (out.size < 100) throw new ParserMatchedNothing(`PSX closing rates (only ${out.size} rows)`);
  return { prices: [...out.values()], events };
}
function section($, title) {
  return $("h1").filter((_, e) => clean($(e).text()) === title).first().parent();
}
function parseFinancials($, symbol) {
  const rows = /* @__PURE__ */ new Map();
  const fieldFor = (label) => {
    const l = label.toLowerCase();
    if (l === "sales" || l.startsWith("revenue") || l.startsWith("net sales") || l.startsWith("total income")) return "sales";
    if (l.startsWith("profit after tax")) return "profitAfterTax";
    if (l === "eps" || l.startsWith("earnings per share")) return "eps";
    if (l.startsWith("gross profit margin")) return "grossMarginPct";
    if (l.startsWith("net profit margin")) return "netMarginPct";
    if (l.startsWith("eps growth")) return "epsGrowthPct";
    if (l === "peg") return "peg";
    return null;
  };
  for (const title of ["Financials", "Ratios"]) {
    section($, title).find("table").each((_, t) => {
      const trs = $(t).find("tr");
      const periods = trs.first().find("th, td").map((__, c) => clean($(c).text())).get().slice(1);
      if (!periods.length || !periods.every((p) => /^(Q[1-4] )?\d{4}$/.test(p))) return;
      trs.slice(1).each((__, tr) => {
        const cells = $(tr).find("th, td").map((___, c) => clean($(c).text())).get();
        const field = fieldFor(cells[0] ?? "");
        if (!field) return;
        periods.forEach((period, i) => {
          const periodType = period.startsWith("Q") ? "quarter" : "annual";
          const key = `${periodType}:${period}`;
          const row = rows.get(key) ?? { symbol, periodType, period };
          row[field] = num(cells[i + 1]);
          rows.set(key, row);
        });
      });
    });
  }
  return [...rows.values()];
}
function parseProfile($) {
  const out = {};
  section($, "Equity Profile").find(".stats_item, .stats__item").each((_, el) => {
    const label = clean($(el).find(".stats_label, .stats__label").text()).toLowerCase();
    const raw = clean($(el).find(".stats_value, .stats__value").text());
    const v = num(raw);
    if (v === null) return;
    if (label.startsWith("market cap")) out.marketCap = label.includes("000") ? v * 1e3 : v;
    else if (label === "shares") out.shares = v;
    else if (label === "free float") {
      if (raw.includes("%")) out.freeFloatPct = v;
      else out.freeFloatShares = v;
    }
  });
  const desc = clean(section($, "Company Profile").find(".profile__item--decription p, .profile__item--description p").first().text());
  if (desc) out.description = desc.slice(0, 1500);
  return out;
}
const BASE = "https://dps.psx.com.pk";
function parsePsxCompany(html, symbol) {
  const $ = cheerio__namespace.load(html);
  const close = num($(".quote__close").first().text());
  const asOf = /As of\s+(?:[A-Za-z]{3}, )?([A-Za-z]{3,9} \d{1,2}, \d{4})/i.exec(clean($(".quote__date").first().text()));
  const date = asOf ? parseDate(asOf[1]) : null;
  if (close === null || !date) throw new ParserMatchedNothing(`PSX company quote for ${symbol}`);
  const stats = /* @__PURE__ */ new Map();
  const regPanel = $('.tabs__panel[data-name="REG"]');
  const scope = regPanel.length ? regPanel : $("body");
  scope.find(".stats_item, .stats__item").each((_, el) => {
    const label = clean($(el).find(".stats_label, .stats__label").text()) || "";
    const value = clean($(el).find(".stats_value, .stats__value").text()) || "";
    if (label) {
      if (!stats.has(label.toUpperCase())) stats.set(label.toUpperCase(), value);
    } else {
      const t = clean($(el).text());
      const m = /^([A-Za-z0-9 \-]+?)\s*([\d,.\-—\s]+)$/.exec(t);
      if (m && !stats.has(m[1].trim().toUpperCase())) stats.set(m[1].trim().toUpperCase(), m[2].trim());
    }
  });
  const stat = (k) => num(stats.get(k));
  const range52 = (stats.get("52-WEEK RANGE ^") ?? stats.get("52-WEEK RANGE") ?? "").split(/—|-/).map((s) => num(s));
  const name = clean($(".quote__name").first().text()).replace(/(XD|XB|XR|NC|SPOT)$/, "").trim();
  const sector = clean($(".quote__sector").first().text()) || void 0;
  const announcements = [];
  $("table").each((_, t) => {
    const head = clean($(t).find("tr").first().text());
    if (!/^Date\s*Title/i.test(head)) return;
    $(t).find("tr").slice(1).each((__, tr) => {
      const tds = $(tr).find("td");
      const d = parseDate(clean(tds.eq(0).text()));
      const title = clean(tds.eq(1).text());
      const href = tds.find("a[href]").last().attr("href");
      if (!d || !title) return;
      const url = href ? href.startsWith("http") ? href : BASE + href : `${BASE}/company/${symbol}#${d}-${encodeURIComponent(title.slice(0, 40))}`;
      announcements.push({ symbol, date: d, title, url, kind: classifyAnnouncement(title) });
    });
  });
  return {
    price: { symbol, date, close, open: stat("OPEN"), high: stat("HIGH"), low: stat("LOW"), volume: stat("VOLUME"), ldcp: stat("LDCP") },
    company: { symbol, name: name || symbol, sector, week52Low: range52[0] ?? null, week52High: range52[1] ?? null, ...parseProfile($) },
    announcements,
    financials: parseFinancials($, symbol)
  };
}
function classifyAnnouncement(title) {
  const t = title.toLowerCase();
  if (/financial results|quarter|annual accounts|half year/.test(t)) return "results";
  if (/dividend|bonus|right shares|payout|book closure/.test(t)) return "payout";
  if (/board meeting/.test(t)) return "board";
  if (/material information/.test(t)) return "material";
  if (/agm|eogm|general meeting/.test(t)) return "meeting";
  return "other";
}
const DATE = String.raw`(?:[A-Za-z]+,\s*)?([A-Za-z]{3,9}\.? \d{1,2},? \d{4})`;
function parsePayoutNotice(text2, symbol, noticeDate, url) {
  const t = text2.replace(/\s+/g, " ");
  const out = [];
  const period = /(?:year|period|quarter|half year)\s+ended\s+([A-Za-z]+ \d{1,2},? \d{4})/i.exec(t);
  const periodEnd = period ? parseDate(period[1]) : null;
  const base = { symbol, sourceUrl: url, periodEnd };
  const tail = String.raw`\s*cash dividend\b[^@]{0,200}?@\s*Rs\.?\s*([\d.]+)\s*\/?-?\s*per share(?:\s*,?\s*(?:i\.?\s*e\.?)?\s*([\d.]+)\s*(?:%|percent))?`;
  const labelled = [...t.matchAll(new RegExp(String.raw`\b((?:first |second |third )?interim|final|special)` + tail, "gi"))];
  const matches = labelled.length ? labelled.map((m) => [m[0], m[1], m[2], m[3]]) : [...t.matchAll(new RegExp(tail, "gi"))].map((m) => [m[0], void 0, m[1], m[2]]);
  for (const m of matches) {
    const amount = num(m[2]);
    if (amount === null) continue;
    const label = (m[1] ?? "").toLowerCase().trim() || "cash";
    out.push({ ...base, kind: "cash_dividend", date: noticeDate, label, amount, pct: num(m[3]), detail: `${label} cash dividend Rs ${amount} per share` });
  }
  const bonus = /\bbonus (?:shares|issue)\b[^.]{0,160}?([\d.]+)\s*(?:%|percent)/i.exec(t);
  if (bonus) out.push({ ...base, kind: "bonus", date: noticeDate, label: "bonus", pct: num(bonus[1]), detail: `bonus issue ${bonus[1]}%` });
  const right = /\bright (?:shares|issue)\b[^.]{0,200}?([\d.]+)\s*(?:%|percent)/i.exec(t);
  if (right) out.push({ ...base, kind: "right", date: noticeDate, label: "right", pct: num(right[1]), detail: `right issue ${right[1]}%` });
  const closure = new RegExp(String.raw`closed from\s+${DATE}\s+to\s+${DATE}`, "i").exec(t);
  const from = closure ? parseDate(closure[1]) : null;
  if (from) out.push({ ...base, kind: "book_closure", date: from, endDate: parseDate(closure[2]), label: "book closure", detail: "share transfer books closed (entitlement cut-off)" });
  return out;
}
function parseMufapNavs(html) {
  const $ = cheerio__namespace.load(html);
  const table = $("#table_id").length ? $("#table_id") : $("table").first();
  const head = table.find("tr").first().find("th,td").map((_, x) => clean($(x).text()).toLowerCase()).get();
  const col = (name) => head.findIndex((h) => h === name);
  const ix = { sector: col("sector"), amc: col("amc"), fund: col("fund"), category: col("category"), offer: col("offer"), rep: col("repurchase"), nav: col("nav"), valid: col("validity date") };
  if (ix.fund < 0 || ix.nav < 0) throw new ParserMatchedNothing("MUFAP header");
  const rows = [];
  const seen = /* @__PURE__ */ new Set();
  table.find("tr").slice(1).each((_, tr) => {
    const c = $(tr).find("td").map((__, td) => clean($(td).text())).get();
    const fundName = c[ix.fund];
    const nav = num(c[ix.nav]);
    const date = parseDate(c[ix.valid]);
    if (!fundName || nav === null || nav <= 0 || !date) return;
    const key = `${fundName}|${date}`;
    if (seen.has(key)) return;
    seen.add(key);
    rows.push({
      fundName,
      date,
      amc: c[ix.amc] ?? "",
      category: c[ix.category] ?? "",
      sector: c[ix.sector],
      nav,
      offer: num(c[ix.offer]),
      repurchase: num(c[ix.rep]),
      validityDate: date
    });
  });
  if (!rows.length) throw new ParserMatchedNothing("MUFAP rows");
  return rows;
}
function parseGold(html) {
  const text2 = clean(cheerio__namespace.load(html)("body").text());
  const asOn = /As on (?:[A-Za-z]{3}, )?([A-Za-z]{3,9} \d{1,2},? \d{4})/i.exec(text2);
  const date = asOn && parseDate(asOn[1]) || pkToday();
  const k24 = /24 karat is Rs\.?\s*([\d,.]+)/i.exec(text2) ?? /Rs\.?\s*([\d,.]+)\s*24 Karat Gold Rate \(1 Tola\)/i.exec(text2);
  const k22 = /22 Karat Gold price for today is Rs\.?\s*([\d,.]+)/i.exec(text2);
  const rows = [];
  const v24 = num(k24?.[1]);
  if (v24) rows.push({ kind: "gold_24k_tola", date, value: v24, meta: { source: "gold.pk" } });
  const v22 = num(k22?.[1]);
  if (v22) rows.push({ kind: "gold_22k_tola", date, value: v22, meta: { source: "gold.pk" } });
  if (!rows.length) throw new ParserMatchedNothing("gold.pk tola rate");
  return rows;
}
function parseDollar(html) {
  const $ = cheerio__namespace.load(html);
  const asOn = /as on (?:[A-Za-z]{3}, )?([A-Za-z]{3,9} \d{1,2},? \d{4})/i.exec(clean($("body").text()));
  const date = asOn && parseDate(asOn[1]) || pkToday();
  let buy = null;
  let sell = null;
  $("tr").each((_, tr) => {
    const c = $(tr).children("td,th").map((__, x) => clean($(x).text())).get();
    if (c.length >= 4 && c.length <= 6 && c[1] === "USD") {
      buy = num(c[2]);
      sell = num(c[3]);
      return false;
    }
    return void 0;
  });
  if (buy === null || sell === null) throw new ParserMatchedNothing("forex.pk USD row");
  return [
    { kind: "usd_open_buy", date, value: buy, meta: { source: "forex.pk" } },
    { kind: "usd_open_sell", date, value: sell, meta: { source: "forex.pk" } }
  ];
}
const CURRENCY_API_URLS = [
  "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json",
  "https://latest.currency-api.pages.dev/v1/currencies/usd.json"
];
function parseCurrencyApiUsdPkr(json, source) {
  const data = JSON.parse(json);
  const pkr = data.usd?.pkr;
  if (!data.date || !/^\d{4}-\d{2}-\d{2}$/.test(data.date) || typeof pkr !== "number" || !(pkr > 50 && pkr < 5e3)) throw new ParserMatchedNothing("USD/PKR reference rate");
  return { kind: "usd_ref", date: data.date, value: Math.round(pkr * 100) / 100, meta: { source, note: "international reference rate, not the open-market selling rate" } };
}
const SCHEMES = [
  [/Defence-Saving/i, "dsc", "Defence Savings Certificates"],
  [/^BSC/i, "bsc", "Behbood Savings Certificates (also PBA, SFWA)"],
  [/^RIC/i, "ric", "Regular Income Certificates"],
  [/^SavingAccount/i, "sa", "Savings Account"],
  [/^SISA/i, "sisa", "Shuhada Islamic Savings Account"],
  [/^SITA/i, "sita", "Sarwa Islamic Term Account"]
];
function parseSavingsLinks(html) {
  const $ = cheerio__namespace.load(html);
  const out = [];
  const seen = /* @__PURE__ */ new Set();
  $("a[href$='.pdf']").each((_, a) => {
    const url = $(a).attr("href");
    const file = url.split("/").pop() ?? "";
    const hit = SCHEMES.find(([re]) => re.test(file));
    if (!hit || seen.has(hit[1])) return;
    seen.add(hit[1]);
    out.push({ url, scheme: hit[1], label: hit[2] });
  });
  return out;
}
function parseSavingsPdfText(text2, scheme, label) {
  const line = text2.split(/\r?\n/).find((l) => /\d{1,2}-[A-Za-z]{3,5}-\d{2}\s+Till Date/i.test(l));
  if (!line) return null;
  const from = /(\d{1,2}-[A-Za-z]{3,5}-\d{2})/.exec(line);
  const pct = /(\d+(?:\.\d+)?)%/.exec(line.slice(line.toLowerCase().indexOf("till date")));
  if (!pct || !from) return null;
  const since = parseDate(from[1].replace(/-([A-Za-z]{3})[A-Za-z]*-/, "-$1-"));
  return { kind: `savings_${scheme}`, date: since ?? (/* @__PURE__ */ new Date()).toISOString().slice(0, 10), value: Number(pct[1]), meta: { label, since } };
}
function findCpiReportLink(homeHtml) {
  const $ = cheerio__namespace.load(homeHtml);
  let href = null;
  $("a[href*='monthly-inflation-report-for-']").each((_, a) => {
    href = $(a).attr("href") ?? null;
    return false;
  });
  return href;
}
function findCpiDocxLink(reportHtml) {
  const $ = cheerio__namespace.load(reportHtml);
  let href = null;
  $("a[href$='.docx']").each((_, a) => {
    const h = $(a).attr("href") ?? "";
    if (/review/i.test(h) || /review/i.test($(a).text())) {
      href = h;
      return false;
    }
    return void 0;
  });
  return href;
}
const MONTH_NUM = {
  january: "01",
  february: "02",
  march: "03",
  april: "04",
  may: "05",
  june: "06",
  july: "07",
  august: "08",
  september: "09",
  october: "10",
  november: "11",
  december: "12"
};
function parseCpiText(text2) {
  const t = text2.replace(/\s+/g, " ");
  const yoy = /CPI inflation General,? (increased|decreased) by (\d+(?:\.\d+)?)% on year-on-year basis in ([A-Za-z]+) (\d{4})/i.exec(t);
  if (!yoy) throw new ParserMatchedNothing("PBS CPI year-on-year sentence");
  const month = MONTH_NUM[yoy[3].toLowerCase()];
  if (!month) throw new ParserMatchedNothing("PBS CPI month");
  const date = `${yoy[4]}-${month}-01`;
  const sign = (w) => /decreased/i.test(w) ? -1 : 1;
  const rows = [{ kind: "cpi_yoy", date, value: sign(yoy[1]) * Number(yoy[2]), meta: { month: `${yoy[3]} ${yoy[4]}` } }];
  const mom = /month-on-month basis,? it (increased|decreased) by (\d+(?:\.\d+)?)%/i.exec(t);
  if (mom) rows.push({ kind: "cpi_mom", date, value: sign(mom[1]) * Number(mom[2]) });
  return rows;
}
const parser$1 = new Parser({
  customFields: { item: [["content:encoded", "contentEncoded"]] }
});
async function parseRss(xml, maxItems = 100) {
  let feed;
  try {
    feed = await parser$1.parseString(xml);
  } catch (err) {
    throw new ParserMatchedNothing(`feed not readable: ${err.message}`);
  }
  const rows = [];
  for (const it of (feed.items ?? []).slice(0, maxItems)) {
    const url = it.link?.trim();
    const title = clean(it.title);
    if (!url || !title) continue;
    const html = it.contentEncoded ?? it["content:encoded"] ?? it.content ?? "";
    const content = htmlToText$1(html);
    const summary = clean(it.contentSnippet ?? htmlToText$1(it.summary ?? "")).slice(0, 600);
    const published = it.isoDate ?? (it.pubDate ? new Date(it.pubDate).toISOString() : (/* @__PURE__ */ new Date()).toISOString());
    rows.push({
      externalId: (it.guid || it.id || url).slice(0, 500),
      url,
      title: title.slice(0, 500),
      summary: summary || void 0,
      content: content && content !== summary ? content : void 0,
      publishedAt: Number.isNaN(Date.parse(published)) ? (/* @__PURE__ */ new Date()).toISOString() : new Date(published).toISOString()
    });
  }
  if (!rows.length) throw new ParserMatchedNothing("RSS items");
  return rows;
}
async function parseGoogleNews(xml) {
  const rows = await parseRss(xml, 100);
  return rows.map((r) => {
    const m = / - ([^-]{2,60})$/.exec(r.title);
    const publisher = m?.[1]?.trim();
    return {
      ...r,
      externalId: r.url,
      title: publisher ? r.title.slice(0, -m[0].length) : r.title,
      summary: publisher ? `Source: ${publisher}` : void 0,
      content: void 0
    };
  });
}
function isGoogleRedirect(url) {
  return /^https:\/\/news\.google\.com\/(rss\/)?articles\//.test(url);
}
const parser = new Parser({
  customFields: {
    feed: ["yt:channelId"],
    item: [
      ["yt:videoId", "videoId"],
      ["media:group", "mediaGroup"]
    ]
  }
});
async function parseYoutubeFeed(xml, feedUrl) {
  const feed = await parser.parseString(xml);
  const fromUrl = feedUrl ? /channel_id=([\w-]+)/.exec(feedUrl)?.[1] : void 0;
  const raw = feed["yt:channelId"] ?? "";
  const channelId = fromUrl ?? (raw && !raw.startsWith("UC") ? `UC${raw}` : raw);
  const channelName = clean(feed.title);
  const videos = [];
  const news = [];
  for (const it of feed.items ?? []) {
    const videoId = it.videoId ?? /v=([\w-]{11})/.exec(it.link ?? "")?.[1];
    if (!videoId || !it.title) continue;
    const description = clean(it.mediaGroup?.["media:description"]?.[0] ?? "").slice(0, 5e3);
    const publishedAt = it.isoDate ?? (/* @__PURE__ */ new Date()).toISOString();
    videos.push({ channelId, channelName, videoId, title: clean(it.title), publishedAt, description });
    news.push({
      externalId: videoId,
      url: `https://www.youtube.com/watch?v=${videoId}`,
      title: `${clean(it.title)} (YouTube: ${channelName})`,
      summary: description.slice(0, 600) || void 0,
      content: description || void 0,
      publishedAt
    });
  }
  if (!videos.length) throw new ParserMatchedNothing("YouTube feed entries");
  return { videos, news };
}
let pdfModule = null;
function loadPdfParse() {
  if (!pdfModule) {
    pdfModule = (async () => {
      const g = globalThis;
      if (typeof g.DOMMatrix === "undefined") {
        const canvas = await import("@napi-rs/canvas");
        g.DOMMatrix = canvas.DOMMatrix;
        g.ImageData = canvas.ImageData;
        g.Path2D = canvas.Path2D;
      }
      const req = node_module.createRequire(__filename);
      const mod = req("pdf-parse");
      if (process.versions.electron) {
        const { getPath } = req("pdf-parse/worker");
        mod.PDFParse.setWorker(node_url.pathToFileURL(getPath()).href);
      }
      return mod;
    })();
    pdfModule.catch(() => {
      pdfModule = null;
    });
  }
  return pdfModule;
}
async function pdfText(data) {
  const { PDFParse } = await loadPdfParse();
  const p = new PDFParse({ data: new Uint8Array(data) });
  try {
    const { text: text2 } = await p.getText();
    return text2;
  } finally {
    await p.destroy();
  }
}
async function docxText(data) {
  const { value } = await mammoth.extractRawText({ buffer: data });
  return value;
}
const MAX_QUOTE_SYMBOLS = 25;
const MAX_GNEWS_RESOLVE = 20;
const MAX_NOTICE_DOCS = 3;
const NOTICE_MAX_AGE_DAYS = 120;
const NOTICE_MAX_BYTES = 25 * 1024 * 1024;
async function main(job, get) {
  const r = await get(job.url, { etag: job.etag, lastModified: job.lastModified });
  return r.status === 304 ? null : r;
}
const text = (r) => r.body.toString("utf8");
const notModified = (job) => ({ notModified: true, harvest: {}, etag: job.etag, lastModified: job.lastModified });
async function resolveGoogleLink(url, get) {
  try {
    const r = await get(url, { redirect: "manual", noRetry: true });
    if (r.location && !isGoogleRedirect(r.location)) return r.location;
  } catch {
  }
  return url;
}
const SENSORS = {
  sbp: async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    return { harvest: { rates: parseSbp(text(r)) }, etag: r.etag, lastModified: r.lastModified };
  },
  /** Index levels, plus who is in KSE-100 / KMI-30 / KMI All Share (membership changes twice a year, so it is read live). */
  "psx-indices": async (job, get) => {
    const r = await main(job, get);
    const harvest = r ? { indexLevels: parsePsxIndices(text(r)) } : {};
    const warnings = [];
    harvest.indexMembers = [];
    for (const ix of MEMBER_INDICES) {
      try {
        harvest.indexMembers.push(parsePsxIndexMembers(text(await get(ix.url)), ix.code, ix.min));
      } catch (err) {
        warnings.push(`${ix.code} constituents: ${err.message}`);
      }
    }
    if (!r) return { notModified: !harvest.indexMembers.length, harvest, etag: job.etag, lastModified: job.lastModified, warnings };
    return { harvest, etag: r.etag, lastModified: r.lastModified, warnings };
  },
  "psx-eod": async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    const eod = parsePsxEodText(await pdfText(r.body));
    const warnings = [];
    let closing = { prices: [], events: [] };
    try {
      closing = parsePsxClosingSummary(await pdfText((await get(closingRatesUrl(eod.date))).body));
    } catch (err) {
      warnings.push(`closing rates: ${err.message}`);
    }
    return { harvest: { indexLevels: eod.indexLevels, rates: eod.rates, prices: closing.prices, events: closing.events }, etag: r.etag, lastModified: r.lastModified, warnings };
  },
  /** One company page per watched symbol per day (PSX policy). */
  "psx-quotes": async (job, get) => {
    const symbols = [...new Set((job.extra?.symbols ?? []).map((s) => s.toUpperCase()))].slice(0, MAX_QUOTE_SYMBOLS);
    const harvest = {
      prices: [],
      companies: [],
      announcements: [],
      financials: [],
      events: [],
      checkedDocs: []
    };
    const warnings = [];
    const checked = new Set(job.extra?.checkedDocs ?? []);
    const noticeFrom = new Date(Date.now() - NOTICE_MAX_AGE_DAYS * 864e5).toISOString().slice(0, 10);
    for (const sym of symbols) {
      try {
        const r = await get(`https://dps.psx.com.pk/company/${encodeURIComponent(sym)}`);
        const page = parsePsxCompany(text(r), sym);
        harvest.prices.push(page.price);
        harvest.companies.push(page.company);
        harvest.announcements.push(...page.announcements);
        harvest.financials.push(...page.financials);
        for (const a of page.announcements) {
          if (harvest.checkedDocs.length >= MAX_NOTICE_DOCS) break;
          if (a.kind !== "results" || a.date < noticeFrom || !/\.pdf$/i.test(a.url) || checked.has(a.url)) continue;
          checked.add(a.url);
          harvest.checkedDocs.push(a.url);
          try {
            const doc = await get(a.url, { maxBytes: NOTICE_MAX_BYTES });
            harvest.events.push(...parsePayoutNotice(await pdfText(doc.body), sym, a.date, a.url));
          } catch (err) {
            warnings.push(`${sym} notice: ${err.message}`);
          }
        }
      } catch (err) {
        warnings.push(`${sym}: ${err.message}`);
      }
    }
    if (symbols.length && !harvest.prices.length) throw new Error(`no quotes fetched (${warnings.join("; ")})`);
    return { harvest, warnings };
  },
  mufap: async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    return { harvest: { fundNavs: parseMufapNavs(text(r)) }, etag: r.etag, lastModified: r.lastModified };
  },
  gold: async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    return { harvest: { rates: parseGold(text(r)) }, etag: r.etag, lastModified: r.lastModified };
  },
  /** Open-market rate from forex.pk; if that fails, the international reference rate as a labelled backup. */
  dollar: async (job, get) => {
    let primaryError;
    try {
      const r = await main(job, get);
      if (!r) return notModified(job);
      return { harvest: { rates: parseDollar(text(r)) }, etag: r.etag, lastModified: r.lastModified };
    } catch (err) {
      primaryError = err;
    }
    const reason = primaryError?.message ?? String(primaryError);
    for (const url of CURRENCY_API_URLS) {
      try {
        const row = parseCurrencyApiUsdPkr(text(await get(url, { noRetry: true })), new URL(url).host);
        return { harvest: { rates: [row] }, etag: job.etag, lastModified: job.lastModified, warnings: [`forex.pk failed (${reason}); saved the international reference rate instead`] };
      } catch {
      }
    }
    throw primaryError;
  },
  savings: async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    const links = parseSavingsLinks(text(r));
    if (!links.length) throw new Error("parser matched nothing (savings PDF links)");
    const rates = [];
    const warnings = [];
    for (const l of links) {
      try {
        const pdf = await get(l.url, { accept: "application/pdf" });
        const row = parseSavingsPdfText(await pdfText(pdf.body), l.scheme, l.label);
        if (row) rates.push(row);
        else warnings.push(`${l.scheme}: no "Till Date" row`);
      } catch (err) {
        warnings.push(`${l.scheme}: ${err.message}`);
      }
    }
    if (!rates.length) throw new Error(`parser matched nothing (savings rates: ${warnings.join("; ")})`);
    return { harvest: { rates }, etag: r.etag, lastModified: r.lastModified, warnings };
  },
  cpi: async (job, get) => {
    const home = await get(job.url);
    const reportUrl = findCpiReportLink(text(home));
    if (!reportUrl) throw new Error("parser matched nothing (PBS inflation report link)");
    const report = await get(reportUrl);
    const docxUrl = findCpiDocxLink(text(report));
    if (!docxUrl) throw new Error("parser matched nothing (PBS monthly review .docx)");
    const docx = await get(docxUrl, { accept: "application/octet-stream" });
    return { harvest: { rates: parseCpiText(await docxText(docx.body)) } };
  },
  rss: async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    return { harvest: { news: await parseRss(text(r)) }, etag: r.etag, lastModified: r.lastModified };
  },
  "google-news": async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    const known = new Set(job.extra?.knownExternalIds ?? []);
    const fresh = (await parseGoogleNews(text(r))).filter((n) => !known.has(n.externalId));
    fresh.sort((a, b) => b.publishedAt.localeCompare(a.publishedAt));
    for (const n of fresh.slice(0, MAX_GNEWS_RESOLVE)) n.url = await resolveGoogleLink(n.url, get);
    return { harvest: { news: fresh }, etag: r.etag, lastModified: r.lastModified };
  },
  "youtube-feed": async (job, get) => {
    const r = await main(job, get);
    if (!r) return notModified(job);
    const { videos, news } = await parseYoutubeFeed(text(r), job.url);
    return { harvest: { videos, news }, etag: r.etag, lastModified: r.lastModified };
  }
};
async function runSensor(job, get = httpGet) {
  const fn = SENSORS[job.kind];
  if (!fn) throw new Error(`unknown sensor kind ${job.kind}`);
  return fn(job, get);
}
const MAX_BYTES = 5 * 1024 * 1024;
const MAX_REDIRECTS = 5;
const MAX_TEXT = 1e5;
const SNIPPET_MAX = 300;
const SEARCH_HEADERS = {
  "User-Agent": USER_AGENT,
  Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
  "Accept-Language": "en-PK,en;q=0.9"
};
class WebError extends Error {
}
function isPrivateAddress(ip) {
  if (node_net.isIP(ip) === 4) {
    const [a, b] = ip.split(".").map(Number);
    return a === 0 || a === 10 || a === 127 || a === 169 && b === 254 || a === 172 && b >= 16 && b <= 31 || a === 192 && b === 168 || a === 100 && b >= 64 && b <= 127;
  }
  const v6 = ip.toLowerCase().replace(/^\[|\]$/g, "");
  if (v6.startsWith("::ffff:")) return isPrivateAddress(v6.slice(7));
  return v6 === "::" || v6 === "::1" || v6.startsWith("fc") || v6.startsWith("fd") || v6.startsWith("fe8") || v6.startsWith("fe9") || v6.startsWith("fea") || v6.startsWith("feb");
}
async function assertPublicUrl(raw) {
  let u;
  try {
    u = new URL(raw);
  } catch {
    throw new WebError("Not a valid URL.");
  }
  if (u.protocol !== "http:" && u.protocol !== "https:") throw new WebError("Only http and https URLs are allowed.");
  if (u.username || u.password) throw new WebError("URLs with a user name or password are not allowed.");
  const host = u.hostname.replace(/^\[|\]$/g, "");
  if (/^localhost$|\.localhost$|\.local$|\.internal$|\.lan$/i.test(host)) throw new WebError("Refused: that address is on this PC or the local network.");
  const addrs = node_net.isIP(host) ? [host] : (await promises$1.lookup(host, { all: true }).catch(() => [])).map((a) => a.address);
  if (!addrs.length) throw new WebError(`Could not resolve ${host}.`);
  if (addrs.some(isPrivateAddress)) throw new WebError("Refused: that address is on this PC or the local network.");
  return u;
}
async function readCapped(res) {
  if (!res.body) return Buffer.alloc(0);
  const reader = res.body.getReader();
  const chunks = [];
  let total = 0;
  for (; ; ) {
    const { done, value } = await reader.read();
    if (done) break;
    total += value.length;
    if (total > MAX_BYTES) {
      await reader.cancel();
      throw new WebError("Response is larger than 5 MB.");
    }
    chunks.push(value);
  }
  return Buffer.concat(chunks);
}
async function guardedFetch(url, init) {
  let current = url;
  for (let hop = 0; hop <= MAX_REDIRECTS; hop++) {
    await assertPublicUrl(current);
    const res = await netFetch(current, { method: init.method, headers: init.headers, redirect: "manual", signal: AbortSignal.timeout(init.timeoutMs) });
    const loc = res.headers.get("location");
    if (res.status >= 300 && res.status < 400 && loc) {
      await res.body?.cancel();
      current = new URL(loc, current).href;
      continue;
    }
    return { res, finalUrl: current };
  }
  throw new WebError(`More than ${MAX_REDIRECTS} redirects.`);
}
const DROP = "script, style, noscript, svg, iframe, template, form, nav, footer, header, aside, [role=navigation], [aria-hidden=true]";
const BLOCKS = "p, div, section, article, main, li, tr, h1, h2, h3, h4, h5, h6, blockquote, pre, table, ul, ol, dl, dt, dd, figcaption";
function htmlToText(html, opts = {}) {
  const $ = cheerio__namespace.load(html);
  const title = $("title").first().text().trim();
  $(DROP).remove();
  const article = $("article").first();
  const main2 = $("main").first();
  const root = article.length && article.text().trim().length > 500 ? article : main2.length && main2.text().trim().length > 500 ? main2 : $("body").first();
  if (opts.links) {
    root.find("a[href]").each((_, a) => {
      const href = $(a).attr("href") ?? "";
      if (!/^(https?:|\/)/.test(href)) return;
      let abs = href;
      try {
        abs = new URL(href, opts.baseUrl).href;
      } catch {
      }
      $(a).append(` [${abs}]`);
    });
  }
  root.find("br").replaceWith("\n");
  root.find(BLOCKS).each((_, el) => {
    $(el).prepend("\n").append("\n");
  });
  root.find("td, th").append(" | ");
  const text2 = root.text().replace(/[ \t ]+/g, " ").replace(/ *\n */g, "\n").replace(/\n{3,}/g, "\n\n").trim();
  return { title, text: text2 };
}
const TEXTISH = /text|json|xml|javascript|csv/i;
async function fetchPage(req) {
  const raw = req.mode === "raw";
  const headers = raw ? { "User-Agent": USER_AGENT, Accept: "application/json, text/plain, */*", ...req.headers ?? {} } : { ...BROWSER_HEADERS, ...req.headers ?? {} };
  const { res, finalUrl } = await guardedFetch(req.url, { method: req.method ?? "GET", headers, timeoutMs: req.timeoutMs ?? 2e4 });
  const contentType = res.headers.get("content-type") ?? "";
  const keep = ["content-type", "content-length", "last-modified", "date", "etag"];
  const outHeaders = Object.fromEntries(keep.map((k) => [k, res.headers.get(k)]).filter(([, v]) => v));
  if (req.method === "HEAD") {
    await res.body?.cancel();
    return { url: finalUrl, status: res.status, contentType, headers: outHeaders, title: "", text: "", chars: 0 };
  }
  const body = await readCapped(res);
  let title = "";
  let text2;
  const isPdf = /pdf/i.test(contentType) || body.subarray(0, 5).toString("latin1") === "%PDF-";
  if (isPdf) text2 = (await pdfText(body)).replace(/\n{3,}/g, "\n\n").trim();
  else if (/wordprocessingml/i.test(contentType)) text2 = (await docxText(body)).replace(/\n{3,}/g, "\n\n").trim();
  else if (!TEXTISH.test(contentType) && contentType && !/html/i.test(contentType)) throw new WebError(`Not a text page (content-type ${contentType}).`);
  else if (!raw && /html/i.test(contentType || "html")) ({ title, text: text2 } = htmlToText(body.toString("utf8"), { links: req.links, baseUrl: finalUrl }));
  else text2 = body.toString("utf8");
  return { url: finalUrl, status: res.status, contentType, headers: raw ? outHeaders : void 0, title, text: text2.slice(0, MAX_TEXT), chars: text2.length };
}
const RANGE_DAYS = { day: 1, week: 7, month: 31, year: 366 };
const cut = (s, n = SNIPPET_MAX) => {
  const t = s.replace(/\s+/g, " ").trim();
  return t.length > n ? `${t.slice(0, n - 1)}…` : t;
};
async function exa(q, key, n, recency) {
  const res = await netFetch("https://api.exa.ai/search", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-api-key": key },
    body: JSON.stringify({
      query: q,
      type: "auto",
      numResults: n,
      contents: { text: { maxCharacters: SNIPPET_MAX * 2, verbosity: "compact" } },
      ...recency ? { startPublishedDate: new Date(Date.now() - RANGE_DAYS[recency] * 864e5).toISOString() } : {}
    }),
    signal: AbortSignal.timeout(15e3)
  });
  if (res.status === 401 || res.status === 403) throw new WebError("Exa key is not valid.");
  if (!res.ok) throw new WebError(`Exa HTTP ${res.status}`);
  const data = await res.json();
  return { engine: "exa", results: data.results.map((r) => ({ title: r.title ?? "", url: r.url, snippet: cut(r.text ?? ""), date: r.publishedDate?.slice(0, 10) })) };
}
async function tavily(q, key, n, recency) {
  const res = await netFetch("https://api.tavily.com/search", {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${key}` },
    // "basic" costs one credit; "advanced" costs two.
    body: JSON.stringify({ api_key: key, query: q, search_depth: "basic", max_results: Math.min(n, 20), include_answer: true, include_raw_content: false, ...recency ? { time_range: recency } : {} }),
    signal: AbortSignal.timeout(2e4)
  });
  if (res.status === 401) throw new WebError("Tavily key is not valid.");
  if (!res.ok) throw new WebError(`Tavily HTTP ${res.status}`);
  const data = await res.json();
  return { engine: "tavily", answer: data.answer ? cut(data.answer, 600) : void 0, results: data.results.map((r) => ({ title: r.title, url: r.url, snippet: cut(r.content), date: r.published_date?.slice(0, 10) })) };
}
let ddgNext = 0;
async function ddgGate() {
  const now = Date.now();
  const at = Math.max(now, ddgNext);
  ddgNext = at + 3e3;
  if (at > now) await new Promise((r) => setTimeout(r, at - now));
}
function ddgRealUrl(href) {
  try {
    const u = new URL(href, "https://duckduckgo.com");
    return u.searchParams.get("uddg") ?? u.href;
  } catch {
    return href;
  }
}
function parseDdgHtml(html, n) {
  const $ = cheerio__namespace.load(html);
  const out = [];
  $(".result").each((_, el) => {
    if (out.length >= n) return;
    if ($(el).hasClass("result--ad")) return;
    const a = $(el).find("a.result__a").first();
    const url = ddgRealUrl(a.attr("href") ?? "");
    if (!/^https?:/.test(url) || /duckduckgo\.com\/y\.js/.test(url)) return;
    out.push({ title: cut(a.text(), 200), url, snippet: cut($(el).find(".result__snippet").text()) });
  });
  return out;
}
async function ddg(q, n, recency) {
  const df = recency ? { day: "d", week: "w", month: "m", year: "y" }[recency] : void 0;
  for (let attempt = 1; attempt <= 2; attempt++) {
    await ddgGate();
    const res = await netFetch("https://html.duckduckgo.com/html/", {
      method: "POST",
      headers: { ...SEARCH_HEADERS, "Content-Type": "application/x-www-form-urlencoded", Referer: "https://html.duckduckgo.com/" },
      body: new URLSearchParams({ q, kl: "pk-en", ...df ? { df } : {} }),
      signal: AbortSignal.timeout(15e3)
    });
    if (!res.ok) throw new WebError(`DuckDuckGo HTTP ${res.status}`);
    const html = await res.text();
    if (html.includes("anomaly-modal") || html.includes("challenge-form")) {
      if (attempt === 2) throw new WebError("DuckDuckGo is showing a bot check. Add a free Exa or Tavily key in Settings → Search for reliable search.");
      await new Promise((r) => setTimeout(r, 1e3 + Math.random() * 800));
      continue;
    }
    const results = parseDdgHtml(html, n);
    if (!results.length) throw new WebError("DuckDuckGo returned no results.");
    return { engine: "duckduckgo", results };
  }
  throw new WebError("DuckDuckGo failed.");
}
async function ddgInstant(q, n) {
  const res = await netFetch(`https://api.duckduckgo.com/?q=${encodeURIComponent(q)}&format=json&no_html=1&skip_disambig=1`, {
    headers: { "User-Agent": USER_AGENT, Accept: "application/json" },
    signal: AbortSignal.timeout(1e4)
  });
  if (!res.ok) throw new WebError(`DuckDuckGo Instant HTTP ${res.status}`);
  const d = await res.json();
  const results = [];
  if (d.AbstractURL && d.AbstractText) results.push({ title: d.Heading || q, url: d.AbstractURL, snippet: cut(d.AbstractText) });
  for (const t of (d.RelatedTopics ?? []).flatMap((x) => x.Topics ? x.Topics : [x])) {
    if (results.length >= n) break;
    if (t.FirstURL && t.Text) results.push({ title: cut(t.Text, 120), url: t.FirstURL, snippet: cut(t.Text) });
  }
  if (!results.length) throw new WebError("DuckDuckGo Instant has no entry for this query.");
  return { engine: "duckduckgo-instant", results };
}
async function search(req) {
  const n = Math.min(Math.max(req.maxResults ?? 5, 1), 10);
  const engines = [];
  if (req.keys?.exa) engines.push(["exa", () => exa(req.query, req.keys.exa, n, req.recency)]);
  if (req.keys?.tavily) engines.push(["tavily", () => tavily(req.query, req.keys.tavily, n, req.recency)]);
  engines.push(["duckduckgo", () => ddg(req.query, n, req.recency)]);
  engines.push(["duckduckgo-instant", () => ddgInstant(req.query, n)]);
  const failures = [];
  for (const [name, run] of engines) {
    try {
      const r = await run();
      return { ...r, results: r.results.slice(0, n), failed: failures.length ? failures : void 0 };
    } catch (err) {
      failures.push(`${name}: ${err.message}`);
    }
  }
  throw new WebError(`All search engines failed. ${failures.join("; ")}`);
}
async function fileText(path) {
  const ext = node_path.extname(path).toLowerCase();
  if (!protocol.FILE_TYPES.includes(ext)) throw new WebError(`Igent cannot read ${ext || "this"} files. Use PDF, DOCX, TXT or MD.`);
  const info = await promises.stat(path);
  if (!info.isFile()) throw new WebError("Not a file.");
  if (info.size > protocol.MAX_FILE_BYTES) throw new WebError(`The file is too large (${Math.round(info.size / 1048576)} MB). The limit is ${protocol.MAX_FILE_BYTES / 1048576} MB.`);
  const data = await promises.readFile(path);
  const raw = ext === ".pdf" ? await pdfText(data) : ext === ".docx" ? await docxText(data) : data.toString("utf8");
  const text2 = raw.replace(/\r\n/g, "\n").replace(/\n{3,}/g, "\n\n").trim();
  return { url: path, status: 200, contentType: ext.slice(1), title: node_path.basename(path), text: text2.slice(0, MAX_TEXT), chars: text2.length };
}
async function runWeb(req) {
  return req.op === "search" ? search(req) : req.op === "file" ? fileText(req.path) : fetchPage(req);
}
setFetchImpl(electron.net.fetch);
const port = process.parentPort;
port.on("message", (e) => {
  const msg = e.data;
  if (!msg || typeof msg !== "object" || !msg.id) return;
  if ("web" in msg) {
    void runWeb(msg.web).then(
      (out) => port.postMessage({ id: msg.id, ok: true, out }),
      (err) => port.postMessage({ id: msg.id, ok: false, error: err?.message ?? String(err) })
    );
    return;
  }
  const job = msg;
  void (async () => {
    let reply;
    try {
      const out = await runSensor(job);
      reply = { id: job.id, ok: true, ...out };
    } catch (err) {
      reply = { id: job.id, ok: false, error: err?.message ?? String(err) };
    }
    port.postMessage(reply);
  })();
});
port.postMessage({ id: "__ready__", ok: true, harvest: {} });
