package com.igent.mobile.domain.engine

import kotlin.math.pow

data class ScamRule(
    val re: Regex,
    val flag: String,
    val why: String,
    val strong: Boolean = false
)

data class RuleFlag(
    val flag: String,
    val why: String
)

object ScamRules {
    val RULES = listOf(
        ScamRule(
            Regex("advance fee|upfront fee|processing fee|membership fee|registration fee|hidden charges", RegexOption.IGNORE_CASE),
            "Upfront fee or charges",
            "Legitimate investments don't ask for fees before you see any return."
        ),
        ScamRule(
            Regex("guaranteed return|fixed return|sure profit|no risk|risk-free", RegexOption.IGNORE_CASE),
            "Guaranteed or fixed returns",
            "All investments carry risk. Anyone promising fixed returns with no risk is lying.",
            strong = true
        ),
        ScamRule(
            Regex("referral bonus|downline|binary plan|matrix plan|level income", RegexOption.IGNORE_CASE),
            "Multi-level marketing (MLM) or pyramid scheme",
            "MLM schemes primarily make money from recruiting new members, not from selling products. These are often illegal pyramid schemes.",
            strong = true
        ),
        ScamRule(
            Regex("earn passively|easy money|work from home|no effort", RegexOption.IGNORE_CASE),
            "Easy money / no effort required",
            "Legitimate earnings require work, skill, or capital. Be skeptical of claims that promise high returns for little to no effort."
        ),
        ScamRule(
            Regex("unregulated|unregistered|no license|offshore", RegexOption.IGNORE_CASE),
            "Unregulated or unlicensed entity",
            "Invest only with entities regulated by the SECP or SBP. Unregulated entities offer no investor protection.",
            strong = true
        ),
        ScamRule(
            Regex("secret formula|exclusive access|insider tip", RegexOption.IGNORE_CASE),
            "Secret or exclusive investment",
            "Scammers often create a sense of urgency and exclusivity to pressure you into investing without due diligence."
        ),
        ScamRule(
            Regex("bitcoin|crypto|forex|commodities\\s+trading\\s+bot", RegexOption.IGNORE_CASE),
            "High-risk, complex product with promised software returns",
            "Automated trading bots for highly volatile assets like crypto or forex often make unrealistic promises. Understand the underlying asset and trading strategy thoroughly.",
            strong = true
        ),
        ScamRule(
            Regex("committee|kameti|bc (scheme|group)|gold (scheme|committee)|plot (scheme|booking) .{0,30}(profit|return)", RegexOption.IGNORE_CASE),
            "Informal committee or scheme",
            "Committees and informal gold or plot schemes have no legal protection if the organiser disappears."
        )
    )

    val STANDARD_CHECKS = listOf(
        "Search the company's exact name on the SECP website (secp.gov.pk). For an investment scheme, check it holds a licence for that business, not just a company registration.",
        "For a bank or a bank product, check SBP's list of banks. For a share broker, check the PSX list of TRE certificate holders.",
        "Pay only into an account in the licensed company's own name, and get a receipt. Never pay into a person's account or mobile wallet.",
        "Compare the promised return with the safe rate (T-bills, money market funds, National Savings). Much higher with 'no risk' is not possible.",
        "Read SECP's JamaPunji investor education site for warning signs, and ask someone you trust before you pay.",
        "To report a scam: SECP's complaint portal (SDMS), or the FIA Cyber Crime Wing for online fraud."
    )

    fun highReturnFlag(text: String): RuleFlag? {
        val re = Regex("(\\d+(?:\\.\\d+)?)\\s?%\\s*(?:profit|return|munafa|interest)?\\s*(?:per|a|an|every|each|/|har)?\\s*(day|daily|week|weekly|month|monthly|mahina|mahine|rozana)\\b", RegexOption.IGNORE_CASE)
        val matches = re.findAll(text)
        for (m in matches) {
            val pctStr = m.groupValues[1]
            val pct2 = pctStr.toDoubleOrNull() ?: continue
            val unit = m.groupValues[2].lowercase()
            val monthly = unit.matches(Regex("month|monthly|mahina|mahine"))
            if ((monthly && pct2 >= 3.0) || (!monthly && pct2 > 0.0)) {
                val fullMatch = m.value.trim()
                val annualRate = if (monthly) Math.round(((1.0 + pct2 / 100.0).pow(12.0) - 1.0) * 100.0) else null
                val why = if (monthly) {
                    "$pct2% a month is about $annualRate% a year, far above what safe options or even good shares pay. Returns like this usually come from new members' money."
                } else {
                    "Daily or weekly profit figures are a classic sign of a Ponzi or trading scam."
                }
                return RuleFlag("Very high return claimed ($fullMatch)", why)
            }
        }
        return null
    }

    fun ruleFlags(text: String): List<RuleFlag> {
        val out = mutableListOf<RuleFlag>()
        highReturnFlag(text)?.let { out.add(it) }
        for (r in RULES) {
            if (r.re.containsMatchIn(text)) {
                out.add(RuleFlag(r.flag, r.why))
            }
        }
        return out
    }

    fun ruleVerdict(flags: List<RuleFlag>): String {
        val strongFlags = RULES.filter { it.strong }.map { it.flag }.toSet()
        val strongCount = flags.count { f -> strongFlags.contains(f.flag) || f.flag.startsWith("Very high return") }
        return when {
            strongCount >= 1 && flags.size >= 2 -> "likely_scam"
            strongCount >= 1 || flags.size >= 2 -> "suspicious"
            flags.isNotEmpty() -> "suspicious"
            else -> "low_risk"
        }
    }
}
