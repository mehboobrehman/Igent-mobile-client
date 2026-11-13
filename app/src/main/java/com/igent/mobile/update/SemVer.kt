package com.igent.mobile.update

data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val prerelease: String? = null,
    val build: String? = null
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)

        // Prerelease comparison:
        // A version with a prerelease is considered lower than the corresponding version without a prerelease.
        if (prerelease == null && other.prerelease != null) return 1
        if (prerelease != null && other.prerelease == null) return -1
        if (prerelease != null && other.prerelease != null) {
            return comparePrerelease(prerelease, other.prerelease)
        }
        return 0
    }

    private fun comparePrerelease(p1: String, p2: String): Int {
        val parts1 = p1.split(".")
        val parts2 = p2.split(".")
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val s1 = parts1.getOrNull(i)
            val s2 = parts2.getOrNull(i)
            if (s1 == null) return -1
            if (s2 == null) return 1

            val num1 = s1.toIntOrNull()
            val num2 = s2.toIntOrNull()
            if (num1 != null && num2 != null) {
                if (num1 != num2) return num1.compareTo(num2)
            } else if (num1 != null && num2 == null) {
                return -1 // Numeric identifiers always have lower precedence than non-numeric identifiers
            } else if (num1 == null && num2 != null) {
                return 1
            } else {
                val cmp = s1.compareTo(s2)
                if (cmp != 0) return cmp
            }
        }
        return 0
    }

    override fun toString(): String {
        val sb = StringBuilder("$major.$minor.$patch")
        if (!prerelease.isNullOrEmpty()) sb.append("-$prerelease")
        if (!build.isNullOrEmpty()) sb.append("+$build")
        return sb.toString()
    }

    companion object {
        private val semVerRegex = Regex("""^v?(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z\.-]+))?(?:\+([0-9A-Za-z\.-]+))?$""")

        fun parse(versionStr: String?): SemVer? {
            if (versionStr.isNullOrBlank()) return null
            val match = semVerRegex.matchEntire(versionStr.trim()) ?: return null
            val (major, minor, patch, prerelease, build) = match.destructured
            return SemVer(
                major = major.toIntOrNull() ?: return null,
                minor = minor.toIntOrNull() ?: return null,
                patch = patch.toIntOrNull() ?: return null,
                prerelease = prerelease.ifEmpty { null },
                build = build.ifEmpty { null }
            )
        }
    }
}
