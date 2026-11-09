package com.igent.mobile.ui

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Chat : Screen("chat")
    object Stocks : Screen("stocks")
    object StockCheckup : Screen("stock_checkup")
    object Portfolio : Screen("portfolio")
    object Screener : Screen("screener")
    object Rss : Screen("rss")
    object Analysis : Screen("analysis")
    object IdeaCards : Screen("idea_cards")
    object Learning : Screen("learning")
    object Quiz : Screen("quiz")
    object PracticeBook : Screen("practice_book")
    object Settings : Screen("settings")
    object NotificationSettings : Screen("notification_settings")
    object Debug : Screen("debug")
}
