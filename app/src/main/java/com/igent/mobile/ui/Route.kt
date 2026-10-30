package com.igent.mobile.ui

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Chat : Screen("chat")
    object Stocks : Screen("stocks")
    object Rss : Screen("rss")
    object Analysis : Screen("analysis")
    object IdeaCards : Screen("idea_cards")
    object Learning : Screen("learning")
    object Settings : Screen("settings")
    object NotificationSettings : Screen("notification_settings")
    object Debug : Screen("debug")
}
