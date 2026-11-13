package com.igent.mobile.update

import org.junit.Assert.*
import org.junit.Test

class SemVerTest {

    @Test
    fun testParseValidVersions() {
        val v1 = SemVer.parse("1.2.3")
        assertNotNull(v1)
        assertEquals(1, v1!!.major)
        assertEquals(2, v1.minor)
        assertEquals(3, v1.patch)
        assertNull(v1.prerelease)

        val v2 = SemVer.parse("v2.0.0-beta.1+build.123")
        assertNotNull(v2)
        assertEquals(2, v2!!.major)
        assertEquals(0, v2.minor)
        assertEquals(0, v2.patch)
        assertEquals("beta.1", v2.prerelease)
        assertEquals("build.123", v2.build)
    }

    @Test
    fun testParseInvalidVersions() {
        assertNull(SemVer.parse("invalid"))
        assertNull(SemVer.parse("1.2"))
        assertNull(SemVer.parse(""))
        assertNull(SemVer.parse(null))
    }

    @Test
    fun testComparison() {
        val v1 = SemVer.parse("1.1.0")!!
        val v2 = SemVer.parse("1.2.0")!!
        val v3 = SemVer.parse("1.1.1")!!
        val vBeta = SemVer.parse("1.1.0-beta.1")!!
        val vBeta2 = SemVer.parse("1.1.0-beta.2")!!

        assertTrue(v2 > v1)
        assertTrue(v3 > v1)
        assertTrue(v1 > vBeta) // Stable > prerelease
        assertTrue(vBeta2 > vBeta)
        assertEquals(0, v1.compareTo(SemVer.parse("v1.1.0")!!))
    }
}
