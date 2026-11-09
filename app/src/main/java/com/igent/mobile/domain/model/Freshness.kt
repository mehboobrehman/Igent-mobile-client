package com.igent.mobile.domain.model

sealed interface Freshness {
    object Fresh : Freshness
    data class Stale(val since: Long) : Freshness
    object Missing : Freshness
}
